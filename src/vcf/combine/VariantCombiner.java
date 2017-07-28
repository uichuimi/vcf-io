package vcf.combine;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import vcf.Genotype;
import vcf.Variant;
import vcf.VariantSet;
import vcf.VcfHeader;
import vcf.io.VariantSetReaderList;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created on 12/07/16.
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VariantCombiner implements Runnable {

    private final List<Sample> samples;
    private final Property<String> message = new SimpleObjectProperty<>();
    private final Property<Double> progress = new SimpleObjectProperty<>();
    private boolean delete;
    private VariantSet variantSet;

    public VariantCombiner(List<Sample> samples, boolean delete) {
        this.samples = samples;
        this.delete = delete;
    }

    @Override
    public void run() {
        final List<File> files = samples.stream()
                .map(Sample::getFile)
                .distinct()
                .collect(Collectors.toList());
        try (VariantSetReaderList reader = new VariantSetReaderList(files)) {
            final VcfHeader header = reader.getMergedHeader();
            this.variantSet = new VariantSet(header);
            while (reader.hasNext()) {
                final Variant variant = reader.nextMerged();
                if (!delete || valid(variant))
                    variantSet.getVariants().add(variant);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean valid(Variant variant) {
        return samples.stream().allMatch(sample -> valid(variant, sample));
    }

    public VariantSet getResult() {
        return variantSet;
    }

    private boolean valid(Variant variant, Sample sample) {
        final String[] gt = getGenotype(variant, sample.getName());
        if (gt[0].equals(".") && gt[1].equals(".")) {
            if (sample.getMist() != null && sample.getMist().isInMistRegion(variant)) {
                variant.getInfo().set("MIST", true);
                return true;
            } else return sample.getGenotype() == Genotype.WILD;
        }
        return checkZygosity(sample, gt);
    }

    private boolean checkZygosity(Sample sample, String[] gt) {
        switch (sample.getGenotype()) {
            case HOMOZYGOUS:
                return !gt[0].equals("0") && gt[0].equals(gt[1]);
            case HETEROZYGOUS:
                return !gt[0].equals(gt[1]);
            case WILD:
                return gt[0].equals("0") && gt[1].equals("0");
            default:
                return !gt[0].equals("0") || !gt[1].equals("0");
        }
    }

    private String[] getGenotype(Variant variant, String sample) {
        final String genotype = variant.getSampleInfo().getFormat(sample, "GT");
        return genotype == null ? new String[]{".", "."} : genotype.split("[/|]");
    }


    public Property<String> messageProperty() {
        return message;
    }

    public Property<Double> progressProperty() {
        return progress;
    }
}
