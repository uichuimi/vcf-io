package org.uichuimi.variant.io.vcf;

import org.junit.jupiter.api.Test;
import org.uichuimi.variant.io.vcf.header.VcfHeader;
import org.uichuimi.variant.io.vcf.input.SuperVariantFactory;
import org.uichuimi.variant.io.vcf.io.SuperVariantWriter;
import org.uichuimi.variant.io.vcf.io.VariantSetFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class SuperVariantFactoryTest {

	@Test
	public void test() {
		final InputStream resource = getClass().getResourceAsStream("/files/Combine1.vcf");
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource))) {
			final VcfHeader header = VariantSetFactory.readHeader(reader);
			final SuperVariantFactory factory = new SuperVariantFactory(header);
			reader.lines().forEach(line -> {
				final SuperVariant variant = factory.parse(line);
				System.out.println(SuperVariantWriter.toString(variant));
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}