package io.elasticore.demo.crm.adapter.in.seed;

/**
 * The words the sample data is written in - one set per language. The shape of the data
 * (how many companies, deals at which stage, which quotes are accepted) does not depend on
 * it: the seeder draws the same random numbers in either language, only the text and the
 * money scale differ (won in Korean, dollars in English).
 *
 * <p>Every company and person is made up.
 */
final class SeedText {

    final String language;
    /** What an amount written in won is divided by: 1 in Korean, 1000 (won to dollars, roughly) in English. */
    final long moneyDivisor;

    final String adminName;
    final String staffCompany;
    /** login, name */
    final String[][] reps;
    /** login, name, company */
    final String[][] customers;
    final String[] companyPrefix;
    final String[] companySuffix;
    final String companyJoin;
    final String[] cities;
    final String[] familyNames;
    final String[] givenNames;
    final boolean familyNameFirst;
    final String[] jobTitles;
    final String[] leadSites;
    final String[] dealKinds;
    /** The quote title: %1$s the deal, %2$d the round. */
    final String quoteTitle;
    /** subject, ActivityType */
    final String[][] activities;
    /** subject, TicketCategory, body */
    final String[][] tickets;
    final String ticketReply;
    /** code, name, ProductCategory, price in won */
    final String[][] products;
    /** %s the product's name. */
    final String productDescription;
    /** title, TicketCategory, keywords, body */
    final String[][] articles;
    final String websiteSuffix;
    /** Phone numbers: %1$d a number below 10000 (or 300-899 for an office line), %2$04d four digits. */
    final String mobileFormat;
    final String officeFormat;

    private SeedText(Builder b) {
        this.language = b.language;
        this.moneyDivisor = b.moneyDivisor;
        this.adminName = b.adminName;
        this.staffCompany = b.staffCompany;
        this.reps = b.reps;
        this.customers = b.customers;
        this.companyPrefix = b.companyPrefix;
        this.companySuffix = b.companySuffix;
        this.companyJoin = b.companyJoin;
        this.cities = b.cities;
        this.familyNames = b.familyNames;
        this.givenNames = b.givenNames;
        this.familyNameFirst = b.familyNameFirst;
        this.jobTitles = b.jobTitles;
        this.leadSites = b.leadSites;
        this.dealKinds = b.dealKinds;
        this.quoteTitle = b.quoteTitle;
        this.activities = b.activities;
        this.tickets = b.tickets;
        this.ticketReply = b.ticketReply;
        this.products = b.products;
        this.productDescription = b.productDescription;
        this.articles = b.articles;
        this.websiteSuffix = b.websiteSuffix;
        this.mobileFormat = b.mobileFormat;
        this.officeFormat = b.officeFormat;
    }

    /** ko or en (anything else: en). */
    static SeedText of(String language) {
        return language != null && language.trim().toLowerCase().startsWith("ko") ? KOREAN : ENGLISH;
    }

    String person(int family, int given) {
        String f = familyNames[family % familyNames.length];
        String g = givenNames[given % givenNames.length];
        return familyNameFirst ? f + g : g + " " + f;
    }

    String company(int prefix, int suffix) {
        return companyPrefix[prefix] + companyJoin + companySuffix[suffix];
    }

    /** A mobile number from two numbers below 10000: 010-1234-5678, or 212-555-5678 (a 555 number, never a real one). */
    String mobile(int a, int b) {
        return String.format(mobileFormat, "ko".equals(language) ? a : 200 + a % 800, b);
    }

    String office(int a, int b) {
        return String.format(officeFormat, a, b);
    }

    // ---- English (the default) -------------------------------------------------------

