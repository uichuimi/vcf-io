package org.uichuimi.vcf.io;

import org.uichuimi.vcf.header.VcfHeader;
import org.uichuimi.vcf.variant.Variant;

import java.util.Iterator;

public interface VariantInputReader extends AutoCloseable, Iterator<Variant>, Iterable<Variant> {

	VcfHeader getHeader();
}
