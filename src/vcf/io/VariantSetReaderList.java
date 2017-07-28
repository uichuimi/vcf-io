package vcf.io;

import vcf.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.logging.Logger;
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
 * while (readerList.hasNext()) {
 * final List&lt;Variant&gt; variants = readerList.next();
 * for (Variant variant : variants) {
 * // do something with each variant
 * }
 * }
 * }
 * </code>
 * <p>
 * If you are not using each variant individually, maybe you want to use the merged version:
 * </p>
 * <code>
 * try (VariantSetReaderList readerList = new VariantSetReaderList(new LinkedList&lt;&gt;(files))) {
 * while (readerList.hasNext()) {
 * final Variant variant = readerList.nextMerged();
 * // do something with the variant
 * }
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
        final List<Variant> next = new LinkedList<>();
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
        // These variants DO have same Coordinate
        final Coordinate coordinate = variants.get(0).getCoordinate();
        final String ref = collectReferences(variants);
        final List<String> alts = variants.stream()
                .map(Variant::getAltArray).flatMap(Arrays::stream)
                .distinct()
                .collect(Collectors.toList());
        final String[] alternatives = alts.toArray(new String[alts.size()]);
        final Variant variant = new Variant(coordinate.getChrom(), coordinate.getPosition(), ref, alternatives, header);
        final String[] alleles = variant.getAlleles();

        for (Variant other : variants) {
            if (variant.getId().equals(VariantSet.EMPTY_VALUE)
                    && !other.getId().equals(VariantSet.EMPTY_VALUE))
                variant.setId(other.getId());
            other.getInfo().foreach((key, value) ->
                    variant.getInfo().set(key, value));
            header.getSamples().forEach(sample -> {
                header.getIdList("FORMAT").forEach(key -> {
                    final String value = other.getSampleInfo().getFormat(sample, key);
                    if (value != null) {
                        if (key.equals("GT")) {
                            try {
                                variant.getSampleInfo().setFormat(sample, key, reindexGT(alleles, other.getAlleles(), value));
                            } catch (ArrayIndexOutOfBoundsException e) {
                                System.err.println(variant);
                                System.err.println(other);

                            }
                        } else
                            variant.getSampleInfo().setFormat(sample, key, value);
                    }
                });
            });
        }
        return variant;
    }

    private String reindexGT(String[] targetAlleles, String[] sourceAlleles, String sourceGT) {
        if (sourceGT.contains("."))
            return sourceGT;
        final boolean phased = sourceGT.contains("\\|");
        final String separator = phased ? "\\|" : "/";
        final String[] gts = sourceGT.split(separator);
        final String alleleA = sourceAlleles[Integer.valueOf(gts[0])];
        final String alleleB = sourceAlleles[Integer.valueOf(gts[1])];
        final int newGt0 = Arrays.binarySearch(targetAlleles, alleleA);
        final int newGt1 = Arrays.binarySearch(targetAlleles, alleleB);
        return String.format("%s%s%s", newGt0, separator, newGt1);
    }

    private String collectReferences(List<Variant> variants) {
        final List<String> references = variants.stream().map(Variant::getRef)
                .distinct().collect(Collectors.toList());
        if (references.size() > 1) {
            final String message = String.format("At coordinate %s," +
                            " variants do not share the same reference: %s." +
                            " Will use first one (%s)",
                    variants.get(0).getCoordinate(), references, references.get(0));
            Logger.getLogger(getClass().getName()).warning(message);
        }
        return references.get(0);
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
        readers.stream().map(VariantSetReader::header).forEach(vcfHeader ->
                vcfHeader.getHeaderLines().forEach(sourceHeader -> {
                    if (sourceHeader.getClass() == SimpleHeaderLine.class)
                        addSimpleHeader((SimpleHeaderLine) sourceHeader);
                    if (sourceHeader.getClass() == ComplexHeaderLine.class)
                        addComplexHeader((ComplexHeaderLine) sourceHeader);
                }));
    }

    private void addSimpleHeader(SimpleHeaderLine sourceHeader) {
        if (headerContainsSimpleHeaderLine(sourceHeader)) return;
        header.getHeaderLines().add(sourceHeader);
    }

    private boolean headerContainsSimpleHeaderLine(SimpleHeaderLine sourceHeader) {
        for (SimpleHeaderLine headerLine : header.getSimpleHeaders())
            if (headerLine.getKey().equals(sourceHeader.getKey())
                    && headerLine.getValue().equals(sourceHeader.getValue()))
                return true;
        return false;
    }

    private void addComplexHeader(ComplexHeaderLine sourceHeader) {
        if (headerContainsComplexHeaderLine(sourceHeader)) return;
        header.getHeaderLines().add(sourceHeader);

    }

    private boolean headerContainsComplexHeaderLine(ComplexHeaderLine sourceHeader) {
        for (ComplexHeaderLine headerLine : header.getComplexHeaders()) {
            if (headerLine.getKey().equals(sourceHeader.getKey())
                    && headerLine.getValue("ID").equals(sourceHeader.getValue("ID")))
                return true;
        }
        return false;
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
