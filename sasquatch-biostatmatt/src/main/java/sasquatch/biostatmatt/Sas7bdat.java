package sasquatch.biostatmatt;

import static sasquatch.biostatmatt.RUtils.*;
import sasquatch.biostatmatt.RUtils.DataType;
import static sasquatch.biostatmatt.RUtils.DataType.*;
import sasquatch.biostatmatt.RUtils.RCon;
import sasquatch.biostatmatt.RUtils.RFrame;
import sasquatch.biostatmatt.RUtils.RFunc;
import sasquatch.biostatmatt.RUtils.RList;
import sasquatch.biostatmatt.RUtils.RVector;
import java.io.IOException;
import java.nio.ByteOrder;
import static java.nio.ByteOrder.*;

/**
 * https://github.com/BioStatMatt/sas7bdat/blob/master/R/sas7bdat.R
 *
 * @version b2aeaf00e1b8861c419d0370ed02684253b4e9a7
 */
public final class Sas7bdat {

    static final String VERSION = "0.5";
    static final String BUGREPORT = "please report bugs to maintainer";
    static final String CAUTION = "please verify data correctness";

    //# Subheader 'signatures'
    static final RVector<Byte> SUBH_ROWSIZE = as.raw(c(0xF7, 0xF7, 0xF7, 0xF7));
    static final RVector<Byte> SUBH_COLSIZE = as.raw(c(0xF6, 0xF6, 0xF6, 0xF6));
    static final RVector<Byte> SUBH_COLTEXT = as.raw(c(0xFD, 0xFF, 0xFF, 0xFF));
    static final RVector<Byte> SUBH_COLATTR = as.raw(c(0xFC, 0xFF, 0xFF, 0xFF));
    static final RVector<Byte> SUBH_COLNAME = as.raw(c(0xFF, 0xFF, 0xFF, 0xFF));
    static final RVector<Byte> SUBH_COLLABS = as.raw(c(0xFE, 0xFB, 0xFF, 0xFF));
    static final RVector<Byte> SUBH_COLLIST = as.raw(c(0xFE, 0xFF, 0xFF, 0xFF));
    static final RVector<Byte> SUBH_SUBHCNT = as.raw(c(0x00, 0xFC, 0xFF, 0xFF));

    //# Page types
    static final int[] PAGE_META = {0};
//    static final int[] PAGE_DATA = {256};          //#1<<8
    static final int[] PAGE_DATA = c(256/*[>]*/, 384/*[<]*/); //#1<<8,1<<8|1<<7
    static final int[] PAGE_MIX = c(512, 640); //#1<<9,1<<9|1<<7
    static final int[] PAGE_AMD = {1024};          //#1<<10
    static final int[] PAGE_METC = {16384};        //#1<<14 (compressed data)
    static final int[] PAGE_COMP = {-28672};       //#~(1<<14|1<<13|1<<12) 
    static final int[] PAGE_MIX_DATA = c(PAGE_MIX, PAGE_DATA);
    static final int[] PAGE_META_MIX_AMD = c(PAGE_META, PAGE_MIX, PAGE_AMD);
    static final int[] PAGE_ANY = c(PAGE_META_MIX_AMD, PAGE_DATA, PAGE_METC, PAGE_COMP);

    static String page_type_strng(int type) {
        if (_in_(type, PAGE_META)) {
            return ("meta");
        }
        if (_in_(type, PAGE_DATA)) {
            return ("data");
        }
        if (_in_(type, PAGE_MIX)) {
            return ("mix");
        }
        if (_in_(type, PAGE_AMD)) {
            return ("amd");
        }
        return ("unknown");
    }

