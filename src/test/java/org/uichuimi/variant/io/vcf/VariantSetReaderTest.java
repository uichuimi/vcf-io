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
import org.uichuimi.variant.io.vcf.header.VcfHeader;
import org.uichuimi.variant.io.vcf.io.VariantSetReader;

import java.io.File;
import java.io.IOException;

/**
 * Created by uichuimi on 3/10/16.
 */
public class VariantSetReaderTest {

	@Test
	public void test() {
		final File file = new File("test/files/MultiSample.vcf");
		try (VariantSetReader reader = new VariantSetReader(file)) {
			final VcfHeader header = reader.header();
//            System.out.println(header);
//            reader.variants().forEach(System.out::println);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}