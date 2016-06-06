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

package vcf;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 * Created by uichuimi on 24/05/16.
 */
public class SampleTest {

    @Test
    public void test() {
        final VariantSet file = VariantSetFactory.createFromFile(new File("test/files/Sample2.vcf"));
        final Sample sample = new Sample(file, file.getHeader().getSamples().get(0));
        final Sample sample1 = new Sample(file, file.getHeader().getSamples().get(1));
        final Sample sample2 = new Sample(file, file.getHeader().getSamples().get(2));
        Assert.assertEquals("NA00001", sample.getName());
//        Assert.assertEquals(2, sample.getVariants().size());
//        Assert.assertEquals(4, sample1.getVariants().size());
//        Assert.assertEquals(3, sample2.getVariants().size());
    }
}
