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

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import org.uichuimi.vcf.Variant;
import org.uichuimi.vcf.header.VcfHeader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Stores in memory a Vcf file data. Variant Call Format (VCF) Version 4.2.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VariantSet {

	public static final String EMPTY_VALUE = ".";
	private final ObservableSet<Variant> variants = FXCollections.observableSet(new TreeSet<>());
	private final VcfHeader header;

	private Map<String, Map<Integer, Variant>> index = new TreeMap<>();

	{
		variants.addListener((SetChangeListener<Variant>) c -> {
			if (c.wasAdded()) addToIndex(c.getElementAdded());
			else if (c.wasRemoved()) removeFromIndex(c.getElementRemoved());
		});
	}

	/**
	 * Creates a new VariantSet using the given header.
	 *
	 * @param header
	 */
	public VariantSet(VcfHeader header) {
		this.header = header;
	}

	/**
	 * Creates a new VariantSet with an empty header.
	 */
	public VariantSet() {
		header = new VcfHeader();
	}

	private void removeFromIndex(Variant variant) {
		index.get(variant.getChrom()).remove(variant.getPosition());
	}

	private void addToIndex(Variant variant) {
		index.putIfAbsent(variant.getChrom(), new TreeMap<>());
		final Map<Integer, Variant> variantMap = index.get(variant.getChrom());
		variantMap.put(variant.getPosition(), variant);
	}

	public VcfHeader getHeader() {
		return header;
	}

	/**
	 * Get the list of variants.
	 *
	 * @return the list of variants
	 */
	public ObservableSet<Variant> getVariants() {
		return variants;
	}

	/**
	 * Save current data to a file.
	 *
	 * @param file target file
	 */
	public void save(File file) {
		save(file, variants);
	}

	/**
	 * Save list of variants passed by args, using this VCFFile for headers into the file.
	 *
	 * @param file     target file
	 * @param variants list of variants
	 */
	public void save(File file, Set<Variant> variants) {
		if (file.exists() && !file.delete())
			System.err.println("No access on " + file);
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			writer.write(header.toString());
			writer.newLine();
			for (Variant variant : variants) {
				writer.write(variant.toString());
				writer.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addOrUpdate(Variant variant) {
		final Variant find = findVariant(variant.getChrom(), variant.getPosition());
		if (find != null) update(find, variant);
		else cloneAndAdd(variant);
	}

	public Variant findVariant(String chrom, int position) {
		return index.containsKey(chrom) ? index.get(chrom).getOrDefault(position, null) : null;
	}

	private void update(Variant target, Variant source) {
		copyId(target, source);
		copySampleInfo(target, source);
		copyInfo(target, source);
	}

	private void copyId(Variant target, Variant source) {
		if (target.getId().equals(EMPTY_VALUE) && !source.getId().equals(EMPTY_VALUE))
			target.setId(source.getId());
	}

	private void copySampleInfo(Variant target, Variant source) {
		final List<String> formatKeys = source.getVcfHeader().getIdList("FORMAT");
		source.getVcfHeader().getSamples().forEach(sample ->
				formatKeys.forEach(key -> target.getSampleInfo().setFormat(sample, key, source.getSampleInfo().getFormat(sample, key))));
	}

	private void cloneAndAdd(Variant variant) {
		final Variant clone = clone(variant);
		variants.add(clone);
	}

	/**
	 * Creates a copy of variant. The copy will have the same chrom, pos, qual, ref, alt, filter and info, but not
	 * sample(FORMAT) info. new variant will have this as VariantSet.
	 *
	 * @param source
	 * @return
	 */
	private Variant clone(Variant source) {
		final Variant target = new Variant(source.getChrom(), source.getPosition(), source.getRef(), source.getAlt(), getHeader());
		target.setQual(source.getQual());
		target.setId(source.getId());
		target.setFilter(source.getFilter());
		copyInfo(target, source);
		copyFormat(target, source);
		return target;
	}

	private void copyInfo(Variant target, Variant source) {
		source.getVcfHeader().getIdList("INFO").forEach(key -> {
			if (source.getInfo().hasInfo(key))
				target.getInfo().set(key, source.getInfo().get(key));
		});
	}

	private void copyFormat(Variant target, Variant source) {
		final List<String> formatKeys = source.getVcfHeader().getIdList("FORMAT");
		source.getVcfHeader().getSamples().forEach(sample ->
				formatKeys.forEach(key ->
						target.getSampleInfo().setFormat(sample, key,
								source.getSampleInfo().getFormat(sample, key))));
	}


	/**
	 * Completely removes sample info from VariantSet. Sample info is deleted from every Variant in VariantSet, and
	 * sample name is removed from VariantSetHeader.
	 *
	 * @param name sample name
	 */
	public void removeSample(String name) {
		header.getSamples().remove(name);
		variants.forEach(variant -> variant.getSampleInfo().removeSample(name));
	}
}
