# 📚 Codebase Mastery & Optimization Plan (학습 및 최적화 로드맵)

**[최우선 과제]** 앱의 구조를 바닥부터 완전히 이해하고, 이를 바탕으로 최적화(Refactoring)를 수행합니다. 이 과정이 완료되기 전까지는 새로운 기능 개발을 잠시 중단합니다.

## 1. 🏗️ Foundation (구조와 진입점)
- [ ] **Manifest & Application (`AndroidManifest.xml`, `VoyageApplication.kt`)**: 앱 권한, 설정, Hilt 초기화 이해.
- [ ] **Dependency Injection (`AppModule.kt`)**: 데이터베이스와 전역 객체들이 어떻게 생성되고 주입되는지 파악.
- [ ] **Navigation (`MainActivity.kt`, `VoyageNavigation.kt`)**: `BottomNavigation`과 화면 전환(NavHost) 흐름 이해.

## 2. 💾 Data Layer (데이터 저장소)
- [ ] **Models (`data/model/` 패키지)**: `Ship`, `DailyLog`, `Port` 등 핵심 데이터 클래스 구조 분석.
- [ ] **Local DB (`data/local/` 패키지)**: Room Database 설정과 DAO(쿼리) 분석. 관계형 데이터 처리 방식 이해.

## 3. 🖥️ UI & Feature Logic (화면별 심층 분석)
- [ ] **Home Feature (`ui/home/`)**:
    - `HomeViewModel`: 데이터를 가공해 UI 상태(`StateFlow`)로 변환하는 패턴.
    - `HomeScreen`: `LazyColumn`, `Canvas`, 애니메이션 등 UI 구성 요소.
- [ ] **Game Loop (`ui/game/`)**:
    - `GameActivity` & `GameViewModel`: 실시간 게임 로직, 3D/2D 렌더링 방식.
    - `WorldMapView` (`MapComposables.kt`): `Canvas` 드로잉 최적화 포인트 찾기.
- [ ] **Goals & Trade (`ui/goals/`, `ui/trade/`)**:
    - 복잡한 비즈니스 로직(목표 달성 체크, 시세 계산)과 UI 상호작용.

## 4. 🚀 Optimization Targets (최적화 목표 - 분석 후 실행)
- [ ] **하드코딩 제거**: `dp`, `color` 값들을 리소스 파일(`dimens.xml`, `colors.xml`)로 분리.
- [ ] **성능 최적화**: 불필요한 리컴포지션(Recomposition) 방지, 메모리 누수 점검.
- [ ] **구조 개선**: ViewModel 비대화 방지(UseCases 분리), 중복 코드 제거(공통 컴포넌트화).

---

# 📋 신대륙 개척기 작업 목록 (Pending)

## 🏗️ Phase 4: Settlement & Polish (보류)
- [ ] **Event System**
    - [ ] 항해 중 무작위 인카운터 로직 구현 (폭풍우, 보물 발견 등)
    - [ ] 이벤트 결과에 따른 자원(골드, 식량) 변동 시스템
- [ ] **Ship Upgrade**
    - [ ] 조선소(Shipyard) 건물을 통한 배 성능 강화 (속도, 적재량)
    - [ ] 선체 업그레이드 시각적 반영 (아이콘 또는 텍스트 변경)
- [ ] **Resource Management**
    - [ ] 식량(Supplies) 소모 로직 정교화
    - [ ] 식량 부족 시 패널티(표류) 구현

## 🛠️ 개선 및 리팩토링 (Polish)
- [ ] **UI/UX 개선**
    - [ ] 전체적인 테마 색상(Material 3) 및 타이포그래피 정돈
    - [ ] 항해 중 애니메이션 효과 추가 (부드러운 배의 움직임)
- [ ] **안정성 강화**
    - [ ] 데이터베이스 마이그레이션 정책 수립
    - [ ] 주요 로직(이동 거리 계산 등) 유닛 테스트 추가

## 💡 아이디어 & 추가 기능
- [ ] 업적(Achievement) 시스템 (총 항해 거리, 누적 골드 등)
- [ ] 날씨 시스템 (지도상의 시각 효과 및 항해 속도 영향)
- [ ] NPC 함선 및 다른 세력과의 상호작용
- [ ] 아침 점호 알림 문구를 매일 조금씩 다르게 하기 (선원들의 무작위 대사 등)

## 🐛 발견된 버그 (Fix Me!)
- [ ] **AlarmReceiver 부팅 로직 수정**: 폰 재부팅 시 `BOOT_COMPLETED` 신호를 받으면 알림을 띄우지 않고 알람만 재등록하도록 수정 필요 (현재는 무조건 알림이 뜸). `RECEIVE_BOOT_COMPLETED` 권한도 추가 필요.