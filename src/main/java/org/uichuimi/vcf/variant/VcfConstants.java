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

package org.uichuimi.vcf.variant;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VcfConstants {

	/**
	 * Represents a missing or null value.
	 */
	public static final String EMPTY_VALUE = ".";
	/**
	 * Main VCF separator. Delimits the main columns CHROM, POS, ID, REF, ALT, QUAL, FILTER, INFO,
	 * FORMAT and samples.
	 */
	public static final String DELIMITER = "\t";
	/**
	 * Delimiter for arrays. Arrays are present in the INFO and sample columns for values with
	 * Number = A, R, G or a number greater than 1.
	 */
	public static final String ARRAY_DELIMITER = ",";
	/**
	 * Delimiter of the INFO field. Delimits the INFO field in key=value pairs.
	 */
	public static final String INFO_DELIMITER = ";";
	/**
	 * Delimiter for the FORMAT and samples columns. The FORMAT column contains the keys.
	 */
	public static final String FORMAT_DELIMITER = ":";
	/**
	 * Each key and value pair is delimited by the = symbol
	 */
	public static final String KEY_VALUE_DELIMITER = "=";

	/**
	 * In INFO and FORMAT header lines, when Type=R, values will be arrays which contain one value
	 * per allele, both reference and alternative.
	 */
	public static final String NUMBER_R = "R";
	/**
	 * In INFO and FORMAT header lines, when Type=A, values will be arrays which contain one value
	 * per alternative allele.
	 */
	public static final String NUMBER_A = "A";
	/**
	 * In INFO and FORMAT header lines, when Type=G, values will be arrays which contain one value
	 * per genotype.
	 */
	public static final String NUMBER_G = "G";
	/**
	 * In INFO and FORMAT header lines, when Type=., values will be arrays which contain an
	 * undetermined number of values.
	 */
	public static final String NUMBER_UNDEFINED = ".";
}
