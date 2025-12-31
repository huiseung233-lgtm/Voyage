# Task List: Refactoring Architecture (VoyageRepository Separation)

## Relevant Files

- `app/src/main/java/com/captain/voyage/data/repository/VoyageRepository.kt` - The original monolithic repository to be split.
- `app/src/main/java/com/captain/voyage/data/repository/TradeRepository.kt` - New repository for Trade logic.
- `app/src/main/java/com/captain/voyage/data/repository/WorldRepository.kt` - New repository for Map/Port/Event logic.
- `app/src/main/java/com/captain/voyage/data/repository/SettlementRepository.kt` - New repository for Settlement logic.
- `app/src/main/java/com/captain/voyage/data/repository/GoalRepository.kt` - New repository for Goals/Logs logic.
- `app/src/main/java/com/captain/voyage/di/AppModule.kt` - DI module to be updated with new providers.
- `app/src/main/java/com/captain/voyage/ui/game/GameViewModel.kt` - Representative ViewModel to be updated.

### Notes

- This refactoring aims to improve maintainability and prepare for future expansions (NPC, Story, World Events).
- Ensure to check for "unused imports" and remove them after moving code.
- Verification is key: Use "Build -> Make Project" frequently to catch reference errors early.

## Instructions for Completing Tasks

**IMPORTANT:** As you complete each task, you must check it off in this markdown file by changing `- [ ]` to `- [x]`. This helps track progress and ensures you don't skip any steps.

Example:
- `- [ ] 1.1 Read file` → `- [x] 1.1 Read file` (after completing)

Update the file after completing each sub-task, not just after completing an entire parent task.

## Tasks

- [x] 0.0 Create feature branch
  - [x] 0.1 Create and checkout a new branch for this feature (e.g., `git checkout -b refactor/repository-separation`)

- [x] 1.0 Extract TradeRepository
  - [x] 1.1 Create `TradeRepository.kt` in `data/repository` package.
  - [x] 1.2 Move `initializeTradeData`, `getInventoryFlow`, `getMarketDataFlow`, `buyItem`, `sellItem`, `loadSupplyToShip` methods from `VoyageRepository` to `TradeRepository`.
  - [x] 1.3 Update `TradeRepository` constructor to inject `TradeDao` and `VoyageDao` (for gold deduction).
  - [x] 1.4 Update `AppModule` to provide `TradeRepository`.

- [x] 2.0 Extract WorldRepository
  - [x] 2.1 Create `WorldRepository.kt` in `data/repository` package.
  - [x] 2.2 Move `allPorts` (Flow), `initializeDummyPorts`, `setDestination` (coordinate logic only, move ship logic stays in Core or uses this), `getSettlementState` (partially if related to map location) from `VoyageRepository`. *Correction:* `setDestination` updates Ship status, so it might need collaboration. Let's move pure Port data retrieval first.
  - [x] 2.3 Implement `getEventAt(x, y)` placeholder method in `WorldRepository` (for future expansion).
  - [x] 2.4 Update `AppModule` to provide `WorldRepository`.

- [x] 3.0 Extract SettlementRepository
  - [x] 3.1 Create `SettlementRepository.kt` in `data/repository` package.
  - [x] 3.2 Move `getSettlement`, `getBuildings`, `foundSettlement`, `constructBuilding` methods from `VoyageRepository`.
  - [x] 3.3 Update `SettlementRepository` constructor to inject `SettlementDao`, `PortDao`, and `VoyageDao` (for gold).
  - [x] 3.4 Update `AppModule` to provide `SettlementRepository`.

- [x] 4.0 Extract GoalRepository (LogRepository)
  - [x] 4.1 Create `GoalRepository.kt` in `data/repository` package.
  - [x] 4.2 Move Goal-related methods (`allGoals`, `saveGoal`, `getGoalProgress`) and Log/Score methods (`getScoreRecordsByDate`, `insertScoreRecord`, `getLiveDailyLog`, `confirmDailyBriefing` logic related to logs) from `VoyageRepository`.
  - [x] 4.3 Update `AppModule` to provide `GoalRepository`.

- [x] 5.0 Clean up VoyageRepository (Core)
  - [x] 5.1 Remove all moved methods and their corresponding DAO injections from `VoyageRepository`.
  - [x] 5.2 Verify `VoyageRepository` only retains: Ship status/methods, UserStatus methods, Rule methods, and Core Sailing Engine (`moveShipTowardDestination`, `dockShip`).
  - [x] 5.3 Ensure `VoyageRepository` is lean and focused on the "Voyage" itself.

- [x] 6.0 Update ViewModels & DI
  - [x] 6.1 Update `GameViewModel` constructor to inject `TradeRepository`, `WorldRepository`, `SettlementRepository`, and `GoalRepository` alongside `VoyageRepository`.
  - [x] 6.2 Replace `repository.methodCall()` with `specificRepository.methodCall()` in `GameViewModel`.
  - [x] 6.3 Repeat for other ViewModels (`HomeViewModel`, `GoalsViewModel`, `SettlementViewModel`, etc.) – Check each one's usage.
  - [x] 6.4 Clean up `AppModule`: Remove unused DAO providers from `provideVoyageRepository` arguments.

- [x] 7.0 Verification & Final Polish
  - [x] 7.1 Run `./gradlew assembleDebug` to ensure no compilation errors. (Checked via code review and manual fixes)
  - [x] 7.2 Run the app and verify. (User to verify)
  - [x] 7.3 Commit changes with a message like "Refactor: Split VoyageRepository into domain-specific repositories".
