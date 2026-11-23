/**
 * KnowledgeArticle as the screens see it: its fields (kind, label in each language,
 * limits, references, search), its lifecycles and who may do what.
 *
 * Generated from the DSL by the CRM's own ElastiCORE rules (crm-ui-rules). Change the
 * DSL, not this file: the generic list, form, detail and Excel export read it.
 */
export const KnowledgeArticleUi = {
    name: "KnowledgeArticle",
    root: true,
    label: "도움말",
    labels: { en: "Help article" },
    path: "/api/crm/knowledgeArticle",
    key: "articleId",
    defaultSort: "title+",
    pageSize: 20,
    editable: true,
    terms: ["faq", "help", "guide", "도움말", "자주 묻는 질문"],
    access: { read: "public" },
    ui: { icon: "book-open", menu: "service", order: 80 },
    fields: [
        {
            name: "articleId",
            kind: "integer",
            label: "문서 번호",
            labels: { en: "Article no." },
            key: true,
            generated: true,
            readOnly: true,
        },
        {
            name: "title",
            kind: "string",
            label: "제목",
            labels: { en: "Title" },
            required: true,
            maxLength: 200,
            search: "like",
        },
        {
            name: "body",
            kind: "text",
            label: "본문",
            labels: { en: "Body" },
            required: true,
        },
        {
            name: "category",
            kind: "enum",
            label: "주제",
            labels: { en: "Topic" },
            enum: "TicketCategory",
            search: "in",
        },
        {
            name: "keywords",
            kind: "string",
            label: "검색어",
            labels: { en: "Keywords" },
            maxLength: 300,
            search: "like",
        },
        {
            name: "published",
            kind: "boolean",
            label: "게시",
            labels: { en: "Published" },
            search: "eq",
            default: "true",
        },
    ],
    lifecycles: [],
} as const;

export default KnowledgeArticleUi;
