/*
 * Copyright (c) UICHUIMI 2016
 *
 * This file is part of VariantCallFormat.
 *
 * VariantCallFormat is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * VariantCallFormat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package vcf;

import utils.OS;
import utils.StringStore;

/**
 * Stores a vcf. chrom, position, ref, alt, filter and format are Strings. position is an integer, qual a
 * double. Info is stored as a map of key==value. If value is null, key is treated as a flag.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class Variant implements Comparable<Variant> {


    private final SampleInfo sampleInfo;
    private final Info info;
    private VariantSet variantSet;
    private String chrom;
    private String ref;
    private String alt;
    private String filter;
    private int position;
    private double qual;
    private String id;
    private int chromIndex;

    /**
     * This constructor is intended to be used by VariantFactory.
     *
     * @param file
     * @param chrom      chromosome or contig
     * @param position   genomic position
     * @param id         usually an rs id
     * @param ref        reference allele sequence
     * @param alt        alternative allele sequence
     * @param qual       quality
     * @param filter     filter
     * @param info       vcf info
     * @param sampleInfo vcf sample information
     */
    Variant(VariantSet file, String chrom, int position, String id, String ref, String alt, double qual, String filter, Info info, SampleInfo sampleInfo) {
        this.chrom = StringStore.getInstance(chrom);
        this.position = position;
        this.id = StringStore.getInstance(id);
        this.ref = StringStore.getInstance(ref);
        this.alt = StringStore.getInstance(alt);
        this.qual = qual;
        this.filter = StringStore.getInstance(filter);
        this.sampleInfo = sampleInfo;
        this.info = info;
        this.variantSet = file;
        chromIndex = OS.getStandardChromosomes().indexOf(chrom);
    }

    public Variant(String chrom, int position, String ref, String alt) {
        this.chrom = StringStore.getInstance(chrom);
        this.position = position;
        this.ref = StringStore.getInstance(ref);
        this.alt = StringStore.getInstance(alt);
        sampleInfo = new SampleInfo(this);
        info = new Info();
        chromIndex = OS.getStandardChromosomes().indexOf(chrom);
    }

    /**
     * Gets the chromosome of the vcf.
     *
     * @return the chromosome of the vcf
     */
    public String getChrom() {
        return chrom;
    }

    /**
     * Gets the ID of the vcf.
     *
     * @return the ID of the vcf
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = StringStore.getInstance(id);
    }

    /**
     * Gets the REF value of the vcf.
     *
     * @return the ref value
     */
    public String getRef() {
        return ref;
    }

    /**
     * Gets the ALT value of the vcf.
     *
     * @return the alt value
     */
    public String getAlt() {
        return alt;
    }

    /**
     * Gets the position of the vcf.
     *
     * @return the position
     */
    public int getPosition() {
        return position;
    }

    /**
     * Gets the QUAL of the vcf.
     *
     * @return the quality
     */
    public double getQual() {
        return qual;
    }

    public void setQual(double qual) {
        this.qual = qual;
    }

    @Override
    public int compareTo(Variant variant) {
        // Variants with no standard chromosome goes to the end
        if (chromIndex != -1 && variant.chromIndex == -1) return -1;
        if (chromIndex == -1 && variant.chromIndex != -1) return 1;
        // Non-standard chromosomes are ordered alphabetically
        int compare = (chromIndex == -1)
                ? chrom.compareTo(variant.chrom)
                : Integer.compare(chromIndex, variant.chromIndex);
        if (compare != 0) return compare;
        return Integer.compare(position, variant.position);
    }

    public String getFilter() {
        return filter;
    }

    public VariantSet getVariantSet() {
        return variantSet;
    }

    public void setVariantSet(VariantSet variantSet) {
        this.variantSet = variantSet;
    }

    @Override
    public String toString() {
        return chrom +
                "\t" + position +
                "\t" + id +
                "\t" + ref +
                "\t" + alt +
                "\t" + qual +
                "\t" + filter +
                "\t" + info +
                sampleInfo;
    }

    public SampleInfo getSampleInfo() {
        return sampleInfo;
    }

    public Info getInfo() {
        return info;
    }

    public void setFilter(String filter) {
        this.filter = StringStore.getInstance(filter);
    }
}
