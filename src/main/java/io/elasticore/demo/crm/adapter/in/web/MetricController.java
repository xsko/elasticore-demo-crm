package io.elasticore.demo.crm.adapter.in.web;

import io.elasticore.demo.crm.infrastructure.semantic.MetricService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * The DSL's metrics over HTTP, for the dashboard: GET /api/crm/metrics lists them,
 * GET /api/crm/metrics/{name} answers one, ?by=stage breaks it down by a column. In the
 * package of the generated controllers so the generated error handler answers its 401/403/404.
 */
@RestController
@RequestMapping("/api/crm/metrics")
public class MetricController {

    private final MetricService metrics;

    public MetricController(MetricService metrics) {
        this.metrics = metrics;
    }

    @GetMapping
    public List<Map<String, Object>> catalog() {
        return metrics.catalog();
    }

    @GetMapping("/{name}")
    public Object value(@PathVariable("name") String name, @RequestParam(name = "by", required = false) String by) {
        return by == null || by.isBlank() ? metrics.value(name) : metrics.by(name, by);
    }
}
