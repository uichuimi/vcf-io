package org.uichuimi.variant.io.vcf.combine;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.uichuimi.variant.io.vcf.variant.GenotypeIndex;

public class GenotpyeIndexTest {

	@Test
	public void test() {
		Assertions.assertEquals(0, GenotypeIndex.get(0,0));
		Assertions.assertEquals(1, GenotypeIndex.get(0, 1));
		Assertions.assertEquals(14, GenotypeIndex.get(4,4));
	}
}
