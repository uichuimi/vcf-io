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

import de.saxsys.mvvmfx.testingutils.jfxrunner.JfxRunner;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import vcf.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by uichuimi on 25/05/16.
 */
@RunWith(JfxRunner.class)
public class VariantCombinerTaskTest {

    private final static File COMBINE_1 = new File("test/files/Combine1.vcf");
    private final static File COMBINE_2 = new File("test/files/Combine2.vcf");
    @Test
    public void testIntersectDelete() throws Exception {
        final List<Sample> samples = new ArrayList<>();
        VcfHeader header = VariantSetFactory.readHeader(COMBINE_1);
        header.getSamples().stream().map(s -> new Sample(COMBINE_1, s)).forEach(samples::add);
        VcfHeader header1 = VariantSetFactory.readHeader(COMBINE_2);
        header1.getSamples().stream().map(s -> new Sample(COMBINE_2, s)).forEach(samples::add);
        final VariantCombinerTask combineTask = new VariantCombinerTask(samples, true);
        final VariantSet start = combineTask.call();
        start.save(new File("output.txt"));
        Assert.assertEquals(1, start.getVariants().size());
        final Variant variant = start.getVariants().stream().findFirst().get();
        Assert.assertEquals("10", variant.getInfo().getString("DP"));
    }
    @Test
    public void testIntersectNoDelete() throws Exception {
        final List<Sample> samples = new ArrayList<>();
        VcfHeader header = VariantSetFactory.readHeader(COMBINE_1);
        header.getSamples().stream().map(s -> new Sample(COMBINE_1, s)).forEach(samples::add);
        VcfHeader header1 = VariantSetFactory.readHeader(COMBINE_2);
        header1.getSamples().stream().map(s -> new Sample(COMBINE_2, s)).forEach(samples::add);
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
        VcfHeader header = VariantSetFactory.readHeader(COMBINE_1);
        header.getSamples().stream().map(s -> new Sample(COMBINE_1, s)).forEach(samples::add);
        VcfHeader header1 = VariantSetFactory.readHeader(COMBINE_2);
        header1.getSamples().stream().map(s -> new Sample(COMBINE_2, s)).forEach(samples::add);
        samples.get(0).statusProperty().setValue(Sample.Status.UNAFFECTED);
        final VariantCombinerTask combineTask = new VariantCombinerTask(samples, true);
        final VariantSet start = combineTask.call();
        start.save(new File("output.txt"));
        Assert.assertEquals(1, start.getVariants().size());
    }

    @Test
    public void testHomozygoteDelete() throws Exception {
        final List<Sample> samples = new ArrayList<>();
        VcfHeader header = VariantSetFactory.readHeader(COMBINE_1);
        header.getSamples().stream().map(s -> new Sample(COMBINE_1, s)).forEach(samples::add);
        VcfHeader header1 = VariantSetFactory.readHeader(COMBINE_2);
        header1.getSamples().stream().map(s -> new Sample(COMBINE_2, s)).forEach(samples::add);
        samples.get(3).statusProperty().setValue(Sample.Status.HOMOZYGOTE);
        final VariantCombinerTask combineTask = new VariantCombinerTask(samples, true);
        final VariantSet start = combineTask.call();
        start.save(new File("output.txt"));
        Assert.assertEquals(1, start.getVariants().size());
    }

    @Test
    public void testHeterozygoteDelete() throws Exception {
        final List<Sample> samples = new ArrayList<>();
        VcfHeader header = VariantSetFactory.readHeader(COMBINE_1);
        header.getSamples().stream().map(s -> new Sample(COMBINE_1, s)).forEach(samples::add);
        VcfHeader header1 = VariantSetFactory.readHeader(COMBINE_2);
        header1.getSamples().stream().map(s -> new Sample(COMBINE_2, s)).forEach(samples::add);
        samples.get(1).statusProperty().setValue(Sample.Status.HETEROZYGOTE);
        final VariantCombinerTask combineTask = new VariantCombinerTask(samples, true);
        final VariantSet start = combineTask.call();
        start.save(new File("output.txt"));
        Assert.assertEquals(1, start.getVariants().size());
    }

    @Test @Ignore
    public void testBigFiles() throws Exception {
        final File w001 = new File("/media/uichuimi/Elements/GENOME_DATA/WDH/WDH_001/VCF/w001.vcf");
        final File w002 = new File("/media/uichuimi/Elements/GENOME_DATA/WDH/WDH_002/VCF/w002.vcf");
        final List<Sample> samples = new ArrayList<>();
        VcfHeader header = VariantSetFactory.readHeader(w001);
        header.getSamples().stream().map(s -> new Sample(w001, s)).forEach(samples::add);
        VcfHeader header1 = VariantSetFactory.readHeader(w002);
        header1.getSamples().stream().map(s -> new Sample(w002, s)).forEach(samples::add);
        final VariantCombinerTask combineTask = new VariantCombinerTask(samples, true);
        final VariantSet start = combineTask.call();
        Assert.assertEquals(143548, start.getVariants().size());
        start.save(new File("output.txt"));
    }

}