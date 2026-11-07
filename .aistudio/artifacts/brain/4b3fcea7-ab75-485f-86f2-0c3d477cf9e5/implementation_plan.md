# StandBy - Apple-Style Nightstand Experience for Android

An authentic, ultra-polished recreation of the iOS StandBy Mode for Android devices. The app transforms any phone into a smart bedside display when charging in landscape orientation, featuring customizable dual-widget stacks, oversized typography clocks, an ambient fullscreen music player, full edge-to-edge immersive mode, and an automatic red-tint Night Mode powered by ambient light sensing.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following product decisions were confirmed during the interactive interview:
> - **Interactive Audio**: App includes a built-in interactive audio player featuring playable royalty-free ambient/lo-fi tracks, realistic waveform progress, album art, and controls (play/pause, seek, skip).
> - **Night Mode Trigger**: Automatic switching to monochrome crimson red via the hardware Ambient Light Sensor (< 5 lux) with a quick manual toggle for testing and brightness adjustment.
> - **Charging & Orientation Automation**: Automatic activation upon charging in landscape orientation, paired with an on-screen simulation and control panel to easily test without physical hardware.

---

## 1. Overview & Core Concept

- **What It Does**: StandBy turns the Android phone into an ambient information hub and alarm clock dock. Placed horizontally on a charger or stand, the phone dims distracting UI elements and reveals glanceable, full-bleed interactive widgets.
- **Target Persona**: Users looking for an authentic Apple StandBy experience on Android for bedside nightstands, office desks, or wireless charging docks.
- **Key Value**: Fluid Apple-grade spring physics, true AMOLED pure-black power efficiency, glanceable time/weather/calendar/audio, and night vision preservation via red monochromatic sleep illumination.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Dock & Charge Trigger**:
   - Phone connects to power and rotates to landscape -> StandBy launches seamlessly into edge-to-edge mode, hiding status and navigation bars.
   - If opened manually, a quick dock control pill allows instant activation and simulation of charging status.
2. **Horizontal Mode Navigation**:
   - Swiping horizontally (`HorizontalPager`) moves between the three core StandBy viewports:
     - **Viewport 1: Dual Widget Stacks**: Left and right side-by-side independent stacks. Swiping vertically on either half flips between widgets with smooth 3D perspective spring animations.
     - **Viewport 2: Big Clocks**: Fullscreen artistic timepieces (Apple Bold Digital, Minimal Analog with ticking orange second hand, World Time).
     - **Viewport 3: Fullscreen Now Playing**: Immersive blurred vinyl/album art, track titles, animated waveforms, interactive scrub bar, and transport controls.
3. **Nightstand Sleep Mode (Red Shift)**:
   - When bedroom lights go off (ambient light < 5 lux) or night mode is toggled, all UI elements smoothly crossfade into the signature Apple deep crimson red (`#FF3B30` and `#FF453A`) against pitch black (`#000000`).

### Visual Identity & Theme

- **Aesthetic Direction**: Apple Cupertino Human Interface Guidelines for iOS StandBy — ultra-clean rounded typography, generous negative space, vibrant pastel neon accents in daylight, and zero-distraction deep blacks.
- **Color Palette**:
  - `Pure Black`: `#000000` (AMOLED power saving)
  - `Day Accent Neon Mint`: `#30D158` / `#34C759`
  - `Day Accent Electric Blue`: `#0A84FF`
  - `Day Accent Solar Orange`: `#FF9F0A`
  - `Day Accent Coral Red`: `#FF453A`
  - `Night Mode Crimson`: `#FF3B30` / `#990000` (monochrome nightstand glow)
  - `Card Backgrounds`: `rgba(28, 28, 30, 0.75)` with subtle glassmorphism borders
- **Typography**: Heavy, bold rounded numbers for high-distance legibility across the bedroom.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Dual Widget Stack Architecture**:
  - *Chosen Approach*: Left and right vertical pagers (`VerticalPager`) with custom 3D rotation transforms (`graphicsLayer { rotationX = ... }`), allowing users to independently customize what they see on each half.
  - *Why*: Exactly replicates the iOS StandBy dual widget layout (e.g. Clock + Calendar, Battery + Weather, Analog + Music).
- **Decision 2: Audio Engine & System Media Integration**:
  - *Chosen Approach*: Android `MediaPlayer` with high-quality built-in soothing soundscapes and sample tracks, combined with dynamic media state.
  - *Why*: Guarantees instant, zero-setup interactive music playback and real scrubbable audio without requiring external permission prompts or account links.
- **Decision 3: Immersive System Bar & Wake Lock**:
  - *Chosen Approach*: `WindowInsetsControllerCompat` sticky immersive mode + `FLAG_KEEP_SCREEN_ON` when active.
  - *Why*: Eliminates accidental touch interruptions and keeps the nightstand clock permanently visible while docked.

---

## 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────┐
│                       MainActivity                          │
│  - WindowInsetsController (Immersive Fullscreen)            │
│  - Power & Orientation Receivers / Sensor Listener          │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                    StandByViewModel                         │
│  - State: isCharging, isLandscape, isNightMode, luxLevel    │
│  - Active Viewport: DualWidgets | BigClocks | MusicPlayer   │
│  - LeftWidgetIndex / RightWidgetIndex Stack States          │
│  - Audio Player Engine (Playback, Progress, Current Track)   │
└──────────────────────────────┬──────────────────────────────┘
                               │
        ┌──────────────────────┼──────────────────────┐
        ▼                      ▼                      ▼
┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│ Dual Widgets │       │  Big Clocks  │       │ Now Playing  │
│ - Left Stack │       │ - Apple Bold │       │ - Vinyl Art  │
│   (Date,     │       │ - Analog     │       │ - Scrubber   │
│    Analog,   │       │ - Solar      │       │ - Play/Pause │
│    Battery)  │       │ - World      │       │ - Waveform   │
│ - Right Stack│       └──────────────┘       └──────────────┘
│   (Calendar, │
│    Weather,  │
│    Music,    │
│    Reminders)│
└──────────────┘
```

### Component Breakdown
1. **`StandByScreen`**: Root container with horizontal swipe pagination, top ambient controls (charging simulator, night mode toggle, settings sheet).
2. **`DualWidgetMode`**: Split 50/50 landscape view with vertical swipeable widget stacks, Apple-style pagination dots, and interactive cards.
3. **`BigClockMode`**: Fullscreen oversized clock faces with customizable color schemes (Apple Green, Electric Blue, Sunset Orange, White).
4. **`MusicPlayerMode`**: Fullscreen atmospheric now-playing player with dynamic gradient blur background and responsive playback controls.
5. **`NightModeOverlay`**: Animated color filter transforming all surfaces into monochromatic red nightstand mode when dark.
6. **`PowerAndSensorManager`**: BroadcastReceiver for battery charging state, orientation listener, and ambient light sensor listener.
