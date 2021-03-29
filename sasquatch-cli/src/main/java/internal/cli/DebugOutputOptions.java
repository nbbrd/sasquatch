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

import internal.bytes.PValue;
import internal.ri.base.Encoding;
import internal.ri.base.SubHeaderLocation;
import internal.ri.data.ColText;
import internal.ri.data.StringRef;
import nbbrd.console.picocli.yaml.YamlOutput;
import nbbrd.console.picocli.yaml.YamlOutputOptions;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.nio.ByteOrder;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Philippe Charles
 */
@lombok.Data
public class DebugOutputOptions extends YamlOutputOptions {

    public void dump(Class<?> rootType, Object item) throws IOException {
        dump(toYaml(rootType), item);
    }

    public void dumpAll(Class<?> rootType, List<?> items) throws IOException {
        dumpAll(toYaml(rootType), items);
    }

    private static Yaml toYaml(Class<?> rootType) {
        DumperOptions opts = new DumperOptions();
        opts.setAllowReadOnlyProperties(true);
        return new Yaml(getRepresenter(rootType), opts);
    }

    private static Representer getRepresenter(Class<?> rootType) {
        Representer result = new Representer() {
            {
                this.representers.put(PValue.class, data -> representScalar(Tag.STR, toShortString((PValue) data)));
                this.representers.put(LocalDateTime.class, data -> representScalar(Tag.STR, ((LocalDateTime) data).toString()));
                this.representers.put(StringRef.class, data -> representScalar(Tag.STR, toShortString((StringRef) data)));
                this.representers.put(ByteOrder.class, data -> representScalar(Tag.STR, ((ByteOrder) data).toString()));
                this.representers.put(SubHeaderLocation.class, data -> representScalar(Tag.STR, toShortString((SubHeaderLocation) data)));
                this.representers.put(ColText.class, data -> representMapping(Tag.MAP, asMapping((ColText) data), DumperOptions.FlowStyle.AUTO));
            }
        };
        result.addClassTag(rootType, Tag.MAP);
        result.setPropertyUtils(YamlOutput.newLinkedPropertyUtils());
        result.getPropertyUtils().setBeanAccess(BeanAccess.FIELD);
        return result;
    }

    private static String toShortString(PValue<?, ?> pv) {
        if (pv.isKnown()) {
            Object obj = pv.get();
            if (obj instanceof Encoding) {
                return ((Encoding) obj).getCharsetName();
            }
        }
        return pv.toString();
    }

    private static String toShortString(StringRef ref) {
        return ref.getLen() > 0
                ? ref.getHdr() + "+" + ref.getOff() + ":" + ref.getLen()
                : "";
    }

    private static String toShortString(SubHeaderLocation location) {
        return location.getPage() + "/" + location.getIndex();
    }

    private static Map<?, ?> asMapping(ColText colText) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("location", colText.getLocation());
        result.put("length", colText.getContent().getLength());
        return result;
    }
}
