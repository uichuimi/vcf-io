package org.uichuimi.variant.io.vcf.combine;

import org.uichuimi.variant.io.vcf.MultiLevelInfo;
import org.uichuimi.variant.io.vcf.SuperVariant;
import org.uichuimi.variant.io.vcf.header.DataFormatLine;

public interface DataMerger {

	/**
	 * Merges values of formatLine from sourceInfo into targetInfo. target and source variants are given
	 * to compute indexes for alleles and genotypes.
	 */
	void accept(SuperVariant target, MultiLevelInfo targetInfo, SuperVariant source, MultiLevelInfo sourceInfo, DataFormatLine formatLine);
}
