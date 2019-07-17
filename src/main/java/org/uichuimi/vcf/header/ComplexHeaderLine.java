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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * An structured meta information line.
 */
public class ComplexHeaderLine implements HeaderLine<Map<String, String>> {

	private final String key;
	private final String id;
	private final LinkedHashMap<String, String> map = new LinkedHashMap<>();

	public ComplexHeaderLine(String key, Map<String, String> map) {
		this.key = key;
		this.id = map.get("ID");
		this.map.putAll(map);
	}

	@Override
	public Map<String, String> getValue() {
		return map;
	}

	public String getKey() {
		return key;
	}

	public String getId() {
		return id;
	}

	public String getValue(String key) {
		return map.get(key);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final ComplexHeaderLine that = (ComplexHeaderLine) o;
		if (!Objects.equals(key, that.key)) return false;
		// Check if both maps are the same, order does not matter
		if (!map.keySet().equals(that.map.keySet())) return false;
		for (String key : map.keySet())
			if (!Objects.equals(map.get(key), that.map.get(key))) return false;
		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(key, map);
	}

	@Override
	public String toString() {
		return String.format("##%s=%s", key, toString(map));
	}

	private String toString(LinkedHashMap<String, String> map) {
		final StringJoiner joiner = new StringJoiner(",", "<", ">");
		map.forEach((key, value) -> joiner.add(stringifyComplex(key, value)));
		return joiner.toString();
	}

	private String stringifyComplex(String key, String value) {
		final String v = !(value.startsWith("\"") && value.endsWith("\""))
				&& ((key.equals("Description")
				|| value.contains(" ")
				|| value.contains(","))) ? "\"" + value + "\""
				: value;
		return key + "=" + v;

	}
}
