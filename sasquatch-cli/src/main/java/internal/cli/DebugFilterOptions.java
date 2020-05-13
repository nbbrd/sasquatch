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
package internal.cli;

import internal.ri.base.Header;
import internal.ri.base.PageHeader;
import internal.ri.base.SubHeaderPointer;
import internal.ri.data.ColAttr;
import internal.ri.data.Document;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Philippe Charles
 */
@lombok.Data
public final class DebugFilterOptions {

    public enum Scope {
        ALL, KNOWN, UNKNOWN;
    }

    @CommandLine.Option(
            names = {"--scope"},
            description = "Filter on scope (${COMPLETION-CANDIDATES}).",
            paramLabel = "<scope>",
            defaultValue = "ALL"
    )
    private Scope scope;

    @CommandLine.Option(
            names = {"--where"},
            description = "Filter on key-value.",
            paramLabel = "<key=value>"
    )
    private Map<String, String> keyValues = new HashMap<>();

    public boolean testScope(Header header) {
        switch (scope) {
            case ALL:
                return true;
            case KNOWN:
                return !hasUnkwown(header);
            case UNKNOWN:
                return hasUnkwown(header);
            default:
                throw new RuntimeException();
        }
    }

    public boolean testScope(Document doc) {
        switch (scope) {
            case ALL:
                return true;
            case KNOWN:
                return !hasUnkwown(doc);
            case UNKNOWN:
                return hasUnkwown(doc);
            default:
                throw new RuntimeException();
        }
    }

    public boolean testScope(PageHeader page) {
        switch (scope) {
            case ALL:
                return true;
            case KNOWN:
                return !hasUnkwown(page);
            case UNKNOWN:
                return hasUnkwown(page);
            default:
                throw new RuntimeException();
        }
    }

    public boolean testScope(SubHeaderPointer pointer) {
        switch (scope) {
            case ALL:
                return true;
            case KNOWN:
                return !hasUnkwown(pointer);
            case UNKNOWN:
                return hasUnkwown(pointer);
            default:
                throw new RuntimeException();
        }
    }

    public boolean testKeyValues(Object obj) {
        if (keyValues.isEmpty()) {
            return true;
        }
        String headerString = normalize(obj.toString());
        return keyValues.entrySet()
                .stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .map(DebugFilterOptions::normalize)
                .allMatch(headerString::contains);
    }

    private static String normalize(String input) {
        return input.toLowerCase(Locale.ROOT);
    }

    private static boolean hasUnkwown(Header header) {
        return header.getEncoding().isUnknown()
                || header.getPlatform().isUnknown();
    }

    private static boolean hasUnkwown(Document doc) {
        return hasUnkwown(doc.getHeader())
                || doc.getCompression().isUnknown()
                || doc.getColAttrList().stream().anyMatch(DebugFilterOptions::hasUnkwown);
    }

    private static boolean hasUnkwown(ColAttr colAttr) {
        return colAttr.getType().isUnknown();
    }

    private static boolean hasUnkwown(PageHeader page) {
        return page.getType().isUnknown();
    }

    private static boolean hasUnkwown(SubHeaderPointer pointer) {
        return pointer.getFormat().isUnknown();
    }
}
