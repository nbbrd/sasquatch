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
package _test;

import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;
import sasquatch.SasForwardCursor;
import sasquatch.SasMetaData;
import sasquatch.SasScrollableCursor;
import sasquatch.SasSplittableCursor;
import sasquatch.util.SasCursors;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value(staticConstructor = "of")
public class SasArray {

    @lombok.Getter
    @lombok.NonNull
    private SasMetaData metaData;

    @lombok.NonNull
    private List<Object[]> rows;

    @NonNull
    public SasForwardCursor readForward() {
        return SasCursors.forwardOf(metaData, rows);
    }

    @NonNull
    public SasScrollableCursor readScrollable() {
        return SasCursors.scrollableOf(metaData, rows);
    }

    @NonNull
    public SasSplittableCursor readSplittable() {
        return SasCursors.splittableOf(metaData, rows);
    }
}
