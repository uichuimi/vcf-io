package org.uichuimi.vcf.combine;

import org.uichuimi.vcf.header.DataFormatLine;
import org.uichuimi.vcf.variant.GenotypeIndex;
import org.uichuimi.vcf.variant.Info;
import org.uichuimi.vcf.variant.Variant;

import java.util.List;

/**
 * Merges data of Number=G.
 */
public class GenotypeMerger implements DataMerger {

	private static GenotypeMerger instance;

	private GenotypeMerger() {
	}

	public static GenotypeMerger getInstance() {
		if (instance == null) instance = new GenotypeMerger();
		return instance;
	}

	@Override
	public void accept(Variant target, Info targetInfo, Variant source, Info sourceInfo, DataFormatLine formatLine) {
		final List s = sourceInfo.get(formatLine.getId());
		if (s == null) return;  // Nothing to merge
		List t = getOrCreate(targetInfo, formatLine);
		while (t.size() < target.getNumberOfGenotypes()) t.add(null);

		for (int i = 0; i < source.getNumberOfGenotypes(); i++) {
			final Object value = s.get(i);
			if (value == null) continue;
			//  Reindex genotype
			final int j = GenotypeIndex.getJ(i);
			final int k = GenotypeIndex.getK(i);
			final String a = source.getAlleles().get(j);
			final String b = source.getAlleles().get(k);
			final int newj = target.getAlleles().indexOf(a);
			final int newk = target.getAlleles().indexOf(b);
			final int index = GenotypeIndex.get(newj, newk);
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