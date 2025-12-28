# Task List: Fix Status Bar Color

## Relevant Files

- `app/src/main/res/values/themes.xml` - Define global status bar color.
- `app/src/main/res/values-night/themes.xml` - Define global status bar color for dark mode.
- `app/src/main/java/com/captain/voyage/ui/home/HomeScreen.kt` - Remove conflicting SideEffect code.
- `app/src/main/java/com/captain/voyage/MainActivity.kt` - Verify system UI setup.

### Notes

- The goal is a **solid dark color** (e.g., `#3E2723` or `@color/voyage_wood_medium`) for the status bar.
- Icons must be **White** (`windowLightStatusBar = false`).
- We must ensure that `WindowCompat.setDecorFitsSystemWindows(window, false)` doesn't inadvertently make the status bar transparent if we want a solid color, OR we must handle it by ensuring the `window.statusBarColor` is set to the solid color (which works even in edge-to-edge if the flag is set correctly).

## Instructions for Completing Tasks

**IMPORTANT:** As you complete each task, you must check it off in this markdown file by changing `- [ ]` to `- [x]`. This helps track progress and ensures you don't skip any steps.

## Tasks

- [x] 0.0 Create feature branch
  - [x] 0.1 Create and checkout a new branch for this feature (e.g., `git checkout -b fix/status-bar-color`)

- [x] 1.0 Clean up Programmatic Overrides
  - [x] 1.1 In `HomeScreen.kt`, remove the `SideEffect` block that manually sets `window.statusBarColor` and `WindowCompat` flags. This is likely causing conflicts or inconsistent behavior.
  - [x] 1.2 Check `GameActivity.kt` and `MainActivity.kt` for similar manual overrides and standardize them (or remove them in favor of XML/Theme control if possible). (Checked: No conflicting overrides found).

- [x] 2.0 Configure Themes (XML)
  - [x] 2.1 Update `app/src/main/res/values/themes.xml`:
      - Set `<item name="android:statusBarColor">@color/voyage_wood_medium</item>` (or hardcoded color if resource missing).
      - Set `<item name="android:windowLightStatusBar">false</item>` (White icons).
  - [x] 2.2 Update `app/src/main/res/values-night/themes.xml` with the same settings to ensure consistency.

- [x] 3.0 Verify & Adjust
  - [x] 3.1 Build and Run. Check if the status bar is now the desired dark wood color on all screens.
  - [x] 3.2 If still transparent/grey due to Edge-to-Edge logic (`setDecorFitsSystemWindows(false)`), ensure `MainActivity.kt` or `GameActivity.kt` explicitly sets `window.statusBarColor` to the solid color *after* the `WindowCompat` call, ensuring it's not overwritten by `Color.Transparent`. (Final Solution: Used `enableEdgeToEdge()` and a root `Box` with `VoyageTextPrimary` background in `MainActivity`).

### Final Solution Note
For Android 15+ compatibility, the status bar is made transparent via `enableEdgeToEdge()`, and a root `Box` with the theme's dark brown color is placed behind it. Content is pushed down using `safeDrawingPadding()`.


