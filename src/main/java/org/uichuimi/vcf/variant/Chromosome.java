package org.uichuimi.vcf.variant;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

/**
 * A reference sequence for human. Each chromosome has a different name in each namespace.
 */
public class Chromosome implements Comparable<Chromosome> {

	private static final String NA = "na";
	private static final List<Chromosome> chromosomes = new ArrayList<>(ChromosomeFactory.getChromosomeList());
	private static final Map<Chromosome.Namespace, Map<String, Chromosome>> index = new TreeMap<>();

	private static final Map<String, Chromosome> unknown = new HashMap<>();
	private static final List<Chromosome> unknownList = new ArrayList<>();

	static {
		index.put(Chromosome.Namespace.GENEBANK, indexBy(Chromosome::getGeneBank));
		index.put(Chromosome.Namespace.GRCH, indexBy(Chromosome::getName));
		index.put(Chromosome.Namespace.UCSC, indexBy(Chromosome::getUcsc));
		index.put(Chromosome.Namespace.REFSEQ, indexBy(Chromosome::getRefseq));
	}

	@NotNull
	private static Map<String, Chromosome> indexBy(Function<Chromosome, String> key) {
		final Map<String, Chromosome> map = new TreeMap<>();
		for (Chromosome chromosome : chromosomes) {
			final String k = key.apply(chromosome);
			if (k.equals(NA)) continue;
			map.put(k, chromosome);
		}
		return Collections.unmodifiableMap(map);
	}

	private final String name;
	private final String role;
	private final String molecule;
	private final String type;
	private final String geneBank;
	private final String relationship;
	private final String refseq;
	private final String assemblyUnit;
	private final long length;
	private final String ucsc;

	Chromosome(String name, String role, String molecule, String type, String geneBank, String relationship, String refseq, String assemblyUnit, long length, String ucsc) {
		this.name = name;
		this.role = role;
		this.molecule = molecule;
		this.type = type;
		this.geneBank = geneBank;
		this.relationship = relationship;
		this.refseq = refseq;
		this.assemblyUnit = assemblyUnit;
		this.length = length;
		this.ucsc = ucsc;
	}

	public String getName() {
		return name;
	}

	public String getName(Namespace namespace) {
		return namespace.getName(this);
	}

	public String getMolecule() {
		return molecule;
	}

	public String getRole() {
		return role;
	}

	public String getType() {
		return type;
	}

	public String getGeneBank() {
		return geneBank;
	}

	public String getRelationship() {
		return relationship;
	}

	String getRefseq() {
		return refseq;
	}

	public String getAssemblyUnit() {
		return assemblyUnit;
	}

	public long getLength() {
		return length;
	}

	public String getUcsc() {
		return ucsc;
	}

	public static Chromosome get(String name, Chromosome.Namespace namespace) {
		Chromosome chr = index.get(namespace).get(name);
		if (chr != null) return chr;
		// Try to find the chromosome into another namespace
		for (Namespace ns : Namespace.values()) {
			if (ns == namespace) continue;
			chr = index.get(ns).get(name);
			if (chr != null) return chr;
		}
		// Maybe it is an unknown chr
		chr = unknown.get(name);
		if (chr != null) return chr;
		// Create a synthetic chromosome
		chr = new Chromosome(name, NA, NA, NA, name, NA, name, NA, 0, name);
		unknown.put(name, chr);
		unknownList.add(chr);
		return chr;
	}

	public static Chromosome get(String name) {
		return index.get(Chromosome.Namespace.getDefault()).get(name);
	}

	@Override
	public int compareTo(@NotNull Chromosome that) {
		if (this == that) return 0;
		if (chromosomes.contains(this) && chromosomes.contains(that))
			return Integer.compare(chromosomes.indexOf(this), chromosomes.indexOf(that));
		else if (chromosomes.contains(this)) return -1;
		else if (chromosomes.contains(that)) return 1;
		else return Integer.compare(unknownList.indexOf(this), unknownList.indexOf(that));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Chromosome that = (Chromosome) o;
		return Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	public enum Namespace {
		GRCH {
			@Override
			public String getName(Chromosome chromosome) {
				return chromosome.getName();
			}
		}, UCSC {
			@Override
			public String getName(Chromosome chromosome) {
				return chromosome.getUcsc();
			}
		}, REFSEQ {
			@Override
			public String getName(Chromosome chromosome) {
				return chromosome.getRefseq();
			}
		}, GENEBANK {
			@Override
			public String getName(Chromosome chromosome) {
				return chromosome.getGeneBank();
			}
		};

		public abstract String getName(Chromosome chromosome);

		public static Namespace getDefault() {
			return GRCH;
		}
	}
}
