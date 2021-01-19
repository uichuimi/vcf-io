package org.uichuimi.vcf;

import org.junit.jupiter.api.Test;
import org.uichuimi.vcf.io.MultipleVariantReader;
import org.uichuimi.vcf.variant.Variant;

import java.io.InputStream;
import java.util.List;

public class FailingVcfTest {

	@Test
	public void test() {
		final InputStream resource = getClass().getResourceAsStream("/failing.vcf");
		try (MultipleVariantReader reader = new MultipleVariantReader(List.of(resource))) {
			while (reader.hasNext()) {
				final Variant variant = reader.nextMerged();
				System.out.println(variant);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
