/**
 * Product as the screens see it: its fields (kind, label in each language,
 * limits, references, search), its lifecycles and who may do what.
 *
 * Generated from the DSL by the CRM's own ElastiCORE rules (crm-ui-rules). Change the
 * DSL, not this file: the generic list, form, detail and Excel export read it.
 */
export const ProductUi = {
    name: "Product",
    root: true,
    label: "상품",
    labels: { en: "Product" },
    path: "/api/crm/product",
    key: "productId",
    defaultSort: "name+",
    pageSize: 20,
    editable: true,
    terms: ["item", "offering", "제품", "품목"],
    access: { read: "public" },
    ui: { icon: "package", menu: "catalog", order: 90 },
    fields: [
        {
            name: "productId",
            kind: "integer",
            label: "상품 번호",
            labels: { en: "Product no." },
            key: true,
            generated: true,
            readOnly: true,
        },
        {
            name: "code",
            kind: "string",
            label: "상품 코드",
            labels: { en: "Product code" },
            required: true,
            maxLength: 20,
            search: "eq",
        },
        {
            name: "name",
            kind: "string",
            label: "상품명",
            labels: { en: "Product name" },
            required: true,
            maxLength: 100,
            search: "like",
        },
        {
            name: "category",
            kind: "enum",
            label: "분류",
            labels: { en: "Category" },
            enum: "ProductCategory",
            required: true,
            search: "in",
        },
        {
            name: "price",
            kind: "money",
            label: "단가",
            labels: { en: "Unit price" },
            required: true,
            search: "between",
            terms: ["list price", "정가"],
        },
        {
            name: "active",
            kind: "boolean",
            label: "판매 중",
            labels: { en: "On sale" },
            search: "eq",
            default: "true",
        },
        {
            name: "description",
            kind: "text",
            label: "설명",
            labels: { en: "Description" },
        },
    ],
    lifecycles: [],
} as const;

export default ProductUi;
