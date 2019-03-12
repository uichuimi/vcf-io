package org.uichuimi.variant.io.vcf.combine;

import org.junit.jupiter.api.Test;
import org.uichuimi.variant.io.vcf.io.MultipleVcfReader;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VariantMergerTest {

	@Test
	public void test() {
		final List<InputStream> readers = Stream.of("/files/to-merge-1.vcf", "/files/to-merge-2.vcf",
				"/files/to-merge-3.vcf")
				.map(file -> getClass().getResourceAsStream(file))
				.collect(Collectors.toList());
		final MultipleVcfReader reader = new MultipleVcfReader(readers);

//		System.out.println(reader.getHeader());
//		while (reader.hasNext()) {
//			final Variant variant = reader.nextMerged();
//			System.out.println(variant);
//		}

	}
}
