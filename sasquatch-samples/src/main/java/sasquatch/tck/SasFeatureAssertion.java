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
package sasquatch.tck;

import java.io.IOException;
import java.util.Set;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import org.assertj.core.api.SoftAssertions;
import sasquatch.spi.SasFeature;
import sasquatch.spi.SasReader;

/**
 *
 * @author Philippe Charles
 */
@ServiceDefinition(singleton = true, quantifier = Quantifier.MULTIPLE)
public interface SasFeatureAssertion {

    void assertFeature(SoftAssertions s, SasReader reader, Set<SasFeature> features) throws IOException;
}
