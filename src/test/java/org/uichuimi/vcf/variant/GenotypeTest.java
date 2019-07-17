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
}
