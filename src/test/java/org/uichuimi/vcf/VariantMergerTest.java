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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.uichuimi.vcf.combine.VariantMerger;
import org.uichuimi.vcf.header.ComplexHeaderLine;
import org.uichuimi.vcf.header.FormatHeaderLine;
import org.uichuimi.vcf.header.InfoHeaderLine;
import org.uichuimi.vcf.header.VcfHeader;
import org.uichuimi.vcf.lazy.Variant;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VariantMergerTest {

	private VcfHeader header1, header2, targetHeader;

	@BeforeEach
	void before() {

		final ComplexHeaderLine dpLine = new InfoHeaderLine("DP", "1", "Float", "Read depth");
		final FormatHeaderLine gtLine = new FormatHeaderLine("GT", "1", "String", "Genotype");

		header1 = new VcfHeader("VCFv4.2");
		header1.getSamples().add("S1");
		header1.getHeaderLines().add(gtLine);
		header1.getHeaderLines().add(dpLine);

		header2 = new VcfHeader("VCFv4.2");
		header2.getSamples().add("S2");
		header2.getHeaderLines().add(gtLine);
		header2.getHeaderLines().add(dpLine);

		targetHeader = new VcfHeader("VCFv4.2");
		targetHeader.getSamples().add("S1");
		targetHeader.getSamples().add("S2");
		targetHeader.getHeaderLines().add(gtLine);
		targetHeader.getHeaderLines().add(dpLine);
	}

	@Test
	void testBasicMerge() {
		//1   155776549   . CT  C   192.74  .   .   GT:AD:DP:GQ:PL  1/1:0,9:9:27:230,27,0
		final Variant v1 = new Variant(header1, "1\t155776549\t.\tCT\tC\t10\t.\t.");
		final int s1 = header1.getSamples().indexOf("S1");
		v1.getSampleInfo(s1).set("GT", "1/1");

		// 1   155776549   . CT  C   114.74  .   .   GT:AD:DP:GQ:PL  0/1:2,7:9:27:152,0,27
		final Variant v2 = new Variant(header2, "1\t155776549\t.\tCT\tC\t20\t.\t.");
		final int s2 = header2.getSamples().indexOf("S2");
		v2.getSampleInfo(s2).set("GT", "0/1");

		final Variant merge = VariantMerger.merge(Arrays.asList(v1, v2), targetHeader);
		final int ts1 = targetHeader.getSamples().indexOf("S1");
		final int ts2 = targetHeader.getSamples().indexOf("S2");

		assertEquals("1/1", merge.getSampleInfo(ts1).get("GT"));
		assertEquals("0/1", merge.getSampleInfo(ts2).get("GT"));
	}

	@Test
	public void testThreeAlleles() {
		//1   155776549   . CT  C   192.74  .   .   GT:AD:DP:GQ:PL  1/1:0,9:9:27:230,27,0
		final Variant v1 = new Variant(header1, "1\t31653746\trs1\tAACAC\tA\t12.9\t21\t.\tDP=10");
		v1.setFormat("S1", "GT", "1/1");

		// 1   155776549   . CT  C   114.74  .   .   GT:AD:DP:GQ:PL  0/1:2,7:9:27:152,0,27
		final Variant v2 = new Variant(header2, "1\t31653746\trs1\tAACAC\tA,AACACAC\t400\tPASS\t.");
		v2.setFormat("S2", "GT", "1/2");

		final Variant merge = VariantMerger.merge(Arrays.asList(v1, v2), targetHeader);

		final String[] expected = {"AACAC", "A", "AACACAC"};
		for (int i = 0; i < expected.length; i++)
			assertEquals(merge.getAlleles().get(i), expected[i]);

		assertEquals("1/1", merge.getFormat("S1", "GT"));
		assertEquals("1/2", merge.getFormat("S2", "GT"));
	}

	@Test
	public void testDP() {
//		final Variant v1 = new Variant("1", 1, "A", "C", header1);
//		v1.getInfo().set("DP", 8);
//		final Variant v2 = new Variant("1", 1, "A", "C", header2);
//		v2.getInfo().set("DP", 9);
//		final Variant merge = VariantMerger.merge(Arrays.asList(v1, v2), targetHeader);
//		// TODO: 27/03/19 this test will not work. It is not merger task to recompute stats
////		assertEquals(17, (int)merge.getInfo().get("DP"));
	}

}