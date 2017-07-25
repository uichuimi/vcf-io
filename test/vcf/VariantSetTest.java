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

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by uichuimi on 25/05/16.
 */
public class VariantSetTest {


    @Test(expected = VariantException.class)
    public void testAddIncompleteInfoHeader() {
        final VariantSet variantSet = new VariantSet();
        final Map<String, String> map = new TreeMap<>();
        // ##INFO=<ID=DP,Number=1,Type=Integer,Description="Total Depth">
        map.put("ID", "DP");
        map.put("Number", "1");
        map.put("Description", "Total Depth");
        variantSet.getHeader().addComplexHeader("INFO", map);
        Assert.assertEquals(0, variantSet.getHeader().getComplexHeaders().get("INFO").size());
    }

    @Test
    public void testAddCompleteInfoHeader() {
        final VariantSet variantSet = new VariantSet();
        final Map<String, String> map = new TreeMap<>();
        //##INFO=<ID=DP,Number=1,Type=Integer,Description="Total Depth">
        map.put("ID", "DP");
        map.put("Number", "1");
        map.put("Description", "Total Depth");
        map.put("Type", "Integer");
        variantSet.getHeader().addComplexHeader("INFO", map);
        Assert.assertEquals(1, variantSet.getHeader().getComplexHeaders().get("INFO").size());
    }

    @Test(expected = VariantException.class)
    public void testAddIncompleteFormatHeader() {
        final VariantSet variantSet = new VariantSet();
        final Map<String, String> map = new TreeMap<>();
        // ##FORMAT=<ID=GT,Number=1,Type=String,Description="Genotype">
        map.put("ID", "GT");
        map.put("Number", "1");
        map.put("Type", "String");
        variantSet.getHeader().addComplexHeader("FORMAT", map);
        Assert.assertEquals(0, variantSet.getHeader().getComplexHeaders().get("FORMAT").size());
    }

    @Test
    public void testAddCompleteFormatHeader() {
        final VariantSet variantSet = new VariantSet();
        final Map<String, String> map = new TreeMap<>();
        // ##FORMAT=<ID=GT,Number=1,Type=String,Description="Genotype">
        map.put("ID", "GT");
        map.put("Number", "1");
        map.put("Type", "String");
        map.put("Description", "Genotype");
        variantSet.getHeader().addComplexHeader("FORMAT", map);
        Assert.assertEquals(1, variantSet.getHeader().getComplexHeaders().get("FORMAT").size());
    }
}