    static RList<SubHeader> read_subheaders(Page page, boolean u64) {
        RList<SubHeader> subhs = list();
        int subh_total = 0;
        if (!(_in_(page.type, PAGE_META_MIX_AMD))) {
            return (subhs);
        }
        //# page offset of subheader pointers    
        int oshp = u64 ? 40 : 24;
        //# length of subheader pointers    
        int lshp = u64 ? 24 : 12;
        //# length of first two subheader fields    
        int lshf = u64 ? 8 : 4;
        for (int i : seq(1, page.subh_count)) {
            subh_total = subh_total + 1;
            int base = oshp + (i - 1) * lshp;
            subhs.set(subh_total, new SubHeader());
            subhs.get(subh_total).page = page.page;
            // DEBUG > int vs long for offset and length
            subhs.get(subh_total).offset = read_int(page.data, base, lshf).intValue();
            subhs.get(subh_total).length = read_int(page.data, base + lshf, lshf).intValue();
            if (subhs.get(subh_total).length > 0) {
                subhs.get(subh_total).raw = read_raw(page.data,
                        subhs.get(subh_total).offset, subhs.get(subh_total).length);
                subhs.get(subh_total).signature = read_raw(subhs.get(subh_total).raw, 0, 4);
            }
        }
        return subhs;
    }

    static RList<ColumnName> read_column_names(RList<SubHeader> col_name, RList<SubHeader> col_text, boolean u64) {
        RList<ColumnName> names = list();
        int name_count = 0;
        int offp = u64 ? 8 : 4;
        for (SubHeader subh : col_name) {
            int cmax = (subh.length - (u64 ? 28 : 20)) / 8;
            for (int i : seq(1, cmax)) {
                name_count = name_count + 1;
                names.set(name_count, new ColumnName());
                int base = (u64 ? 16 : 12) + (i - 1) * 8;
                short hdr = read_int(subh.raw, base, 2).shortValue();
                short off = read_int(subh.raw, base + 2, 2).shortValue();
                short len = read_int(subh.raw, base + 4, 2).shortValue();
                names.get(name_count).name = read_str(col_text.get(hdr + 1).raw,
                        off + offp, len);
            }
        }
        return names;
    }

    static RList<ColumnLabelFormat> read_column_labels_formats(RList<SubHeader> col_labs, RList<SubHeader> col_text, boolean u64) {
        if (length(col_labs) < 1) {
            return null;
        }
        int offp = u64 ? 8 : 4;
        RList<ColumnLabelFormat> labs = list();
        for (int i : seq(1, length(col_labs))) {
            labs.set(i, new ColumnLabelFormat());
            int base = u64 ? 46 : 34;
            short hdr = read_int(col_labs.get(i).raw, base, 2).shortValue();
            short off = read_int(col_labs.get(i).raw, base + 2, 2).shortValue();
            short len = read_int(col_labs.get(i).raw, base + 4, 2).shortValue();
            if (len > 0) {
                labs.get(i).format = read_str(col_text.get(hdr + 1).raw,
                        off + offp, len);
            }
            labs.get(i).fhdr = hdr;
            labs.get(i).foff = off;
            labs.get(i).flen = len;
            base = u64 ? 52 : 40;
            hdr = read_int(col_labs.get(i).raw, base, 2).shortValue();
            off = read_int(col_labs.get(i).raw, base + 2, 2).shortValue();
            len = read_int(col_labs.get(i).raw, base + 4, 2).shortValue();
            if (len > 0) {
                labs.get(i).label = read_str(col_text.get(hdr + 1).raw,
                        off + offp, len);
            }
            labs.get(i).lhdr = hdr;
            labs.get(i).loff = off;
            labs.get(i).llen = len;
        }
        return labs;
    }

    static RList<ColumnAttribute> read_column_attributes(RList<SubHeader> col_attr, boolean u64) {
        RList<ColumnAttribute> info = list();
        int info_ct = 0;
        int lcav = u64 ? 16 : 12;
        for (SubHeader subh : col_attr) {
            int cmax = (subh.length - (u64 ? 28 : 20)) / lcav;
            for (int i : seq(1, cmax)) {
                info_ct = info_ct + 1;
                info.set(info_ct, new ColumnAttribute());
                int base = lcav + (i - 1) * lcav;
                info.get(info_ct).offset = read_int(subh.raw, base,
                        u64 ? 8 : 4).intValue();
                info.get(info_ct).length = read_int(subh.raw,
                        base + (u64 ? 8 : 4),
                        4).intValue();
                info.get(info_ct)._type_ = read_int(subh.raw,
                        base + (u64 ? 14 : 10),
                        1).byteValue();
                info.get(info_ct).type = (info.get(info_ct)._type_ == 1
                        ? NUMERIC : CHARACTER);
            }
        }
        return info;
    }