    static final SeedText ENGLISH = new Builder("en", 1000)
            .staff("Administrator", "ElastiCORE Demo")
            .reps(new String[][]{{"minjun", "Minjun Kim"}, {"seoyeon", "Seoyeon Lee"}, {"jihoon", "Jihoon Park"}, {"yuna", "Yuna Choi"}})
            .customers(new String[][]{{"customer", "Haneul Jung", "Bright Precision"}, {"haneul", "Bada Yoon", "Bluefield Healthcare"},
                    {"garam", "Garam Han", "Cedar Logistics"}})
            .companies(new String[]{"Bright", "Bluefield", "Summitline", "Cedar", "Novalis", "Harborview", "Silverpine", "Granitebay",
                            "Maplewood", "Orbitron", "Pinecrest", "Clearwater", "Redstone", "Lumenix", "Atlaswing", "Crescent", "Evergreen",
                            "Vertexa", "Sterlington", "Brightwater"},
                    new String[]{"Precision", "Logistics", "Healthcare", "Data Labs", "Mobility", "Energy", "Financial", "Retail",
                            "Bio", "Systems", "Networks", "Materials", "Foods", "Engineering"}, " ")
            .cities(new String[]{"Seoul", "Singapore", "Tokyo", "Sydney", "London", "Berlin", "New York", "San Francisco",
                    "Toronto", "Chicago", "Austin", "Boston"})
            .people(new String[]{"Smith", "Johnson", "Brown", "Taylor", "Anderson", "Thomas", "Moore", "Martin", "Clark", "Lewis",
                            "Walker", "Hall", "Young", "King", "Wright", "Scott"},
                    new String[]{"Olivia", "Liam", "Emma", "Noah", "Ava", "Ethan", "Sophia", "Mason", "Mia", "Lucas", "Amelia",
                            "James", "Harper", "Benjamin", "Evelyn", "Henry", "Grace", "Daniel", "Chloe", "Ryan"}, false)
            .jobTitles(new String[]{"Purchasing Manager", "IT Planning Manager", "CIO", "Sales Operations Associate", "Finance Manager",
                    "Head of Operations", "Information Security Manager", "Director of Strategy", "Office Administrator", "CTO"})
            .leadSites(new String[]{"HQ", "R&D Center", "Branch Office", "Distribution Center"})
            .deals(new String[]{"ERP Upgrade", "CRM Rollout", "Data Platform Build", "Managed Security Service", "Cloud Migration",
                    "AI Contact Center", "Logistics Control System", "Annual Maintenance Renewal", "Groupware Replacement",
                    "BI Dashboard Build"}, "%1$s - Quote %2$d")
            .activities(new String[][]{{"Kickoff meeting", "MEETING"}, {"Requirements call", "CALL"}, {"Quote follow-up e-mail", "EMAIL"},
                    {"Schedule the product demo", "TASK"}, {"Review the contract draft", "TASK"}, {"Quarterly business review", "MEETING"},
                    {"Check-in call", "CALL"}, {"Send the proposal", "EMAIL"}, {"Proof-of-concept support", "TASK"}})
            .tickets(new String[][]{
                    {"Please reissue last month's invoice", "BILLING", "Last month's invoice shows the wrong company registration number. Could you reissue it?"},
                    {"How do I turn on two-step verification?", "ACCOUNT", "I want to turn on two-step verification for the admin account but cannot find the setting."},
                    {"401 error when calling the API", "TECHNICAL", "Our integration server gets an intermittent 401 when it calls the API. Can you tell us why?"},
                    {"Cost of upgrading our subscription", "PRODUCT", "If we move from Standard to Enterprise, how is the rest of the term charged?"},
                    {"Row limit for the Excel export", "TECHNICAL", "How many rows can we download when we export a list to Excel?"},
                    {"Change of our account manager", "ACCOUNT", "We would like to change the sales representative assigned to us."},
                    {"Renewing the annual contract", "BILLING", "Our annual contract expires next month. What are the steps to renew it?"},
                    {"Training for new users", "OTHER", "We would like a training session for the people who joined this month."}},
                    "Thank you for reaching out. Here is what we found. If anything is still unclear, just reply on this ticket.")
            .products(new String[][]{
                    {"LIC-CORE", "ElastiCORE runtime license (core)", "LICENSE", "12000000"},
                    {"LIC-STD", "CRM Standard license (per user)", "LICENSE", "450000"},
                    {"LIC-ENT", "CRM Enterprise license (per user)", "LICENSE", "890000"},
                    {"SUB-STD", "CRM Standard subscription (monthly, per user)", "SUBSCRIPTION", "39000"},
                    {"SUB-ENT", "CRM Enterprise subscription (monthly, per user)", "SUBSCRIPTION", "79000"},
                    {"SUB-AI", "AI assistant add-on (monthly)", "SUBSCRIPTION", "1500000"},
                    {"SUB-BI", "Analytics dashboard add-on (monthly)", "SUBSCRIPTION", "900000"},
                    {"SVC-IMPL", "Implementation consulting (per day)", "SERVICE", "1100000"},
                    {"SVC-MIG", "Data migration (per project)", "SERVICE", "8500000"},
                    {"SVC-INTG", "System integration (per interface)", "SERVICE", "4500000"},
                    {"SVC-EDU", "User training (per session)", "SERVICE", "1800000"},
                    {"SUP-STD", "Standard maintenance (annual)", "SUPPORT", "6000000"},
                    {"SUP-PRM", "Premium maintenance 24x7 (annual)", "SUPPORT", "15000000"},
                    {"SUP-ONS", "On-site support (per visit)", "SUPPORT", "700000"}},
                    "%s - list price; the contract may give a discount.")
            .articles(new String[][]{
                    {"When is the invoice issued?", "BILLING", "invoice, billing, issue date",
                            "Invoices are issued on the 1st of each month for the previous month's use and sent to the billing contact's e-mail. If your company details changed, update them under Settings > Billing and open a ticket - we will reissue the invoice."},
                    {"How is the price worked out when I change plans?", "PRODUCT", "upgrade, downgrade, proration",
                            "An upgrade is charged pro rata for the rest of the term from the day you change. A downgrade applies from the next billing cycle."},
                    {"Turning on two-step verification", "ACCOUNT", "two-step verification, OTP, security",
                            "Go to My profile > Security, turn on two-step verification and scan the QR code with an authenticator app. Administrators can require it for the whole organization."},
                    {"The API answers 401", "TECHNICAL", "API, 401, authentication error, token",
                            "A 401 means the API key has expired or belongs to another environment (development or production). Check the key's validity and the address you call, and issue a new key if needed."},
                    {"Downloading a list to Excel", "TECHNICAL", "Excel, export, download",
                            "From the Excel menu of any list choose 'The table as shown' or 'Every matching row'. A full export writes up to 100,000 rows."},
                    {"Changing your account manager", "ACCOUNT", "account manager, sales representative",
                            "An administrator makes the change. Open a ticket and it is done within two business days."},
                    {"Renewing an annual contract", "BILLING", "renewal, expiry, contract",
                            "We send a renewal quote 60 days before the contract ends. Accepting it extends the contract by one year on the same terms; talk to your account manager to change them."},
                    {"Signing up for user training", "OTHER", "training, onboarding, new users",
                            "Online training runs every Wednesday at 2 p.m. On-site training can be ordered as the 'User training' product."},
                    {"How does a data migration work?", "PRODUCT", "migration, data transfer, import",
                            "We take your existing data as Excel files or a database dump, review the field mapping with you, then run a trial migration followed by the real one. It usually takes two to four weeks."},
                    {"How is customer data protected?", "ACCOUNT", "privacy, encryption, security",
                            "Contacts' e-mail addresses and phone numbers are encrypted when stored and shown masked on screens and in the API. Only the account owner and administrators can reach them."}})
            .contact(".com", "%1$03d-555-%2$04d", "%1$d-555-%2$04d")
            .build();

