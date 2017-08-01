/*
 * Copyright (c) UICHUIMI 2016
 *
 * This file is part of VariantCallFormat.
 *
 * VariantCallFormat is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * VariantCallFormat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package utils;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by uichuimi on 7/03/16.
 */
public class StringStoreTest {


	@Test
	public void letsTest() {
		assertEquals("hello", StringStore.getInstance("hello"));
		assertEquals("goodbye", StringStore.getInstance("goodbye"));
		assertEquals("hi", StringStore.getInstance("hi"));
		assertEquals("patri", StringStore.getInstance("patri"));
		assertEquals("hello", StringStore.getInstance("hello"));
		for (int i = 0; i < 5000; i++)
			assertEquals(String.valueOf(i), StringStore.getInstance(String.valueOf(i)));
	}

}