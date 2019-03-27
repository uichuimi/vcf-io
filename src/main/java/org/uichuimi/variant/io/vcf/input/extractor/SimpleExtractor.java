package org.uichuimi.variant.io.vcf.input.extractor;

import org.uichuimi.variant.io.vcf.MultiLevelInfo;
import org.uichuimi.variant.io.vcf.SuperVariant;
import org.uichuimi.variant.io.vcf.header.DataFormatLine;

/**
 * Extracts info fields with Number=1.
 */
public class SimpleExtractor extends DataExtractor {

	private static SimpleExtractor instance;

	private SimpleExtractor() {
	}

	static SimpleExtractor getInstance() {
		if (instance == null) instance = new SimpleExtractor();
		return instance;
	}

	@Override
	public void accept(SuperVariant variant, MultiLevelInfo info, DataFormatLine headerLine, String value) {
		final Object val = headerLine.parse(value);
		if (val != null) info.getInfo().set(headerLine.getId(), val);
	}

	@Override
	public String extract(SuperVariant variant, MultiLevelInfo info, DataFormatLine formatLine) {
		return info.getInfo().getString(formatLine.getId());
	}
}
