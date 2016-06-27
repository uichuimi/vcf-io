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

import utils.OS;
import utils.StringStore;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class SampleInfo {

    private Map<String, Map<String, String>> content = new TreeMap<>();
    private Variant variant;

    SampleInfo(Variant variant) {
        this.variant = variant;
    }

    public void setFormat(String sample, String key, String value) {
        sample = StringStore.getInstance(sample);
        key = StringStore.getInstance(key);
        value = StringStore.getInstance(value);
        if (!content.containsKey(sample)) content.put(sample, new TreeMap<>());
        content.get(sample).put(key, value);
    }

    public String getFormat(String sample, String key) {
        return content.containsKey(sample) ? content.get(sample).getOrDefault(key, VariantSet.EMPTY_VALUE) : VariantSet.EMPTY_VALUE;
    }

    @Override
    public String toString() {
        if (content.isEmpty()) return "";
        final List<String> formatKeys = variant.getVariantSet().getHeader().getIdList("FORMAT");
        if (formatKeys.isEmpty()) return "";
        final List<String> samples = variant.getVariantSet().getHeader().getSamples();
        final String FORMAT = OS.asString(":", formatKeys);
        final StringBuilder builder = new StringBuilder("\t").append(FORMAT);
        for (String sample : samples) {
            final List<String> values = formatKeys.stream()
                    .map(key -> content.containsKey(sample) ? content.get(sample).getOrDefault(key, VariantSet
                            .EMPTY_VALUE) : VariantSet.EMPTY_VALUE)
                    .collect(Collectors.toList());
            builder.append("\t").append(OS.asString(":", values));
        }
        return builder.toString();
    }

    public boolean isHomozigote(String sample) {
        final String[] gt = getGenotype(sample);
        return gt != null && !gt[0].matches("[0|.]") && gt[0].equals(gt[1]);
    }

    public boolean isHeterozygote(String sample) {
        final String[] gt = getGenotype(sample);
        return gt != null &&  !gt[0].equals(gt[1]);
    }

    public boolean isAffected(String sample) {
        final String[] gt = getGenotype(sample);
        return gt != null && (!gt[0].matches("[0|.]") || !gt[1].matches("[0|.]"));
    }

    private String[] getGenotype(String sample) {
        final String gt = content.containsKey(sample) ? content.get(sample).getOrDefault("GT", null) : null;
        return gt == null ? null : gt.split("[/|]");
    }
}
