package org.uichuimi.variant.io;/*
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

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.jupiter.api.Test;
import org.uichuimi.variant.io.vcf.variant.VariantSet;
import org.uichuimi.variant.io.vcf.io.VariantSetFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by uichuimi on 9/06/16.
 */
public class MemoryTest {

	// 134.3MB /media/uichuimi/Elements/GENOME_DATA/SQZ/SQZ_030/VCF/s030.vep.vcf
	//  95.8MB /media/uichuimi/Elements/GENOME_DATA/SQZ/SQZ_077/VCF/sqz_077.vcf
	// Test result 11/11/2016 476.3MB
	// Test result 11/11/2016 547.7MB
//    private static final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

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

	@Ignore
	@Test
	public void memoryTest() {
		long tic = System.currentTimeMillis();
		System.out.println("         Max       Total        Free       Usage");
		printMemory();
		final List<VariantSet> variantSets = new ArrayList<>();
		for (int i = 0; i < 1000; i++) {
			final InputStream resource = getClass().getResourceAsStream("/files/SP030.vcf");
			variantSets.add(VariantSetFactory.create(resource));
		}
		System.gc();
		printMemory();
		System.out.printf("%tT\n", (System.currentTimeMillis() - tic));
		tic = System.currentTimeMillis();
		for (int i = 0; i < 1000; i++) {
			final InputStream resource = getClass().getResourceAsStream("/files/SP072.vcf");
			variantSets.add(VariantSetFactory.create(resource));
		}
		System.gc();
		printMemory();
		System.out.printf("%tT\n", (System.currentTimeMillis() - tic));
		System.out.println();
	}


}
