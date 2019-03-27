package org.uichuimi.variant.io.vcf.input.extractor;

import org.uichuimi.variant.io.vcf.MultiLevelInfo;
import org.uichuimi.variant.io.vcf.SuperVariant;
import org.uichuimi.variant.io.vcf.header.DataFormatLine;

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
	public void accept(SuperVariant variant, MultiLevelInfo info, DataFormatLine headerLine, String value) {
		final String[] values = split(value);
		if (values == null) return;
		final int offset = variant.getReferences().size();
		for (int i = 0; i < variant.getAlternatives().size(); i++) {
			info.getAlleleInfo(offset + i).set(headerLine.getId(), headerLine.parse(values[i]));
		}
	}

	@Override
	public String extract(SuperVariant variant, MultiLevelInfo info, DataFormatLine formatLine) {
		final List<Object> objects = new ArrayList<>();
		final int offset = variant.getReferences().size();
		for (int i = 0; i < variant.getAlternatives().size(); i++)
			objects.add(info.getAlleleInfo(offset + i).get(formatLine.getId()));
		return toValueString(objects);
	}

}
