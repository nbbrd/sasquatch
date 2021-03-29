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
package internal.ri.base;

import internal.bytes.PValue;

/**
 *
 * @author Philippe Charles
 */
public enum PageType {

    META,
    DATA,
    MIX,
    AMD,
    METC,
    @XRef(var = "comp")
    INDEX;

    public boolean hasMeta() {
        return this == META || this == MIX || this == AMD;
    }

    public static PValue<PageType, Short> tryParse(short value) {
        switch (value) {
            case 0:      //0b00000000_00000000
            case 128:    //0b00000000_10000000
                return PValue.known(META);
            case 256:    //0b00000001_00000000
            case 384:    //0b00000001_10000000
                return PValue.known(DATA); //#1<<8,1<<8|1<<7
            case 512:    //0b00000010_00000000
            case 640:    //0b00000010_10000000
                return PValue.known(MIX); //#1<<9,1<<9|1<<7
            case 1024:   //0b00000100_00000000
                return PValue.known(AMD); //#1<<10
            case 16384:  //0b01000000_00000000
                return PValue.known(METC); //#1<<14 (compressed data)
            case -28672: //0b10010000_00000000 FIXME: short overflow -> need unsigned short ?
                return PValue.known(INDEX); //#~(1<<14|1<<13|1<<12)
        }
        return PValue.unknown(value);
    }
}
