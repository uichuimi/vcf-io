package org.uichuimi.variant.io.vcf.combine;

import org.uichuimi.variant.io.vcf.header.DataFormatLine;
import org.uichuimi.variant.io.vcf.variant.GenotypeIndex;
import org.uichuimi.variant.io.vcf.variant.MultiLevelInfo;
import org.uichuimi.variant.io.vcf.variant.VariantContext;

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
	public void accept(VariantContext target, MultiLevelInfo targetInfo, VariantContext source, MultiLevelInfo sourceInfo, DataFormatLine formatLine) {
		for (int i = 0; i < source.getInfo().getNumberOfGenotypes(); i++) {
			final Object value = sourceInfo.getGenotype(i).get(formatLine.getId());
			if (value == null) continue;
			//  Reindex genotype
			final int j = GenotypeIndex.getJ(i);
			final int k = GenotypeIndex.getK(i);
			final String a = source.getAlleles().get(j);
			final String b = source.getAlleles().get(k);
			final int newj = target.getAlleles().indexOf(a);
			final int newk = target.getAlleles().indexOf(b);
			final int index = GenotypeIndex.get(newj, newk);
			targetInfo.getGenotype(index).set(formatLine.getId(), value);
		}
	}
}