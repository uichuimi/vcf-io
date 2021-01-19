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
import org.uichuimi.vcf.variant.Coordinate;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by uichuimi on 4/10/16.
 */
public class CoordinateTest {

	private final static List<Coordinate> SORTED = Arrays.asList(
			new Coordinate("1", 1456),
			new Coordinate("1", 1457),
			new Coordinate("2", 1111),
			new Coordinate("2", 2121),
			new Coordinate("10", 123456),
			new Coordinate("11", 432),
			new Coordinate("22", 321),
			new Coordinate("X", 11111),
			new Coordinate("X", 11112),
			new Coordinate("Y", 123456),
			new Coordinate("HSCHR15_RANDOM_CTG1", 3455),
			new Coordinate("HSCHRUN_RANDOM_111", 3456),
			new Coordinate("HSCHR17_3_CTG1", 123),
			new Coordinate("MT", 11));

	@Test
	public void test() {
		for (int i = 0; i < SORTED.size(); i++) {
			for (int j = 0; j < SORTED.size(); j++) {
				final int compareTo = SORTED.get(i).compareTo(SORTED.get(j));
				if (i < j) assertTrue(compareTo < 0);
				if (i > j) assertTrue(compareTo > 0);
				if (i == j) assertTrue(compareTo == 0);
			}
		}
	}

}