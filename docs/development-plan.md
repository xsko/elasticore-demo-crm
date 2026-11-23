# ElastiCORE 3 CRM 데모 (elasticore-demo-crm) 개발 계획

## 2026-11-22 갱신 — ElastiCORE 3 정식판으로 마무리

처음 보고한 ElastiCORE 이슈 9건(#266~#274)이 ElastiCORE 3 정식판에 들어가,
데모를 그 판으로 올리고 아래 계획의 우회 부분을 생성 기능으로 바꿨습니다. 아래 본문은 처음 계획 그대로 둡니다.

| 처음 계획 | 지금 |
|---|---|
| enum 상수 라벨은 `labelEn` 열 + `crm-ui-rules` 가 `XxxLabels` 생성 | enum 의 `label_en` 열을 ElastiCORE 가 TS(`XxxLabels`)·라벨 번들·Ossie(`code_labels`)·거절 문구에 씀. 규칙의 enum 부분 삭제 |
| 직원 전용 화면은 손으로 쓴 `StaffOnly` 검사 + `@ui(staff=true)` | `@access(read=REP&owner, create=REP, answer=REP ...)`. 영업 담당이 문의를 모두 보고 답변 |
| "조건 결과 전체" Excel 은 자체 `ExcelExportService` + `POST /api/crm/export/{Entity}` | `@expose(20, methods="...,export")` 가 만드는 `POST /api/crm/{entity}/export` (.xlsx). enum 칸은 코드 → 라벨 선택지 요청(#289) |
| 가린 값은 폼에서 비워 두고 입력할 때만 전송 | 가린 값을 그대로 보여 주고 그대로 보내면 저장값 유지(#270) |
| 거절 문구의 상태는 코드(`PRS`) | `labelLanguage: ko` + `{2}`~`{5}`: "단계가 발굴일 때는 수주로 바꿀 수 없습니다" |
| `@ui(board=true)` (값 없는 표식이 사라져서) | `@ui(board, pipeline)` — `metaFlags`/`dslFlags` 로 읽음 |
| 지표 권한은 `read=owner` 등 단순 수준만 해석 | ElastiCORE 런타임 `AccessPolicy` 로 같은 식을 해석 |
| 시험은 손으로만 작성 (v3 Java 번들에는 생성 시험이 없음) | `testCode: "contract"`(#295)가 상태 흐름·제약·`@access` 시험 361개를 생성. `CrmContractSupport.request(...)`에 XSRF 쿠키와 헤더를 넣었고, 견적 품목 수량 시험 1개는 손으로 추가 |
| 화면 입력 검사는 필수·길이만, 가입 화면은 DSL 비밀번호 규칙을 베껴 둠 | 생성된 `validateXxxCodes`로 DSL의 모든 제약을 브라우저에서 검사하고, 코드마다 화면 문구로 표시(`web/src/form/codes.ts`) |

새로 보고: #289 (내보내기 enum 라벨), #290 (규칙 API 에서 blind index 필드 구분). 둘 다 정식판에 들어가 적용했습니다.

## Context
- **목표:** ElastiCORE **3**으로 만든 CRM 데모를 React 화면까지 갖춰 공개합니다.
- **보여 줄 것:** DSL 한 곳을 고치면 서버, 화면, 다국어 라벨, Ossie 의미 모델, AI 상담 챗봇이 함께 바뀌는 구조입니다.
- **참고만 할 것:** jmix-crm(Jmix + Vaadin, B2B 주문·청구·결제)은 기획 수준만 참고합니다. 도메인 구성, 화면 흐름, 디자인은 다르게 만듭니다.
- **이슈 보고:** ElastiCORE를 쓰다가 부족하거나 버그가 나오면 우회하지 않고 https://github.com/xsko/ElastiCORE 에 이슈로 보고합니다.

### 1차 조사 결과 (근거)
- **버전 ElastiCORE 3** (Maven Central)
  - 플러그인 `id 'io.elasticore'` (판은 `gradle.properties`)
  - 라이브러리 `io.elasticore:elasticore-base`, `io.elasticore.springboot4:elasticore-springboot4`
  - 기반: Boot 4.0.8, JDK 17, `architecture='hexagonal'`
- **이번 판에서 새로 쓸 기능** (모두 선택 사항)
  - **다국어 라벨** `@label(en=..., ja=...) -- 기본 언어 라벨`: 필드 줄과 entity·DTO·enumeration의 `meta:` 줄에 씁니다.
    - 서버: `i18n/<domain>-labels.properties`와 `_en` 같은 언어별 파일을 생성합니다. 키는 `Type`, `Type.field`입니다. `MessageSource`가 요청 Locale로 읽습니다.
    - TS: `XxxFields.f.labels = { en: "..." }`. 번역이 없으면 `label`(-- 문구)을 씁니다.
    - Ossie: 번역이 `ai_context.synonyms`에 들어갑니다. 그래서 영어로 물어도 같은 필드를 찾습니다.
    - **안 되는 것:** enum 상수에는 `@label`을 못 씁니다. 상수 라벨은 enum 열로 모델링합니다.
    - **문서에 없는 것:** TS에 entity·enum 라벨이 나오는지 문서에 없습니다.
  - **`@encrypt`**: AES-256-GCM. `@search(eq)`면 blind index로 같음 검색이 됩니다.
  - **`@Mask(PHONE|EMAIL|NAME|CARD…)`**: 응답만 가립니다.
  - **`@signin(refresh=password)`**: 비밀번호를 바꾸면 이전 세션을 끝냅니다.
  - **오류 메시지 언어:** `Accept-Language` 순서대로 고르고, 없으면 `spring.web.locale`을 씁니다. 서버 OS 언어는 쓰지 않습니다.
  - **필수 설정:** `spring.messages.basename: messages,i18n/crm-labels`, `spring.messages.fallback-to-system-locale: false`. 이게 없으면 서버 언어 파일로 잘못 넘어갑니다.
- **생성되는 REST:** `{apiPath}/{domain}/{entity}`
  - `POST /search`: 요청은 `pageNumber`(0부터)·`pageSize`·`sortCode "name+,amount-"`, 응답은 Spring `Page`
  - 그 밖에 `GET /{id}`, `POST /`, `PUT /`, `DELETE /{id}`, `POST /{id}/{command}`(상태 전환)
  - 오류는 RFC 9457 + `fieldErrors`
- **TS 클라이언트(`typescriptClient=true`):**
  - `XxxApi`, `XxxDTO`, `XxxFields`(label·labels·required·maxLength·min·max·readOnly), `validateXxx`, enum 라벨, `canX()`
  - **화면 생성은 없습니다.** 필드 형식·순서·참조 대상·검색 필드는 TS 메타에 없습니다.
- **보안:**
  - `@signin`+`@login`+`@password`(PBKDF2)로 세션 쿠키 로그인과 `/signin`·`/signout`·`/me`가 생성됩니다.
  - `@access`(public/user/owner/admin)와 `@owner`는 CoreService에서 강제됩니다.
  - **회원가입은 생성되지 않습니다.** petstore는 `AccountController`를 손으로 씁니다.
- **팀 규칙 확장(`elasticore-rule-dev-guide.md`):**
  - 별도 규칙 모듈 + `DomainArtifactContributor`로 새 파일을 만들 수 있습니다.
  - `EntitySpec`/`FieldSpec`(`typeName`, `isEntityRef`, `dslValue`, **`labelTranslations()`**)으로 DSL을 읽습니다.
  - → **DSL→화면 메타 생성기는 이 규칙 모듈로 만듭니다.**
- **Ossie:** Apache Ossie core spec `0.3.0.dev0`, 변환기는 `OssieSemanticModelExporter`(`rules=['ossie-export']`)
- **참고 구현:**
  - `ElastiCORE/elasticore-project/elasticore-sample-petstore`: 다국어 라벨 예시도 이 샘플 기준입니다.
  - `xsko/elasticore-petstore`: rc1로 오래됨. CI·k8s 틀만 참고합니다.
- **대상 저장소:** `xsko/elasticore-demo-crm`은 비어 있습니다. `gh` CLI가 설치돼 있지 않습니다.

## 기획 — jmix-crm과 겹치지 않게
| 구분 | jmix-crm | 우리 CRM |
|---|---|---|
| 업무 초점 | 주문→청구→결제(재무) | **영업 기회 + 고객 상담(서비스)** |
| 중심 흐름 | 주문 단계 막대(New→Done) | 리드 → 전환 → 기회 칸반(단계 = DSL `states:`) → 견적 → 상담 티켓 |
| AI | 사내용 분석 대화 | **고객용 상담 챗봇** + 영업 담당 비서. 둘 다 DSL 의미 모델로 근거를 댑니다 |
| 화면 틀 | Vaadin 좌측 메뉴 | 상단 작업 공간 탭 + 접히는 좌측 아이콘 레일, 밀도 높은 표, 오른쪽 상세 서랍 |
| 색·느낌 | Lumo 기본 | 짙은 남색(ink) + 청록 포인트, 각진 6px 모서리, 이모지 없이 lucide 아이콘 |

**도메인 `crm` 엔티티 초안:**
| 엔티티 | 핵심 |
|---|---|
| Member | `@signin(refresh=password)`, 역할 ADMIN/MANAGER/REP/CUSTOMER, `phone @encrypt @Mask(PHONE)` |
| Account | 고객사. 담당자가 `@owner` |
| Contact | 사람. 이메일·전화는 `@encrypt @search(eq)` + `@Mask` |
| Lead | 상태 NEW→QUALIFIED→CONVERTED/LOST, `convert` 명령 |
| Opportunity | 단계 PROSPECT→PROPOSAL→NEGOTIATION→WON/LOST, `money` |
| Product | 상품 |
| Quote / QuoteLine | `@snapshot(product.price)`, `@compute(sum(lines.subTotal))` |
| Activity | CALL / MEETING / TASK |
| Ticket | 상담 건. OPEN→ANSWERED→CLOSED. CUSTOMER가 `@owner` |
| KnowledgeArticle | 챗봇 근거 문서. 공개 열람 |

`metrics:`에는 파이프라인 금액, 승률, 미해결 티켓, 이번 달 수주를 둡니다. `@terms`에는 동의어를 둡니다.

## 다국어 (ElastiCORE 3 방식 그대로)
- **데이터 라벨은 모두 DSL 한 곳에서 관리합니다.**
  - 기본 언어(`--`)는 한국어, `@label(en=...)`은 영어입니다. 예: `amount: money(15.2)! @label(en=Amount) -- 금액`
  - entity·DTO·enum의 `meta:` 줄에도 `@label(en=...)`을 붙입니다. 메뉴 이름과 화면 제목에 씁니다.
  - **enum 상수:** 상수에는 `@label`을 못 쓰므로 enum에 언어별 열을 둡니다. 예: `fields: {code, label, labelEn}`, `PROSPECT: PRS, 잠재, Prospect`
- **화면:** 고른 언어(ko/en)로 `XxxFields.f.labels[lang] ?? label`을 씁니다. 버튼·안내 같은 화면 고유 문구만 i18next `ko.json`/`en.json`에 둡니다.
- **서버:**
  - `spring.messages.basename: messages,i18n/crm-labels`
  - `fallback-to-system-locale: false`, `spring.web.locale: ko`
  - 화면 fetch는 언제나 `Accept-Language: <선택 언어>`를 보냅니다. 그래서 오류 메시지와 서버 라벨(Excel 머리글, 챗봇)이 같은 언어로 나옵니다.
- **오류 메시지 번역:** `crm-messages_ko.properties`에 둡니다(생성본 `crm-messages.properties`는 영어).
- **빠진 번역 검사:** `crm-ui-rules`가 `labelTranslations()`에 `en`이 없는 필드·엔티티를 찾아 `ModelCheck.fail`로 빌드를 멈춥니다. 지원 언어 목록은 env.yml `config("crm.languages")`에서 읽습니다.
- **처음에 확인할 것(spike):** TS에 entity·enum 라벨(`labels`)과 enum 추가 열이 나오는지 확인합니다. 안 나오면 이슈로 보고하고, 그동안은 `crm-ui-rules`가 `EntitySpec.labelTranslations()`로 직접 메타에 담습니다.

## 기술 선택
- **서버:**
  - Spring Boot 4.0.8, JDK 17, ElastiCORE 3
  - `elasticore { architecture='hexagonal'; typescriptClient=true; rules=['ossie-export', '<crm UI 규칙>'] }`
  - DB는 개발 H2, 운영 프로필 PostgreSQL
  - 암호화 키 `elasticore.crypto.key`는 환경 변수로 받습니다.
- **화면:** Vite + React 18 + TypeScript + **Refine + Ant Design 5**
  - Refine: 대중적이고 headless입니다. 데이터 공급자 하나만 쓰면 ElastiCORE REST에 그대로 맞습니다.
  - Ant Design: 서버 페이징 표가 강하고 ko/en 로케일을 기본 제공합니다.
- **표 그리드·Excel:** 아래 "HTML 표 그리드 설계"를 따릅니다.
- **AI:** Spring AI. Anthropic(Claude)과 OpenAI 호환(로컬 LLM) 중 설정으로 고릅니다. 키는 환경 변수로만 받습니다.

## DSL → 화면 한 번에 반영하는 기반 (핵심)
1. **`crm-ui-rules` 모듈**(ElastiCORE 팀 규칙)
   - `DomainArtifactContributor`로 `crmUiModel.ts`를 생성합니다.
   - 엔티티마다 담는 것: 라벨(언어별), 필드 순서·형식(string/text/money/date/enum/ref/List), 참조 엔티티와 표시 필드, 검색 필드와 연산자, 목록 열, 상태 명령, 접근 수준, 암호화·마스킹 여부
   - 사용자 표식 `@ui(list, width=120, widget=textarea, hidden)`을 둡니다.
   - **처음에 확인할 것:** 규칙 산출물을 TS 경로에 쓸 수 있는지 확인합니다. 안 되면 이슈로 보고하고, 같은 모듈의 Gradle 작업이 ElastiCORE 모델 로더로 JSON을 만듭니다.
2. **범용 화면 부품** `web/src/meta/`:
   - `EntityList`: 서버 페이징·정렬·검색 줄·열 선택·Excel
   - `EntityForm`: 형식별 위젯, `validateXxx`, 서버 `fieldErrors`
   - `EntityShow` 서랍: 연관 목록 탭
   - `CommandBar`: `canX()`로 버튼 활성화
   - 메뉴와 라우트는 `crmUiModel`에서 자동으로 만듭니다.
3. **직접 만드는 화면:** 대시보드(metrics), 기회 칸반(끌어다 놓으면 단계 명령 실행), Account 360, 챗봇 창
4. **반영 흐름:** DSL 수정 → `./gradlew build`(elcore3 → TS → UI 메타 → webBuild) → 새 필드가 목록·폼·Excel·라벨 번들·Ossie·챗봇 도구에 함께 나타납니다.
   - TS 형식 검사가 직접 만든 화면의 깨짐을 잡습니다.
   - 번역이 빠지면 빌드가 실패합니다.

## HTML 표 그리드 설계 (페이징 · Excel)

### 무엇을 쓰나 — Ant Design 5 `Table`을 `CrmGrid`로 감싸서
| 후보 | 실제 `<table>` | 서버 페이징 | Excel | 라이선스 | 판단 |
|---|---|---|---|---|---|
| **AntD Table** | O | O(`pagination` + `onChange`). Refine `@refinedev/antd` `useTable`이 그대로 연결 | 직접 구현 | MIT | **채택** |
| TanStack Table v8 | O (headless) | O(`manualPagination`) | 직접 구현 | MIT | 탈락. 같은 기능을 쓰려면 화면 코드가 더 많아집니다 |
| AG Grid | X (div 격자) | 서버 행 모델은 Enterprise | Excel은 Enterprise | 유료 | 탈락 |
| MUI DataGrid | X (div) | O | Excel은 Premium | 유료 | 탈락 |

**채택 이유:**
- 실제 `<table>` 마크업이라 접근성, 인쇄, 복사가 자연스럽습니다.
- 고정 머리글, 열 고정, 가상 스크롤(`virtual`)이 있고 ko/en 로케일을 기본 제공합니다.
- Refine과 바로 맞물립니다.

**감싸는 방식:**
- 화면은 `CrmGrid`만 씁니다. 바꿀 일이 생기면 엔진만 교체합니다.
- 열 정의는 `crmUiModel`에서 자동으로 만듭니다.

### 페이징 · 정렬 · 검색 (서버 처리)
- **변환 (Refine 데이터 공급자 `getList`):**
  - AntD 1부터 쪽 번호 ↔ `pageNumber` 0부터, `pageSize`
  - 여러 열 정렬 → `sortCode "amount-,name+"`
  - 검색 조건 → SrchDTO 필드
  - 응답 `Page.totalElements`로 전체 건수와 쪽 수를 표시합니다.
- **쪽 크기:** 20/50/100(기본 20). 서버 상한을 넘기는 요청은 서버가 거절합니다(`@searchable(pageSize)`나 설정값, 안 되면 이슈로 보고).
- **검색 줄:** DSL `@search(like|eq|between|in)` 필드만 노출합니다. 연산자에 맞는 입력을 씁니다(문자열·날짜 범위·enum 다중 선택·참조 선택). 입력은 300ms debounce 합니다.
- **화면 상태를 URL에 둡니다**(`?page=2&size=50&sort=amount-&q.stage=PRS`). 새로고침·링크 공유·뒤로 가기에도 그대로입니다.
- **읽는 중에는 이전 행을 유지**하고(깜빡임 없음), 빈 결과·오류는 표 안에 안내합니다.
- **열 기능:**
  - 보이기·숨기기, 순서 끌기, 너비 조절, 왼쪽 고정, 행 높이(촘촘/보통)
  - 사용자별로 저장합니다(localStorage, 키 `grid:<entity>`).
- **선택:** 여러 행을 선택해 일괄 삭제(`POST /delete`)나 일괄 상태 명령을 실행합니다. 명령 버튼은 `canX()`로 활성화합니다.
- **칸 표시(형식별):**
  - money는 오른쪽 정렬 + 언어 서식, date·datetime은 언어 서식
  - enum은 언어별 라벨 배지, 참조는 링크(누르면 상세 서랍)
  - boolean, 가린 값(`@Mask`)은 그대로 표시합니다.
- **모바일(<768px):** 같은 데이터를 카드 목록으로 보여 주고, 페이징·검색은 동일합니다.

### Excel 다운로드 — 두 가지
| 버튼 | 처리 | 내용 |
|---|---|---|
| **지금 보이는 표** | 화면(`exceljs`, MIT) | 현재 쪽 또는 선택한 행. **보이는 열·순서·너비·정렬 그대로** |
| **조건 결과 전체** | 서버 스트리밍 `POST /api/crm/{entity}/export.xlsx` (Apache POI `SXSSFWorkbook`) | 같은 SrchDTO 조건의 모든 행 |

**"조건 결과 전체"의 서버 처리:**
- 화면이 지금의 열 목록과 순서를 함께 보내서, 표와 같은 모양의 파일을 받습니다.
- 서버는 **생성된 UseCase `search`를 쪽 단위로 돌며** 행을 씁니다. 그래서 `@access`·`@owner`·`@Mask`가 화면과 똑같이 적용됩니다(권한 밖 행·원문 유출 없음).
- 머리글은 `MessageSource`가 `crm-labels`를 요청 `Accept-Language`로 읽습니다.
- 상한(예: 10만 행)을 두고, 넘으면 미리 안내합니다. 다운로드 기록(누가·언제·조건·행 수)을 남깁니다.
- 엔티티→UseCase 연결표 `CrmExportRegistry.java`는 `crm-ui-rules`가 DSL에서 생성합니다. 엔티티를 추가하면 내보내기도 자동으로 됩니다.

**두 방식 공통 서식:**
- 머리글은 고른 언어의 DSL 라벨입니다.
- enum은 언어별 라벨, money는 숫자 서식(`#,##0.00`), 날짜는 Excel 날짜 형식입니다.
- 첫 행 고정, 자동 필터를 겁니다.
- 시트 이름은 엔티티 라벨, 파일 이름은 `<엔티티라벨>_<yyyyMMdd-HHmm>.xlsx`입니다.
- 수식 주입을 막기 위해 `= + - @`로 시작하는 문자열 앞에 `'`를 붙입니다.

**ElastiCORE 이슈 후보:** 생성 API에 Excel/CSV 내보내기(`@expose(export)`)가 없습니다.

## 보안 · 회원가입
- **가입 `POST /api/crm/account/signup`** (petstore 방식)
  - 비밀번호 정책을 둡니다.
  - 가입 직후 역할은 CUSTOMER이고, 직원 역할은 ADMIN만 줍니다.
  - 오류 문구로 계정 존재 여부를 알 수 없게 합니다.
- **세션:** `@signin(refresh=password)`로 비밀번호를 바꾸면 이전 세션을 끝냅니다.
  - 쿠키는 HttpOnly + SameSite=Lax, 운영은 Secure
  - CSRF는 이중 제출 토큰으로 막습니다.
  - 로그인 시도 횟수를 제한하고, 로그인하면 세션을 새로 만듭니다.
- **개인정보:** 전화·이메일은 `@encrypt`(같음 검색만 됨) + 응답 `@Mask`. 담당자·관리자만 원문을 보는 경로가 필요하면 그 방식을 검토하고, 안 되면 이슈로 보고합니다.
- **권한:**
  - REP는 `@owner`로 자기 것만, MANAGER·ADMIN은 전체를 봅니다.
  - CUSTOMER는 자기 Ticket과 공개 지식 문서만 봅니다.
  - 팀 단위 열람이 `@access`로 안 되면 이슈로 보고합니다.
- **기타:** 보안 헤더(CSP)를 넣고, 운영 프로필은 관리자 비밀번호·암호화 키 환경 변수가 없으면 기동하지 않습니다. OWASP ASVS L1 체크리스트와 권한 우회 시험(403/404 확인)을 합니다.

## AI 상담 챗봇 (DSL 기반)
- **근거:** `semantic_model.yml`(Ossie)을 씁니다.
  - 엔티티·필드 설명, 상태, 지표, 그리고 **언어별 라벨이 들어간 synonyms**를 맥락으로 씁니다.
  - 그래서 한국어 질문과 영어 질문이 같은 필드를 찾습니다.
- **도구:** `crmUiModel`/Ossie에서 범용 도구를 자동 생성합니다.
  - 목록: `search_<entity>`, `get_<entity>`, `metric(name)`, `create_ticket`
  - **로그인한 사용자 권한으로 생성된 서비스를 호출**합니다. `@access`·`@owner`·`@Mask`가 그대로 적용되고, SQL 직접 생성은 없습니다.
- **두 가지 역할:**
  - 고객 상담사: 지식 문서 근거로 답하고, 내 티켓을 조회·접수합니다. 근거가 없으면 티켓 접수로 넘깁니다.
  - 영업 비서: 내 파이프라인, 이번 달 수주 같은 질문에 답합니다.
- **답변 언어:** 사용자가 고른 언어로 답하고, 결과 표 머리글은 DSL 라벨 번들을 씁니다.
- **평가:** 한·영 질문 20개로 근거 정확도와 권한 누설 0건을 확인합니다.

## 작업 단계
| 단계 | 내용 | 완료 기준 |
|---|---|---|
| 0 | 저장소 준비, 계획서 `docs/development-plan.md` | 계획서 커밋 |
| 1 | Gradle 뼈대(ElastiCORE 3), DSL 모델(`@label(en=)` 포함), `ossie-export`, `crm-ui-rules` 검증 | `build` 통과, DSL 경고 0, `crm-labels_en.properties` 생성, Ossie 스키마 검증 통과 |
| 2 | 보안: 가입·로그인·권한·암호화·마스킹·CSRF·시도 제한 | 보안 시험 통과 |
| 3 | 화면 기반: Refine 데이터 공급자, `CrmGrid`(서버 페이징·정렬·검색·URL 상태·열 설정), 범용 폼·상세, 메뉴 자동 생성, 언어 전환, Excel 두 방식(화면 / 서버 스트리밍 + `CrmExportRegistry`) | 모든 엔티티 CRUD, ko/en 전환, 페이징·Excel 시험 통과 |
| 4 | 직접 만드는 화면: 대시보드, 칸반, Account 360, 디자인 | 화면 캡처 확인 |
| 5 | AI 챗봇: Ossie 맥락, 자동 도구, 상담·비서 | 평가 묶음 통과 |
| 6 | "DSL 필드 추가 → 화면 반영" 시연 시험, README(한·영), 데모 데이터 | e2e 통과 |
| 수시 | ElastiCORE 결함·개선을 `docs/elasticore-issues/NN-*.md`에 버그 양식(버전, 최소 DSL, 생성 코드, 기대값)으로 작성 → GitHub 이슈 등록 | 이슈 링크 기록 |

**미리 보이는 이슈 후보:**
- TS 메타에 형식·순서·참조 대상이 없음
- TS에 entity·enum 다국어 라벨이 나오는지 문서에 없음
- enum 상수 `@label` 미지원
- 회원가입 미생성
- 표 Excel 내보내기 미지원
- 팀 단위 열람
- 가린 필드의 원문 열람 권한

## 실행 시 주의
- **이슈 등록:** ElastiCORE 결함·개선은 `docs/elasticore-issues/` 에 버그 양식으로 쓰고 xsko/ElastiCORE 에 이슈로 등록합니다.
- **jmix-crm 코드는 복사하지 않습니다.** 참고는 기능 목록 수준까지입니다. 데모 데이터는 가상 회사로만 만듭니다.

## 검증
- `./gradlew build`: elcore3·TS·UI 메타·webBuild·시험, `TEST-*.xml` failures 0
- 라벨 번들 키(`Opportunity.amount`)가 ko/en 파일에 있는지 확인합니다.
- `Accept-Language: en`/`ko`로 같은 오류 응답을 보내 언어가 바뀌는지 확인합니다.
- **그리드·Excel 시험:**
  - 데모 데이터 1만 건 계정으로 쪽 이동, 여러 열 정렬, 검색 후 건수가 서버 `totalElements`와 같은지 확인합니다.
  - URL 새로고침 후 같은 상태가 나오는지 확인합니다.
  - "보이는 표" Excel이 화면의 열·순서·행과 같은지 확인합니다.
  - "조건 결과 전체" Excel의 행 수가 `totalElements`와 같은지 확인합니다.
  - REP 계정으로 받은 파일에 남의 고객이 0건이고 가린 값은 가려져 있는지 확인합니다.
  - 머리글이 ko/en으로 바뀌는지, `=1+1` 셀이 문자로 남는지 확인합니다.
  - 서버 시험은 POI로 파일을 다시 열어 검사하고, e2e는 Playwright 다운로드로 검사합니다.
- Ossie `validation/validate.py` 검증, synonyms에 영어 라벨이 들어갔는지 확인합니다.
- Playwright e2e:
  - 가입 → 로그인 → 각 엔티티 목록 페이징 → Excel(머리글 언어·행 수) → ko/en 전환 → 칸반 단계 이동 → 권한 우회 실패
  - **DSL에 필드 하나 추가(`@label(en=)` 포함) 후 재빌드 → 목록·폼·Excel·Ossie에 두 언어로 나타나는지**
- 챗봇 평가 묶음: 한·영 질문, 근거 정확도, 권한 누설 0
- 로컬 실행(`bootRun` + `npm run dev` 프록시)으로 화면을 직접 캡처해 확인
