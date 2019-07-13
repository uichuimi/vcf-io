package org.uichuimi.vcf.header;

import org.uichuimi.vcf.combine.*;
import org.uichuimi.vcf.lazy.Variant;
import org.uichuimi.vcf.lazy.VariantInfo;
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
		merger = getMerger();
	}

	private Function<? super Object, String> getFormatter() {
		switch (type) {
			case "Float":
				return f -> Float.isFinite((Float) f) ? DECIMAL.format(f) : VariantSet.EMPTY_VALUE;
			case "Flag":
				return s -> null;
			case "String":
			case "Character":
			case "Integer":
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
				// Normally if this function is called, it is because the flag is present, so it must be true
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
			Integer.valueOf(number);
			// If 0, it is a Flag, merge by value
			// If 1, it is a Simple value, merge by value
			// If >1, it is a List, merge by value
			return SimpleMerger.getInstance();
		} catch (NumberFormatException ignored) {
		}
		// not a number
		switch (number) {
			case "R":
				return AlleleMerger.getInstance();
			case "A":
				return AlternativeMerger.getInstance();
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
		return obj == null ? null : formatter.apply(obj);
	}

	public void mergeInto(Variant target, VariantInfo targetInfo, Variant source, VariantInfo sourceInfo) {
		merger.accept(target, targetInfo, source, sourceInfo, this);

	}
}
