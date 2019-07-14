package org.uichuimi.vcf.combine;

import org.uichuimi.vcf.header.DataFormatLine;
import org.uichuimi.vcf.lazy.Info;
import org.uichuimi.vcf.lazy.Variant;

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
		final List t = targetInfo.get(formatLine.getId());
		final List s = sourceInfo.get(formatLine.getId());

		final int referenceAlleles = target.getReferences().size();
		final int alternativeAlleles = target.getAlternatives().size();
		// target should contain all alleles
		while (t.size() < referenceAlleles + alternativeAlleles) t.add(null);

		for (int r = 0; r < source.getReferences().size(); r++) {
			final String allele = source.getReferences().get(r);
			// Since target should contain all alleles from source, index is always >= 0
			final int index = target.getReferences().indexOf(allele);
			t.set(index, merge(t.get(index), s.get(r)));
		}
		for (int a = 0; a < source.getAlternatives().size(); a++) {
			final String allele = source.getAlternatives().get(a);
			// Since target should contain all alleles from source, index is always >= 0
			final int index = referenceAlleles + target.getAlternatives().indexOf(allele);
			final Object value = s.get(a);
			t.set(index, merge(t.get(index), value));
		}
	}

	private Object merge(Object target, Object source) {
		if (target == null) return source;
		if (source == null) return target;
		return source;
	}
}