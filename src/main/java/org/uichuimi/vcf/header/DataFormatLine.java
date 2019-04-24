package org.uichuimi.vcf.header;

import org.uichuimi.vcf.combine.*;
import org.uichuimi.vcf.input.extractor.DataExtractor;
import org.uichuimi.vcf.variant.MultiLevelInfo;
import org.uichuimi.vcf.variant.VariantContext;
import org.uichuimi.vcf.variant.VariantSet;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;
import java.util.function.Function;

public class DataFormatLine extends ComplexHeaderLine {

	private static final NumberFormat DECIMAL = new DecimalFormat("#.#####");


	private final String id;
	private final String type;
	private final String description;
	private final String number;
	private final Function<String, ?> parser;
	private final Function<? super Object, String> formatter;
	private final DataMerger merger;
	private final DataExtractor extractor;


	public DataFormatLine(String key, Map<String, String> map) {
		super(key, map);
		id = map.get("ID");
		type = map.get("Type");
		number = map.get("Number");
		description = map.get("Description");
		if (id == null || type == null || number == null || description == null)
			throw new IllegalArgumentException("Missing keys");
		parser = getParser();
		formatter = getFormatter();
		extractor = DataExtractor.getInstance(number);
		merger = getMerger();
	}

	private Function<? super Object, String> getFormatter() {
		switch (type) {
			case "Float":
				return f -> Float.isFinite((Float) f) ? DECIMAL.format(f) : VariantSet.EMPTY_VALUE;
			case "Integer":
				return String::valueOf;
			case "Flag":
				return s -> null;
			case "String":
			case "Character":
			default:
				return String::valueOf;
		}
	}

	private Function<String, ?> getParser() {
		switch (type) {
			case "Float":
				return s1 -> {
					final float f = Float.parseFloat(s1);
					if (Float.isFinite(f)) return f;
					else return null;
				};
			case "Integer":
				return Integer::valueOf;
			case "Flag":
				// Normally is this function is called, it is because the flag is present, so it must be true
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

	public String format(Object obj) {
		return obj == null ? VariantSet.EMPTY_VALUE : formatter.apply(obj);
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
