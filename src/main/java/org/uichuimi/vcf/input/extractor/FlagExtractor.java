package org.uichuimi.vcf.input.extractor;

import org.uichuimi.vcf.header.DataFormatLine;
import org.uichuimi.vcf.variant.MultiLevelInfo;
import org.uichuimi.vcf.variant.VariantContext;

/**
 * Extracts info fields with Number=1.
 */
public class FlagExtractor extends DataExtractor {

	private static FlagExtractor instance;

	private FlagExtractor(){}

	static FlagExtractor getInstance() {
		if (instance == null) instance = new FlagExtractor();
		return instance;
	}

	@Override
	public void accept(VariantContext variant, MultiLevelInfo info, DataFormatLine headerLine, String value) {
		info.getGlobal().set(headerLine.getId(), true);
	}

	@Override
	public String extract(VariantContext variant, MultiLevelInfo info, DataFormatLine formatLine) {
		throw new UnsupportedOperationException("Flags do not have value in info field");
	}
}
