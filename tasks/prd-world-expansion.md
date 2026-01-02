# PRD: World Expansion - Dual Continents & Archipelago

## 1. Introduction / Overview
맵의 크기를 대폭 확장하고(-10000 ~ 10000), 동/서 양쪽에 거대 대륙을 배치하는 새로운 지형 구조를 도입합니다. 탐험의 재미를 위해 전장의 안개와 육지 충돌 로직을 구현하며, 기존의 무역 시스템을 심화합니다.

## 2. Goals
*   **Massive World:** 맵 크기를 20,000x20,000으로 확장하여 광활한 모험을 제공합니다.
*   **Strategic Geography:** 동/서 양쪽의 대륙과 그 사이의 섬들을 통해 전략적인 항로 선택을 유도합니다.
*   **Fog & Collision:** 안개 시스템과 육지 충돌 로직으로 현실적인 항해 경험을 구현합니다.
*   **Trade Depth:** 25개 항구별 특산품과 수요를 설정하여 무역의 재미를 더합니다.

## 3. User Stories
*   **탐험가:** 동쪽 끝에 있다는 전설의 대륙을 찾기 위해, 중간의 섬들을 징검다리 삼아 긴 항해를 떠난다.
*   **항해사:** 빠른 속도(2000km/day)로 넓은 바다를 누비며, 암초와 섬들을 피해 최단 경로를 찾는다.
*   **무역상:** 서쪽 대륙의 특산품을 동쪽 대륙에 비싸게 팔아 거상을 꿈꾼다.

## 4. Functional Requirements

### 4.1. World Topology (지형)
*   **Map Size:** -10000 ~ +10000 (Total 20,000 units).
*   **East Continent:** 좌표 (8000, 0) 부근에 위치한 거대 육지.
*   **West Continent:** 좌표 (-8000, 0) 부근에 위치한 거대 육지.
*   **Mid-size Lands:** 맵 중앙 및 남북에 3~4개의 중간 크기 섬 배치.
*   **Archipelago:** 나머지 영역에 다수의 작은 섬들 분산 배치.
*   **Total Ports:** 약 25개 (대륙별 2~3개, 중간 섬, 작은 섬들에 분배).

### 4.2. Movement & Collision
*   **Speed:** 기본 이동 속도를 **2000**으로 설정 (테스트용 치트).
*   **Collision:** 육지 타일 위로는 이동 불가. 목적지 설정 시 육지 경계에서 멈추거나 우회.

### 4.3. Fog of War
*   **Radius:** 배 주변 500 유닛 반경을 밝힘.
*   **Persistence:** 탐험한 지역은 영구적으로 밝혀짐(DB 저장).

### 4.4. Port Facilities & Trade
*   **Facilities:** 항구별 조선소, 주점, 시장 보유 여부 설정.
*   **Trade:** 각 항구별 고유 특산품 및 수요 품목 설정.

## 5. Technical Considerations
*   **Rendering:** `MapComposables`에서 육지(녹색/갈색)와 안개(검은색 마스크)를 렌더링.
*   **Data:** `WorldData`에 지형(Circle/Polygon) 및 항구/무역 데이터 정의.

## 6. Success Metrics
*   맵의 양쪽 끝에 대륙이 시각적으로 확인되는가?
*   배가 2000의 속도로 빠르게 이동하며, 육지를 통과하지 않는가?
*   안개가 탐험에 따라 정상적으로 걷히는가?
