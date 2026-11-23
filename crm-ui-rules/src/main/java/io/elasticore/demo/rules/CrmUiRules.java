package io.elasticore.demo.rules;

import io.elasticore.base.model.pub.rules.ArtifactContributor;
import io.elasticore.base.model.pub.rules.CodeRules;
import io.elasticore.base.model.pub.rules.ContributedArtifact;
import io.elasticore.base.model.pub.rules.EntitySpec;
import io.elasticore.base.model.pub.rules.FieldSpec;
import io.elasticore.base.model.pub.rules.ModelCheck;
import io.elasticore.base.model.pub.rules.Rule;
import io.elasticore.base.model.pub.rules.SourceCode;
import io.elasticore.base.model.pub.rules.Syntax;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static io.elasticore.base.model.pub.rules.Ctx.ctx;

/**
 * What the screens need from the DSL, written beside the generated TypeScript client.
 *
 * <p>The generated client already carries each DTO's types, labels, limits and
 * validators. It does not say which kind of input a field wants, which entity a
 * reference points to, which fields can be searched and how, where a lifecycle can go,
 * or who may do what. This rule set writes that for every entity into
 * {@code src/main/ts/<Entity>Ui.ts}, so the generic list, form, detail and Excel export
 * follow the DSL with no screen code per entity: the screens collect every
 * {@code *Ui.ts} at build time, so a new entity gets its menu entry and screens too.
 *
 * <p>(One file per entity: a new entity is a new file the screens pick up by glob.)
 *
 * <p>The enumerations need nothing of it: ElastiCORE writes each one's labels in every
 * language ({@code XxxLabel}, and {@code XxxLabels} from the enum's {@code label_xx} columns).
 *
 * <p>A screen marker of the CRM's own, {@code @ui(...)}, refines the defaults:
 * {@code @ui(hidden)} keeps a field off lists and forms, {@code @ui(list=false)} off
 * the list only, {@code @ui(width=160)} sets the column width, {@code @ui(widget=textarea)}
 * the input, {@code @ui(title)} names the field that titles a row, and on the entity
 * line {@code @ui(menu=sales, order=20, icon=building)} places it in the menu.
 *
 * <p>Every label must have a translation for each language the domain lists in
 * {@code config: crm.languages} of {@code env.yml}: one missing stops the build.
 */
public class CrmUiRules implements CodeRules, ArtifactContributor {

    static final String TARGET = "crmUi";

    @Override
    public List<ContributedArtifact> artifactsFor(EntitySpec e) {
        if (exposed(e) || part(e)) {
            return Collections.singletonList(ContributedArtifact.of(TARGET, "dto", e.getName() + "Ui"));
        }
        return Collections.emptyList();
    }

    // ---- one entity as the screens see it ----------------------------------------------

    @Rule(value = "crm-ui-model", target = TARGET)
    void uiModel(EntitySpec e) {
        SourceCode c = ctx(e);
        c.requireSyntaxAnyOf("crm-ui-model", Syntax.TYPESCRIPT);
        List<String> languages = languages(e);

        Js out = Js.object();
        out.put("name", Js.str(e.getName()));
        out.put("root", Js.bool(exposed(e)));
        labels(out, e.getName(), labelOf(e), e.labelTranslations(), languages);
        if (exposed(e)) {
            out.put("path", Js.str(endpointOf(e)));
            String key = keyOf(e);
            if (key != null) out.put("key", Js.str(key));
            String sort = e.metaValue("sort");
            if (sort == null || sort.isBlank()) sort = e.defaultSort();
            if (sort != null && !sort.isBlank()) out.put("defaultSort", Js.str(sort));
            String pageSize = pageSizeOf(e);
            if (pageSize != null) out.put("pageSize", Js.num(pageSize));
            out.put("editable", Js.bool(e.hasMeta("editable")));
            List<String> terms = splitList(e.metaValue("terms"));
            if (!terms.isEmpty()) out.put("terms", Js.strings(terms));

            Js access = Js.object();
            for (Map.Entry<String, String> a : accessOf(e).entrySet()) access.put(a.getKey(), Js.str(a.getValue()));
            out.put("access", access);
            String owner = e.metaValue("owner");
            if (owner != null && !owner.isBlank()) out.put("owner", Js.str(owner));
            Js ui = hints(e.metaProperties("ui"), e.metaFlags("ui"));
            if (ui != null) out.put("ui", ui);
        }

        out.put("fields", fields(e, languages));
        out.put("lifecycles", lifecycles(e));

        c.var("className", e.getName() + "Ui");
        c.var("entityName", e.getName());
        c.var("model", out.render(0));
    }

