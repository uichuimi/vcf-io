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

package utils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by uichuimi on 28/04/16.
 */
public class OS {

    private static List<String> standardChromosomes = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
            "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y");

    public static String asString(String separator, List<String> values) {
        if (values.isEmpty()) return "";
        final StringBuilder builder = new StringBuilder(values.get(0));
        for (int i = 1; i < values.size(); i++) builder.append(separator).append(values.get(i));
        return builder.toString();
    }

    public static List<String> getStandardChromosomes() {
        return standardChromosomes;
    }
}