    // ---- Korean ------------------------------------------------------------------------

    static final SeedText KOREAN = new Builder("ko", 1)
            .staff("관리자", "ElastiCORE 데모")
            .reps(new String[][]{{"minjun", "김민준"}, {"seoyeon", "이서연"}, {"jihoon", "박지훈"}, {"yuna", "최유나"}})
            .customers(new String[][]{{"customer", "정하늘", "다온정밀"}, {"haneul", "윤바다", "누리헬스케어"}, {"garam", "한가람", "미르로지스"}})
            .companies(new String[]{"다온", "누리", "가람", "미르", "라온", "해든", "온새", "도담", "여울", "새솔", "아라", "푸른솔",
                            "늘봄", "하람", "윤슬", "마루", "별빛", "단비", "그린벨", "큰나래"},
                    new String[]{"정밀", "로지스", "헬스케어", "데이터랩", "모빌리티", "에너지", "파이낸셜", "리테일", "바이오",
                            "시스템즈", "네트웍스", "소재", "푸드", "엔지니어링"}, "")
            .cities(new String[]{"서울", "성남", "수원", "인천", "대전", "대구", "부산", "광주", "울산", "세종", "천안", "창원"})
            .people(new String[]{"김", "이", "박", "최", "정", "강", "조", "윤", "장", "임", "한", "오", "서", "신", "권", "황"},
                    new String[]{"도윤", "서준", "하준", "지우", "서윤", "하은", "민서", "지호", "예린", "건우", "수아", "현우", "지민",
                            "유진", "태윤", "소율", "시우", "다은", "준서", "채원"}, true)
            .jobTitles(new String[]{"구매팀장", "IT기획 과장", "CIO", "영업지원 대리", "재무팀장", "운영본부장", "정보보안 차장",
                    "전략기획 부장", "총무 대리", "CTO"})
            .leadSites(new String[]{"본사", "연구소", "지사", "물류센터"})
            .deals(new String[]{"ERP 고도화", "통합 CRM 도입", "데이터 플랫폼 구축", "보안 관제 서비스", "클라우드 전환", "AI 상담 도입",
                    "물류 관제 시스템", "연간 유지보수 갱신", "그룹웨어 교체", "BI 대시보드 구축"}, "%1$s 견적 %2$d차")
            .activities(new String[][]{{"킥오프 미팅", "MEETING"}, {"요구사항 확인 통화", "CALL"}, {"견적 회신 메일", "EMAIL"},
                    {"데모 일정 조율", "TASK"}, {"계약서 초안 검토", "TASK"}, {"분기 리뷰 미팅", "MEETING"},
                    {"담당자 안부 통화", "CALL"}, {"제안서 발송", "EMAIL"}, {"기술 검증 지원", "TASK"}})
            .tickets(new String[][]{
                    {"세금계산서 재발행 요청", "BILLING", "지난달 청구분 세금계산서의 사업자번호가 잘못 기재되었습니다. 재발행 부탁드립니다."},
                    {"로그인 2단계 인증 문의", "ACCOUNT", "관리자 계정에 2단계 인증을 켜고 싶은데 설정 위치를 모르겠습니다."},
                    {"API 호출 시 401 오류", "TECHNICAL", "연동 서버에서 API를 호출하면 간헐적으로 401이 납니다. 원인을 알 수 있을까요?"},
                    {"구독 상품 업그레이드 비용", "PRODUCT", "Standard에서 Enterprise로 바꾸면 남은 기간 비용은 어떻게 계산되나요?"},
                    {"엑셀 내보내기 행 수 제한", "TECHNICAL", "목록을 엑셀로 받을 때 최대 몇 건까지 받을 수 있나요?"},
                    {"담당자 변경 요청", "ACCOUNT", "당사 담당 영업 대표를 변경하고 싶습니다."},
                    {"연간 계약 갱신 일정", "BILLING", "다음 달 만료되는 연간 계약의 갱신 절차를 알려주세요."},
                    {"교육 일정 문의", "OTHER", "신규 입사자 대상 사용자 교육을 요청드립니다."}},
                    "문의 주셔서 감사합니다. 확인 결과를 안내드립니다. 추가로 궁금하신 점은 이 상담에 이어서 남겨 주세요.")
            .products(new String[][]{
                    {"LIC-CORE", "ElastiCORE 런타임 라이선스 (코어)", "LICENSE", "12000000"},
                    {"LIC-STD", "CRM Standard 라이선스 (사용자)", "LICENSE", "450000"},
                    {"LIC-ENT", "CRM Enterprise 라이선스 (사용자)", "LICENSE", "890000"},
                    {"SUB-STD", "CRM Standard 구독 (월, 사용자)", "SUBSCRIPTION", "39000"},
                    {"SUB-ENT", "CRM Enterprise 구독 (월, 사용자)", "SUBSCRIPTION", "79000"},
                    {"SUB-AI", "AI 상담 애드온 (월)", "SUBSCRIPTION", "1500000"},
                    {"SUB-BI", "분석 대시보드 애드온 (월)", "SUBSCRIPTION", "900000"},
                    {"SVC-IMPL", "도입 컨설팅 (M/D)", "SERVICE", "1100000"},
                    {"SVC-MIG", "데이터 이관 (건)", "SERVICE", "8500000"},
                    {"SVC-INTG", "외부 시스템 연동 (인터페이스)", "SERVICE", "4500000"},
                    {"SVC-EDU", "사용자 교육 (회)", "SERVICE", "1800000"},
                    {"SUP-STD", "표준 유지보수 (연)", "SUPPORT", "6000000"},
                    {"SUP-PRM", "프리미엄 유지보수 24x7 (연)", "SUPPORT", "15000000"},
                    {"SUP-ONS", "현장 지원 (회)", "SUPPORT", "700000"}},
                    "%s - 표준 단가이며 계약 조건에 따라 할인될 수 있습니다.")
            .articles(new String[][]{
                    {"세금계산서는 언제 발행되나요?", "BILLING", "세금계산서, 청구서, 발행일",
                            "세금계산서는 매월 1일에 전월 사용분으로 발행되며, 등록된 청구 담당자 이메일로 발송됩니다. 사업자 정보가 바뀌었다면 [설정 > 청구 정보]에서 수정한 뒤 상담을 남겨 주시면 재발행해 드립니다."},
                    {"구독 등급을 바꾸면 요금은 어떻게 계산되나요?", "PRODUCT", "업그레이드, 다운그레이드, 일할 계산",
                            "등급을 올리면 변경일부터 남은 기간을 일할 계산하여 차액만 청구합니다. 등급을 낮추면 다음 청구 주기부터 적용됩니다."},
                    {"2단계 인증을 켜는 방법", "ACCOUNT", "2단계 인증, OTP, 보안",
                            "[내 정보 > 보안]에서 2단계 인증을 켜고 인증 앱으로 QR 코드를 읽으면 됩니다. 관리자는 조직 전체에 2단계 인증을 의무로 설정할 수 있습니다."},
                    {"API 호출이 401로 실패할 때", "TECHNICAL", "API, 401, 인증 오류, 토큰",
                            "API 키가 만료되었거나 다른 환경(개발/운영)의 키를 사용하면 401이 납니다. 키의 유효 기간과 호출하는 주소를 확인하고, 키를 다시 발급해 보세요."},
                    {"목록을 엑셀로 내려받기", "TECHNICAL", "엑셀, 내보내기, 다운로드",
                            "목록 화면의 [엑셀] 메뉴에서 '지금 보이는 표' 또는 '조건 결과 전체'를 고를 수 있습니다. 전체 내보내기는 최대 10만 건까지 지원합니다."},
                    {"담당 영업 대표를 바꾸고 싶어요", "ACCOUNT", "담당자 변경, 영업 대표",
                            "담당자 변경은 관리자가 처리합니다. 상담을 남겨 주시면 영업일 기준 2일 안에 변경해 드립니다."},
                    {"연간 계약 갱신 절차", "BILLING", "계약 갱신, 만료, 재계약",
                            "만료 60일 전에 갱신 견적을 보내 드립니다. 견적을 승인하시면 같은 조건으로 1년 연장되며, 조건 변경이 필요하면 담당 영업 대표와 협의해 주세요."},
                    {"사용자 교육 신청", "OTHER", "교육, 온보딩, 신규 사용자",
                            "온라인 정기 교육은 매주 수요일 오후 2시에 열립니다. 별도 방문 교육은 '사용자 교육' 상품으로 신청할 수 있습니다."},
                    {"데이터 이관은 어떻게 진행되나요?", "PRODUCT", "이관, 마이그레이션, 데이터 옮기기",
                            "기존 시스템의 데이터를 엑셀이나 DB 덤프로 받아 매핑표를 함께 검토한 뒤 시험 이관, 본 이관 순으로 진행합니다. 보통 2~4주가 걸립니다."},
                    {"고객 데이터는 어떻게 보호되나요?", "ACCOUNT", "개인정보, 암호화, 보안",
                            "연락처의 이메일과 전화번호는 저장할 때 암호화되고, 화면과 API에는 가려진 값으로 표시됩니다. 접근 권한은 담당자와 관리자에게만 있습니다."}})
            .contact(".co.kr", "010-%1$04d-%2$04d", "02-%1$d-%2$04d")
            .build();

