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

package vcf.combine;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import vcf.Genotype;
import vcf.VariantSet;
import vcf.VcfHeader;
import vcf.io.VariantSetFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by uichuimi on 25/05/16.
 */
public class VariantCombinerTest {

    private final static File SP030_VCF = new File("test/files/SP030.vcf");
    private final static File SP072_VCF = new File("test/files/SP072.vcf");
    private final static File SP077_VCF = new File("test/files/SP077.vcf");
    private final static File SP030_MIST = new File("test/files/SP030.mist");
    private final static File SP072_MIST = new File("test/files/SP072.mist");
    private final static File SP077_MIST = new File("test/files/SP077.mist");
    private static final File DA_VCF = new File("test/files/DA.vcf");
    private static final File DA_MIST = new File("test/files/DA.mist");

    private static final File[] ALL_VCF = new File[]{SP030_VCF, SP072_VCF, SP077_VCF, DA_VCF};

    @Disabled
    public void testJoin() {
        final List<Sample> samples = new ArrayList<>();
        final Sample sp030 = new Sample(SP030_VCF, "SP030");
        sp030.setMistFile(SP030_MIST);
        samples.add(sp030);
        final Sample sp072 = new Sample(SP072_VCF, "SP072");
        sp030.setMistFile(SP072_MIST);
        samples.add(sp072);
        final Sample sp077 = new Sample(SP077_VCF, "SP077");
        sp030.setMistFile(SP077_MIST);
        samples.add(sp077);
        final VariantSet variantSet = testCombine(samples, false, 94);
//        variantSet.save(new File("SP_30_72_77_sum.vcf"));
    }

    @Disabled
    public void testCommonWithoutMist() {
        final List<Sample> samples = new ArrayList<>();
        final Sample sp030 = new Sample(SP030_VCF, "SP030");
        samples.add(sp030);
        final Sample sp072 = new Sample(SP072_VCF, "SP072");
        samples.add(sp072);
        final Sample sp077 = new Sample(SP077_VCF, "SP077");
        samples.add(sp077);
        final VariantSet variantSet = testCombine(samples, true, 4);
//        variantSet.save(new File("SP_30_72_77_common.vcf"));
    }

    @Disabled
    public void testCommonWithMist() {
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
        final VariantSet variantSet = testCombine(samples, true, 13);
//        variantSet.save(new File("SP_30_72_77_common_mist.vcf"));

    }

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
        final File file = new File("test/files/MultiSample.vcf");
        final VcfHeader header = VariantSetFactory.readHeader(file);
        header.getSamples().forEach(s -> samples.add(new Sample(file, s)));
        samples.get(2).setGenotype(Genotype.WILD);
        testCombine(samples, true, 12767);

    }

    private VariantSet testCombine(List<Sample> samples, boolean remove, long expectedSize) {
        final VariantCombiner combiner = new VariantCombiner(samples, remove);
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
        if (files != null && files.length > 0)
            System.err.println(Arrays.toString(files));
    }

    private void findBam(File file) {
        final File[] files = file.getParentFile().listFiles((dir, filename)
                -> filename.toLowerCase().matches(file.getName().replace(".vcf", "").toLowerCase() + ".*\\.bam"));
        if (files != null && files.length > 0)
            System.err.println(Arrays.toString(files));
    }

}