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
package sasquatch.desktop;

import sasquatch.SasMetaData;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
class SasBasicFileModel {

    public static final SasBasicFileModel EMPTY = new SasBasicFileModel(SasMetaData.builder().name("EMPTY").build(), new Object[0][0]);

    @lombok.NonNull
    private final SasMetaData metaData;

    @lombok.NonNull
    private final Object[][] data;
}
