# Nyxai Glow App Design UI

## 1. Product Overview

Nyxai Glow is an Android beauty camera studio focused on a natural live preview. The current experience combines a CameraX preview, ambient-light guidance, beauty presets, camera controls, and a lightweight AI status layer.

The visual direction is calm and premium: soft coral accents, dark translucent camera overlays, rounded controls, and a light bottom navigation surface.

## 2. Design System

| Role | Color | Usage |
|---|---|---|
| Coral | `#FF9A8B` | Selected controls, glow ring, status dot, primary actions |
| Deep coral | `#96463B` | Text and icons on coral surfaces |
| Mist | `#FAF8FF` | Permission screen and bottom navigation |
| Ink | `#171B2B` | Camera overlays and dark controls |
| Muted gray | `#595F65` | Inactive navigation and secondary labels |
| White | `#FFFFFF` | Camera-screen text and icons |

The interface uses rounded pills, circular camera actions, coral glow effects, and compact telemetry labels. Body labels are at least 12sp where practical, with larger sizes for primary actions and headings.

## 3. App States and Screens

### 3.1 Camera Permission Screen

Shown when camera permission has not been granted.

- Mist full-screen background
- Centered Aura logo
- Headline: **Camera access brings the glow to life**
- Supporting text explaining why camera access is required
- Coral **Enable camera** button

Tapping the button opens the Android permission dialog. Granting permission opens the live camera studio. Denying permission leaves the retry action available.

### 3.2 Live Camera Studio

The live camera studio is the currently implemented primary screen. It contains:

1. CameraX preview background
2. Dark vertical gradient for control legibility
3. Top app bar
4. Glow Engine status capsule
5. Compact neural focus reticle
6. Lighting guidance
7. Texture-preservation toggle
8. Beauty preset and zoom controls
9. Capture controls
10. Four-item bottom navigation

## 4. Live Camera Studio UI

### 4.1 Camera Preview

The CameraX preview fills the display behind the interface.

- Front-facing camera is selected by default.
- `AmbientLightAnalyzer` samples luminance at most four times per second rather than processing every frame.
- The analyzer updates the semantic lighting message in the focus reticle.
- Flipping cameras rebinds the preview and analyzer to the opposite lens.

The preview has a dark vertical gradient, darker near the top and bottom, so white controls remain readable without obscuring the face.

### 4.2 Top App Bar

The top bar contains:

- Aura concentric-ring logo
- **Nyxai Glow** brand name
- **Camera** screen label
- Flash button
- Settings button

The flash button changes from white to coral when its local state is active. Camera hardware flash control is not connected yet. The settings button is reserved for camera and processing preferences.

### 4.3 Glow Engine Status Capsule

A translucent dark pill appears near the top center of the camera area.

- Coral status dot
- **Glow Engine** label
- **Ready** state

This is user-facing status language. Developer version information is intentionally kept out of the main camera experience and should belong in a future Settings > About screen.

### 4.4 Neural Focus Reticle

The reticle is intentionally compact at approximately 150dp so it does not cover the user's face.

Its layers are:

1. Coral radial aura that fades to transparent at the edges
2. Coral progress arc
3. Solid focus ring
4. Dashed calibration ring
5. Aura logo
6. **NEURAL FOCUS** label
7. Semantic lighting guidance

The reticle appears after camera-related interaction and automatically hides after two seconds of inactivity. It is a state indicator rather than a permanent obstruction over the live preview.

The progress arc represents the selected look intensity:

| Preset | Glow intensity |
|---|---:|
| Soft | 68% |
| Radiant | 86% |
| Velvet | 34% |
| Defined | 57% |

### 4.5 Semantic Lighting Guidance

The UI does not expose a raw percentage because that is not actionable for most users. It displays a short lighting interpretation instead:

| Luminance | Display |
|---|---|
| Below 20% | `Low light - glow boosted` |
| 20-60% | `Balanced light` |
| 60-85% | `Bright - highlights softened` |
| Above 85% | `Harsh light - smoothing adjusted` |

The guidance is derived from the camera image analyzer and changes as lighting changes.

### 4.6 Preserve Natural Texture Toggle

A translucent capsule below the reticle contains:

- Sparkle icon
- **Preserve natural texture** label
- **On** or **Off** state

Tapping the capsule toggles the state. This communicates the intended identity-preserving behavior more clearly than a percentage.

Current limitation: the toggle is UI state only. A real TFLite texture-preservation model is not connected yet, and no before/after thumbnail is currently shown.

### 4.7 Beauty Preset Selector

The horizontal preset row contains four on-brand pills:

- **Soft**
- **Radiant**
- **Velvet**
- **Defined**

The selected preset uses a coral background, deep coral text, a sparkle icon, and semi-bold type. Unselected presets use dark translucent backgrounds and white text.

Selecting a preset updates the selected appearance and glow arc value.

### 4.8 Zoom Selector

The zoom row contains:

- `0.5x`
- `1x`
- `2x`
- `3x`

The selected value uses a coral background and deep coral bold text. The `3x` label does not imply a paid or Pro-only feature.

Current limitation: the selected value is not yet connected to `CameraControl.setZoomRatio`, so the preview does not actually zoom.

### 4.9 Capture Control Row

The capture row contains three controls:

#### Gallery

Photo library icon reserved for selecting existing photos. The gallery screen and picker are not connected yet.

#### Shutter

Large circular coral-and-white camera action intended to capture a photo. Real image capture and storage are not connected yet.

