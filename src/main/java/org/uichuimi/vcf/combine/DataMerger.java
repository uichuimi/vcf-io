package org.uichuimi.vcf.combine;

import org.uichuimi.vcf.header.DataFormatLine;
import org.uichuimi.vcf.variant.Info;
import org.uichuimi.vcf.variant.Variant;

/**
 * Specifies how values are merged from a source info to a target info.
 */
public interface DataMerger {

	/**
	 * Merges values of formatLine from sourceInfo into targetInfo. target and source variants are
	 * given to compute indexes for alleles and genotypes.
	 *
	 * @param target
	 * 		variant where data will be put
	 * @param targetInfo
	 * 		info structure where data will be put
	 * @param source
	 * 		variant where data will be read
	 * @param sourceInfo
	 * 		info structure where data will be read
	 * @param formatLine
	 * 		header line to indicate which property to copy
	 */
	void accept(Variant target, Info targetInfo, Variant source, Info sourceInfo, DataFormatLine formatLine);
}
