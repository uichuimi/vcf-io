package org.uichuimi.vcf.io;

import org.uichuimi.vcf.header.VcfHeader;
import org.uichuimi.vcf.variant.Variant;

public interface VariantOutput extends AutoCloseable {

	void setHeader(VcfHeader header);

	void write(Variant variant);
}
