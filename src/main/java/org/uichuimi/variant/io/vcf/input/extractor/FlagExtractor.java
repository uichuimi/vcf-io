package org.uichuimi.variant.io.vcf.input.extractor;

import org.uichuimi.variant.io.vcf.MultiLevelInfo;
import org.uichuimi.variant.io.vcf.SuperVariant;
import org.uichuimi.variant.io.vcf.header.DataFormatLine;

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
	public void accept(SuperVariant variant, MultiLevelInfo info, DataFormatLine headerLine, String value) {
		info.getInfo().set(headerLine.getId(), true);
	}

	@Override
	public String extract(SuperVariant variant, MultiLevelInfo info, DataFormatLine formatLine) {
		throw new UnsupportedOperationException("Flags do not have value in info field");
	}
}
