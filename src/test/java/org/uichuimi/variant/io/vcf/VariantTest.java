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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.uichuimi.variant.io.vcf.io.VariantSetFactory;
import org.uichuimi.variant.io.vcf.variant.Genotype;
import org.uichuimi.variant.io.vcf.variant.VariantSet;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by uichuimi on 26/05/16.
 */
public class VariantTest {

	private final VariantSet file = VariantSetFactory.create(getClass().getResourceAsStream("/files/Sample1.vcf"));
	/*
    -> FORMATS
    AD                .   Integer   "Allelic depths for the ref and alt alleles in the order listed"
    DP                1   Integer   "Approximate read depth (reads with MQ=255 or with bad mates are filtered)"
    GQ                1   Integer   "Genotype Quality"
    GT                1   String    "Genotype"
    PL                G   Integer   "Normalized, Phred-scaled likelihoods for genotypes as defined in the VCF..."
    -> INFOS
    AC                A   Integer   "Allele count in genotypes, for each ALT allele, in the same order as listed"
    AF                A   Float     "Allele Frequency, for each ALT allele, in the same order as listed"
    AN                1   Integer   "Total number of alleles in called genotypes"
    BaseQRankSum      1   Float     "Z-score from Wilcoxon rank sum test of Alt Vs. Ref base qualities"
    ClippingRankSum   1   Float     "Z-score From Wilcoxon rank sum test of Alt vs. Ref number of hard clipped bases"
    DB                0   Flag      "dbSNP Membership"
    DP                1   Integer   "Approximate read depth; some reads may have been filtered"
    DS                0   Flag      "Were any of the samples downsampled?"
    FS                1   Float     "Phred-scaled p-value using Fisher's exact test to detect strand bias"
    HaplotypeScore    1   Float     "Consistency of the site with at most two segregating haplotypes"
    InbreedingCoeff   1   Float     "Inbreeding coefficient as estimated from the genotype likelihoods per-sample ..."
    MLEAC             A   Integer   "Maximum likelihood expectation (MLE) for the allele counts (not necessarily ..."
    MLEAF             A   Float     "Maximum likelihood expectation (MLE) for the allele frequency (not necessarily ..."
    MQ                1   Float     "RMS Mapping Quality"
    MQ0               1   Integer   "Total Mapping Quality Zero Reads"
    MQRankSum         1   Float     "Z-score From Wilcoxon rank sum test of Alt vs. Ref read mapping qualities"
    ReadPosRankSum    1   Float     "Z-score from Wilcoxon rank sum test of Alt vs. Ref read position bias"
    SOR               1   Float     "Symmetric Odds Ratio of 2x2 contingency table to detect strand bias"
#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO	FORMAT	sample01
     * 1	13273	.	G	C	124.77	.	AC=1;AF=0.500;AN=2;BaseQRankSum=0.972;ClippingRankSum=-0.972;DP=26;FS=0.000;MLEAC=1;MLEAF=0.500;MQ=26.99;MQ0=0;MQRankSum=0.472;QD=4.80;ReadPosRankSum=-0.361;SOR=0.947	GT:AD:DP:GQ:PL	0/1:18,8:26:99:153,0,428
     * 1	69511	rs75062661	A	G	1592.77	.	AC=2;AF=1.00;AN=2;DB;DP=60;FS=0.000;MLEAC=2;MLEAF=1.00;MQ=31.44;MQ0=0;QD=26.55;SOR=1.316	GT:AD:DP:GQ:PL	1/1:0,59:59:99:1621,176,0
     * 1	133160	.	G	A	118.77	.	AC=1;AF=0.500;AN=2;BaseQRankSum=-1.221;ClippingRankSum=-0.322;DP=8;FS=0.000;MLEAC=1;MLEAF=0.500;MQ=35.55;MQ0=0;MQRankSum=-0.956;QD=14.85;ReadPosRankSum=0.322;SOR=1.329	GT:AD:DP:GQ:PL	0/1:3,5:8:89:147,0,89
     * 1	139213	.	A	G,C	67.77	.	AC=1;AF=0.500;AN=2;BaseQRankSum=0.296;ClippingRankSum=0.895;DP=10;FS=0.000;MLEAC=1;MLEAF=0.500;MQ=31.98;MQ0=0;MQRankSum=0.296;QD=6.78;ReadPosRankSum=0.296;SOR=0.223	GT:AD:DP:GQ:PL	0/1:7,3:10:96:96,0,334
     * 1	139233	.	C	A	69.77	.	AC=1;AF=0.500;AN=2;BaseQRankSum=1.683;ClippingRankSum=0.248;DP=9;FS=4.771;MLEAC=1;MLEAF=0.500;MQ=34.96;MQ0=0;MQRankSum=0.248;QD=7.75;ReadPosRankSum=1.001;SOR=0.045	GT:AD:DP:GQ:PL	0/1:6,3:9:98:98,0,308
     * 1	651149	.	C	T	40.74	.	AC=2;AF=1.00;AN=2;DP=2;FS=0.000;MLEAC=2;MLEAF=1.00;MQ=29.00;MQ0=0;QD=20.37;SOR=2.303	GT:AD:DP:GQ:PL	1/1:0,2:2:6:68,6,0
     * 1	715348	rs3131984	T	G	85.28	.	AC=2;AF=1.00;AN=2;DB;DP=3;FS=0.000;MLEAC=2;MLEAF=1.00;MQ=51.62;MQ0=0;QD=28.43;SOR=2.833	GT:AD:DP:GQ:PL	1/1:0,3:3:9:113,9,0
     * 1	752566	rs3094315	G	A	190.84	.	AC=2;AF=1.00;AN=2;DB;DP=6;FS=0.000;MLEAC=2;MLEAF=1.00;MQ=49.84;MQ0=0;QD=31.81;SOR=3.912	GT:AD:DP:GQ:PL	1/1:0,6:6:18:219,18,0
     * 1	752721	rs3131972	A	G	1228.77	.	AC=2;AF=1.00;AN=2;DB;DP=38;FS=0.000;MLEAC=2;MLEAF=1.00;MQ=49.11;MQ0=0;QD=32.34;SOR=1.670	GT:AD:DP:GQ:PL	1/1:0,38:38:99:1257,114,0
     * 1	752894	rs3131971	T	C	440.77	.	AC=2;AF=1.00;AN=2;DB;DP=15;FS=0.000;MLEAC=2;MLEAF=1.00;MQ=32.92;MQ0=0;QD=29.38;SOR=5.549	GT:AD:DP:GQ:PL	1/1:0,15:15:45:469,45,0
     * 1	754182	rs3131969	A	G	62.74	.	AC=2;AF=1.00;AN=2;DB;DP=2;FS=0.000;MLEAC=2;MLEAF=1.00;MQ=60.00;MQ0=0;QD=31.37;SOR=0.693	GT:AD:DP:GQ:PL	1/1:0,2:2:6:90,6,0
     * 1	754192	rs3131968	A	G	.	.	AC=2;AF=1.00;AN=2;DB;DP=2;FS=0.000;MLEAC=2;MLEAF=1.00;MQ=60.00;MQ0=0;
     * QD=31.37;SOR=0.693	GT:AD:DP:GQ:PL	1/1:0,2:2:6:90,6,0
     * 7	150968234	.	C	T	58.28	.	AC=2;AF=1.00;AN=2;DP=3;FS=0.000;MLEAC=2;MLEAF=1.00;MQ=60.00;MQ0=0;QD=19.43;SOR=1.179	GT:AD:DP:GQ:PL	1/1:0,3:3:9:86,9,0
     * 7	150972189	.	G	A	667.77	.	AC=1;AF=0.500;AN=2;BaseQRankSum=0.146;ClippingRankSum=0.851;DP=59;FS=4.409;MLEAC=1;MLEAF=0.500;MQ=60.00;MQ0=0;MQRankSum=0.468;QD=11.32;ReadPosRankSum=1.035;SOR=0.284	GT:AD:DP:GQ:PL	0/1:34,25:59:99:696,0,1021
     * 7	150979714	.	T	A	75.78	.	AC=1;AF=0.500;AN=2;BaseQRankSum=0.727;ClippingRankSum=0.727;DP=4;FS=0.000;MLEAC=1;MLEAF=0.500;MQ=60.00;MQ0=0;MQRankSum=0.727;QD=18.95;ReadPosRankSum=0.727;SOR=1.609	GT:AD:DP:GQ:PL	0/1:1,3:4:25:104,0,25
     */