    //# Magic number
    static final RVector<Byte> MAGIC = as.raw(c(
            0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
            0x0, 0x0, 0x0, 0x0, 0xc2, 0xea, 0x81, 0x60,
            0xb3, 0x14, 0x11, 0xcf, 0xbd, 0x92, 0x8, 0x0,
            0x9, 0xc7, 0x31, 0x8c, 0x18, 0x1f, 0x10, 0x11));

    static boolean check_magic_number(RVector<Byte> data) {
        return identical(_sub_(data, 1, length(MAGIC)), MAGIC);
    }

    //# These functions utilize offset + length addressing
    static Object read_bin(RVector<Byte> buf, int off, int len, DataType type) {
        return readBin(_sub_(buf, off + 1, off + len), type, 1, len);
    }

    static RVector<Byte> read_raw(RVector<Byte> buf, int off, int len) {
        return (RVector<Byte>) readBin(_sub_(buf, off + 1, off + len), RAW, len, 1);
    }

    static Number read_int(RVector<Byte> buf, int off, int len) {
        return (Number) read_bin(buf, off, len, INTEGER);
    }

    static String read_str(RVector<Byte> buf, int off, int len) {
        return ((String) read_bin(buf, off, len, CHARACTER))/*[>]*/.trim()/*[<]*/;
    }

    static Number read_flo(RVector<Byte> buf, int off, int len) {
        return (Number) read_bin(buf, off, len, DOUBLE);
    }

    static RList<SubHeader> get_subhs(RList<SubHeader> subhs, final RVector<Byte> signature) {
        Iterable<Boolean> keep = sapply(subhs, new RFunc<SubHeader, Boolean>() {
            @Override
            public Boolean apply(SubHeader subhs) {
                return identical(subhs.signature, signature);
            }
        });
        return subhs.filter(keep);
    }

    //# Sometimes there is more than one column attribute subheader.
    //# In these cases, the column attribute data are spliced together
    //# so that the appear to have been in the same subheader
    static void splice_col_attr_subheaders(RList<SubHeader> col_attr) {
        RVector<Byte> raw = read_raw(col_attr.get(1).raw, 0, col_attr.get(1).length - 8);
        for (int i : seq(2, length(col_attr))) {
            raw = c(raw, read_raw(col_attr.get(i).raw, 12,
                    col_attr.get(i).length - 20));
        }
//        return list(raw = raw);
    }

    static RFrame readSas7bdat(Object file, String encoding, boolean debug/*[>]*/, Callback _callback_/*[<]*/) throws IOException {
        RCon con = null;
        boolean close_con = true;
        if (inherits(file, "connection") && isOpen(file, "read")) {
            con = (RCon) file;
            close_con = false;
        } else if (is.character(file)) {
            con = file((String) file, "rb");
            close_con = true;
        } else {
            stop("invalid 'file' argument");
        }
        /*[>]*/
        try {
            return readSas7bdat(con, encoding, debug, _callback_, close_con);
        } finally {
            if (close_con) {
                close(con);
            }
        }
        /*[<]*/
    }

