package org.uichuimi.variant.io.vcf.input.extractor;

import org.uichuimi.variant.io.vcf.variant.MultiLevelInfo;
import org.uichuimi.variant.io.vcf.variant.VariantContext;
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
	public void accept(VariantContext variant, MultiLevelInfo info, DataFormatLine headerLine, String value) {
		final Object val = headerLine.parse(value);
		if (val != null) info.getGlobal().set(headerLine.getId(), val);
	}

	@Override
	public String extract(VariantContext variant, MultiLevelInfo info, DataFormatLine formatLine) {
		return info.getGlobal().getString(formatLine.getId());
	}
}
