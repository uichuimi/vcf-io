package org.uichuimi.vcf.combine;

import org.uichuimi.vcf.header.DataFormatLine;
import org.uichuimi.vcf.header.InfoHeaderLine;
import org.uichuimi.vcf.variant.Info;
import org.uichuimi.vcf.variant.Variant;

import java.util.ArrayList;
import java.util.Arrays;
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
		if (s == null) return;  // Nothing to merge
		List t = getOrCreate(targetInfo, formatLine);
		final int referenceAlleles = target.getReferences().size();
		final int alternativeAlleles = target.getAlternatives().size();
		// target should contain all alleles
		while (t.size() < referenceAlleles + alternativeAlleles) t.add(null);

		for (int r = 0; r < source.getReferences().size(); r++) {
			final String allele = source.getReferences().get(r);
			// Since target should contain all alleles from source, index is always >= 0
			final int index = target.getAlleles().indexOf(allele);
			t.set(index, merge(t.get(index), s.get(r)));
		}
		for (int a = 0; a < source.getAlternatives().size(); a++) {
			final String allele = source.getAlternatives().get(a);
			// Since target should contain all alleles from source, index is always >= 0
			final int index = target.getAlleles().indexOf(allele);
			final Object value = s.get(a);
			t.set(index, merge(t.get(index), value));
		}
	}

	public void accept(Variant target, Info targetInfo, Variant source, Info sourceInfo) {
		final List<InfoHeaderLine> lines = new ArrayList<>(target.getHeader().getInfoLines().values());
		final int alleles = target.getAlternatives().size() + target.getReferences().size();
		final Object[][] values = new Object[lines.size()][alleles];
		for (int t = 0; t < target.getAlleles().size(); t++) {
			final int s = source.getAlleles().indexOf(target.getAlleles().get(t));
			for (int l = 0; l < lines.size(); l++) {
				InfoHeaderLine line = lines.get(l);
				values[l][t] = sourceInfo.<List>get(line.getId()).get(s);
			}
		}
		for (int l = 0; l < lines.size(); l++)
			targetInfo.set(lines.get(l).getId(), Arrays.asList(values[l]));
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