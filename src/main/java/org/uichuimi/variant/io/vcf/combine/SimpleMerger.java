package org.uichuimi.variant.io.vcf.combine;

import org.uichuimi.variant.io.vcf.header.DataFormatLine;
import org.uichuimi.variant.io.vcf.variant.MultiLevelInfo;
import org.uichuimi.variant.io.vcf.variant.VariantContext;

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
	public void accept(VariantContext target, MultiLevelInfo targetInfo, VariantContext source, MultiLevelInfo sourceInfo, DataFormatLine formatLine) {
		if (sourceInfo.getGlobal().hasInfo(formatLine.getId()) && !targetInfo.getGlobal().hasInfo(formatLine.getId()))
			targetInfo.getGlobal().set(formatLine.getId(), sourceInfo.getGlobal().get(formatLine.getId()));

	}
}