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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nbbrd.picocsv.Csv;
import sasquatch.SasColumn;
import sasquatch.SasForwardCursor;
import sasquatch.SasMetaData;
import sasquatch.SasRow;
import sasquatch.spi.SasReader;

/**
 *
 * @author Philippe Charles
 */
public abstract class CsvContent implements SasContent {

    @Override
    public List<FileError> parse(SasReader reader) {
        return getSasFiles()
                .map(sasFile -> compareToCsv(reader, sasFile))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    abstract protected Stream<Path> getSasFiles();

    abstract protected Path resolveCsvFile(Path sasFile);

    abstract protected Path relativizeSasFile(Path sasFile);

    abstract protected SasRow.Mapper<String> getColumnFunc(SasColumn c);

    protected Charset getCharset(Path csvFile) {
        return StandardCharsets.UTF_8;
    }

    protected Csv.Format getFormat(Path csvFile) {
        return Csv.Format.RFC4180;
    }

    protected Csv.Parsing getParsing(Path csvFile) {
        return Csv.Parsing.LENIENT;
    }

    private FileError compareToCsv(SasReader reader, Path sasFile) {
        Path csvFile = resolveCsvFile(sasFile);
        if (Files.exists(csvFile)) {
            try {
                return compareToCsv(reader, sasFile, csvFile);
            } catch (Exception ex) {
                return new UnexpectedError(getName(), relativizeSasFile(sasFile), ex);
            }
        }
        return new MissingError(getName(), relativizeSasFile(sasFile));
    }

    private FileError compareToCsv(SasReader reader, Path sasFile, Path csvFile) throws IOException {
        try (Csv.Reader csv = Csv.Reader.of(csvFile, getCharset(csvFile), getFormat(csvFile), getParsing(csvFile))) {
            try (SasForwardCursor sas = reader.readForward(sasFile)) {
                SasMetaData meta = sas.getMetaData();

                if (csv.readLine()) {
                    int col = 0;
                    while (csv.readField()) {
                        CharSequence expected = csv.toString();
                        String actual = meta.getColumns().get(col).getName();
                        if (!actual.contentEquals(expected)) {
                            return new HeadError(getName(), relativizeSasFile(sasFile), col, expected.toString(), actual);
                        }
                        col++;
                    }
                } else {
                    return new EmptyError(getName(), relativizeSasFile(sasFile));
                }

                int row = 0;
                List<SasRow.Mapper<String>> func = getRowFunc(meta);
                while (csv.readLine() && sas.next()) {
                    int col = 0;
                    while (csv.readField()) {
                        CharSequence expected = csv;
                        String actual = func.get(col).apply(sas);
                        if (!actual.contentEquals(expected)) {
                            return new BodyError(getName(), relativizeSasFile(sasFile), row, col, expected.toString(), actual);
                        }
                        col++;
                    }
                    row++;
                }

                if (csv.readLine()) {
                    return new BodyError(getName(), relativizeSasFile(sasFile), row, 0, "", null);
                }

                if (sas.next()) {
                    return new BodyError(getName(), relativizeSasFile(sasFile), row, 0, null, "");
                }
            }
        }
        return null;
    }

    private List<SasRow.Mapper<String>> getRowFunc(SasMetaData meta) {
        return meta.getColumns().stream()
                .map(this::getColumnFunc)
                .collect(Collectors.toList());
    }
}
