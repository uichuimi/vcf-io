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

import org.junit.Ignore;
import org.junit.Test;
import vcf.VariantSet;
import vcf.io.VariantSetFactory;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by uichuimi on 9/06/16.
 */
public class MemoryTest {

    // 134.3MB /media/uichuimi/Elements/GENOME_DATA/SQZ/SQZ_030/VCF/s030.vep.vcf
    //  95.8MB /media/uichuimi/Elements/GENOME_DATA/SQZ/SQZ_077/VCF/sqz_077.vcf
    // Test result 11/11/2016 476.3MB
    // Test result 11/11/2016 547.7MB
    private static final File input = new File("/media/uichuimi/Elements/GENOME_DATA/SQZ/SQZ_030/VCF/s030.vep.vcf");
    private static final File input2 = new File("/media/uichuimi/Elements/GENOME_DATA/SQZ/SQZ_077/VCF/sqz_077.vcf");
    private static final File input3 = new File("/media/uichuimi/DiscoInterno/GENOME_DATA/DTM/aa_more/DTM.vcf");
    private static final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private static void printMemory() {
        final long max = Runtime.getRuntime().maxMemory();
        final long total = Runtime.getRuntime().totalMemory();
        final long free = Runtime.getRuntime().freeMemory();
        System.out.println(getFormattedBytes(max)
                + getFormattedBytes(total)
                + getFormattedBytes(free)
                + getFormattedBytes(total - free));
    }

    private static String getFormattedBytes(long bytes) {
        return String.format("%1$12s", humanReadableByteCount(bytes, false));
    }

    /**
     * Takes a byte value and convert it to the corresponding human readable unit.
     *
     * @param bytes value in bytes
     * @param si    if true, divides by 1000; else by 1024
     * @return a human readable size
     */
    public static String humanReadableByteCount(long bytes, boolean si) {
        final int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        final String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    @Test
    public void memoryTest() {
        long tic = System.currentTimeMillis();
        System.out.println("         Max       Total        Free       Usage");
        printMemory();
        final VariantSet variantSet1 = VariantSetFactory.createFromFile(input);
        System.gc();
        printMemory();
        System.out.printf("%tT\n", (System.currentTimeMillis() - tic));
        tic = System.currentTimeMillis();
        final VariantSet variantSet = VariantSetFactory.createFromFile(input2);
        System.gc();
        printMemory();
        System.out.printf("%tT\n", (System.currentTimeMillis() - tic));
        System.out.println();
    }


}
