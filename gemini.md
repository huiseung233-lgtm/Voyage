# 🚢 신대륙 개척기 (Voyage to the New World)

![Android](https://img.shields.io/badge/Android-3DDC84?style=flat-square&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack_Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)
![Hilt](https://img.shields.io/badge/DI-Hilt-orange?style=flat-square)
![Room](https://img.shields.io/badge/Database-Room-blue?style=flat-square)

**"당신의 오늘이 대륙을 만든다. 습관이 동력이 되는 항해 시뮬레이션"**

**신대륙 개척기**는 **'자기관리(Habit Tracker)'**와 **'대항해시대풍 무역 시뮬레이션(Trade Sim)'**을 결합한 혁신적인 동기부여 게임입니다. 사용자의 실제 성취가 배의 추진력이 되고, 전략적인 항해와 무역을 통해 정착지를 발전시켜야 합니다.

---

## 💡 Core Development Principle (개발 원칙)
> **"Functionality First, Aesthetics Later"**
> - 현재 단계에서는 **핵심 기능 구현(항해 로직, 데이터 연동)**에 집중합니다.
> - 부드러운 이동 애니메이션이나 고품질 아트워크 등의 시각적 요소는 추후 폴리싱 단계에서 적용합니다.
> - 단, 나중에 아트워크를 적용할 때 구조적 문제가 발생하지 않도록 **확장성 있는 코드 구조**를 유지합니다.

---

## 🔄 Core Gameplay Loop

1.  **규율 (Habit):** 선장이 설정한 일과를 수행하여 '추진력'을 얻습니다.
2.  **항행 (Voyage):** 목표 달성도에 따라 배가 전진하며, 항해 중 다양한 이벤트(폭풍, 해적 등)를 마주합니다.
3.  **무역 (Trade):** 항구마다 다른 특산물 시세를 이용하여 부를 축적합니다.
4.  **건설 (Settlement):** 무역으로 번 돈으로 신대륙의 정착지를 발전시키고 함대를 업그레이드합니다.

---

## 🎮 Detailed Mechanics

### 🚀 A. 추진력 시스템 (Propulsion Logic)
사용자의 일일 목표(Daily Goal) 달성 여부가 다음 날의 항해 거리를 결정합니다.
- **성공 (Success):** 어제의 목표를 달성했다면 배가 100km(기본) 전진합니다.
- **실패 (Fail):** 달성하지 못했다면 배가 50km(절반)만 전진합니다.
- **아침 점호:** 매일 아침 '출항하기' 버튼을 누르면 정산과 함께 배가 이동하고, 하루 동안은 '항해 중' 상태를 유지합니다.

### 💰 B. 경제 및 무역 시스템 (Economy & Trade)
단순한 골드 보상이 아닌, 전략적인 상거래가 핵심입니다.
- **항구 (Port):** 각 섬/대륙은 고유한 특산물과 수요/공급 물품이 존재합니다.
- **시세 차익:** A항구에서 쌀 때 사서 B항구에 비싸게 파는 전략이 필요합니다.
- **보급 (Supply):** 항해 거리에 비례하여 식량, 물, 자재 등의 소모품을 미리 구매하고 적재해야 합니다.

### 🌊 C. 리스크 관리 (Risk Management)
- **항해 준비:** 얼마나 먼 섬을 목표로 할 것인가? (Risk vs Return)
- **이벤트:** 항해 도중 폭풍우(내구도 감소), 괴혈병(선원 감소), 해적(전투 혹은 협상) 등의 이벤트가 발생하며, 이를 대비한 아이템(대포, 라임 등)이 필요합니다.

---

## 🏗️ Architecture & Tech Stack

**Modern Android Architecture** (Single Activity, Compose, Hilt, MVVM)

### 📂 Project Structure
```
com.captain.voyage
├── data/                 # Data Layer (Room, Repository)
├── di/                   # Dependency Injection (Hilt)
├── ui/                   # UI Layer (Compose)
│   ├── home/             # 항해 일지 (Main Dashboard)
│   ├── goals/            # 목표 및 항로 설정
│   ├── rules/            # 규칙 관리
│   ├── game/             # 인게임 (항해/무역/전투 비주얼)
│   ├── trade/            # (Planned) 무역소 화면
│   └── settlement/       # (Planned) 정착지 건설 화면
└── utils/                # Utility
```

---

## 🚧 Development Roadmap

#### Phase 1: Foundation & Habit (완료)
- [x] Hilt & Navigation Compose 아키텍처 구축.
- [x] 항해 일지(Home) 및 목표 관리(Goals) 시스템 구현.
- [x] 커스텀 목표 및 데이터 일치성 로직 확보.

#### Phase 2: Visualization & Map (완료)
- [x] **Interactive Map:** Canvas 기반의 월드 맵 구현 (격자선, 항구, 배 표시).
- [x] **Navigation:** 지도 터치 시 목적지 설정 및 자석(Snapping) 기능 구현.
- [x] **Sailing Engine:** 일일 목표 달성 여부에 따른 이동 거리 계산 및 벡터 이동 로직 구현.

#### Phase 3: Voyage & Trade (진행 중)
- [ ] **Daily Briefing:** 아침 점호(정산) 팝업 및 UI 구현.
- [ ] **Trade System:** 항구 데이터(Port), 교역품(TradeItem) 모델링 및 거래 화면 구현.
- [ ] **Event System:** 항해 중 무작위 인카운터(Event) 로직 구현.

#### Phase 4: Settlement (최종 콘텐츠)
- [ ] **Building:** 정착지 건물 건설 및 업그레이드.
- [ ] **Endless Mode:** 더 먼 바다로의 탐험.

---

## 📝 Recent Updates
- **[Refactor]** Fragment 제거 및 Pure Compose 아키텍처로 전환.
- **[Feature]** 항해 일지 컨셉의 홈 화면 및 커스텀 목표 기능 추가.
- **[Feature]** 항해 엔진(Voyage Engine) 탑재 및 지도 목적지 설정 기능 구현.
- **[Fix]** Deprecated API(Divider, Icons) 수정 및 안정성(Crash Prevention) 확보.