/**
 * QuoteLine as the screens see it: its fields (kind, label in each language,
 * limits, references, search), its lifecycles and who may do what.
 *
 * Generated from the DSL by the CRM's own ElastiCORE rules (crm-ui-rules). Change the
 * DSL, not this file: the generic list, form, detail and Excel export read it.
 */
export const QuoteLineUi = {
    name: "QuoteLine",
    root: false,
    label: "견적 품목",
    labels: { en: "Quote line" },
    fields: [
        {
            name: "lineId",
            kind: "integer",
            label: "품목 번호",
            labels: { en: "Line no." },
            key: true,
            generated: true,
            readOnly: true,
        },
        {
            name: "product",
            kind: "ref",
            label: "상품",
            labels: { en: "Product" },
            ref: { entity: "Product", idField: "productId", nameField: "productName" },
            required: true,
        },
        {
            name: "quantity",
            kind: "quantity",
            label: "수량",
            labels: { en: "Quantity" },
            required: true,
        },
        {
            name: "unitPrice",
            kind: "money",
            label: "단가",
            labels: { en: "Unit price" },
            readOnly: true,
            derived: true,
        },
        {
            name: "subTotal",
            kind: "money",
            label: "금액",
            labels: { en: "Amount" },
            readOnly: true,
            derived: true,
        },
        {
            name: "productName",
            kind: "string",
            label: "상품명",
            labels: { en: "Product name" },
            readOnly: true,
            derived: true,
            display: true,
        },
    ],
    lifecycles: [],
} as const;

export default QuoteLineUi;
