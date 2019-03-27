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

package org.uichuimi.variant.io.vcf.combine;

import org.uichuimi.variant.io.vcf.variant.Coordinate;
import org.uichuimi.variant.io.vcf.variant.GenotypeIndex;
import org.uichuimi.variant.io.vcf.Variant;
import org.uichuimi.variant.io.vcf.variant.VariantSet;
import org.uichuimi.variant.io.vcf.header.ComplexHeaderLine;
import org.uichuimi.variant.io.vcf.header.VcfHeader;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class VariantMerger {

	/**
	 * Returns a new Variant using information from all variants and header as VcfHeader.
	 *
	 * @param variants list of variants to merge, all of them must have the same Coordinate
	 * @param header   header for the new Variant
	 * @return a new Variant containing all the INFO and FORMAT information of the variants list
	 */
	public static Variant merge(Collection<Variant> variants, VcfHeader header) {
		if (variants.isEmpty()) return null;
		final List<Variant> variantList = new ArrayList<>(variants);
		if (variantList.size() == 1 && variantList.get(0).getVcfHeader() == header)
			return variantList.get(0);
		final Variant variant = baseVariant(variantList, header);

		mergeInfo(variantList, header, variant);

		for (Variant other : variantList) {
			// ID
			if (variant.getId().equals(VariantSet.EMPTY_VALUE)
					&& !other.getId().equals(VariantSet.EMPTY_VALUE))
				variant.setId(other.getId());
			// Qual
			if (variant.getQual() == null && other.getQual() != null)
				variant.setQual(other.getQual());
			// Filter
			if (variant.getFilter() == null && other.getFilter() != null)
				variant.setFilter(other.getFilter());
		}

		mergeSamples(header, variantList, variant);
		return variant;
	}

	private static void mergeSamples(VcfHeader header, List<Variant> variantList, Variant variant) {
		for (String sample : header.getSamples()) {
			for (ComplexHeaderLine headerLine : header.getComplexHeaders("FORMAT")) {
				final String id = headerLine.getValue("ID");
				final List<Object> objects = variantList.stream()
						.map(v -> v.getSampleInfo().getRichFormat(sample, id))
						.collect(Collectors.toList());
				final Object value = mergeField(variant, headerLine, variantList, objects);
				if (value != null) variant.getSampleInfo().setFormat(sample, id, value);
			}
		}
	}

	private static void mergeInfo(List<Variant> variants, VcfHeader header, Variant variant) {
		for (ComplexHeaderLine headerLine : header.getComplexHeaders("INFO")) {
			final String id = headerLine.getValue("ID");
			final List<Object> objects = variants.stream().map(v -> v.getInfo().get(id)).collect(Collectors.toList());
			final Object value = mergeField(variant, headerLine, variants, objects);
			if (value != null) variant.getInfo().set(id, value);
		}
	}

	private static Object mergeField(Variant variant, ComplexHeaderLine info,
	                                 List<Variant> variants, List<Object> objects) {
		final String id = info.getValue("ID");
		if (id.equals("GT")) {
			// we need to find the first object non null with a genotype not ./.
			for (int i = 0; i < objects.size(); i++) {
				final Object value = objects.get(i);
				if (value == null) continue;
				final String gt = (String) value;
				if (gt.contains(".")) continue;
				final Variant src = variants.get(i);
				return reindexGT(variant.getAlleles(), src.getAlleles(), gt);
			}
			return "./.";
		}
		switch (info.getValue("Number")) {
			case "A": {
				// A: 1 per alternative allele
				final Object[] values = new Object[variant.getAltArray().length];
				for (int index = 0; index < variants.size(); index++) {
					final Variant src = variants.get(index);
					final Object srcValue = objects.get(index);
					if (srcValue == null) continue;
					if (srcValue instanceof Object[]) {
						final Object[] array = (Object[]) srcValue;
						for (int i = 0; i < array.length; i++) {
							int idx = indexOf(variant.getAltArray(), src.getAltArray()[i]);
							// Values with the same index will be overwritten
							values[idx] = array[i];
						}
					} else {
						int idx = indexOf(variant.getAltArray(), src.getAltArray()[0]);
						values[idx] = srcValue;
					}
				}
				if (Arrays.stream(values).allMatch(Objects::isNull)) return null;
				replaceNulls(values, info.getValue("Type"));
				return values;
			}
			case "R": {
				// R: 1 per allele (reference + alternative)
				// [[R,A],[R,B],[R,C,D]] to R,A,B,C,D
				final Object[] values = new Object[variant.getAlleles().length];
				for (int index = 0; index < variants.size(); index++) {
					final Variant src = variants.get(index);
					final Object[] srcValues = (Object[]) objects.get(index);
					if (srcValues == null) continue;
					// Reference is overwritten by all variants with this value
					for (int i = 0; i < srcValues.length; i++) {
						final int idx = indexOf(variant.getAlleles(), src.getAlleles()[i]);
						values[idx] = srcValues[i];
					}
				}
				if (Arrays.stream(values).allMatch(Objects::isNull)) return null;
				replaceNulls(values, info.getValue("Type"));
				return values;
			}
			case "G":
				// G: 1 per genotype
				// Target variant will have the same or more genotypes than src variants
				// For each src variant, for each genotype, we must remap the position and copy
				// the value
				int jk = variant.getAltArray().length;
				final int maxIndex = GenotypeIndex.get(jk, jk);
				final Object[] values = new Object[1 + maxIndex];

				for (int idx = 0; idx < variants.size(); idx++) {
					final Object srcValue = objects.get(idx);
					if (srcValue == null) continue;
					final Variant src = variants.get(idx);
					final Object[] array = (Object[]) srcValue;
					for (int i = 0; i < array.length; i++) {
						int index = getIndex(i, variant, src);
						values[index] = array[i];
					}
				}
				if (Arrays.stream(values).allMatch(Objects::isNull)) return null;
				replaceNulls(values, info.getValue("Type"));
				return values;
			default:
				return objects.stream()
						.filter(Objects::nonNull)
						.findFirst()
						.orElse(null);
		}
	}

	private static int getIndex(int i, Variant variant, Variant src) {
		// 1) find k, j in src

		// Well, although there is a mathematical solution, it implies rounding and a square root.
		// As the number of alternative alleles is usually low, we can manually build the table
		// This approach is much faster and, in case of higher number of alternatives we can
		// dynamically grow the lists
		final int k = GenotypeIndex.getK(i);
		final int j = GenotypeIndex.getJ(i);

		// Just for the sake, the math approach starts as
		// To find k and j, we first suppose j = 0
		// i = (k * (k + 1) / 2)
		// .5kk + .5k - i = 0
		// Once we have k, we floor it and use it to calculate j
		// j = i - (k * (k + 1) / 2)

		// 2) map k, j to variant
		int newK = indexOf(variant.getAlleles(), src.getAlleles()[k]);
		int newJ = indexOf(variant.getAlleles(), src.getAlleles()[j]);

		// 2/1 or 1/0
		if (newK < newJ) {
			final int aux = newJ;
			newJ = newK;
			newK = aux;
		}
		// 3) call getIndexInGenotypes
		return GenotypeIndex.get(newJ, newK);
	}

	private static void replaceNulls(Object[] values, Object value) {
		if (value.equals("Float")) value = 0;
		else if (value.equals("Integer")) value = 0;
		else value = VariantSet.EMPTY_VALUE;
		for (int i = 0; i < values.length; i++)
			if (values[i] == null)
				values[i] = value;
	}

	private static int indexOf(String[] array, String value) {
		for (int i = 0; i < array.length; i++)
			if (array[i].equals(value))
				return i;
		return -1;
	}

	private static Variant baseVariant(Collection<Variant> variants, VcfHeader header) {
		// These variants DO have same Coordinate
		final Coordinate coordinate = variants.iterator().next().getCoordinate();
		final String ref = collectReferences(variants);
		final String[] alternatives = variants.stream()
				.map(Variant::getAltArray).flatMap(Arrays::stream)
				.distinct().toArray(String[]::new);
		return new Variant(coordinate.getChrom(), coordinate.getPosition(), ref, alternatives, header);
	}

	private static String reindexGT(String[] targetAlleles,
	                                String[] sourceAlleles,
	                                String sourceGT) {
		if (sourceGT.contains(".")) return sourceGT;
		final String[] gts;
		final String sep;
		if (sourceGT.contains("/")) {
			sep = "/";
			gts = sourceGT.split("/");
		} else {
			sep = "|";  // This symbol has meaning in a regex and must be escaped
			gts = sourceGT.split("\\|");
		}
		final String alleleA = sourceAlleles[Integer.valueOf(gts[0])];
		final String alleleB = sourceAlleles[Integer.valueOf(gts[1])];
		final int newGt0 = indexOf(targetAlleles, alleleA);
		final int newGt1 = indexOf(targetAlleles, alleleB);
		return String.format("%s%s%s", newGt0, sep, newGt1);
	}

	private static String collectReferences(Collection<Variant> variants) {
		final List<String> references = variants.stream().map(Variant::getRef)
				.distinct().collect(Collectors.toList());
		if (references.size() > 1) {
			final String message = String.format("At coordinate %s," +
							" variants do not share the same reference: %s." +
							" Will use first one (%s)",
					variants.iterator().next().getCoordinate(), references, references.get(0));
			Logger.getLogger(VariantMerger.class.getName()).warning(message);
		}
		return references.get(0);
	}

}
