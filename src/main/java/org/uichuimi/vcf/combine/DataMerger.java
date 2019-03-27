package org.uichuimi.vcf.combine;

import org.uichuimi.vcf.header.DataFormatLine;
import org.uichuimi.vcf.variant.MultiLevelInfo;
import org.uichuimi.vcf.variant.VariantContext;

public interface DataMerger {

	/**
	 * Merges values of formatLine from sourceInfo into targetInfo. target and source variants are given
	 * to compute indexes for alleles and genotypes.
	 */
	void accept(VariantContext target, MultiLevelInfo targetInfo, VariantContext source, MultiLevelInfo sourceInfo, DataFormatLine formatLine);
}
