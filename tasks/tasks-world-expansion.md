# Tasks: World Expansion - Dual Continents & Archipelago

## Relevant Files
- `app/src/main/java/com/captain/voyage/utils/GameConstants.kt` - Map configuration.
- `app/src/main/java/com/captain/voyage/data/model/Port.kt` - Port entity updates.
- `app/src/main/java/com/captain/voyage/data/model/MapTile.kt` - New tile/terrain models.
- `app/src/main/java/com/captain/voyage/data/initial/WorldData.kt` - World topology and port data.
- `app/src/main/java/com/captain/voyage/data/repository/WorldRepository.kt` - Collision logic.
- `app/src/main/java/com/captain/voyage/ui/game/MapComposables.kt` - Map rendering (Land/Fog).

### Notes
- Ensure coordinate system changes (-10000 to 10000) are consistent across all files.

## Instructions for Completing Tasks
**IMPORTANT:** As you complete each task, you must check it off in this markdown file by changing `- [ ]` to `- [x]`. This helps track progress and ensures you don't skip any steps.

Example:
- `- [ ] 1.1 Read file` → `- [x] 1.1 Read file` (after completing)

Update the file after completing each sub-task, not just after completing an entire parent task.

## Tasks

- [x] 0.0 Create feature branch
  - [x] 0.1 Create and checkout a new branch for this feature (e.g., `git checkout -b feature/world-expansion`)
- [x] 1.0 Update Configuration & Models
  - [x] 1.1 Update `GameConstants.kt` with new MAP_SIZE (20000), SPEED (2000), and other constants.
  - [x] 1.2 Add facility flags (`hasShipyard`, `hasTavern`, `hasMarket`) to `Port` entity.
  - [x] 1.3 Create `MapTile.kt` with `TileType` (LAND/SEA) and `BiomeType` enums.
  - [x] 1.4 Increment `VoyageDatabase` version to 15.
- [x] 2.0 Define World Data
  - [x] 2.1 Create `WorldData.kt` and define `LandShape` class (e.g., Circle, Polygon).
  - [x] 2.2 Define world topology in `WorldData`: East/West Continents, Mid-lands, Archipelago using shapes.
  - [x] 2.3 Define `ports` list (25 ports) in `WorldData` with correct coordinates and facilities.
- [x] 3.0 Implement Core Logic
  - [x] 3.1 Implement `isLand(x, y)` function in `WorldRepository` using `WorldData` shapes.
  - [x] 3.2 Update `moveShipTowardDestination` in `VoyageRepository` to check collision via `WorldRepository.isLand`.
  - [x] 3.3 Implement `initializeWorld` in `WorldRepository` to seed ports and items from `WorldData`.
- [x] 4.0 Map Visualization
  - [x] 4.1 Update `MapComposables.kt` to render landmasses using `WorldData` shapes.
  - [x] 4.2 Update `MapComposables.kt` to implement Fog of War (masking unexplored areas).
- [ ] 5.0 Verification
  - [ ] 5.1 Launch app and verify map boundaries and continents are visible.
  - [ ] 5.2 Test sailing: ensure ship moves fast (2000 speed) and stops at land boundaries.
