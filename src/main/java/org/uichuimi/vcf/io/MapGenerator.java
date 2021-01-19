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

import java.util.LinkedHashMap;

/**
 * Returns a LinkedHashMap with the content of the line parsed. So "ID=AC,Number=A,Type=Integer"
 * becomes a map. This class is convenient to parse almost any VCF header lines.
 */
class MapGenerator {

	private final static char QUOTE = '"';
	private final static char COMMA = ',';
	private final static char EQUALS = '=';

	/**
	 * @param line
	 * 		line to map, without ##INFO neither ##FORMAT neither &lt; neither &gt;
	 * @return a map with the content of the line
	 */
	synchronized static LinkedHashMap<String, String> parse(String line) {
		return privateParse(line);
	}

	private static LinkedHashMap<String, String> privateParse(String line) {
		Status status = new Status(line);
		while (status.cursor < line.length()) {
			switch (line.charAt(status.cursor)) {
				case QUOTE:
					putQuotedValue(status);
					break;
				case EQUALS:
					// Equals symbol: cursor at next position and expected a value
					status.cursor++;
					status.isKey = false;
					break;
				case COMMA:
					// Comma symbol, cursor at next position and expected a key
					status.cursor++;
					status.isKey = true;
					break;
				default:
					putUnquotedValue(status);
			}
		}
		return status.map;
	}

	private static void putUnquotedValue(Status status) {
		int end = endOfToken(status);
		if (status.isKey)
			status.key = status.line.substring(status.cursor, end);
		else {
			String value = status.line.substring(status.cursor, end);
			status.map.put(status.key, value);
		}
		status.cursor = end;
	}

	//
	private static int endOfToken(Status status) {
		// Text not in quotes
		// token is the text between cursor and next "=" or ","
		// cursor at "=" or ","
		int end = status.cursor;
		while (end < status.line.length()
				&& status.line.charAt(end) != EQUALS
				&& status.line.charAt(end) != COMMA)
			end++;
		return end;
	}

	//
	private static void putQuotedValue(Status status) {
		// If isKey is false, something went wrong
		// token is the text between quotes
		// place cursor at next position after end quote
		int endQuotePosition = status.line.indexOf(QUOTE, status.cursor + 1);
		final String value = status.line.substring(status.cursor + 1, endQuotePosition);
		status.map.put(status.key, value);
		status.cursor = endQuotePosition + 1;
	}

	private static class Status {
		public LinkedHashMap<String, String> map = new LinkedHashMap<>();
		public String key = "";
		boolean isKey = true;
		String line;
		int cursor = 0;

		Status(String line) {
			this.line = line;
		}
	}

}
