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

import java.util.*;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class Info {

    private final Map<String, Object> values = new HashMap<>();

    public void set(String key, Object value) {
        key = StringStore.getInstance(key);
        if (value.getClass().equals(String.class)) value = StringStore.getInstance((String) value);
        values.put(key, value);
    }

    public Object get(String key) {
        return values.get(key);
    }

    public String getString(String key) {
        return (String) get(key);
    }

    public Number getNumber(String key) {
        return (Number) get(key);
    }

    public Boolean getBoolean(String key) {
        return (boolean) get(key);
    }

    public boolean hasInfo(String key) {
        return values.containsKey(key);
    }


    @Override
    public String toString() {
        final List<String> infos = new ArrayList<>();
        values.forEach((key, value) -> {
            if (value.getClass().equals(Boolean.class)) infos.add(key);
            else infos.add(key + "=" + value.toString());
        });
        Collections.sort(infos);
        return OS.asString(";", infos);
    }

}