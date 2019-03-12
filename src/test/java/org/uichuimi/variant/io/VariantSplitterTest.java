package org.uichuimi.variant.io;

import org.junit.jupiter.api.Test;
import org.uichuimi.variant.io.vcf.Variant;
import org.uichuimi.variant.io.vcf.VariantSet;
import org.uichuimi.variant.io.vcf.io.VariantSetFactory;

import java.util.Collection;

public class VariantSplitterTest {

	@Test
	public void splitVariant() {
		final VariantSet variantSet = VariantSetFactory.create(getClass().getResourceAsStream("/files/test-split.vcf"));
		final VariantSplitter splitter = new VariantSplitter(variantSet.getHeader());
		for (Variant variant : variantSet.getVariants()) {
			final Collection<Variant> variants = splitter.split(variant);
			for (Variant v : variants) {
				System.out.println(v);
			}
		}
	}
}
