package org.uichuimi.vcf.chromosome;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.uichuimi.vcf.io.VariantReader;
import org.uichuimi.vcf.variant.Chromosome;

import java.io.IOException;

public class ChromosomeTest {

	@Test
	public void guessNamespace() {
		try (VariantReader reader = new VariantReader(getClass().getResourceAsStream("/files/to-merge-1.vcf"))) {
			Assertions.assertEquals(Chromosome.Namespace.GRCH, reader.getHeader().getNamespace());
		} catch (IOException e) {
			e.printStackTrace();
		}

		try (VariantReader reader = new VariantReader(getClass().getResourceAsStream("/files/ucsc_header.vcf"))) {
			Assertions.assertEquals(Chromosome.Namespace.UCSC, reader.getHeader().getNamespace());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
