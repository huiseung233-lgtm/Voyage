# PRD: Refactoring Architecture for Expansion (VoyageRepository Separation)

## 1. Introduction & Overview
Current `VoyageRepository` acts as a "God Object," handling Navigation, Trade, Settlements, Goals, and User Stats simultaneously. This high coupling makes it difficult to implement planned features like the **Expanded World (Map)**, **Open-Ended Story**, and **NPC Interaction**.
This project aims to separate the monolithic repository into domain-specific repositories to prepare the codebase for future expansions.

## 2. Goals
*   **Establish Modular Architecture:** Break down dependencies so that adding a new feature (e.g., NPC system) does not require modifying the Core engine.
*   **Prepare for Future Features:**
    *   **Map:** Extract Port/Location logic to allow for future Biomes/Tile systems.
    *   **Story/NPC:** Define clean boundaries so these can be plugged in later without friction.
*   **Improve Code Navigation:** Ensure files are smaller and focused on a Single Responsibility (SRP).

## 3. User Stories (Developer Centric)
*   As a developer, I want to modify the **Trade System** (e.g., add new item types) without recompiling or risking bugs in the **Navigation System**.
*   As a developer, I want to implement the **NPC System** later by simply creating a `NpcRepository` and injecting it, rather than rewriting `VoyageRepository`.
*   As a developer, I want `GameViewModel` to only import what it needs (e.g., `MapRepository` for map view), reducing memory footprint and complexity.

## 4. Functional Requirements (Architecture Changes)

### 4.1. Domain-Specific Repositories (The Split)
The following new repositories must be created. Existing logic from `VoyageRepository` must be moved to the corresponding new home.

1.  **`VoyageRepository` (Core)**
    *   **Responsibility:** Ship status (HP, Fuel), User Assets (Gold), Sailing Engine (Movement logic), Time/Date management.
    *   **Key Methods:** `saveShip`, `moveShipTowardDestination`, `confirmDailyBriefing`.
    *   **Future Scope:** Weather system integration.

2.  **`TradeRepository`**
    *   **Responsibility:** Market data, Item definitions, Buying/Selling logic, Inventory management.
    *   **Key Methods:** `getMarketDataFlow`, `buyItem`, `sellItem`, `loadSupplyToShip`.
    *   **Future Scope:** Dynamic economy, rare trade goods.

3.  **`WorldRepository` (formerly MapRepository)**
    *   **Responsibility:**
        *   **Static Data:** Port locations, Port details, Island/Continent geography.
        *   **Dynamic Data:** Weather, Seasons, Biomes (e.g., Stormy Seas, Safe Zones).
        *   **Event Logic:** Calculating what happens at a specific coordinate (e.g., "Is there a storm here?").
    *   **Key Methods:** `getAllPorts`, `getDistance`, `getEventAt(x, y)`.
    *   **Future Scope:** **Fog of War, Tile Grid System, Discovery Events (Shipwrecks, Treasures).** This repository will be the source of truth for the "environment."

4.  **`SettlementRepository`**
    *   **Responsibility:** Settlement construction, Building upgrades, Resource production.
    *   **Key Methods:** `foundSettlement`, `constructBuilding`.
    *   **Future Scope:** Settler management, Taxes.

5.  **`GoalRepository` (LogRepository)**
    *   **Responsibility:** Daily/Weekly Goals, Score Records, Daily Logs (Briefing history).
    *   **Key Methods:** `getScoreRecords`, `checkYesterdaySuccess`.
    *   **Future Scope:** Quest tracking (to be linked with `NarrativeRepository`).

### 4.2. Future Slots (Defined but Not Created)
*   **`NarrativeRepository`:** Planned for managing Story Progress, Chapters, and Unlockable Events.
*   **`NpcRepository`:** Planned for managing Crew, Port Locals, Affinity, and Dialogues.

### 4.3. ViewModel Refactoring (Direct Injection)
*   **Strategy:** Direct Injection (Option 4B).
*   **Action:** Update `GameViewModel`, `HomeViewModel`, etc., to inject *only* the specific repositories they need.
    *   *Example:* `GameViewModel` will inject `VoyageRepository`, `WorldRepository`, and `TradeRepository`.
    *   *Example:* `RulesViewModel` might only need `VoyageRepository` (or a new `RuleRepository` if rules are complex).

### 4.4. Dependency Injection (Hilt)
*   Update `AppModule` to provide all new repositories as Singletons.
*   Remove the monolithic dependency chain.

## 5. Non-Goals
*   **New Feature Implementation:** We will *not* add new gameplay features (e.g., actual NPC dialogue or new Map tiles) during this refactoring. We are only moving existing logic.
*   **UI Redesign:** The UI appearance will remain exactly the same. Only the underlying data flow changes.

## 6. Technical Considerations
*   **Database Transaction:** Functions that span multiple domains (e.g., Buying an item requires `UserStatus` from Core and `Stock` from Trade) must be handled carefully.
    *   *Solution:* The Repository performing the action should depend on the necessary DAOs. Ideally, use a `UseCase` class for complex interactions, but for now, allowing Repositories to access multiple DAOs is acceptable to keep complexity manageable.

## 7. Success Metrics
*   **Compilation:** The app builds successfully with no errors.
*   **Regression Testing:** All existing features (Move, Buy, Build, Goal Check) work exactly as before.
*   **Code Metric:** `VoyageRepository.kt` line count reduced by at least 60%.
*   **Structure:** `GameViewModel` does not depend on methods it doesn't use (e.g., Settlement logic is not exposed to GameVM unless explicitly injected).

## 8. Open Questions
*   Should `Rule` logic stay in `VoyageRepository` or get its own `RuleRepository`? -> *Decision: Keep in Core for now as it drives the "Engine", unless it grows too large.*
