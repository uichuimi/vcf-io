package org.uichuimi.vcf.combine;

import org.uichuimi.vcf.header.DataFormatLine;
import org.uichuimi.vcf.variant.MultiLevelInfo;
import org.uichuimi.vcf.variant.VariantContext;

/**
 * Merges data with Type=Flag and Number=0
 */
public class FlagMerger implements DataMerger {

	private static FlagMerger instance;

	private FlagMerger() {
	}

	public static FlagMerger getInstance() {
		if (instance == null) instance = new FlagMerger();
		return instance;
	}

	@Override
	public void accept(VariantContext target, MultiLevelInfo targetInfo, VariantContext source, MultiLevelInfo sourceInfo, DataFormatLine formatLine) {
		if (sourceInfo.getGlobal().hasInfo(formatLine.getId()))
			targetInfo.getGlobal().set(formatLine.getId(), true);
	}
}