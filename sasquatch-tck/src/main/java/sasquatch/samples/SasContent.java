/*
 * Copyright 2017 National Bank of Belgium
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
package sasquatch.samples;

import java.nio.file.Path;
import java.util.List;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceId;
import sasquatch.spi.SasReader;

/**
 *
 * @author Philippe Charles
 */
@ServiceDefinition(singleton = true, quantifier = Quantifier.MULTIPLE)
public interface SasContent {

    @ServiceId
    String getName();

    List<FileError> parse(SasReader reader);

    default void printErrors(SasReader reader) {
        parse(reader)
                .forEach(System.err::println);
    }

    interface FileError {

        String getName();

        Path getFile();
    }

    @lombok.Value
    static final class MissingError implements FileError {

        private String name;
        private Path file;
    }

    @lombok.Value
    static final class UnexpectedError implements FileError {

        private String name;
        private Path file;
        private Exception ex;
    }

    @lombok.Value
    static final class EmptyError implements FileError {

        private String name;
        private Path file;
    }

    @lombok.Value
    static final class HeadError implements FileError {

        private String name;
        private Path file;
        private int col;
        private String expected;
        private String actual;
    }

    @lombok.Value
    static final class BodyError implements FileError {

        private String name;
        private Path file;
        private int row;
        private int col;
        private String expected;
        private String actual;
    }
}
