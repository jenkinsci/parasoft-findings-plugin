/*
 * MIT License
 *
 * Copyright (c) 2018 Shenyu Zheng and other Jenkins contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.parasoft.findings.jenkins.coverage.api.metrics.steps;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import edu.hm.hafner.util.FilteredLog;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.Fraction;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import com.parasoft.findings.jenkins.coverage.model.ClassNode;
import com.parasoft.findings.jenkins.coverage.model.ContainerNode;
import com.parasoft.findings.jenkins.coverage.model.Coverage;
import com.parasoft.findings.jenkins.coverage.model.CyclomaticComplexity;
import com.parasoft.findings.jenkins.coverage.model.FileNode;
import com.parasoft.findings.jenkins.coverage.model.FractionValue;
import com.parasoft.findings.jenkins.coverage.model.LinesOfCode;
import com.parasoft.findings.jenkins.coverage.model.MethodNode;
import com.parasoft.findings.jenkins.coverage.model.Metric;
import com.parasoft.findings.jenkins.coverage.model.ModuleNode;
import com.parasoft.findings.jenkins.coverage.model.Node;
import com.parasoft.findings.jenkins.coverage.model.PackageNode;
import com.parasoft.findings.jenkins.coverage.model.Value;

import hudson.util.XStream2;

import io.jenkins.plugins.util.AbstractXmlStream;

/**
 * Configures the XML stream for the coverage tree, which consists of {@link Node}s.
 */
@SuppressWarnings("PMD.CouplingBetweenObjects")
class CoverageXmlStream extends AbstractXmlStream<Node> {
    private static final Collector<CharSequence, ?, String> ARRAY_JOINER = Collectors.joining(", ", "[", "]");

    private static String[] toArray(final String value) {
        String cleanInput = StringUtils.removeEnd(StringUtils.removeStart(StringUtils.deleteWhitespace(value), "["), "]");

        return StringUtils.split(cleanInput, ",");
    }

    /**
     * Creates an XML stream for {@link Node}.
     */
    CoverageXmlStream() {
        super(Node.class);
    }

    @Override
    protected void configureXStream(final XStream2 xStream) {
        registerConverters(xStream);

        xStream.alias("container", ContainerNode.class);
        xStream.alias("module", ModuleNode.class);
        xStream.alias("package", PackageNode.class);
        xStream.alias("file", FileNode.class);
        xStream.alias("class", ClassNode.class);
        xStream.alias("method", MethodNode.class);

        xStream.registerLocalConverter(FileNode.class, "coveredPerLine", new IntegerLineMapConverter());
        xStream.registerLocalConverter(FileNode.class, "missedPerLine", new IntegerLineMapConverter());

        xStream.registerLocalConverter(FileNode.class, "changedLines", new IntegerSetConverter());
        xStream.registerLocalConverter(FileNode.class, "coverageDelta", new MetricFractionMapConverter());
    }

    static void registerConverters(final XStream2 xStream) {
        xStream.alias("com.parasoft.findings.jenkins.coverage.model.Metric", Metric.class);

        xStream.alias("com.parasoft.findings.jenkins.coverage.model.Coverage", Coverage.class);
        xStream.addImmutableType(Coverage.class, false);
        xStream.alias("com.parasoft.findings.jenkins.coverage.model.CyclomaticComplexity", CyclomaticComplexity.class);
        xStream.addImmutableType(CyclomaticComplexity.class, false);
        xStream.alias("com.parasoft.findings.jenkins.coverage.model.LinesOfCode", LinesOfCode.class);
        xStream.addImmutableType(LinesOfCode.class, false);
        xStream.alias("com.parasoft.findings.jenkins.coverage.model.FractionValue", FractionValue.class);
        xStream.addImmutableType(FractionValue.class, false);

        xStream.registerConverter(new FractionConverter());
        xStream.registerConverter(new SimpleConverter<>(Value.class, Value::serialize, Value::valueOf));
        xStream.registerConverter(new SimpleConverter<>(Metric.class, Metric::name, Metric::valueOf));
    }

    @Override
    protected Node createDefaultValue() {
        return new ModuleNode("Empty");
    }

    /**
     * {@link Converter} for {@link Fraction} instances so that only the values will be serialized. After reading the
     * values back from the stream, the string representation will be converted to an actual instance again.
     */
    static final class FractionConverter implements Converter {
        @SuppressWarnings("PMD.NullAssignment")
        @Override
        public void marshal(final Object source, final HierarchicalStreamWriter writer,
                final MarshallingContext context) {
            writer.setValue(source instanceof Fraction ? ((Fraction) source).toProperString() : null);
        }

        @Override
        public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
            return Fraction.getFraction(reader.getValue());
        }

