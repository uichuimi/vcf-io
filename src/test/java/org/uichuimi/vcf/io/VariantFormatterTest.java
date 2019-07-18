package org.uichuimi.vcf.io;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.uichuimi.vcf.header.FormatHeaderLine;
import org.uichuimi.vcf.header.VcfHeader;
import org.uichuimi.vcf.variant.Variant;

import java.util.List;

class VariantFormatterTest {

	@Test
	public void testDropMissingGenotypeInfo() {
		final VcfHeader header = new VcfHeader("VCFv4.3");
		header.getSamples().add("S1");
		header.getSamples().add("S2");
		header.getSamples().add("S3");
		header.addHeaderLine(new FormatHeaderLine("GT", "1", "String", "Genotype"));
		header.addHeaderLine(new FormatHeaderLine("AD", "R", "Integer", "Read depth for each allele"));
		header.addHeaderLine(new FormatHeaderLine("DP", "1", "Integer", "Read depth"));
		header.addHeaderLine(new FormatHeaderLine("GL", "G", "Float", "Genotype likelihoods"));
		final String line = "1\t123\trs1\tT\tA\t23\t.\t.\tGT:AD:DP:GL\t0/1:2,3:5:100,0,5\t.\t1/1:0,10:10";
		final Variant variant = new Variant(header, line);
		Assertions.assertNull(variant.getSampleInfo(1).get("AD"));
		Assertions.assertNull(variant.getSampleInfo(1).get("GT"));
		Assertions.assertNull(variant.getSampleInfo(2).get("GL"));
		Assertions.assertEquals(10, variant.getSampleInfo(2).<List<Integer>>get("AD").get(1));
		Assertions.assertEquals(line, VariantFormatter.toVcf(variant));
	}
}