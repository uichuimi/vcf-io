package org.uichuimi.vcf.variant;

import org.jetbrains.annotations.NotNull;
import org.uichuimi.vcf.header.VcfHeader;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Representation of a line in a VCF file. A variant actually can store more than one genetic
 * variation, as far as they share the same coordinate.
 * Variants are mostly immutable,
 */
public class Variant {

	private VcfHeader header;
	private CoordinateProperty coordinate;
	private ListProperty<String> references;
	private ListProperty<String> alternatives;
	private ListProperty<String> identifiers;
	private ListProperty<String> filters;
	private DoubleProperty quality;
	private Info info;
	private LazyProperty<List<Info>> sampleInfo;
	private int genotypes = -1;


	/**
	 * Creates a variant
	 *
	 * @param header
	 * 		the header of the VCF file this variant belongs to
	 * @param line
	 * 		the raw line read from the file
	 */
	public Variant(VcfHeader header, String line) {
		this(header, line, Chromosome.Namespace.getDefault());
	}

	public Variant(VcfHeader header, String line, Chromosome.Namespace namespace) {
		this.header = header;
		final String[] data = line.split(VcfConstants.DELIMITER);
		coordinate = new CoordinateProperty(data[0], data[1], namespace);
		identifiers = new ListProperty<>(data[2], Function.identity());
		references = new ListProperty<>(data[3], Function.identity());
		alternatives = new ListProperty<>(data[4], Function.identity());
		quality = new DoubleProperty(data[5]);
		filters = new ListProperty<>(data[6], Function.identity());
		info = new Info(header, data[7]);
		sampleInfo = new SampleInfoProperty(header, data);
	}

	public Variant(VcfHeader header, Coordinate coordinate, List<String> references, List<String> alternatives) {
		this.header = header;
		this.coordinate = new CoordinateProperty(coordinate);
		this.references = new ListProperty<>(references);
		this.alternatives = new ListProperty<>(alternatives);
		identifiers = new ListProperty<>(new ArrayList<>());
		quality = new DoubleProperty(VcfConstants.EMPTY_VALUE);
		filters = new ListProperty<>(new ArrayList<>());
		info = new Info(header, VcfConstants.EMPTY_VALUE);
		sampleInfo = new SampleInfoProperty(header, new String[0]);
	}

	public Variant(Variant variant) {
		this(variant.getHeader(), variant.getCoordinate(), variant.getReferences(), variant.getAlternatives());
	}

	public VcfHeader getHeader() {
		return header;
	}

	public Coordinate getCoordinate() {
		return coordinate.getValue();
	}

	public List<String> getReferences() {
		return references.getValue();
	}

	public List<String> getAlternatives() {
		return alternatives.getValue();
	}

	/**
	 * Get a list of all alleles (references plus alternatives)
	 *
	 * @return all alleles of the variant
	 */
	@NotNull
	public List<String> getAlleles() {
		final List<String> alleles = new ArrayList<>(getReferences().size() + getAlternatives().size());
		alleles.addAll(getReferences());
		alleles.addAll(getAlternatives());
		return alleles;
	}

	public List<String> getIdentifiers() {
		return identifiers.getValue();
	}

	public List<String> getFilters() {
		return filters.getValue();
	}

	public Double getQuality() {
		return quality.getValue();
	}

	public void setQuality(Double quality) {
		this.quality = new DoubleProperty(quality);
	}

	public Info getInfo() {
		return info;
	}

	public List<Info> getSampleInfo() {
		return sampleInfo.getValue();
	}

	/**
	 * Get the Info associated to the sample in position. If position exceeds header.sample size,
	 * null is returned.
	 *
	 * @param position
	 * 		the index of sample in 'header.samples'
	 * @return the Info associate to the sample in position
	 */
	public Info getSampleInfo(int position) {
		final List<Info> samples = sampleInfo.getValue();
		while (samples.size() <= position)
			samples.add(new Info(header, VcfConstants.EMPTY_VALUE));
		return samples.get(position);
	}


	public int getNumberOfGenotypes() {
		if (genotypes < 0) genotypes = computeGenotypes(getAlleles().size());
		return genotypes;
	}

	private int computeGenotypes(int numberOfAlleles) {
		int numberOfGenotypes = 0;
		for (int i = 1; i <= numberOfAlleles; i++) numberOfGenotypes += i;
		return numberOfGenotypes;
	}

	public <T> T getInfo(String key) {
		return getInfo().get(key);
	}

	public <T> void setInfo(String key, T value) {
		getInfo().set(key, value);
	}

	public <T> T getFormat(String sample, String key) {
		final int index = getHeader().getSamples().indexOf(sample);
		return getSampleInfo(index).get(key);
	}

	public <T> void setFormat(String sample, String key, T value) {
		final int index = getHeader().getSamples().indexOf(sample);
		getSampleInfo(index).set(key, value);
	}

	@Override
	public String toString() {
		return String.format("Variant[%s, ref=%s, alt=%s, ids=%s]", getCoordinate(), getReferences(), getAlternatives(), getIdentifiers());
	}
}
