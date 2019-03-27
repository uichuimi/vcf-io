package org.uichuimi.variant.io.vcf.input.extractor;

import org.uichuimi.variant.io.vcf.MultiLevelInfo;
import org.uichuimi.variant.io.vcf.SuperVariant;
import org.uichuimi.variant.io.vcf.header.DataFormatLine;

import java.util.ArrayList;
import java.util.List;

/**
 * Extracts info fields with Number=G
 */
public class GenotypeExtractor extends DataExtractor {

	private static GenotypeExtractor instance;

	private GenotypeExtractor() {
	}

	static GenotypeExtractor getInstance() {
		if (instance == null) instance = new GenotypeExtractor();
		return instance;
	}

	@Override
	public void accept(SuperVariant variant, MultiLevelInfo info, DataFormatLine headerLine, String value) {
		final String[] values = split(value);
		if (values == null) return;
		for (int i = 0; i < values.length; i++)
			info.getGenotypeInfo(i).set(headerLine.getId(), headerLine.parse(values[i]));
	}

	@Override
	public String extract(SuperVariant variant, MultiLevelInfo info, DataFormatLine formatLine) {
		final List<Object> objects = new ArrayList<>();
		for (int i = 0; i < variant.getNumberOfGenotypes(); i++)
			objects.add(info.getGenotypeInfo(i).get(formatLine.getId()));
		return toValueString(objects);
	}

}
