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

package org.uichuimi.vcf.combine;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.uichuimi.vcf.header.VcfHeader;
import org.uichuimi.vcf.io.VariantSetFactory;
import org.uichuimi.vcf.variant.Genotype;
import org.uichuimi.vcf.variant.VariantSet;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by uichuimi on 25/05/16.
 */
public class VariantCombinerTest {

	private final static File SP030_VCF = getFile("/files/SP030.vcf");

	private static File getFile(String file) {
		return new File(VariantCombinerTest.class.getResource(file).getPath());
	}

	private final static File SP072_VCF = getFile("/files/SP072.vcf");
	private final static File SP077_VCF = getFile("/files/SP077.vcf");
	private final static File SP030_MIST = getFile("/files/SP030.mist");
	private final static File SP072_MIST = getFile("/files/SP072.mist");
	private final static File SP077_MIST = getFile("/files/SP077.mist");
	private static final File DA_VCF = getFile("/files/DA.vcf");
	private static final File DA_MIST = getFile("/files/DA.mist");

	@Test
	void testWithControlAndMist() {
		final List<Sample> samples = new ArrayList<>();
		final Sample sp030 = new Sample(SP030_VCF, "SP030");
		sp030.setMistFile(SP030_MIST);
		samples.add(sp030);
		final Sample sp072 = new Sample(SP072_VCF, "SP072");
		sp072.setMistFile(SP072_MIST);
		samples.add(sp072);
		final Sample sp077 = new Sample(SP077_VCF, "SP077");
		sp077.setMistFile(SP077_MIST);
		samples.add(sp077);
		final Sample da = new Sample(DA_VCF, "DA");
		da.setMistFile(DA_MIST);
		da.setGenotype(Genotype.WILD);
		samples.add(da);
		final VariantSet variantSet = testCombine(samples, true, 7);
//        variantSet.save(new File("SP_30_72_77_common_mist_minus_DA.vcf"));
	}

	@Test
	void testWithControlWithoutMist() {
		final List<Sample> samples = new ArrayList<>();
		final Sample sp030 = new Sample(SP030_VCF, "SP030");
		samples.add(sp030);
		final Sample sp072 = new Sample(SP072_VCF, "SP072");
		samples.add(sp072);
		final Sample sp077 = new Sample(SP077_VCF, "SP077");
		samples.add(sp077);
		final Sample da = new Sample(DA_VCF, "DA");
		da.setGenotype(Genotype.WILD);
		samples.add(da);
		final VariantSet variantSet = testCombine(samples, true, 3);
//        variantSet.save(new File("SP_30_72_77_common_minus_DA.vcf"));
	}

	@Test
	void testUniqueFileWithSeveralSamples() {
		final List<Sample> samples = new ArrayList<>();
		final File file = getFile("/files/MultiSample.vcf");
		final VcfHeader header = VariantSetFactory.readHeader(file);
		header.getSamples().forEach(s -> samples.add(new Sample(file, s)));
		samples.get(2).setGenotype(Genotype.WILD);
		testCombine(samples, true, 12767);

	}

	private VariantSet testCombine(List<Sample> samples, boolean remove, long expectedSize) {
		final List<File> files = samples.stream().map(Sample::getFile)
				.collect(Collectors.toList());
		final Map<String, Genotype> spls = new LinkedHashMap<>();
		samples.forEach(sample -> spls.put(sample.getName(), sample.getGenotype()));
		final VariantCombiner combiner = new VariantCombiner(files, spls);
//        final VariantCombiner combiner = new VariantCombiner(samples, remove);
		combiner.run();
		final VariantSet variantSet = combiner.getResult();
		if (expectedSize != variantSet.getVariants().size())
			System.err.println("Assert error. Expected " + expectedSize + ". Found " + variantSet.getVariants().size());
		return variantSet;
	}

	@Test
	public void testFindMist() {
		for (File file : new File[]{SP077_VCF, SP030_VCF, SP072_VCF}) {
			findMist(file);
			findBam(file);
		}

	}

	private void findMist(File file) {
		final File[] files = file.getParentFile().listFiles((dir, filename)
				-> filename.toLowerCase().matches(file.getName().replace(".vcf", "").toLowerCase() + ".*\\.mist"));
		Assertions.assertNotNull(files);
		Assertions.assertEquals(files.length, 1);
	}

	private void findBam(File file) {
		final File[] files = file.getParentFile().listFiles((dir, filename)
				-> filename.toLowerCase().matches(file.getName().replace(".vcf", "").toLowerCase() + ".*\\.bam"));
		if (files != null && files.length > 0)
			System.err.println(Arrays.toString(files));
	}

}