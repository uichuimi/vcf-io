package org.uichuimi.vcf.header;

import java.util.LinkedHashMap;
import java.util.Map;

public class InfoHeaderLine extends DataFormatLine {

	public static final String INFO = "INFO";

	public InfoHeaderLine(Map<String, String> map) {
		super(INFO, map);
	}

	public InfoHeaderLine(String id, String number, String type, String description) {
		this(new LinkedHashMap<String, String>() {{
			put("ID", id);
			put("Number", number);
			put("Type", type);
			put("Description", description);
		}});
	}
}
