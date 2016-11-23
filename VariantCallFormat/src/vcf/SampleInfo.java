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

    /**
     * A matrix containing FORMAT columns. To access a cell: <strong>content.get(sample).get(format)</strong>.
     */
    private Map<String, Map<String, Object>> content = new LinkedHashMap<>();
    private Variant variant;

    SampleInfo(Variant variant) {
        this.variant = variant;
    }

    public void setFormat(String sample, String key, String value) {
//        sample = StringStore.getInstance(sample);
        if (!content.containsKey(sample)) content.put(StringStore.getInstance(sample), new LinkedHashMap<>());
        final String type = variant.getVariantSet().getHeader().hasComplexHeader("FORMAT", key)
                ? variant.getVariantSet().getHeader().getComplexHeader("FORMAT", key).get("Type")
                : "String";
        content.get(sample).put(StringStore.getInstance(key), ValueUtils.getValue(value, type));
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
        return content.containsKey(sample)
                ? ValueUtils.getString(content.get(sample).getOrDefault(key, VariantSet.EMPTY_VALUE))
                : VariantSet.EMPTY_VALUE;
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
        return content.containsKey(sample) ? content.get(sample).getOrDefault(key, VariantSet.EMPTY_VALUE) : VariantSet.EMPTY_VALUE;
    }

    @Override
    public String toString() {
        if (content.isEmpty()) return "";
        final List<String> usedTags = getUsedTags();
        if (usedTags.isEmpty()) return "";
        final List<String> samples = variant.getVariantSet().getHeader().getSamples();
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
        content.forEach((sample, map) ->
                map.forEach((tag, value) -> {
                    if (value != null && !value.equals("."))
                        usedTags.add(tag);
                }));
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
        content.remove(name);
    }
}
