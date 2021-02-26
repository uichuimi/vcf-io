package org.uichuimi.vcf.io;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.uichuimi.vcf.TestUtils;

import java.io.ByteArrayOutputStream;

/**
 * Arrays are stored as comma separated values. When an array is read as null, or is emptied, the written value is the
 * empty string. This should be changed to the null value and, optionally, remove the tag from the info field.
 */
public class EmptyArrayTest {


	@Test
	public void testType() {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (VariantReader reader = new VariantReader(getClass().getResourceAsStream("/files/empty-array/input.vcf"));
		     VariantWriter writer = new VariantWriter(baos)) {
			writer.setHeader(reader.getHeader());
			reader.forEach(writer::write);
		} catch (Exception e) {
			Assertions.fail(e);
		}

		final String expected = TestUtils.readResource(getClass().getResource("/files/empty-array/output.vcf"));
		Assertions.assertEquals(expected, baos.toString());
	}
}