    private Js lifecycles(EntitySpec e) {
        Js list = Js.array();
        for (EntitySpec.StateMachine m : e.stateMachines()) {
            Js lc = Js.object();
            lc.put("field", Js.str(m.field()));
            if (m.initial() != null) lc.put("initial", Js.str(m.initial()));
            Js moves = Js.array();
            for (EntitySpec.Move mv : m.moves()) {
                Js move = Js.object();
                move.put("command", Js.str(mv.command()));
                move.put("from", Js.strings(mv.from()));
                move.put("to", Js.str(mv.to()));
                moves.add(move);
            }
            lc.put("moves", moves);
            if (m.deletable() != null) lc.put("deletableIn", Js.strings(m.deletable()));
            list.add(lc);
        }
        return list;
    }

    private Js fields(EntitySpec e, List<String> languages) {
        Js list = Js.array();
        Set<String> names = new LinkedHashSet<>();
        for (FieldSpec f : e.fields()) names.add(f.name());
        Set<String> identities = new LinkedHashSet<>();
        for (FieldSpec f : e.fields()) {
            if (isReference(e, f)) {
                String id = identityOf(e, f);
                if (id != null && names.contains(id)) identities.add(id);
            }
        }
        // The blind index an @encrypt @search(eq) field keeps beside it (phoneBidx) is no field
        // of the record: the API never answers it (FieldSpec.blindIndexOf, ElastiCORE #290).
        for (FieldSpec f : e.fields()) {
            if (identities.contains(f.name())) continue;           // shown through its reference
            if (f.blindIndexOf() != null) continue;
            if (f.hasDsl("password") || f.hasDsl("writeonly") || f.isVersion() || f.isParentField()) continue;
            if (isReference(e, f) && part(e) && e.containedIn() != null && e.containedIn().equals(f.typeName())) continue;
            list.add(field(e, f, languages));
        }
        // The answer's extra fields (@ref names of what a row points to), from the DTO.
        for (FieldSpec f : e.dtoFields()) {
            if (names.contains(f.name()) || !f.hasDsl("ref")) continue;
            Js field = Js.object();
            field.put("name", Js.str(f.name()));
            field.put("kind", Js.str("string"));
            labels(field, e.getName() + "." + f.name(), f.label(), f.labelTranslations(), languages);
            field.put("readOnly", Js.bool(true));
            field.put("derived", Js.bool(true));
            field.put("display", Js.bool(true));
            list.add(field);
        }
        return list;
    }

    private Js field(EntitySpec e, FieldSpec f, List<String> languages) {
        Js out = Js.object();
        out.put("name", Js.str(f.name()));
        String kind = kindOf(e, f);
        out.put("kind", Js.str(kind));
        labels(out, e.getName() + "." + f.name(), f.label(), f.labelTranslations(), languages);

        if ("enum".equals(kind)) out.put("enum", Js.str(f.typeName()));
        if ("ref".equals(kind)) {
            Js ref = Js.object();
            ref.put("entity", Js.str(f.typeName()));
            String id = identityOf(e, f);
            if (id != null) ref.put("idField", Js.str(id));
            String display = displayFieldOf(e, f);
            if (display != null) ref.put("nameField", Js.str(display));
            out.put("ref", ref);
        }
        if ("list".equals(kind) || "embedded".equals(kind)) out.put("of", Js.str(f.coreItemType() != null ? f.coreItemType() : f.typeName()));

        if (f.required()) out.put("required", Js.bool(true));
        int max = f.validationMaxLength();
        if (max > 0 && f.isStringType()) out.put("maxLength", Js.num(String.valueOf(max)));
        String search = searchOf(f);
        if (search != null) out.put("search", Js.str(search));
        if (f.hasDsl("id")) out.put("key", Js.bool(true));
        if (f.hasDslAny("sequence", "genid")) out.put("generated", Js.bool(true));
        if (readOnly(e, f)) out.put("readOnly", Js.bool(true));
        if (lockedAfterCreate(e, f)) out.put("createOnly", Js.bool(true));
        if (f.hasDslAny("compute", "snapshot", "stamp")) out.put("derived", Js.bool(true));
        if (f.hasDsl("encrypt")) out.put("encrypted", Js.bool(true));
        String env = f.dslValue("env");
        if (env != null && env.toLowerCase(Locale.ROOT).startsWith("mask")) out.put("masked", Js.bool(true));
        if ("admin".equals(f.dslValue("access"))) out.put("adminOnly", Js.bool(true));
        String deflt = f.dslValue("default");
        if (deflt != null && !deflt.isBlank()) out.put("default", Js.str(deflt.replace("\"", "")));
        List<String> terms = splitList(f.dslValue("terms"));
        if (!terms.isEmpty()) out.put("terms", Js.strings(terms));

        Js ui = fieldHints(f);
        if (ui != null) out.put("ui", ui);
        return out;
    }

