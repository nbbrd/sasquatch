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
package sasquatch.ri;

import sasquatch.SasColumnType;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
enum SasCommonFormats {
    // DATES
    B8601DA(SasColumnType.DATE),
    DATE(SasColumnType.DATE),
    DAY(SasColumnType.DATE),
    DDMMYY(SasColumnType.DATE),
    DDMMYYB(SasColumnType.DATE),
    DDMMYYC(SasColumnType.DATE),
    DDMMYYD(SasColumnType.DATE),
    DDMMYYN(SasColumnType.DATE),
    DDMMYYP(SasColumnType.DATE),
    DDMMYYS(SasColumnType.DATE),
    DOWNAME(SasColumnType.DATE),
    E8601DA(SasColumnType.DATE),
    JULDAY(SasColumnType.DATE),
    JULIAN(SasColumnType.DATE),
    MMDDYY(SasColumnType.DATE),
    MMDDYYC(SasColumnType.DATE),
    MMDDYYD(SasColumnType.DATE),
    MMDDYYN(SasColumnType.DATE),
    MMDDYYP(SasColumnType.DATE),
    MMDDYYS(SasColumnType.DATE),
    MMYY(SasColumnType.DATE),
    MMYYC(SasColumnType.DATE),
    MMYYD(SasColumnType.DATE),
    MMYYN(SasColumnType.DATE),
    MMYYP(SasColumnType.DATE),
    MMYYS(SasColumnType.DATE),
    MONNAME(SasColumnType.DATE),
    MONTH(SasColumnType.DATE),
    MONYY(SasColumnType.DATE),
    WEEKDATE(SasColumnType.DATE),
    WEEKDATX(SasColumnType.DATE),
    WEEKDAY(SasColumnType.DATE),
    WORDDATE(SasColumnType.DATE),
    WORDDATX(SasColumnType.DATE),
    YEAR(SasColumnType.DATE),
    YYMM(SasColumnType.DATE),
    YYMMC(SasColumnType.DATE),
    YYMMD(SasColumnType.DATE),
    YYMMDD(SasColumnType.DATE),
    YYMMDDB(SasColumnType.DATE),
    YYMMDDC(SasColumnType.DATE),
    YYMMDDD(SasColumnType.DATE),
    YYMMDDN(SasColumnType.DATE),
    YYMMDDP(SasColumnType.DATE),
    YYMMDDS(SasColumnType.DATE),
    YYMMN(SasColumnType.DATE),
    YYMMP(SasColumnType.DATE),
    YYMMS(SasColumnType.DATE),
    YYMON(SasColumnType.DATE),
    // DATETIMES
    B8601DN(SasColumnType.DATETIME),
    B8601DT(SasColumnType.DATETIME),
    B8601DX(SasColumnType.DATETIME),
    B8601DZ(SasColumnType.DATETIME),
    B8601LX(SasColumnType.DATETIME),
    DATEAMPM(SasColumnType.DATETIME),
    DATETIME(SasColumnType.DATETIME),
    DTDATE(SasColumnType.DATETIME),
    DTMONYY(SasColumnType.DATETIME),
    DTWKDATX(SasColumnType.DATETIME),
    DTYEAR(SasColumnType.DATETIME),
    E8601DN(SasColumnType.DATETIME),
    E8601DT(SasColumnType.DATETIME),
    E8601DX(SasColumnType.DATETIME),
    E8601DZ(SasColumnType.DATETIME),
    E8601LX(SasColumnType.DATETIME),
    MDYAMPM(SasColumnType.DATETIME),
    TOD(SasColumnType.DATETIME),
    // TIMES
    TIME(SasColumnType.TIME);

    @lombok.Getter
    private final SasColumnType columnType;
}
