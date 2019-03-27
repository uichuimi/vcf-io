package org.uichuimi.variant.io.vcf.combine;

import org.junit.jupiter.api.Test;
import org.uichuimi.variant.io.vcf.VariantContext;
import org.uichuimi.variant.io.vcf.input.SuperMultipleVcfReader;
import org.uichuimi.variant.io.vcf.io.SuperVariantWriter;

import java.io.File;
import java.util.Arrays;

class SuperVariantMergerTest {

	@Test
	public void testMerge() {
		final File A = new File("/media/pascual/Resources/uichuimi/AHO_ROS_GATK_GRCH38.g.vcf");
		final File B = new File("/media/pascual/Resources/uichuimi/NIV025.vcf.gz");
		try (SuperMultipleVcfReader reader = SuperMultipleVcfReader.getInstance(Arrays.asList(A, B))) {
			while (reader.hasNext()) {
				final VariantContext variant = reader.nextMerged();
				if (variant.getReferences().size() > 1 || variant.getAlternatives().size() > 1)
					System.out.println(SuperVariantWriter.toString(variant));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}