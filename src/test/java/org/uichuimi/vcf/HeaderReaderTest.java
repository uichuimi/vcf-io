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
package org.uichuimi.vcf;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.uichuimi.vcf.header.ComplexHeaderLine;
import org.uichuimi.vcf.header.SimpleHeaderLine;
import org.uichuimi.vcf.header.VcfHeader;
import org.uichuimi.vcf.input.HeaderReader;
import org.uichuimi.vcf.input.VariantReader;
import org.uichuimi.vcf.lazy.VariantWriter;

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
class HeaderReaderTest {

	@Test
	void testLoadFromFile() {
		try (VariantReader reader = new VariantReader(getFile("/files/Sample2.vcf"))) {
			Assertions.assertEquals(6, reader.getHeader().getComplexHeaders("INFO").size());
			assertEquals(5, reader.variants().count());
		} catch (IOException e) {
			throw new MissingResourceException("", getClass().getName(), "");
		}
	}

	@Test
	void size() {
		try (VariantReader reader = new VariantReader(getFile("/files/Sample1.vcf"))) {
			assertEquals(15, reader.variants().count());
		} catch (IOException e) {
			throw new MissingResourceException("", getClass().getName(), "");
		}
	}

	@Test
	void testWithOneSample() throws IOException {
		final VcfHeader header = HeaderReader.readHeader(getFile("/files/Sample1.vcf"));
		final List<String> expected = new ArrayList<>(Collections.singletonList("sample01"));
		final List<String> samples = header.getSamples();
		assertEquals(expected, samples);

	}

	@Test
	void testWithNoSample() throws IOException {
		final VcfHeader header = HeaderReader.readHeader(getFile("/files/NoSample.vcf"));
		final List<String> samples = header.getSamples();
		assertEquals(Collections.emptyList(), samples);
	}

	@Test
	void testWithMultipleSample() throws IOException {
		final VcfHeader header = HeaderReader.readHeader(getFile("/files/MultiSample.vcf"));
		final List<String> expected = Arrays.asList("S_7", "S_75", "S_42", "S_81", "S_8", "S_76", "S_53", "S_82", "S_30", "S_77", "S_70", "S_83", "S_36", "S_78", "S_71", "S_84", "S_37", "S_79", "S_72", "S_85", "S_39", "S_80", "S_73", "S_86", "S_87", "S_99", "S_93", "S_110", "S_88", "S_100", "S_94", "S_111", "S_89", "S_102", "S_95", "S_120", "S_90", "S_103", "S_96", "S_185", "S_91", "S_104", "S_97", "PM", "S_92", "S_105", "S_98", "DAM");
		final List<String> samples = header.getSamples();
		assertEquals(expected, samples);
	}

	@Test
	void testComplexHeader() throws IOException {
		final VcfHeader header = HeaderReader.readHeader(getFile("/files/HeaderTest.vcf"));
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
	void testSimpleHeader() throws IOException {
	    /*
         * ##fileformat=VCFv4.1
         * ##reference=file:///home/unidad03/DNA_Sequencing/HomoSapiensGRCh37/human_g1k_v37.fasta
         */
		final VcfHeader header = HeaderReader.readHeader(getFile("/files/HeaderTest.vcf"));
		assertEquals("VCFv4.1", header.getSimpleHeader("fileformat").getValue());
		assertEquals("human_g1k_v37.fasta",
				header.getSimpleHeader("reference").getValue());
	}

	@Test
	void testSaveFile() {
		final File saveFile = new File(getClass().getResource("/files/").getPath(), "saveFile.vcf");
		try (VariantReader reader = new VariantReader(getFile("/files/Sample1.vcf"));
		     VariantWriter writer = new VariantWriter(saveFile)) {
			writer.setHeader(reader.getHeader());
			reader.variants().forEach(writer::write);
		} catch (Exception e) {
			throw new MissingResourceException("", getClass().getName(), "");
		}
		final File expected = getFile("/files/ExpectedSample1.vcf");
		assertTrue(filesAreEqual(expected, saveFile));
	}

	private File getFile(String filename) {
		return new File(getClass().getResource(filename).getPath());
	}

	@Test
	void testFormats() throws IOException {
		final VcfHeader header = HeaderReader.readHeader(getFile("/files/Sample1.vcf"));
		Assertions.assertEquals(Arrays.asList("AD", "DP", "GQ", "GT", "PL"), header.getIdList("FORMAT"));
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
