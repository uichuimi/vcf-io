package org.uichuimi.variant.io.vcf.io;

import org.uichuimi.variant.io.vcf.header.ComplexHeaderLine;
import org.uichuimi.variant.io.vcf.header.HeaderLine;
import org.uichuimi.variant.io.vcf.header.SimpleHeaderLine;
import org.uichuimi.variant.io.vcf.header.VcfHeader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class CustomMultipleVcfReader {

	private final Collection<VariantSetReader> readers;
	private final VcfHeader baseHeader;

	private final VcfHeader header = new VcfHeader();

	public CustomMultipleVcfReader(Collection<InputStream> inputStreams) {
		this.readers = new ArrayList<>(inputStreams.size());
		for (InputStream inputStream : inputStreams) readers.add(new CustomVariantSetReader(inputStream));
		baseHeader = VcfHeader.merge(readers.stream().map(VariantSetReader::header).collect(Collectors.toList()));
		cloneHeader();
	}

	private void cloneHeader() {
		for (HeaderLine line : baseHeader.getHeaderLines()) {
			if (line instanceof SimpleHeaderLine) header.getHeaderLines().add(line);
			else if (line instanceof ComplexHeaderLine) {
				final ComplexHeaderLine complexHeaderLine = (ComplexHeaderLine) line;
				final String key = complexHeaderLine.getKey();
				if (!"INFO".equals(key) && !"FORMAT".equals(key))
					header.getHeaderLines().add(line);
			}
		}
	}


}