    private static RFrame readSas7bdat(RCon con, String encoding, boolean debug, Callback _callback_, boolean close_con) throws IOException {

        //# Check magic number
        RVector<Byte> header = (RVector<Byte>) readBin(con, RAW, 288, 1);
        if (length(header) < 288) {
            stop("header too short (not a sas7bdat file?)");
        }
        if (!check_magic_number(header)) {
            stop(paste("magic number mismatch", BUGREPORT));
        }

        //# Check for 32 or 64 bit alignment
        int align1;
        RVector<Byte> _align1_ = read_raw(header, 32, 1);
        if (identical(_align1_, as.raw(0x33))) {
            align1 = 4;
        } else {
            align1 = 0;
        }

        //# If align1 == 4, file is u64 type
        boolean u64;
        u64 = align1 == 4;

        int align2;
        RVector<Byte> _align2_ = read_raw(header, 35, 1);
        if (identical(_align2_, as.raw(0x33))) {
            align2 = 4;
        } else {
            align2 = 0;
        }

        ByteOrder endian;
        RVector<Byte> _endian_ = read_raw(header, 37, 1);
        if (identical(_endian_, as.raw(0x01))) {
            endian = LITTLE_ENDIAN;
        } else {
            endian = BIG_ENDIAN;
            stop("big endian files are not supported");
        }

        String winunix = read_str(header, 39, 1);
        if (identical(winunix, "1")) {
            winunix = "unix";
        } else if (identical(winunix, "2")) {
            winunix = "windows";
        } else {
            winunix = "unknown";
        }

        //# Timestamp is epoch 01/01/1960
        double datecreated = read_flo(header, 164 + align1, 8).doubleValue();
        datecreated = datecreated + as.POSIXct("1960/01/01", "%Y/%m/%d");
        double datemodified = read_flo(header, 172 + align1, 8).doubleValue();
        datemodified = datemodified + as.POSIXct("1960/01/01", "%Y/%m/%d");

        //# Read the remaining header
        int header_length = read_int(header, 196 + align2, 4).intValue();
        header = c(header, (RVector<Byte>) readBin(con, RAW, header_length - 288, 1));
        if (length(header) < header_length) {
            stop("header too short (not a sas7bdat file?)");
        }

        int page_size = read_int(header, 200 + align2, 4).intValue();
        if (page_size < 0) {
            stop(paste("page size is negative", BUGREPORT));
        }

        int page_count = read_int(header, 204 + align2, 4).intValue();
        if (page_count < 1) {
            stop(paste("page count is not positive", BUGREPORT));
        }

        String SAS_release = read_str(header, 216 + align1 + align2, 8);

        //# SAS_host is a 16 byte field, but only the first eight are used
        //# FIXME: It would be preferable to eliminate this check
        String SAS_host = read_str(header, 224 + align1 + align2, 8);

        String OS_version = read_str(header, 240 + align1 + align2, 16);
        String OS_maker = read_str(header, 256 + align1 + align2, 16);
        String OS_name = read_str(header, 272 + align1 + align2, 16);

        /*[>]*/
        MissingHeader _missingHeader_ = new MissingHeader(
                read_str(header, 92, 64),
                read_str(header, 156, 8),
                page_count,
                page_size,
                u64,
                endian == LITTLE_ENDIAN);
        /*[<]*/

        //# Read pages
        RList<Page> pages = list();
        for (int page_num : seq(1, page_count)) {
            pages.set(page_num, new Page());
            pages.get(page_num).page = page_num;
            pages.get(page_num).data = (RVector<Byte>) readBin(con, RAW, page_size, 1);
            pages.get(page_num).type = read_int(pages.get(page_num).data, u64 ? 32 : 16, 2).shortValue();
            pages.get(page_num).type_strng = page_type_strng(pages.get(page_num).type);
            pages.get(page_num).blck_count = read_int(pages.get(page_num).data, u64 ? 34 : 18, 2).shortValue();
            pages.get(page_num).subh_count = read_int(pages.get(page_num).data, u64 ? 36 : 20, 2).shortValue();
        }

        //# Read all subheaders
        RList<SubHeader> subhs = list();
        for (Page page : pages) {
            subhs = c(subhs, read_subheaders(page, u64));
        }

        //# Parse row size subheader
        RList<SubHeader> _row_size_ = get_subhs(subhs, SUBH_ROWSIZE);
        if (length(_row_size_) != 1) {
            stop(paste("found", length(_row_size_),
                    "row size subheaders where 1 expected", BUGREPORT));
        }
        SubHeader row_size = _row_size_.get(1);
        int row_length = read_int(row_size.raw,
                u64 ? 40 : 20,
                u64 ? 8 : 4).intValue();
        int row_count = read_int(row_size.raw,
                u64 ? 48 : 24,
                u64 ? 8 : 4).intValue();
        int col_count_p1 = read_int(row_size.raw,
                u64 ? 72 : 36,
                u64 ? 8 : 4).intValue();
        int col_count_p2 = read_int(row_size.raw,
                u64 ? 80 : 40,
                u64 ? 8 : 4).intValue();
        int row_count_fp = read_int(row_size.raw,
                u64 ? 120 : 60,
                u64 ? 8 : 4).intValue();

        /*[>]*/
        RowsInfo _rowsInfo_ = new RowsInfo(row_length, row_count, row_count_fp);
        /*[<]*/

        //# Parse col size subheader
        RList<SubHeader> _col_size_ = get_subhs(subhs, SUBH_COLSIZE);
        if (length(_col_size_) != 1) {
            stop(paste("found", length(_col_size_),
                    "column size subheaders where 1 expected", BUGREPORT));
        }
        SubHeader col_size = _col_size_.get(1);
        int col_count_6 = read_int(col_size.raw,
                u64 ? 8 : 4,
                u64 ? 8 : 4).intValue();
        int col_count = col_count_6;

        //#if((col_count_p1 + col_count_p2) != col_count_6)
        //#    warning(paste("column count mismatch" , CAUTION))
        //# Read column information
        RList<SubHeader> col_text = get_subhs(subhs, SUBH_COLTEXT);
        if (length(col_text) < 1) {
            stop(paste("no column text subheaders found", BUGREPORT));
        }

        //# Test for COMPRESS=CHAR compression
        //# This test is done earlier at the page level
        //#if("SASYZCRL" == read_str(col_text[[1]]$raw, 16, 8))
        //#    stop(paste("file uses unsupported CHAR compression
        RList<SubHeader> _col_attr_ = get_subhs(subhs, SUBH_COLATTR);
        if (length(_col_attr_) < 1) {
            stop(paste("no column attribute subheaders found", BUGREPORT));
        }

        RList<ColumnAttribute> col_attr = read_column_attributes(_col_attr_, u64);
        if (length(col_attr) != col_count) {
            stop(paste("found", length(col_attr),
                    "column attributes where", col_count,
                    "expected", BUGREPORT));
        }

        RList<SubHeader> _col_name_ = get_subhs(subhs, SUBH_COLNAME);
        if (length(_col_name_) < 1) {
            stop(paste("no column name subheaders found", BUGREPORT));
        }

        RList<ColumnName> col_name = read_column_names(_col_name_, col_text, u64);
        if (length(col_name) != col_count) {
            stop(paste("found", length(col_name),
                    "column names where", col_count, "expected", BUGREPORT));
        }

        //# Make column names unique, if not already
        RList<String> col_name_uni = make.unique(sapply(col_name, function_x_x_name()));
        for (int i : seq(1, length(col_name_uni))) {
            col_name.get(i).name = col_name_uni.get(i);
        }

        RList<SubHeader> _col_labs_ = get_subhs(subhs, SUBH_COLLABS);
        RList<ColumnLabelFormat> col_labs = read_column_labels_formats(_col_labs_, col_text, u64);
        if (col_labs == null) {
            col_labs = list(col_count);
        }
        if (length(col_labs) != col_count) {
            stop(paste("found", length(col_labs),
                    "column formats and labels", col_count, "expected", BUGREPORT));
        }

        //# Collate column information
        RList<Column> col_info = list();
        for (int i : seq(1, col_count)) {
            col_info.set(i, co(col_name.get(i), col_attr.get(i), col_labs.get(i)));
        }

        //# Check pages for known type     
        for (int page_num : seq(1, page_count)) {
            if (!(_in_(pages.get(page_num).type, PAGE_ANY)) /*[>]*/ && !_callback_.ignoreUnknownPage(pages.get(page_num).type) /*[<]*/) {
                stop(paste("page", page_num, "has unknown type:",
                        pages.get(page_num).type, BUGREPORT));
            }
            if (_in_(pages.get(page_num).type, c(PAGE_METC, PAGE_COMP))) {
                stop("file contains compressed data");
            }
        }

        //# Parse data    
        RList<RVector<Object>> _data_ = list();
        for (Column col : col_info) {
            if (col.length > 0) {
                _data_.set(col.name, vector(col.type, row_count));
            }
        }

        if (/*[>]*/_callback_.parseData(col_info, row_count)/*[<]*/) {

            int row = 0;
            for (Page page : pages) {
                //#FIXME are there data on pages of type 4?
                if (!(_in_(page.type, PAGE_MIX_DATA))) {
                    continue;
                }
                int base = (u64 ? 32 : 16) + 8;
                int row_count_p;
                if (_in_(page.type, PAGE_MIX)) {
                    row_count_p = row_count_fp;
                    //# skip subheader pointers
                    base = base + page.subh_count * (u64 ? 24 : 12);
                    base = base + base % 8;
                } else {
                    row_count_p = read_int(page.data, u64 ? 34 : 18, 2).shortValue();
                }
                //# round up to 8-byte boundary	
                base = ((base + 7) / 8) * 8 + base % 8;
                if (row_count_p > row_count) {
                    row_count_p = row_count;
                }
                for (int _row_ : seq(row + 1, row + row_count_p)) {
                    for (Column col : col_info) {
                        int off = base + col.offset;
                        if (col.length > 0) {
                            RVector<Byte> raw = read_raw(page.data, off, col.length);
                            if (col.type == NUMERIC && col.length < 8) {
                                raw = c(as.raw(rep(0x00, 8 - col.length)), raw);
                                col.length = 8;
                            }
                            _data_.get(col.name).set(_row_, readBin(raw, col.type, 1, col.length));
                            if (col.type == CHARACTER) {
                                //# Apply encoding
                                _data_.get(col.name).set(_row_, encoding(_data_.get(col.name).get(_row_), encoding));
                                //# Strip beginning and trailing spaces
                                _data_.get(col.name).set(_row_, gsub("^ +| +$", "", _data_.get(col.name).get(_row_)));
                            }
                        }
                    }
                    base = base + row_length;
                    /*[>]*/ row++;/*[<]*/

                }
            }

            if (row != row_count /*[>]*/ && _callback_.reportInvalidRecordsCount(row, row_count)/*[<]*/) {
                warning(paste("found", row, "records where", row_count,
                        "expected", BUGREPORT));
            }
        }

        if (close_con) {
            close(con);
        }

        RFrame data = RAs.data.frame(_data_);
        attr(data, "pkg.version", VERSION);
        attr(data, "column.info", col_info);
        attr(data, "date.created", datecreated);
        attr(data, "date.modified", datemodified);
        attr(data, "SAS.release", SAS_release);
        attr(data, "SAS.host", SAS_host);
        attr(data, "OS.version", OS_version);
        attr(data, "OS.maker", OS_maker);
        attr(data, "OS.name", OS_name);
        attr(data, "endian", endian);
        attr(data, "winunix", winunix);
        if (debug) {
            attr(data, "debug", "sys.frame(1)");
        }
        /*[>]*/ attr(data, "missingHeader", _missingHeader_);/*[<]*/
 /*[>]*/ attr(data, "rowsInfo", _rowsInfo_);/*[<]*/

        return data;
    }

