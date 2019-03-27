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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by uichuimi on 9/06/16.
 */
public class MistFactory {
	private static final String MIST_HEADER = "chrom\texon_start\texon_end\tmist_start\tmist_end\tgene_id\tgene_name\texon_number\texon_id\ttranscript_name\tbiotype\tmatch";

	public static Mist load(File file) {
		final Mist mist = new Mist();
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			reader.lines()
					.filter(line -> !line.startsWith("#"))
					.filter(line -> !line.equals(MIST_HEADER))
					.map(line -> line.split("\t")).forEach(line -> {
				// chrom, exon_start, exon_end, mist_start, mist_end, gene_id, gene_name, exon_number, exon_id,
				// transcript_name, biotype, match
				final String chrom = line[0];
				final int start = Integer.valueOf(line[3]);
				final int end = Integer.valueOf(line[4]);
				mist.addRegion(chrom, start, end);
			});

		} catch (IOException e) {
			e.printStackTrace();
		}
		return mist;

	}
}
