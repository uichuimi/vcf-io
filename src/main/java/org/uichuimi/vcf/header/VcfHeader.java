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
	private static final List<String> FORMAT_ORDER = Arrays.asList("GT", "AD", "DP", "GQ", "PL");

	private final List<String> samples = new ArrayList<>();
	private final List<HeaderLine> headerLines = new LinkedList<>();
	private Map<String, List<String>> cache = new LinkedHashMap<>();
	private List<FormatHeaderLine> formatLines;
	private List<InfoHeaderLine> infoLines;

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
	 * 		when override is true, the old header line is removed and the new one is added;
	 * 		if override is false, the new line is discarded
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

	public static VcfHeader merge(Collection<VcfHeader> headers) {
		final VcfHeader header = new VcfHeader();
		// Samples
		headers.stream().map(VcfHeader::getSamples)
				.flatMap(Collection::stream)
				.distinct()
				.forEach(header.getSamples()::add);
		// header lines
		headers.forEach(h -> h.getHeaderLines().forEach(sourceHeader -> {
			if (sourceHeader instanceof SimpleHeaderLine) {
				addSimpleHeader(header, (SimpleHeaderLine) sourceHeader);
			}
			if (sourceHeader instanceof ComplexHeaderLine) {
				addComplexHeader(header, (ComplexHeaderLine) sourceHeader);
			}
		}));

		return header;
	}

	private static void addSimpleHeader(VcfHeader header, SimpleHeaderLine sourceHeader) {
		if (headerContains(header, sourceHeader)) return;
		header.getHeaderLines().add(sourceHeader);
	}

	private static boolean headerContains(VcfHeader header, SimpleHeaderLine sourceHeader) {
		for (SimpleHeaderLine headerLine : header.getSimpleHeaders())
			if (headerLine.getKey().equals(sourceHeader.getKey())
					&& headerLine.getValue().equals(sourceHeader.getValue()))
				return true;
		return false;
	}

	private static void addComplexHeader(VcfHeader header, ComplexHeaderLine sourceHeader) {
		if (header.hasComplexHeader(sourceHeader.getKey(), sourceHeader.getValue("ID"))) return;
		header.getHeaderLines().add(sourceHeader);

	}

	public List<FormatHeaderLine> getFormatLines() {
		if (formatLines == null) {
			formatLines = headerLines.stream()
					.filter(FormatHeaderLine.class::isInstance)
					.map(FormatHeaderLine.class::cast)
					.sorted(Comparator.comparingInt(format -> FORMAT_ORDER.indexOf(format.getId())))
					.collect(Collectors.toList());
		}
		return formatLines;
	}

	public List<InfoHeaderLine> getInfoLines() {
		if (infoLines == null)
			infoLines = headerLines.stream()
					.filter(InfoHeaderLine.class::isInstance)
					.map(InfoHeaderLine.class::cast)
					.collect(Collectors.toList());
		return infoLines;
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
}
