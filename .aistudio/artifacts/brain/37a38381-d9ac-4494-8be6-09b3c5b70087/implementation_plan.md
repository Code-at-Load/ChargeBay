# Implementation Plan: Music Slider Conflict, Vertical Volume Control, Clean Single Clock & Live Frosted Gradient Clock

The user requested four key enhancements and bug fixes:
1. **Fix Music Scrubber Conflict with Clock Navigation**: Resolve the issue where sliding the progress scrubber in "Now Playing" triggers `HorizontalPager` to swipe back to the clock view.
2. **Interactive Vertical Volume Slider**: Implement a sleek vertical minimalist volume capsule on the right side of the Now Playing card matching the user's screenshot, allowing vertical slide up/down to adjust music volume.
3. **Hide Sliding Dots in Single Clock Mode**: Remove the vertical pagination dots and navigation chevrons from `BigClocksScreen` for a pure, unobstructed clock face display.
4. **Live Frosted Gradient Clock**: Greatly enlarge the digital typography in `GradientBatteryClock`, animate a smooth 15-second ambient color shift, and apply frosted glass gradient styling to the digits that dynamically adapts to the background hues.

---

## User Review Required

> [!IMPORTANT]
> - **Volume Feedback Style**: Following user selection, the volume slider on the right will be a sleek minimalist capsule with a filled level track (no intrusive text or numbers).
> - **Gradient Animation**: Following user selection, the live gradient in the Gradient Battery Clock will smoothly transition over a calm, ambient 15-second cycle.

---

## Proposed Changes

### 1. Fix Slider Drag Conflict in Music Player (`MusicPlayerScreen.kt` & `StandByRootScreen.kt`)
- In `StandByRootScreen.kt`, introduce an `isScrubbing` state passed to `MusicPlayerScreen` (or track user dragging via `Slider` interaction source).
- Set `HorizontalPager(userScrollEnabled = !isScrubbing)` so that when the user touches and drags the progress slider, `HorizontalPager` cannot intercept the gesture or switch back to the clocks.
- Add pointer consumption on the slider track so all horizontal drag gestures remain strictly bound to seek operations.

### 2. Vertical Volume Control Slider (`MiniMusicWidget.kt` & `DualWidgetsScreen.kt`)
- In `MiniMusicWidget.kt` / `DualWidgetsScreen.kt`, add an interactive vertical volume capsule on the right side matching the exact position indicated in the screenshot:
  - Width: ~14dp, Height: ~140dp, fully rounded capsule shape.
  - Background: Translucent dark glass (`Color(0xFF2C2C2E)` or dark backdrop).
  - Fill indicator: Dynamic filled bar rising from bottom representing `state.volume` (0f..1f).
  - Gestures: `detectVerticalDragGestures` allowing smooth sliding up (increase volume) and down (decrease volume).
  - Real hardware volume sync: Wire to `StandByViewModel.setVolume(vol)` which invokes `AudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, ...)`.

### 3. Hide Sliding Dots in Single Clock Mode (`BigClocksScreen.kt`)
- In `BigClocksScreen.kt`, remove or hide the `standby_vertical_clock_switcher` Column (the up/down chevron buttons and vertical dot matrix pills on the right edge).
- Clock switching remains seamlessly controllable via vertical swipe gestures (swiping up or down anywhere on the screen) as previously implemented, keeping the screen clean and distraction-free.

### 4. Enlarge Clock & Live Ambient Frosted Digits in `GradientBatteryClock.kt`
- Wrap `GradientBatteryClock` in `BoxWithConstraints` and enlarge `fontSize` to `minOf(h.value * 0.88f, w.value * 0.38f).sp` so digits fill the central landscape area generously.
- Implement an infinite ambient transition (`rememberInfiniteTransition`) with a 15-second duration:
  - Slowly rotates the gradient angle and shifts color stops across rich lavender, twilight indigo, and rose mauve.
- Style the time text with a frosted glass appearance:
  - Translucent multi-stop vertical gradient overlay (`Color.White.copy(alpha = 0.92f)` to soft lavender/blush `Color(0xFFFFE8FD).copy(alpha = 0.65f)`) with subtle glow shadow.
  - The text responds and adapts naturally to the shifting ambient light beneath it.

---

## Verification Plan

### Automated Verification
- Run `compile_applet` to ensure syntax, imports, and Compose build succeed without errors.
- Run `gradle :app:testDebugUnitTest` to verify all unit tests pass.

### Manual / Feature Verification
- Verify that scrubbing the music slider in Now Playing does not trigger horizontal page swiping.
- Verify sliding up/down on the vertical volume bar changes `volume` and sets device media volume.
- Verify that single clock faces in `BigClocksScreen` no longer display the right-edge dots.
- Verify `GradientBatteryClock` displays oversized frosted typography with slowly shifting gradient colors over a 15-second cycle.
