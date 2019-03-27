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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.uichuimi.variant.io.vcf.header.ComplexHeaderLine;
import org.uichuimi.variant.io.vcf.header.VcfHeader;
import org.uichuimi.variant.io.vcf.variant.Genotype;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by uichuimi on 24/05/16.
 */
public class SampleTest {

	@Test
	public void test() {
		final VcfHeader header = new VcfHeader();
		header.getSamples().add("S1");
		header.getSamples().add("S2");
		header.getSamples().add("S3");
		header.getHeaderLines().add(new ComplexHeaderLine("FORMAT", getGenotypeHeader()));
		final Variant variant = new Variant("1", 1, "A", new String[]{"C", "T"}, header);
		variant.getSampleInfo().setFormat("S1", "GT", "0/1");
		Assertions.assertEquals(Genotype.HETEROZYGOUS, variant.getSampleInfo().getGenotype("S1"));
//        Assertions.assertEquals(new String[]{"A", "C"}, variant.getSampleInfo().getAlleles("S1"));
//        final VariantSet file = VariantSetFactory.createFromFile(new File("test/files/Sample2.vcf"));
//        final Sample sample = new Sample(file, file.getHeader().getSamples().get(0));
//        final Sample sample1 = new Sample(file, file.getHeader().getSamples().get(1));
//        final Sample sample2 = new Sample(file, file.getHeader().getSamples().get(2));
//        Assert.assertEquals("NA00001", sample.getName());
//        Assert.assertEquals(2, sample.getVariants().size());
//        Assert.assertEquals(4, sample1.getVariants().size());
//        Assert.assertEquals(3, sample2.getVariants().size());
	}

	private Map<String, String> getGenotypeHeader() {
		// ##FORMAT=<ID=GT,Number=1,Type=String,Description="Genotype">
		final Map<String, String> gt = new LinkedHashMap<>();
		gt.put("ID", "GT");
		gt.put("Number", "1");
		gt.put("Type", "String");
		gt.put("Description", "Genotype");
		return gt;
	}
}
