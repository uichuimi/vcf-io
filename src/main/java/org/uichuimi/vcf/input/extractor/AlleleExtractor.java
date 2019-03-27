package org.uichuimi.vcf.input.extractor;

import org.uichuimi.vcf.header.DataFormatLine;
import org.uichuimi.vcf.variant.MultiLevelInfo;
import org.uichuimi.vcf.variant.VariantContext;
import org.uichuimi.vcf.variant.VariantSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Extracts info fields with Number=R.
 */
public class AlleleExtractor extends DataExtractor {

	private static AlleleExtractor instance;

	private AlleleExtractor() {
	}

	@Override
	public void accept(VariantContext variant, MultiLevelInfo info, DataFormatLine headerLine, String value) {
		final String[] values = value.split(SEPARATOR);
		final boolean allDots = Arrays.stream(values).allMatch(s -> s.equals(VariantSet.EMPTY_VALUE));
		if (allDots) return;
		final String id = headerLine.getId();
		for (int i = 0; i < variant.getReferences().size() + variant.getAlternatives().size(); i++) {
			info.getAllele(i).set(id, headerLine.parse(values[i]));
		}
	}

	public String extract(VariantContext variant, MultiLevelInfo info, DataFormatLine formatLine) {
		final List<Object> objects = new ArrayList<>();
		for (int i = 0; i < variant.getInfo().getNumberOfAlleles(); i++)
			objects.add(info.getAllele(i).get(formatLine.getId()));
		return toValueString(objects);
	}

	static AlleleExtractor getInstance() {
		if (instance == null) instance = new AlleleExtractor();
		return instance;
	}
}
