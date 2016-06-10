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

import org.junit.Test;
import vcf.Variant;
import vcf.VariantSet;
import vcf.VariantSetFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by uichuimi on 9/06/16.
 */
public class MemoryTest {

    // /media/uichuimi/Elements/GENOME_DATA/SQZ/SQZ_030/VCF/s030.vep.vcf
    // Disk size = 134MB
    private static final File input = new File("/media/uichuimi/Elements/GENOME_DATA/SQZ/SQZ_030/VCF/s030.vep.vcf");
    private static final File input2 = new File("/media/uichuimi/Elements/GENOME_DATA/SQZ/SQZ_077/VCF/sqz_077.vcf");
    private static final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    @Test
    public void memoryTest() {
        final long startMillis = System.currentTimeMillis();
        System.out.println("Total\tMax\tFree\tUsage");
        printMemory();
        final VariantSet variantSet1 = VariantSetFactory.createFromFile(input);
        System.gc();
        printMemory();
        final VariantSet variantSet = VariantSetFactory.createFromFile(input2);
        System.gc();
        printMemory();
        final long totalMillis = System.currentTimeMillis() - startMillis;
        System.out.println(dateFormat.format(totalMillis));

    }

    private static void printMemory() {
        final long total = Runtime.getRuntime().totalMemory();
        final long max = Runtime.getRuntime().maxMemory();
        final long free = Runtime.getRuntime().freeMemory();
        System.out.println(total + "\t" + max + "\t" + free + "\t" + (total - free));
//        System.out.println("Total\t" + total);
//        System.out.println("Max: " + max);
//        System.out.println("Free: " + free);
//        System.out.println("Total - free: " + (total - free));
//        System.out.println("Max - free: " + (max - free));
    }

}
