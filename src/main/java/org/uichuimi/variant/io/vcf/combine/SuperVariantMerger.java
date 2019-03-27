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
import org.uichuimi.variant.io.vcf.variant.MultiLevelInfo;
import org.uichuimi.variant.io.vcf.variant.VariantContext;
import org.uichuimi.variant.io.vcf.header.FormatHeaderLine;
import org.uichuimi.variant.io.vcf.header.InfoHeaderLine;
import org.uichuimi.variant.io.vcf.header.VcfHeader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SuperVariantMerger {

	/**
	 * Returns a new Variant using information from all variants and header as VcfHeader.
	 *
	 * @param variants list of variants to merge, all of them must have the same Coordinate
	 * @param header   header for the new Variant
	 * @return a new Variant containing all the INFO and FORMAT information of the variants list
	 */
	public static VariantContext merge(Collection<VariantContext> variants, VcfHeader header) {
		if (variants.isEmpty()) return null;
		final List<VariantContext> variantList = new ArrayList<>(variants);
		if (variantList.size() == 1 && variantList.get(0).getHeader() == header)
			return variantList.get(0);
		final VariantContext variant = baseVariant(variantList, header);

		for (VariantContext other : variantList) {
			// ID
			// TODO: 27/03/19 associate id with allele
			for (String id : other.getIds())
				if (!variant.getIds().contains(id))
					variant.getIds().add(id);

			// Quality (take the first non null value)
			if (variant.getQuality() == null && other.getQuality() != null)
				variant.setQuality(other.getQuality());

			// Filter
			for (String filter : other.getFilters())
				if (!variant.getFilters().contains(filter))
					variant.getFilters().add(filter);
		}

		mergeInfo(variantList, variant);
		mergeSamples(variantList, variant);
		return variant;
	}

	private static void mergeInfo(List<VariantContext> variants, VariantContext variant) {
		final List<InfoHeaderLine> infoLines = variant.getHeader().getInfoLines();
		for (InfoHeaderLine headerLine : infoLines)
			for (VariantContext other : variants)
				headerLine.mergeInto(variant, variant, other, other);
	}

	private static void mergeSamples(List<VariantContext> variantList, VariantContext variant) {
		for (final VariantContext other : variantList) {
			for (int s = 0; s < other.getHeader().getSamples().size(); s++) {
				final String sample = other.getHeader().getSamples().get(s);
				final MultiLevelInfo sourceInfo = other.getSampleInfo(s);
				final int vs = variant.getHeader().getSamples().indexOf(sample);
				final MultiLevelInfo targetInfo = variant.getSampleInfo(vs);
				for (FormatHeaderLine formatLine : variant.getHeader().getFormatLines())
					formatLine.mergeInto(variant, targetInfo, other, sourceInfo);
			}
		}
	}

	private static VariantContext baseVariant(Collection<VariantContext> variants, VcfHeader header) {
		// These variants DO have same Coordinate
		final Coordinate coordinate = variants.iterator().next().getCoordinate();
		final List<String> references = variants.stream()
				.flatMap(v -> v.getReferences().stream())
				.distinct().collect(Collectors.toList());
		final List<String> alternatives = variants.stream()
				.flatMap(v -> v.getAlternatives().stream())
				.distinct().collect(Collectors.toList());
		return new VariantContext(header, coordinate, references, alternatives);
	}

}