	@Test
	void testVcfFile() {
		for (Variant variant : file.getVariants())
			assertEquals(file.getHeader(), variant.getVcfHeader());
	}

	@Test
	void testChrom() {
		final String[] chroms = {"1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "7", "7", "7"};
		int i = 0;
		for (Variant variant : file.getVariants())
			assertEquals(chroms[i++], variant.getChrom());
	}

	@Test
	void testPos() {
		final int[] pos = {13273, 69511, 133160, 139213, 139233, 651149, 715348, 752566, 752721, 752894, 754182, 754192, 150968234, 150972189, 150979714};
		int i = 0;
		for (Variant variant : file.getVariants())
			assertEquals(pos[i++], variant.getPosition());
	}

	@Test
	void testId() {
		final String[] values = {".", "rs75062661", ".", ".", ".", ".", "rs3131984", "rs3094315", "rs3131972", "rs3131971", "rs3131969", "rs3131968", ".", ".", "."};
		int i = 0;
		for (Variant variant : file.getVariants())
			assertEquals(values[i++], variant.getId());
	}

	@Test
	public void testRef() {
		final String[] values = {"G", "A", "G", "A", "C", "C", "T", "G", "A", "T", "A", "A", "C", "G", "T"};
		int i = 0;
		for (Variant variant : file.getVariants())
			assertEquals(values[i++], variant.getRef());
	}

	@Test
	public void testAlt() {
		final Object[] values = {"C", "G", "A", "G,C", "A", "T", "G", "A", "G", "C", "G", "G", "T",
				"A", "A"};
		int i = 0;
		for (Variant variant : file.getVariants())
			assertEquals(values[i++], variant.getAlt());
	}

	@Test
	public void testQual() {
		final Object[] quals = {124.77, 1592.77, 118.77, 67.77, 69.77, 40.74, 85.28, 190.84, 1228.77, 440.77, 62.74,
				null, 58.28, 667.77, 75.78};
		int i = 0;
		for (Variant variant : file.getVariants())
			assertEquals(quals[i++], variant.getQual());
	}

	@Test
	public void testFilter() {
		final String[] values = {null, null, null, null, null, null, null, null,
				null, "PASS", null, null, null, null, null};
		int i = 0;
		for (Variant variant : file.getVariants())
			assertEquals(values[i++], variant.getFilter());
	}

	@Test
	public void testInfo() {
		final Map<String, Object[]> values = new HashMap<>();
		values.put("AC", new Object[]{1, 2, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1});
		values.put("AF", new Object[]{.5, 1., .5, .5, .5, 1., 1., 1., 1., 1., 1., 1., 1., .5, .5});
		values.put("AN", new Object[]{2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2});
		values.put("DB", new Object[]{null, true, null, null, null, null, true, true, true, true, true, true, null,
				null, null});
		for (Map.Entry<String, Object[]> entry : values.entrySet()) {
			int i = 0;
			for (Variant variant : file.getVariants()) {
				final String message = String.format("at line %d for ID=%s", i, entry.getKey());
				assertEquals(entry.getValue()[i++],
						variant.getInfo().get(entry.getKey()), message);
			}
		}
		int i = 0;
		final Boolean[] db = new Boolean[]{false, true, false, false, false, false, true, true, true, true, true, true, false,
				false, false};
		for (Variant variant : file.getVariants()) {
			assertEquals(db[i++], variant.getInfo().getBoolean("DB"), String.format("at line %d for ID=DB", i));
		}
	}

	@Test
	public void testFormat() {
		final Map<String, Object[]> values = new HashMap<>();
		values.put("GT", new String[]{"0/1", "1/1", "0/1", "0/1", "0/1", "1/1", "1/1", "1/1", "1/1", "1/1", "1/1", "1/1", "1/1", "0/1", "0/1"});
		values.put("AD", new String[]{"18,8", "0,59", "3,5", "7,3", "6,3", "0,2", "0,3", "0,6", "0,38", "0,15", "0,2", "0,2", "0,3", "34,25", "1,3"});
		values.put("DP", new String[]{"26", "59", "8", "10", "9", "2", "3", "6", "38", "15", "2", "2", "3", "59", "4"});
		values.put("GQ", new String[]{"99", "99", "89", "96", "98", "6", "9", "18", "99", "45", "6", "6", "9", "99", "25"});
		values.put("PL", new String[]{"153,0,428", "1621,176,0", "147,0,89", "96,0,334", "98,0,308", "68,6,0", "113,9,0", "219,18,0", "1257,114,0", "469,45,0", "90,6,0", "90,6,0", "86,9,0", "696,0,1021", "104,0,25"});
		for (Map.Entry<String, Object[]> entry : values.entrySet()) {
			int i = 0;
			for (Variant variant : file.getVariants()) {
				final String message = String.format("line %d, key %s", i, entry.getKey());
				assertEquals(entry.getValue()[i++], variant.getSampleInfo().getFormat("sample01", entry.getKey()), message);
			}
		}
	}

	@Test
	public void testSetId() {
		// Given
		final Variant variant = new Variant("1", 14, "A", "T", null);
		// When
		variant.setId("rs00002");
		// Then
		assertEquals("rs00002", variant.getId());
	}

	@Test
	public void testSetQual() {
		// Given
		final Variant variant = new Variant("1", 14, "A", "T", null);
		// When
		variant.setQual(123.45);
		// Then
		assertEquals(123.45, variant.getQual(), 0.001);
	}

	@Test
	public void testCompare() {
//        for (int i = 0; i < file.getVariants().size() - 1; i++) {
//            assertEquals(-1, file.getVariants().get(i).compareTo(file.getVariants().get(i + 1)));
//        }
	}

	@Test
	public void testAddInfo() {
		final Variant variant = new Variant("1", 14, "A", "T", null);
		variant.getInfo().set("DP", "23");
		assertEquals("23", variant.getInfo().get("DP"));
	}

	@Test
	public void testModifySamples() {
		final Variant variant = new Variant("1", 15000, "A", "C,T", file.getHeader());
//        variant.getSampleInfo().setFormat("pepe", "GT", "0/0");
		assertEquals(null, variant.getSampleInfo().getFormat("pepe", "GT"));
		variant.getVcfHeader().getSamples().add("pepe");
		variant.getSampleInfo().setFormat("pepe", "GT", "0/1");
		assertEquals("0/1", variant.getSampleInfo().getFormat("pepe", "GT"));
	}

	@Test
	public void testGenotype() {
		final Variant variant = file.getVariants().stream().findFirst().get();
		assertEquals(Genotype.HETEROZYGOUS, variant.getSampleInfo().getGenotype("sample01"));
	}

	@Test
	public void testHgvs() {
		Variant variant = new Variant("1", 19, "A", "AT", null);
		Assertions.assertEquals("chr1:g.19_20insT", variant.getHgvs());
		variant = new Variant("1", 20, "AT", "A", null);
		Assertions.assertEquals("chr1:g.21del", variant.getHgvs());
		variant = new Variant("1", 20, "ATC", "A", null);
		Assertions.assertEquals("chr1:g.21_22del", variant.getHgvs());
	}

}