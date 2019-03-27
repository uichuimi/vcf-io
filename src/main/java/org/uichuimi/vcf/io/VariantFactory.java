/*
 * Copyright (c) UICHUIMI 2017
 *
 * This file is part of VariantCallFormat.
 *
 * VariantCallFormat is free software:
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * VariantCallFormat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with VariantCallFormat.
 *
 * If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package org.uichuimi.vcf.io;

import org.uichuimi.vcf.Variant;
import org.uichuimi.vcf.header.ComplexHeaderLine;
import org.uichuimi.vcf.header.VcfHeader;
import org.uichuimi.vcf.variant.ValueUtils;
import org.uichuimi.vcf.variant.VariantException;
import org.uichuimi.vcf.variant.VariantSet;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Factory to create Variants. Use method
 * <code>createVariant(line, header)</code> to get a new Variant. Line should be
 * a String corresponding to a VCF line in a text VCF file.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VariantFactory {

	private static final Set<String> warnings = new LinkedHashSet<>();
	private static final String VCF_SEPARATOR = "\t";

	/**
	 * Generates a new Variant using line to populate.
	 *
	 * @param line      a VCF line
	 * @param vcfHeader the owner VariantSet
	 * @return a vcf representing the line in the VCF variantSet
	 */
	public static Variant createVariant(String line, VcfHeader vcfHeader)
			throws VariantException {
		final String[] v = line.split(VCF_SEPARATOR);
		if (v.length < 8) throw new VariantException("Variant line does not contain enough columns: " + line);
		final Variant variant = getBaseVariant(vcfHeader, v);
		if (!v[2].equals(VariantSet.EMPTY_VALUE))
			variant.setId(v[2]);
		if (!v[5].equals(VariantSet.EMPTY_VALUE))
			try {
				variant.setQual(Double.valueOf(v[5]));
			} catch (NumberFormatException e) {
				throw new VariantException(e.getMessage());
			}
		if (!v[6].equals(VariantSet.EMPTY_VALUE))
			variant.setFilter(v[6]);
		parseInfo(variant, v[7], false);
		addSamples(variant, v, vcfHeader.getSamples());
		return variant;
	}

	/**
	 * This method is specific for loading only parts of a variant.
	 *
	 * @param line       variant line
	 * @param header     header to assign to variant
	 * @param fileHeader header loaded from file
	 * @param loadId     true to load the ID
	 * @param loadQual   true to load QUAL
	 * @param loadFilter true to load FILTER
	 * @return a new variant with header as VcfHeader
	 * @throws VariantException
	 */
	public static Variant createVariant(String line, VcfHeader header, VcfHeader fileHeader, boolean loadId,
	                                    boolean loadQual, boolean loadFilter) throws VariantException {
		final String[] v = line.split(VCF_SEPARATOR);
		if (v.length < 8) throw new VariantException("Variant line does not contain enough columns: " + line);
		final Variant variant = getBaseVariant(header, v);
		if (loadId && !v[2].equals(VariantSet.EMPTY_VALUE))
			variant.setId(v[2]);
		if (loadQual && !v[5].equals(VariantSet.EMPTY_VALUE)) {
			try {
				variant.setQual(Double.valueOf(v[5]));
			} catch (NumberFormatException e) {
				throw new VariantException(e.getMessage());
			}
		}
		if (loadFilter && !v[6].equals(VariantSet.EMPTY_VALUE))
			variant.setFilter(v[6]);
		parseInfo(variant, v[7], true);
		addSamples(variant, v, fileHeader.getSamples());
		return variant;
	}

	private static Variant getBaseVariant(VcfHeader header, String[] v) {
		final String chrom = v[0];
		final int pos = Integer.valueOf(v[1]);
		final String ref = v[3];
		final String alt = v[4];
		return new Variant(chrom, pos, ref, alt, header);
	}

	/**
	 * Strictly to header
	 *
	 * @param variant
	 * @param line
	 * @param samples list of samples
	 * @throws VariantException
	 */
	private static void addSamples(Variant variant, String[] line, List<String> samples) throws VariantException {
		if (line.length > 8) {
			final String[] keys = line[8].split(":");
			final int numberOfSamples = line.length - 9;
			assertNumberOfSamples(samples, numberOfSamples);
			for (int i = 0; i < numberOfSamples; i++) {
				final String sample = samples.get(i);
				if (variant.getVcfHeader().getSamples().contains(sample)) {
					final String[] values = line[i + 9].split(":");
					for (int j = 0; j < values.length; j++)
						if (variant.getVcfHeader().hasComplexHeader("FORMAT", keys[j]))
							variant.getSampleInfo().setFormat(sample, keys[j], values[j]);
				}
			}
		}

	}

	private static void assertNumberOfSamples(List<String> samples, int numberOfSamples) throws VariantException {
		if (numberOfSamples != samples.size()) {
			final String message = "Bad line format, should be " + samples.size() + " samples";
			throw new VariantException(message);
		}
	}

	private static void parseInfo(Variant variant, String info, boolean strict) {
		if (info.equals(VariantSet.EMPTY_VALUE))
			return;
		final List<String> idList = variant.getVcfHeader().getIdList("INFO");
		if (strict && idList.isEmpty()) return;
		final String[] fields = info.split(";");
		for (String field : fields) {
			final String[] pair = field.split("=");
			final String key = pair[0];
			if (strict && !idList.contains(key))
				continue;
			final String type = getInfoType(variant, key);
			if (pair.length > 1) {
				final String value = pair[1];
				variant.getInfo().set(key, ValueUtils.getValue(value, type));
			} else {
				if (type.equals("Flag")) variant.getInfo().set(key, true);
				else
					raiseWarning(key + " is not Flag and has missing value in " + variant);
			}
		}
	}

	private static String getInfoType(Variant variant, String id) {
		final ComplexHeaderLine info = variant.getVcfHeader().getComplexHeader("INFO", id);
		if (info == null) {
			raiseWarning(id + " not found in INFO headers, assuming Type=String");
			return "String";
		}
		return info.getValue("Type");
	}

	private static void raiseWarning(String message) {
		if (!warnings.contains(message))
			Logger.getLogger(VariantFactory.class.getName()).warning(message);
		warnings.add(message);
	}
}
