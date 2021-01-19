package org.uichuimi.vcf.variant;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Genotype {

    final List<String> genotypes;
    final String separator;

    public Genotype(@NotNull String value) {
        if (value.equals(".")) {
            separator = null;
            genotypes = Collections.emptyList();
        } else if (value.contains("/")) {
            separator = "/";
            genotypes = Arrays.asList(value.split(separator));
        } else if (value.contains("|")) {
            separator = "\\|";
            genotypes = Arrays.asList(value.split("\\|"));
        } else throw new IllegalArgumentException("separator for genotypes is not valid " + value);
    }

    public boolean isPhased() {
        return separator != null && separator.equals("|");
    }

    public boolean hasGenotypes() {
        return !genotypes.isEmpty();
    }

    public int size() {
        return genotypes.size();
    }

    public String genotype(int pos) {
        return genotypes.get(pos);
    }
}
