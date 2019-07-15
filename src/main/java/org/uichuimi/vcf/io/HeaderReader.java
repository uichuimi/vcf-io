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

package org.uichuimi.vcf.io;

import org.uichuimi.vcf.header.*;
import org.uichuimi.vcf.utils.FileUtils;
import org.uichuimi.vcf.variant.VariantException;
import org.uichuimi.vcf.variant.VcfConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Deals with the header of VCF files. This class methods should be invoked first when reading any
 * VCF file.
 */
public class HeaderReader {

	private static final Pattern HEADER_LINE = Pattern.compile("##([^=]+)=(.+)");
	private static final Pattern COMPLEX_HEADER = Pattern.compile("<(.*)>");
	private static final Pattern FIELDS_LINE = Pattern.compile("#CHROM(.*)");

	private static void addHeader(VcfHeader header, String line) {
		final Matcher metaLine = HEADER_LINE.matcher(line);
		if (metaLine.matches()) addMetaLine(header, metaLine);
		else addFormatLine(header, line);
	}

	private static void addMetaLine(VcfHeader header, Matcher metaLine) {
		final String key = metaLine.group(1);
		final String value = metaLine.group(2);
		final Matcher contentMatcher = COMPLEX_HEADER.matcher(value);
		if (contentMatcher.matches())
			addComplexHeader(header, key, contentMatcher.group(1));
		else header.addHeaderLine(new SimpleHeaderLine(key, value));
	}

	private static void addComplexHeader(VcfHeader header, String key, String group) {
		final Map<String, String> map = MapGenerator.parse(group);
		try {
			final ComplexHeaderLine complexHeaderLine = getComplexHeaderLine(key, map);
			header.addHeaderLine(complexHeaderLine);
		} catch (VariantException e) {
			e.printStackTrace();
		}
	}

	private static ComplexHeaderLine getComplexHeaderLine(String key, Map<String, String> map) {
		switch (key) {
			case "INFO":
				return new InfoHeaderLine(map);
			case "FORMAT":
				return new FormatHeaderLine(map);
			default:
				return new ComplexHeaderLine(key, map);
		}
	}

	private static void addFormatLine(VcfHeader header, String line) {
		final Matcher matcher = FIELDS_LINE.matcher(line);
		if (matcher.matches()) {
			final String[] split = line.split(VcfConstants.DELIMITER);
			int numberOfSamples = split.length - 9;
			if (numberOfSamples > 0)
				for (int i = 0; i < numberOfSamples; i++)
					header.getSamples().add(split[i + 9]);
		}
	}

	/**
	 * Reads the header of a VCF file.
	 *
	 * @param file
	 * 		file to be read
	 * @return the header of the file
	 * @throws IOException
	 * 		any exception caused when reading the file, including {@link java.io.FileNotFoundException}
	 */
	public static VcfHeader readHeader(File file) throws IOException {
		final VcfHeader header = new VcfHeader();
		try (BufferedReader reader = FileUtils.getBufferedReader(file)) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("#")) addHeader(header, line);
				else break;
			}
		}
		return header;
	}

	/**
	 * Reads a header in the reader, and leaves the reader at the exact point of the first variant.
	 *
	 * @param reader
	 * 		an open reader
	 * @return the header present in reader
	 * @throws IOException
	 * 		any exception caused when reading the reader
	 */
	public static VcfHeader readHeader(BufferedReader reader) throws IOException {
		final VcfHeader header = new VcfHeader();
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.startsWith("##")) addHeader(header, line);
			else if (line.startsWith("#CHROM")) {
				addHeader(header, line);
				break;
			}
		}
		return header;
	}
}
