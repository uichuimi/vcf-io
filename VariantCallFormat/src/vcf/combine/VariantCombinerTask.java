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

package vcf.combine;

import javafx.concurrent.Task;
import vcf.VariantSet;

import java.util.List;

/**
 * Created by uichuimi on 25/05/16.
 */
public class VariantCombinerTask extends Task<VariantSet> {

    private final VariantCombiner combiner;

    public VariantCombinerTask(List<Sample> samples, boolean delete) {
        this.combiner = new VariantCombiner(samples, delete);
        combiner.messageProperty().addListener((observable, oldValue, newValue) -> updateMessage(newValue));
        combiner.progressProperty().addListener((observable, oldValue, newValue) -> updateProgress(newValue, 1));
    }

    @Override
    protected VariantSet call() throws Exception {
        combiner.run();
        return combiner.getResult();
    }
}
