# ✅ Tasks Checklist

> **Rule:** 모든 작업은 위에서부터 순서대로 진행하며, 한 번에 하나의 체크박스만 처리한다.

## 1. 📚 Codebase Mastery & Optimization (현재 진행 중)
앱의 구조를 완전히 이해하고, 기술적 부채를 청산하는 단계.

### 1.1. Foundation Analysis
- [ ] [Learn] Manifest & Application (`AndroidManifest.xml`, `VoyageApplication.kt`) 분석 및 주석 정리
- [ ] [Learn] Dependency Injection (`AppModule.kt`) 구조 파악
- [ ] [Learn] Navigation (`MainActivity.kt`) 흐름 파악

### 1.2. Data Layer Analysis
- [ ] [Learn] Data Models (`data/model/`) 구조 분석
- [ ] [Learn] Local DB (`data/local/`) Room 설정 및 DAO 쿼리 분석

### 1.3. UI & Feature Analysis
- [ ] [Learn] Home Feature (`ui/home/`) - ViewModel & UI 연결 분석
- [ ] [Learn] Game Feature (`ui/game/`) - 게임 루프 및 렌더링 분석
- [ ] [Learn] Goals & Trade - 비즈니스 로직 분석

### 1.4. Optimization (Tidy First)
- [ ] [Refactor] 하드코딩된 문자열/치수 리소스 분리 (Hardcoded strings/dimens)
- [ ] [Refactor] `GameActivity` UI 레이아웃 최적화 (반응형 적용)
- [ ] [Refactor] 불필요한 중복 코드 제거 (Helper 함수 분리)

---

## 2. 🏗️ Phase 4: Settlement & Polish (보류)
기능 확장을 위한 작업 목록.

### 2.1. Resource Management
- [ ] [Test] 식량 소모 로직(Supply Consumption) 테스트 작성
- [ ] [Impl] 식량 소모 로직 구현
- [ ] [Test] 식량 고갈 시 패널티(표류) 테스트 작성
- [ ] [Impl] 식량 고갈 패널티 구현

### 2.2. Event System
- [ ] [Impl] 무작위 항해 이벤트(폭풍, 순풍) 기초 로직
- [ ] [UI] 이벤트 발생 시 알림 다이얼로그 구현

### 2.3. Ship Upgrade
- [ ] [DB] 선박 업그레이드 테이블/컬럼 추가
- [ ] [UI] 조선소(Shipyard) 화면 구현
