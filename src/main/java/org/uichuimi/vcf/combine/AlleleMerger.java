package org.uichuimi.vcf.combine;

import org.uichuimi.vcf.header.DataFormatLine;
import org.uichuimi.vcf.variant.Info;
import org.uichuimi.vcf.variant.Variant;

import java.util.List;

/**
 * Merges data of Number=R
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
	public void accept(Variant target, Info targetInfo, Variant source, Info sourceInfo, DataFormatLine formatLine) {
		final List s = sourceInfo.get(formatLine.getId(), List.class);
		if (s == null || s.isEmpty()) return;  // Nothing to merge
		List t = getOrCreate(targetInfo, formatLine);
		final int referenceAlleles = target.getReferences().size();
		final int alternativeAlleles = target.getAlternatives().size();
		// target should contain all alleles
		while (t.size() < referenceAlleles + alternativeAlleles) t.add(null);
		final List<String> alleles = source.getAlleles();
		for (int a = 0; a < alleles.size(); a++) {
			final String allele = alleles.get(a);
			final int index = target.getAlleles().indexOf(allele);
			t.set(index, merge(t.get(index), s.get(a)));
		}
	}

	private List getOrCreate(Info targetInfo, DataFormatLine formatLine) {
		List t = targetInfo.get(formatLine.getId());
		if (t == null) {
			t = formatLine.getType().newList();
			targetInfo.set(formatLine.getId(), t);
		}
		return t;
	}

	private Object merge(Object target, Object source) {
		if (target == null) return source;
		if (source == null) return target;
		return source;
	}
}