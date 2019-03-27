package org.uichuimi.variant.io.vcf.header;

import java.util.Map;

public class FormatHeaderLine extends DataFormatLine {

	public static final String FORMAT = "FORMAT";

	public FormatHeaderLine(Map<String, String> map) {
		super(FORMAT, map);
	}
}
