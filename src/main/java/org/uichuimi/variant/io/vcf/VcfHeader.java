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

import java.util.*;
import java.util.stream.Collectors;

/**
 * Stores headers of Variant Call Format Version 4.2.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VcfHeader {

	private final static List<String> REQUIRED_COLUMNS =
			Arrays.asList("CHROM", "POS", "ID", "REF", "ALT", "QUAL", "FILTER", "INFO");

	private final List<String> samples = new ArrayList<>();
	private final LinkedList<HeaderLine> headerLines = new LinkedList<>();
	private Map<String, List<String>> cache = new LinkedHashMap<>();

	/**
	 * An empty VcfHeader. Remember that fileformat must be the first HeaderLine.
	 */
	public VcfHeader() {
	}

	/**
	 * Creates a Vcf file with specified fileformat
	 */
	public VcfHeader(String fileformat) {
		headerLines.add(new SimpleHeaderLine("fileformat", fileformat));
	}


	public List<ComplexHeaderLine> getComplexHeaders() {
		return headerLines.stream()
				.filter(headerLine -> headerLine.getClass() == ComplexHeaderLine.class)
				.map(headerLine -> (ComplexHeaderLine) headerLine)
				.collect(Collectors.toList());
	}

	@Override
	public String toString() {
		final StringJoiner joiner = new StringJoiner(System.lineSeparator());
		headerLines.forEach(headerLine -> joiner.add(headerLine.toString()));
		joiner.add(getColumnsLine());
		return joiner.toString();
	}

	private String getColumnsLine() {
		final StringJoiner joiner = new StringJoiner("\t");
		REQUIRED_COLUMNS.forEach(joiner::add);
		if (!samples.isEmpty()) {
			joiner.add("FORMAT");
			samples.forEach(joiner::add);
		}
		return "#" + joiner.toString();
	}

	public List<String> getSamples() {
		return samples;
	}

	/**
	 * Get a list with the IDs of the ComplexHeaderLines of type key
	 *
	 * @param key the type of ComplexHeaderLines
	 * @return a list with all the IDs of type key
	 */
	public List<String> getIdList(String key) {
		if (cache.containsKey(key))
			return cache.get(key);
		final List<String> list = headerLines.stream()
				.filter(header -> header.getClass() == ComplexHeaderLine.class)
				.map(header -> (ComplexHeaderLine) header)
				.filter(header -> header.getKey().equals(key))
				.map(header -> header.getValue("ID"))
				.collect(Collectors.toList());
		cache.put(key, list);
		return list;
	}

	public int indexOf(String sample) {
		return samples.indexOf(sample);
	}

	public List<SimpleHeaderLine> getSimpleHeaders() {
		return headerLines.stream()
				.filter(hLine -> hLine.getClass() == SimpleHeaderLine.class)
				.map(headerLine -> (SimpleHeaderLine) headerLine)
				.collect(Collectors.toList());
	}

	public boolean hasComplexHeader(String type, String id) {
		return getComplexHeader(type, id) != null;
	}

	public boolean hasSimpleHeader(String key) {
		return headerLines.stream()
				.filter(hLine -> hLine.getClass() == SimpleHeaderLine.class)
				.map(headerLine -> (SimpleHeaderLine) headerLine)
				.anyMatch(headerLine -> headerLine.getKey().equals(key));
	}

	/**
	 * Gets the first SimpleHeaderLine that has key
	 *
	 * @param key key of the SimpleHeaderLine to match
	 * @return the first found SimpleHeaderLine
	 */
	public SimpleHeaderLine getSimpleHeader(String key) {
		return headerLines.stream()
				.filter(hLine -> hLine.getClass() == SimpleHeaderLine.class)
				.map(headerLine -> (SimpleHeaderLine) headerLine)
				.filter(headerLine -> headerLine.getKey().equals(key))
				.findFirst().orElse(null);
	}

	public ComplexHeaderLine getComplexHeader(String key, String id) {
		return headerLines.stream()
				.filter(header -> header.getClass() == ComplexHeaderLine.class)
				.map(header -> (ComplexHeaderLine) header)
				.filter(header -> header.getKey().equals(key))
				.filter(header -> header.getValue("ID").equals(id))
				.findFirst().orElse(null);
	}

	public LinkedList<HeaderLine> getHeaderLines() {
		return headerLines;
	}

	public List<ComplexHeaderLine> getComplexHeaders(String key) {
		return headerLines.stream()
				.filter(header -> header.getClass() == ComplexHeaderLine.class)
				.map(header -> (ComplexHeaderLine) header)
				.filter(header -> header.getKey().equals(key))
				.collect(Collectors.toList());
	}
}