        @Override
        public boolean canConvert(final Class type) {
            return type == Fraction.class;
        }
    }

    /**
     * {@link Converter} for a {@link TreeMap} of coverage percentages per metric. Stores the mapping in the condensed
     * format {@code metric1: numerator1/denominator1, metric2: numerator2/denominator2, ...}.
     */
    static final class MetricFractionMapConverter extends TreeMapConverter<Metric, Fraction> {
        @Override
        protected Function<Entry<Metric, Fraction>, String> createMapEntry() {
            return e -> String.format("%s: %s", e.getKey().name(), e.getValue().toProperString());
        }

        @Override
        protected Entry<Metric, Fraction> createMapping(final String key, final String value) {
            return entry(Metric.valueOf(key), Fraction.getFraction(value));
        }
    }

    /**
     * {@link Converter} for {@link Coverage} instances so that only the values will be serialized. After reading the
     * values back from the stream, the string representation will be converted to an actual instance again.
     *
     * @param <T> type of the objects that will be marshalled and unmarshalled
     */
    public static class SimpleConverter<T> implements Converter {
        private final Class<T> type;
        private final Function<T, String> marshaller;
        private final Function<String, Object> unmarshaller;

        protected SimpleConverter(final Class<T> type, final Function<T, String> marshaller, final Function<String, Object> unmarshaller) {
            this.type = type;
            this.marshaller = marshaller;
            this.unmarshaller = unmarshaller;
        }

        @SuppressWarnings("PMD.NullAssignment")
        @Override
        public void marshal(final Object source, final HierarchicalStreamWriter writer,
                final MarshallingContext context) {
            writer.setValue(type.isInstance(source) ? marshaller.apply(type.cast(source)) : null);
        }

        @Override
        public final Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
            return unmarshaller.apply(reader.getValue());
        }

        @Override
        public final boolean canConvert(final Class clazz) {
            return type.isAssignableFrom(clazz);
        }
    }

    /**
     * {@link Converter} base class for {@link TreeMap} instance. Stores the mappings in a condensed format
     * {@code key1: value1, key2: value2, ...}.
     *
     * @param <K>
     *         the type of keys maintained by this map
     * @param <V>
     *         the type of mapped values
     */
    abstract static class TreeMapConverter<K extends Comparable<K>, V> implements Converter {
        @Override
        @SuppressWarnings({"PMD.NullAssignment", "unchecked"})
        public void marshal(final Object source, final HierarchicalStreamWriter writer,
                final MarshallingContext context) {
            writer.setValue(source instanceof NavigableMap ? marshal((NavigableMap<K, V>) source) : null);
        }

        String marshal(final SortedMap<K, V> source) {
            return source.entrySet()
                    .stream()
                    .map(createMapEntry())
                    .collect(ARRAY_JOINER);
        }

        @Override
        public boolean canConvert(final Class type) {
            return type == TreeMap.class;
        }

        @Override
        public NavigableMap<K, V> unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
            return unmarshal(reader.getValue());
        }

        NavigableMap<K, V> unmarshal(final String value) {
            FilteredLog log = new FilteredLog("Errors during reading coverage XML:");
            NavigableMap<K, V> map = new TreeMap<>();

            for (String marshalledValue : toArray(value)) {
                if (StringUtils.contains(marshalledValue, ":")) {
                    try {
                        Entry<K, V> entry = createMapping(
                                StringUtils.substringBefore(marshalledValue, ':'),
                                StringUtils.substringAfter(marshalledValue, ':'));
                        map.put(entry.getKey(), entry.getValue());
                    }
                    catch (IllegalArgumentException exception) { // parasoft-suppress OWASP2021.A9.LGE "This is intentionally designed to ensure exceptions during coverage report processing don't cause the build to fail."
                        log.logError("Failed to read coverage XML due to an exception: %s", ExceptionUtils.getRootCauseMessage(exception));
                    }
                }
            }
            return map;
        }

        protected abstract Function<Entry<K, V>, String> createMapEntry();

        protected abstract Map.Entry<K, V> createMapping(String key, String value);

        protected SimpleEntry<K, V> entry(final K key, final V value) {
            return new SimpleEntry<>(key, value);
        }
    }

    /**
     * {@link Converter} for a {@link SortedMap} of coverages per line. Stores the mapping in the condensed format
     * {@code key1: covered1/missed1, key2: covered2/missed2, ...}.
     */
    static final class IntegerLineMapConverter extends TreeMapConverter<Integer, Integer> {
        @Override
        protected Function<Entry<Integer, Integer>, String> createMapEntry() {
            return e -> String.format("%d: %d", e.getKey(), e.getValue());
        }

        @Override
        protected Entry<Integer, Integer> createMapping(final String key, final String value) {
            return entry(Integer.valueOf(key), Integer.valueOf(value));
        }
    }

    /**
     * {@link Converter} for a {@link TreeSet} of integers that serializes just the values. After
     * reading the values back from the stream, the string representation will be converted to an actual instance
     * again.
     */
    static final class IntegerSetConverter implements Converter {
        @SuppressWarnings({"PMD.NullAssignment", "unchecked"})
        @Override
        public void marshal(final Object source, final HierarchicalStreamWriter writer,
                final MarshallingContext context) {
            writer.setValue(source instanceof TreeSet ? marshal((TreeSet<Integer>) source) : null);
        }

        String marshal(final Set<Integer> lines) {
            return lines.stream().map(String::valueOf).collect(ARRAY_JOINER);
        }

        @Override
        public NavigableSet<Integer> unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
            return unmarshal(reader.getValue());
        }

        NavigableSet<Integer> unmarshal(final String value) {
            return Arrays.stream(toArray(value)).map(Integer::valueOf).collect(Collectors.toCollection(TreeSet::new));
        }

        @Override
        public boolean canConvert(final Class type) {
            return type == TreeSet.class;
        }
    }
}
