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

package org.uichuimi.variant.io.vcf.combine;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import org.uichuimi.variant.io.vcf.Genotype;
import org.uichuimi.variant.io.vcf.Variant;
import org.uichuimi.variant.io.vcf.VariantSet;
import org.uichuimi.variant.io.vcf.VcfHeader;
import org.uichuimi.variant.io.vcf.io.MultipleVariantSetReader;
import org.uichuimi.variant.io.vcf.io.VariantSetFactory;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Combines a list of VCF files.
 * <p>
 * Created on 12/07/16.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VariantCombiner implements Runnable {

	private final Map<String, Genotype> genotypes;
	private final List<String> samples;
	private final Property<String> message = new SimpleObjectProperty<>();
	private final Property<Double> progress = new SimpleObjectProperty<>();
	private final List<File> files;
	private boolean delete;
	private VariantSet variantSet;


	/**
	 * Combines all the samples across all of the VCFs.
	 * <p>
	 * <code><pre>
	 * SELECT * from files
	 * WHERE chrom=chrom and pos=pos
	 * </pre></code>
	 *
	 * @param files list of file to combine
	 */
	public VariantCombiner(List<File> files) {
		this.files = files;
		this.samples = getAllSamples(files);
		this.delete = false;
		this.genotypes = null;
	}

	/**
	 * Combines the Vcf files and only keeps data for samples passed by samples.
	 *
	 * @param files   list of vcf files to combine
	 * @param samples list of samples to combine
	 */
	public VariantCombiner(List<File> files, List<String> samples) {
		this.files = files;
		this.samples = samples;
		this.delete = false;
		this.genotypes = null;
	}

	/**
	 * Combines the VCF files. Only store variants where the genotype of samples
	 * is valid according to the map passed by args. Only samples in genotypes
	 * are stored.
	 *
	 * @param files
	 * @param genotypes
	 */
	public VariantCombiner(List<File> files, Map<String, Genotype> genotypes) {
		this.files = files;
		this.samples = new LinkedList<>(genotypes.keySet());
		this.delete = true;
		this.genotypes = genotypes;
	}

	private List<String> getAllSamples(List<File> files) {
		return files.stream().map(VariantSetFactory::readHeader)
				.flatMap(header -> header.getSamples().stream())
				.distinct()
				.collect(Collectors.toList());
	}

	@Override
	public void run() {
		try (MultipleVariantSetReader reader = new MultipleVariantSetReader(files)) {
			final VcfHeader header = reader.getMergedHeader();
			this.variantSet = new VariantSet(header);
			while (reader.hasNext()) {
				final Variant variant = reader.nextMerged();
				if (!delete || valid(variant))
					variantSet.getVariants().add(variant);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean valid(Variant variant) {
		return genotypes.entrySet().stream()
				.allMatch(entry -> valid(variant, entry.getKey(), entry.getValue()));
	}

	public VariantSet getResult() {
		return variantSet;
	}

	private boolean valid(Variant variant, String name, Genotype expected) {
		final Genotype predicted = variant.getSampleInfo().getGenotype(name);
		switch (expected) {
			case AFFECTED:
				return predicted == Genotype.HOMOZYGOUS ||
						predicted == Genotype.HETEROZYGOUS;
			case ANY:
				return true;
			default:
				return predicted == expected;
		}
	}

	public Property<String> messageProperty() {
		return message;
	}

	public Property<Double> progressProperty() {
		return progress;
	}
}
