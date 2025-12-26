# 🚢 신대륙 개척기 (Voyage to the New World)

![Android](https://img.shields.io/badge/Android-3DDC84?style=flat-square&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack_Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)
![Hilt](https://img.shields.io/badge/DI-Hilt-orange?style=flat-square)
![Room](https://img.shields.io/badge/Database-Room-blue?style=flat-square)

**"당신의 오늘이 대륙을 만든다. 습관이 동력이 되는 항해 시뮬레이션"**

---

### 📖 프로젝트 관리 가이드 (The 3-File System)
이 프로젝트는 라이언 카슨의 3-File 시스템을 통해 관리됩니다. 모든 상세 내용은 아래 문서들을 우선합니다.
1.  **[PRD.md](./PRD.md):** 제품 요구 사항 및 핵심 시스템 정의 (기획서)
2.  **[TASKS.md](./TASKS.md):** 상세 작업 체크리스트 및 진척도 관리 (계획서)
3.  **[RULES.md](./RULES.md):** AI 에이전트 행동 지침 및 TDD 규칙 (실행규칙)

---

## 🔄 Core Gameplay Loop
1.  **규율 (Habit):** 선장이 설정한 일과를 수행하여 '추진력'을 얻습니다.
2.  **항행 (Voyage):** 매일 아침 '점호'를 통해 어제의 성과만큼 배가 전진합니다.
3.  **무역 (Trade):** 항구에 도착하면 특산물을 사고팔아 골드를 법니다.
4.  **건설 (Settlement):** 무역으로 번 돈으로 정착지를 발전시킵니다.

---

## 🏗️ Architecture & Tech Stack
- **Architecture:** MVVM + Repository Pattern
- **UI:** Jetpack Compose
- **DI:** Hilt
- **DB:** Room
- **Async:** Coroutines & Flow

---

## 📝 Recent Updates
- **[System] 3-File System 도입:** `PRD.md`, `TASKS.md`, `RULES.md`로 프로젝트 관리 체계 일원화.
- **[Config] Dev/Prod 빌드 분리:** `Voyage (Dev)`와 `Voyage` 앱을 동일 기기에 독립 설치 가능하도록 설정 (`.dev` 접미사 및 `appLabel` 적용).
- **[UI] HomeScreen 전면 리팩토링:** 1인칭 항해 뷰 및 양피지 달력 레이아웃 적용.
- **[Map] 지도 통합:** `MapActivity`를 신설하여 Goals 탭과 Game 화면에서 공통 지도로 이동 가능하도록 통합 및 항로 설정 버그 수정.

## 🐛 Known Issues
- **[Bug] 시스템 상태 표시줄(Status Bar) 색상:** `Theme.kt`와 `SideEffect`로 강제 설정했으나 여전히 일부 기기/상황에서 회색으로 보이는 현상 지속.
- **[Layout] 하드코딩된 오프셋:** `HomeScreen`의 요소 배치가 특정 해상도 기준의 고정값으로 되어 있어 반응형 레이아웃 개선 필요.

---
*과거의 Roadmap 및 상세 규칙은 TASKS.md와 RULES.md에서 관리됩니다.*
