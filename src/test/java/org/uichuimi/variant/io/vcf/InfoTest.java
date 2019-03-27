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


import org.junit.jupiter.api.Test;
import org.uichuimi.variant.io.vcf.variant.Info;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class InfoTest {

	@Test
	void testInfo() {
		final Info info = new Info();
		info.set("this", "hello");
		info.set("that", "hi");
		info.set("those", 17);
		info.set("number", -14.67);
		info.set("true", true);
		info.set("false", false);
		assertEquals("hello", info.getString("this"));
		assertEquals("hi", info.getString("that"));
		assertEquals(17, info.getNumber("those"));
		assertEquals(-14.67, (double) info.getNumber("number"), 0.001);
		assertEquals(true, info.getBoolean("true"));
		assertEquals(false, info.getBoolean("false"));
	}


}