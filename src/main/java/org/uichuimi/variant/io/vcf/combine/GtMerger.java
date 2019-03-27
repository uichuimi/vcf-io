package org.uichuimi.variant.io.vcf.combine;

import org.uichuimi.variant.io.vcf.MultiLevelInfo;
import org.uichuimi.variant.io.vcf.VariantContext;
import org.uichuimi.variant.io.vcf.VariantSet;
import org.uichuimi.variant.io.vcf.header.DataFormatLine;

public class GtMerger implements DataMerger {

	private static GtMerger instance;

	private GtMerger() {
	}

	public static GtMerger getInstance() {
		if (instance == null) instance = new GtMerger();
		return instance;
	}

	@Override
	public void accept(VariantContext target, MultiLevelInfo targetInfo, VariantContext source, MultiLevelInfo sourceInfo, DataFormatLine formatLine) {
		// id is expected to be GT
		final String gt = sourceInfo.getGlobal().getString(formatLine.getId());
		if (gt == null) return;
		// if any of the alleles is ., dont merge
		if (gt.contains(VariantSet.EMPTY_VALUE)) return;
		String sep;
		String[] gts;
		if (gt.contains("/")) {
			sep = "/";
			gts = gt.split("/");
		} else {
			sep = "|";  // This symbol has meaning in a regex and must be escaped
			gts = gt.split("\\|");
		}
		final String a = source.getAlleles().get(Integer.valueOf(gts[0]));
		final String b = source.getAlleles().get(Integer.valueOf(gts[1]));
		final int gt0 = target.getAlleles().indexOf(a);
		final int gt1 = target.getAlleles().indexOf(b);
		targetInfo.getGlobal().set(formatLine.getId(), gt0 + sep + gt1);
	}
}