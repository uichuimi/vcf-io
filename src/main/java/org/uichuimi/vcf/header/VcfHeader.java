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

package org.uichuimi.vcf.header;

import org.uichuimi.vcf.variant.VcfConstants;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Stores headers of Variant Call Format Version 4.3
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VcfHeader {

	private final static List<String> REQUIRED_COLUMNS =
			Arrays.asList("CHROM", "POS", "ID", "REF", "ALT", "QUAL", "FILTER", "INFO");

	private final List<String> samples = new ArrayList<>();
	private final List<HeaderLine> headerLines = new LinkedList<>();
	private Map<String, List<String>> cache = new LinkedHashMap<>();
	private Map<String, FormatHeaderLine> formatLines;
	private Map<String, InfoHeaderLine> infoLines;

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


	public List<HeaderLine> getHeaderLines() {
		return headerLines;
	}

	public List<String> getSamples() {
		return samples;
	}

	public List<ComplexHeaderLine> getComplexHeaders() {
		return headerLines.stream()
				.filter(ComplexHeaderLine.class::isInstance)
				.map(ComplexHeaderLine.class::cast)
				.collect(Collectors.toList());
	}

	/**
	 * Get a list with the IDs of the ComplexHeaderLines of type key
	 *
	 * @param key
	 * 		the type of ComplexHeaderLines
	 * @return a list with all the IDs of type key
	 */
	public List<String> getIdList(String key) {
		if (cache.containsKey(key)) return cache.get(key);
		final List<String> list = headerLines.stream()
				.filter(ComplexHeaderLine.class::isInstance)
				.map(ComplexHeaderLine.class::cast)
				.filter(header -> header.getKey().equals(key))
				.map(header -> header.getValue("ID"))
				.collect(Collectors.toList());
		cache.put(key, list);
		return list;
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
		return getSimpleHeader(key) != null;
	}

	/**
	 * Gets the first SimpleHeaderLine that has key
	 *
	 * @param key
	 * 		key of the SimpleHeaderLine to match
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
				.filter(header -> header instanceof ComplexHeaderLine)
				.map(header -> (ComplexHeaderLine) header)
				.filter(header -> header.getKey().equals(key))
				.filter(header -> header.getValue("ID").equals(id))
				.findFirst().orElse(null);
	}

	public List<ComplexHeaderLine> getComplexHeaders(String key) {
		return headerLines.stream()
				.filter(ComplexHeaderLine.class::isInstance)
				.map(ComplexHeaderLine.class::cast)
				.filter(header -> header.getKey().equals(key))
				.collect(Collectors.toList());
	}

	public void add(HeaderLine headerLine) {
		add(headerLine, false);
	}

	/**
	 * Adds a new header line to header. Tries to keep the lines grouped by type
	 *
	 * @param headerLine
	 * 		the new line to add
	 * @param override
	 * 		if header line is complex, and there is already a header line with the same type and id,
	 * 		when override is true, the old header line is removed and the new one is added; if override
	 * 		is false, the new line is discarded
	 */
	public void add(HeaderLine headerLine, boolean override) {
		// 1) find similar line
		for (HeaderLine line : headerLines) if (line.equals(headerLine)) return;
		if (headerLine instanceof SimpleHeaderLine) {
			// insert right after last line with the same key or at the end
			int i = -1;
			for (int j = 0; j < headerLines.size(); j++)
				if (headerLines.get(j).getKey().equals(headerLine.getKey())) i = j;
			if (i > 0) {
				headerLines.add(i, headerLine);
			} else headerLines.add(headerLine);
		} else if (headerLine instanceof ComplexHeaderLine) {
			// insert right after last line with the same key and id or at the end
			final ComplexHeaderLine complexHeaderLine = (ComplexHeaderLine) headerLine;
			final ComplexHeaderLine synonym = getComplexHeader(headerLine.getKey(), complexHeaderLine.getValue("ID"));
			if (synonym != null) {
				if (override) headerLines.remove(synonym);
				else return;
			}
			int i = -1;
			for (int j = 0; j < headerLines.size(); j++) {
				final HeaderLine line = headerLines.get(j);
				if (line instanceof ComplexHeaderLine) {
					if (line.getKey().equals(headerLine.getKey())) i = j;
				}
			}
			if (i > 0) {
				headerLines.add(i + 1, headerLine);
			} else headerLines.add(headerLine);
		} else headerLines.add(headerLine);
	}

	public Collection<FormatHeaderLine> getFormatLines() {
		if (formatLines == null) {
			formatLines = headerLines.stream()
					.filter(FormatHeaderLine.class::isInstance)
					.map(FormatHeaderLine.class::cast)
					.collect(Collectors.toMap(FormatHeaderLine::getId, Function.identity()));
		}
		return formatLines.values();
	}

	public Collection<InfoHeaderLine> getInfoLines() {
		indexInfoLines();
		return infoLines.values();
	}

	private void indexInfoLines() {
		if (infoLines == null)
			infoLines = headerLines.stream()
					.filter(InfoHeaderLine.class::isInstance)
					.map(InfoHeaderLine.class::cast)
					.collect(Collectors.toMap(InfoHeaderLine::getId, Function.identity()));
	}

	@Override
	public String toString() {
		final StringJoiner joiner = new StringJoiner(System.lineSeparator());
		headerLines.forEach(headerLine -> joiner.add(headerLine.toString()));
		joiner.add(getColumnsLine());
		return joiner.toString();
	}

	private String getColumnsLine() {
		final StringJoiner joiner = new StringJoiner(VcfConstants.DELIMITER);
		REQUIRED_COLUMNS.forEach(joiner::add);
		if (!samples.isEmpty()) {
			joiner.add("FORMAT");
			samples.forEach(joiner::add);
		}
		return "#" + joiner.toString();
	}

	public InfoHeaderLine getInfoHeader(String key) {
		indexInfoLines();
		return infoLines.computeIfAbsent(key, k -> new InfoHeaderLine(k, "1", "String", ""));
	}
}
