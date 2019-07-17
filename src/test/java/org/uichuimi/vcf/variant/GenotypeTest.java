package org.uichuimi.vcf.variant;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.uichuimi.vcf.combine.VariantMerger;
import org.uichuimi.vcf.header.FormatHeaderLine;
import org.uichuimi.vcf.header.VcfHeader;

import java.util.List;

public class GenotypeTest {

	@Test
	public void testMerge() {
		final VcfHeader header = new VcfHeader("VCFv4.2");
		header.getSamples().add("S1");
		header.getSamples().add("S2");
		header.getSamples().add("S3");
		header.addHeaderLine(new FormatHeaderLine("GT", "1", "String", "Genotype"));
		final Coordinate coordinate = new Coordinate("1", 34533);
		final Variant v1 = new Variant(header, coordinate, List.of("T"), List.of("A"));
		final Variant v2 = new Variant(header, coordinate, List.of("T"), List.of("G"));
		v1.getSampleInfo(0).set("GT", "0/1");
		v2.getSampleInfo(1).set("GT", "0/1");
		v2.getSampleInfo(2).set("GT", ".");
		final Variant result = VariantMerger.merge(List.of(v1, v2), header);
		Assertions.assertEquals("0/2", result.getSampleInfo(1).get("GT"));
		Assertions.assertNull(result.getSampleInfo(2).get("GT"));
	}

	@Test
	public void testDropTrailing() {
		final VcfHeader header = new VcfHeader("VCFv4.3");
		header.getSamples().add("S1");
		header.getSamples().add("S2");
		header.getSamples().add("S3");
		header.addHeaderLine(new FormatHeaderLine("GT", "1", "String", "Genotype"));
		header.addHeaderLine(new FormatHeaderLine("AD", "R", "Integer", "Read depth for each allele"));
		header.addHeaderLine(new FormatHeaderLine("DP", "1", "Integer", "Read depth"));
		header.addHeaderLine(new FormatHeaderLine("GL", "G", "Float", "Genotype likelihoods"));
		final Variant variant = new Variant(header, "1\t123\trs1\tT\tA\t23\t.\t.\tGT:AD:DP:GL\t0/1:2,3:5:100,0,5\t.\t1/1:0,10:10");
		Assertions.assertNull(variant.getSampleInfo(1).get("AD"));
		Assertions.assertNull(variant.getSampleInfo(1).get("GT"));
		Assertions.assertNull(variant.getSampleInfo(2).get("GL"));
		Assertions.assertEquals(10, variant.getSampleInfo(2).<List<Integer>>get("AD").get(1));
	}
}
