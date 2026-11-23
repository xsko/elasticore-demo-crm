package io.elasticore.demo.crm.adapter.in.assistant;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Any entity's generated use case, by name: search, read one, create. Every call goes through
 * the generated service as the member signed in to this request, so @access, @owner and the
 * masking of the answer apply exactly as to the REST API - the assistant can never see more
 * than the member could. The values come back as the API answers them (masked JSON).
 */
@Component
public class RecordAccess {

    private static final String BASE = "io.elasticore.demo.crm";

    public record Rows(long total, List<Map<String, Object>> rows) {}

    private final ApplicationContext context;
    private final ObjectMapper json;

    public RecordAccess(ApplicationContext context, ObjectMapper json) {
        this.context = context;
        this.json = json;
    }

    public Rows search(String entity, Map<String, Object> filters, String sortCode, int limit) {
        Class<?> srch = type(".application.dto." + entity + "SrchDTO");
        Map<String, Object> body = new LinkedHashMap<>(filters == null ? Map.of() : filters);
        body.put("pageNumber", 0);
        body.put("pageSize", Math.max(1, Math.min(limit, 50)));
        if (sortCode != null && !sortCode.isBlank()) body.put("sortCode", sortCode);
        Object condition = json.convertValue(body, srch);
        Page<?> page = (Page<?>) call(entity, "findBySearch", srch, condition);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Object dto : page.getContent()) rows.add(asJson(dto));
        return new Rows(page.getTotalElements(), rows);
    }

    public Map<String, Object> get(String entity, Object id) {
        Method m = method(entity, "findById", null);
        Object key = json.convertValue(id, m.getParameterTypes()[0]);
        Object found = invoke(m, useCase(entity), key);
        if (found instanceof Optional<?> o) found = o.orElse(null);
        if (found == null) throw new EntityNotFoundException(entity + " " + id + " was not found");
        return asJson(found);
    }

    public Map<String, Object> create(String entity, Map<String, Object> values) {
        Class<?> dto = type(".application.dto." + entity + "DTO");
        Object saved = call(entity, "save", dto, json.convertValue(values, dto));
        return asJson(saved);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asJson(Object dto) {
        return json.convertValue(dto, Map.class);
    }

    private Object call(String entity, String name, Class<?> arg, Object value) {
        return invoke(method(entity, name, arg), useCase(entity), value);
    }

    private Object useCase(String entity) {
        return context.getBean(type(".application.port.in." + entity + "UseCase"));
    }

    private Method method(String entity, String name, Class<?> arg) {
        Class<?> uc = type(".application.port.in." + entity + "UseCase");
        for (Method m : uc.getMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == 1 && (arg == null || m.getParameterTypes()[0] == arg)) return m;
        }
        throw new EntityNotFoundException(entity + " has no " + name);
    }

    private static Class<?> type(String suffix) {
        String name = BASE + suffix;
        if (!name.matches("[a-zA-Z0-9.]+")) throw new EntityNotFoundException("No such type");
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new EntityNotFoundException("No entity " + suffix.substring(suffix.lastIndexOf('.') + 1));
        }
    }

    private static Object invoke(Method m, Object target, Object arg) {
        try {
            return m.invoke(target, arg);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException re) throw re;
            throw new IllegalStateException(e.getCause());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
