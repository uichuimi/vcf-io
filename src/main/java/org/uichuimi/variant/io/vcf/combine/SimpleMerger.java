package org.uichuimi.variant.io.vcf.combine;

import org.uichuimi.variant.io.vcf.MultiLevelInfo;
import org.uichuimi.variant.io.vcf.SuperVariant;
import org.uichuimi.variant.io.vcf.header.DataFormatLine;

public class SimpleMerger implements DataMerger {

	private static SimpleMerger instance;

	private SimpleMerger() {
	}

	public static SimpleMerger getInstance() {
		if (instance == null) instance = new SimpleMerger();
		return instance;
	}

	@Override
	public void accept(SuperVariant target, MultiLevelInfo targetInfo, SuperVariant source, MultiLevelInfo sourceInfo, DataFormatLine formatLine) {
		if (sourceInfo.getInfo().hasInfo(formatLine.getId()) && !targetInfo.getInfo().hasInfo(formatLine.getId()))
			targetInfo.getInfo().set(formatLine.getId(), sourceInfo.getInfo().get(formatLine.getId()));

	}
}