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

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Stores headers of Variant Call Format Version 4.2.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VcfHeader {

    private static final Pattern META_LINE = Pattern.compile("##([^=]+)=(.+)");
    private static final Pattern META_LINE_CONTENT = Pattern.compile("<(.*)>");
    private static final Pattern FIELDS_LINE = Pattern.compile("#CHROM(.*)");
    private static final Map<String, List<String>> REQUIRED_KEYS = new TreeMap<>();

    static {
        REQUIRED_KEYS.put("INFO", Arrays.asList("ID", "Number", "Type", "Description"));
        REQUIRED_KEYS.put("FORMAT", Arrays.asList("ID", "Number", "Type", "Description"));
        REQUIRED_KEYS.put("FILTER", Arrays.asList("ID", "Description"));
        REQUIRED_KEYS.put("ALT", Arrays.asList("ID", "Description"));
        REQUIRED_KEYS.put("contig", Collections.singletonList("ID"));
        REQUIRED_KEYS.put("SAMPLE", Collections.singletonList("ID"));
    }

    private final Map<String, List<Map<String, String>>> complexHeaders = new LinkedHashMap<>();
    private final Map<String, String> simpleHeaders = new LinkedHashMap<>();
    private final List<String> samples = new ArrayList<>();

    /**
     * By default, VcfHeaders have VCFv4.2 format
     */
    public VcfHeader() {
        addSimpleHeader("fileformat", "VCFv4.2");
    }

    /**
     * Adds a header line
     *
     * @param line
     * @deprecated use addSimpleHeader and addComplexHeader instead. Parsing of lines should be done out of
     * VcfHeader, as with VariantSetFactory.
     */
    @Deprecated
    public void addHeader(String line) {
        final Matcher metaLine = META_LINE.matcher(line);
        if (metaLine.matches()) addMetaLine(metaLine);
        else addFormatLine(line);
    }

    private void addMetaLine(Matcher metaLine) {
        final String key = metaLine.group(1);
        final String value = metaLine.group(2);
        final Matcher contentMatcher = META_LINE_CONTENT.matcher(value);
        if (contentMatcher.matches()) addComplexHeader(key, contentMatcher.group(1));
        else addSingleHeader(key, value);
    }

    private void addComplexHeader(String key, String value) {
        complexHeaders.putIfAbsent(key, new ArrayList<>());
        final List<Map<String, String>> headers = complexHeaders.get(key);
        final Map<String, String> map = MapGenerator.parse(value);
        if (!headerContainsId(key, headers)) {
            headers.add(map);
        }
    }

    private boolean headerContainsId(String key, List<Map<String, String>> headers) {
        for (Map<String, String> header : headers) if (header.get("ID").equals(key)) return true;
        return false;
    }

    private void addSingleHeader(String key, String value) {
        simpleHeaders.putIfAbsent(key, value);
    }

    private void addFormatLine(String line) {
        final Matcher matcher = FIELDS_LINE.matcher(line);
        if (matcher.matches()) {
            final String[] split = line.split("\t");
            int numberOfSamples = split.length - 9;
            if (numberOfSamples > 0) for (int i = 0; i < numberOfSamples; i++) samples.add(split[i + 9]);
//            if (numberOfSamples > 0) samples.addAll(Arrays.asList(split).subList(9, numberOfSamples));
        }
    }

    public Map<String, List<Map<String, String>>> getComplexHeaders() {
        return complexHeaders;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("##fileformat=").append(simpleHeaders.get("fileformat")).append(System.lineSeparator());
        appendSingleHeaders(builder);
        appendComplexHeaders(builder);
        appendFormatLine(builder);
        return builder.toString();
    }

    private void appendSingleHeaders(StringBuilder builder) {
        simpleHeaders.entrySet().stream()
                .filter(entry -> !entry.getKey().equals("fileformat"))
                .forEach(entry -> {
                    builder.append("##").append(entry.getKey()).append("=");
                    if (entry.getValue().contains(" ")) builder.append("\"").append(entry.getValue()).append("\"");
                    else builder.append(entry.getValue());
                    builder.append(System.lineSeparator());
                });
    }

    private void appendComplexHeaders(StringBuilder builder) {
        complexHeaders.forEach((type, headers) ->
                headers.forEach(map -> {
                    builder.append("##").append(type).append("=<");
                    final AtomicBoolean first = new AtomicBoolean(true);
                    if (REQUIRED_KEYS.containsKey(type))
                        REQUIRED_KEYS.get(type).forEach(requiredKey ->
                                appendHeaderKey(builder, first, toString(requiredKey, map.get(requiredKey))));
                    map.forEach((key, value) -> {
                        if (!REQUIRED_KEYS.containsKey(type) || !REQUIRED_KEYS.get(type).contains(key))
                            appendHeaderKey(builder, first, toString(key, value));
                    });
                    builder.append(">").append(System.lineSeparator());
                }));
    }

    private void appendHeaderKey(StringBuilder builder, AtomicBoolean first, String text) {
        if (first.compareAndSet(true, false)) builder.append(text);
        else builder.append(",").append(text);
    }

    private String toString(String key, String value) {
        final String v = (key.equals("Description")
                || value.contains(" ")
                || value.contains(",")) ? "\"" + value + "\""
                : value;
        return key + "=" + v;
    }

    private void appendFormatLine(StringBuilder builder) {
        builder.append("#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO");
        if (!samples.isEmpty()) {
            builder.append("\tFORMAT");
            samples.forEach(f -> builder.append("\t").append(f));
        }
//        builder.append(System.lineSeparator());
    }

    public List<String> getSamples() {
        return samples;
    }

    public List<String> getIdList(String type) {
        final List<Map<String, String>> list = complexHeaders.get(type);
        if (list == null) return Collections.emptyList();
        return list.stream().map(map -> map.get("ID")).collect(Collectors.toList());
    }

    public int indexOf(String sample) {
        return samples.indexOf(sample);
    }

    public Map<String, String> getSimpleHeaders() {
        return simpleHeaders;
    }


    /**
     * Adds a new complex header. If there is another header of the same type with the same ID, it is updated.
     *
     * @param type type of header. One of FILTER, INFO, FORMAT, contig, SAMPLE, PEDIGREE, ALT
     * @param map  type=value with the content of the header
     * @throws VcfException when header is not added due to malformed header: id est, required type no present or
     *                      inconsistency in values
     */
    public void addComplexHeader(String type, Map<String, String> map) {
        createTypeMap(type);
        checkRequiredKeys(type, map);
        final Map<String, String> map1 = getComplexHeader(type, map.get("ID"));
        if (map1 == null) {
            if (type.equals("FORMAT") && map.get("ID").equals("GT")) complexHeaders.get(type).add(0, map);
            else complexHeaders.get(type).add(map);
        } else {
            map.forEach(map1::put);
            Logger.getLogger(getClass().getName()).info("Updating " + type + " " + map.get("ID"));
        }
    }

    private void createTypeMap(String type) {
        if (!complexHeaders.containsKey(type)) complexHeaders.put(type, new ArrayList<>());
    }

    private void checkRequiredKeys(String type, Map<String, String> map) {
        if (REQUIRED_KEYS.containsKey(type))
            for (String key : REQUIRED_KEYS.get(type))
                if (!map.containsKey(key)) throw new VcfException("INFO headers must contain '" + key + "' key");
    }

    public void addSimpleHeader(String key, String value) {
        simpleHeaders.put(key, value);
    }

    public boolean hasComplexHeader(String type, String id) {
        if (complexHeaders.containsKey(type))
            for (Map map : complexHeaders.get(type))
                if (map.get("ID").equals(id)) return true;
        return false;
    }

    public boolean hasSimpleHeader(String key) {
        return simpleHeaders.containsKey(key);
    }

    public Map<String, String> getComplexHeader(String type, String id) {
        if (complexHeaders.containsKey(type))
            for (Map<String, String> map : complexHeaders.get(type))
                if (map.get("ID").equals(id)) return map;
        return null;
    }
}
