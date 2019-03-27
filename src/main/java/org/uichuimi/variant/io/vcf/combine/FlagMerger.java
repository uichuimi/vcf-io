package org.uichuimi.variant.io.vcf.combine;

import org.uichuimi.variant.io.vcf.MultiLevelInfo;
import org.uichuimi.variant.io.vcf.SuperVariant;
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
	public void accept(SuperVariant target, MultiLevelInfo targetInfo, SuperVariant source, MultiLevelInfo sourceInfo, DataFormatLine formatLine) {
		if (sourceInfo.getInfo().hasInfo(formatLine.getId()))
			targetInfo.getInfo().set(formatLine.getId(), true);
	}
}