/*
 * Copyright 2020 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package internal.ri.base;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
public class SubHeaderLocation implements Comparable<SubHeaderLocation> {

    @NonNegative
    private int page;

    @NonNegative
    private int index;

    @NonNull
    public SubHeaderLocation next() {
        return new SubHeaderLocation(page, index + 1);
    }

    @Override
    public int compareTo(SubHeaderLocation that) {
        int result = Integer.compare(this.page, that.page);
        return result != 0 ? result : Integer.compare(this.index, that.index);
    }
}
