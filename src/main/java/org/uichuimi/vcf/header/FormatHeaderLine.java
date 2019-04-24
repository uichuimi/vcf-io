package org.uichuimi.vcf.header;

import java.util.LinkedHashMap;
import java.util.Map;

public class FormatHeaderLine extends DataFormatLine {

	public static final String FORMAT = "FORMAT";

	public FormatHeaderLine(Map<String, String> map) {
		super(FORMAT, map);
	}

	public FormatHeaderLine(String id, String number, String type, String description) {
		this(new LinkedHashMap<String, String>() {{
			put("ID", id);
			put("Number", number);
			put("Type", type);
			put("Description", description);
		}});
	}

}
