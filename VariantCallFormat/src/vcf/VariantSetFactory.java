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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Creates a VariantSet by reading a text file (usually a .vcf).
 * Created by uichuimi on 25/05/16.
 */
public class VariantSetFactory {

    private static final Pattern META_LINE = Pattern.compile("##([^=]+)=(.+)");
    private static final Pattern META_LINE_CONTENT = Pattern.compile("<(.*)>");
    private static final Pattern FIELDS_LINE = Pattern.compile("#CHROM(.*)");

    public static VariantSet createFromFile(File file) {
        final VariantSet variantSet = new VariantSet();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            readLines(reader, variantSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return variantSet;
    }

    private static void readLines(final BufferedReader reader, VariantSet variantSet) throws VariantException {
        reader.lines().forEach(line -> {
            if (line.startsWith("#")) addHeader(variantSet.getHeader(), line);
            else try {
                variantSet.getVariants().add(VariantFactory.createVariant(line, variantSet));
            } catch (VariantException e) {
                e.printStackTrace();
            }
        });
    }

    private static void addHeader(VcfHeader header, String line) {
        final Matcher metaLine = META_LINE.matcher(line);
        if (metaLine.matches()) addMetaLine(header, metaLine);
        else addFormatLine(header, line);
    }

    private static void addMetaLine(VcfHeader header, Matcher metaLine) {
        final String type = metaLine.group(1);
        final String value = metaLine.group(2);
        final Matcher contentMatcher = META_LINE_CONTENT.matcher(value);
        if (contentMatcher.matches()) addComplexHeader(header, type, contentMatcher.group(1));
        else header.addSimpleHeader(type, value);
    }

    private static void addComplexHeader(VcfHeader header, String type, String group) {
        final Map<String, String> map = MapGenerator.parse(group);
        header.addComplexHeader(type, map);
    }

    private static void addFormatLine(VcfHeader header, String line) {
        final Matcher matcher = FIELDS_LINE.matcher(line);
        if (matcher.matches()) {
            final String[] split = line.split("\t");
            int numberOfSamples = split.length - 9;
            if (numberOfSamples > 0)
                for (int i = 0; i < numberOfSamples; i++) header.getSamples().add(split[i + 9]);
//            if (numberOfSamples > 0) samples.addAll(Arrays.asList(split).subList(9, numberOfSamples));
        }
    }


    public static VcfHeader readHeader(File file) {
        final VcfHeader header = new VcfHeader();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.lines().filter(line -> line.startsWith("#")).forEach(line -> addHeader(header, line));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return header;
    }
}
