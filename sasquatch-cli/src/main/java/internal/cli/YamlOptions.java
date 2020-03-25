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
import java.io.IOException;
import java.io.Writer;
import java.nio.ByteOrder;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;
import picocli.CommandLine;
import picocli.ext.CharOptions;

/**
 *
 * @author Philippe Charles
 */
@lombok.Getter
@lombok.Setter
public class YamlOptions {

    @CommandLine.ArgGroup
    private CharOptions.Output output = new CharOptions.Output();

    public void dump(Class<?> rootType, Object item) throws IOException {
        try (Writer writer = output.newCharWriter(Optional::empty)) {
            getYaml(rootType).dump(item, writer);
        }
    }

    public void dumpAll(Class<?> rootType, List<?> items) throws IOException {
        try (Writer writer = output.newCharWriter(Optional::empty)) {
            getYaml(rootType).dump(items, writer);
        }
    }

    public Yaml getYaml(Class<?> rootType) {

        Representer representer = new Representer() {
            {
                this.representers.put(PValue.class, data -> representScalar(Tag.STR, toShortString((PValue) data)));
                this.representers.put(LocalDateTime.class, data -> representScalar(Tag.STR, ((LocalDateTime) data).toString()));
                this.representers.put(StringRef.class, data -> representScalar(Tag.STR, toShortString((StringRef) data)));
                this.representers.put(ByteOrder.class, data -> representScalar(Tag.STR, ((ByteOrder) data).toString()));
                this.representers.put(SubHeaderLocation.class, data -> representScalar(Tag.STR, toShortString((SubHeaderLocation) data)));
                this.representers.put(ColText.class, data -> representMapping(Tag.MAP, asMapping((ColText) data), DumperOptions.FlowStyle.AUTO));
            }
        };

        representer.addClassTag(rootType, Tag.MAP);

        representer.setPropertyUtils(new PropertyUtils() {
            @Override
            protected Set<Property> createPropertySet(Class<? extends Object> type, BeanAccess bAccess) {
                return getPropertiesMap(type, BeanAccess.FIELD)
                        .values()
                        .stream()
                        .filter(property -> property.isReadable() && (isAllowReadOnlyProperties() || property.isWritable()))
                        .collect(Collectors.toCollection(LinkedHashSet::new));
            }
        });

        DumperOptions opts = new DumperOptions();
        opts.setAllowReadOnlyProperties(true);

        return new Yaml(representer, opts);
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