    // ---- helpers -------------------------------------------------------------------

    /** The page size of @expose(20), @expose(20, methods="...") or @expose(pageSize=20): null when none is given. */
    private static String pageSizeOf(EntitySpec e) {
        String named = e.metaProperty("expose", "pageSize");
        if (named != null && named.trim().matches("\\d+")) return named.trim();
        for (String flag : e.metaFlags("expose")) {
            if (flag != null && flag.trim().matches("\\d+")) return flag.trim();
        }
        String value = e.metaValue("expose");
        return value != null && value.trim().matches("\\d+") ? value.trim() : null;
    }

    static boolean exposed(EntitySpec e) {
        return e.aggregateRoot() && e.hasMeta("expose");
    }

    /** A member of an aggregate (a quote's lines) or an embeddable value: shown inside its root. */
    static boolean part(EntitySpec e) {
        return !e.aggregateRoot() && (e.containedIn() != null || e.hasMeta("embeddable"));
    }

    private static boolean isReference(EntitySpec e, FieldSpec f) {
        return !f.isList() && (f.isEntityRef() || (e.isKnownEntity(f.typeName()) && !e.isEmbeddableType(f.typeName())));
    }

    /** The generated controller's base path: {apiPath}/{domain}/{entity with a small first letter}. */
    private static String endpointOf(EntitySpec e) {
        String api = e.apiPath() == null ? "/api" : e.apiPath().replaceAll("/+$", "");
        String n = e.getName();
        return api + "/" + e.domainName() + "/" + Character.toLowerCase(n.charAt(0)) + n.substring(1);
    }

    private static String keyOf(EntitySpec e) {
        List<FieldSpec> pk = e.pkFields();
        return pk.isEmpty() ? null : pk.get(0).name();
    }

    private static String labelOf(EntitySpec e) {
        String text = e.metaValue("label");
        if (text != null && !text.isBlank() && !text.contains("=")) return text.trim();
        return null;
    }

    /** The label, its translations, and a check that every listed language has one. */
    private static void labels(Js out, String where, String label, Map<String, String> translations, List<String> languages) {
        String own = label == null || label.isBlank() ? null : label.trim();
        out.put("label", Js.str(own == null ? where.substring(where.lastIndexOf('.') + 1) : own));
        Js t = Js.object();
        if (translations != null) {
            for (Map.Entry<String, String> x : translations.entrySet()) t.put(x.getKey(), Js.str(x.getValue()));
        }
        out.put("labels", t);
        if (own != null) {
            for (String lang : languages) {
                if (translations == null || !translations.containsKey(lang)) {
                    ModelCheck.fail(where + " has no label in " + lang + ": add @label(" + lang + "=...) to its line"
                            + " (env.yml lists " + languages + " in crm.languages)");
                }
            }
        }
    }

    private static List<String> languages(EntitySpec e) {
        String listed = e.config("crm.languages");
        if (listed == null || listed.isBlank()) return Collections.emptyList();
        List<String> out = new ArrayList<>();
        for (String s : listed.replace("[", "").replace("]", "").split(",")) {
            if (!s.isBlank()) out.add(languageTag(s.trim()));
        }
        return out;
    }

    private static String kindOf(EntitySpec e, FieldSpec f) {
        if (f.isList()) return "list";
        if (e.isEmbeddableType(f.typeName())) return "embedded";
        if (isReference(e, f)) return "ref";
        if (e.isKnownEnum(f.typeName())) return "enum";
        if (f.isMoney()) return "money";
        if (f.isQuantity()) return "quantity";
        // The type as the DSL wrote it: long, string, money(15.2), date, timestamp ...
        String t = f.initTypeName() == null ? f.typeName() : f.initTypeName();
        String type = t == null ? "" : t.toLowerCase(Locale.ROOT);
        if (f.hasDslAny("text", "clob") || type.startsWith("text") || type.startsWith("clob")) return "text";
        if (type.startsWith("bool")) return "boolean";
        if (type.startsWith("timestamp") || type.startsWith("instant")) return "timestamp";
        if (f.isDateTime() || type.startsWith("datetime") || type.startsWith("localdatetime")) return "datetime";
        if (f.isDate() || type.equals("date") || type.startsWith("localdate")) return "date";
        if (type.startsWith("time") || type.startsWith("localtime")) return "time";
        if (type.startsWith("int") || type.startsWith("long") || type.startsWith("biginteger")) return "integer";
        if (type.startsWith("decimal") || type.startsWith("bigdecimal") || type.startsWith("double") || type.startsWith("float")) return "decimal";
        if (type.startsWith("json")) return "json";
        return "string";
    }

