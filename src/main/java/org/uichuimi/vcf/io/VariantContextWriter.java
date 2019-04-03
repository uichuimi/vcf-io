package org.uichuimi.vcf.io;

import org.uichuimi.vcf.header.VcfHeader;
import org.uichuimi.vcf.utils.FileUtils;
import org.uichuimi.vcf.variant.VariantContext;
import org.uichuimi.vcf.variant.VariantException;

import java.io.*;
import java.util.function.Consumer;

/**
 * writes VCF format to an output stream. As header must be the first thing to
 * write, a valid header must be provided via the <strong>setHeader()</strong>
 * method. If a call is made to <strong>write(Variant variant)</strong>, but no
 * call has been made to writeHeader(), then it is implicit call.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VariantContextWriter implements AutoCloseable {

	private final BufferedWriter writer;
	private VcfHeader vcfHeader;
	/**
	 * By using a function, we avoid having a flag (headerWritten) that will be true only the first time.
	 */
	private Consumer<VariantContext> consumer;

	/**
	 * Creates a new VariantSetWriter that writes into a File. Once created, a
	 * FileWriter is hold to file, so try to close this as soon as you finish
	 * using it.
	 *
	 * @param file file to write. It will be overwritten. If it does not exists,
	 *             it will be created. See {@link FileWriter}
	 * @throws IOException copied from {@link FileWriter}: if the file exists
	 *                     but is a directory rather than a regular file, does
	 *                     not exist but cannot be created, or cannot be opened
	 *                     for any other reason
	 */
	public VariantContextWriter(File file) throws IOException {
		this(FileUtils.getOutputStream(file));
	}

	/**
	 * Creates a new VariantSetWriter that writes into an output stream. An
	 * OutputStreamWriter is opened, so remember to close it when you finish
	 * using it or use a try-with-resources block.
	 *
	 * @param outputStream where to write
	 */
	public VariantContextWriter(OutputStream outputStream) {
		this.writer = new BufferedWriter(new OutputStreamWriter(outputStream));
		consumer = withHeader();
	}

	/**
	 * Get consumer for the first line. Writes header and variant.
	 */
	private Consumer<VariantContext> withHeader() {
		return variant -> {
			try {
				writeHeader();
				writeVariant(variant);
				consumer = withoutHeader();
			} catch (IOException e) {
				e.printStackTrace();
			}
		};
	}

	/**
	 * Get consumer for the rest of lines. Writes the variant.
	 */
	private Consumer<VariantContext> withoutHeader() {
		return variant -> {
			try {
				writeVariant(variant);
			} catch (IOException e) {
				e.printStackTrace();
			}
		};
	}

	public void setHeader(VcfHeader header) {
		vcfHeader = header;
	}

	/**
	 * Writes header into writer. Only first call to this method is effective.
	 *
	 * @throws IOException copied from {@link BufferedWriter} write method: if
	 *                     an I/O error occurs
	 */
	private void writeHeader() throws IOException {
		writer.write(vcfHeader.toString());
		writer.newLine();
	}

	private void writeVariant(VariantContext variant) throws IOException {
		writer.write(VcfFormatter.toVcf(variant));
		writer.newLine();
	}

	/**
	 * Writes a variant. If header has not been yet written, then a call to
	 * writeHeader is made. This operation changes the
	 *
	 * @param variant variant to write
	 */
	public void write(VariantContext variant) throws VariantException {
		if (variant.getHeader() != vcfHeader)
			throw new VariantException("Variant VcfHeader does not correspond to file VcfHeader");
		consumer.accept(variant);
	}

	@Override
	public void close() throws Exception {
		writer.flush();
		writer.close();
	}


}
