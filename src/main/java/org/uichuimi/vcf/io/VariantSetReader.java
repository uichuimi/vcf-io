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

package org.uichuimi.vcf.io;

import org.uichuimi.vcf.Variant;
import org.uichuimi.vcf.header.VcfHeader;
import org.uichuimi.vcf.variant.Coordinate;

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

	public VariantSetReader(InputStream is) {
		reader = new BufferedReader(new InputStreamReader(is));
		header = VariantSetFactory.readHeader(reader);
	}

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
			}
		}
	}

	/**
	 * This is the only intended method to be overwritten. VariantSetReader encapsulates the logic
	 * of opening, iterating and closing the file.
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

	/**
	 * Get, but do not remove, next variant in the buffer. Multiple call to this method should
	 * return the same value.
	 *
	 * @return the next variant in the buffer. If there are no more variants in the buffer, returns
	 * null.
	 */
	public Variant peek() {
		return hasNext() ? nextVariant : null;
	}

	/**
	 * Get the next variant which coordinate is equals to the coordinate passed as argument. If this
	 * reader does not contain a variant with this coordinate, null is returned and all variants
	 * with coordinate less than
	 * <em>coordinate</em> will be skipped. Next variant returned by {@link
	 * VariantSetReader#next()}
	 * will have coordinate greater than <em>coordinate</em>.
	 *
	 * @param coordinate coordinate of the next variant to return
	 * @return a variant matching coordinate or null
	 */
	public Variant next(Coordinate coordinate) {
		while (hasNext()) {
			final int compare = nextVariant.getCoordinate().compareTo(coordinate);
			if (compare == 0) {
				final Variant variant = nextVariant;
				nextVariant = null;
				return variant;
			} else if (compare > 0) {
				// Do not nullify nextVariant
				return null;
			} else {
				// Force fetch of next variant
				nextVariant = null;
			}

		}
		return null;
	}
}
