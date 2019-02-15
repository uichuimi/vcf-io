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

import org.junit.jupiter.api.Disabled;
import org.uichuimi.variant.io.vcf.io.CustomVariantSetReader;

import java.io.File;
import java.io.IOException;

/**
 * Created by uichuimi on 3/10/16.
 */
public class CustomVariantSetReaderTest {

	@Disabled
	public void test() {
		final File file = new File("test/files/SP030.vcf");
		try (CustomVariantSetReader reader = new CustomVariantSetReader(file)) {
			reader.addInfo("DP");
			reader.addSample("SP030");
			reader.addFormat("GT");
			reader.addFormat("DP");
			reader.setloadId(true);
			final VcfHeader header = reader.header();
			System.out.println(header);
			reader.variants().forEach(System.out::println);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}