package org.uichuimi.variant.io.vcf.header;

import org.uichuimi.variant.io.vcf.MultiLevelInfo;
import org.uichuimi.variant.io.vcf.VariantContext;
import org.uichuimi.variant.io.vcf.VariantSet;
import org.uichuimi.variant.io.vcf.combine.*;
import org.uichuimi.variant.io.vcf.input.extractor.DataExtractor;

import java.util.Map;
import java.util.function.Function;

public class DataFormatLine extends ComplexHeaderLine {


	private final String id;
	private final String type;
	private final String description;
	private final String number;
	private final Function<String, ?> parser;
	private final DataMerger merger;
	private final DataExtractor extractor;


	public DataFormatLine(String key, Map<String, String> map) {
		super(key, map);
		id = map.get("ID");
		type = map.get("Type");
		number = map.get("Number");
		description = map.get("Description");
		parser = getParser();
		extractor = DataExtractor.getInstance(number);
		merger = getMerger();

	}


	private Function<String, ?> getParser() {
		switch (type) {
			case "Float":
				return Float::valueOf;
			case "Integer":
				return Integer::valueOf;
			case "Flag":
				return s -> true;
			case "String":
			case "Character":
			default:
				return Function.identity();
		}
	}

	private DataMerger getMerger() {
		// special case
		if (id.equals("GT")) return GtMerger.getInstance();
		// a number
		try {
			final int n = Integer.valueOf(number);
			if (n == 0) return FlagMerger.getInstance();
			if (n > 0) return SimpleMerger.getInstance();
		} catch (NumberFormatException ignored) {
		}
		// not a number
		switch (number) {
			case "R":
			case "A":
				// Note, A should work, cause values for reference are null, so nothing is copied
				return AlleleMerger.getInstance();
			case "G":
				return GenotypeMerger.getInstance();
			case ".":
			default:
				return SimpleMerger.getInstance();
		}
	}

	public String getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public String getNumber() {
		return number;
	}

	public String getType() {
		return type;
	}

	public <T> T parse(String val) {
		return val.equals(VariantSet.EMPTY_VALUE) ? null : (T) parser.apply(val);
	}

	public void apply(VariantContext variant, MultiLevelInfo info, String value) {
		extractor.accept(variant, info, this, value);
	}

	public String extract(VariantContext variant, MultiLevelInfo info) {
		return extractor.extract(variant, info, this);
	}

	public void mergeInto(VariantContext target, MultiLevelInfo targetInfo, VariantContext source, MultiLevelInfo sourceInfo) {
		merger.accept(target, targetInfo, source, sourceInfo, this);

	}
}
