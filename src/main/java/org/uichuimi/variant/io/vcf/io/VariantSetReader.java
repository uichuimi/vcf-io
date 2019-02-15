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
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by uichuimi on 3/10/16.
 */
public class VariantSetReader implements AutoCloseable, Iterator<Variant> {


	protected final VcfHeader header;
	protected final BufferedReader reader;
	private Variant nextVariant;

	public VariantSetReader(File file) throws FileNotFoundException {
		header = VariantSetFactory.readHeader(file);
		reader = new BufferedReader(new FileReader(file));
	}

	public final Stream<Variant> variants() {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this,
				Spliterator.NONNULL | Spliterator.ORDERED), true);
	}

	@Override
	public final void close() throws IOException {
		reader.close();
	}

	public final VcfHeader header() {
		return header;
	}

	@Override
	public final boolean hasNext() {
		if (nextVariant != null) return true;
		else {
			try {
				String line = reader.readLine();
				while (line != null && line.startsWith("#"))
					line = reader.readLine();
				if (line == null) return false;
				nextVariant = createVariant(line);
				return true;
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			} catch (VariantException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * This is the only intended method to be overwritten. VariantSetReader
	 * encapsulates the logic of opening, iterating and closing the file.
	 *
	 * @param line each variant line found in file
	 * @return a variant
	 */
	protected Variant createVariant(String line) {
		return VariantFactory.createVariant(line, header);
	}

	@Override
	public final Variant next() {
		if (hasNext()) {
			final Variant variant = nextVariant;
			nextVariant = null;
			return variant;
		} else throw new NoSuchElementException();
	}
}
