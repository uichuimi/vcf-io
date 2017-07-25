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

import com.sun.istack.internal.NotNull;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by uichuimi on 28/04/16.
 */
public class OS {

    private final static File configPath;

    static {
        configPath = new File(getJarDir(OS.class), "config");
    }

    public static File getConfigPath() {
        return configPath;
    }

    /**
     * Compute the absolute file path to the jar file.
     * The framework is based on http://stackoverflow.com/a/12733172/1614775
     * But that gets it right for only one of the four cases.
     *
     * @param clazz A class residing in the required jar.
     * @return A File object for the directory in which the jar file resides.
     * During testing with NetBeans, the result is ./build/classes/,
     * which is the directory containing what will be in the jar.
     */
    private static File getJarDir(Class clazz) {

        final URL url1 = getClassUrl(clazz);
        final String extURL = toExternalUrl(clazz, url1);
        URL url = toUrl(extURL);
        if (url == null) url = url1;
        try {
            return new File(url.toURI());
        } catch (URISyntaxException ex) {
            return new File(url.getPath());
        }
    }

    private static URL toUrl(String extURL) {
        try {
            return new URL(extURL);
        } catch (MalformedURLException mux) {
            // leave url unchanged; probably does not happen
        }
        return null;
    }

    private static URL getClassUrl(Class aclass) {
        URL url;
        try {
            url = aclass.getProtectionDomain().getCodeSource().getLocation();
            // url is in one of two forms
            //        ./build/classes/   NetBeans test
            //        jardir/JarName.jar  froma jar
        } catch (SecurityException ex) {
            url = aclass.getResource(aclass.getSimpleName() + ".class");
            // url is in one of two forms, both ending "/com/physpics/tools/ui/PropNode.class"
            //          file:/U:/Fred/java/Tools/UI/build/classes
            //          jar:file:/U:/Fred/java/Tools/UI/dist/UI.jar!
        }
        return url;
    }

    @NotNull
    private static String toExternalUrl(Class aclass, URL url) {
        String extURL = url.toExternalForm();

        // prune for various cases
        if (extURL.endsWith(".jar"))   // from getCodeSource
            extURL = extURL.substring(0, extURL.lastIndexOf("/"));
        else {  // from getResource
            String suffix = "/" + (aclass.getName()).replace(".", "/") + ".class";
            extURL = extURL.replace(suffix, "");
            if (extURL.startsWith("jar:") && extURL.endsWith(".jar!"))
                extURL = extURL.substring(4, extURL.lastIndexOf("/"));
        }
        return extURL;
    }


}
