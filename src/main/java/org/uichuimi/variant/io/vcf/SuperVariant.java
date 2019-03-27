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

package org.uichuimi.variant.io.vcf;


import org.uichuimi.variant.io.vcf.header.VcfHeader;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores a vcf. chrom, position, ref, alt, filter and format are Strings. position is an integer, qual a double. Info
 * is stored as a map of key=value. If value is null, key is treated as a flag.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class SuperVariant extends MultiLevelInfo {

	private final VcfHeader header;
	private final List<String> alleles;
	private Coordinate coordinate;
	private List<String> references;
	private List<String> alternatives;

	private List<String> ids = new ArrayList<>();
	private Double quality;
	private List<String> filters = new ArrayList<>();

	private final MultiLevelInfo[] sampleInfo;

	public SuperVariant(VcfHeader header, Coordinate coordinate, List<String> references, List<String> alternatives) {
		super(references.size(), alternatives.size());
		this.header = header;
		this.coordinate = coordinate;
		this.references = references;
		this.alternatives = alternatives;
		this.sampleInfo = new MultiLevelInfo[header.getSamples().size()];
		this.alleles = new ArrayList<>(getNumberOfAlleles());
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
}
