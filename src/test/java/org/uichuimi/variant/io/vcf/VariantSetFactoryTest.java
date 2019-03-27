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

import org.junit.jupiter.api.Test;
import org.uichuimi.variant.io.vcf.header.ComplexHeaderLine;
import org.uichuimi.variant.io.vcf.header.SimpleHeaderLine;
import org.uichuimi.variant.io.vcf.header.VcfHeader;
import org.uichuimi.variant.io.vcf.io.VariantSetFactory;
import org.uichuimi.variant.io.vcf.variant.VariantSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VariantSetFactoryTest {

	@Test
	public void testLoadFromFile() {
		final VariantSet file = VariantSetFactory.createFromFile(getFile("/files/Sample2.vcf"));
		assertEquals(5, file.getVariants().size());
		assertEquals(6, file.getHeader().getComplexHeaders("INFO").size());
	}

	@Test
	public void size() {
		// Given
		final File file = getFile("/files/Sample1.vcf");
		// When
		final VariantSet variantSet = VariantSetFactory.createFromFile(file);
		// Then
		assertEquals(15, variantSet.getVariants().size());
//        assertEquals(file, variantSet.getFile());
	}

	@Test
	void testWithOneSample() {
		// Given
		final VariantSet variantSet = VariantSetFactory.createFromFile(getFile("/files/Sample1.vcf"));
		final List<String> expected = new ArrayList<>(Collections.singletonList("sample01"));
		final List<String> samples = variantSet.getHeader().getSamples();
		// Then
		assertEquals(expected, samples);
	}

	@Test
	void testWithNoSample() {
		// Given
		final VariantSet variantSet = VariantSetFactory.createFromFile(getFile("/files/NoSample.vcf"));
		final List<String> expected = new ArrayList<>();
		final List<String> samples = variantSet.getHeader().getSamples();
		// Then
		assertEquals(expected, samples);
	}

	@Test
	void testWithMultipleSample() {
		// Given
		final VariantSet variantSet = VariantSetFactory.createFromFile(getFile("/files/MultiSample.vcf"));
		final List<String> expected = Arrays.asList("S_7", "S_75", "S_42", "S_81", "S_8", "S_76", "S_53", "S_82", "S_30", "S_77", "S_70", "S_83", "S_36", "S_78", "S_71", "S_84", "S_37", "S_79", "S_72", "S_85", "S_39", "S_80", "S_73", "S_86", "S_87", "S_99", "S_93", "S_110", "S_88", "S_100", "S_94", "S_111", "S_89", "S_102", "S_95", "S_120", "S_90", "S_103", "S_96", "S_185", "S_91", "S_104", "S_97", "PM", "S_92", "S_105", "S_98", "DAM");
		final List<String> samples = variantSet.getHeader().getSamples();
		// Then
		assertEquals(expected, samples);
	}

	@Test
	public void testComplexHeader() {
		final VcfHeader header = VariantSetFactory.readHeader(getFile("/files/HeaderTest.vcf"));
		final VcfHeader expected = new VcfHeader("VCFv4.1");
		expected.getHeaderLines().add(new SimpleHeaderLine("reference", "human_g1k_v37.fasta"));
		final LinkedHashMap<String, String> filter1 = new LinkedHashMap<>();
		filter1.put("ID", "LowQual");
		filter1.put("Description", "Low quality");
		expected.getHeaderLines().add(new ComplexHeaderLine("FILTER", filter1));

		final LinkedHashMap<String, String> infoMap1 = new LinkedHashMap<>();
		infoMap1.put("ID", "MistZone");
		infoMap1.put("Number", "0");
		infoMap1.put("Type", "Flag");
		infoMap1.put("Description", "If present, indicates that the position is in an MIST Zone");
		expected.getHeaderLines().add(new ComplexHeaderLine("INFO", infoMap1));

		final LinkedHashMap<String, String> infoMap2 = new LinkedHashMap<>();
		infoMap2.put("ID", "AF");
		infoMap2.put("Number", "A");
		infoMap2.put("Type", "Float");
		infoMap2.put("Description", "Allele Frequency, for each ALT allele, in the same order as listed");
		expected.getHeaderLines().add(new ComplexHeaderLine("INFO", infoMap2));

		final LinkedHashMap<String, String> contigMap1 = new LinkedHashMap<>();
		contigMap1.put("ID", "1");
		contigMap1.put("length", "249250621");
		contigMap1.put("assembly", "b37");
		expected.getHeaderLines().add(new ComplexHeaderLine("contig", contigMap1));

		final LinkedHashMap<String, String> contig2 = new LinkedHashMap<>();
		contig2.put("ID", "2");
		contig2.put("length", "243199373");
		contig2.put("assembly", "b37");
		expected.getHeaderLines().add(new ComplexHeaderLine("contig", contig2));

		// Not the best way, but valid
		assertEquals(expected.toString(), header.toString());
	}

	@Test
	public void testSimpleHeader() {
	    /*
         * ##fileformat=VCFv4.1
         * ##reference=file:///home/unidad03/DNA_Sequencing/HomoSapiensGRCh37/human_g1k_v37.fasta
         */
		final VcfHeader header = VariantSetFactory.readHeader(getFile("/files/HeaderTest.vcf"));
		assertEquals("VCFv4.1", header.getSimpleHeader("fileformat").getValue());
		assertEquals("human_g1k_v37.fasta",
				header.getSimpleHeader("reference").getValue());
	}

	@Test
	public void testSaveFile() {
		final VariantSet variantSet = VariantSetFactory.createFromFile(getFile("/files/Sample1.vcf"));
		final File expected = getFile("/files/ExpectedSample1.vcf");
		final File saveFile;
		saveFile = new File(getClass().getResource("/files/").getPath(), "saveFile.vcf");
		variantSet.save(saveFile);
		assertTrue(filesAreEqual(expected, saveFile));
	}

	private File getFile(String filename) {
		return new File(getClass().getResource(filename).getPath());
	}

	@Test
	public void testFormats() {
		final VariantSet variantSet = VariantSetFactory.createFromFile(getFile("/files/Sample1.vcf"));
		assertEquals(Arrays.asList("AD", "DP", "GQ", "GT", "PL"), variantSet.getHeader().getIdList("FORMAT"));
	}

	private boolean filesAreEqual(File expected, File file) {
		try (BufferedReader expectedReader = new BufferedReader(new FileReader(expected));
		     BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
			String expectedLine;
			String fileLine;
			int lineNumber = 0;
			while ((expectedLine = expectedReader.readLine()) != null) {
				fileLine = fileReader.readLine();
				if (lineIsNull(expectedLine, fileLine, lineNumber))
					return false;
				if (lineIsDifferent(expectedLine, fileLine, lineNumber))
					return false;
				lineNumber++;
			}
			fileLine = fileReader.readLine();
			if (fileLine != null) {
				System.err.println("At line " + lineNumber);
				System.err.println("No more lines expected");

			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	private boolean lineIsDifferent(String expectedLine, String fileLine, int lineNumber) {
		if (!fileLine.equals(expectedLine)) {
			printError(expectedLine, fileLine, lineNumber);
			return true;
		}
		return false;
	}

	private boolean lineIsNull(String expectedLine, String fileLine, int lineNumber) {
		if (fileLine == null) {
			printError(expectedLine, null, lineNumber);
			return true;
		}
		return false;
	}

	private void printError(String expectedLine, String fileLine, int lineNumber) {
		System.err.println("At line " + lineNumber);
		System.err.println("Expected: " + expectedLine);
		System.err.println("Current : " + fileLine);
	}

}