    //<editor-fold defaultstate="collapsed" desc="Custom structures">
    @lombok.Value
    public static final class MissingHeader {

        String name;
        String fileType;
        int pageCount;
        int pageSize;
        boolean u64;
        boolean littleEndian;
    }

    @lombok.Value
    public static final class RowsInfo {

        int row_length;
        int row_count;
        int row_count_fp;
    }

    static final class Page {

        short type;
        short subh_count;
        RVector<Byte> data;
        int page;
        String type_strng;
        short blck_count;
    }

    static final class SubHeader {

        int page;
        int offset;
        int length;
        RVector<Byte> raw;
        RVector<Byte> signature;
    }

    static final class ColumnName {

        String name;
    }

    static final class ColumnLabelFormat {

        String format;
        int fhdr;
        int foff;
        int flen;
        String label;
        int lhdr;
        int loff;
        int llen;
    }

    static final class ColumnAttribute {

        int offset;
        int length;
        int _type_;
        DataType type;
    }

    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.ToString
    @lombok.EqualsAndHashCode
    public static final class Column {

        public String name;
        public int offset;
        public int length;
        public DataType type;
        public String format;
        public String label;
    }

    public static class Callback {

        public boolean parseData(RList<Column> columns, int row_count) {
            return true;
        }

        public boolean ignoreUnknownPage(short pageType) {
            return false;
        }

        public boolean reportInvalidRecordsCount(int expected, int found) {
            return true;
        }
    }

    private static RUtils.RFunc<ColumnName, String> function_x_x_name() {
        return new RUtils.RFunc<ColumnName, String>() {
            @Override
            public String apply(ColumnName input) {
                return input.name;
            }
        };
    }

    private static Column co(ColumnName name, ColumnAttribute attr, ColumnLabelFormat lab) {
        Column result = new Column();
        result.name = name.name;
        result.offset = attr.offset;
        result.length = attr.length;
        result.type = attr.type;
        result.label = lab.label;
        result.format = lab.format;
        return result;
    }
    //</editor-fold>
}
