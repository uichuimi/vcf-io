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

import org.uichuimi.variant.io.vcf.ComplexHeaderLine;
import org.uichuimi.variant.io.vcf.Variant;
import org.uichuimi.variant.io.vcf.VcfHeader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;

/**
 * By default, variants will only load chromosome, reference and alternative
 * Created by uichuimi on 20/12/16.
 */
public class CustomVariantSetReader extends VariantSetReader
		implements AutoCloseable, Iterator<Variant> {

	private final VcfHeader customReader;
	private boolean loadId = false;
	private boolean loadQual = false;
	private boolean loadFilter = false;

	/**
	 * Creates a VariantSet reader that will only read chrom, pos, id, ref,
	 * alt, qual and filter by default. No INFO, no FORMAT is loaded by default.
	 *
	 * @param file
	 * @throws FileNotFoundException
	 */
	public CustomVariantSetReader(File file) throws FileNotFoundException {
		super(file);
		customReader = VariantSetFactory.readHeader(file);
		customReader.getSamples().clear();
		customReader.getHeaderLines().removeAll(customReader.getComplexHeaders("INFO"));
		customReader.getHeaderLines().removeAll(customReader.getComplexHeaders("FORMAT"));
	}

	@Override
	protected Variant createVariant(String line) {
		return VariantFactory.createVariant(line, customReader, header, loadId, loadQual, loadFilter);
	}

	/**
	 * Generated VcfHeader with personalized load options
	 *
	 * @return
	 */
	public VcfHeader getCustomHeader() {
		return customReader;
	}

	/**
	 * Sets whether to load ID field or not.
	 *
	 * @param loadId if true, ID will be read. If false ID will be always null.
	 *               By default is true.
	 */
	public void setloadId(boolean loadId) {
		this.loadId = loadId;
	}

	/**
	 * Sets whether to load QUAL field or not.
	 *
	 * @param loadQual if true, QUAL will be read. If false QUAL will be always
	 *                 null. By default is true.
	 */
	public void setLoadQual(boolean loadQual) {
		this.loadQual = loadQual;
	}

	/**
	 * Sets whether to load FILTER field or not.
	 *
	 * @param loadFilter if true, FILTER will be read. If false FILTER will be
	 *                   always null. By default is true.
	 */
	public void setLoadFilter(boolean loadFilter) {
		this.loadFilter = loadFilter;
	}

	/**
	 * Add a sample to load its genotype
	 *
	 * @param sample a sample you want to load its genotype.
	 */
	public void addSample(String sample) {
		if (header.getSamples().contains(sample) && !customReader.getSamples().contains(sample))
			customReader.getSamples().add(sample);
	}

	/**
	 * Removes a sample so its genotype will not be loaded.
	 *
	 * @param sample the name of the sample to not be loaded
	 */
	public void removeSample(String sample) {
		customReader.getSamples().remove(sample);
	}

	/**
	 * Adds a FORMAT (a genotype info) to be loaded.
	 *
	 * @param format the name of the format to be loaded
	 */
	public void addFormat(String format) {
		addComplexHeader("FORMAT", format);
	}


	/**
	 * Removes a FORMAT (a genotype info) so it will not be loaded.
	 *
	 * @param format the name of the format to not be loaded
	 */
	public void removeFormat(String format) {
		removeComplexHeader("FORMAT", format);
	}

	/**
	 * Adds a INFO to be loaded.
	 *
	 * @param info the name of the info to be loaded
	 */
	public void addInfo(String info) {
		addComplexHeader("INFO", info);
	}

	/**
	 * Removes a INFO so it will not be loaded.
	 *
	 * @param info the name of the info to not be loaded
	 */
	public void removeInfo(String info) {
		removeComplexHeader("INFO", info);
	}

	/**
	 * Copies a complex header from disk file to memory VcfHeader
	 *
	 * @param key
	 * @param id
	 */
	private void addComplexHeader(String key, String id) {
		final ComplexHeaderLine complexHeader = header.getComplexHeader(key, id);
		if (complexHeader == null)
			return;
		customReader.getHeaderLines().add(complexHeader);
	}

	/**
	 * Removes a complex header from memory VcfHeader
	 *
	 * @param key
	 * @param id
	 */
	private void removeComplexHeader(String key, String id) {
		final ComplexHeaderLine complexHeader = header.getComplexHeader(key, id);
		if (complexHeader == null)
			return;
		customReader.getHeaderLines().remove(complexHeader);
	}
}
