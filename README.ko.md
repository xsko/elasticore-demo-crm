# ElastiCORE CRM 데모

[English](README.md) | **한국어**

**ElastiCORE 3** 의 DSL 하나로 만든 CRM 입니다. 영업(리드·영업 기회·고객사·연락처·활동·견적)과
고객 상담(문의·도움말)을 다루고, React 화면, 한국어·영어, Excel 내보내기, Apache Ossie 의미 모델,
AI 상담까지 DSL에서 나옵니다.

> DSL 한 줄을 고치면 DB 컬럼, API, 목록·검색·입력 화면, Excel 머리글, 두 언어의 라벨,
> Ossie 모델, AI 상담이 아는 내용이 함께 바뀝니다. `scripts/dsl-change-demo.ps1` 이 그 과정을 그대로 보여 줍니다.

## 바로 실행

필요한 것: JDK 17+, Node.js 20+ (화면 빌드). DB는 내장 H2(파일 `./data/crm-en` 또는 `./data/crm-ko`)이고 처음 시작할 때 샘플 데이터가 들어갑니다. ElastiCORE 3은 Maven Central에서 받고,
빌드가 쓰는 판은 `gradle.properties`의 `elasticoreVersion`에 있습니다.

```bash
./gradlew bootRun            # Windows: gradlew.bat bootRun
# http://localhost:8080
```

| 아이디 | 역할 | 비밀번호 |
|---|---|---|
| `admin` | 관리자 - 모든 데이터 | `demo1234` |
| `minjun`, `seoyeon`, `jihoon`, `yuna` | 영업 담당 - 자기 고객·영업 기회만, 고객 문의는 전부 보고 답변 | `demo1234` |
| `customer`, `haneul`, `garam` | 고객 - 자기 문의, 도움말, AI 상담 | `demo1234` |

**샘플 데이터 언어**: 기본은 영어(금액은 달러)입니다. 한국어(금액은 원)로 보려면 처음 시작할 때 고릅니다.

```bash
CRM_SEED_LANGUAGE=ko ./gradlew bootRun      # 기본 en. 통화는 CRM_CURRENCY=USD|KRW 로 따로 정할 수도 있습니다
# Windows PowerShell: $env:CRM_SEED_LANGUAGE='ko'; .\gradlew.bat bootRun
```

언어마다 DB 파일이 따로 있어서(`./data/crm-en`, `./data/crm-ko`) 바꿔도 지울 것이 없습니다.
샘플 데이터를 처음부터 다시 만들려면 서버를 멈추고 해당 파일을 지운 뒤 다시 시작합니다. 회사·사람 이름은 모두 지어낸 것입니다.

### AI 상담 켜기 (선택)

키는 환경 변수로만 받습니다.

```bash
# Claude
CRM_AI_PROVIDER=anthropic ANTHROPIC_API_KEY=... ./gradlew bootRun
# OpenAI 또는 OpenAI 호환 서버(로컬 LLM, vLLM 등)
CRM_AI_PROVIDER=openai OPENAI_BASE_URL=http://<host>:8000/v1 CRM_AI_MODEL=<model> OPENAI_API_KEY=<key 또는 아무 값> ./gradlew bootRun
```

Windows PowerShell 에서는 변수를 먼저 하나씩 넣고(`$env:CRM_AI_PROVIDER='anthropic'` ...) `.\gradlew.bat bootRun` 을 실행합니다.
설정하지 않으면 AI 상담 화면은 "설정되지 않음"(503)으로 답하고 나머지는 그대로 동작합니다.

## 일반 개발과 비교 — ElastiCORE 로 얻은 것

아래 수치는 이 저장소를 직접 센 값입니다(빈 줄·주석 제외, 2026-11-23 기준).
"일반 개발"은 같은 기능을 Spring Boot + React 로 손으로 짜는 경우를 말합니다.

### 1. 사람이 쓴 것과 ElastiCORE 가 만든 것

