package org.uichuimi.variant.io.vcf.combine;

import org.uichuimi.variant.io.vcf.MultiLevelInfo;
import org.uichuimi.variant.io.vcf.VariantContext;
import org.uichuimi.variant.io.vcf.header.DataFormatLine;

public class AlleleMerger implements DataMerger {

	private static AlleleMerger instance;

	private AlleleMerger() {
	}

	public static AlleleMerger getInstance() {
		if (instance == null) instance = new AlleleMerger();
		return instance;
	}

	@Override
	public void accept(VariantContext target, MultiLevelInfo targetInfo, VariantContext source, MultiLevelInfo sourceInfo, DataFormatLine formatLine) {
		for (int i = 0; i < sourceInfo.getNumberOfAlleles(); i++) {
			final Object value = sourceInfo.getAlleleInfo(i).get(formatLine.getId());
			if (value == null) continue;
			final String allele = source.getAlleles().get(i);
			final int index = target.getAlleles().indexOf(allele);
			targetInfo.getAlleleInfo(index).set(formatLine.getId(), value);
		}
	}
}