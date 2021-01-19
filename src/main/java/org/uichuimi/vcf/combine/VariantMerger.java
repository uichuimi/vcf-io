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

package org.uichuimi.vcf.combine;

import org.uichuimi.vcf.header.FormatHeaderLine;
import org.uichuimi.vcf.header.InfoHeaderLine;
import org.uichuimi.vcf.header.VcfHeader;
import org.uichuimi.vcf.variant.Coordinate;
import org.uichuimi.vcf.variant.Info;
import org.uichuimi.vcf.variant.Variant;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class VariantMerger {

	/**
	 * Returns a new Variant using information from all variants using header as the header of the
	 * newly created variant.
	 *
	 * @param variants
	 * 		list of variants to merge, all of them must have the same Coordinate
	 * @param header
	 * 		header for the new Variant
	 * @return a new Variant containing all the INFO and FORMAT information of the variants list
	 */
	public static Variant merge(Collection<Variant> variants, VcfHeader header) {
		if (variants.isEmpty()) return null;
		final Variant variant = baseVariant(variants, header);

		for (Variant other : variants) {
			// ID
			for (String id : other.getIdentifiers())
				if (!variant.getIdentifiers().contains(id))
					variant.getIdentifiers().add(id);

			// Quality (max)
			if (variant.getQuality() == null)
				variant.setQuality(other.getQuality());
			else if (other.getQuality() != null)
				variant.setQuality(Double.max(variant.getQuality(), other.getQuality()));

			// Filter
			for (String filter : other.getFilters())
				if (!variant.getFilters().contains(filter))
					variant.getFilters().add(filter);
		}

		mergeInfo(variants, variant);
		mergeSamples(variants, variant);
		return variant;
	}

	private static void mergeInfo(Collection<Variant> variants, Variant variant) {
		for (Variant other : variants)
			for (InfoHeaderLine headerLine : variant.getHeader().getInfoLines().values())
				headerLine.mergeInto(variant, variant.getInfo(), other, other.getInfo());
	}

	private static void mergeSamples(Collection<Variant> variantList, Variant variant) {
		for (final Variant other : variantList) {
			for (int s = 0; s < other.getHeader().getSamples().size(); s++) {
				final String sample = other.getHeader().getSamples().get(s);
				final int vs = variant.getHeader().getSamples().indexOf(sample);
				if (vs < 0) continue;
				final Info sourceInfo = other.getSampleInfo(s);
				final Info targetInfo = variant.getSampleInfo(vs);
				for (FormatHeaderLine formatLine : variant.getHeader().getFormatLines().values())
					formatLine.mergeInto(variant, targetInfo, other, sourceInfo);
			}
		}
	}

	private static Variant baseVariant(Collection<Variant> variants, VcfHeader header) {
		// These variants DO have same Coordinate
		final Coordinate coordinate = variants.iterator().next().getCoordinate();
		final List<String> references = variants.stream()
				.flatMap(v -> v.getReferences().stream())
				.distinct()
				.collect(Collectors.toList());
		if (references.size() > 1) {
			final String ref = references.stream().max(Comparator.comparingInt(String::length)).orElse(null);
			// 1) ABC -> AB,     ABCD -> ? (ABD)
			// 2) ABC -> ABD,    ABCD -> ? (ABDD)
			// 3) ABC -> ABCD,   ABCD -> ? (ABCDD)
			for (Variant variant : variants) {
				if (variant.getReferences().get(0).equals(ref)) continue;
				final String oref = variant.getReferences().get(0);
				variant.getReferences().set(0, ref);
				final List<String> variantAlternatives = variant.getAlternatives();
				for (int i = 0; i < variantAlternatives.size(); i++) {
					String alternative = variantAlternatives.get(i);
					final String alt;
					if (alternative.length() < oref.length())       // 1)
						alt = ref.replaceFirst(oref, alternative);
					else if (alternative.length() == oref.length()) // 2)
						alt = ref.replaceFirst(oref, alternative);
					else alt = alternative.replaceFirst(oref, ref); // 3)
					variantAlternatives.set(i, alt);
				}
			}
			references.retainAll(List.of(ref));
		}
		final List<String> alternatives = variants.stream()
				.flatMap(v -> v.getAlternatives().stream())
				.distinct()
				.sorted(Comparator.comparingInt(String::length))
				.collect(Collectors.toList());
		return new Variant(header, coordinate, references, alternatives);
	}

}