| 구분 | 파일 | 줄 | 누가 |
|---|---:|---:|---|
| **DSL** (`blueprint/crm/*.yml`) | 8 | **403** | 사람 |
| 서버 Java (엔티티·DTO·매퍼·리포지토리·서비스·REST·권한·상태 전환·검증·오류 처리) | 155 | 12,007 | ElastiCORE |
| TypeScript 클라이언트 (DTO·API·검증·enum 라벨) + 화면 모델 | 71 | 2,970 | ElastiCORE (+ 팀 규칙) |
| 라벨·오류 문구 번들 (ko/en) | 4 | 760 | ElastiCORE |
| DB 스키마 문서 + Ossie 의미 모델 (AI 상담의 근거) | 2 | 1,391 | ElastiCORE |
| 시험: 엔티티마다 계약 시험 클래스 하나와 그 지원 클래스, 계층 시험 | 12 | 2,956 | ElastiCORE (한 번 생성, 이후 프로젝트 소유) |
| 팀 규칙 `crm-ui-rules` (DSL → 화면 모델, 번역 누락 검사) | 3 | 418 | 사람, 한 번 |
| 서버 Java 직접 작성 (샘플 데이터 545, AI 상담 422, 지표 197, 보안 필터 180, 그 밖 172) | 16 | 1,516 | 사람 |
| 화면 `web/src` (모든 엔티티 공용 목록·상세·입력·보드·홈·AI 상담) | 30 | 2,675 (+CSS 354) | 사람 |

- **DSL 403줄이 코드 약 15,000줄(Java + TypeScript)을 만듭니다. DSL 1줄당 약 37줄입니다.**
  라벨 번들·스키마·의미 모델까지 넣으면 약 17,000줄이고, 그 위에 시험 코드가 약 3,000줄 더 생깁니다.
- 서버 Java 의 **89%** (12,007 / 13,523줄)가 생성 코드입니다. 손으로 쓴 서버 코드는 DSL 이 말할 수 없는 것뿐입니다:
  샘플 데이터, AI 상담, 지표 계산, CSRF·보안 헤더·로그인 시도 제한, 리드 전환 처리.
- 생성된 REST API **107개**(API 가 있는 엔티티 10개)를 손으로 쓴 줄은 0입니다. 조회·검색·생성·수정·삭제·일괄 처리, 상태 명령, 내보내기가 모두 포함됩니다.
- 화면 코드는 엔티티 이름을 모릅니다. 엔티티 이름이 나오는 곳은 홈 대시보드(3개 엔티티)와 가입 화면(로그인 엔티티의 생성 검사 함수)뿐이고, 나머지 목록·상세·입력·보드·Excel 은 모든 엔티티가 같은 코드를 씁니다.

### 2. 필드 하나를 추가할 때

DSL 에 `segment: string(30) @search(like) @label(en=Segment) -- 고객 세그먼트` **한 줄**을 넣고 생성만 돌렸을 때 실제로 바뀐 것:

| | ElastiCORE (실측) | 일반 개발 (보통 손으로 고치는 곳) |
|---|---|---|
| 손으로 고치는 곳 | **DSL 1줄** | DB 컬럼, 엔티티, 응답 DTO, 검색 DTO와 쿼리, 매퍼, 서버·화면 검증, TS 타입, 목록 열, 검색 입력, 입력 폼, 상세 화면, Excel 열, 한·영 번역, API 문서, AI 상담 맥락 - 10곳 넘게 |
| 같이 바뀐 파일 | 14개, 76줄 (엔티티, DTO, 검색 DTO, 매퍼, 쿼리 클래스, 라벨 3개, 스키마, Ossie 모델, TS DTO·검색 DTO·화면 모델, 그리고 새 필드 길이의 계약 시험) — 모두 생성 | 사람이 하나씩 |
| 화면 코드 수정 | **0줄** — 목록·검색·폼·상세·Excel·두 언어에 바로 나타나고, 폼이 길이도 검사함 | 화면마다 수정 |
| 빠뜨렸을 때 | 영어 라벨이 없으면 **빌드가 멈춤**, 화면이 생성 클라이언트와 어긋나면 **타입 검사에서 빌드 실패** | 시험이나 운영에서 발견 |

`scripts/dsl-change-demo.ps1` 이 이 과정(라벨 없으면 빌드 중단 → 라벨 넣으면 모든 곳 반영 → 브라우저 시험)을 그대로 보여 줍니다.

### 3. 손으로 짜면 매번 다시 검증해야 하는 것을 DSL 한 곳에서

