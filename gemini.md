# 🚢 신대륙 개척기 (Voyage to the New World)

![Android](https://img.shields.io/badge/Android-3DDC84?style=flat-square&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![MVVM](https://img.shields.io/badge/Architecture-MVVM-blue?style=flat-square)
![Room](https://img.shields.io/badge/Database-Room-orange?style=flat-square)

**"당신의 오늘이 대륙을 만든다. 습관이 동력이 되는 항해 시뮬레이션"**

**신대륙 개척기**는 **'자기관리(Habit Tracker)'**와 **'항해 시뮬레이션(Voyage Sim)'**, 그리고 **'도시 건설(Settlement Builder)'**을 결합한 혁신적인 동기부여 게임입니다. 사용자의 실제 성취가 게임 내 물리적 거리와 경제적 이득으로 치환되어, 기존 앱들이 해결하지 못한 '장기적 동기부여의 부재'를 해결합니다.

---

## 🔄 Core Gameplay Loop

1.  **규율 (Habit):** 선장(사용자)이 설정한 일과를 수행합니다.
2.  **항행 (Sailing):** 달성도에 따라 배가 전진하며 새로운 섬을 발견합니다.
3.  **무역 (Trade):** 섬의 특산물을 매매하여 금화를 벌고 건설 자재를 확보합니다.
4.  **건설 (Build):** 확보한 자원들로 신대륙의 정착지를 발전시킵니다.

---

## 🎮 Detailed Mechanics

### 🚀 A. 추진력 시스템 (Propulsion Logic)
사용자의 일일 목표 달성 여부는 선박의 '추진력'에 직접적인 영향을 미칩니다.
- **정상 순항 (Full Sail):** 일일 목표 달성 시 20km 전진.
- **저속 항행 (Low Speed):** 일일 목표 미달 시 10km 전진.
- **지휘 효율 배율 (Command Multiplier):**
    - **피로 상태 (Fatigue):** 취침 시간 미준수 시 이동 거리 x0.7 페널티 (선원들의 태업).
    - **표류 상태 (Drift):** 기상 시간 미준수 시 방향 상실. 전진 거리 0km 및 무작위 방향 표류.

### 💰 B. 경제 및 보상 시스템 (Economic Incentives)
시간적 단위의 목표 달성은 게임 내 경제적 우위로 직결됩니다.
- **주간 목표 (Weekly Milestone):** 달성 시 차주 정박하는 섬에서 '무역 할인권' 획득 (자재/특산물 20% 할인).
- **월간 목표 (Monthly Legend):** 달성 시 다음 섬에서 '전설적 보물' 발견 (특수 건물용 아이템, 대량의 금화).
- **커스텀 목표 (Captain's Code):** '지휘관 명성치' 상승. 선원 모집이나 함선 업그레이드 해금 조건으로 활용.

### 🗺️ C. 탐험과 무역 (Exploration & Trade)
- **가변적 맵:** 안개에 싸인 바다를 탐험하며 섬들을 발견합니다.
- **특산물 시스템:** 각 섬은 고유한 자원(목재, 석재, 철광, 향신료 등)을 보유합니다.
- **무역의 묘미:** 시세 차익(예: 섬 A에서 10G 구매 -> 섬 B에서 50G 판매)을 통해 건설 자금을 마련합니다.

### 🏗️ D. 정착지 건설 (Settlement Expansion)
항해의 끝에서 마주할 신대륙은 사용자의 성취를 증명하는 공간입니다.
- **인프라 구축:** 부두(수익 증대), 창고(보관량 증가), 주점(정보 획득) 등을 건설.
- **문명 발전:** 정착지가 커질수록 선장님의 '개척 등급'이 상승하며 더 먼 바다로 나갈 수 있는 대형 함선 해금.

---

## 🛠 Tech Stack

| Category | Technology |
|Data|**Room Database** (SQLite abstraction), Shared Preferences|
|UI|**XML Layouts** (View System), Material Design Components|
|Architecture|**MVVM** (Model-View-ViewModel), Repository Pattern|
|Async|**Coroutines** & **Flow**|
|Language|**Kotlin**|
|Build|**Gradle KTS** (Kotlin DSL)|

---

## 📂 Project Structure

본 프로젝트는 **Clean Architecture** 원칙을 지향하며, 관심사의 분리를 위해 패키지가 구조화되어 있습니다.

```
com.captain.voyage
├── data/                 # Data Layer
│   ├── local/            # Room Database, DAO
│   ├── model/            # Data Entities (DailyLog, Rule, Ship, etc.)
│   └── repository/       # Single Source of Truth
├── ui/                   # Presentation Layer (MVVM)
│   ├── home/             # Main Dashboard (항해 현황)
│   ├── rules/            # Rule Management (규율 설정)
│   ├── goals/            # Goal Tracking (목표 및 보상)
│   ├── game/             # Gamification (무역, 건설, 탐험 로직)
│   ├── settings/         # App Configuration
│   └── main/             # MainActivity (Entry Point)
└── utils/                # Utility Classes (Alarm, Notification, Time)
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Koala Feature Drop | 2024.1.1 Patch 1 이상 권장
- JDK 17 이상

### Installation
1. Repository를 Clone 합니다.
   ```bash
   git clone https://github.com/your-username/voyage-android.git
   ```
2. Android Studio에서 프로젝트를 엽니다.
3. `Gradle Sync`를 실행하여 의존성을 설치합니다.
4. 에뮬레이터 또는 실제 기기에서 앱을 실행합니다 (Min SDK 26+).

---

## 📝 Database Schema (Room) - *To Be Updated*

| Entity | Description | Key Fields |
|:---:|:---|:---|
| **DailyLog** | 일일 활동 기록 | `date`, `content`, `score` |
| **Rule** | 사용자 정의 규칙 | `id`, `title`, `description`, `isActive` |
| **ScoreRecord** | 점수 변동 내역 | `timestamp`, `amount`, `reason` |
| **UserStatus** | 사용자 레벨/상태 | `level`, `currentExp`, `shipId` |
| **TradeItem** *(Planned)* | 무역품 정보 | `itemId`, `name`, `basePrice` |
| **Island** *(Planned)* | 발견된 섬 정보 | `islandId`, `location`, `specialty` |

---

## 🤝 Contribution

1. 이 저장소를 Fork 합니다.
2. 새로운 Feature 브랜치를 생성합니다 (`git checkout -b feature/AmazingFeature`).
3. 변경 사항을 Commit 합니다 (`git commit -m 'Add some AmazingFeature'`).
4. Branch에 Push 합니다 (`git push origin feature/AmazingFeature`).
5. Pull Request를 생성합니다.

---

## 🛠️ Troubleshooting & Updates

### [2025-12-22] Build Error: Invalid `compileSdk` Configuration
- **Issue:** `app/build.gradle.kts`에서 `compileSdk { version = release(36) }`와 같이 잘못된 문법과 미지원 SDK 버전(36)을 사용하여 빌드 에러 발생.
- **Fix:** 
    - `compileSdk` 설정을 표준 문법인 `compileSdk = 35`로 수정.
    - `targetSdk`를 안정적인 최신 버전인 `35`로 하향 조정.
- **Outcome:** 빌드 설정 오류 해결 및 프로젝트 안정성 확보.

### [2025-12-22] Major Architecture & UI Overhaul
- **Architecture Migration:**
    - **Hilt Integration:** `Dagger-Hilt` 도입으로 의존성 주입 자동화 및 `ViewModelFactory` 제거.
    - **Navigation Compose:** 기존 `Fragment` 기반 네비게이션을 제거하고 순수 Compose `NavHost`로 전환. `MainActivity`는 단일 진입점(Single Activity) 역할만 수행.
    - **Cleanup:** 사용하지 않는 XML 레이아웃(Activity, Fragment, Dialog) 및 View 기반 Kotlin 클래스(Adapter) 전량 삭제.
- **UI Redesign (Home Tab):**
    - **Concept:** "항해 일지(Logbook)" 컨셉의 몰입형 UI 구현.
    - **Layout:** 
        - 상단 30%: 클릭 가능한 '출항(Sail)' 배너 (배경 이미지 + 그라데이션).
        - 중앙/하단: 스크롤 가능한 양피지 스타일 카드.
    - **Features:** 
        - 일일 목표 달성률 게이지 바(Progress Bar) 추가.
        - 직관적인 월간 캘린더 그리드 통합 (클릭 시 기록 팝업).
- **Tech Stack Update:**
    - `androidx.compose.material3` 적극 활용.
    - `compileSdk` 및 `targetSdk`를 **36**으로 상향 조정 (최신 라이브러리 호환성).