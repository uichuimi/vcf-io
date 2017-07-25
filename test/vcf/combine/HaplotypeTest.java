package vcf.combine;

import org.junit.Ignore;
import vcf.Variant;
import vcf.VariantSet;
import vcf.io.VariantSetFactory;

import java.io.File;
import java.util.*;

/**
 * Created by uichuimi on 11/07/16.
 */
public class HaplotypeTest {
    private final File SQZ001 = new File("/media/uichuimi/Elements/GENOME_DATA/SQZ/SQZ_001/VCF/s001.vcf");
    private final File SQZ002 = new File("/media/uichuimi/Elements/GENOME_DATA/SQZ/SQZ_002/VCF/s002.vcf");
    private final File SQZ003 = new File("/media/uichuimi/Elements/GENOME_DATA/SQZ/SQZ_003/VCF/s003.vcf");
    private final File SQZ020 = new File("/media/uichuimi/Elements/GENOME_DATA/SQZ/SQZ_020/VCF/s020.vcf");
    private final File SQZ025 = new File("/media/uichuimi/Elements/GENOME_DATA/SQZ/SQZ_025/VCF/s025.vcf");
    private final File SQZ030 = new File("/media/uichuimi/Elements/GENOME_DATA/SQZ/SQZ_030/VCF/s030.vcf");
    private final File SQZ072 = new File("/media/uichuimi/Elements/GENOME_DATA/SQZ/SQZ_072/VCF/sqz_072.vcf");
    private final File SQZ077 = new File("/media/uichuimi/Elements/GENOME_DATA/SQZ/SQZ_077/VCF/sqz_077.vcf");

    private final List<File> files = Arrays.asList(SQZ001, SQZ002, SQZ003, SQZ020, SQZ025, SQZ030, SQZ072, SQZ077);
    private final List<String> names = Arrays.asList("sqz_001", "sqz_002", "sqz_003", "sqz_020", "sqz_025",
            "sqz_030", "sqz_072", "sqz_077");

    @Ignore()
    public void calculateEqualities() {
        final Map<String, Map<String, Long>> matrix = new LinkedHashMap<>();
        for (int i = 0; i < files.size(); i++) {
            System.out.println(files.get(i));
            final VariantSet variantSet = VariantSetFactory.createFromFile(files.get(i));
            final String sample = variantSet.getHeader().getSamples().get(0);
            matrix.put(sample, new LinkedHashMap<>());
            for (int j = i; j < files.size(); j++) {
                System.out.println("with " + files.get(j));
                if (files.get(i).equals(files.get(j))) matrix.get(sample).put(sample, 0L);
                else {
                    final VariantSet variantSet1 = VariantSetFactory.createFromFile(files.get(j));
                    final String sample1 = variantSet1.getHeader().getSamples().get(0);
                    final long equalities = equalities(variantSet, variantSet1);
                    System.out.println(equalities);
                    matrix.get(sample).put(sample1, equalities);
                }
            }
            System.out.println();
        }
        printMatrix(matrix);

    }

    private long equalities(VariantSet variantSet, VariantSet variantSet1) {
        return variantSet.getVariants().stream()
                .filter(variant -> variantSet1.findVariant(variant.getChrom(), variant.getPosition()) != null)
                .count();
    }

    private void printMatrix(Map<String, Map<String, Long>> matrix) {
        matrix.forEach((sample, map) -> System.out.println(sample + "->" + map));
    }

    @Ignore
    public void megaJoin() {
        final List<Sample> samples = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) samples.add(new Sample(files.get(i), names.get(i)));
        VariantCombiner combinerTask = new VariantCombiner(samples, false);
        combinerTask.messageProperty().addListener((observable, oldValue, newValue) -> System.out.println(newValue));
        combinerTask.run();
        final VariantSet result = combinerTask.getResult();
        result.save(new File("join.vcf"));
    }

    @Ignore
    public void findHaplotypes() {
        final Set<String> possibilities = new LinkedHashSet<>();
        final String chr = "1";
        final int start = 10000;
        final int end = 30000;
        final VariantSet join = new VariantSet();
        for (File file : files) {
            System.out.println(file);
            final VariantSet variantSet = VariantSetFactory.createFromFile(file);
            join.getHeader().getSamples().addAll(variantSet.getHeader().getSamples());
            variantSet.getHeader().getComplexHeaders()
                    .forEach((type, list) -> list.forEach(map -> join.getHeader().addComplexHeader(type, map)));
            for (int i = start; i < end; i++) {
                final Variant variant = variantSet.findVariant(chr, i);
                if (variant != null) join.addOrUpdate(variant);
            }
        }
        join.save(new File("join.vcf"));
    }

    @Ignore
    public void haplotypeCaller() {
        final File file = new File("join.vcf");
        final Map<Integer, Set<String>> haplotypes = new LinkedHashMap<>();
        final VariantSet variantSet = VariantSetFactory.createFromFile(file);
        variantSet.getVariants().forEach(variant -> {
            final String ref = variant.getRef();
            final String alt = variant.getAlt();
            haplotypes.putIfAbsent(variant.getPosition(), new LinkedHashSet<>());
            final Set<String> hts = haplotypes.get(variant.getPosition());
            variantSet.getHeader().getSamples().forEach(sample -> {
                final String[] gt = variant.getSampleInfo().getFormat(sample, "GT").split("[|/]");
                if (gt[0].equals("1")) System.out.print(alt);
                else if (gt[0].equals("0")) System.out.print(ref);
                else System.out.print(ref);
                System.out.print(" ");
                if (gt.length > 1)
                    if (gt[1].equals("1")) System.out.print(alt);
                    else if (gt[1].equals("0")) System.out.print(ref);
                    else System.out.print(ref);
                else System.out.print(ref);
                System.out.print(" ");
            });
            System.out.println();
        });
        haplotypes.forEach((position, hts) -> System.out.println(position + "->" + hts));

    }
}
