# 🚢 신대륙 개척기 (Voyage to the New World)

![Android](https://img.shields.io/badge/Android-3DDC84?style=flat-square&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack_Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)
![Hilt](https://img.shields.io/badge/DI-Hilt-orange?style=flat-square)
![Room](https://img.shields.io/badge/Database-Room-blue?style=flat-square)

**"당신의 오늘이 대륙을 만든다. 습관이 동력이 되는 항해 시뮬레이션"**

**신대륙 개척기**는 **'자기관리(Habit Tracker)'**와 **'항해 시뮬레이션(Voyage Sim)'**을 결합한 혁신적인 동기부여 게임입니다. 사용자의 실제 성취가 게임 내 물리적 거리와 경제적 이득으로 치환되어, 지속적인 동기부여를 제공합니다.

---

## 🔄 Core Gameplay Loop

1.  **규율 (Habit):** 선장(사용자)이 설정한 규칙을 수행하고 기록합니다.
2.  **목표 (Goal):** 일일/주간/월간 목표 및 커스텀 목표를 달성하여 보상을 극대화합니다.
3.  **항행 (Sailing):** 달성도에 따라 배가 전진하며(탭 투 세일) 새로운 지역을 탐험합니다.
4.  **건설 (Build):** *(Planned)* 확보한 자원으로 정착지를 발전시킵니다.

---

## 🏗️ Architecture & Tech Stack

본 프로젝트는 **Modern Android Architecture**를 준수합니다.

| Layer | Technology |
|---|---|
| **UI** | **Jetpack Compose** (Material3), Single Activity |
| **Navigation** | **Navigation Compose** (No Fragments) |
| **DI** | **Dagger-Hilt** (Dependency Injection) |
| **Data** | **Room Database**, Repository Pattern |
| **Async** | **Coroutines** & **Flow** |

### 📂 Project Structure
```
com.captain.voyage
├── data/                 # Data Layer
│   ├── local/            # Room Database, DAO, TypeConverters
│   ├── model/            # Entities (Goal, Rule, Ship, etc.)
│   └── repository/       # Single Source of Truth
├── di/                   # Hilt Modules (AppModule)
├── ui/                   # Presentation Layer (Compose)
│   ├── home/             # 홈 화면 (항해 일지, 캘린더, 게이지)
│   ├── goals/            # 목표 관리 (주간/월간/커스텀 목표)
│   ├── rules/            # 규칙 관리 (CRUD)
│   ├── game/             # 게임 화면 (항해 비주얼)
│   ├── settings/         # 설정 (시간, 알림)
│   ├── main/             # MainActivity & Navigation Host
│   └── theme/            # Compose Theme
└── utils/                # Utility Classes
```

---

## 🛠️ Features Implementation Status

### ✅ Completed
- **Architecture Overhaul:** View System(Fragment) → **Pure Compose + Hilt** 완벽 전환.
- **Home Tab (Captain's Log):**
    - 항해 일지 컨셉의 UI (양피지 스타일).
    - 일일 목표 달성률 게이지 바 (실시간 연동).
    - 월간 캘린더 그리드 (점수 표시, 오늘 날짜 강조).
    - 미래 날짜 기록 방지 로직.
- **Goals Tab (Navigation):**
    - **Daily Goal:** 일일 목표 점수 설정 및 홈 화면 연동.
    - **Custom Goal:** 기간(Start/End) 및 특정 룰(Rules) 연동 목표 생성.
    - **Stats:** 주간/월간 목표 자동 집계 시스템.
- **Rules Tab:** 규칙 생성, 수정, 삭제, 순서 변경.
- **Game Tab:** 항해 상태(정박/출항) 토글 및 애니메이션 배경(기초).

### 🚧 Roadmap (Future Development)

#### Phase 1: Exploration & Visuals (탐험과 시각화)
- [ ] **Interactive Map:** `BigMapDialog`에 실제 지도 이미지 적용 및 선박 위치 표시.
- [ ] **Sailing Animation:** 홈 화면 상단 및 게임 화면에 파도/선박 애니메이션 효과(Lottie) 추가.
- [ ] **Event System:** 항해 중 무작위 이벤트 발생 (폭풍우, 보물 상자 등).

#### Phase 2: Economy & Construction (경제와 건설)
- [ ] **Economy System:** 목표 달성 시 '골드' 지급 로직 고도화.
- [ ] **Shop & Build:** 골드를 소비하여 배를 업그레이드하거나 정착지 건물을 짓는 기능.
- [ ] **Inventory:** 획득한 아이템(특산물) 관리.

#### Phase 3: Data & Social (분석과 소셜)
- [ ] **Statistics Dashboard:** 주간/월간 성취도 그래프 시각화.
- [ ] **Backup & Restore:** Google Drive 연동 데이터 백업.

---

## 📝 Recent Updates (Changelog)

### [2025-12-22] Major Feature Update & Refactoring
- **Refactoring:**
    - `Hilt` 도입으로 의존성 주입 구조 개선.
    - `Navigation Compose` 적용 및 `Fragment` 전량 삭제.
- **UI/UX Enhancement:**
    - **Home:** 상단 'Tap to Sail' 배너와 하단 캘린더 카드(점수/오늘 표시)로 레이아웃 개편.
    - **Goals:** 커스텀 목표(제목, 기간, 룰 선택) 기능 완벽 구현.
- **Data Integrity:**
    - `Goal` 테이블 추가 및 `ScoreRecord`에 `ruleId` 연동 저장 로직 수정.
    - 커스텀 목표 점수 집계 시 데이터 불일치 방지 로직(실시간 쿼리) 적용.

---

## 🤝 Contribution
1. Fork & Clone
2. Create Feature Branch
3. Commit & Push
4. Pull Request
