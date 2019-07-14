package org.uichuimi.vcf.header;

import org.uichuimi.vcf.combine.*;
import org.uichuimi.vcf.lazy.*;
import org.uichuimi.vcf.variant.VcfType;

import java.util.Map;

public class DataFormatLine extends ComplexHeaderLine {

	private final String id;
	private final VcfType type;
	private final String description;
	private final String number;
	private final DataMerger merger;
	private final boolean array;

	DataFormatLine(String key, Map<String, String> map) {
		super(key, map);
		id = map.get("ID");
		type = VcfType.valueOf(map.get("Type"));
		number = map.get("Number");
		description = map.get("Description");
		if (id == null || number == null || description == null)
			throw new IllegalArgumentException("Missing keys");
		this.array = !number.equals("0") && !number.equals("1");
		this.merger = getMerger();
	}

	/**
	 * Creates a non initialized property
	 *
	 * @param raw
	 * 		raw value of property
	 * @return a property with the proper type and number
	 */
	public LazyProperty<?> getProperty(String raw) {
		return array ? new ListProperty<>(raw, type.getValueExtractor()) : new ObjectProperty<>(raw, type.getValueExtractor());
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

	public VcfType getType() {
		return type;
	}

	public void mergeInto(Variant target, Info targetInfo, Variant source, Info sourceInfo) {
		merger.accept(target, targetInfo, source, sourceInfo, this);

	}
}
