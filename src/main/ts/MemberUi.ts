/**
 * Member as the screens see it: its fields (kind, label in each language,
 * limits, references, search), its lifecycles and who may do what.
 *
 * Generated from the DSL by the CRM's own ElastiCORE rules (crm-ui-rules). Change the
 * DSL, not this file: the generic list, form, detail and Excel export read it.
 */
export const MemberUi = {
    name: "Member",
    root: true,
    label: "회원",
    labels: { en: "Member" },
    path: "/api/crm/member",
    key: "memberId",
    defaultSort: "name+",
    pageSize: 20,
    editable: true,
    terms: ["user", "사용자", "회원"],
    access: { create: "public", read: "owner", update: "owner" },
    owner: "memberId",
    ui: {
        icon: "users",
        menu: "admin",
        order: 100,
        admin: true,
    },
    fields: [
        {
            name: "memberId",
            kind: "integer",
            label: "회원 번호",
            labels: { en: "Member no." },
            key: true,
            generated: true,
            readOnly: true,
        },
        {
            name: "login",
            kind: "string",
            label: "로그인 ID",
            labels: { en: "Login ID" },
            required: true,
            maxLength: 30,
            search: "eq",
        },
        {
            name: "name",
            kind: "string",
            label: "이름",
            labels: { en: "Name" },
            required: true,
            maxLength: 50,
            search: "like",
        },
        {
            name: "email",
            kind: "string",
            label: "이메일",
            labels: { en: "E-mail" },
            required: true,
            maxLength: 100,
            search: "eq",
        },
        {
            name: "phone",
            kind: "string",
            label: "전화번호",
            labels: { en: "Phone" },
            maxLength: 20,
            search: "eq",
            encrypted: true,
        },
        {
            name: "company",
            kind: "string",
            label: "소속 회사",
            labels: { en: "Company" },
            maxLength: 100,
        },
        {
            name: "role",
            kind: "enum",
            label: "역할",
            labels: { en: "Role" },
            enum: "MemberRole",
            adminOnly: true,
            default: "MemberRole.CUSTOMER",
        },
        {
            name: "joinedAt",
            kind: "datetime",
            label: "가입 일시",
            labels: { en: "Joined at" },
            readOnly: true,
            default: "now",
        },
    ],
    lifecycles: [],
} as const;

export default MemberUi;
