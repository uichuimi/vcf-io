package org.uichuimi.variant.io.vcf.combine;

import org.uichuimi.variant.io.vcf.MultiLevelInfo;
import org.uichuimi.variant.io.vcf.VariantContext;
import org.uichuimi.variant.io.vcf.header.DataFormatLine;

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