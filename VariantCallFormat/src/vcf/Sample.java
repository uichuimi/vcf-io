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

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Created by uichuimi on 24/05/16.
 */
public class Sample {

    private final VariantSet variantSet;
    private final String name;
    private Property<Status> status = new SimpleObjectProperty<>(Status.AFFECTED);

    public Sample(VariantSet variantSet, String name) {
        this.variantSet = variantSet;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Property<Status> statusProperty() {
        return status;
    }

    public VariantSet getVariantSet() {
        return variantSet;
    }

    public Status getStatus() {
        return status.getValue();
    }

    public enum Status {
        UNAFFECTED, AFFECTED, HOMOZYGOTE, HETEROZYGOTE
    }
}
