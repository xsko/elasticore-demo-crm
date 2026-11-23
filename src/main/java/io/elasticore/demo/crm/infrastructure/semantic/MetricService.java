package io.elasticore.demo.crm.infrastructure.semantic;

import io.elasticore.springboot3.security.CurrentUser;
import io.elasticore.springboot3.security.AccessPolicy;
import io.elasticore.springboot3.security.SessionCurrentUser;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The business's numbers as the DSL defines them ({@code metrics:} - exported to the Ossie
 * model), worked out on the database for the member who asks.
 *
 * <p>The metric's SQL is the generator's, never a request's; a request only names a metric
 * and, to break it down, one column the model declares. Where the entity's rows belong to
 * an owner ({@code @owner}, {@code read=REP&owner}), a member who is not the administrator
 * counts only their own - the same rows the generated search would give them.
 */
@Service
public class MetricService {

    public record Value(String metric, String description, Object value) {}

    public record Bucket(Object key, Object value) {}

    private final SemanticModel model;
    private final NamedParameterJdbcTemplate jdbc;
    private final CurrentUser currentUser = new SessionCurrentUser();

    public MetricService(SemanticModel model, NamedParameterJdbcTemplate jdbc) {
        this.model = model;
        this.jdbc = jdbc;
    }

    public Value value(String metric) {
        SemanticModel.Metric m = metric(metric);
        SemanticModel.Dataset ds = model.dataset(m.dataset());
        MapSqlParameterSource p = new MapSqlParameterSource();
        String sql = "SELECT " + m.sql() + " FROM " + ds.table() + where(ds, p);
        Object v = jdbc.queryForObject(sql, p, Object.class);
        return new Value(m.name(), m.description(), number(v));
    }

    /** The metric for each value of one of the entity's columns (pipeline by stage). */
    public List<Bucket> by(String metric, String column) {
        SemanticModel.Metric m = metric(metric);
        SemanticModel.Dataset ds = model.dataset(m.dataset());
        SemanticModel.Field f = ds.field(column);
        if (f == null || !f.column().matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new io.elasticore.springboot3.validation.BadRequestException(m.dataset() + " has no column " + column);
        }
        MapSqlParameterSource p = new MapSqlParameterSource();
        String sql = "SELECT " + f.column() + " AS k, " + m.sql() + " AS v FROM " + ds.table() + where(ds, p)
                + " GROUP BY " + f.column() + " ORDER BY " + f.column();
        List<Bucket> out = new ArrayList<>();
        jdbc.query(sql, p, rs -> {
            out.add(new Bucket(rs.getObject("k"), number(rs.getObject("v"))));
        });
        return out;
    }

    /** Every metric with its description and synonyms: what the assistant and the dashboard can ask for. */
    public List<Map<String, Object>> catalog() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (SemanticModel.Metric m : model.metrics().values()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", m.name());
            row.put("entity", m.dataset());
            row.put("description", m.description());
            row.put("synonyms", m.synonyms());
            out.add(row);
        }
        return out;
    }

    private SemanticModel.Metric metric(String name) {
        SemanticModel.Metric m = model.metrics().get(name);
        if (m == null) throw new EntityNotFoundException("No metric " + name);
        return m;
    }

    /**
     * The dataset's read access as the generated services check it - ElastiCORE's own
     * AccessPolicy over the @access the Ossie model carries (read=REP&owner, read=owner|REP):
     * 401 for a visitor, 403 for a member it does not let in, and the owner filter the search
     * would apply.
     */
    private String where(SemanticModel.Dataset ds, MapSqlParameterSource p) {
        StringBuilder declaration = new StringBuilder();
        ds.access().forEach((op, entry) -> declaration.append(declaration.length() == 0 ? "" : ", ").append(op).append('=').append(entry));
        AccessPolicy policy = AccessPolicy.parse(ds.name(), declaration.toString(), "ADMIN");
        policy.require(currentUser, "read");
        Object owner = policy.ownerFilter(currentUser, "read");
        if (owner == null || ds.ownerColumn() == null) return "";
        p.addValue("me", owner);
        return " WHERE " + ds.ownerColumn() + " = :me";
    }

    private static Object number(Object v) {
        if (v == null) return 0;
        if (v instanceof BigDecimal b) return b.stripTrailingZeros().scale() <= 0 ? b.toBigInteger() : b;
        return v;
    }
}
