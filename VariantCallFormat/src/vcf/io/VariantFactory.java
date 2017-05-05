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

package vcf.io;

import vcf.ValueUtils;
import vcf.Variant;
import vcf.VariantException;
import vcf.VcfHeader;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
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
     * @param line      a VCF line
     * @param vcfHeader the owner VariantSet
     * @return a vcf representing the line in the VCF variantSet
     */
    public static Variant createVariant(String line, VcfHeader vcfHeader) throws VariantException {

        final String[] v = line.split("\t");
        final String chrom = v[0];
        final int pos = Integer.valueOf(v[1]);
        final String ref = v[3];
        final String alt = v[4];
        final Variant variant = new Variant(chrom, pos, ref, alt, vcfHeader);
        try {
            variant.setQual(Double.valueOf(v[5]));
        } catch (Exception ignored) {
            ignored.printStackTrace();
            return null;
        }
//        variant.setVcfHeader(vcfHeader);
        variant.setId(v[2]);
        variant.setFilter(v[6]);
        addInfos(variant, v[7]);
        addSamples(variant, v);
        return variant;
    }

    private static void addInfos(Variant variant, String field) {
        if (field.equals(".")) return;
        final String[] split = field.split(";");
        for (String s : split) {
            if (s.contains("=")) {
                final String[] pair = s.split("=");
                final String key = pair[0];
                final String value = pair[1];
                final String type = getType(variant, key);
                variant.getInfo().set(key, ValueUtils.getValue(value, type));
            } else {
                final String type = getType(variant, s);
                if (type.equals("Flag")) variant.getInfo().set(s, true);
                else raiseWarning(s + " is not Flag and has missing value in " + variant);
            }
        }
    }

    private static void addSamples(Variant variant, String[] line) throws VariantException {
        if (line.length > 8) {
            final String[] keys = line[8].split(":");
            final int numberOfSamples = line.length - 9;
            assertNumberOfSamples(variant.getVcfHeader(), numberOfSamples);
            for (int i = 0; i < numberOfSamples; i++) {
                final String[] values = line[i + 9].split(":");
                for (int j = 0; j < values.length; j++) {
                    if (!values[j].equals("."))
                        variant.getSampleInfo().setFormat(variant.getVcfHeader().getSamples().get(i), keys[j], values[j]);
                }
            }
        }
    }

    public static Variant createVariant(String line, VcfHeader header, VcfHeader readHeader, boolean loadId,
                                        boolean loadQual, boolean loadFilter) throws VariantException {
        final String[] v = line.split("\t");
        final String chrom = v[0];
        final int pos = Integer.valueOf(v[1]);
        final String ref = v[3];
        final String alt = v[4];
        final Variant variant = new Variant(chrom, pos, ref, alt);
        variant.setVcfHeader(header);
        if (loadId) variant.setId(v[2]);
        if (loadFilter) variant.setFilter(v[6]);
        if (loadQual) {
            try {
                variant.setQual(Double.valueOf(v[5]));
            } catch (NumberFormatException ignored) {

            }
        }
        if (!header.getComplexHeaders().get("INFO").isEmpty())
            addInfosStrictly(variant, v[7]);
        if (!header.getComplexHeaders().get("FORMAT").isEmpty() && !header.getSamples().isEmpty())
            addSamples(variant, readHeader, v);
        return variant;
    }

    private static void addSamples(Variant variant, VcfHeader readHeader, String[] line) throws VariantException {
        if (line.length > 8) {
            final String[] keys = line[8].split(":");
            final int numberOfSamples = line.length - 9;
            assertNumberOfSamples(readHeader, numberOfSamples);
            for (int i = 0; i < numberOfSamples; i++) {
                final String sample = readHeader.getSamples().get(i);
                if (variant.getVcfHeader().getSamples().contains(sample)) {
                    final String[] values = line[i + 9].split(":");
                    for (int j = 0; j < values.length; j++) {
                        if (variant.getVcfHeader().hasComplexHeader("FORMAT", keys[j]))
                            variant.getSampleInfo().setFormat(sample, keys[j], values[j]);
                    }
                }
            }
        }

    }

    private static void assertNumberOfSamples(VcfHeader header, int numberOfSamples) throws VariantException {
        if (numberOfSamples != header.getSamples().size()) {
            final String message = "Bad line format, should be " + header.getSamples().size() + " samples";
            throw new VariantException(message);
        }
    }

    private static void addInfosStrictly(Variant variant, String info) {
        final String[] fields = info.split(";");
        final List<String> tagged = new LinkedList<>(variant.getVcfHeader().getIdList("INFO"));
        for (String field : fields) {
            final String[] pair = field.split("=");
            if (tagged.contains(pair[0])) {
                setInfo(variant, pair[0], pair.length > 1 ? pair[1] : null);
                tagged.remove(pair[0]);
                if (tagged.isEmpty()) return;
            }
        }
    }

    private static void setInfo(Variant variant, String key, String value) {
        final String type = getType(variant, key);
        if (value == null || value.isEmpty()) {
            if (!type.equals("Flag")) {
                Logger.getLogger(VariantFactory.class.getName()).severe(type + " INFO has no value: " + key);
            } else variant.getInfo().set(key, true);
        } else {
            final Object val = ValueUtils.getValue(value, type);
            variant.getInfo().set(key, val);
        }
    }

    private static String getType(Variant variant, String id) {
        if (variant.getVcfHeader().hasComplexHeader("INFO", id))
            return variant.getVcfHeader().getComplexHeader("INFO", id).get("Type");
        else
            raiseWarning(id + " not found in INFO headers, assuming Type=String");
        return "String";
    }

    private static void raiseWarning(String message) {
        if (!warnings.contains(message)) Logger.getLogger(VariantFactory.class.getName()).warning(message);
        warnings.add(message);
    }
}
