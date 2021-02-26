package org.uichuimi.vcf;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

public class TestUtils {

	public static String readResource(URL resource) {
		try {
			return IOUtils.toString(resource, Charset.defaultCharset());
		} catch (IOException e) {
			Assertions.fail(e);
			// unreachable
			return null;
		}
	}
}
