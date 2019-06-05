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

package org.uichuimi.vcf.variant;


import org.uichuimi.vcf.header.VcfHeader;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the newer approach of a VCF line. A context defines all possible variations in a genomic coordinate. It can
 * contain 1 or more reference alleles, and 1 or more alternative alleles. It can store information at different levels.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VariantContext {

	private final VcfHeader header;
	private final List<String> alleles;
	private Coordinate coordinate;
	private List<String> references;
	private List<String> alternatives;

	private List<String> ids = new ArrayList<>();
	private Double quality;
	private List<String> filters = new ArrayList<>();

	private final MultiLevelInfo[] sampleInfo;
	private final MultiLevelInfo info;

	public VariantContext(VcfHeader header, Coordinate coordinate, List<String> references, List<String> alternatives) {
		this.info = new MultiLevelInfo(references.size(), alternatives.size());
		this.header = header;
		this.coordinate = coordinate;
		this.references = references;
		this.alternatives = alternatives;
		this.sampleInfo = new MultiLevelInfo[header.getSamples().size()];
		this.alleles = new ArrayList<>(info.getNumberOfAlleles());
		alleles.addAll(references);
		alleles.addAll(alternatives);
	}

	public List<String> getIds() {
		return ids;
	}

	public void setQuality(double quality) {
		this.quality = quality;
	}

	public Double getQuality() {
		return quality;
	}

	public List<String> getFilters() {
		return filters;
	}

	public VcfHeader getHeader() {
		return header;
	}

	public Coordinate getCoordinate() {
		return coordinate;
	}

	public List<String> getReferences() {
		return references;
	}

	public List<String> getAlternatives() {
		return alternatives;
	}

	/**
	 * Get the FORMAT data of the sample in index.
	 */
	public MultiLevelInfo getSampleInfo(int index) {
		if (sampleInfo[index] == null)
			sampleInfo[index] = new MultiLevelInfo(references.size(), alternatives.size());
		return sampleInfo[index];
	}

	public String getAllele(int index) {
		return alleles.get(index);
	}

	public List<String> getAlleles() {
		return alleles;
	}

	public int indexOfAllele(String allele) {
		return alleles.indexOf(allele);
	}

	/**
	 * Get the INFO values of this variant.
	 */
	public MultiLevelInfo getInfo() {
		return info;
	}

	@Override
	public String toString() {
		return String.format("VariantContext{%s, references=%s, alternatives=%s, ids=%s}", coordinate, references, alternatives, ids);
	}
}
