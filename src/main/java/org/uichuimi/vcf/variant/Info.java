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

package org.uichuimi.vcf.variant;


import java.util.*;
import java.util.function.BiConsumer;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class Info {


	private static String[] keys = {};
	private Object[] vals = {};

	public void set(String key, Object value) {
		int index = updateKeys(key);
		insertValue(value, index);
	}

	private void insertValue(Object value, int index) {
		if (vals.length < index + 1) resizeVals(index + 1);
		if (value!= null && value.equals(VariantSet.EMPTY_VALUE)) return;
		vals[index] = value;
	}

	/**
	 * Removes a value from this INFO.
	 *
	 * @param key the key of the INFO field to remove
	 */
	public void remove(String key) {
		for (int i = 0; i < keys.length; i++)
			if (keys[i].equals(key) && vals.length > i) vals[i] = null;
	}

	private int updateKeys(String key) {
		int index = indexOf(key);
		if (index == -1) {
			resizeKeys();
			index = keys.length - 1;
			keys[index] = key;
		}
		return index;
	}

	private void resizeVals(int size) {
		final Object[] newVals = new Object[size];
		System.arraycopy(vals, 0, newVals, 0, vals.length);
		vals = newVals;
	}

	private void resizeKeys() {
		final String[] newKeys = new String[keys.length + 1];
		System.arraycopy(keys, 0, newKeys, 0, keys.length);
		keys = newKeys;
	}

	private int indexOf(String key) {
		for (int i = 0; i < keys.length; i++) if (keys[i].equals(key)) return i;
		return -1;
	}

	/**
	 * Gets the value corresponding to this key as it is stored
	 *
	 * @param key key
	 * @return the value associated to key or null if not found
	 */
	public Object get(String key) {
		for (int i = 0; i < keys.length; i++)
			if (keys[i].equals(key) && vals.length > i) return vals[i];
		return null;
	}

	/**
	 * Gets the value corresponding to this key as String, calling toString
	 * if it is not a native String
	 *
	 * @param key key
	 * @return the value associated to key as a String or null if not found
	 */
	public String getString(String key) {
		final Object value = get(key);
		if (value == null) return null;
		if (String.class.isAssignableFrom(value.getClass()))
			return (String) value;
		if (Object[].class.isAssignableFrom(value.getClass()))
			return Arrays.toString((Object[]) value);
		return value.toString();
	}

	/**
	 * Gets the value corresponding to this key as Number.
	 *
	 * @param key key
	 * @return the value associated to key as a Number or null if not found
	 * @throws ClassCastException if not a number
	 */
	public Number getNumber(String key) {
		final Object value = get(key);
		if (value == null) return null;
		return (Number) value;
	}

	/**
	 * Gets the value corresponding to this key as Double.
	 *
	 * @param key key
	 * @return the value associated to key as a Double or null if not found
	 * @throws ClassCastException if not a Double
	 */
	public Double getDouble(String key) {
		final Object value = get(key);
		if (value == null) return null;
		return (Double) value;
	}

	/**
	 * Gets the value corresponding to this key as Boolean.
	 *
	 * @param key key
	 * @return the value associated to key as a Boolean or false if not found
	 * @throws ClassCastException if not a boolean
	 */
	public Boolean getBoolean(String key) {
		final Object value = get(key);
		if (value == null) return false;
		return (Boolean) value;
	}

	public boolean hasInfo(String key) {
		for (int i = 0; i < keys.length; i++)
			if (keys[i].equals(key)) return vals.length > i && vals[i] != null;
		return false;
	}

	/**
	 * Returns the value associated to key as an array. If value is an array,
	 * it is returned as is, else, it is returned a new array with a single
	 * element.
	 *
	 * @param key the key
	 * @return null if there is no value. The array stored in key, or a new
	 * array with a single element.
	 */
	public Object[] getArray(String key) {
		final Object value = get(key);
		if (value == null) return null;
		if (ValueUtils.isArray(value)) return (Object[]) value;
		else return new Object[]{value};
	}


	@Override
	public String toString() {
		final List<String> infos = new ArrayList<>();
		for (int i = 0; i < vals.length; i++) {
			if (vals[i] != null) {
				if (vals[i].getClass() == Boolean.class) infos.add(keys[i]);
				else infos.add(keys[i] + "=" + ValueUtils.getString(vals[i]));
			}
		}
		Collections.sort(infos);
		return String.join(";", infos);
	}

	public void forEach(BiConsumer<String, Object> action) {
		Objects.requireNonNull(action);
		for (int i = 0; i < vals.length; i++)
			if (vals[i] != null)
				action.accept(keys[i], vals[i]);
	}
}