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

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Factory to create Variants. Use method <code>createVariant(line, file)</code> to get a new Variant. Line should be
 * a String corresponding to a VCF line in a text VCF file.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VariantFactory {

    private static final Set<String> warnings = new LinkedHashSet<>();

    /**
     * Generates a new Variant using line to populate.
     *
     * @param line       a VCF line
     * @param variantSet the owner VariantSet
     * @return a vcf representing the line in the VCF variantSet
     */
    public static Variant createVariant(String line, VariantSet variantSet) throws VariantException {
        final String[] v = line.split("\t");
        final String chrom = v[0];
        final int pos = Integer.valueOf(v[1]);
        final String id = v[2];
        final String ref = v[3];
        final String alt = v[4];
        final String filter = v[6];
        Double qual;
        try {
            qual = Double.valueOf(v[5]);
        } catch (Exception ignored) {
            qual = null;
        }
        final Variant variant = new Variant(chrom, pos, ref, alt);
        variant.setVariantSet(variantSet);
        variant.setId(id);
        variant.setQual(qual);
        variant.setFilter(filter);
        addInfos(variant, v[7]);
        addSamples(variant, v);
        return variant;
    }

    private static void addInfos(Variant variant, String fieldInfo) {
        final String[] fields = fieldInfo.split(";");
        for (String field : fields) setInfo(variant, field);

    }

    private static void setInfo(Variant variant, String field) {
        final String[] pair = field.split("=");
        final String id = pair[0];
        final String type;
        if (variant.getVariantSet().getHeader().hasComplexHeader("INFO", id)) {
            type = variant.getVariantSet().getHeader().getComplexHeader("INFO", id).get("Type");
        } else {
            type = "String";
            final String warning = id + " not found in INFO headers, assuming Type=String";
            if (!warnings.contains(warning)) Logger.getLogger(VariantFactory.class.getName()).warning(warning);
            warnings.add(warning);
        }
        if (pair.length == 1) {
            if (!type.equals("Flag")) {
                Logger.getLogger(VariantFactory.class.getName()).severe(type + " INFO has no value: " + field);
            } else variant.getInfo().set(id, true);
        } else {
            Object value = ValueUtils.getValue(pair[1], type);
            variant.getInfo().set(id, value);
        }

    }

    private static void addSamples(Variant variant, String[] line) throws VariantException {
        if (line.length > 8) {
            final String[] keys = line[8].split(":");
            final int numberOfSamples = line.length - 9;
            if (numberOfSamples != variant.getVariantSet().getHeader().getSamples().size()) {
                final String message = "Bad line format, should be " + variant.getVariantSet().getHeader().getSamples().size() + " samples";
                throw new VariantException(message);
            }
            for (int i = 0; i < numberOfSamples; i++) {
                final String[] values = line[i + 9].split(":");
                for (int j = 0; j < values.length; j++)
                    variant.getSampleInfo().setFormat(variant.getVariantSet().getHeader().getSamples().get(i), keys[j], values[j]);
            }
        }
    }

}
