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

package vcf;

import utils.StringStore;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class SampleInfo {

	private final List<SampleEntry> entries = new ArrayList<>();
	/**
	 * A matrix containing FORMAT columns. To access a cell: <strong>content[sampleIndex.get(sample)].get(format)
	 * </strong>.
	 */
	private Variant variant;

	SampleInfo(Variant variant) {
		this.variant = variant;
	}

	public void setFormat(String sample, String key, String value) {
		final SampleEntry entry = getorCreateEntry(sample);
		entry.set(key, value);
	}

	private SampleEntry getorCreateEntry(String sample) {
		SampleEntry entry = getEntry(sample);
		if (entry == null) {
			entry = new SampleEntry(sample, variant.getAlleles().length);
			entries.add(entry);
		}
		return entry;
	}


	/**
	 * Get the value of the key FORMAT for sample. If sample or key do not
	 * exist, then return null.
	 *
	 * @param sample name of the sample: one of the vcf samples
	 * @param key    FORMAT id
	 * @return the value of key for the given sample or VariantSet.EMPTY_VALUE if not present
	 */
	public String getFormat(String sample, String key) {
		final SampleEntry entry = getEntry(sample);
		if (entry == null)
			return null;
		final Object value = entry.get(key);
		return value == null
				? null
				: ValueUtils.getString(value);
	}

	/**
	 * Get the value of the key FORMAT for sample. If sample or key do not exist, then the VariantSet.EMPTY_VALUE (.) is
	 * returned.
	 *
	 * @param sample name of the sample: one of the vcf samples
	 * @param key    FORMAT id
	 * @return the value of key for the given sample or VariantSet.EMPTY_VALUE if not present
	 */
	public Object getRichFormat(String sample, String key) {
		final SampleEntry entry = getEntry(sample);
		return entry == null ? null : entry.get(key);
	}

	@Override
	public String toString() {
		if (entries.isEmpty()) return "";
		final List<String> usedTags = getUsedTags();
		if (usedTags.isEmpty()) return "";
		final List<String> samples = variant.getVcfHeader().getSamples();
		final String FORMAT = String.join(":", usedTags);
		final StringBuilder builder = new StringBuilder("\t").append(FORMAT);
		for (String sample : samples) {
			final List<String> values = usedTags.stream()
					.map(key -> getFormat(sample, key))
					.collect(Collectors.toList());
			builder.append("\t").append(String.join(":", values));
		}
		return builder.toString();
	}

	private List<String> getUsedTags() {
		final Set<String> usedTags = new LinkedHashSet<>();
		for (SampleEntry entry : entries)
			usedTags.addAll(entry.map.keySet());
		return new ArrayList<>(usedTags);
	}

	/**
	 * Removes a sample info from variant.
	 *
	 * @param name sample name
	 * @deprecated it is not a good practise to remove samples from a variant.
	 * If you are removing to create a new Vcf, consider cloning the variant.
	 */
	@Deprecated
	public void removeSample(String name) {
		final SampleEntry entry = getEntry(name);
		if (name != null)
			entries.remove(entry);
	}

	/**
	 * Generates a Genotype by using the GT field associated to sample.
	 *
	 * @param sample sample
	 * @return the Genotype of this sample at this variant. UNCALLED for .
	 */
	public Genotype getGenotype(String sample) {
		final String gt = getFormat(sample, "GT");
		if (gt == null) return Genotype.UNCALLED;
		if (gt.equals(VariantSet.EMPTY_VALUE)) return Genotype.UNCALLED;
		final String[] alleles = gt.split("[/|]");
		if (alleles[0].equals("0") && alleles[1].equals("0"))
			return Genotype.WILD;
		if (alleles[0].equals(alleles[1]))
			return Genotype.HOMOZYGOUS;
		return Genotype.HETEROZYGOUS;
	}

	private SampleEntry getEntry(String sample) {
		for (SampleEntry entry : entries)
			if (entry.sample.equals(sample))
				return entry;
		return null;
	}

	private class SampleEntry {
		public Map<String, Object> map = new LinkedHashMap<>();
		public String sample;

		public SampleEntry(String sample, int length) {
			this.sample = sample;
		}

		public void set(String key, String value) {
			final String type = variant.getVcfHeader().hasComplexHeader("FORMAT", key)
					? variant.getVcfHeader().getComplexHeader("FORMAT", key).getValue("Type")
					: "String";
			map.put(StringStore.getInstance(key), ValueUtils.getValue(value, type));
		}

		public Object get(String key) {
			return map.get(key);
		}
	}
}
