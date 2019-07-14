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

package org.uichuimi.vcf.header;

import java.util.Objects;

/**
 * A header line whose value is a simple string. A VCF header may contain more than one simple line
 * with the same key.
 */
public class SimpleHeaderLine implements HeaderLine<String> {

	private final String key;
	private final String value;

	public SimpleHeaderLine(String key, String value) {
		super();
		this.key = key;
		this.value = value;
	}

	@Override
	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SimpleHeaderLine that = (SimpleHeaderLine) o;
		return key.equals(that.key) &&
				value.equals(that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(key, value);
	}

	@Override
	public String toString() {
		return String.format("##%s=%s", key, value);
	}
}
