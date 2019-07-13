package org.uichuimi.vcf.lazy;

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
public class VariantInfo {

	private static final String INFO_SEPARATOR = ";";
	private static final String KEY_VALUE_SEPARATOR = "=";
	private static Map<String, Integer> keys = new TreeMap<>();
	private static final AtomicInteger nextIndex = new AtomicInteger();

	private final List<LazyProperty<?>> values = new ArrayList<>();
	private final VcfHeader header;
	private String raw;


	/**
	 * @param header
	 * 		header of the Vcf file
	 * @param raw
	 * 		a string containing the INFO field of a Vcf line
	 */
	public VariantInfo(VcfHeader header, String raw) {
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
		extratValues();
		final Integer index = keys.get(key);
		if (index == null) return null;
		if (values.size() <= index) return null;
		final LazyProperty<?> property = values.get(index);
		return property == null ? null : (T) property.getValue();
	}

	/**
	 * Stores a new property in this INFO field. If key already exists, value will be overwritten.
	 *
	 * @param key
	 * 		key of the property to store
	 * @param value
	 * 		value of the property
	 * @param <T>
	 * 		type of the property
	 */
	public synchronized <T> void set(String key, T value) {
		set(key, new ObjectProperty<>(value));
	}

	private void parseRaw() {
		final String[] data = raw.split(INFO_SEPARATOR);
		for (String datum : data) {
			final String[] element = datum.split(KEY_VALUE_SEPARATOR);
			if (element.length > 1) setLazy(element[0], element[1]);
			else set(element[0], true);
		}
	}

	/**
	 * Returns whether this info contains a property associated to key.
	 *
	 * @param key key of the property
	 * @return true if property exists, false otherwise
	 */
	public boolean contains(String key) {
		return get(key) != null;
	}

	public void forEach(BiConsumer<String, Object> consumer) {
		extratValues();
		keys.forEach((key, index) -> {
			if (values.size() > index) {
				final LazyProperty<?> property = values.get(index);
				if (property != null) consumer.accept(key, property.getValue());
			}
		});
	}

	private void extratValues() {
		if (raw != null) {
			parseRaw();
			raw = null;
		}
	}

	private void setLazy(String key, String value) {
		final InfoFunction function = header.getFunction(key);
		set(key, function.getProperty(value));

	}

	private <T> void set(String key, LazyProperty<T> property) {
		int index = updateKeys(key);
		insertProperty(index, property);
	}

	private <T> void insertProperty(int index, LazyProperty<T> property) {
		while (values.size() <= index) values.add(null);
		values.set(index, property);
	}

	private int updateKeys(String key) {
		return keys.computeIfAbsent(key, k -> nextIndex.getAndIncrement());
	}


}
