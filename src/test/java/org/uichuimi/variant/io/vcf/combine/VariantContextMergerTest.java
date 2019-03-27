package org.uichuimi.variant.io.vcf.combine;

import org.junit.jupiter.api.Test;
import org.uichuimi.variant.io.vcf.input.MultipleVariantContextReader;
import org.uichuimi.variant.io.vcf.io.VariantContextWriter;
import org.uichuimi.variant.io.vcf.variant.VariantContext;

import java.io.File;
import java.util.Arrays;

class VariantContextMergerTest {

	@Test
	public void testMerge() {
		final File A = new File("/media/pascual/Resources/uichuimi/AHO_ROS_GATK_GRCH38.g.vcf");
		final File B = new File("/media/pascual/Resources/uichuimi/NIV025.vcf.gz");
		try (MultipleVariantContextReader reader = MultipleVariantContextReader.getInstance(Arrays.asList(A, B))) {
			while (reader.hasNext()) {
				final VariantContext variant = reader.nextMerged();
				if (variant.getReferences().size() > 1 || variant.getAlternatives().size() > 1)
					System.out.println(VariantContextWriter.toVcf(variant));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}