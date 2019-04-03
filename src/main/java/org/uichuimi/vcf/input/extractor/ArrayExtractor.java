package org.uichuimi.vcf.input.extractor;

import org.uichuimi.vcf.header.DataFormatLine;
import org.uichuimi.vcf.variant.MultiLevelInfo;
import org.uichuimi.vcf.variant.VariantContext;

public class ArrayExtractor extends DataExtractor {

	private static ArrayExtractor instance;

	private ArrayExtractor() {
	}

	public static ArrayExtractor getInstance() {
		if (instance == null) instance = new ArrayExtractor();
		return instance;
	}

	@Override
	public void accept(VariantContext variant, MultiLevelInfo info, DataFormatLine headerLine, String value) {
		final String[] values = split(value);
		if (values == null) return;
		final Object[] array = new Object[values.length];
		for (int i = 0; i < values.length; i++) array[i] = headerLine.parse(values[i]);
		info.getGlobal().set(headerLine.getId(), array);
	}

	@Override
	public String extract(VariantContext variant, MultiLevelInfo info, DataFormatLine formatLine) {
		final Object[] array = info.getGlobal().getArray(formatLine.getId());
		if (array == null) return null;
		final String[] strings = new String[array.length];
		for (int i = 0; i < array.length; i++)
			strings[i] = formatLine.format(array[i]);
		return String.join(SEPARATOR, strings);
	}


}
