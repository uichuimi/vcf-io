package org.uichuimi.vcf.combine;

import org.uichuimi.vcf.header.DataFormatLine;
import org.uichuimi.vcf.variant.Info;
import org.uichuimi.vcf.variant.Variant;

import java.util.List;

/**
 * Merges data of Number=A
 */
public class AlternativeMerger implements DataMerger {

	private static AlternativeMerger instance;

	private AlternativeMerger() {
	}

	public static AlternativeMerger getInstance() {
		if (instance == null) instance = new AlternativeMerger();
		return instance;
	}

	@Override
	public void accept(Variant target, Info targetInfo, Variant source, Info sourceInfo, DataFormatLine formatLine) {
		final List s = sourceInfo.get(formatLine.getId(), List.class);
		if (s == null || s.isEmpty()) return;  // Nothing to merge
		List t = getOrCreate(targetInfo, formatLine);

		final int alternativeAlleles = target.getAlternatives().size();
		// target should contain all alleles
		while (t.size() < alternativeAlleles) t.add(null);

		for (int a = 0; a < source.getAlternatives().size(); a++) {
			final String allele = source.getAlternatives().get(a);
			// Since target should contain all alleles from source, index is always >= 0
			final int index = target.getAlternatives().indexOf(allele);
			final Object value = s.get(a);
			t.set(index, merge(t.get(index), value));
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