    // ---- builder -----------------------------------------------------------------------

    private static final class Builder {
        final String language;
        final long moneyDivisor;
        String adminName, staffCompany, companyJoin, quoteTitle, ticketReply, productDescription;
        String websiteSuffix, mobileFormat, officeFormat;
        String[][] reps, customers, activities, tickets, products, articles;
        String[] companyPrefix, companySuffix, cities, familyNames, givenNames, jobTitles, leadSites, dealKinds;
        boolean familyNameFirst;

        Builder(String language, long moneyDivisor) {
            this.language = language;
            this.moneyDivisor = moneyDivisor;
        }

        Builder staff(String adminName, String company) { this.adminName = adminName; this.staffCompany = company; return this; }
        Builder reps(String[][] v) { this.reps = v; return this; }
        Builder customers(String[][] v) { this.customers = v; return this; }
        Builder companies(String[] prefix, String[] suffix, String join) { companyPrefix = prefix; companySuffix = suffix; companyJoin = join; return this; }
        Builder cities(String[] v) { this.cities = v; return this; }
        Builder people(String[] family, String[] given, boolean familyFirst) { familyNames = family; givenNames = given; familyNameFirst = familyFirst; return this; }
        Builder jobTitles(String[] v) { this.jobTitles = v; return this; }
        Builder leadSites(String[] v) { this.leadSites = v; return this; }
        Builder deals(String[] kinds, String quoteTitle) { this.dealKinds = kinds; this.quoteTitle = quoteTitle; return this; }
        Builder activities(String[][] v) { this.activities = v; return this; }
        Builder tickets(String[][] v, String reply) { this.tickets = v; this.ticketReply = reply; return this; }
        Builder products(String[][] v, String description) { this.products = v; this.productDescription = description; return this; }
        Builder articles(String[][] v) { this.articles = v; return this; }
        Builder contact(String website, String mobile, String office) { websiteSuffix = website; mobileFormat = mobile; officeFormat = office; return this; }
        SeedText build() { return new SeedText(this); }
    }
}
