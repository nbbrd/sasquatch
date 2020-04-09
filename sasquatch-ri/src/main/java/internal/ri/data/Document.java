/*
 * Copyright 2013 National Bank of Belgium
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
package internal.ri.data;

import internal.bytes.BytesReader;
import internal.bytes.PValue;
import internal.ri.base.Header;
import internal.ri.base.PageHeader;
import internal.ri.base.RowIndex;
import internal.ri.base.SasFile;
import internal.ri.base.SasFileVisitor;
import internal.ri.base.SubHeaderFormat;
import internal.ri.base.SubHeaderPointer;
import static internal.ri.data.DescriptorType.COL_LABS;
import static internal.ri.data.DescriptorType.COL_SIZE;
import static internal.ri.data.DescriptorType.COL_TEXT;
import static internal.ri.data.DescriptorType.ROW_SIZE;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.READ;
import java.util.ArrayList;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;
import static internal.ri.data.DescriptorType.COL_ATTRS;
import static internal.ri.data.DescriptorType.COL_NAMES;
import java.nio.charset.Charset;
import java.util.Optional;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
public final class Document {

    /*
     * Other possible values:
     * engine, protection, type, label, nbrIndexes, 
     * nbrDeletedRows, isCompressed, isSorted, 
     * firstDataPage, maxRowPerPage,repairCount
     */
    private long length;
    private Header header;
    private PValue<Compression, String> compression;
    private RowSize rowSize;
    private ColSize colSize;
    private SubhCnt subhCnt;
    private List<ColText> colTextList;
    private List<ColName> colNameList;
    private List<ColAttr> colAttrList;
    private List<ColLabs> colLabsList;
    private List<RowIndex> rowIndexList;

    public String getColumnName(int index, Charset charset) {
        return getString(colNameList.get(index).getName(), charset);
    }

    public String getColumnLabel(int index, Charset charset) {
        return getString(colLabsList.get(index).getLabel(), charset);
    }

    public String getColumnFormatName(int index, Charset charset) {
        return getString(colLabsList.get(index).getFormatName(), charset);
    }

    public String getString(StringRef ref, Charset charset) {
        return getString(colTextList, header.isU64(), ref, charset);
    }

    private static String getString(List<ColText> list, boolean u64, StringRef ref, Charset charset) {
        return list
                .get(ref.getHdr())
                .getContent()
                .getString((u64 ? 8 : 4) + ref.getOff(), ref.getLen(), charset);
    }

    @NonNull
    public static Document parse(@NonNull Path file) throws IOException {
        try (SeekableByteChannel sbc = Files.newByteChannel(file, READ)) {
            return parse(sbc);
        }
    }

    @NonNull
    public static Document parse(@NonNull SeekableByteChannel sbc) throws IOException {
        DocumentVisitor v = new DocumentVisitor();
        SasFile.visit(sbc, v);

        // Check sub headers
        checkDescriptorCount(v.rowSizeList, 1, DescriptorType.ROW_SIZE);
        RowSize rowSize = v.rowSizeList.get(0);

        checkDescriptorCount(v.colSizeList, 1, DescriptorType.COL_SIZE);
        ColSize colSize = v.colSizeList.get(0);

        checkDescriptorCount(v.subhCntList, 1, DescriptorType.SUBH_CNT);
        SubhCnt subhCnt = v.subhCntList.get(0);

        checkDescriptorCount(v.colNameList, colSize.getCount(), DescriptorType.COL_NAMES);
        checkDescriptorCount(v.colAttrList, colSize.getCount(), DescriptorType.COL_ATTRS);
        checkDescriptorCount(v.colLabsList, colSize.getCount(), DescriptorType.COL_LABS);

        if (v.colTextList.isEmpty()) {
            throw new IOException("At least one column-text subheaders expected");
        }

        String compression = getString(v.colTextList, v.header.isU64(), rowSize.getCompression(), US_ASCII);

        return new Document(
                sbc.size(), v.header, Compression.tryParse(compression),
                rowSize, colSize, subhCnt, v.colTextList,
                v.colNameList, v.colAttrList, v.colLabsList, v.rowIndexList
        );
    }

    private static void checkDescriptorCount(List<?> list, int expectedSize, DescriptorType type) throws IOException {
        if (list.size() != expectedSize) {
            throw new IOException("Invalid count of descriptor " + type + ", expected:" + expectedSize + ", actual:" + list.size());
        }
    }

    private static final class DocumentVisitor implements SasFileVisitor {

        Header header;
        final List<RowSize> rowSizeList = new ArrayList<>();
        final List<ColSize> colSizeList = new ArrayList<>();
        final List<ColAttr> colAttrList = new ArrayList<>();
        final List<ColName> colNameList = new ArrayList<>();
        final List<ColLabs> colLabsList = new ArrayList<>();
        final List<ColText> colTextList = new ArrayList<>();
        final List<RowIndex> rowIndexList = new ArrayList<>();
        final List<SubhCnt> subhCntList = new ArrayList<>();

        @Override
        public FileVisitResult onHeader(Header header) {
            this.header = header;
            return FileVisitResult.CONTINUE;
        }

        private boolean hasRemainingMeta() {
            return rowSizeList.size() == 1 && rowSizeList.get(0).getNct() > colTextList.size();
        }

        @Override
        public FileVisitResult onPage(Header header, PageHeader page, BytesReader pageData) {
            if (page.getType().isKnown()) {
                switch (page.getType().get()) {
                    case META:
                    case MIX:
                    case AMD:
                    case INDEX:
                        return FileVisitResult.CONTINUE;
                }
            }
            return hasRemainingMeta() ? FileVisitResult.CONTINUE : FileVisitResult.TERMINATE;
        }

        @Override
        public FileVisitResult onSubHeaderPointer(Header header, PageHeader page, BytesReader pageData, SubHeaderPointer pointer) {
            if (pointer.getFormat().isKnownAs(SubHeaderFormat.PLAIN)) {
                Optional<PValue<DescriptorType, String>> type = DescriptorType.tryParse(pageData, header.isU64(), pointer);
                if (type.isPresent() && type.get().isKnown()) {
                    switch (type.get().get()) {
                        case ROW_SIZE:
                            rowSizeList.add(RowSize.parse(pageData, header.isU64(), pointer));
                            break;
                        case COL_SIZE:
                            colSizeList.add(ColSize.parse(pageData, header.isU64(), pointer));
                            break;
                        case COL_ATTRS:
                            colAttrList.addAll(ColAttrs.parse(pageData, header.isU64(), pointer).getItems());
                            break;
                        case COL_NAMES:
                            colNameList.addAll(ColNames.parse(pageData, header.isU64(), pointer).getItems());
                            break;
                        case COL_LABS:
                            colLabsList.add(ColLabs.parse(pageData, header.isU64(), pointer));
                            break;
                        case COL_TEXT:
                            colTextList.add(ColText.parse(pageData, header.isU64(), pointer));
                            break;
                        case SUBH_CNT:
                            subhCntList.add(SubhCnt.parse(pageData, header.isU64(), pointer));
                            break;
                    }
                }
                return FileVisitResult.CONTINUE;
            }
            return FileVisitResult.SKIP_SIBLINGS;
        }

        @Override
        public FileVisitResult onRowIndex(Header header, PageHeader page, BytesReader pageData, RowIndex rowIndex) {
            rowIndexList.add(rowIndex);
            return FileVisitResult.CONTINUE;
        }
    }
}
