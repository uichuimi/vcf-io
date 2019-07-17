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
	private final Map<String, List<String>> cache = new LinkedHashMap<>();
	private final Map<String, FormatHeaderLine> formatLines = new HashMap<>();
	private final Map<String, InfoHeaderLine> infoLines = new HashMap<>();
	private final Map<String, Map<String, ComplexHeaderLine>> complexLines = new LinkedHashMap<>();

	/**
	 * An empty VcfHeader. Remember that fileformat must be the first HeaderLine.
	 */
	public VcfHeader() {
	}

	/**
	 * Creates a Vcf file with specified fileformat. fileformat must be the value of the first line,
	 * like <em>VCFv4.1</em>.
	 *
	 * @param fileformat
	 * 		VCF version as written in the first line
	 */
	public VcfHeader(String fileformat) {
		headerLines.add(new SimpleHeaderLine("fileformat", fileformat));
	}

	/**
	 * Get an unmodifiable list of header lines. To modify this list use {@link
	 * VcfHeader#addHeaderLine(HeaderLine)} or {@link VcfHeader#addHeaderLine(HeaderLine, boolean)}
	 *
	 * @return an unmodifiable list of the header lines
	 */
	public List<HeaderLine> getHeaderLines() {
		return Collections.unmodifiableList(headerLines);
	}

	public Map<String, Map<String, ComplexHeaderLine>> getComplexLines() {
		return complexLines;
	}

	public Map<String, FormatHeaderLine> getFormatLines() {
		return formatLines;
	}

	public Map<String, InfoHeaderLine> getInfoLines() {
		return infoLines;
	}

	public List<String> getSamples() {
		return samples;
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
		final List<String> list = new ArrayList<>(complexLines.get(key).keySet());
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
		return complexLines.getOrDefault(key, Collections.emptyMap()).get(id);
	}

	public void addHeaderLine(HeaderLine headerLine) {
		addHeaderLine(headerLine, false);
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
	public void addHeaderLine(HeaderLine headerLine, boolean override) {
		// 1) find similar line
		if (headerLines.contains(headerLine)) return;
		if (headerLine instanceof SimpleHeaderLine) {
			// insert right after last line with the same key or at the end
			headerLines.add(headerLine);
		} else if (headerLine instanceof ComplexHeaderLine) {
			// insert right after last line with the same key and id or at the end
			final ComplexHeaderLine complexHeaderLine = (ComplexHeaderLine) headerLine;
			final String key = complexHeaderLine.getKey();
			final String id = complexHeaderLine.getId();
			final ComplexHeaderLine synonym = getComplexHeader(headerLine.getKey(), id);
			if (synonym != null) {
				if (override) headerLines.remove(synonym);
				else return;
			}
			headerLines.add(headerLine);
			if (complexHeaderLine instanceof FormatHeaderLine) {
				final FormatHeaderLine fhl = (FormatHeaderLine) complexHeaderLine;
				formatLines.put(fhl.getId(), fhl);
			} else if (complexHeaderLine instanceof InfoHeaderLine) {
				final InfoHeaderLine ihl = (InfoHeaderLine) complexHeaderLine;
				infoLines.put(ihl.getId(), ihl);
			}
			if (id != null)
				complexLines.computeIfAbsent(key, k -> new LinkedHashMap<>()).put(id, complexHeaderLine);
		} else headerLines.add(headerLine);
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
		return infoLines.computeIfAbsent(key, k -> new InfoHeaderLine(k, "1", "String", k));
	}

	public FormatHeaderLine getFormatHeader(String key) {
		return formatLines.computeIfAbsent(key, k -> new FormatHeaderLine(k, "1", "String", k));
	}

}
