package org.uichuimi.variant.io.vcf.combine;

import org.uichuimi.variant.io.vcf.header.DataFormatLine;
import org.uichuimi.variant.io.vcf.variant.MultiLevelInfo;
import org.uichuimi.variant.io.vcf.variant.VariantContext;

/**
 * Merges data of Number=A and Number=R
 */
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
			final Object value = sourceInfo.getAllele(i).get(formatLine.getId());
			if (value == null) continue;
			final String allele = source.getAlleles().get(i);
			final int index = target.getAlleles().indexOf(allele);
			targetInfo.getAllele(index).set(formatLine.getId(), value);
		}
	}
}