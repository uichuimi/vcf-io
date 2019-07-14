package org.uichuimi.vcf.combine;

import org.uichuimi.vcf.header.DataFormatLine;
import org.uichuimi.vcf.lazy.Info;
import org.uichuimi.vcf.lazy.Variant;

public interface DataMerger {

	/**
	 * Merges values of formatLine from sourceInfo into targetInfo. target and source variants are given
	 * to compute indexes for alleles and genotypes.
	 */
	void accept(Variant target, Info targetInfo, Variant source, Info sourceInfo, DataFormatLine formatLine);
}
