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
import org.uichuimi.vcf.header.VcfHeader;
import org.uichuimi.vcf.input.VariantReader;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class VcfHeaderTest {

	@Test
	void testConstructor() {
		final VcfHeader header = new VcfHeader("VCFv4.2");
		assertEquals("VCFv4.2", header.getSimpleHeader("fileformat").getValue());
	}

	@Test
	void testFromFile() {
		final File file = new File(getClass().getResource("/files/Sample2.vcf").getPath());
		try (VariantReader reader = new VariantReader(file)) {
			final VcfHeader header = reader.getHeader();
//        assertEquals(5, header.getSimpleHeaders().size());
			assertEquals(18, header.getHeaderLines().size());
			assertEquals(5, header.getSimpleHeaders().size());
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}

	}

}