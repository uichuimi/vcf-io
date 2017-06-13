package vcf.io;

import vcf.Coordinate;
import vcf.Variant;
import vcf.VcfHeader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Reads 1 or more VCF files at the same time. By repeatedly calling <code>next()</code> you can walk along genome
 * coordinates, by having all variants found in each position. Coordinates with no variants present are not reported.
 * <p>
 * Sample code:
 * </p>
 * <code>
 * try (VariantSetReaderList readerList = new VariantSetReaderList(new LinkedList&lt;&gt;(files))) {
 *   while (readerList.hasNext()) {
 *     final List&lt;Variant&gt; variants = readerList.next();
 *     for (Variant variant : variants) {
 *         // do something with each variant
 *     }
 *   }
 * }
 * </code>
 * <p>
 * If you are not using each variant individually, maybe you want to use the merged version:
 * </p>
 * <code>
 * try (VariantSetReaderList readerList = new VariantSetReaderList(new LinkedList&lt;&gt;(files))) {
 *   while (readerList.hasNext()) {
 *     final Variant variant = readerList.nextMerged();
 *     // do something with the variant
 *   }
 * }
 * </code>
 * When using the merged version, a merged header is created by joining all the vcf headers, avoiding redundant lines.
 * To merge variants, the first variant is used as base. Then, for each other, the ID is updated and the INFO and
 * FORMAT fields are filled with the missing values. NOTE that QUAL and filter values are not updated, and INFO
 * values as DP or AC are not recalculated. This is a work in progress. This version is useful when using only
 * genotype information and INFO values such as frequencies or consequences.
 */
public class VariantSetReaderList implements AutoCloseable {

    private final List<VariantSetReader> readers = new ArrayList<>();
    private final List<Variant> currentVariants;
    private VcfHeader header;

    /**
     * Creates a list of opened VariantSetReaders
     *
     * @param samples list of samples with criteria
     * @throws FileNotFoundException if any of the files is not found
     */
    public VariantSetReaderList(List<File> samples) throws FileNotFoundException {
        for (File sample : samples) readers.add(new VariantSetReader(sample));
        currentVariants = readers.stream().map(VariantSetReader::next).collect(Collectors.toList());
    }

    @Override
    public void close() throws Exception {
        for (VariantSetReader reader : readers) reader.close();
    }

    /**
     * @return true if there are remaining variants in any of the input files
     */
    public boolean hasNext() {
        return currentVariants.stream().filter(Objects::nonNull).count() > 0;
    }

    /**
     * Get a list of variants in the next coordinate where there is at least one variant
     *
     * @return a list of at least one variant in the next coordinate
     */
    public List<Variant> next() {
        final Coordinate nextCoordinate = nextCoordinate();
        final List<Variant> next = new ArrayList<>();
        IntStream.range(0, currentVariants.size()).forEach(i -> {
            if (currentVariants.get(i) != null) {
                if (currentVariants.get(i).getCoordinate().equals(nextCoordinate)) {
                    next.add(currentVariants.get(i));
                    currentVariants.set(i, readers.get(i).hasNext() ? readers.get(i).next() : null);
                }
            }
        });
        return next;
    }

    private Coordinate nextCoordinate() {
        return currentVariants.stream()
                .filter(Objects::nonNull)
                .map(Variant::getCoordinate)
                .min(Coordinate::compareTo)
                .orElse(null);
    }

    public Variant nextMerged() {
        return merge(next());
    }

    private Variant merge(List<Variant> variants) {
        if (header == null) mergeHeaders();
        final Variant variant = variants.get(0);
        final List<String> alleles = new LinkedList<>();
        for (Object alt : variant.getAltArray()) if (!alleles.contains(alt)) alleles.add((String) alt);
        variant.setVcfHeader(header);
        if (variants.size() > 1) {
            final List<String> formatKeys = header.getIdList("FORMAT");
            for (int i = 1; i < variants.size(); i++) {
                final Variant toMerge = variants.get(i);
                for (Object alt : variant.getAltArray()) if (!alleles.contains(alt)) alleles.add((String) alt);
                // Id
                if (variant.getId().equals(".") && !toMerge.getId().equals("."))
                    variant.setId(toMerge.getId());
                // Info
                for (String key : toMerge.getVcfHeader().getIdList("INFO")) {
                    if (!variant.getInfo().hasInfo(key) && toMerge.getInfo().hasInfo(key))
                        variant.getInfo().set(key, toMerge.getInfo().get(key));
                }
                // Format
                toMerge.getVcfHeader().getSamples().forEach(sample -> {
                    final String gt = variant.getSampleInfo().getFormat(sample, "GT");

                    final boolean phased = gt.contains("|");
                    final String[] gts = gt.split(phased ? "\\|" : "/");
                    final int gt0 = Integer.valueOf(gts[0]);
                    final int gt1 = Integer.valueOf(gts[1]);
                    System.out.println(variant);
                    final String r = reindexGT(variant, alleles, gt0);
                    final String a = reindexGT(variant, alleles, gt1);
                    variant.getSampleInfo().setFormat(sample, "GT", String.join(phased ? "|" : "/", r, a));
                    formatKeys.forEach(key -> variant.getSampleInfo()
                            .setFormat(sample, key, toMerge.getSampleInfo().getFormat(sample, key)));
                });
            }
        }
        return variant;
    }

    private String reindexGT(Variant variant, List<String> alleles, int gt0) {
        if (gt0 == 0) return "0";
        else {
            final String alt = (String) variant.getAltArray()[gt0 - 1];
            final int newIndex = alleles.indexOf(alt) + 1;
            return String.valueOf(newIndex);
        }
    }

    private void mergeHeaders() {
        header = new VcfHeader();
        readers.stream().map(VariantSetReader::header)
                .flatMap(vcfHeader -> vcfHeader.getSamples().stream())
                .distinct()
                .forEach(header.getSamples()::add);
        readers.stream().map(VariantSetReader::header).forEach(vcfHeader -> {
            vcfHeader.getSimpleHeaders().forEach(header::addSimpleHeader);
            vcfHeader.getComplexHeaders().forEach((type, maps) ->
                    maps.forEach(map -> header.addComplexHeader(type, map)));
        });
    }

    /**
     * Gets the header in case you want to use use the merged version
     *
     * @return the merged header
     */
    public VcfHeader getMergedHeader() {
        if (header == null) mergeHeaders();
        return header;
    }
}
