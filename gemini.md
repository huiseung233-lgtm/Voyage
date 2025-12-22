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
2.  **항행 (Voyage):** 매일 아침 '점호'를 통해 어제의 성과만큼 배가 전진합니다.
3.  **무역 (Trade):** 항구에 도착하면 특산물을 사고팔아 골드를 법니다.
4.  **건설 (Settlement):** (예정) 무역으로 번 돈으로 정착지를 발전시킵니다.

---

## 🎮 Detailed Mechanics

### 🚀 A. 추진력 시스템 (Propulsion Logic)
사용자의 일일 목표(Daily Goal) 달성 여부가 다음 날의 항해 거리를 결정합니다.
- **아침 점호 (Daily Briefing):** 매일 아침 [출항하기] 버튼을 누르면 어제의 성과를 보고받습니다.
    - **양심 점검:** 스마트폰 사용 등 규율 위반 여부를 스스로 고백합니다.
    - **성과 정산:** 성공 시 100km, 실패(혹은 고백) 시 50km를 이동합니다.

### 💰 B. 경제 및 무역 시스템 (Economy & Trade)
단순한 골드 보상이 아닌, 전략적인 상거래가 핵심입니다.
- **항구 (Port):** 각 섬/대륙은 고유한 특산물(Item)과 상점(Market)을 가집니다.
- **시세 차익:** A항구에서 쌀 때 사서 B항구에 비싸게 파는 전략이 필요합니다.
- **무역 화면:** 항구에 정박 중일 때 상점을 열어 물건을 사고팔 수 있습니다.

### 🌊 C. 리스크 관리 (Risk Management)
- **항해 준비:** 얼마나 먼 섬을 목표로 할 것인가? (Risk vs Return)
- **자원 소모:** 항해 중에는 식량(Supplies)이 지속적으로 소모됩니다. 식량이 0이 되면 표류합니다.

---

## 🏗️ Architecture & Tech Stack

**Modern Android Architecture** (Single Activity, Compose, Hilt, MVVM)

### 📂 Project Structure
```
com.captain.voyage
├── data/                 # Data Layer (Room, Repository)
│   ├── model/            # Entity (Ship, Port, Item, Market...)
│   └── local/            # DAO
├── di/                   # Dependency Injection (Hilt)
├── ui/                   # UI Layer (Compose)
│   ├── home/             # 항해 일지 (Main Dashboard)
│   ├── goals/            # 목표 및 항로 설정
│   ├── game/             # 인게임 (항해/무역/전투 비주얼)
│   ├── trade/            # 무역 시스템 화면
│   └── ...
└── utils/                # Utility
```

---

## 🚧 Development Roadmap

#### Phase 1: Foundation & Habit (완료)
- [x] Hilt & Navigation Compose 아키텍처 구축.
- [x] 항해 일지(Home) 및 목표 관리(Goals) 시스템 구현.

#### Phase 2: Visualization & Map (완료)
- [x] **Interactive Map:** Canvas 기반의 월드 맵 구현.
- [x] **Navigation:** 지도 터치 시 목적지 설정 및 자석(Snapping) 기능 구현.
- [x] **Sailing Engine:** 일일 목표 달성 여부에 따른 이동 거리 계산 및 벡터 이동 로직 구현.

#### Phase 3: Voyage & Trade (완료)
- [x] **Daily Briefing:** 아침 점호(정산) 및 양심 점검 팝업 구현.
- [x] **Trade System:** Item, Market, Inventory 데이터 모델링 및 거래 화면 구현.
- [x] **Economy:** 골드 및 아이템 재고 연동.

#### Phase 4: Settlement (다음 단계)
- [ ] **Event System:** 항해 중 무작위 인카운터(Event) 로직 구현.
- [ ] **Ship Upgrade:** 배 업그레이드 시스템.
- [ ] **Building:** 정착지 건물 건설 및 업그레이드.

---

## 📝 Recent Updates
- **[Feature]** 아침 점호(Daily Briefing) 다이얼로그 및 양심 고백 기능 추가.
- **[Feature]** 무역 시스템(Trade System) 구현: 아이템 사고 팔기 가능.
- **[Fix]** 게임 화면 세로 모드 고정 및 UI 안정화.
