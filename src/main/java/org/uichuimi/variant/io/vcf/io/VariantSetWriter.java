/*
 * Copyright (c) UICHUIMI 2017
 *
 * This file is part of VariantCallFormat.
 *
 * VariantCallFormat is free software:
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * VariantCallFormat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with VariantCallFormat.
 *
 * If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package org.uichuimi.variant.io.vcf.io;

import org.uichuimi.variant.io.vcf.Variant;
import org.uichuimi.variant.io.vcf.VariantException;
import org.uichuimi.variant.io.vcf.VcfHeader;

import java.io.*;

/**
 * writes VCF format to an output stream. As header must be the first thing to
 * write, a valid header must be provided via the <strong>setHeader()</strong>
 * method. If a call is made to <strong>write(Variant variant)</strong>, but no
 * call has been made to writeHeader(), then it is implicit call.
 * Created by uichuimi on 4/10/16.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VariantSetWriter implements AutoCloseable {

	private final BufferedWriter writer;
	private VcfHeader vcfHeader;
	private boolean headerWritten;


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
	public VariantSetWriter(File file) throws IOException {
		writer = new BufferedWriter(new FileWriter(file));
	}

	/**
	 * Creates a new VariantSetWriter that writes into an output stream. An
	 * OutputStreamWriter is opened, so remember to close it when you finish
	 * using it or use a try-with-resources block.
	 *
	 * @param outputStream where to write
	 */
	public VariantSetWriter(OutputStream outputStream) {
		this.writer = new BufferedWriter(new OutputStreamWriter(outputStream));
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
	public void writeHeader() throws IOException {
		if (!headerWritten) {
			writer.write(vcfHeader.toString());
			writer.newLine();
			writer.flush();
			headerWritten = true;
		}
	}

	/**
	 * Writes a variant. If header has not been yet written, then a call to
	 * writeHeader is made. This operation changes the
	 *
	 * @param variant variant to write
	 * @throws IOException copied from {@link BufferedWriter} write method: if
	 *                     an I/O error occurs
	 */
	public void write(Variant variant) throws IOException, VariantException {
		if (variant.getVcfHeader() != vcfHeader)
			throw new VariantException("Variant VcfHeader does not correspond to file VcfHeader");
		writeHeader();
		writer.write(variant.toString());
		writer.newLine();
		writer.flush();
	}

	@Override
	public void close() throws Exception {
		writer.flush();
		writer.close();
	}
}
