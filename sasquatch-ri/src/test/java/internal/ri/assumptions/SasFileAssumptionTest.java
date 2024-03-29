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
package internal.ri.assumptions;

import internal.ri.assumptions.impl.PageAssumptions;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import sasquatch.samples.SasResources;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Philippe Charles
 */
public class SasFileAssumptionTest {

    @Test
    public void testAssumptions() throws IOException {
        for (Iterator<Path> iter = SasResources.all().iterator(); iter.hasNext(); ) {
            Path file = iter.next();
            SasFileAssumption.testAll(file, this::checkError);
        }
    }

    private void checkError(SasFileError error) {
        KnownProblem problem = asKnownProblem(error);
        if (!knownProblems
                .stream()
                .filter(problem::isRelatedTo)
                .findFirst()
                .isPresent()) {
            Assertions.fail(error.toString());
        }
    }

    private KnownProblem asKnownProblem(SasFileError error) {
        String fileName = error.getFile().getName(error.getFile().getNameCount() - 1).toString();
        return new KnownProblem(fileName, error.getAssumption().getName());
    }

    private final Set<KnownProblem> knownProblems = new HashSet<>(Arrays.asList(
            new KnownProblem("kole.sas7bdat", PageAssumptions.FILE_SIZE.getName()),
            new KnownProblem("comp_deleted.sas7bdat", "")
    ));

    @lombok.Value
    private static class KnownProblem {

        private String fileName;
        private String assumptionName;

        public boolean isRelatedTo(KnownProblem other) {
            return isSameFile(getFileName(), other.getFileName())
                    && isSameAssumption(getAssumptionName(), other.getAssumptionName());
        }

        private static boolean isSameFile(String l, String r) {
            return l.equals(r);
        }

        private static boolean isSameAssumption(String l, String r) {
            return l.isEmpty() || r.isEmpty() || l.equals(r);
        }
    }
}
