package io.elasticore.demo.crm.infrastructure.semantic;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Apache Ossie semantic model ElastiCORE exports from the DSL
 * ({@code blueprint/semantic_model.yml}), read at start-up: each entity's table, columns,
 * descriptions, synonyms (with the labels in every language), metrics and the
 * ELASTICORE extension (owner, access, states).
 *
 * <p>The dashboard and the AI assistant read the business's numbers and words from here, so
 * a metric or a synonym added to the DSL reaches both with the next build.
 */
@Component
public class SemanticModel {

    public record Field(String name, String column, String datatype, String description, List<String> synonyms, String context) {}

    public record Metric(String name, String dataset, String sql, String description, List<String> synonyms) {}

    public record Dataset(String name, String table, String description, List<String> synonyms, List<Field> fields,
                          List<Metric> metrics, String ownerField, Map<String, String> access) {

        public Field field(String nameOrColumn) {
            for (Field f : fields) {
                if (f.name().equalsIgnoreCase(nameOrColumn) || f.column().equalsIgnoreCase(nameOrColumn)
                        || f.name().equalsIgnoreCase(snake(nameOrColumn))) return f;
            }
            return null;
        }

        /** The column that holds the row's owner, when the DSL names one (@owner). */
        public String ownerColumn() {
            if (ownerField == null) return null;
            Field f = field(ownerField);
            return f == null ? null : f.column();
        }
    }

    private final Map<String, Dataset> datasets = new LinkedHashMap<>();
    private final Map<String, Metric> metrics = new LinkedHashMap<>();
    private String name = "";
    private String instructions = "";

    public SemanticModel() throws IOException {
        this(new ClassPathResource("blueprint/semantic_model.yml"));
    }

    SemanticModel(ClassPathResource resource) throws IOException {
        if (!resource.exists()) return;
        try (InputStream in = resource.getInputStream()) {
            load(new Yaml(new SafeConstructor(new LoaderOptions())).load(in));
        }
    }

    @SuppressWarnings("unchecked")
    private void load(Object root) {
        if (!(root instanceof Map<?, ?> doc)) return;
        name = str(doc.get("name"));
        if (doc.get("ai_context") instanceof Map<?, ?> ai) instructions = str(ai.get("instructions"));
        for (Object o : list(doc.get("datasets"))) {
            if (!(o instanceof Map<?, ?> ds)) continue;
            String dsName = str(ds.get("name"));
            List<Field> fields = new ArrayList<>();
            for (Object fo : list(ds.get("fields"))) {
                if (!(fo instanceof Map<?, ?> f)) continue;
                fields.add(new Field(str(f.get("name")), sqlOf(f.get("expression")), str(f.get("datatype")),
                        str(f.get("description")), synonyms(f.get("ai_context")), context(f.get("ai_context"))));
            }
            List<Metric> ms = new ArrayList<>();
            for (Object mo : list(ds.get("metrics"))) {
                if (!(mo instanceof Map<?, ?> m)) continue;
                Metric metric = new Metric(str(m.get("name")), dsName, sqlOf(m.get("expression")),
                        str(m.get("description")), synonyms(m.get("ai_context")));
                ms.add(metric);
                metrics.put(metric.name(), metric);
            }
            String owner = null;
            Map<String, String> access = new LinkedHashMap<>();
            for (Object eo : list(ds.get("nonportable_extensions"))) {
                if (eo instanceof Map<?, ?> ext && "ELASTICORE".equals(ext.get("vendor_name")) && ext.get("data") instanceof Map<?, ?> data) {
                    owner = str(data.get("owner"));
                    if (data.get("access") instanceof Map<?, ?> a) a.forEach((k, v) -> access.put(str(k), str(v)));
                }
            }
            datasets.put(dsName, new Dataset(dsName, str(ds.get("source")), str(ds.get("description")),
                    synonyms(ds.get("ai_context")), fields, ms, owner == null || owner.isBlank() ? null : owner, access));
        }
    }

    public String name() { return name; }

    public String instructions() { return instructions; }

    public Map<String, Dataset> datasets() { return Collections.unmodifiableMap(datasets); }

    public Map<String, Metric> metrics() { return Collections.unmodifiableMap(metrics); }

    public Dataset dataset(String name) { return datasets.get(name); }

    private static String sqlOf(Object expression) {
        if (expression instanceof Map<?, ?> e) {
            if (e.get("sql") != null) return str(e.get("sql"));
            for (Object d : list(e.get("dialects"))) {
                if (d instanceof Map<?, ?> dm && dm.get("expression") != null) return str(dm.get("expression"));
            }
        }
        return expression == null ? "" : str(expression);
    }

    private static List<String> synonyms(Object aiContext) {
        if (aiContext instanceof Map<?, ?> ai && ai.get("synonyms") instanceof List<?> l) {
            List<String> out = new ArrayList<>();
            for (Object o : l) out.add(str(o));
            return out;
        }
        return List.of();
    }

    /** The ai_context written as text (an enumeration's codes and their meaning), or "". */
    private static String context(Object aiContext) {
        if (aiContext instanceof String s) return s;
        if (aiContext instanceof Map<?, ?> m && m.get("instructions") != null) return str(m.get("instructions"));
        return "";
    }

    private static List<?> list(Object o) {
        return o instanceof List<?> l ? l : List.of();
    }

    private static String str(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    static String snake(String camel) {
        return camel.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase(java.util.Locale.ROOT);
    }
}
