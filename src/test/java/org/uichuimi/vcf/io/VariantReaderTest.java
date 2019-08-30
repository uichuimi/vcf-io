package org.uichuimi.vcf.io;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.uichuimi.vcf.variant.Coordinate;
import org.uichuimi.vcf.variant.Variant;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class VariantReaderTest {

	@Test
	public void nextCollected() {
		final InputStream resource = getClass().getResourceAsStream("/files/reader/readCollected.vcf");
		try (VariantReader reader = new VariantReader(resource)) {
			final Coordinate coordinate = new Coordinate("3", 193637313);
			final Collection<Variant> variants = reader.nextCollected(coordinate);
			Assertions.assertEquals(2, variants.size());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
