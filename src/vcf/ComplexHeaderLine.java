/*
 * Copyright (c) UICHUIMI 2017
 *
 * This file is part of VariantCallFormat.
 *
 * Poirot is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * VariantCallFormat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 */

package vcf;

import java.util.*;

public class ComplexHeaderLine extends HeaderLine {

    private static final Map<String, List<String>> REQUIRED_KEYS = new TreeMap<>();

    static {
        REQUIRED_KEYS.put("INFO", Arrays.asList("ID", "Number", "Type", "Description"));
        REQUIRED_KEYS.put("FORMAT", Arrays.asList("ID", "Number", "Type", "Description"));
        REQUIRED_KEYS.put("FILTER", Arrays.asList("ID", "Description"));
        REQUIRED_KEYS.put("ALT", Arrays.asList("ID", "Description"));
        REQUIRED_KEYS.put("contig", Collections.singletonList("ID"));
        REQUIRED_KEYS.put("SAMPLE", Collections.singletonList("ID"));
    }

    private final String key;
    private final LinkedHashMap<String, String> map = new LinkedHashMap<>();

    /**
     * @param key
     * @param map
     */
    public ComplexHeaderLine(String key, Map<String, String> map) {
        checkRequiredKeys(key, map);
        this.key = key;
        this.map.putAll(map);
    }

    public Map<String, String> getMap() {
        return map;
    }

    public String getKey() {
        return key;
    }

    public String getValue(String key) {
        return map.get(key);
    }

    private void checkRequiredKeys(String type, Map<String, String> map) throws VariantException {
        if (REQUIRED_KEYS.containsKey(type))
            for (String key : REQUIRED_KEYS.get(type))
                if (!map.containsKey(key))
                    throw new VariantException("INFO headers must contain '" + key + "' key");
    }

    @Override
    public String toString() {
        return String.format("##%s=%s", key, toString(map));
    }

    private String toString(LinkedHashMap<String, String> map) {
        final StringJoiner joiner = new StringJoiner(",", "<", ">");
        map.forEach((key, value) -> joiner.add(stringifyComplex(key, value)));
        return joiner.toString();
    }

    private String stringifyComplex(String key, String value) {
        final String v = !(value.startsWith("\"") && value.endsWith("\""))
                && ((key.equals("Description")
                || value.contains(" ")
                || value.contains(","))) ? "\"" + value + "\""
                : value;
        return key + "=" + v;

    }
}