    /** The identity a request and an answer carry for a reference: owner: Member -> ownerMemberId. */
    private static String identityOf(EntitySpec e, FieldSpec f) {
        String key = e.keyFieldOf(f.typeName());
        if (key == null || key.isBlank()) key = e.pkFieldOf(f.typeName());
        if (key == null || key.isBlank()) return null;
        return key.startsWith(f.name()) ? key : f.name() + Character.toUpperCase(key.charAt(0)) + key.substring(1);
    }

    /** The DTO's @ref field that names what this reference points to: owner -> ownerName. */
    private static String displayFieldOf(EntitySpec e, FieldSpec ref) {
        for (FieldSpec d : e.dtoFields()) {
            String path = d.dslValue("ref");
            if (path != null && path.startsWith(ref.name() + ".")) return d.name();
        }
        return null;
    }

    private static String searchOf(FieldSpec f) {
        if (!f.hasDslAny("search", "s")) return null;
        String s = f.dslValueAny("search", "s");
        return s == null || s.isBlank() ? "eq" : s.trim();
    }

    /** Never taken from a request: the server sets it. */
    private static boolean readOnly(EntitySpec e, FieldSpec f) {
        if (f.hasDslAny("readonly", "behavior", "stamp", "compute", "snapshot", "noinput", "sequence", "genid")) return true;
        for (EntitySpec.StateMachine m : e.stateMachines()) if (m.field().equals(f.name())) return true;
        return false;
    }

    /** On a root split for its behaviour, a field without @editable is given once, when the row is made. */
    private static boolean lockedAfterCreate(EntitySpec e, FieldSpec f) {
        return exposed(e) && !e.hasMeta("editable") && !f.hasDsl("editable") && !readOnly(e, f) && !f.isList();
    }

    /** A field's @ui(...): its key=value pairs and its bare flags (ElastiCORE #273). */
    private static Js fieldHints(FieldSpec f) {
        if (!f.hasDsl("ui")) return null;
        Js ui = hints(f.dslProperties("ui"), f.dslFlags("ui"));
        return ui == null ? Js.object() : ui;
    }

    /** @ui(menu=sales, order=30, board) -> { menu: "sales", order: 30, board: true }. */
    private static Js hints(Map<String, String> pairs, List<String> flags) {
        if ((pairs == null || pairs.isEmpty()) && (flags == null || flags.isEmpty())) return null;
        Js ui = Js.object();
        if (pairs != null) {
            for (Map.Entry<String, String> p : new java.util.TreeMap<>(pairs).entrySet()) {
                String v = p.getValue() == null ? "" : p.getValue().trim();
                ui.put(p.getKey().trim(), v.matches("-?\\d+") ? Js.num(v)
                        : ("true".equals(v) || "false".equals(v)) ? Js.bool(Boolean.parseBoolean(v)) : Js.str(v));
            }
        }
        if (flags != null) for (String flag : flags) if (!flag.isBlank()) ui.put(flag.trim(), Js.bool(true));
        return ui;
    }
    /** read=owner, create=user ... (the administrator's for each the line does not name). */
    private static Map<String, String> accessOf(EntitySpec e) {
        Map<String, String> out = new LinkedHashMap<>();
        String text = e.metaText("access");
        if (text == null) return out;
        String body = text.trim();
        int open = body.indexOf('(');
        int close = body.lastIndexOf(')');
        if (open >= 0 && close > open) body = body.substring(open + 1, close);
        for (String part : body.split(",")) {
            int eq = part.indexOf('=');
            if (eq > 0) out.put(part.substring(0, eq).trim(), part.substring(eq + 1).trim());
        }
        return out;
    }

    private static List<String> splitList(String v) {
        if (v == null || v.isBlank()) return Collections.emptyList();
        List<String> out = new ArrayList<>();
        for (String s : v.split(",")) if (!s.isBlank()) out.add(s.trim());
        return out;
    }

    /** En -> en, ZhTW -> zh-TW, Pt_BR -> pt-BR. */
    static String languageTag(String suffix) {
        String s = suffix.replace('_', '-');
        if (s.contains("-")) {
            String[] p = s.split("-", 2);
            return p[0].toLowerCase(Locale.ROOT) + "-" + p[1].toUpperCase(Locale.ROOT);
        }
        if (s.length() == 4 && Character.isUpperCase(s.charAt(2))) {
            return s.substring(0, 2).toLowerCase(Locale.ROOT) + "-" + s.substring(2).toUpperCase(Locale.ROOT);
        }
        return s.toLowerCase(Locale.ROOT);
    }

}
