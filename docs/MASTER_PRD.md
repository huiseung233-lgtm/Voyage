# 🚢 신대륙 개척기 (Voyage) - Product Requirements Document (PRD)

## 1. Project Overview (개요)
**"당신의 오늘이 대륙을 만든다. 습관이 동력이 되는 항해 시뮬레이션"**
'자기관리(Habit Tracker)'와 '대항해시대풍 무역 시뮬레이션(Trade Sim)'을 결합한 안드로이드 앱. 사용자의 실제 성취(할 일 달성)가 게임 내 배의 추진력이 되고, 전략적인 항해와 무역을 통해 정착지를 발전시킵니다.

## 2. Target Audience (타겟 유저)
- 단순한 체크리스트보다 시각적인 성취감을 원하는 사용자.
- '대항해시대' 등 고전 무역/항해 게임의 향수를 가진 사용자.
- 하루의 노력이 게임 속 성장으로 이어지는 몰입감을 즐기는 사용자.

## 3. Core Mechanics (핵심 시스템)

### 3.1. Propulsion & Goals (추진력 시스템)
- **Goal Types:**
    - **Daily (일일):** 매일 반복되거나 단발성 하루 목표.
    - **Weekly (주간):** 한 주 동안 달성해야 할 목표.
    - **Monthly (월간):** 장기 프로젝트형 목표.
    - **Custom:** 사용자 정의 목표 (개수 제한 없음, 특정 규칙들과 연동 가능).
- **Time System (시간 관리):**
    - **Effective Date:** 사용자의 '기상 시간(Wake Time)'을 기준으로 하루의 경계가 나뉩니다. (예: 새벽 2시는 아직 '어제'로 간주)
    - **Maintenance Window:** '하루 마감 시간(Limit Time)'부터 '기상 시간' 사이는 **선박 정비 시간**으로 간주하여 출항이 불가능합니다.
- **Daily Briefing (아침 점호):**
    - **Timing:** 기상 시간 이후 접속 시 브리핑 시작.
    - **Conscience Check (양심 고백):** 전날 점호 이후 딴짓 여부를 묻는 단계. 고백 시 정직함은 인정되나 추진력 패널티(50%) 부여.
    - **Reward:** 목표 달성 여부 및 양심 고백 결과에 따라 이동 거리(Fuel) 충전 (성공: 100km / 실패: 50km).

### 3.2. Voyage System (항해 - Game Loop)
- **Map & Navigation:**
    - **Coordinate System:** 격자(Grid) 좌표계 기반의 대양 항해.
    - **Mechanics:** '일일 브리핑'으로 충전된 '남은 거리(Remaining Distance)'만큼 자유롭게 이동 가능.
- **Shipwreck & SOS (난파 및 구조):**
    - **Trigger:** 식량 고갈 또는 골드 파산 시.
    - **Logic:** 정착지로 강제 귀환하며 배는 **기본 배(Starter Ship)**로 초기화되거나 상태 이상(DOOMED)이 발생.

### 3.3. Trade & Settlement (무역 및 정착지)
- **Trade System:**
    - **Item Types:**
        - **Trade Good:** 시세 차익을 위한 무역품.
        - **Food:** 사용 시 배의 보급품(Supplies)을 회복.
    - **Market:** 항구마다 고유 시세(Buy/Sell Price)와 재고(Stock) 보유.
- **Settlement (정착지 건설):**
    - **Foundation:** 정착지 건설 가능 항구(Port)에서 일정 골드를 지불하여 본부 창설.
    - **Buildings:**
        1. **Headquarters (본부):** 정착지의 중심. 다른 건물의 최대 레벨 제한을 해제.
        2. **Warehouse (창고):** 무역 한도 증가.
        3. **Shipyard (조선소):** 수리 및 개조.
        4. **Marketplace (시장):** 세금 수입 증가.
    - **Growth:** 골드를 투자하여 건물 레벨업 가능 (단, 본부 레벨을 초과할 수 없음).

### 3.4. Penalties (상태 이상)
- **Condition:** 목표 달성 실패 또는 수면/기상 규칙 위반 시 발생.
- **Penalty Types:**
    - **Fatigue (피로):** 늦게 잤을 때(Limit Time 위반) 발생.
    - **Laziness (나태):** 늦게 일어났을 때(Wake Time + Buffer 위반) 발생.
    - **Doom (파멸):** 두 가지가 겹쳤을 때 발생하는 최악의 상태.
- **Effects:** 이동 속도 감소, 식량 소모 증가, 항해 이벤트 악화 등.

## 4. UI/UX Structure

### 4.1. Screen Hierarchy (화면 구조)
- **Main Activity (Host):**
    - **Bottom Navigation / Tabs:**
        1.  **Main (선장실):** 1인칭 시점 홈 화면. 캘린더, 상태 확인, 게임 진입.
        2.  **Rule (규율):** 습관 규칙(Rule) 추가/수정/삭제.
        3.  **Goal (항로):** 일/주/월/커스텀 목표 관리 및 진행도 확인. (지도 배너 포함)
        4.  **Setting (조타실):** 기상/마감 시간 설정, 알림 설정, 데이터 초기화.
    - **Sub-Features:**
        - **GameActivity (Voyage View):** 아이소메트릭(Isometric) 뷰 항해, 상점, 인벤토리, 정착지 관리.
        - **MapActivity:** 전체 지도 확인 및 경로 설정 (Paper/Digital 테마 지원).

### 4.2. UX Principles
- **Aesthetic:** 앤티크(Antique), 양피지(Parchment), 목재(Wood) 질감의 고전적인 디자인.
- **Dialog Strategy:** 
    - **Functional Separation:** 기능별 패키지(`ui/trade/`, `ui/goals/` 등) 내에 `~Dialogs.kt` 파일로 분리하여 관리.
    - **Stateless Dialogs:** 데이터와 콜백을 주입받는 형태로 구현.
    - **Common UI:** `ui/common/` 패키지에서 공통 다이얼로그 관리.

## 5. Tech Stack (기술 스택)
- **Platform:** Android (Min SDK 26, Target SDK 36)
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material 3)
- **Architecture:** MVVM + Clean Architecture Guide
- **DI:** Hilt
- **Database:** Room (Local DB) - `VoyageDatabase` (Ver. 13)
- **Async:** Coroutines & Flow
