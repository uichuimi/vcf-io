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

package org.uichuimi.vcf;

import org.junit.jupiter.api.Test;
import org.uichuimi.vcf.header.ComplexHeaderLine;
import org.uichuimi.vcf.header.FormatHeaderLine;
import org.uichuimi.vcf.header.InfoHeaderLine;

import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ComplexHeaderLineTest {

	@Test
	void testIncomplete() {
		final Map<String, String> map = new TreeMap<>();
		// ##INFO=<ID=DP,Number=1,Type=Integer,Description="Total Depth">
		map.put("ID", "DP");
		map.put("Number", "1");
		map.put("Description", "Total Depth");
		assertThrows(IllegalArgumentException.class, () -> new InfoHeaderLine(map));
	}

	@Test
	void testComplete() {
		final Map<String, String> map = new TreeMap<>();
		//##INFO=<ID=DP,Number=1,Type=Integer,Description="Total Depth">
		map.put("ID", "DP");
		map.put("Number", "1");
		map.put("Description", "Total Depth");
		map.put("Type", "Integer");
		final ComplexHeaderLine complexHeaderLine = new ComplexHeaderLine("INFO", map);
		assertEquals("INFO", complexHeaderLine.getKey());
		assertEquals("DP", complexHeaderLine.getValue("ID"));
		assertEquals("1", complexHeaderLine.getValue("Number"));
		assertEquals("Integer", complexHeaderLine.getValue("Type"));
		assertEquals("Total Depth", complexHeaderLine.getValue("Description"));
	}

	@Test
	public void testIncompleteFormatHeader() {
		// ##FORMAT=<ID=GT,Number=1,Type=String,Description="Genotype">
		final Map<String, String> map = new TreeMap<>();
		map.put("ID", "GT");
		map.put("Number", "1");
		map.put("Type", "String");
		assertThrows(IllegalArgumentException.class, () -> new FormatHeaderLine(map));
	}


}