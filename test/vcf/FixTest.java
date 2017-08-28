/*
 * Copyright (c) UICHUIMI 2017
 *
 * This file is part of VariantCallFormat.
 *
 * VariantCallFormat is free software:
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * VariantCallFormat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with VariantCallFormat.
 *
 * If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package vcf;

import org.junit.jupiter.api.Disabled;
import vcf.io.VariantSetFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Created by uichuimi on 29/09/16.
 */
public class FixTest {

	@Disabled
	public void testFix() {
		final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:**.vcf");
		final FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if (pathMatcher.matches(file)) fixFile(file.toFile());
				return FileVisitResult.CONTINUE;
			}
		};
		final Path root = new File("/media").toPath();
		try {
			Files.walkFileTree(root, visitor);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void fixFile(File file) {
		VariantSet variantSet = VariantSetFactory.createFromFile(file);
	}
}
