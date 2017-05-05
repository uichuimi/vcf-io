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

package vcf;

import utils.StringStore;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class SampleInfo {

    private static Map<String, Integer> sampleIndex;
    /**
     * A matrix containing FORMAT columns. To access a cell: <strong>content[sampleIndex.get(sample)].get(format)
     * </strong>.
     */
    private LinkedHashMap<String, Object>[] content = new LinkedHashMap[]{};
    private Variant variant;

    SampleInfo(Variant variant) {
        this.variant = variant;
        if (variant.getVcfHeader() != null) {
            final List<String> samples = variant.getVcfHeader().getSamples();
            if (sampleIndex == null) {
                sampleIndex = new HashMap<>();
                int i = 0;
                for (String sample : samples) sampleIndex.put(sample, i++);
            }
            content = new LinkedHashMap[samples.size()];
            for (int i = 0; i < samples.size(); i++)
                content[i] = new LinkedHashMap<>();
        }
    }

    public void setFormat(String sample, String key, String value) {
        Integer index = getSampleIndex(sample);
        if (index == null) return;
        while (index >= content.length) increaseContent();
        if (content[index] == null) content[index] = new LinkedHashMap<>();
        final LinkedHashMap<String, Object> map = content[index];
        final String type = variant.getVcfHeader().hasComplexHeader("FORMAT", key)
                ? variant.getVcfHeader().getComplexHeader("FORMAT", key).get("Type")
                : "String";
        map.put(StringStore.getInstance(key), ValueUtils.getValue(value, type));
    }

    private Integer getSampleIndex(String sample) {
        Integer index = sampleIndex.get(sample);
        if (index == null && variant.getVcfHeader().getSamples().contains(sample)) {
            sampleIndex.put(sample, sampleIndex.size());
            index = sampleIndex.size() - 1;
        }
        return index;
    }

    private void increaseContent() {
        final LinkedHashMap[] maps = new LinkedHashMap[content.length + 1];
        System.arraycopy(content, 0, maps, 0, content.length);
        content = maps;
    }

    /**
     * Get the value of the key FORMAT for sample. If sample or key do not exist, then the VariantSet.EMPTY_VALUE (.) is
     * returned.
     *
     * @param sample name of the sample: one of the vcf samples
     * @param key    FORMAT id
     * @return the value of key for the given sample or VariantSet.EMPTY_VALUE if not present
     */
    public String getFormat(String sample, String key) {
        final Integer index = getSampleIndex(sample);
        if (index == null) return VariantSet.EMPTY_VALUE;
        if (content == null || index >= content.length) return VariantSet.EMPTY_VALUE;
        final LinkedHashMap<String, Object> map = content[index];
        if (content[index] == null) return VariantSet.EMPTY_VALUE;
        return ValueUtils.getString(map.getOrDefault(key, VariantSet.EMPTY_VALUE));
//        return content.containsKey(sample)
//                ? ValueUtils.getString(content.get(sample).getOrDefault(key, VariantSet.EMPTY_VALUE))
//                : VariantSet.EMPTY_VALUE;
    }

    /**
     * Get the value of the key FORMAT for sample. If sample or key do not exist, then the VariantSet.EMPTY_VALUE (.) is
     * returned.
     *
     * @param sample name of the sample: one of the vcf samples
     * @param key    FORMAT id
     * @return the value of key for the given sample or VariantSet.EMPTY_VALUE if not present
     */
    public Object getRichFormat(String sample, String key) {
        final Integer index = getSampleIndex(sample);
        if (index == null) return VariantSet.EMPTY_VALUE;
        final LinkedHashMap<String, Object> map = content[index];
        return map.getOrDefault(key, VariantSet.EMPTY_VALUE);
//        return content.containsKey(sample) ? content.get(sample).getOrDefault(key, VariantSet.EMPTY_VALUE) : VariantSet.EMPTY_VALUE;
    }

    @Override
    public String toString() {
//        if (content.isEmpty()) return "";
        final List<String> usedTags = getUsedTags();
        if (usedTags.isEmpty()) return "";
        final List<String> samples = variant.getVcfHeader().getSamples();
        final String FORMAT = String.join(":", usedTags);
        final StringBuilder builder = new StringBuilder("\t").append(FORMAT);
        for (String sample : samples) {
            final List<String> values = usedTags.stream()
                    .map(key -> getFormat(sample, key))
                    .collect(Collectors.toList());
            builder.append("\t").append(String.join(":", values));
        }
        return builder.toString();
    }

    private List<String> getUsedTags() {
        final Set<String> usedTags = new LinkedHashSet<>();
        for (LinkedHashMap<String, Object> map : content) {
            if (map != null)
                map.forEach((tag, value) -> {
                    if (value != null && !value.equals("."))
                        usedTags.add(tag);
                });
        }
        return new ArrayList<>(usedTags);
    }

    public boolean isHomozygous(String sample) {
        final String[] gt = getGenotype(sample);
        return gt != null && !gt[0].matches("[0.]") && gt[0].equals(gt[1]);
    }

    public boolean isHeterozygous(String sample) {
        final String[] gt = getGenotype(sample);
        return gt != null && !gt[0].equals(gt[1]);
    }

    public boolean isAffected(String sample) {
        final String[] gt = getGenotype(sample);
        return gt != null && (!gt[0].matches("[0.]") || !gt[1].matches("[0.]"));
    }

    private String[] getGenotype(String sample) {
        final String gt = getFormat(sample, "GT");
        if (gt == null) return null;
        if (gt.equals(VariantSet.EMPTY_VALUE)) return new String[]{".", "."};
        return gt.split("[/|]");
    }

    /**
     * Removes a sample info from variant.
     *
     * @param name sample name
     */
    public void removeSample(String name) {
        final Integer index = getSampleIndex(name);
        if (index == null) return;
        content[index] = null;
//        content.remove(name);
    }
}
