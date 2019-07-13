package org.uichuimi.vcf.combine;

import org.uichuimi.vcf.header.DataFormatLine;
import org.uichuimi.vcf.lazy.Variant;
import org.uichuimi.vcf.lazy.VariantInfo;

public interface DataMerger {

	/**
	 * Merges values of formatLine from sourceInfo into targetInfo. target and source variants are given
	 * to compute indexes for alleles and genotypes.
	 */
	void accept(Variant target, VariantInfo targetInfo, Variant source, VariantInfo sourceInfo, DataFormatLine formatLine);
}
