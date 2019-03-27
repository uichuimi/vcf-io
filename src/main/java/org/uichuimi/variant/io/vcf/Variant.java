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

package org.uichuimi.variant.io.vcf;


import org.uichuimi.variant.io.vcf.header.VcfHeader;

import java.util.Arrays;

/**
 * Stores a vcf. chrom, position, ref, alt, filter and format are Strings. position is an integer, qual a double. Info
 * is stored as a map of key=value. If value is null, key is treated as a flag.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class Variant implements Comparable<Variant> {

	/*
	 * A VCF line can hold more than one variant, for instance A/T,TAC
	 * In this case, some fields have several values separated by comma (,)
	 * Some of these fields are ALT, AC, AF, MLEAF and PL
	 *
	 * SQZ102
	 * 1	11944422	.	TACACACAC	T,TAC	2337.73	.	AC=1,1;AF=0.500,0.500;AN=2;DP=61;ExcessHet=3.0103;FS=0.000;MLEAC=1,1;MLEAF=0.500,0.500;MQ=60.00;QD=30.26;SOR=6.701	GT:AD:DP:GQ:PL	1/2:0,9,48:57:99:2375,1613,1523,302,0,133
	 * 1	11944422	rs33981344	TAC	T	177.73	.	AC=1;AF=0.500;AN=2;BaseQRankSum=-2.379e+00;ClippingRankSum=0.00;DB;DP=26;ExcessHet=3.0103;FS=0.000;MLEAC=1;MLEAF=0.500;MQ=60.00;MQRankSum=0.00;QD=9.87;ReadPosRankSum=1.87;SOR=0.540	GT:AD:DP:GQ:PL	0/1:12,6:22:99:215,0,316
	 * 1	11944422	.	T	TACACACAC	680.73	.	AC=1;AF=0.500;AN=2;BaseQRankSum=-2.124e+00;ClippingRankSum=0.00;DP=62;ExcessHet=3.0103;FS=0.000;MLEAC=1;MLEAF=0.500;MQ=59.79;MQRankSum=0.00;QD=17.02;ReadPosRankSum=-7.720e-01;SOR=0.540	GT:AD:DP:GQ:PL	0/1:26,14:48:99:718,0,1819
	 * 1	11944422	rs112065997	TACACAC	T	1256.73	.	AC=2;AF=1.00;AN=2;DB;DP=38;ExcessHet=3.0103;FS=0.000;MLEAC=2;MLEAF=1.00;MQ=60.00;QD=29.49;SOR=4.615	GT:AD:DP:GQ:PL	1/1:0,28:31:93:1293,93,0
	 *
	 */

	private final Coordinate coordinate;
	private final SampleInfo sampleInfo;
	private final Info info;
	private VcfHeader vcfHeader;
	private String[] alleles;
	private String filter;
	private Double qual;
	private Object id;
	//    private String ref;
	//    private String[] alt;

	public Variant(String chrom, int position, String ref, String[] alt, VcfHeader header) {
		this.coordinate = new Coordinate(chrom, position);
		this.alleles = new String[alt.length + 1];
		this.alleles[0] = ref;
		System.arraycopy(alt, 0, alleles, 1, alt.length);
		this.vcfHeader = header;
		sampleInfo = new SampleInfo(this);
		info = new Info();
	}

	public Variant(String chrom, int position, String ref, String alt, VcfHeader header) {
		this(chrom, position, ref, alt.split(","), header);
	}

	/**
	 * Gets the chromosome of the vcf.
	 *
	 * @return the chromosome of the vcf
	 */
	public String getChrom() {
		return coordinate.getChrom();
	}

	/**
	 * Changes the contig. Be sure you explicitly reorder the variants in your dataset after changing the name of the
	 * contigs.
	 *
	 * @param chrom
	 */
	public void setChrom(String chrom) {
		coordinate.setContig(chrom);
	}

	/**
	 * Gets the ID of the vcf.
	 *
	 * @return the ID of the vcf
	 */
	public String getId() {
		return ValueUtils.getString(id);
	}

	public void setId(String id) {
		this.id = ValueUtils.getValue(id, "text");
	}

	public String[] getIdArray() {
		return ValueUtils.isArray(id)
				? (String[]) id
				: new String[]{(String) id};
	}

	/**
	 * Gets the REF value of the vcf.
	 *
	 * @return the ref value
	 */
	public String getRef() {
		return alleles[0];
	}

	/**
	 * Gets the ALT value of the vcf.
	 *
	 * @return the alt value
	 */
	public String getAlt() {
		return ValueUtils.getString(Arrays.copyOfRange(alleles, 1, alleles.length));
	}

	/**
	 * Gets the alt field as an array of Strings.
	 *
	 * @return alt as <code>"A"</code> or <code>String[]{"A", "AC"}</code>
	 */
	public String[] getAltArray() {
		return Arrays.copyOfRange(alleles, 1, alleles.length);
	}

	/**
	 * Gets the position of the vcf.
	 *
	 * @return the position
	 */
	public int getPosition() {
		return coordinate.getPosition();
	}

	public Coordinate getCoordinate() {
		return coordinate;
	}

	/**
	 * Gets the QUAL of the vcf.
	 *
	 * @return the quality
	 */
	public Double getQual() {
		return qual;
	}

	public void setQual(Double qual) {
		this.qual = qual;
	}

	@Override
	public int compareTo(Variant variant) {
		return coordinate.compareTo(variant.coordinate);
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public VcfHeader getVcfHeader() {
		return vcfHeader;
	}

	/**
	 * Get the HGVS identifier of this variant for the first alternative allele.
	 *
	 * @return the HGVS identifier as specified by <a href=http://varnomen.hgvs.org/>Sequence Variant Nomenclature</a>
	 */
	public String getHgvs() {
		final String ref = getRef();
		final String alt = alleles[1];
		final String chrom = getChrom();
		final int position = getPosition();
		if (ref.length() > alt.length()) {
			// DELETION
			//
			// <chrom>:g.<start>[_<end>]del[<seq>]
			//
			// chrom: chromosome
			// start: initial position of deletion (inclusive)
			// end: final position of deletion (inclusive). Mandatory if length of deletion is greater than 1.
			// seq: sequence removed
			//
			// example 1: a T is removed in position 19
			//      111112222
			//      567890123
			// ref: AGAATCACA
			// alt: AGAA_CACA
			//
			// HGVS: chr1:g.19del (or chr1:g.19delT)
			// VCF:  chr1	18 AT 	A
			//
			// example 2: a TCA is removed in position 19
			//      111112222
			//      567890123
			// ref: AGAATCACA
			// al2: AGAA___CA
			//
			// HGVS: chr1:g.19_21del (or chr1:g.19_21delTCA)
			// VCF:  chr1	18 ATCA 	A
			final int length = ref.length() - alt.length();
			final String pos = length == 1
					? String.format("%d", position + 1)
					: String.format("%d_%d", position + 1, position + length);
			return String.format("chr%s:g.%sdel", chrom, pos);
		} else if (alt.length() > ref.length()) {
			// insertion
			// <chrom>:g.<start>_<end>ins<seq>
			//
			// chrom: chromosome
			// start: previous position in ref of insertion
			// end: latter position in ref of insertion (usually start + 1)
			// seq: sequence inserted
			//
			//      11112222
			//      67890123
			// ref: AGAA__CA
			// alt: AGAACACA
			//
			// HGVS: chr1:g.19_20CA
			// VCF:  chr1 19 A ACA
			final String inserted = alt.substring(1);
			return String.format("chr%s:g.%d_%dins%s", chrom, position, position + 1, inserted);
		} else {
			// substitution
			return String.format("chr%s:g.%d%s>%s", chrom, position, ref, alt);
		}
	}

	/**
	 * Changing variant from VariantSet is dangerous, as samples are indexed for current VcfHeader. Instead, create a
	 * new variant and copy data.
	 *
	 * @param vcfHeader new VcfHeader for variant
	 */
	@Deprecated
	public void setVcfHeader(VcfHeader vcfHeader) {
		this.vcfHeader = vcfHeader;
	}

	@Override
	public String toString() {
		return coordinate.getChrom() +
				"\t" + coordinate.getPosition() +
				"\t" + ValueUtils.getString(id) +
				"\t" + alleles[0] +
				"\t" + ValueUtils.getString(Arrays.copyOfRange(alleles, 1, alleles.length)) +
				"\t" + ValueUtils.getString(qual) +
				"\t" + ValueUtils.getString(filter) +
				"\t" + info +
				sampleInfo;
	}

	public SampleInfo getSampleInfo() {
		return sampleInfo;
	}

	public Info getInfo() {
		return info;
	}

	public String[] getAlleles() {
		return alleles;
	}
}