| 요구 | DSL | 일반 개발이라면 |
|---|---|---|
| 누가 무엇을 하는지 | `@access(read=REP&owner, answer=REP)`, `@owner(...)` | 서비스마다 권한 검사, 검색마다 "내 것만" 조건, 내보내기·지표에도 같은 조건을 따로. 하나 빠지면 정보 유출 |
| 업무 흐름 | `states:` (`win: PROPOSAL, NEGOTIATION -> WON`) | 상태 검사·전환 API·오류 문구·화면 버튼 활성화를 따로 |
| 개인정보 | `@encrypt @s(eq)`, `@env:maskphone` | 암호화 변환기, 같음 검색용 색인, 응답 가림, 가린 값이 저장값을 덮지 않게 하는 처리 |
| 다국어 | `-- 한국어 @label(en=...)`, enum 의 `label_en` 열 | 번역 키를 서버·화면·Excel·오류 문구·AI 맥락에 따로 관리 |
| AI 가 데이터를 이해 | 생성된 Ossie 의미 모델 (필드 뜻, 코드 의미 한·영, 지표 SQL) | 프롬프트용 설명서를 따로 쓰고 모델이 바뀔 때마다 손으로 맞춤 |

서버가 강제하는 규칙과 화면이 보여 주는 규칙이 같은 DSL 에서 나오므로, 화면에는 버튼이 보이는데 서버가 거절하는 식의 어긋남이 구조적으로 생기지 않습니다.

### 4. 보안 — DSL 이 강제하는 것과 직접 만든 것

일반 개발에서는 아래 줄마다 코드를 따로 쓰고, 기능이 늘 때마다 빠진 곳이 없는지 다시 봐야 합니다.
ElastiCORE 열의 항목은 DSL 한 줄이 모든 엔티티·모든 경로에 같은 방식으로 들어갑니다.

| 위협 / 요구 | 막는 방법 | 누가 | 시험으로 확인 |
|---|---|---|---|
| 남의 데이터 조회·수정 | `@access` + `@owner` 가 모든 생성 서비스(조회·검색·수정·삭제·상태 명령·내보내기)에서 검사되고, 검색은 "내 것만"으로 좁혀짐. 대시보드 지표도 같은 규칙(ElastiCORE `AccessPolicy`)으로 계산 | ElastiCORE | 다른 담당자의 고객사 404, 고객 계정의 영업 데이터 403, 고객의 답변 처리 403. 생성된 계약 시험이 모든 엔티티의 모든 작업을 역할별로 확인(401, 403, 404, 남의 행은 검색에 안 나옴) |
| 역할 올리기 | 가입은 생성된 `create=public` 이지만 `role` 필드는 `@access(admin)` - 가입 때 관리자를 요청해도 고객으로 저장 | ElastiCORE | 가입 시 `role=A` 요청 → `C` |
| 비밀번호 저장·규칙 | `@password` → PBKDF2 해시, 응답에 안 나옴(내보내기 포함). `@length(min=8)` + `@pattern`(문자·숫자) 을 서버가 검사 | ElastiCORE | 숫자 없는 비밀번호 400 (회원 내보내기에 비밀번호 열이 없는 것은 수동 확인) |
| 세션 탈취·고정 | 로그인하면 세션 ID 교체, 비밀번호를 바꾸면 다른 세션 종료(`@signin(refresh=password)`). 쿠키 HttpOnly·SameSite=Lax, 운영은 Secure, 60분 | ElastiCORE + 설정 | 로그인 전후 세션 ID 다름 |
| 다른 사이트의 요청 위조(CSRF) | 이중 제출 토큰: `XSRF-TOKEN` 쿠키를 `X-XSRF-TOKEN` 헤더로 되돌려야 쓰기 가능, 상수 시간 비교 (`CsrfCookieFilter`) | 직접 | 토큰 없는 쓰기 403 |
| 비밀번호 대입 | 아이디당 5회·주소당 20회 실패하면 10분 잠금, 잠겨도 "틀림"과 같은 응답(아이디 존재 여부 숨김) (`SignInThrottle`) | 직접 | 5회 틀린 뒤엔 맞는 비밀번호도 거절 |
| XSS·클릭재킹 | CSP `script-src 'self'`·`frame-ancestors 'none'`, nosniff, Referrer-Policy, API 는 `no-store` (`SecurityHeadersFilter`). React 가 출력 이스케이프, AI 답변 마크다운은 HTML 을 그리지 않음 | 직접 | CSP·nosniff 헤더 |
| 개인정보 저장 | `@encrypt` → AES-256-GCM 으로 저장, `@s(eq)` → HMAC 색인으로 같음 검색만. 키는 환경 변수, 운영에서 없으면 기동 안 함 | ElastiCORE | 전화번호로 같음 검색 성공 |
| 개인정보 노출 | `@Mask` 로 응답을 가리고 Excel·AI 상담도 같은 응답만 봄. 가린 값을 그대로 다시 보내면 저장값 유지. Ossie 모델에서 비밀번호·암호화 열 제외 | ElastiCORE | 연락처 전화·이메일 가림, Excel 도 가림, 가린 값 왕복 후 원래 번호로 검색됨 |
| CSV/Excel 수식 주입, 대량 유출 | 내보내기: CSV 는 `= + - @` 로 시작하는 값 앞에 `'`, xlsx 는 항상 텍스트 칸, `elasticore.export.max-rows`(10만) 넘으면 400 | ElastiCORE | `=HYPERLINK(...)` 가 수식이 안 됨 |
| AI 상담을 통한 유출·SQL 주입 | 도구는 생성 서비스만 로그인한 사람의 권한으로 부름(권한·가림 그대로). LLM 은 SQL 을 만들지 않고, 지표 SQL 은 생성기 것을 이름으로만 고름. AI 키는 환경 변수 | 직접 (구조) | 로컬 LLM 으로 수동 확인, 자동 시험 없음 |
| 오류로 내부 정보 노출 | RFC 9457 오류 응답(코드·문구만), stack trace 안 냄, H2 콘솔 꺼짐. 운영 프로필은 Swagger 끔, `ddl-auto: validate`, 데모 계정 없음, 비밀값 기본값 없음 | ElastiCORE + 설정 | - |

