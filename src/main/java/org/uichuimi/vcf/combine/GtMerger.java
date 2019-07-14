package org.uichuimi.vcf.combine;

import org.uichuimi.vcf.header.DataFormatLine;
import org.uichuimi.vcf.lazy.Info;
import org.uichuimi.vcf.lazy.Variant;
import org.uichuimi.vcf.variant.VcfConstants;

/**
 * Merges the unique format GT.
 */
public class GtMerger implements DataMerger {

	private static GtMerger instance;

	private GtMerger() {
	}

	public static GtMerger getInstance() {
		if (instance == null) instance = new GtMerger();
		return instance;
	}

	@Override
	public void accept(Variant target, Info targetInfo, Variant source, Info sourceInfo, DataFormatLine formatLine) {
		// key is expected to be GT
		final String key = formatLine.getId();
		final String gt = sourceInfo.get(key);
		if (gt == null) return;
		// if any of the alleles is ., dont merge
		if (gt.contains(VcfConstants.EMPTY_VALUE)) return;
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
		targetInfo.set(key, gt0 + sep + gt1);
	}
}