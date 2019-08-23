package org.uichuimi.vcf.combine;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.uichuimi.vcf.io.MultipleVariantReader;
import org.uichuimi.vcf.variant.Variant;

import java.io.InputStream;
import java.util.List;

public class VariantMergerTest {

	@Test
	public void differentReferenceAlleles() {
		//          ref         alt
		// a:       CCA	        C
		// b:       CCACACA     C,CCA,CCACA,CCACACACA
		// merged:  CCACACA     C,CCA,CCACA,CCACACACA
		final InputStream a = getClass().getResourceAsStream("/files/combine/differentReferenceAllelesA.vcf");
		final InputStream b = getClass().getResourceAsStream("/files/combine/differentReferenceAllelesB.vcf");
		try (MultipleVariantReader reader = new MultipleVariantReader(List.of(a, b))) {
			final Variant variant = reader.nextMerged();
			Assertions.assertEquals(1, variant.getReferences().size());
			Assertions.assertEquals("CCACACA", variant.getReferences().get(0));
			Assertions.assertEquals(List.of("C", "CCA", "CCACA", "CCACACACA"), variant.getAlternatives());
			final int jmg = reader.getHeader().getSamples().indexOf("JMG");
			// JMG is 1/1 (C/C) in a, thus 3/3 (CCACA/CCACA) in b
			Assertions.assertEquals("3/3", variant.getSampleInfo(jmg).<String>get("GT"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
