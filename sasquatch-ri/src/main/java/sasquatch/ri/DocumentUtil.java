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
package sasquatch.ri;

import internal.bytes.PValue;
import internal.ri.base.Encoding;
import internal.ri.data.ColAttr;
import internal.ri.data.ColType;
import internal.ri.data.Document;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import sasquatch.SasColumn;
import sasquatch.SasColumnFormat;
import sasquatch.SasColumnType;
import sasquatch.SasMetaData;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
class DocumentUtil {

    Charset getCharset(Document doc) throws IOException {
        return doc.getHeader().getEncoding().or(Encoding.DEFAULT).getCharset();
    }

    int[] getOffsets(Document doc) {
        int[] result = new int[doc.getColAttrList().size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = doc.getColAttrList().get(i).getOffset();
        }
        return result;
    }

    SasMetaData getMetaData(Document doc) throws IOException {
        Charset charset = getCharset(doc);

        SasMetaData.Builder result = SasMetaData.builder()
                .name(doc.getHeader().getName())
                .label(doc.getString(doc.getRowSize().getLabelRef(), charset))
                .creationTime(doc.getHeader().getCreationTime())
                .lastModificationTime(doc.getHeader().getLastModificationTime())
                .release(doc.getHeader().getSasRelease())
                .host(doc.getHeader().getSasHost())
                .rowCount(doc.getRowSize().getCount());

        SasColumn.Builder cb = SasColumn.builder();
        for (int j = 0; j < doc.getColSize().getCount(); j++) {
            ColAttr colAttr = doc.getColAttrList().get(j);
            String formatName = doc.getColumnFormatName(j, charset);
            result.column(cb
                    .order(j)
                    .name(doc.getColumnName(j, charset))
                    .format(SasColumnFormat
                            .builder()
                            .name(formatName)
                            .width(doc.getColLabsList().get(j).getFormatWidth())
                            .precision(doc.getColLabsList().get(j).getFormatPrecision())
                            .build())
                    .label(doc.getColumnLabel(j, charset))
                    .type(getColumnType(colAttr.getType(), formatName))
                    .length(colAttr.getLength())
                    .build());
        }

        return result.build();
    }

    private static SasColumnType getColumnType(PValue<ColType, ?> type, String format) {
        return type.isKnownAs(ColType.NUMERIC)
                ? TYPE_BY_FORMAT.getOrDefault(format, SasColumnType.NUMERIC)
                : SasColumnType.CHARACTER;
    }

    private static final Map<String, SasColumnType> TYPE_BY_FORMAT = Stream.of(SasCommonFormats.values())
            .collect(Collectors.toMap(SasCommonFormats::name, SasCommonFormats::getColumnType));

}
