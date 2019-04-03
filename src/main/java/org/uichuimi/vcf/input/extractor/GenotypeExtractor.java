package org.uichuimi.vcf.input.extractor;

import org.uichuimi.vcf.header.DataFormatLine;
import org.uichuimi.vcf.variant.MultiLevelInfo;
import org.uichuimi.vcf.variant.VariantContext;

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
	public void accept(VariantContext variant, MultiLevelInfo info, DataFormatLine headerLine, String value) {
		final String[] values = split(value);
		if (values == null) return;
		for (int i = 0; i < values.length; i++)
			info.getGenotype(i).set(headerLine.getId(), headerLine.parse(values[i]));
	}

	@Override
	public String extract(VariantContext variant, MultiLevelInfo info, DataFormatLine formatLine) {
		final List<String> objects = new ArrayList<>();
		for (int i = 0; i < variant.getInfo().getNumberOfGenotypes(); i++)
			objects.add(formatLine.format(info.getGenotype(i).get(formatLine.getId())));
		return toValueString(objects);
	}

}
