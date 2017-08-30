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

package vcf;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class VariantMerger {

	/**
	 * Returns a new Variant using information from all variants and header as
	 * VcfHeader.
	 *
	 * @param variants list of variants to merge, all of them must have the
	 *                 same Coordinate
	 * @param header header for the new Variant
	 * @return a new Variant containing all the INFO and FORMAT information of
	 * the variants list
	 */
	public static Variant merge(List<Variant> variants, VcfHeader header) {
		final Variant variant = baseVariant(variants, header);
		final List<String> alleles = Arrays.asList(variant.getAlleles());

		mergeInfo(variants, header, variant);

		for (Variant other : variants) {
			// ID
			if (variant.getId().equals(VariantSet.EMPTY_VALUE)
					&& !other.getId().equals(VariantSet.EMPTY_VALUE))
				variant.setId(other.getId());
			mergeFormat(header, variant, alleles, other);

		}
		return variant;
	}

	private static void mergeFormat(VcfHeader header, Variant variant, List<String> alleles, Variant other) {
		// FORMAT (sample x key)
		header.getSamples().forEach(sample ->
				header.getIdList("FORMAT").forEach(key -> {
					final String value = other.getSampleInfo().getFormat(sample, key);
					if (value != null) {
						if (key.equals("GT")) {
							try {
								final List<String> otherAlleles = Arrays.asList(other.getAlleles());
								variant.getSampleInfo().setFormat(sample, key, reindexGT(alleles, otherAlleles, value));
							} catch (ArrayIndexOutOfBoundsException e) {
								System.err.println(variant);
								System.err.println(other);

							}
						} else
							variant.getSampleInfo().setFormat(sample, key, value);
					}
				}));
	}

	private static void mergeInfo(List<Variant> variants, VcfHeader header, Variant variant) {
		header.getComplexHeaders("INFO").stream()
				.map(h -> h.getValue("ID"))
				.forEach(info -> {
					final List<Object> values = variants.stream()
							.map(v -> v.getInfo().get(info))
							.collect(Collectors.toList());
					final Object value = mergeValues(info, values);
					if (value != null)
						variant.getInfo().set(info, value);
				});
	}

	private static Object mergeValues(String info, List<Object> values) {
		if (info.equals("DP"))
			return mergeDP(values);
		for (Object val : values)
			if (val != null) return val;
		return null;
	}

	private static int mergeDP(List<Object> values) {
		return values.stream().mapToInt(v -> (int) v).sum();
	}

	private static Variant baseVariant(List<Variant> variants, VcfHeader header) {
		// These variants DO have same Coordinate
		final Coordinate coordinate = variants.get(0).getCoordinate();
		final String ref = collectReferences(variants);
		final List<String> alts = variants.stream()
				.map(Variant::getAltArray).flatMap(Arrays::stream)
				.distinct()
				.collect(Collectors.toList());
		final String[] alternatives = alts.toArray(new String[alts.size()]);
		return new Variant(coordinate.getChrom(), coordinate.getPosition(), ref, alternatives, header);
	}

	private static String reindexGT(List<String> targetAlleles, List<String> sourceAlleles, String sourceGT) {
		if (sourceGT.contains("."))
			return sourceGT;
		final boolean phased = sourceGT.contains("\\|");
		final String separator = phased ? "\\|" : "/";
		final String[] gts = sourceGT.split(separator);
		final String alleleA = sourceAlleles.get(Integer.valueOf(gts[0]));
		final String alleleB = sourceAlleles.get(Integer.valueOf(gts[1]));
		final int newGt0 = targetAlleles.indexOf(alleleA);
		final int newGt1 = targetAlleles.indexOf(alleleB);
		return String.format("%s%s%s", newGt0, separator, newGt1);
	}

	private static String collectReferences(List<Variant> variants) {
		final List<String> references = variants.stream().map(Variant::getRef)
				.distinct().collect(Collectors.toList());
		if (references.size() > 1) {
			final String message = String.format("At coordinate %s," +
							" variants do not share the same reference: %s." +
							" Will use first one (%s)",
					variants.get(0).getCoordinate(), references, references.get(0));
			Logger.getLogger(VariantMerger.class.getName()).warning(message);
		}
		return references.get(0);
	}
}
