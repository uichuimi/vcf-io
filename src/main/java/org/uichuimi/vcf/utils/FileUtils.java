package org.uichuimi.vcf.utils;

import java.io.*;
import java.util.zip.*;

/**
 * Utility class to obtain an {@link InputStream}, {@link BufferedReader} or {@link OutputStream}
 * from a file. The file can be compressed in zip of gzip format. The format is automatically
 * detected by its extension.
 */
public class FileUtils {

	/**
	 * Get a buffered reader from the file.
	 *
	 * @param file
	 * 		input file
	 * @return a buffered reader pointing at the beginning of the file
	 * @throws IOException
	 * 		if file is unreadable or not in zip format when extension is '.zip' or not in gzip format
	 * 		when extension is '.gz'.
	 */
	public static BufferedReader getBufferedReader(File file) throws IOException {
		return new BufferedReader(new InputStreamReader(getInputStream(file)));
	}

	/**
	 * Get an input stream from the file.
	 *
	 * @param file
	 * 		input file
	 * @return an input stream pointing at the beginning of the file
	 * @throws IOException
	 * 		if file is unreadable or not in zip format when extension is '.zip' or not in gzip format
	 * 		when extension is '.gz'.
	 */
	public static InputStream getInputStream(File file) throws IOException {
		if (file.getName().endsWith(".zip")) {
			final ZipFile zipFile = new ZipFile(file);
			final ZipEntry zipEntry = zipFile.entries().nextElement();
			return zipFile.getInputStream(zipEntry);
		} else if (file.getName().endsWith(".gz")) {
			return new GZIPInputStream(new FileInputStream(file));
		} else return new FileInputStream(file);
	}

	/**
	 * Get an output stream for the file. No parent dir is created, so at least the parent directory
	 * must exists. If the extension is '.zip' or '.gz' the file will be compressed.
	 *
	 * @param file
	 * 		output file
	 * @return an output stream ready to be written
	 * @throws IOException
	 * 		if file cannot be written, or is inaccessible.
	 */

	public static OutputStream getOutputStream(File file) throws IOException {
		if (file.getName().endsWith(".zip")) {
			return new ZipOutputStream(new FileOutputStream(file));
		} else if (file.getName().endsWith(".gz")) {
			return new GZIPOutputStream(new FileOutputStream(file));
		} else return new FileOutputStream(file);
	}
}
