package vcf.combine;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import vcf.Variant;
import vcf.VariantSet;
import vcf.io.VariantSetFactory;
import vcf.VcfHeader;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * Created by uichuimi on 12/07/16.
 */
public class VariantCombiner implements Runnable {

    private final List<Sample> samples;
    private final Property<String> message = new SimpleObjectProperty<>();
    private final Property<Double> progress = new SimpleObjectProperty<>();
    private boolean delete;
    private int total;
    private int progess;
    private VariantSet variantSet;

    public VariantCombiner(List<Sample> samples, boolean delete) {
        this.samples = samples;
        this.delete = delete;
    }

    @Override
    public void run() {
        total = samples.size() * 2;
        progess = 0;
        message.setValue("Joining files");
        final VariantSet variantSet = joinVcfs();
        if (delete) deleteInvalidVariants(variantSet);
        removeNonUsedSamples(variantSet);
        this.variantSet = variantSet;
    }

    public VariantSet getResult() {
        return variantSet;
    }

    private void deleteInvalidVariants(VariantSet joinVcfs) {
        message.setValue("Deleting invalid variants");
        joinVcfs.getVariants().removeIf(variant -> samples.stream()
                .filter(sample -> !valid(variant, sample)).count() > 0);
    }

    private boolean valid(Variant variant, Sample sample) {
        final String[] gt = getGenotype(variant, sample.getName());
        if (gt[0].equals(".") && gt[1].equals(".")) {
            if (sample.getMist() != null && sample.getMist().isInMistRegion(variant)) {
                variant.getInfo().set("MIST", true);
                return true;
            } else return sample.getStatus() == Sample.Status.WILD;
        }
        return checkZygosity(sample, gt);
    }

    private boolean checkZygosity(Sample sample, String[] gt) {
        switch (sample.getStatus()) {
            case HOMOZYGOUS:
                return !gt[0].equals("0") && gt[0].equals(gt[1]);
            case HETEROZYGOUS:
                return !gt[0].equals(gt[1]);
            case WILD:
                return gt[0].equals("0") && gt[1].equals("0");
            case AFFECTED:
            default:
                return !gt[0].equals("0") || !gt[1].equals("0");
        }
    }

    private String[] getGenotype(Variant variant, String sample) {
        final String genotype = variant.getSampleInfo().getFormat(sample, "GT");
        return genotype.equals(VariantSet.EMPTY_VALUE) ? new String[]{".", "."} : genotype.split("[/|]");
    }

    private void addMistToHeader(VcfHeader header) {
        if (!header.hasComplexHeader("INFO", "MIST")) {
            final Map<String, String> mist = new LinkedHashMap<>();
            mist.put("ID", "MIST");
            mist.put("Type", "Flag");
            mist.put("Number", "0");
            mist.put("Description", "Some samples fall into a MIST (low DP) region");
            header.addComplexHeader("INFO", mist);
        }
    }

    /**
     * Join all the samples in one big VCF, which contains all the variants present in any sample, regardless its status.
     *
     * @return a VariantSet which contains all the variants from all the Samples
     */
    private VariantSet joinVcfs() {
        final VariantSet variantSet = join();
        addMistToHeader(variantSet.getHeader());
        return variantSet;
    }

    private VariantSet join() {
        final AtomicReference<VariantSet> variantSetReference = new AtomicReference<>();
        final List<File> loadedFiles = new ArrayList<>();
        samples.forEach(sample -> {
            if (!loadedFiles.contains(sample.getFile())) {
                Logger.getLogger(getClass().getName()).info("Reading VCF");
                final VariantSet variantSet = loadVcf(sample);
                Logger.getLogger(getClass().getName()).info("Joining VCF");
                merge(variantSetReference, sample, variantSet);
                loadedFiles.add(sample.getFile());
            } else skipSample(sample);
        });
        Logger.getLogger(getClass().getName()).info("Done");
        return variantSetReference.get();
    }

    private void skipSample(Sample sample) {
        message.setValue("Joining " + sample.getName());
        progress.setValue((progess += 2) / (double) total);
    }

    private void merge(AtomicReference<VariantSet> variantSetReference, Sample sample, VariantSet variantSet) {
        progress.setValue(progess++ / (double) total);
        message.setValue("Merging " + sample.getName());
        // First VariantSet will be the reference
        if (variantSetReference.get() == null) variantSetReference.set(variantSet);
        else mergeVcfFiles(variantSetReference.get(), variantSet);
    }

    private VariantSet loadVcf(Sample sample) {
        progress.setValue(progess++ / (double) total);
        message.setValue("Loading " + sample.getFile());
        return VariantSetFactory.createFromFile(sample.getFile());
    }

    private void mergeVcfFiles(VariantSet target, VariantSet source) {
        addHeaders(target, source);
        addVariants(target, source);
    }

    private void addVariants(VariantSet target, VariantSet source) {
        source.getVariants().forEach(target::addOrUpdate);
    }

    private void addHeaders(VariantSet target, VariantSet source) {
        addSampleNames(target, source);
        addSimpleHeaders(target, source);
        addComplexHeaders(target, source);
    }

    private void addSampleNames(VariantSet target, VariantSet source) {
        source.getHeader().getSamples().stream()
                .filter(sample -> !target.getHeader().getSamples().contains(sample))
                .forEach(sample -> target.getHeader().getSamples().add(sample));
    }

    private void addSimpleHeaders(VariantSet target, VariantSet source) {
        source.getHeader().getSimpleHeaders().forEach((key, value) -> {
            if (!target.getHeader().hasSimpleHeader(key))
                target.getHeader().addSimpleHeader(key, value);
        });
    }

    private void addComplexHeaders(VariantSet target, VariantSet source) {
        source.getHeader().getComplexHeaders().forEach((type, mapList) -> mapList.forEach(map -> {
            if (!target.getHeader().hasComplexHeader(type, map.get("ID")))
                target.getHeader().addComplexHeader(type, map);
        }));
    }

    private void removeNonUsedSamples(VariantSet variantSet) {
        final List<String> allSampleNames = new ArrayList<>(variantSet.getHeader().getSamples());
        allSampleNames.stream()
                .filter(name -> !samples.stream().anyMatch(sample -> sample.getName().equals(name)))
                .forEach(variantSet::removeSample);
    }

    public Property<String> messageProperty() {
        return message;
    }

    public Property<Double> progressProperty() {
        return progress;
    }
}
