package io.elasticore.demo.rules;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A small TypeScript literal writer: objects, arrays, strings, numbers and booleans, indented. */
final class Js {

    private enum Kind { OBJECT, ARRAY, STRING, NUMBER, BOOLEAN }

    private final Kind kind;
    private final String scalar;
    private final Map<String, Js> props;
    private final List<Js> items;

    private Js(Kind kind, String scalar) {
        this.kind = kind;
        this.scalar = scalar;
        this.props = kind == Kind.OBJECT ? new LinkedHashMap<>() : null;
        this.items = kind == Kind.ARRAY ? new ArrayList<>() : null;
    }

    static Js object() { return new Js(Kind.OBJECT, null); }
    static Js array() { return new Js(Kind.ARRAY, null); }
    static Js str(String s) { return new Js(Kind.STRING, s == null ? "" : s); }
    static Js num(String n) { return new Js(Kind.NUMBER, n); }
    static Js bool(boolean b) { return new Js(Kind.BOOLEAN, String.valueOf(b)); }

    static Js strings(List<String> values) {
        Js a = array();
        for (String v : values) a.add(str(v));
        return a;
    }

    Js put(String key, Js value) {
        props.put(key, value);
        return this;
    }

    Js add(Js value) {
        items.add(value);
        return this;
    }

    /** On one line: for small objects such as { "": "발굴", en: "Prospecting" }. */
    String renderInline() {
        switch (kind) {
            case OBJECT: {
                if (props.isEmpty()) return "{}";
                StringBuilder b = new StringBuilder("{ ");
                int i = 0;
                for (Map.Entry<String, Js> e : props.entrySet()) {
                    if (i++ > 0) b.append(", ");
                    b.append(key(e.getKey())).append(": ").append(e.getValue().renderInline());
                }
                return b.append(" }").toString();
            }
            case ARRAY: {
                StringBuilder b = new StringBuilder("[");
                for (int i = 0; i < items.size(); i++) {
                    if (i > 0) b.append(", ");
                    b.append(items.get(i).renderInline());
                }
                return b.append("]").toString();
            }
            default:
                return render(0);
        }
    }

    String render(int depth) {
        switch (kind) {
            case STRING: return quote(scalar);
            case NUMBER:
            case BOOLEAN: return scalar;
            case ARRAY: {
                if (items.isEmpty()) return "[]";
                boolean simple = true;
                for (Js j : items) if (j.kind == Kind.OBJECT || j.kind == Kind.ARRAY) simple = false;
                if (simple) return renderInline();
                StringBuilder b = new StringBuilder("[\n");
                for (Js j : items) b.append(pad(depth + 1)).append(j.render(depth + 1)).append(",\n");
                return b.append(pad(depth)).append("]").toString();
            }
            default: {
                if (props.isEmpty()) return "{}";
                boolean small = props.size() <= 3;
                for (Js j : props.values()) if (j.kind == Kind.OBJECT && !j.props.isEmpty() || j.kind == Kind.ARRAY) small = false;
                if (small) return renderInline();
                StringBuilder b = new StringBuilder("{\n");
                for (Map.Entry<String, Js> e : props.entrySet()) {
                    b.append(pad(depth + 1)).append(key(e.getKey())).append(": ").append(e.getValue().render(depth + 1)).append(",\n");
                }
                return b.append(pad(depth)).append("}").toString();
            }
        }
    }

    private static String key(String k) {
        return k.matches("[A-Za-z_$][A-Za-z0-9_$]*") ? k : quote(k);
    }

    private static String pad(int depth) {
        return "    ".repeat(depth);
    }

    static String quote(String s) {
        StringBuilder b = new StringBuilder("\"");
        for (char ch : s.toCharArray()) {
            switch (ch) {
                case '"': b.append("\\\""); break;
                case '\\': b.append("\\\\"); break;
                case '\n': b.append("\\n"); break;
                case '\r': b.append("\\r"); break;
                case '\t': b.append("\\t"); break;
                default:
                    if (ch < 0x20) b.append(String.format("\\u%04x", (int) ch));
                    else b.append(ch);
            }
        }
        return b.append('"').toString();
    }
}