아직 없는 것(운영 전에 더 할 일):
- **감사 기록:** 누가 언제 무엇을 바꿨는지 남기는 기록이 없습니다.
- **2단계 인증**이 없습니다.
- **로그인 시도 제한**이 서버 한 대의 메모리에만 있어서, 서버를 여러 대로 늘리면 공유 저장소가 필요합니다.
- **팀 단위 권한과 "본인에게만 원문 보이기"**는 ElastiCORE 에 요청 중입니다(#178).
- **외부 점검:** 침투 시험, 의존성 취약점 점검(SBOM)은 하지 않았습니다.
- **데모 키:** `application.yml` 의 암호화 키는 데모 전용입니다. 운영에서는 `CRM_CRYPTO_KEY`, `CRM_CRYPTO_INDEX_KEY` 를 반드시 따로 넣습니다.

### 5. 비용과 한계 (솔직하게)

- DSL 과 생성 규칙을 배워야 하고, 생성 파일은 직접 고치지 않는 규율이 필요합니다. DSL 로 말할 수 없는 것(보안 필터, AI 상담, 샘플 데이터, 지표 계산)은 여전히 손으로 씁니다.
- 이 데모를 만들며 ElastiCORE 의 결함·부족을 12건 찾았습니다. 모두 보고했고, 11건은 정식 ElastiCORE 3에 들어갔으며 1건(#295, 시험 생성)은 단계별로 진행 중입니다.
  이번에는 빨리 고쳐졌지만, 프레임워크가 막는 곳은 고쳐질 때까지 기다리거나 보고해야 한다는 점은 비용입니다.
- 지금 생성되는 시험은 상태 흐름, 필드 제약, `@access`까지입니다(#295). 가림·암호화와 내보내기는 다음 단계가 나올 때까지 `CrmApiTest`에서 손으로 시험합니다.
- 위 "일반 개발" 열은 보통 손으로 고치는 곳을 적은 것이고, 같은 기능을 따로 구현해 잰 시간은 아닙니다.

## 무엇이 DSL에서 나오나

```
src/main/resources/blueprint/crm/            ← 사람이 쓰는 곳: env.yml, model/*.yml
        │  ./gradlew build
        ├─ elcore3   → Java: 엔티티·DTO·서비스·REST·권한(@access/@owner)·상태 전환(states:)·암호화·마스킹,
        │              src/test: 엔티티별 계약 시험(testCode: contract)과 계층 시험
        ├─ elcore3ts → src/main/ts: 타입 클라이언트 + crm-ui-rules 의 화면 모델(<Entity>Ui.ts)
        ├─ ossie     → blueprint/semantic_model.yml (Apache Ossie): 대시보드 지표와 AI 상담의 근거
        ├─ i18n      → crm-labels*.properties (라벨, enum 값 포함), crm-messages.properties (거절 문구)
        └─ webBuild  → web/ 을 생성 클라이언트에 맞춰 타입 검사 후 번들 (DSL과 안 맞으면 빌드 실패)
```

| 기능 | 어디서 | 설명 |
|---|---|---|
| 다국어 라벨 | `-- 한국어 @label(en=English)` | 엔티티·필드·enum 줄. enum 값은 enum 자체의 `label`, `label_en` 열 → 생성 `XxxLabel`/`XxxLabels`, 라벨 번들, Ossie. 영어 라벨이 빠지면 빌드가 멈춥니다(`crm.languages` in env.yml) |
| 제약 | `!`, `string(n)`, `@length`, `@min`, `@max`, `@pattern`, `@email`, `@minsize` | 서버가 검사하고, 화면도 요청 전에 같은 검사를 하며(생성된 `validateXxxCodes`), 생성된 계약 시험이 확인합니다 |
| 거절 문구 | `labelLanguage: ko` (env.yml) | "단계가 발굴일 때는 수주로 바꿀 수 없습니다. [제안, 협상]일 때만 할 수 있습니다" 처럼 필드·엔티티·상태를 DSL 라벨로 부릅니다 |
| 메뉴·화면 힌트 | `@ui(menu=sales, order=20, icon=..., board, title)` | `crm-ui-rules` 가 읽어 화면 모델에 넣습니다 |
| 권한 | `@access(read=REP&owner, create=REP, answer=REP, ...)` `@owner(...)` | 역할(`REP`)과 수준(`owner`)을 `&`·`\|` 로 묶습니다. 서버가 강제하고 화면·지표도 같은 규칙을 씁니다 |
| 상태 흐름 | `states:` | 기록 화면의 단계 표시, 보드(칸반)의 끌어 놓기, 명령 버튼 |
| 지표 | `metrics:` | Ossie 로 내보내고, 서버가 그 SQL 을 사용자 권한 범위로 실행해 대시보드와 AI 상담에 씁니다 |
| 개인정보 | `@encrypt`, `@env:maskphone` | 저장은 AES-GCM, 응답은 가린 값. 가린 값을 그대로 보내면 저장값이 유지됩니다. Excel 과 AI 상담도 같은 값만 봅니다 |
| 내보내기 | `@expose(20, methods="...,export")` | ElastiCORE 가 만드는 `POST .../export` (검색과 같은 권한·마스킹, .xlsx/CSV, 최대 `elasticore.export.max-rows`) |

### 화면 (web/)

Vite + React 19 + **Refine 5 + Ant Design 5**. 화면 코드는 엔티티 이름을 모릅니다.
`import.meta.glob` 으로 모든 `*Ui.ts` 를 모아 메뉴·목록·상세·입력·보드를 만듭니다.

- **표 그리드**: 실제 HTML `<table>`(Ant Design Table). 페이징·정렬·검색은 서버(`POST /search`의
  `pageNumber`·`pageSize`·`sortCode`), 상태는 주소창에 남아 새로 고침·링크 공유 그대로. 열 표시·순서·너비·행 높이는 사용자별 저장.
- **Excel 두 가지**: "지금 보이는 표"(브라우저, exceljs - 보이는 열·순서·너비 그대로) /
  "조건 결과 전체"(ElastiCORE 가 생성한 `POST .../export` - 같은 검색 조건의 모든 행, 권한·마스킹 동일, 최대 10만 건).
  머리글과 enum 값(발굴 / Prospecting)은 고른 언어로, 수식처럼 보이는 값은 문자로. enum 을 라벨로 쓰는 것은
  내보내기 요청의 `labels: "*"` 입니다(ElastiCORE #289).
- **폼은 보내기 전에 DSL 제약을 검사**: ElastiCORE 가 생성한 `validateXxxCodes` 로 모든 제약을 확인하고, 코드마다 고른 언어의
  문구로 보여 줍니다(`web/src/form/codes.ts`). 가입 화면도 로그인 엔티티의 생성 검사 함수를 쓰므로 비밀번호 규칙은 DSL 에만 있습니다.
  서버가 보낸 필드 오류는 그 필드에, 견적 품목의 오류(`lines[0].quantity`)는 그 행에 표시됩니다.
- 기록 화면(요약·단계·관련 목록·활동 타임라인), 보드(DSL `states:` 가 허락하는 이동만), 대시보드(DSL 지표), AI 상담.

### 보안

위 "일반 개발과 비교 → 4. 보안" 에 위협별로 정리했습니다(무엇을 ElastiCORE 가 강제하고 무엇을 직접 만들었는지, 어떤 시험이 확인하는지).

## 시험

| 시험 | 개수 | 확인하는 것 | 누가 |
|---|---:|---|---|
| `<Entity>ContractTest` (10개 클래스) | 362 | HTTP로 확인하고, 판정은 상태 코드와 오류 `code`로만 합니다(문구는 보지 않음). 상태 전이는 나열된 상태에서 성공하고 다른 상태에서는 409 `state.*`로 거절됩니다. `@stamp`가 채워집니다. `!`, 길이, `@min`, `@max`, `@minsize`, `@pattern`, `@notblank`, `@email`은 경계값이 통과하고 한 칸 넘으면 400(필드와 코드)으로 거절됩니다. `@access`는 작업마다 허용된 호출자가 통과하고, 비로그인은 401, 다른 역할은 403, 남의 행은 404이거나 검색에 안 나옵니다 | ElastiCORE(`testCode: "contract"`)가 361개 생성. 1개는 손으로 추가(견적 품목 수량 — 하위 항목 자체의 제약) |
| `CrmArchitectureTest` | 3 | 헥사고날 계층 경계(ArchUnit), 직접 쓴 패키지 포함 | ElastiCORE (`archTest: true`) |
| `CrmApiTest` | 9 | HTTP 로: 생성된 내보내기, `@access` 역할, 가린 값 왕복, 라벨 거절 문구, 지표, CSRF | 사람 |
| `MessageBundleTest` | 2 | 두 언어의 라벨·문구 번들 | 사람 |
| `SecurityFiltersTest` | 8 | CSRF 방어와 보안 헤더 | 사람 |
| `SignInThrottleTest` | 5 | 비밀번호 대입 잠금(아이디·주소별), 풀림, 초기화 | 사람 |
| `SeedTextTest` | 7 | 영어·한국어 샘플 데이터의 모양이 같고, 쓰는 코드가 모두 실제 enum 값인지 | 사람 |
| `AppInfoControllerTest` | 3 | 화면이 쓰는 통화 | 사람 |
| `CrmUiRulesTest` (crm-ui-rules) | 4 | 팀 규칙: DSL → 화면 모델, 영어 라벨 누락 시 빌드 중단, blind index 제외 | 사람 |

Gradle 시험은 모두 403개이고 건너뛴 것은 없습니다. 그 밖에 Playwright 화면 시험 10개와 API 점검 29건이 있습니다.

계약 시험은 ElastiCORE가 한 번 만들어 주고, 그 뒤로는 프로젝트가 관리합니다. DSL이 바뀌면 시험에 병합되고, 손으로 고친 내용은 유지됩니다.
모든 요청은 `CrmContractSupport`를 거치며, 여기서 데모의 XSRF 쿠키와 헤더를 붙입니다. 생성된 시험에서 고친 것은 이것뿐입니다.
`signIn(role)`은 생성된 로그인으로 역할별 회원을 로그인시킵니다.
가림·암호화와 내보내기 시험은 ElastiCORE #295의 다음 단계에서 나옵니다. 그때까지는 `CrmApiTest`가 손으로 확인합니다.

```bash
./gradlew test                                   # 위 시험 전부 (결과: build/test-results)
powershell -File scripts/run-local.ps1           # jar 를 18090 에 띄움
powershell -File scripts/smoke-api.ps1           # 브라우저처럼 API 호출 29건
cd web && CRM_URL=http://localhost:18090 npx playwright test   # 설치된 Chrome 으로 화면 시험
powershell -File scripts/dsl-change-demo.ps1     # DSL 필드 추가 → 모든 곳 반영 시연 (끝나면 되돌림)
```

## AI 로 코딩할 때 — 가이드와 주의점

이 데모는 AI 코딩 에이전트(Claude Code)로 만들었습니다. 그때 효과가 있었던 방식과 실제로 걸렸던 함정입니다.
사람이 AI 에게 일을 맡길 때도, AI 가 이 저장소를 고칠 때도 그대로 적용됩니다.

### 가이드 — 이 순서로

1. **DSL 로 말할 수 있는지부터 본다.** 필드·권한·상태 흐름·계산값·라벨·지표는 `src/main/resources/blueprint/crm/` 의 YAML 에서 바꿉니다.
   DSL 로 안 되는 것만 손으로 씁니다: 서비스의 `beforeSave` 같은 확장 지점, 또는 헥사고날 계층 안의 별도 패키지
   (`application.security`, `adapter.in.web.security`, `adapter.in.assistant`, `adapter.in.seed`, `infrastructure.semantic`).
   계층 밖에 두면 생성된 계층 시험이 검사하지 못합니다.
2. **문법은 기억이 아니라 이 프로젝트의 가이드에서 찾는다.** 생성할 때마다 그 판의 가이드가 `blueprint/elasticore-*.md` 로 복사됩니다.
   `elasticore-guide-index.md` 에서 낱말(`@access`, `states:`, `export`)로 절을 찾고 그 줄 범위만 읽습니다. 판이 바뀌면 문법도 바뀝니다
   (예: 이 데모를 만드는 동안 enum 다국어 열이 `labelEn` 에서 `label_en` 으로 바뀜).
3. **생성 → 경고 확인 → 컴파일 → 시험 순서를 지킨다.**
   - `gradlew.bat elcore3 elcore3ts` 출력에서 DSL 경고를 0으로 만듭니다. 경고가 있어도 생성은 "성공"이라고 나오므로 꼭 읽습니다.
   - 이어서 `compileJava` → `test` → 화면 `npx tsc -b` → `smoke-api.ps1` → Playwright 순으로 돌립니다.
4. **화면은 엔티티 이름을 쓰지 않는다.** 새 화면 기능은 생성된 화면 모델(`src/main/ts/<Entity>Ui.ts`)을 읽게 만듭니다.
   화면 모델에 없는 정보가 필요하면 화면이 아니라 팀 규칙 `crm-ui-rules` 에 넣습니다.
5. **새 필드·엔티티·enum 값에는 처음부터 영어 라벨을 단다**(`@label(en=...)`, enum 의 `label_en`). 빠지면 빌드가 멈추는데, 일부러 그렇게 만든 것입니다.
6. **결과는 업무 말로 보고하게 한다.** "누가 무엇을 할 수 있게 됐다", "어떤 상태에서 무엇이 막힌다"처럼 말하게 하고, 시험 결과를 증거로 붙이게 합니다.
7. **ElastiCORE 가 막으면 우회하지 않는다.** 최소 DSL, 나온 코드, 기대값을 `docs/elasticore-issues/` 에 적고 이슈로 올립니다.
   이 데모에서 이렇게 올린 12건 중 11건이 반영됐고, 데모의 우회 코드는 모두 지웠습니다.

### 주의점 — 실제로 걸렸던 것

| 함정 | 무슨 일이 있었나 | 지킬 것 |
|---|---|---|
| 생성 파일 직접 수정 | 다음 생성에서 덮이거나 병합 충돌 | `.elasticore/cache` 에 있는 파일은 DSL 로 바꿉니다. 캐시 파일은 커밋하고 지우거나 고치지 않습니다 |
| 매번 다시 쓰이는 파일 | `crm-messages.properties`, `schema.yml`, `semantic_model.yml` 은 생성마다 새로 써짐 | 번역은 `_ko` 사본에 넣습니다 |
| 화면에서 권한을 따로 판단 | 화면과 서버 규칙이 어긋날 수 있음 | 화면(`accessControl.ts`)과 지표(`AccessPolicy`)는 같은 `@access` 식을 해석하고, 최종 판단은 서버가 합니다 |
| 겉만 통과한 시험 | `$431,000` 을 숫자로 바꾸다 NaN 이 되어 정렬 시험이 아무것도 확인하지 않고 통과 | 시험이 실제로 무엇을 비교하는지 봅니다. 통과 여부는 `build/test-results/test/TEST-*.xml` 의 failures/errors 로 판단하고, 파이프 뒤 종료 코드는 믿지 않습니다 |
| 규칙 API 의 숨은 필드 | 팀 규칙에 `@encrypt` 의 색인 필드(`phoneBidx`)가 일반 필드처럼 와서 폼에 나타남(#290, 지금은 `FieldSpec.blindIndexOf()` 로 거름) | 새 화면 모델 항목을 만들면 실제 폼과 상세 화면을 브라우저로 확인합니다 |
| 폼 검증 규칙 | Ant Design 의 required 규칙에 다른 키가 붙으면 문자열 검사로 바뀌어, 숫자인 참조값이 "필수" 오류 | 화면 변경은 Playwright 로 저장까지 해 봅니다 |
| 하드코딩된 문구 | 리드 전환 코드에 "신규 도입"이 박혀 영어 데이터에도 섞임 | 사람이 읽는 문구는 DSL 라벨이나 언어 설정에서 가져옵니다 |
| 비밀값 | AI 키·DB 비밀번호·암호화 키가 저장소에 들어갈 위험 | 환경 변수로만 받습니다. `application.yml` 의 키는 데모 전용입니다 |
| AI 상담 기능 확장 | LLM 에 SQL 이나 DB 를 직접 주면 권한·가림을 건너뜀 | 도구는 생성 서비스만, 로그인한 사람의 권한으로 부르게 합니다 |
| 서버 실행 | 도구 셸에서 `gradlew bootRun` 은 끝나지 않고, 프로세스를 이름으로 죽이면 다른 Java 도 죽음 | `scripts/run-local.ps1` (jar 를 백그라운드로, PID 기록) / `stop-local.ps1` (그 PID 만) |
| Windows | `gradlew` 대신 `gradlew.bat`. BOM 없는 `.ps1` 은 Windows PowerShell 5.1 에서 한글이 깨짐 | 스크립트는 UTF-8 BOM 으로 저장합니다 |
| 시험이 남긴 데이터 | smoke·e2e 가 데모 DB 에 기록을 남김 | 시연 전 `data/crm-<언어>.mv.db` 를 지우고 다시 시작합니다 |
| 화면에 베낀 제약 | 가입 화면에 DSL 비밀번호 규칙을 베껴 두면 DSL이 바뀔 때 서로 어긋남 | 브라우저에서도 ElastiCORE가 생성한 `validateXxxCodes`로 검사하고, 코드마다 화면 문구로 바꿉니다(`web/src/form/codes.ts`) |

AI 에게 맡길 때 이렇게 지시하면 결과가 안정적이었습니다:
"DSL 로 할 수 있으면 DSL 로 하고, 안 되면 이유를 말해라. 생성 경고·컴파일·시험·화면 시험 결과를 증거로 보여 줘라.
생성 파일은 고치지 말고, ElastiCORE 문제는 우회하지 말고 보고해라."

## ElastiCORE 에 보고한 것

작업 중 찾은 버그·개선 요청은 [docs/elasticore-issues](docs/elasticore-issues/README.md) 에 ElastiCORE 이슈 양식으로 정리했습니다.
처음 보고한 9건(#266~#274)은 정식 ElastiCORE 3에 들어갔고, 데모는 그 기능으로 바꿔 자체 우회 코드(직원 전용 검사, 자체 Excel 내보내기,
enum 라벨 규칙)를 지웠습니다. 이어서 찾은 2건(#289 내보내기의 enum 라벨, #290 규칙 API 의 색인 필드)도
정식판에 들어가 데모에 적용했습니다. 진행 중인 것: #295(DSL로 시험 생성). 상태 흐름·제약·`@access` 시험이 나와서 데모에 적용했고, 가림·암호화와 내보내기는 아직입니다.

## 기획 참고

기능 범위는 [jmix-crm](https://github.com/jmix-framework/jmix-crm) 을 참고했지만 코드는 가져오지 않았고,
업무 초점(영업 기회 + 고객 상담), 화면 흐름, 디자인을 따로 정했습니다. 개발 계획: [docs/development-plan.md](docs/development-plan.md).
