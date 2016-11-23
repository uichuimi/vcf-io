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

import utils.StringStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class Info {
    private static String[] keys = new String[0];
    private Object[] vals = new Object[0];

    public void set(String key, Object value) {
        int index = updateKeys(key);
        insertValue(value, index);
    }

    private void insertValue(Object value, int index) {
        if (vals.length < index + 1) resizeVals(index + 1);
        if (value.getClass().equals(String.class)) value = StringStore.getInstance((String) value);
        vals[index] = value;
    }

    private int updateKeys(String key) {
        int index = indexOf(key);
        if (index == -1) {
            resizeKeys();
            index = keys.length - 1;
            keys[index] = StringStore.getInstance(key);
        }
        return index;
    }

    private void resizeVals(int size) {
        final Object[] newVals = new Object[size];
        System.arraycopy(vals, 0, newVals, 0, vals.length);
        vals = newVals;
    }

    private void resizeKeys() {
        final String[] newKeys = new String[keys.length + 1];
        System.arraycopy(keys, 0, newKeys, 0, keys.length);
        keys = newKeys;
    }

    private int indexOf(String key) {
        for (int i = 0; i < keys.length; i++) if (keys[i].equals(key)) return i;
        return -1;
    }

    public Object get(String key) {
        for (int i = 0; i < keys.length; i++) if (keys[i].equals(key) && vals.length > i) return vals[i];
        return null;
    }

    public String getString(String key) {
        return (String) get(key);
    }

    public Number getNumber(String key) {
        return (Number) get(key);
    }

    public Boolean getBoolean(String key) {
        final Boolean val = (Boolean) get(key);
        return val == null ? false : val;
    }

    public boolean hasInfo(String key) {
        for (int i = 0; i < keys.length; i++) if (keys[i].equals(key)) return vals.length > i && vals[i] != null;
        return false;
    }

    public Object[] getArray(String key) {
        final Object value = get(key);
        if (ValueUtils.isArray(value)) return (Object[]) value;
        else return new Object[]{value};
    }


    @Override
    public String toString() {
        final List<String> infos = new ArrayList<>();
        for (int i = 0; i < vals.length; i++) {
            if (vals[i] != null) {
                if (vals[i].getClass() == Boolean.class) infos.add(keys[i]);
                else infos.add(keys[i] + "=" + ValueUtils.getString(vals[i]));
            }
        }
        Collections.sort(infos);
        return String.join(";", infos);
    }

}