#### Flip camera

Switches between front and rear camera lenses. This is currently functional and rebinds CameraX.

### 4.10 Bottom Navigation

The bottom navigation is a light Mist panel with rounded top corners and four destinations:

| Item | Icon | Intended destination |
|---|---|---|
| Gallery | Photo library | Saved and imported photos |
| Looks | Sparkles | Beauty looks and presets |
| Camera | Camera | Live camera studio |
| Profile | Person | Account and settings |

Camera is the elevated coral center action. Preset management belongs inside Looks instead of taking a separate bottom-navigation slot.

Only the Camera screen is currently implemented. The active label changes in Compose state, but the other destinations are placeholders.

## 5. Interaction Reference

| Component | Current action | Status |
|---|---|---|
| Enable camera | Requests Android camera permission | Implemented |
| Flash | Toggles visual flash state | UI only |
| Settings | Reserved for future settings | Placeholder |
| Preset pills | Changes look and glow amount | Implemented in UI state |
| Zoom pills | Changes selected zoom label | UI only |
| Preserve texture | Toggles On/Off state | Implemented in UI state |
| Gallery | Reserved for picker/gallery | Placeholder |
| Shutter | Reserved for image capture | Placeholder |
| Flip camera | Switches front/rear lens | Implemented |
| Bottom navigation | Changes active label | UI state only |
| Ambient analyzer | Produces semantic lighting guidance | Implemented |
| Glow Engine | Displays ready status | UI representation only |

## 6. Accessibility

- Interactive preset and zoom controls expose selected/not-selected state semantics.
- The texture control exposes switch semantics and its On/Off state.
- Icon buttons include content descriptions such as `Toggle flash`, `Open gallery`, `Capture photo`, and `Flip camera`.
- Body control labels use 12sp or larger in the optimized camera UI.
- High-priority actions use larger touch targets than secondary controls.
- Color is paired with text or shape changes so state is not communicated by color alone.
- Future screens should support large system font settings, TalkBack traversal, and explicit selected semantics for bottom navigation.

## 7. Technical UI Architecture

`NyxaiGlowApp` owns the camera permission flow and chooses between `PermissionPrompt` and `GlowStudio`.

`GlowStudio` currently owns:

- Glow intensity
- Ambient-light value
- Selected preset
- Selected zoom label
- Camera facing direction
- Flash state
- Texture-preservation state
- Active navigation label
- Reticle visibility timeout

Main composables:

- `CameraPreview`
- `TopBar`
- `CameraOverlay`
- `GlowReticle`
- `CameraDeck`
- `BottomNavigation`
- `NavItem`
- `AuraLogo`
- `PermissionPrompt`

CameraX provides the preview and image analysis. `AmbientLightAnalyzer` samples the Y plane, throttles work to a 250ms interval, and emits a normalized luminance value to Compose state.

## 8. Planned Screens and Empty States

The following destinations are planned but not implemented as separate screens yet. Each should have a designed first-use empty state rather than a blank view.

### Gallery

Photo grid for captured and imported media. Empty state CTA: **Take your first glow photo**.

### Looks

Beauty looks and saved presets. Empty state CTA: **Browse looks**. Presets should be managed as a sub-area of Looks.

### Profile

Account, subscription, privacy, camera preferences, help, and About information. Developer version information belongs here.

### Retouch Editor

Post-capture editing with Adaptive Glow, Preserve natural texture, intensity controls, before/after comparison, undo, redo, and export.

## 9. Missing Runtime States

These states should be explicitly designed before the full product is complete:

| State | Location | Intended treatment |
|---|---|---|
| Loading | First launch and AI initialization | Skeleton or subtle shimmer around the reticle |
| Error | Camera unavailable or permission denied | Full-screen explanation with retry CTA |
| Offline | Future network features | Subtle non-blocking banner |
| Processing | After shutter tap | Coral progress ring around shutter |
| Low storage | Gallery/capture flow | Warning message and disabled capture |
| Low battery | Device below 15% | Reduce expensive processing and show warning |
| Warm/hot device | Thermal monitoring | Reduce effects or resolution with a brief explanation |

## 10. Known Limitations

- Real photo capture and file storage are not connected.
- Gallery picker and gallery destination are not connected.
- Flash and zoom are visual state only.
- Bottom-navigation destinations are placeholders.
- MediaPipe face landmarks are not integrated.
- TFLite texture preservation is not integrated.
- OpenGL Adaptive Glow processing is not integrated.
- Presets are not persisted.
- Settings, export, sharing, account, and subscription flows are not implemented.
- ViewModel separation and thermal monitoring remain future architecture work.

## 11. Roadmap

### Phase 1: UX and performance foundation

- Compact, auto-hiding reticle
- Semantic lighting guidance
- Texture-preservation toggle
- Throttled ambient analysis
- Accessible control semantics
- Four-item navigation and empty-state designs

### Phase 2: Core camera workflow

- Real CameraX capture and storage
- Hardware flash and zoom controls
- Gallery grid and picker
- Haptic feedback
- Scoped camera, effects, and navigation ViewModels

### Phase 3: AI and premium features

- MediaPipe face landmarks
- TFLite texture preservation
- OpenGL Adaptive Glow shader
- Retouch editor with before/after comparison
- Subscription and premium looks

### Phase 4: Device polish

- Thermal and battery guards
- Full accessibility audit
- Onboarding
- Analytics and crash reporting
