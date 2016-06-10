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

/**
 * Factory to create Variants. Use method <code>createVariant(line, file)</code> to get a new Variant. Line should be
 * a String corresponding to a VCF line in a text VCF file.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VariantFactory {

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
        double qual;
        try {
            qual = Double.valueOf(v[5]);
        } catch (Exception ignored) {
            qual = 0;
        }
        final Variant variant = new Variant(chrom, pos, ref, alt);
        variant.setVariantSet(variantSet);
        variant.setId(id);
        variant.setQual(qual);
        variant.setFilter(filter);
        addSamples(variant, v);
        addInfos(variant, v[7]);
        return variant;
    }

    private static void addInfos(Variant variant, String fieldInfo) {
        final String[] fields = fieldInfo.split(";");
        for (String field : fields) setInfo(variant, field);

    }

    private static void setInfo(Variant variant, String field) {
        if ((field.contains("="))) {
            final String[] pair = field.split("=");
            variant.getInfo().set(pair[0], pair[1]);
        } else variant.getInfo().set(field, Boolean.TRUE);
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
