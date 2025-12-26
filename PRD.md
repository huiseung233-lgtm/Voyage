# 🚢 신대륙 개척기 (Voyage) - Product Requirements Document (PRD)

## 1. Project Overview (개요)
**"당신의 오늘이 대륙을 만든다. 습관이 동력이 되는 항해 시뮬레이션"**
'자기관리(Habit Tracker)'와 '대항해시대풍 무역 시뮬레이션(Trade Sim)'을 결합한 안드로이드 앱입니다. 사용자의 실제 성취(할 일 달성)가 게임 내 배의 추진력이 되고, 전략적인 항해와 무역을 통해 정착지를 발전시킵니다.

## 2. Target Audience (타겟 유저)
- 단순한 체크리스트보다 시각적인 성취감을 원하는 사용자.
- '대항해시대' 같은 고전 무역/항해 게임의 향수를 가진 사용자.
- 하루의 노력이 게임 속 성장으로 이어지는 몰입감을 즐기는 사용자.

## 3. Core Mechanics (핵심 시스템)

### 3.1. Propulsion System (추진력 - Habit)
- **Daily Goal:** 사용자가 설정한 일일 목표(규율)를 수행.
- **Daily Briefing (아침 점호):** 매일 아침 전날의 성과를 보고.
    - 성공 시: 100km 이동 (순풍).
    - 실패/양심고백 시: 50km 이동 (역풍/정체).
- **Penalty:** 연속 실패 시 선원 사기 저하, 반란(Game Over) 등의 리스크 발생.

### 3.2. Voyage System (항해 - Game Loop)
- **Navigation:** 실제 지도 기반이 아닌 격자(Grid) 기반의 대양 항해.
- **Visuals:**
    - **Home:** 1인칭 선장실 시점 (책상 위 양피지 달력, 창밖의 바다).
    - **Game:** 3D/2D 탑뷰 또는 쿼터뷰 항해 화면.
- **Events:** 항해 중 날씨 변화, 표류, 보급품 소모 등.

### 3.3. Trade Economy (무역 - Economy)
- **Ports:** 각 항구는 고유한 특산물과 시세를 가짐.
- **Dynamic Pricing:** 수요/공급 또는 랜덤 이벤트에 따른 시세 변동.
- **Profit:** 시세 차익을 통해 골드(Gold) 획득 -> 배 업그레이드 및 정착지 투자.

## 4. UI/UX Principles
- **Aesthetic:** 앤티크(Antique), 양피지(Parchment), 목재(Wood) 질감의 고전적인 디자인.
- **Immersion:** 1인칭 선장 시점의 몰입감 중시 (메인 화면).
- **Simplicity:** 복잡한 조작 없이 직관적인 터치 인터페이스.

## 5. Tech Stack (기술 스택)
- **Platform:** Android (Min SDK 26, Target SDK 36)
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material 3)
- **Architecture:** MVVM + Clean Architecture Guide
- **DI:** Hilt
- **Database:** Room (Local DB)
- **Async:** Coroutines & Flow
