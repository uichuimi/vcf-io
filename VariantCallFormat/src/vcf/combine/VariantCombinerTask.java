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
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
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
//        Logger.getLogger(getClass().getName()).info("Joining VCFs");
        final VariantSet joinVcfs = joinVcfs();
        if (delete) deleteInvalidVariants(joinVcfs);
        return joinVcfs;
    }

    private void deleteInvalidVariants(VariantSet joinVcfs) {
        updateMessage("Deleting invalid variants");
//        Logger.getLogger(getClass().getName()).info("Deleting invalid variants");
        joinVcfs.getVariants().removeIf(variant -> samples.stream()
                .filter(sample -> !valid(variant, sample)).count() > 0);
        Logger.getLogger(getClass().getName()).info("Deleted");
    }

    private boolean valid(Variant variant, Sample sample) {
        final boolean valid = sample.getStatus() == Sample.Status.AFFECTED && variant.getSampleInfo().isAffected(sample.getName())
                || sample.getStatus() == Sample.Status.UNAFFECTED && !variant.getSampleInfo().isAffected(sample.getName())
                || sample.getStatus() == Sample.Status.HOMOZYGOTE && variant.getSampleInfo().isHomozigote(sample.getName())
                || sample.getStatus() == Sample.Status.HETEROZYGOTE && variant.getSampleInfo().isHeterozygote(sample.getName());
        if (valid) return true;
        final boolean inMist = (variant.getSampleInfo().getFormat(sample.getName(), "GT").equals(VariantSet.EMPTY_VALUE)
                || variant.getSampleInfo().getFormat(sample.getName(), "GT").equals("./.")) && inMist(variant, sample);
        if (inMist) {
            variant.getInfo().set("MIST", true);
            addMistToHeader(variant.getVariantSet().getHeader());
        }
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

    private VariantSet joinVcfs() {
        AtomicReference<VariantSet> variantSetReference = new AtomicReference<>();
        // Lets do a super join, so you can have the sum
//        final List<VariantSet> variantSets = new ArrayList<>();
        final List<File> files = new ArrayList<>();
        samples.forEach(sample -> {
            progess++;
            if (files.contains(sample.getFile())) return;
            updateMessage("Joining " + sample.getName());
            updateProgress(progess, total);
//            Logger.getLogger(getClass().getName()).info("Joining " + sample.getName());
            final VariantSet variantSet = VariantSetFactory.createFromFile(sample.getFile());
            if (variantSetReference.get() == null) variantSetReference.set(variantSet);
            else mergeVcfFiles(variantSetReference.get(), variantSet);
            files.add(sample.getFile());
//            if (variantSets.contains(sample.getVariantSet())) return;
//            Logger.getLogger(getClass().getName()).info("Joining " + sample.getName());
//            mergeVcfFiles(variantSet, sample.getVariantSet());
//            variantSets.add(sample.getVariantSet());
        });
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
        soruce.getHeader().getSimpleHeaders()
                .forEach((key, value) -> {
                    if (!target.getHeader().hasSimpleHeader(key))
                        target.getHeader().addSimpleHeader(key, value);
                });
    }

    private void addComplexHeaders(VariantSet target, VariantSet source) {
        source.getHeader().getComplexHeaders()
                .forEach((type, mapList) -> mapList.forEach(map -> {
                    if (!target.getHeader().hasComplexHeader(type, map.get("ID")))
                        target.getHeader().addComplexHeader(type, map);
                }));
    }
}
