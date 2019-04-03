package org.uichuimi.vcf.input.extractor;

import org.uichuimi.vcf.header.DataFormatLine;
import org.uichuimi.vcf.variant.MultiLevelInfo;
import org.uichuimi.vcf.variant.VariantContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Extracts info fields with Number=A.
 */
public class AlternativeExtractor extends DataExtractor {

	private static AlternativeExtractor instance;

	private AlternativeExtractor() {
	}

	static AlternativeExtractor getInstance() {
		if (instance == null) instance = new AlternativeExtractor();
		return instance;
	}

	@Override
	public void accept(VariantContext variant, MultiLevelInfo info, DataFormatLine headerLine, String value) {
		final String[] values = split(value);
		if (values == null) return;
		for (int i = 0; i < variant.getAlternatives().size(); i++)
			info.getAlternativeAllele(i).set(headerLine.getId(), headerLine.parse(values[i]));
	}

	@Override
	public String extract(VariantContext variant, MultiLevelInfo info, DataFormatLine formatLine) {
		final List<String> objects = new ArrayList<>();
		for (int i = 0; i < variant.getAlternatives().size(); i++)
			objects.add(formatLine.format(info.getAlternativeAllele(i).get(formatLine.getId())));
		return toValueString(objects);
	}

}
