package org.uichuimi.vcf.input.extractor;

import org.uichuimi.vcf.header.DataFormatLine;
import org.uichuimi.vcf.variant.MultiLevelInfo;
import org.uichuimi.vcf.variant.VariantContext;
import org.uichuimi.vcf.variant.VariantSet;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class DataExtractor {
	static final String SEPARATOR = ",";


	public abstract void accept(VariantContext variant, MultiLevelInfo info, DataFormatLine headerLine, String value);

	public abstract String extract(VariantContext variant, MultiLevelInfo info, DataFormatLine formatLine);

		public static DataExtractor getInstance(String number) {
		try {
			final int n = Integer.valueOf(number);
			if (n == 0) return FlagExtractor.getInstance();
			if (n == 1) return SimpleExtractor.getInstance();
			return ArrayExtractor.getInstance();
		} catch (NumberFormatException ignored) {

		}
		switch (number) {
			case "A":
				return AlternativeExtractor.getInstance();
			case "R":
				return AlleleExtractor.getInstance();
			case "G":
				return GenotypeExtractor.getInstance();
			case ".":
				return ArrayExtractor.getInstance();
			default:
				return SimpleExtractor.getInstance();
		}
	}

	protected String[] split(String value) {
		final String[] values = value.split(SEPARATOR);
		// Tests if every value is a dot: . or .,.
		if (Arrays.stream(values).allMatch(s -> s.equals(VariantSet.EMPTY_VALUE))) return null;
		return values;
	}

	String toValueString(List<String> objects) {
		if (objects.stream().allMatch(obj -> Objects.isNull(obj) || VariantSet.EMPTY_VALUE.equals(obj)))
			return null;
		return objects.stream()
				.map(o -> o == null ? VariantSet.EMPTY_VALUE : o)
				.collect(Collectors.joining(SEPARATOR));
	}


}
