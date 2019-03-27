package org.uichuimi.variant.io.vcf;

import org.junit.jupiter.api.Test;
import org.uichuimi.variant.io.vcf.header.VcfHeader;
import org.uichuimi.variant.io.vcf.input.VariantContextFactory;
import org.uichuimi.variant.io.vcf.io.VariantContextWriter;
import org.uichuimi.variant.io.vcf.io.VariantSetFactory;
import org.uichuimi.variant.io.vcf.variant.VariantContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class VariantContextFactoryTest {

	@Test
	public void test() {
		final InputStream resource = getClass().getResourceAsStream("/files/Combine1.vcf");
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource))) {
			final VcfHeader header = VariantSetFactory.readHeader(reader);
			final VariantContextFactory factory = new VariantContextFactory(header);
			reader.lines().forEach(line -> {
				final VariantContext variant = factory.parse(line);
				System.out.println(VariantContextWriter.toVcf(variant));
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}