package org.uichuimi.variant.io.vcf.header;

import java.util.Map;

public class InfoHeaderLine extends DataFormatLine {

	public static final String INFO = "INFO";

	public InfoHeaderLine(Map<String, String> map) {
		super(INFO, map);
	}
}
