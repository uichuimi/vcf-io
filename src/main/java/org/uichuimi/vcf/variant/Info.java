package org.uichuimi.vcf.variant;

import org.uichuimi.vcf.header.VcfHeader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

/**
 * List of properties of a variant stored as a dictionary (key=value). This class is equivalent to
 * <em>INFO</em> field of a VCF line, or to each of the FORMAT fields as well.
 */
public class Info {

	private static final AtomicInteger NEXT_INDEX = new AtomicInteger();
	/**
	 * As variants usually share most of the INFO keys, instead of storing a map per variant, the
	 * keys are stored in a global map pointing to a values list. Thus every variant has a values
	 * list, but all share the keys.
	 */
	private static final Map<String, Integer> keys = new TreeMap<>();

	private final List<LazyProperty<?>> values = new ArrayList<>();
	private final VcfHeader header;
	private String raw;


	/**
	 * @param header
	 * 		header of the Vcf file
	 * @param raw
	 * 		a string containing the INFO field of a Vcf line
	 */
	public Info(VcfHeader header, String raw) {
		this.header = header;
		this.raw = raw;
	}

	/**
	 * Get the property assigned to key. If property is not available, gets null
	 *
	 * @param key
	 * 		key of the property to get
	 * @param <T>
	 * 		type of the property
	 * @return the property associated to key, or null if not present
	 */
	public <T> T get(String key) {
		extractValues();
		final Integer index = keys.get(key);
		if (index == null) return null;
		if (values.size() <= index) return null;
		final LazyProperty<?> property = values.get(index);
		return property == null ? null : (T) property.getValue();
	}

	/**
	 * Get the property assigned to key. If property is not available, gets null. This methods adds
	 * an extra security layer by checking the type and throwing a ClassCastException before
	 * returning.
	 *
	 * @param key
	 * 		key of the property to get
	 * @param <T>
	 * 		type of the property
	 * @return the property associated to key, or null if not present
	 * @throws ClassCastException
	 * 		if property is not of class type
	 */
	public <T> T get(String key, Class<T> type) {
		extractValues();
		final Integer index = keys.get(key);
		if (index == null) return null;
		if (values.size() <= index) return null;
		final LazyProperty<?> property = values.get(index);
		if (property == null) return null;
		if (!type.isInstance(property.getValue()))
			throw new ClassCastException(String.format("%s cannot be converted to %s for key %s", property.getValue().getClass(), type.getTypeName(), key));
		return (T) property.getValue();
	}

	/**
	 * Stores a new property in this INFO field. If key already exists, value will be overwritten.
	 * If value is null, then the property is removed.
	 *
	 * @param key
	 * 		key of the property to store
	 * @param value
	 * 		value of the property
	 * @param <T>
	 * 		type of the property
	 */
	public synchronized <T> void set(String key, T value) {
		set(key, value == null ? null : new ObjectProperty<>(value));
	}

	private void parseRaw() {
		if (raw.equals(VcfConstants.EMPTY_VALUE)) return;
		final String[] data = raw.split(VcfConstants.INFO_DELIMITER);
		for (String datum : data) {
			final String[] element = datum.split(VcfConstants.KEY_VALUE_DELIMITER);
			if (element.length > 1) setLazy(element[0], element[1]);
			else set(element[0], true);
		}
	}

	/**
	 * Returns whether this info contains a property associated to key.
	 *
	 * @param key
	 * 		key of the property
	 * @return true if property exists, false otherwise
	 */
	public boolean contains(String key) {
		return get(key) != null;
	}

	public void forEach(BiConsumer<String, Object> consumer) {
		extractValues();
		keys.forEach((key, index) -> {
			if (values.size() > index) {
				final LazyProperty<?> property = values.get(index);
				if (property != null) consumer.accept(key, property.getValue());
			}
		});
	}

	private void extractValues() {
		if (raw != null) {
			parseRaw();
			raw = null;
		}
	}

	private void setLazy(String key, String value) {
		final LazyProperty<?> property = header.getInfoHeader(key).getProperty(value);
		set(key, property);

	}

	<T> void set(String key, LazyProperty<T> property) {
		int index = updateKeys(key);
		insertProperty(index, property);
	}

	private <T> void insertProperty(int index, LazyProperty<T> property) {
		while (values.size() <= index) values.add(null);
		values.set(index, property);
	}

	private int updateKeys(String key) {
		return keys.computeIfAbsent(key, k -> NEXT_INDEX.getAndIncrement());
	}


}
