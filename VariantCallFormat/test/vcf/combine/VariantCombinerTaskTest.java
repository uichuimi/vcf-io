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

import org.junit.Assert;
import org.junit.Test;
import vcf.Sample;
import vcf.Variant;
import vcf.VariantSet;
import vcf.VariantSetFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by uichuimi on 25/05/16.
 */
public class VariantCombinerTaskTest {

    @Test
    public void testIntersectDelete() throws Exception {
        final List<Sample> samples = new ArrayList<>();
        final VariantSet variantSet = VariantSetFactory.createFromFile(new File("test/files/Combine1.vcf"));
        final VariantSet variantSet2 = VariantSetFactory.createFromFile(new File("test/files/Combine2.vcf"));
        variantSet.getHeader().getSamples().stream().map(s -> new Sample(variantSet, s)).forEach(samples::add);
        variantSet2.getHeader().getSamples().stream().map(s -> new Sample(variantSet2, s)).forEach(samples::add);
        final VariantCombinerTask combineTask = new VariantCombinerTask(samples, true);
        final VariantSet start = combineTask.call();
        start.save(new File("output.txt"));
        Assert.assertEquals(17, start.getVariants().size());
        final Variant variant = start.getVariants().stream().findFirst().get();
        Assert.assertEquals("26", variant.getInfo().getString("DP"));
    }
    @Test
    public void testIntersectNoDelete() throws Exception {
        final List<Sample> samples = new ArrayList<>();
        final VariantSet variantSet = VariantSetFactory.createFromFile(new File("test/files/Combine1.vcf"));
        final VariantSet variantSet2 = VariantSetFactory.createFromFile(new File("test/files/Combine2.vcf"));
        variantSet.getHeader().getSamples().stream().map(s -> new Sample(variantSet, s)).forEach(samples::add);
        variantSet2.getHeader().getSamples().stream().map(s -> new Sample(variantSet2, s)).forEach(samples::add);
        final VariantCombinerTask combineTask = new VariantCombinerTask(samples, false);
        final VariantSet start = combineTask.call();
        start.save(new File("output.txt"));
        Assert.assertEquals(20, start.getVariants().size());
        final Variant variant = start.getVariants().stream().findFirst().get();
        Assert.assertEquals("26", variant.getInfo().getString("DP"));
    }

    @Test
    public void testUnaffectedDelete() throws Exception {
        final List<Sample> samples = new ArrayList<>();
        final VariantSet variantSet = VariantSetFactory.createFromFile(new File("test/files/Combine1.vcf"));
        final VariantSet variantSet2 = VariantSetFactory.createFromFile(new File("test/files/Combine2.vcf"));
        variantSet.getHeader().getSamples().stream().map(s -> new Sample(variantSet, s)).forEach(samples::add);
        variantSet2.getHeader().getSamples().stream().map(s -> new Sample(variantSet2, s)).forEach(samples::add);
        samples.get(0).statusProperty().setValue(Sample.Status.UNAFFECTED);
        final VariantCombinerTask combineTask = new VariantCombinerTask(samples, true);
        final VariantSet start = combineTask.call();
        start.save(new File("output.txt"));
        Assert.assertEquals(1, start.getVariants().size());
    }

    @Test
    public void testHomozygoteDelete() throws Exception {
        final List<Sample> samples = new ArrayList<>();
        final VariantSet variantSet = VariantSetFactory.createFromFile(new File("test/files/Combine1.vcf"));
        final VariantSet variantSet2 = VariantSetFactory.createFromFile(new File("test/files/Combine2.vcf"));
        variantSet.getHeader().getSamples().stream().map(s -> new Sample(variantSet, s)).forEach(samples::add);
        variantSet2.getHeader().getSamples().stream().map(s -> new Sample(variantSet2, s)).forEach(samples::add);
        samples.get(3).statusProperty().setValue(Sample.Status.HOMOZYGOTE);
        final VariantCombinerTask combineTask = new VariantCombinerTask(samples, true);
        final VariantSet start = combineTask.call();
        start.save(new File("output.txt"));
        Assert.assertEquals(17, start.getVariants().size());
    }

    @Test
    public void testHeterozygoteDelete() throws Exception {
        final List<Sample> samples = new ArrayList<>();
        final VariantSet variantSet = VariantSetFactory.createFromFile(new File("test/files/Combine1.vcf"));
        final VariantSet variantSet2 = VariantSetFactory.createFromFile(new File("test/files/Combine2.vcf"));
        variantSet.getHeader().getSamples().stream().map(s -> new Sample(variantSet, s)).forEach(samples::add);
        variantSet2.getHeader().getSamples().stream().map(s -> new Sample(variantSet2, s)).forEach(samples::add);
        samples.get(1).statusProperty().setValue(Sample.Status.HETEROZYGOTE);
        final VariantCombinerTask combineTask = new VariantCombinerTask(samples, true);
        final VariantSet start = combineTask.call();
        start.save(new File("output.txt"));
        Assert.assertEquals(17, start.getVariants().size());
    }

    @Test
    public void testBigFiles() throws Exception {
        final VariantSet variantSet = VariantSetFactory.createFromFile(new File("/media/uichuimi/Elements/GENOME_DATA/WDH/WDH_001/VCF/w001.vcf"));
        final VariantSet variantSet2 = VariantSetFactory.createFromFile(new File
                ("/media/uichuimi/Elements/GENOME_DATA/WDH/WDH_002/VCF/w002.vcf"));
        final List<Sample> samples = new ArrayList<>();
        variantSet.getHeader().getSamples().stream().map(s -> new Sample(variantSet, s)).forEach(samples::add);
        variantSet2.getHeader().getSamples().stream().map(s -> new Sample(variantSet2, s)).forEach(samples::add);
        final VariantCombinerTask combineTask = new VariantCombinerTask(samples, true);
        final VariantSet start = combineTask.call();
        start.save(new File("output.txt"));
    }

}