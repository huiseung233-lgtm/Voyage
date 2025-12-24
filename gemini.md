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
4.  **건설 (Settlement):** 무역으로 번 돈으로 정착지를 발전시킵니다.

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

## 🤖 AI Interaction Rules (상호작용 원칙)

> **[최우선 지침]**
> - **코드 수정 및 작성 제한:** 사용자가 프롬프트에 **'작성해줘'**라는 트리거 명령어를 포함하기 전까지는 절대 코드를 작성하거나 수정하지 않습니다. 그전까지는 분석, 기획, 제안에만 집중합니다.
> - **언어 설정:** 모든 응답은 반드시 **한국어**로 진행합니다.
> - **투두 리스트 관리:** 새로운 세션이 시작되거나 한 단계의 작업이 완료될 때마다, 아래의 'Development Roadmap'을 참고하여 현재 진행 상황과 다음 단계를 안내합니다.

---

## 🏗️ Architecture & Tech Stack
... (중략) ...

---

## 🚧 Development Roadmap

#### Phase 1~3: 기초 및 핵심 시스템 구축 (완료)
- [x] Hilt & Navigation, 지도 Canvas, 항해 엔진, 기초 무역 시스템 구현 완료.

#### Phase 4: 시스템 확장 및 폴리싱 (진행 중)
1.  **[x] 식량(Supplies) 구매 시스템:** 상점에서 아이템 구매 시 선박 보급 연동.
2.  **[x] Goals 탭 월드맵 미리보기:** 목표 화면 상단 배너에 실시간 맵 일부 표시.
3.  **[ ] ComfyUI 설치:** 로컬 에셋 제작 환경 구축 (Stable Diffusion).
4.  **[ ] GameActivity UI 정리:** 레이아웃 최적화 및 HUD/에셋 규격 정리.
5.  **[ ] 시세 변동 알고리즘:** 매일 가격이 변하는 동적 경제 시스템.
6.  **[ ] 정착지 개발 알고리즘:** 인구, 세금, 자원 생산 로직 추가.
7.  **[ ] 시나리오 및 다이얼로그:** 초기 시작 스토리 및 등장인물 대사 시스템.
8.  **[ ] 메인 배너 애니메이션:** Lottie 또는 Compose 애니메이션 연출.

#### Phase 5: 마무리 및 출시 준비
- [ ] 사운드 효과 및 배경음악 추가.
- [ ] 전체 밸런싱 및 최종 버그 수정.


---

## 📝 Recent Updates
- **[UI] HomeScreen 전면 리팩토링 (Major Overhaul):**
    - **1인칭 항해 뷰:** `bg_sea_horizon`(움직이는 바다) + `ui_window`(창틀+갑판) 레이어링으로 몰입감 있는 1인칭 시점 구현.
    - **브리핑 데스크:** 짙은 나무색(`VoyageWoodMedium`) 배경 위 양피지(`ui_calendar_parchment`) 레이아웃 적용.
    - **캘린더 최적화:** 그리드 크기 축소 및 폰트 조절로 양피지 내 완벽한 정렬 구현.
    - **애니메이션:** 바다에만 잔잔한 파도(Bobbing) 효과 적용, 창틀은 고정하여 안정감 확보.
- **[Asset]** 픽셀 아트 애셋(`bg_sea_horizon`, `ui_window`, `bg_desk_new` 등) 전면 교체 및 `FillWidth` 적용으로 비율 최적화.
- **[Theme]** 앱 전체 테마 색상을 짙은 나무색(`0xFF3E2723`)으로 통일.

## 🐛 Known Issues & Todo
- **[Bug] 시스템 상태 표시줄(Status Bar) 색상:** `Theme.kt`와 `SideEffect`로 강제 설정했으나 여전히 일부 기기/상황에서 회색으로 보이는 현상 지속. (`Accompanist SystemUiController` 도입 고려 필요)
- **[Layout] 하드코딩된 오프셋:** `HomeScreen`의 요소 배치가 특정 해상도(360dp 너비) 기준의 고정값(`offset`, `padding`)으로 되어 있어, 다양한 화면 크기에 대한 대응 필요.

## 🛠️ Optimization & Refactoring (Next Session Priority)
> **다음 작업 시작 시 최우선으로 진행할 것:**
1.  **매직 넘버 제거:** `HomeScreen.kt`에 산재한 `dp` 값들을 `dimens.xml` 또는 상수 객체로 분리.
2.  **컴포넌트 분리:** `HomeScreen.kt` 내의 `HomeCalendarDayItem` 등을 별도 파일로 분리하여 가독성 향상.
3.  **반응형 레이아웃:** `BoxWithConstraints`를 도입하여 화면 비율에 따라 오프셋이 자동 조절되도록 로직 개선.

## 🏗️ Future Architecture Notes
- **Audio:** 항해 효과음 및 BGM 구현을 위해 `ExoPlayer` 또는 `SoundPool` 도입 필요.
- **Animation:** 선원 스프라이트 및 날씨 효과 고도화를 위해 `Lottie` 또는 커스텀 `Canvas` 애니메이션 준비.

---

## 🚧 Development Roadmap

#### Phase 1~3: 기초 및 핵심 시스템 구축 (완료)
- [x] Hilt & Navigation, 지도 Canvas, 항해 엔진, 기초 무역 시스템 구현 완료.

#### Phase 4: 시스템 확장 및 폴리싱 (진행 중)
1.  **[x] 식량(Supplies) 구매 시스템:** 상점에서 아이템 구매 시 선박 보급 연동.
2.  **[x] Goals 탭 월드맵 미리보기:** 목표 화면 상단 배너에 실시간 맵 일부 표시.
3.  **[x] HomeScreen UI 개편:** 1인칭 항해 뷰 및 데스크 컨셉 적용 완료.
4.  **[ ] GameActivity UI 정리:** 레이아웃 최적화 및 HUD/에셋 규격 정리.
5.  **[ ] 시세 변동 알고리즘:** 매일 가격이 변하는 동적 경제 시스템.
6.  **[ ] 정착지 개발 알고리즘:** 인구, 세금, 자원 생산 로직 추가.
7.  **[ ] 시나리오 및 다이얼로그:** 초기 시작 스토리 및 등장인물 대사 시스템.
8.  **[ ] 메인 배너 애니메이션:** Lottie 또는 Compose 애니메이션 연출.

#### Phase 5: 마무리 및 출시 준비
- [ ] 사운드 효과 및 배경음악 추가.
- [ ] 전체 밸런싱 및 최종 버그 수정.