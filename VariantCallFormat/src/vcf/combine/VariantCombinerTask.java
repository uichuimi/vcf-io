/*
 * Copyright (c) UICHUIMI 2016
 *
 * This file is part of VariantCallFormat.
 *
 * VariantCallFormat is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * VariantCallFormat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package vcf.combine;

import javafx.concurrent.Task;
import vcf.*;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * Created by uichuimi on 25/05/16.
 */
public class VariantCombinerTask extends Task<VariantSet> {

    private final List<Sample> samples;
    private boolean delete;
    private int total;
    private int progess;

    public VariantCombinerTask(List<Sample> samples, boolean delete) {
        this.samples = samples;
        this.delete = delete;
    }

    @Override
    protected VariantSet call() throws Exception {
        total = samples.size();
        progess = 0;
        updateMessage("Joining files");
        final VariantSet joinVcfs = joinVcfs();
        if (delete) deleteInvalidVariants(joinVcfs);
        return joinVcfs;
    }

    private void deleteInvalidVariants(VariantSet joinVcfs) {
        updateMessage("Deleting invalid variants");
        joinVcfs.getVariants().removeIf(variant -> samples.stream()
                .filter(sample -> !valid(variant, sample)).count() > 0);
        Logger.getLogger(getClass().getName()).info("Deleted");
    }

    private boolean valid(Variant variant, Sample sample) {
        final boolean valid = sample.getStatus() == Sample.Status.AFFECTED && variant.getSampleInfo().isAffected(sample.getName())
                || sample.getStatus() == Sample.Status.UNAFFECTED && !variant.getSampleInfo().isAffected(sample.getName())
                || sample.getStatus() == Sample.Status.HOMOZYGOUS && variant.getSampleInfo().isHomozigote(sample.getName())
                || sample.getStatus() == Sample.Status.HETEROZYGOUS && variant.getSampleInfo().isHeterozygote(sample
                .getName());
        if (valid) return true;
        final boolean inMist = (variant.getSampleInfo().getFormat(sample.getName(), "GT").equals(VariantSet.EMPTY_VALUE)
                || variant.getSampleInfo().getFormat(sample.getName(), "GT").equals("./.")) && inMist(variant, sample);
        if (inMist) variant.getInfo().set("MIST", true);
        return inMist;
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

    private boolean inMist(Variant variant, Sample sample) {
        return sample.getMist() != null && sample.getMist().isInMistRegion(variant.getChrom(), variant.getPosition());
    }

    /**
     * Join all the samples in one big VCF, which contains all the variants present in any sample, regardless its status.
     *
     * @return a VariantSet which contains all the variants from all the Samples
     */
    private VariantSet joinVcfs() {
        final AtomicReference<VariantSet> variantSetReference = new AtomicReference<>();
        final List<File> files = new ArrayList<>();
        samples.forEach(sample -> {
            updateMessage("Joining " + sample.getName());
            if (files.contains(sample.getFile())) return;
            updateProgress(progess++, total);
            final VariantSet variantSet = VariantSetFactory.createFromFile(sample.getFile());
            // First VariantSet will be the reference
            if (variantSetReference.get() == null) variantSetReference.set(variantSet);
            else mergeVcfFiles(variantSetReference.get(), variantSet);
            // avoid loading the same file again
            files.add(sample.getFile());
        });
        addMistToHeader(variantSetReference.get().getHeader());
        return variantSetReference.get();
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

    private void addSimpleHeaders(VariantSet target, VariantSet soruce) {
        soruce.getHeader().getSimpleHeaders().forEach((key, value) -> {
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
}
