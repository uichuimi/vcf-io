package org.uichuimi.vcf.combine;

import org.uichuimi.vcf.header.DataFormatLine;
import org.uichuimi.vcf.variant.Info;
import org.uichuimi.vcf.variant.Variant;

/**
 * Merges data with Number=1,2,3,... or Number=.
 */
public class SimpleMerger implements DataMerger {

	private static SimpleMerger instance;

	private SimpleMerger() {
	}

	public static SimpleMerger getInstance() {
		if (instance == null) instance = new SimpleMerger();
		return instance;
	}

	@Override
	public void accept(Variant target, Info targetInfo, Variant source, Info sourceInfo, DataFormatLine formatLine) {
		final String key = formatLine.getId();
		final Object s = sourceInfo.get(key);
		final Object t = targetInfo.get(key);
		targetInfo.set(key, merge(s, t));
	}

	private Object merge(Object target, Object source) {
		if (target == null) return source;
		if (source == null) return target;
		return source;
	}
}