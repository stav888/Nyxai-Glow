# Nyxia Glow UI Screen Specification

## 1. Document Purpose

This document defines the user interface and interaction design for Nyxia Glow. It describes every current screen, overlay, control, state, transition, visual rule, accessibility requirement, and responsive behavior.

The document is intentionally grounded in the current implementation. It separates:

- **Implemented behavior**: behavior already represented in the Android Compose and CameraX code.
- **UI-only behavior**: controls that update application state or feedback but do not yet process image pixels independently.
- **Future design direction**: improvements that can be implemented without changing the product identity or the current navigation model.

The product is a native Android beauty-camera studio. The primary experience is a live camera view with restrained professional controls, fast capture, face-aware makeup overlays, and a focused retouch surface.

---

## 2. Product Experience Principles

### 2.1 Primary user goal

The user should be able to open the app, grant camera access, see their face clearly, adjust a look, and capture a photo with minimal interruption.

### 2.2 Design priorities

1. **The face is the primary content.** Controls should frame the preview, never compete with it.
2. **Capture is always close.** The shutter remains visually dominant and easy to reach.
3. **State is legible at a glance.** Selected presets, zoom, flash, texture preservation, and errors must be obvious without opening another screen.
4. **Beauty effects should feel controlled.** The UI should communicate subtle enhancement rather than aggressive transformation.
5. **Hardware limitations are honest.** Unsupported flash, zoom, camera, storage, and model states must be explained clearly.
6. **The interface should remain calm under live processing.** Avoid noisy animation, repeated alerts, or frame-by-frame status changes.
7. **Touch targets must be reliable.** Camera controls are used repeatedly and often one-handed.

### 2.3 Current product boundaries

The current app includes:

- Live CameraX preview.
- Front and rear camera switching.
- Face Landmarker analysis.
- Face-aware lip and cheek makeup mask rendering in the GL preview.
- Ambient-light guidance.
- Zoom and hardware-aware torch control.
- Camera presets and retouch state.
- Android image picker access.
- MediaStore photo capture.
- Retouch controls and status feedback.

The following are not separate, fully implemented product surfaces yet:

- A native gallery grid.
- A user profile screen.
- A dedicated image editor.
- Saving a processed preview with the makeup mask baked into the captured image.
- A separate skin mask or texture-preservation model.
- A ViewModel-backed navigation architecture.

---

## 3. Information Architecture

```text
App launch
  |
  +-- Camera permission missing
  |     `-- Camera Permission Screen
  |
  `-- Camera permission granted
        `-- Camera Studio
              |
              +-- Looks -> Retouch Screen
              +-- Gallery -> Android system image picker
              +-- Profile -> Camera Settings dialog
              +-- Camera -> Camera Studio
```

### 3.1 Navigation model

The current application uses local Compose state rather than Jetpack Navigation.

The main state values are:

| State | Purpose | Current owner |
|---|---|---|
| `cameraGranted` | Whether camera access is available | `NyxiaGlowApp` |
| `activeTab` | Current Camera or Retouch surface | `GlowStudio` |
| `facing` | Front or rear camera | `GlowStudio` |
| `zoom` | Selected zoom label | `GlowStudio` |
| `flashOn` | Requested torch state | `GlowStudio` |
| `flashAvailable` | Hardware flash availability | `GlowStudio` |
| `preset` | Camera look selection | `GlowStudio` |
| `glow` | Reticle and renderer glow strength | `GlowStudio` |
| `ambient` | Normalized ambient luminance | `GlowStudio` |
| `retouchState` | Retouch tool, preset, smoothing, and texture state | `GlowStudio` |
| `capturedUri` | Latest selected or captured image | `GlowStudio` |
| `statusMessage` | Temporary user feedback | `GlowStudio` |
| `showSettings` | Settings dialog visibility | `GlowStudio` |

### 3.2 Navigation rules

- Camera is the default destination after permission is granted.
- Looks opens the Retouch surface.
- Camera returns to the live camera surface.
- Gallery opens the Android system picker; it does not open an in-app gallery screen.
- Profile opens the current Camera Settings dialog; it does not open a profile page.
- Returning from the picker preserves the current camera state when possible.
- Changing lenses clears the current makeup mask before the new camera pipeline is ready.

---

## 4. Visual Design System

### 4.1 Color tokens

| Token | Hex | Usage |
|---|---|---|
| `Coral` | `#FF9A8B` | Primary action, selected state, focus ring, active accent |
| `CoralDeep` | `#96463B` | Text and icon color on Coral controls |
| `Ink` | `#111111` | Camera background, dark overlay, high-contrast surfaces |
| `SurfaceDark` | `#171515` | Navigation and Retouch background |
| `SurfaceRaised` | `#242020` | Secondary dark surfaces and cards |
| `Mist` | `#F5F1F0` | Permission and settings surfaces |
| `MutedText` | `#595F65` | Supporting text on light surfaces |
| `White` | `#FFFFFF` | Primary text over dark camera surfaces |
| `SuccessGreen` | `#7FD6A3` | Reserved for future success confirmation |
| `ErrorRed` | `#D96868` | Error text, error icon, destructive feedback |

### 4.2 Color usage rules

- Coral is reserved for action, selection, and active state. It should not fill the entire screen.
- CoralDeep is used where Coral would otherwise create insufficient contrast.
- Dark translucent surfaces must preserve enough contrast over the live camera image.
- Error states should not be communicated by color alone; include text and an icon or clear label.
- The makeup mask uses red for lips and green for cheeks internally. These are rendering channels, not user-facing brand colors.

### 4.3 Shape language

- Primary action buttons: circular or compact rounded shapes.
- Secondary controls: rounded rectangles with a maximum radius of 12dp unless they are pills.
- Status capsules: pill shape, used only for short labels.
- Dialogs: 24dp corner radius.
- Cards and preview frames: 12-16dp corner radius.
- Avoid nested cards. Use spacing and surface contrast to create hierarchy.

### 4.4 Typography

The current UI uses Compose Material typography with compact labels and bold display text.

Recommended hierarchy:

| Level | Recommended size | Weight | Usage |
|---|---:|---|---|
| Screen title | 20-24sp | SemiBold | Dialog or non-camera screen title |
| Camera brand label | 11-12sp | Bold | `NYXIA-GLOW` |
| Section label | 10-11sp | Medium | `PRESETS & TONE`, status captions |
| Primary control label | 12-14sp | Medium | Preset, texture, settings labels |
| Supporting text | 12-14sp | Normal | Explanations and permission copy |
| Status text | 11-13sp | Medium | Short feedback and live states |

Text should never depend on letter spacing to fit. Labels must remain readable with larger system font settings.

### 4.5 Spacing scale

Use an 8dp base rhythm:

- 4dp: icon-to-label micro spacing.
- 8dp: compact control padding.
- 12dp: standard control inset.
- 16dp: screen side padding.
- 20dp: section separation.
- 24dp: major group separation.
- 32dp: permission and empty-state breathing room.

### 4.6 Icon rules

- Use Material icons already present in the project.
- Every icon-only button must have a content description.
- Icons should be 21-30dp depending on control size.
- Do not use an icon as the only indication of a selected or disabled state.
- Keep icon placement stable when state changes so controls do not shift.

---

## 5. Global Interaction Rules

### 5.1 Touch targets

- Icon buttons: minimum 48dp touch target.
- Shutter: 70dp visible button inside an 84dp action halo.
- Bottom navigation items: at least 48dp high.
- Slider: preserve the Material touch target even when the visual track is compact.
- The full texture-preservation row is clickable, not only its text.

### 5.2 Feedback

- Short feedback appears in the top status capsule.
- Do not show a status message for every analyzed frame.
- Face detection feedback should be state-based and should not flicker between detected/not detected on every frame.
- Capture feedback should remain visible long enough to be read.
- Errors should explain the next user action where possible.

### 5.3 Motion

Recommended motion is restrained:

- Fade or scale the neural-focus reticle in when a camera parameter changes.
- Fade the reticle out after approximately two seconds.
- Animate selected control color or border changes over 150-200ms.
- Use no continuous decorative animation over the user's face.
- Do not animate the camera preview itself.
- Keep dialog entrance and exit consistent with platform motion.

### 5.4 Loading behavior

- Camera initialization should show a quiet processing state rather than an empty black screen when possible.
- Face model initialization errors should appear as a concise status message.
- The shutter should not accept overlapping capture requests.
- A future implementation may show a small processing indicator around the shutter, but it must not obscure the preview.

---

## 6. Screen: Camera Permission

### 6.1 Internal component

`PermissionPrompt`

### 6.2 Purpose

Explain why camera access is required and provide one clear action to continue.

### 6.3 Layout

```text
Full-screen Mist background
  |
  +-- centered content column
        +-- Aura logo
        +-- title
        +-- supporting explanation
        `-- Enable camera button
```

### 6.4 Components

#### Aura logo

- Centered above the copy.
- Uses the Coral ring treatment and centered glow mark.
- Decorative and non-interactive.
- Recommended visible size: 72dp.

#### Title

Current copy:

`Camera access brings the glow to life`

Design rules:

- 21-24sp.
- SemiBold.
- Ink color.
- Center aligned.
- Wrap naturally on narrow screens.

#### Supporting copy

Current copy:

`Nyxia Glow needs your camera for the live beauty preview.`

Design rules:

- 14sp maximum.
- MutedText color.
- Center aligned.
- Maximum readable width around 300dp.

#### Enable camera button

- Primary Coral filled button.
- CoralDeep text.
- Minimum 48dp high.
- Clear label, not an icon-only action.
- Opens the Android camera permission request.

### 6.5 States

| State | UI behavior |
|---|---|
| Permission not requested | Show normal permission prompt |
| Permission request active | Android system permission dialog owns the interaction |
| Permission granted | Replace prompt with Camera Studio |
| Permission denied | Keep prompt visible |
| Camera unavailable after permission | Show Camera Studio error feedback; do not imply permission is the problem |

### 6.6 Accessibility

- Button must be reachable by TalkBack.
- Title and supporting copy should be read in order.
- Do not rely on the Coral color to explain the primary action.
- Keep sufficient contrast between Mist background, Ink title, and MutedText copy.

---

## 7. Screen: Camera Studio

### 7.1 Internal components

`GlowStudio`, `CameraPreview`, `TopBar`, `CameraOverlay`, `CameraDeck`, `BottomNavigation`.

### 7.2 Purpose

Provide the primary live beauty-camera workflow: preview the face, adjust the look, and capture quickly.

### 7.3 Layer order

The visual stack should be rendered in this order:

1. CameraX and OpenGL preview.
2. Makeup mask and beauty shader output.
3. Dark vertical gradient for control legibility.
4. Top bar.
5. Camera overlay and focus reticle.
6. Camera deck controls.
7. Bottom navigation.
8. Status capsule.
9. Settings dialog, if open.

### 7.4 Preview behavior

- The preview fills the available screen.
- The camera image remains the primary visual surface.
- The preview must not be placed inside a decorative card.
- The GL renderer samples the camera texture and makeup mask texture together.
- The makeup mask follows the actual preview texture dimensions rather than assuming a fixed display size.
- Rotation support includes 0, 90, 180, and 270 degrees.
- Front-camera mirroring is applied consistently to face coordinates and preview presentation.
- When no face is detected, the previous mask is cleared.
- When the camera effect is disposed or the lens changes, the mask is cleared immediately.

### 7.5 Camera pipeline summary

```text
CameraX Preview
     |
     +-- ImageCapture -> MediaStore JPEG
     |
     `-- ImageAnalysis
           +-- luminance sampling -> lighting label
           `-- Face Landmarker -> coordinates -> makeup mask -> GL texture
```

### 7.6 Camera initialization states

#### Initializing

Recommended UI:

- Keep the surface dark.
- Show a small centered processing indicator or `Preparing camera` status.
- Keep the rest of the UI stable where possible.

#### Ready

- Show live preview.
- Show top controls.
- Show the default Soft preset.
- Show default `1x` zoom.
- Show texture preservation as On.
- Show the shutter enabled when capture is available.

#### Camera error

Current feedback uses a status message such as:

`Camera unavailable: ...`

Improved design direction:

- Add a compact warning icon.
- Explain whether the issue is camera binding, permission, hardware, or model initialization.
- Offer a retry action in a future dedicated error state.

---

## 8. Camera Studio: Top Bar

### 8.1 Layout

```text
[ Aura logo  NYXIA-GLOW ]       [ Camera ] [ Flash ] [ Settings ]
```

- Horizontal padding: approximately 18dp.
- Vertical padding: approximately 14dp.
- Align all controls to a common center line.
- Keep the title and icons from overlapping on small screens.

### 8.2 Brand group

#### Aura logo

- 34dp recommended size.
- Left aligned.
- Non-interactive.

#### NYXIA-GLOW

- 11-12sp.
- Bold.
- White with slight transparency if needed over a bright preview.
- Must remain visible without dominating the face.

### 8.3 Screen title

Current values:

- `Camera` on Camera Studio.
- `Retouch Looks` when used with the Retouch context.

The title is a context label, not a navigation button.

### 8.4 Flash control

Behavior:

- Toggles the CameraX torch when a flash unit exists.
- Disabled when hardware reports no flash.
- Resets to Off when switching to a camera without flash.

Visual states:

| State | Icon tint | Interaction |
|---|---|---|
| Available, Off | White with medium alpha | Enabled |
| Available, On | Coral | Enabled |
| Unsupported | White with low alpha | Disabled |

Accessibility:

- Content description should communicate current state where possible, for example `Turn flash on` or `Turn flash off`.
- Disabled state must be exposed to TalkBack.

### 8.5 Settings control

- Always available while the camera surface is shown.
- Opens the Camera Settings dialog.
- Settings icon uses a stable 48dp touch target.

---

## 9. Camera Studio: Camera Overlay

### 9.1 Internal component

`CameraOverlay`

### 9.2 Vertical structure

The overlay occupies the central upper-middle area:

```text
AI ACTIVE / 4K RAW capsule

        Neural Focus reticle

Preserve natural texture row
```

### 9.3 AI status capsule

Current labels:

- `AI ACTIVE`
- `4K RAW`

Design intent:

- Communicate that the camera pipeline is active.
- Keep the capsule compact and non-interactive.
- Use a small Coral status dot.

Important product truth:

`4K RAW` is currently a visual label. It must not be presented as a technical guarantee unless the camera capture configuration actually provides 4K RAW output.

### 9.4 Neural Focus reticle

Visual parts:

- Soft radial Coral glow.
- Coral progress arc based on glow strength.
- Outer circular stroke.
- Dashed inner stroke.
- Small Aura logo.
- `NEURAL FOCUS` label.
- Dynamic lighting status.

Behavior:

- Appears after changes to preset, zoom, flash, or camera facing.
- Remains visible for approximately two seconds.
- Fades rather than disappearing abruptly.
- Must not cover important facial landmarks at an opacity that harms preview visibility.

### 9.5 Ambient-light status

The analyzer samples the Y plane and normalizes the result to 0.0-1.0.

| Ambient value | Label | User meaning |
|---:|---|---|
| `< 0.20` | `Low light - glow boosted` | The environment is dark; the app adapts its guidance |
| `0.20-0.59` | `Balanced light` | Lighting is suitable for the current workflow |
| `0.60-0.84` | `Bright - highlights softened` | Bright areas may need softer treatment |
| `>= 0.85` | `Harsh light - smoothing adjusted` | Strong light may affect highlights and smoothing |

Design rules:

- Lighting text should update only when the semantic label changes.
- Do not flash a notification for every numeric luminance update.
- The label should remain short enough for one or two lines on narrow screens.

### 9.6 Preserve natural texture control

Current content:

- Icon: AutoAwesome.
- Label: `Preserve natural texture`.
- Value: `On` or `Off`.

Behavior:

- The entire row is clickable.
- State is shared with Retouch.
- State affects smoothing strength sent to the renderer.
- It does not yet use a dedicated texture mask or TFLite model.

Visual states:

| State | Value | Recommended treatment |
|---|---|---|
| On | `On` | Coral value and active icon |
| Off | `Off` | Muted value and neutral icon |
| Disabled/future unavailable | `Unavailable` | Muted row and explanation |

Accessibility:

- Expose Switch role.
- Expose `On`/`Off` state description.
- Ensure the row remains usable with keyboard or accessibility focus.

---

## 10. Camera Studio: Camera Deck

### 10.1 Layout

```text
Preset chips

Zoom selector

[ Gallery ]       [ Shutter ]       [ Flip camera ]
```

The deck sits above the bottom navigation with enough bottom spacing to avoid collision with system navigation bars.

### 10.2 Preset chips

Current presets:

- `Soft`
- `Radiant`
- `Velvet`
- `Defined`

Current glow mapping:

| Preset | Glow strength |
|---|---:|
| Soft | 0.68 |
| Radiant | 0.86 |
| Velvet | 0.34 |
| Defined | 0.57 |

Interaction:

- Tap selects a preset.
- Selection updates `preset` and glow strength.
- Selected chip uses Coral fill.
- Selected chip may show AutoAwesome icon.
- Presets are horizontally scrollable so labels remain stable on narrow devices.

Recommended future behavior:

- Keep the preset name visible.
- Add a subtle preview change rather than a large transition.
- If a preset is UI-only, do not imply that it changes pixels beyond the actual renderer behavior.

Accessibility:

- Expose Button role.
- Expose selected/not-selected state.
- Maintain a visible selected state through color and text weight.

### 10.3 Zoom selector

Current choices:

- `0.5x`
- `1x`
- `2x`
- `3x`

Behavior:

1. Store the selected label.
2. Read the active camera's supported zoom range.
3. Clamp the requested value to that range.
4. Pass the result to CameraX.

Visual states:

- Selected zoom: Coral background, CoralDeep text, bold weight.
- Unselected zoom: translucent dark background, white text.
- Unsupported hardware range: retain the control but clamp safely; future UI may mark unavailable options.

### 10.4 Gallery action

- Photo Library icon.
- Opens Android `GetContent` for `image/*`.
- Does not display an internal gallery grid.
- Selected URI is stored as `capturedUri`.
- Status becomes `Photo selected` when a URI is returned.
- Cancelling the picker leaves the current URI and status unchanged.

### 10.5 Capture action

Visual design:

- 84dp Coral-tinted halo.
- 70dp white circular button.
- 30dp CoralDeep camera icon.
- Strongest visual emphasis in the camera deck.

Behavior:

- Uses CameraX `ImageCapture`.
- Uses an atomic capture lock to prevent double-tap overlap.
- Writes a JPEG to MediaStore.
- Stores the output URI after success.
- Reports `Photo captured` after success.
- Deletes a pending URI after failure.

Storage behavior:

#### Android 10+

- Uses scoped MediaStore storage.
- Saves under `Pictures/Nyxia Glow`.
- Uses `IS_PENDING` while writing.

#### Android 8 and 9

- Requests legacy write permission only when the user attempts to capture.
- If denied, show `Storage permission is required to save photos`.

Capture states:

| State | UI behavior |
|---|---|
| Ready | Shutter enabled |
| Capture in progress | Ignore additional capture taps; future UI may show progress |
| Success | Show `Photo captured`; release capture lock |
| Storage permission required | Launch permission request |
| Permission denied | Show storage error; release capture lock |
| MediaStore failure | Show storage preparation error |
| Capture error | Delete pending URI and show `Capture failed` |

### 10.6 Flip camera action

- Uses FlipCameraAndroid icon.
- Swaps front and rear camera selectors.
- Unbinds and rebinds the CameraX lifecycle pipeline.
- Clears the existing makeup mask during teardown.
- Applies front-camera mirroring consistently to the face mask.
- Rechecks flash availability.

During switching:

- Avoid showing stale face makeup from the previous lens.
- Keep the control position stable.
- Preserve independent UI state unless hardware requires a reset.

---

## 11. Bottom Navigation

### 11.1 Layout

```text
[ Gallery ] [ Looks ] [ Camera ] [ Profile ]
```

- Full-width dark surface.
- Rounded top corners.
- Navigation-bar padding.
- Camera action is elevated and visually distinct.
- Labels remain visible under icons.

### 11.2 Gallery item

Current behavior:

- Opens the Android system image picker.
- Does not change to a native Gallery destination.
- Stores the selected URI and shows status feedback.

Recommended semantic label:

`Open gallery`

### 11.3 Looks item

Current behavior:

- Sets `activeTab` to Retouch.
- Shows the Retouch Screen.

Visual state:

- Coral icon and label when active.
- Muted white icon and label when inactive.

### 11.4 Camera item

Current behavior:

- Returns to Camera Studio.
- Uses a larger Coral circular action.
- Icon is CoralDeep.

### 11.5 Profile item

Current behavior:

- Opens Camera Settings dialog.
- Does not represent a user account or profile.

Recommended future direction:

- Rename or replace this item if a real Profile destination is not planned.
- If retained, label it `Settings` to match its actual behavior.

### 11.6 Accessibility

- Every item has an icon content description and visible label.
- The active destination must expose selected state.
- Camera's elevated treatment must not be the only indication of its function.
- Bottom navigation should remain reachable above Android system navigation insets.

---

## 12. Screen: Retouch

### 12.1 Internal component

`RetouchScreen`

### 12.2 Entry point

Tap `Looks` in Bottom Navigation.

### 12.3 Purpose

Provide a focused control surface for retouch categories, tone presets, smoothing intensity, texture preservation, reset, and apply feedback.

### 12.4 Layout structure

```text
Top padding / context header
  |
  +-- AI CORE status and HOLD BEFORE label
  +-- Retouch preview frame
  +-- Tool category row
  +-- PRESETS & TONE section
  +-- Smoothing intensity slider
  +-- Subtle Micro-Texture toggle
  `-- RESET / APPLY TO PREVIEW actions
```

### 12.5 Screen background

- Use `SurfaceDark` as the base.
- Keep the screen visually related to Camera Studio.
- Use `SurfaceRaised` for controls and secondary grouping.
- Do not use excessive cards around every individual option.

### 12.6 Header

#### AI CORE V2.4 ACTIVE

- Small uppercase status label.
- White with reduced opacity.
- Non-interactive.
- Communicates processing context but should not claim an unavailable model capability.

#### HOLD BEFORE

- Compact raised capsule.
- Static visual treatment in the current implementation.
- Non-interactive.
- If the label does not represent a real interaction, consider replacing it with a meaningful state label in future iterations.

### 12.7 Retouch preview frame

Current content:

- Dark preview surface.
- Rounded corners.
- Circular retouch glow reticle.
- `98.4% NATURAL MATCH` label.

Current limitation:

- This is a visual retouch illustration rather than a live image or selected gallery image.
- It should not be described as a measured accuracy score unless the value is calculated.

Recommended future behavior:

- Display the actual camera or selected source image.
- Keep the reticle as a subtle overlay rather than the dominant content.
- Replace static accuracy text with a truthful processing status or remove it.

### 12.8 Retouch tool categories

Current tools:

| Tool | Icon | State change |
|---|---|---|
| Skin | AutoAwesome | `selectedTool = "Skin"` |
| Shape | PhotoLibrary | `selectedTool = "Shape"` |
| Light | FlashOn | `selectedTool = "Light"` |
| Makeup | Palette | `selectedTool = "Makeup"` |

Visual selected state:

- Coral-tinted circular surface.
- Coral icon.
- Coral label.
- Selected semantics.

Interaction rules:

- Tap target includes icon and label.
- Selection must not resize the row.
- The selected state must be visible without relying only on color.
- Tool selection currently changes state but does not start a separate processing pipeline.

### 12.9 Presets & Tone

Current presets:

- `Smooth`
- `Freckles`
- `Matte`
- `Dewy`
- `Refine`

Each item includes:

- A compact tile.
- A thumbnail color block.
- A label.
- Selected semantics.

Interaction:

- Tap a tile to update `selectedPreset`.
- The selected tile uses Coral as its active surface.
- The row is horizontally scrollable.

Design improvements:

- Use meaningful preview thumbnails when real presets are available.
- Keep the label readable at larger font sizes.
- Avoid presenting a static color block as a real image preview.

### 12.10 Smoothing intensity

- Material Slider.
- Range: 0.0-1.0.
- Default: 0.45.
- State is shared with Camera Studio.
- Camera Studio converts it into renderer smoothing strength.
- When texture preservation is enabled, the camera pipeline reduces smoothing intensity by half.

Accessibility:

- Expose slider role.
- Announce the percentage value.
- Keep the thumb touch target large enough for precise adjustment.
- Do not require a user to drag to an exact pixel position.

### 12.11 Subtle Micro-Texture

Current labels:

- `Subtle Micro-Texture`
- `Preserves natural pores & grain`
- `ON` or `OFF`

Behavior:

- Toggles `preserveTexture`.
- State is mirrored in the Camera Overlay.
- State affects smoothing strength but does not yet invoke a separate texture-preservation model.

Design rules:

- Use a clear switch-like selected state.
- Keep the explanatory line visible but secondary.
- Do not claim that actual pores or grain are reconstructed until that processing exists.

### 12.12 Reset action

Current behavior:

- Restores:
  - `preserveTexture = true`
  - `smoothingIntensity = 0.45`
  - `selectedTool = "Skin"`
  - `selectedPreset = "Smooth"`
- Shows `Retouch reset` status.

Design:

- Secondary dark button.
- White label.
- 48dp minimum height.
- Should remain easy to reach but less visually prominent than Apply.

### 12.13 Apply to Preview action

Current behavior:

- Invokes the apply callback.
- Shows feedback based on whether `capturedUri` exists.
- Does not save an edited image.
- Does not modify the original source image.

Messages:

- No source: `No source image; retouch applied to live preview`.
- Source exists: `Retouch applied to live preview; source image unchanged`.

Design rules:

- Coral filled button.
- Bold CoralDeep label.
- Visually dominant action in the bottom row.
- The label should accurately describe the current behavior; do not call it Save until saving exists.

Recommended copy improvement for the current behavior:

`APPLY TO PREVIEW`

is more accurate than `APPLY & SAVE` because the implementation does not save a processed image.

---

## 13. System Surface: Android Image Picker

### 13.1 Entry points

- Camera Deck Gallery button.
- Bottom Navigation Gallery item.

### 13.2 Contract

- Android Activity Result `GetContent`.
- MIME type: `image/*`.

### 13.3 Success behavior

- Store returned URI as `capturedUri`.
- Show `Photo selected` status.
- Return to the current app surface.

### 13.4 Cancel behavior

- Keep the existing URI.
- Do not show a success message.
- Do not clear the live camera state.

### 13.5 Future in-app gallery direction

If a native gallery is added later, it should include:

- Permission-aware media access.
- Empty state when no images are available.
- Thumbnail grid with stable aspect ratios.
- Clear selected state.
- Back navigation to Camera Studio.
- No automatic destructive editing of source images.

---

## 14. Overlay: Camera Settings Dialog

### 14.1 Entry points

- Top Bar Settings button.
- Bottom Navigation Profile item.

### 14.2 Purpose

Expose a concise explanation of current camera-linked behavior without introducing a full settings architecture.

### 14.3 Layout

```text
Dialog surface
  +-- Camera settings title
  +-- texture preservation state
  +-- flash and zoom hardware explanation
  `-- Done action
```

### 14.4 Visual design

- Mist background.
- 24dp corner radius.
- Approximately 22dp internal padding.
- Ink title.
- MutedText supporting copy.
- CoralDeep Done action.

### 14.5 Content

Dynamic text:

- `Texture preservation is on.`
- `Texture preservation is off.`

Static explanation:

`Flash and zoom follow the connected camera hardware.`

### 14.6 Interaction

- `Done` closes the dialog.
- Outside dismissal closes the dialog.
- Android back closes the dialog.
- Opening the dialog does not pause the camera pipeline unless platform lifecycle requires it.

### 14.7 Future settings direction

Potential settings that fit the product:

- Mirror preview toggle.
- Haptic shutter toggle.
- Reticle visibility.
- Debug face-landmark overlay.
- Default camera lens.
- Capture quality.

These should not be added until they have real behavior and persistence requirements.

---

## 15. Overlay: Status Message

### 15.1 Purpose

Provide short feedback without taking the user away from the current surface.

### 15.2 Placement

- Top-center of the active surface.
- Below the top bar.
- Dark translucent pill.
- White text.
- Adequate horizontal padding for short messages.

### 15.3 Current messages

| Message | Trigger |
|---|---|
| `Face detected` | Face Landmarker returns at least one face |
| `Photo captured` | CameraX capture succeeds |
| `Photo selected` | Android picker returns an image URI |
| `Retouch reset` | Retouch Reset is pressed |
| `No source image; retouch applied to live preview` | Apply without selected source |
| `Retouch applied to live preview; source image unchanged` | Apply with selected source |
| `Camera unavailable: ...` | Camera binding fails |
| `Camera initialization failed: ...` | Camera provider initialization fails |
| `Storage permission is required to save photos` | Legacy storage permission is missing or denied |
| `Could not prepare photo storage` | MediaStore insertion returns no URI |
| `Capture failed` | ImageCapture reports an error |
| Analyzer or model error | Face analysis or model initialization fails |

### 15.4 Message rules

- Messages should be concise.
- Long exception details should be shortened for the UI and preserved in logs where appropriate.
- Status should not cover the user's eyes or mouth for an extended period.
- Success and error messages should have different semantic announcements if accessibility support is expanded.

---

## 16. Runtime and Error States

### 16.1 Camera permission denied

Current behavior:

- Remain on Camera Permission screen.
- Allow another attempt.

Recommended improvement:

- If Android reports “Don't ask again,” show an explanation and an Open Settings action.

### 16.2 Camera provider failure

Current feedback:

`Camera initialization failed: ...`

Design requirement:

- Never show a functioning camera control layout that implies the camera is ready when binding failed.
- Provide a retry path in a future dedicated error state.

### 16.3 Camera binding failure

Current feedback:

`Camera unavailable: ...`

Possible causes:

- Lens is unavailable.
- Camera is in use by another app.
- Device hardware does not support the requested combination of use cases.
- Lifecycle binding failed.

### 16.4 Face model unavailable

Behavior:

- Try GPU first.
- Fall back to CPU.
- Report an error if both fail.
- Camera preview may continue without face-aware makeup.

UI recommendation:

- Explain that the camera is still available but face effects are unavailable.
- Do not block basic camera preview unnecessarily.

### 16.5 No face detected

Behavior:

- Keep the camera preview active.
- Clear the previous makeup texture.
- Avoid showing stale lips or blush on an empty frame.
- Do not crash when the result contains no faces or incomplete landmarks.

Recommended UI:

- Optional subtle status: `Move into frame`.
- Avoid persistent warnings while the user is intentionally pointing away.

### 16.6 Flash unavailable

Behavior:

- Disable the flash button.
- Use reduced icon alpha.
- If flash was previously active, reset it to Off.

### 16.7 Storage permission denied

Behavior:

- Do not leave capture locked.
- Show a clear storage message.
- Keep the live camera usable.

### 16.8 Capture failure

Behavior:

- Delete pending MediaStore URI where applicable.
- Release the capture lock.
- Show `Capture failed`.

### 16.9 Orientation and lens switching

Requirements:

- Support 0, 90, 180, and 270 degree coordinate conversion.
- Apply mirroring consistently for the front camera.
- Clear stale mask during camera teardown.
- Preserve stable control layout during rebind.

---

## 17. Accessibility Specification

### 17.1 General

- All interactive elements need a meaningful label.
- Selected, disabled, and checked states must be exposed semantically.
- Do not communicate state through color alone.
- Maintain minimum touch targets even when visuals are compact.
- Support system font scaling without clipping labels.

### 17.2 Camera controls

| Control | Required semantic information |
|---|---|
| Flash | Current on/off and availability |
| Settings | Opens camera settings |
| Preset | Name and selected/not-selected state |
| Zoom | Value and selected/not-selected state |
| Gallery | Opens image picker |
| Shutter | Captures photo; disabled or busy state if applicable |
| Flip camera | Switches front/rear camera |
| Texture toggle | Switch role and On/Off state |
| Bottom navigation item | Destination name and selected state |

### 17.3 Retouch controls

| Control | Required semantic information |
|---|---|
| Tool | Tool name and selected state |
| Retouch preset | Preset name and selected state |
| Smoothing slider | Current percentage |
| Texture toggle | Switch role and On/Off state |
| Reset | Resets retouch controls |
| Apply | Applies current state to preview |

### 17.4 Contrast

- White text over a live preview should use a darkened backing surface where needed.
- CoralDeep must be used on Coral controls when white would not be sufficiently legible.
- Disabled controls must remain readable enough to understand why they are unavailable.

---

## 18. Responsive Layout Requirements

### 18.1 Portrait phones

Primary target:

- Full-screen portrait camera.
- Compact top bar.
- Camera deck above bottom navigation.
- Horizontally scrollable preset rows.

### 18.2 Small-width devices

- Never force all presets into one row without scrolling.
- Keep the shutter centered.
- Allow secondary labels to wrap rather than clip.
- Reduce decorative padding before reducing touch targets.
- Keep status capsules within screen edges.

### 18.3 Large phones and tablets

- Keep the camera preview full bleed.
- Constrain control content to a readable maximum width where appropriate.
- Avoid stretching small labels across the entire screen.
- Preserve the centered shutter relationship with the preview.

### 18.4 System insets

- Respect status bar and navigation bar insets.
- Keep bottom navigation above the system navigation area.
- Keep the shutter and deck clear of gesture navigation zones.

### 18.5 Font scaling

At larger font scales:

- Do not truncate primary action labels.
- Allow permission copy and settings copy to wrap.
- Preserve icon-button touch targets.
- Let preset rows scroll horizontally.

---

## 19. State and Data Contracts

### 19.1 Retouch defaults

```text
preserveTexture = true
smoothingIntensity = 0.45
selectedTool = Skin
selectedPreset = Smooth
```

### 19.2 Reset behavior

Reset must restore every RetouchState field, not only the visible slider.

### 19.3 Apply behavior

The current Apply action is preview/status behavior only:

- It does not save a processed image.
- It does not alter the original image URI.
- It should not be labeled Save until persistence is implemented.

### 19.4 Camera and retouch synchronization

The following values are shared:

- `preserveTexture`.
- `smoothingIntensity`.

Camera-only state:

- Preset.
- Zoom.
- Lens facing.
- Flash.
- Ambient light.

Retouch-only state:

- Selected tool.
- Retouch preset.

---

## 20. Face Mask and Preview UI Contract

Although the face mask is primarily a rendering concern, the UI specification depends on its behavior.

### 20.1 Valid face result

When a valid face result exists:

1. Read landmarks from the first detected face.
2. Convert coordinates using frame rotation.
3. Apply front-camera mirroring when required.
4. Clamp normalized coordinates to 0.0-1.0.
5. Generate a mask using the renderer's preview dimensions.
6. Queue the bitmap handoff to the GL thread.
7. Upload the mask as a GL texture.
8. Render lips and cheek effects over the camera texture.

### 20.2 Small motion threshold

Landmarks that move less than the configured threshold should not force a new bitmap or GL texture upload.

This protects:

- UI responsiveness.
- Battery life.
- Thermal performance.
- GL thread workload.

### 20.3 Empty or incomplete results

- Empty face list produces a transparent mask.
- Missing landmark indices are skipped safely.
- A mask with no visible pixels must clear the previous texture.
- Bitmaps must be recycled exactly once after GL upload or disposal.

### 20.4 Debug visibility

Debug builds may report state transitions for:

- Face detected/not detected.
- Non-empty mask generated.
- Mask uploaded to GL.

Release builds must not log every frame.

---

## 21. Current Implementation vs. Future UI Direction

| Area | Current implementation | Future direction |
|---|---|---|
| Camera preview | Live CameraX + GL preview | Keep full bleed and add truthful processing states |
| Makeup mask | Lip and cheek mask from landmarks | Add additional channels only with real processing support |
| Presets | UI state and glow mapping | Connect each preset to an explicit visual treatment |
| Retouch preview | Static reticle illustration | Show live or selected source image |
| Gallery | Android system picker | Add native gallery grid if product needs it |
| Profile | Opens settings dialog | Create profile only when account features exist |
| Apply | Status feedback and preview state | Add real export/save flow later |
| Texture preservation | State and smoothing adjustment | Add dedicated texture model later |
| Error handling | Status capsule | Add dedicated retryable error states |
| Navigation | Local Compose state | Introduce navigation only when destinations grow |

---

## 22. Screen Inventory & Roadmap

| Phase | Screen Count | Surfaces / Screens |
|---|---|---|
| **Current (V1.0)** | **3 Core + 3 Overlays** | PermissionPrompt, CameraStudio, RetouchScreen, SettingsDialog, SystemPicker, StatusOverlay |
| **V1.5 Expansion** | **+4 Key Screens** | In-App Gallery / Album Grid, Photo Editor & Post-Capture, User Profile & Saved Looks, Full Camera Preferences |
| **V2.0 Full Suite** | **+3 Polish Screens** | Onboarding Carousel, Permission Recovery, Looks & Presets Store / Explorer |

### Complete Screen Matrix

| Surface | Status | Primary Purpose |
|---|---|---|
| Camera Permission | Implemented (V1.0) | Request camera access on fresh install |
| Camera Studio | Implemented (V1.0) | Live viewfinder, face effects, quick deck controls, photo capture |
| Retouch Screen | Implemented (V1.0) | Retouch parameters, presets, texture toggle, smoothing intensity |
| Android Image Picker | Implemented (V1.0) | Temporary OS-level photo chooser |
| Camera Settings Dialog | Implemented (V1.0) | Lightweight popup for quick hardware & texture status |
| Status Message | Implemented (V1.0) | Dynamic feedback pill for face detection, capture, and errors |
| In-App Gallery & Album Grid | V1.5 Specification | Native media browser for captured photos, albums, and multi-select |
| Photo Editor & Post-Capture | V1.5 Specification | High-res photo editor with Before/After split slider, retouch, save & share |
| User Profile & Saved Looks | V1.5 Specification | Custom recipes, favorite looks, saved presets, and creator profile |
| Full Camera Preferences | V1.5 Specification | Dedicated settings for grid lines, front mirror, shutter sound, watermark, RAW |
| Onboarding Welcome Carousel | V2.0 Specification | 3-slide visual onboarding explaining AI glow, micro-texture, and adaptive lighting |
| Permission Denied Recovery | V2.0 Specification | Helpful recovery screen with 1-tap link to system app settings |
| Preset & Looks Explorer | V2.0 Specification | Visual discovery catalog / marketplace for trending makeup & lighting looks |

---

## 23. Detailed Specifications for Future Screens (V1.5 & V2.0)

### 23.1 Screen: In-App Gallery & Album Grid (V1.5)

#### Purpose & User Goal
Provide a native, seamless media browsing experience replacing the OS file picker. Users view photos captured with Nyxia Glow, organize them by date or album, and select them for editing or sharing.

#### Layout Hierarchy
```text
Top App Bar
  +-- Back icon button (returns to Camera Studio)
  +-- Title: "Gallery" (18sp SemiBold White)
  +-- Action: "Select" text button (toggles multi-select mode)
Tab Filter Strip
  +-- "Nyxia Glow" (default pill) | "All Photos" | "Favorites"
Media Grid Viewport
  +-- 3-column square thumbnail grid with 2dp gaps
  +-- Date section headers ("Today", "Yesterday", "September 2026")
  +-- Thumbnail Card Component:
        - Photo thumbnail
        - Badges (Heart icon if favorited, "RAW" badge if DNG, "✨ Retouched" badge)
        - Checkbox overlay (when in multi-select mode)
Bottom Context Action Bar (Active only during multi-select)
  +-- Share (Launches native Android share sheet)
  +-- Favorite toggle
  +-- Delete (Launches deletion confirmation bottom sheet)
```

#### States
- **Loading**: Shimmer grid placeholders.
- **Populated**: Smooth 3-column scrollable grid.
- **Empty**: Centered camera icon, "No photos captured yet", Coral button "Open Camera".
- **Multi-Select**: Blue/Coral checkmarks over selected items, bottom action bar slides up.

---

### 23.2 Screen: Single Photo Editor & Post-Capture Screen (V1.5)

#### Purpose & User Goal
The primary editing and review studio. Opens after tapping a gallery photo or immediately following capture. Allows users to compare original vs retouched with an interactive split slider, adjust beauty layers, and export high-resolution photos.

#### Layout Hierarchy
```text
Top Navigation Bar
  +-- Close / Back button (with discard confirmation if edited)
  +-- Undo / Redo icon buttons
  +-- Primary CTA: "Save" (Coral pill button)
Interactive Viewport (Center)
  +-- Pinch-to-zoom / pan high-res photo canvas
  +-- Interactive Vertical Split Slider:
        - Draggable center vertical bar with Coral diamond handle
        - Left tag: "ORIGINAL" | Right tag: "GLOW RETOUCHED"
  +-- Floating Quick Action: "Hold for Original" (hold anywhere to crossfade to raw image)
Retouch Control Deck (Bottom Sheet)
  +-- Category Tabs: Skin | Makeup | Lighting | Presets | Crop
  +-- Contextual Intensity Slider: (0% to 100% with numerical floating indicator)
  +-- Preset Carousel: Real-time thumbnail preview circles with live effect applied
Bottom Export Bar
  +-- Direct Share Icons: Instagram, TikTok, WhatsApp, More (...)
  +-- Delete / Revert to Original button
```

#### Key Interactions
- **Split Comparison**: Dragging the vertical divider left/right reveals unprocessed pixels vs shader-rendered beauty enhancement.
- **Baking & Save**: Applies full-resolution OpenGL shaders & makeup mask generator to source bitmap, encodes JPEG/PNG to MediaStore, presents animated success checkmark.

---

### 23.3 Screen: User Profile & Saved Looks (V1.5)

#### Purpose & User Goal
Replaces the temporary modal on the bottom navigation "Profile" tab. Acts as a personal beauty studio dashboard where users store custom formulas ("My Looks"), manage favorite presets, and access system settings.

#### Layout Hierarchy
```text
Header Profile Card
  +-- Circular avatar with gradient Coral ring
  +-- User Display Name ("Stav") & Bio tagline ("Radiant & Dewy vibes")
  +-- Stats Row: "12 Saved Looks" | "148 Photos Captured"
"My Custom Looks" Section (Horizontal Carousel)
  +-- "+ Create New Look" Card (dashed Coral border)
  +-- Look Card Component:
        - Photo portrait snapshot with current look
        - Look Title (e.g. "Golden Hour Glow")
        - Recipe badges: "Soft Glow 70%", "Dewy Preset", "Coral Lip 50%"
        - Action: "Apply to Live Camera" button
"Favorite Presets" Section
  +-- Quick-access pill list of top 4 favorite camera presets
Settings & Preferences List (Card Group)
  +-- Camera Preferences (Grid, Watermark, RAW, Shutter Sound)
  +-- Storage & Cache Management
  +-- About & Version Information
```

---

### 23.4 Screen: Full Camera Preferences Page (V1.5)

#### Purpose & User Goal
A comprehensive settings page for advanced camera hardware controls, composition guides, and quality parameters.

#### Layout Hierarchy & Setting Items
```text
Section: Viewfinder & Composition
  +-- Composition Grid Lines [Options: Off / Rule of Thirds / Golden Ratio]
  +-- Horizon Level Indicator [Switch: On/Off]
  +-- Front Camera Mirroring [Switch: On/Off]

Section: Capture & Quality
  +-- Shutter Sound [Switch: On/Off]
  +-- Haptic Feedback on Shutter [Switch: On/Off]
  +-- Image Format [Options: JPEG (High Quality) / JPEG + RAW (DNG)]
  +-- "Shot on Nyxia Glow" Watermark [Switch: On/Off]

Section: AI & Engine Defaults
  +-- Default Startup Look [Options: Soft / Radiant / Velvet / Defined]
  +-- Default Micro-Texture State [Switch: On/Off]
  +-- Auto Ambient Light Adaptation [Switch: On/Off]

Section: Storage & Diagnostics
  +-- Save Location info: "/Pictures/Nyxia Glow"
  +-- Clear Render Cache button
  +-- Reset All Camera Settings to Default
```

---

### 23.5 Screen: Onboarding Welcome Carousel (V2.0)

#### Purpose & User Goal
Appears on first launch before the permission prompt. Teaches new users the 3 core pillars of the Nyxia Glow engine through rich visual storytelling.

#### Layout Hierarchy
```text
Top Bar
  +-- "Skip" button (top right, jumps immediately to permission prompt)
3-Slide Carousel Viewport (Swipeable horizontal pager)
  Slide 1: "Face-Aware Neural Glow"
    - Visual: High-fashion portrait with glowing Coral facial mesh converting into soft real-time makeup.
    - Description: "Real-time AI makeup and luminous skin enhancement that tracks naturally with your facial expressions."
  Slide 2: "Preserve Natural Texture"
    - Visual: Macro beauty close-up highlighting natural skin pores, eyelashes, and authentic grain.
    - Description: "Never look blurred or plastic. Our engine preserves authentic skin texture while softening harsh blemishes."
  Slide 3: "Adaptive Studio Lighting"
    - Visual: Triple-split portrait demonstrating low-light boost, balanced daylight, and studio highlight softening.
    - Description: "Intelligent ambient light sensing adapts camera exposure and glow balance to any room or environment."
Bottom Navigation Controls
  +-- 3-dot pagination indicator (active dot expands to Coral pill)
  +-- Primary Button: "Next" (Slides 1 & 2) / "Get Started" (Slide 3 -> launches Camera Permission Screen)
```

---

### 23.6 Screen: Permanent Permission Recovery Screen (V2.0)

#### Purpose & User Goal
Shown when the user has permanently denied camera permission (selected "Don't ask again" in Android system dialogs). Guides the user to manually enable camera access in Android system settings.

#### Layout Hierarchy
```text
Centered Content Card (Mist / SurfaceDark background)
  +-- Camera Blocked Icon (Coral icon inside soft red warning halo)
  +-- Headline: "Camera Access Required" (22sp SemiBold)
  +-- Explanation Body:
      "Nyxia Glow requires camera access to provide the real-time beauty viewfinder.
       Permission was permanently disabled in Android settings."
  +-- Step-by-Step Instruction Card:
        1. Tap 'Open App Settings' below
        2. Tap 'Permissions' -> 'Camera'
        3. Select 'Allow only while using the app'
  +-- Primary CTA Button: "Open System Settings" (Launches Android Application Details Intent)
  +-- Secondary Text Button: "Check Permission Again" (Re-evaluates ContextCompat.checkSelfPermission)
```

---

### 23.7 Screen: Preset & Looks Explorer / Store (V2.0)

#### Purpose & User Goal
A visual discovery catalog where users explore, preview, and download new curated aesthetic lighting styles, seasonal makeup looks, and beauty presets.

#### Layout Hierarchy
```text
Top App Bar
  +-- Back Button | Search Bar | Filter Icon
Category Chip Strip
  +-- "Trending" | "Minimal" | "Golden Hour" | "Editorial" | "K-Beauty" | "Dewy Night"
Featured Look Hero Banner
  +-- Full-bleed editorial card with live preview model
  +-- Look Title: "Sunset Silk"
  +-- "Try On Live in Camera" floating button
Curated Staggered 2-Column Grid
  +-- Look Card Component:
        - Interactive Before/After split thumbnail
        - Look Title & Creator Name
        - Channel tags: [👄 Velvet Rose] [🌸 Soft Coral Blush] [✨ Warm 0.7 Glow]
        - One-tap "Apply to Camera" button
```

---

### Camera Permission

- Prompt appears when permission is missing.
- Enable camera launches the system request.
- Granted permission opens Camera Studio.
- Denied permission leaves the prompt usable.

### Camera Studio

- Preview occupies the available surface.
- Top bar controls remain reachable.
- Flash disabled state matches hardware.
- Camera flip rebinds the correct lens.
- Zoom values clamp safely to hardware.
- Shutter prevents overlapping captures.
- Gallery opens the system picker.
- Status messages do not cover critical controls.

### Face and mask behavior

- Rotation 0, 90, 180, and 270 degrees align the mask.
- Front-camera mirroring aligns lips and cheeks.
- Edge points clamp safely.
- Missing landmarks do not crash.
- Valid landmarks create non-empty alpha.
- No-face results clear the old mask.
- Small landmark changes do not create unnecessary uploads.
- Mask bitmaps are recycled exactly once.

### Retouch

- Tool selection updates selected semantics.
- All retouch presets can be selected.
- Slider remains within 0.0-1.0.
- Texture toggle updates Camera Overlay state.
- Reset restores every default.
- Apply displays truthful preview-only feedback.

### Settings and system surfaces

- Settings opens from Top Bar.
- Profile opens the same current settings surface.
- Done and outside dismissal close the dialog.
- Picker cancellation does not overwrite the current selection.
- Selected image URI produces `Photo selected` feedback.

### Accessibility and layout

- All icon buttons have labels.
- Selected and disabled states are exposed.
- Large text does not clip primary labels.
- Controls remain above system navigation insets.
- Preset rows scroll on narrow devices.

---

## 24. Product Language Rules

Use language that reflects the real behavior:

- Say `Apply to preview` while the app does not export an edited file.
- Say `Photo selected` when a URI is chosen; do not say imported or edited.
- Say `Face detected` for a detection result; do not imply identity recognition.
- Say `Camera unavailable` for camera binding issues; do not blame permission unless permission is the cause.
- Do not use `4K RAW` as a technical promise without matching capture configuration.
- Do not describe static reticle values as measured accuracy.

---

## 25. Summary

Nyxia Glow should feel like a focused camera studio rather than a collection of settings pages. The Camera Studio is the product center: the face remains visible, the shutter is always discoverable, and controls are grouped by the moment in which they are needed.

The Retouch Screen supports deeper adjustment without pretending that every control already performs a full image-processing operation. The system picker and settings dialog remain lightweight secondary surfaces. Future UI work should preserve this hierarchy, keep claims truthful, and add complexity only when a real capability requires it.

---

## 26. Screen Creation Prompts

This section contains copy-ready prompts for creating the Nyxia Glow screens with an AI design tool, UI generator, or coding agent.

### 26.1 Master product prompt

```text
Design and implement the Nyxia Glow Android beauty-camera studio in English.

Product goal:
Create a calm, premium, face-first camera experience. The live camera preview is the primary content. Controls must frame the preview without covering the user's face, and the shutter must remain the most discoverable action.

Platform:
- Native Android.
- Jetpack Compose.
- Portrait-first layout.
- CameraX live preview.
- OpenGL-rendered camera and makeup mask preview.

Visual direction:
- Dark camera studio interface.
- Coral accent color #FF9A8B.
- Deep coral text/icon color #96463B.
- Ink background #111111.
- Raised dark surface #242020.
- Light Mist surface #F5F1F0 for permission and settings surfaces.
- Compact typography with clear hierarchy.
- Rounded controls, but avoid excessive nested cards.
- Use Material icons and meaningful content descriptions.
- Use restrained motion only for selection changes, reticle entrance, and status feedback.

Required surfaces:
1. Camera Permission screen.
2. Camera Studio screen.
3. Retouch screen.
4. Camera Settings dialog.
5. Android image picker entry flow.
6. Status and error overlays.

Current behavior constraints:
- Camera permission is required before Camera Studio appears.
- Camera Studio includes CameraX preview, face-aware lip and cheek makeup mask rendering, ambient-light guidance, zoom, flash, camera flip, presets, gallery picker, and photo capture.
- Face mask coordinates must support 0, 90, 180, and 270 degree rotation.
- Front-camera mirroring must remain consistent.
- No-face results must clear the previous mask.
- Retouch controls update shared state, but not every tool or preset has an independent pixel-processing implementation yet.
- Apply currently updates preview/status behavior and does not save a processed image.
- Gallery opens the Android system image picker; do not invent an in-app gallery grid.
- Profile currently opens Camera Settings; do not invent account functionality.

Do not add:
- Jetpack Navigation unless explicitly requested.
- A ViewModel refactor.
- A gallery grid.
- A profile/account system.
- A save/export flow for processed images.
- A skin mask or texture model.
- A replacement camera or YUV conversion pipeline.

Deliver:
- Screen hierarchy.
- Responsive layout behavior.
- Component states.
- Interaction descriptions.
- Accessibility semantics.
- Loading, empty, disabled, and error states.
- Production-ready Compose structure or a high-fidelity screen design, depending on the target tool.
```

### 26.2 Camera Permission screen prompt

```text
Create the Nyxia Glow Camera Permission screen for a native Android app.

Layout:
- Full-screen Mist background (#F5F1F0).
- Center a vertical content column with a maximum readable width of about 300dp.
- Place the Aura circular logo at the top, approximately 72dp.
- Add the title: "Camera access brings the glow to life".
- Add the supporting text: "Nyxia Glow needs your camera for the live beauty preview.".
- Add one primary button labeled "Enable camera".

Design:
- Title in Ink (#111111), 21-24sp, SemiBold, centered.
- Supporting copy in muted gray, 14sp, centered.
- Button filled with Coral (#FF9A8B) and CoralDeep (#96463B) text.
- Use generous vertical spacing and avoid decorative content that competes with the permission action.

Behavior:
- The button launches the Android camera permission request.
- If permission is granted, transition to Camera Studio.
- If permission is denied, keep the screen visible and usable.
- Do not imply that photo storage permission is needed at this stage.

Accessibility:
- Button has a clear accessibility label.
- Text is read in title, explanation, button order.
- The button remains at least 48dp high.
- Do not communicate the action through color alone.
```

### 26.3 Camera Studio screen prompt

```text
Create the main Nyxia Glow Camera Studio screen as a full-bleed native Android camera interface.

Primary content:
- A live camera preview fills the screen.
- The preview is the visual priority and must not be placed inside a decorative card.
- Leave the user's face unobstructed.
- Add a subtle dark vertical gradient behind controls for readability.

Top bar:
- Left: Aura logo and "NYXIA-GLOW".
- Center or adjacent context label: "Camera".
- Right: flash icon button and settings icon button.
- All icon buttons have at least 48dp touch targets.

Center overlay:
- Compact status capsule with a Coral dot, "AI ACTIVE", and "4K RAW".
- Circular Neural Focus reticle with subtle Coral glow, progress arc, outer ring, dashed inner ring, Aura mark, and lighting status.
- Show the reticle temporarily after preset, zoom, flash, or lens changes.
- Add a compact row labeled "Preserve natural texture" with an AutoAwesome icon and On/Off value.

Bottom camera deck:
- Horizontally scrollable preset chips: Soft, Radiant, Velvet, Defined.
- Zoom selector: 0.5x, 1x, 2x, 3x.
- Bottom action row: gallery, large circular shutter, flip camera.
- Keep the shutter centered and visually dominant.

Bottom navigation:
- Gallery, Looks, Camera, Profile.
- Camera is an elevated Coral circular action.
- Looks opens Retouch.
- Gallery opens the Android image picker.
- Profile opens Camera Settings; it is not a user account screen.

Required states:
- Initializing camera.
- Ready.
- Face detected.
- No face detected with old mask cleared.
- Flash unavailable.
- Camera binding error.
- Capture in progress.
- Capture success.
- Capture failure.

Technical truth:
- Face Landmarker results drive lip and cheek mask rendering.
- Support 0, 90, 180, and 270 degree rotations.
- Mirror front-camera coordinates consistently.
- Do not claim that 4K RAW is real unless capture configuration supports it.
```

### 26.4 Retouch screen prompt

```text
Create the Nyxia Glow Retouch screen for Jetpack Compose.

Purpose:
Provide deeper retouch controls while keeping the interface calm, compact, and honest about what is currently implemented.

Layout:
- Full-screen SurfaceDark background (#171515).
- Top padding for the app context.
- Header row with "AI CORE V2.4 ACTIVE" and a small "HOLD BEFORE" capsule.
- Large dark preview frame with a subtle retouch reticle and the text "98.4% NATURAL MATCH".
- Four retouch tools: Skin, Shape, Light, Makeup.
- Section label: "PRESETS & TONE".
- Horizontally scrollable tiles: Smooth, Freckles, Matte, Dewy, Refine.
- Smoothing intensity slider from 0.0 to 1.0, default 0.45.
- Full-width "Subtle Micro-Texture" toggle row with supporting text "Preserves natural pores & grain".
- Bottom action row with RESET as secondary and APPLY TO PREVIEW as primary.

Interaction:
- Selecting a tool updates selected state.
- Selecting a preset updates selected state.
- Slider reports a bounded value from 0.0 to 1.0.
- Texture toggle is shared with Camera Studio.
- Reset restores texture On, smoothing 0.45, Skin, and Smooth.
- Apply reports preview-only feedback and must not claim to save an edited image.

Design constraints:
- Use selected color, text weight, and semantics; do not rely only on color.
- Keep the Apply button more prominent than Reset.
- Do not turn the preview frame into a fake image editor.
- Do not add a gallery grid or save flow.
- If a label describes a future model, visually mark it as unavailable or keep it as a state control only.
```

### 26.5 Camera Settings dialog prompt

```text
Create a compact Camera Settings dialog for Nyxia Glow.

Open from:
- Settings icon in the Camera Studio top bar.
- Profile item in bottom navigation.

Visual design:
- Mist background (#F5F1F0).
- 24dp rounded corners.
- Approximately 22dp internal padding.
- Title: "Camera settings".
- Supporting text showing either "Texture preservation is on." or "Texture preservation is off.".
- Supporting text: "Flash and zoom follow the connected camera hardware.".
- One CoralDeep text action labeled "Done".

Behavior:
- Done closes the dialog.
- Outside tap and Android back dismiss the dialog.
- The dialog does not pretend to contain settings that are not implemented.
- Do not add profile, account, cloud, or subscription controls.
```

### 26.6 Status and error state prompt

```text
Design status and error feedback for the Nyxia Glow camera app.

Use a compact dark translucent capsule below the top bar. Keep messages short and readable over the camera preview.

Support these messages:
- Face detected
- Photo captured
- Photo selected
- Retouch reset
- No source image; retouch applied to live preview
- Retouch applied to live preview; source image unchanged
- Camera unavailable
- Camera initialization failed
- Storage permission is required to save photos
- Could not prepare photo storage
- Capture failed
- Face effects unavailable

Rules:
- Do not show a message for every analysis frame.
- Do not let status text cover the user's eyes or mouth for a long period.
- Use distinct icon and text treatment for errors and successes.
- Explain the next action when possible, such as retrying or granting permission.
- Keep long exception details out of the main UI.
```

### 26.7 Responsive and accessibility prompt

```text
Make the Nyxia Glow screens responsive and accessible on Android portrait phones and larger devices.

Responsive requirements:
- Respect status and navigation bar insets.
- Keep the camera preview full bleed.
- Keep the shutter centered.
- Make preset rows horizontally scrollable.
- Keep all status capsules inside screen bounds.
- Allow supporting text to wrap naturally.
- Never reduce touch targets below 48dp to fit more content.
- Test narrow phones, large phones, tablets, and large system font settings.

Accessibility requirements:
- Add content descriptions to all icon-only controls.
- Expose selected/not-selected state for presets, zoom, tools, and bottom navigation.
- Expose Switch role and On/Off state for texture preservation.
- Expose Slider role and current percentage.
- Expose disabled state for unsupported flash.
- Make the shutter announce that it captures a photo.
- Do not communicate state by color alone.
- Preserve a logical TalkBack order: context, preview status, controls, capture, navigation.
```

### 26.8 Compose implementation prompt

```text
Implement the existing Nyxia Glow screen design in Jetpack Compose without changing the camera architecture.

Use the existing composable boundaries where possible:
- NyxiaGlowApp
- PermissionPrompt
- GlowStudio
- CameraPreview
- TopBar
- CameraOverlay
- CameraDeck
- BottomNavigation
- RetouchScreen

Implementation rules:
- Preserve CameraX Preview, ImageCapture, ImageAnalysis, Face Landmarker, MakeupMaskGenerator, and BeautyCameraRenderer.
- Preserve callback ownership and ImageProxy closing behavior.
- Do not move camera processing into a new ViewModel as part of the UI pass.
- Keep UI state behavior compatible with the existing RetouchState.
- Use stable dimensions for shutter, icon buttons, chips, slider, and preview frame.
- Use semantics for selected, switch, slider, and disabled states.
- Keep horizontal lists scrollable on narrow screens.
- Use the existing color language and Material icons.
- Add loading and error visuals without replacing the live camera surface.
- Do not add unrelated navigation or persistence.

Before finishing:
1. Check portrait layouts at narrow and large widths.
2. Check large font scaling.
3. Check front and rear camera controls.
4. Check no-face state and stale-mask clearing.
5. Check picker cancellation and capture failure states.
6. Run unit tests, lint, and debug build.
```

---

## 27. Copy-Ready Google Stitch Prompts for All App Screens

Use these prompts directly in **Google Stitch** (or other AI design / UI generation tools) to generate high-fidelity, production-grade Android mockups in the exact Nyxia Glow design system.

### 27.1 Stitch Prompt: In-App Gallery & Album Grid (V1.5)

```text
Create a high-fidelity mobile UI design in Google Stitch for an Android camera app named "Nyxia Glow".
Screen: In-App Gallery & Media Grid (Dark Theme).

Style & Theme:
- Background: Pitch dark #171515.
- Accents: Coral #FF9A8B and Deep Coral #96463B.
- Text: Crisp white #FFFFFF with soft gray #B8C3BB for dates.

Layout & Structure:
1. Top App Bar:
   - Left: Back arrow icon button to camera.
   - Center: "Gallery" (18sp SemiBold white).
   - Right: "Select" text button (Coral accent).
2. Filter Tab Strip (Pill style):
   - Active Tab: "Nyxia Glow" (Coral filled pill, CoralDeep text).
   - Inactive Tabs: "All Photos", "Favorites" (Translucent dark pills with white text).
3. Media Grid:
   - 3-column square grid with 2dp gaps.
   - Section headers with sticky date badges: "Today", "Yesterday", "14 Sep 2026".
   - Sample beauty camera selfies with clean lighting.
   - Subtle top-right badge on retouched photos: tiny coral sparkle icon "✨".
4. Empty State Variant:
   - Centered circular outlined camera icon in soft coral.
   - "No photos captured yet" (16sp SemiBold white).
   - "Your captured radiance will appear here" (13sp muted gray).
   - Filled Coral CTA button: "Open Camera".
5. Multi-Select Floating Action Bar (bottom docked):
   - Appears when in select mode.
   - Actions: Share icon, Favorite heart icon, Delete trash icon.
```

### 27.2 Stitch Prompt: Photo Editor & Post-Capture Screen (V1.5)

```text
Create a high-fidelity mobile UI design in Google Stitch for an Android beauty camera app named "Nyxia Glow".
Screen: Single Photo Editor with Interactive Before/After Split Comparison Slider.

Style & Theme:
- Background: Studio dark #111111 with floating control decks in #242020.
- Accent: Luminous Coral #FF9A8B and Deep Coral #96463B.

Layout & Structure:
1. Top Navigation Bar:
   - Left: Close 'X' icon button.
   - Center: Undo and Redo curved arrow icons.
   - Right: Primary CTA pill button "Save" (Filled Coral #FF9A8B with bold dark text).
2. Center Viewport (The Hero):
   - Full high-resolution portrait photograph of a model.
   - Prominent interactive vertical split slider cutting down the center:
     * Vertical glowing Coral line divider with a center diamond drag-handle.
     * Left side of divider tagged with translucent black badge: "ORIGINAL".
     * Right side tagged with translucent Coral badge: "GLOW RETOUCHED".
   - Floating pill button at bottom-center of image: "Hold to Compare".
3. Retouch Control Deck (Bottom Sheet container #242020, 16dp rounded top corners):
   - Tool Category Icon Row:
     * [Skin (Sparkle icon, Active Coral)]
     * [Makeup (Lipstick/Palette icon)]
     * [Lighting (Sunbeam icon)]
     * [Presets (Grid icon)]
     * [Crop (Frame icon)]
   - Intensity Slider:
     * Coral slider track with white thumb.
     * Floating numerical indicator pill: "Intensity: 75%".
   - Preset Live Thumbnails:
     * Horizontal row of 5 circular preview chips: "Smooth", "Dewy", "Matte", "Editorial", "Golden".
4. Bottom Export Row:
   - Icons for 1-tap sharing to Instagram, TikTok, WhatsApp, and generic Share Sheet.
```

### 27.3 Stitch Prompt: User Profile & Saved Looks Dashboard (V1.5)

```text
Create a high-fidelity mobile UI design in Google Stitch for an Android beauty camera app named "Nyxia Glow".
Screen: User Profile & Beauty Formula Dashboard ("My Studio").

Style & Theme:
- Background: Dark matte surface #171515.
- Cards: Elevated dark containers #242020 with 16dp corner radius.
- Accents: Coral #FF9A8B, Mist #F5F1F0.

Layout & Structure:
1. Top Header:
   - Centered title "My Studio".
   - Right: Gear / Settings icon.
2. User Profile Card:
   - Large circular avatar with a glowing dual-ring Coral aura border.
   - Name: "Stav K." (20sp Bold white).
   - Tagline: "Curating luminous & dewy aesthetics" (13sp muted gray).
   - Stats row: "8 Saved Looks" | "124 Studio Captures".
3. Section "My Custom Formulas":
   - Header: "MY CUSTOM LOOKS" with "Create New +" button.
   - Horizontal scrolling cards:
     * Card 1: Portrait thumbnail titled "Sunset Golden Hour" with tags [Glow 80%] [Coral Lip 60%] and a Coral button "Apply to Camera".
     * Card 2: Portrait thumbnail titled "Dewy Clean Girl" with tags [Smooth 45%] [Subtle Texture ON].
     * Card 3: Dashed Coral card "+ Build Look".
4. Section "Studio Preferences":
   - Clean grouped list with chevron arrows:
     * "Camera Hardware & Composition"
     * "Storage & Export Quality"
     * "Privacy & Permissions"
     * "About Nyxia Glow Engine"
```

### 27.4 Stitch Prompt: Full Camera Preferences Page (V1.5)

```text
Create a high-fidelity mobile UI design in Google Stitch for an Android camera app named "Nyxia Glow".
Screen: Full Camera Preferences & Settings Page.

Style & Theme:
- Background: Dark matte #171515 with grouped card surfaces in #242020.
- Switch toggles: Coral #FF9A8B thumb when ON, muted gray when OFF.
- Text: High-contrast white headings, muted gray secondary descriptors.

Layout & Structure:
1. Top App Bar:
   - Back arrow icon button + Title "Camera Preferences".
2. Group 1: Viewfinder & Composition:
   - "Grid Lines": Segmented choice [Off | Rule of Thirds (Active Coral) | Golden Ratio].
   - "Horizon Level Guide": Toggle switch [ON].
   - "Front Camera Mirroring": Toggle switch [ON] with caption "Save photos as previewed".
3. Group 2: Capture & Feedback:
   - "Shutter Sound": Toggle switch [OFF].
   - "Haptic Touch Feedback": Toggle switch [ON].
   - "Output Quality": Choice pill [Maximum (100%) | Balanced].
   - "RAW Capture (DNG)": Toggle switch [OFF] with hardware-aware badge "Supported".
   - "Watermark": Toggle switch [OFF] "Add subtle 'Shot on Nyxia Glow'".
4. Group 3: AI Engine Defaults:
   - "Startup Look": Dropdown showing "Soft Glow".
   - "Micro-Texture Preservation": Toggle switch [ON] "Preserves natural skin pores".
5. Bottom Actions:
   - Text button: "Reset All Camera Settings to Default" (Subtle red tint).
```

### 27.5 Stitch Prompt: Onboarding Welcome Carousel (V2.0)

```text
Create a high-fidelity mobile UI design in Google Stitch for an Android beauty camera app named "Nyxia Glow".
Screen: 3-Slide Welcome Onboarding Carousel.

Style & Theme:
- Background: Pitch dark #111111 with cinematic lighting gradients.
- Typography: Elegant modern serif/sans mix, bold titles, Coral #FF9A8B accents.

Layout & Structure:
1. Top Bar:
   - Subtle "Skip" button in top-right corner (Muted gray text).
2. Main Viewport (Swipeable Slide Card):
   - Slide 1: "Face-Aware Neural Glow"
     * Graphic: High-fashion editorial portrait with delicate glowing geometric Coral facial mesh tracking the lips and cheekbones.
     * Headline: "Real-Time Facial Intelligence" (24sp Bold White).
     * Body: "Experience AI makeup and luminous glow that maps flawlessly to your facial contours in real time."
   - Slide 2: "Preserve Natural Texture"
     * Graphic: Ultra-detailed macro beauty shot showcasing crisp pore definition, natural freckles, and silky light without plastic blur.
     * Headline: "Authentic Skin Preservation" (24sp Bold White).
     * Body: "Never look artificial. Our engine softens harsh tones while keeping your authentic skin texture alive."
   - Slide 3: "Adaptive Studio Lighting"
     * Graphic: Split visual of low-light glow enhancement transitioning to bright highlight softening.
     * Headline: "Smart Lighting Engine" (24sp Bold White).
     * Body: "Automatic ambient luminance sensing adapts camera exposure and glow balance to any room."
3. Bottom Navigation:
   - 3-Dot page indicator: Active dot is an elongated Coral pill #FF9A8B, inactive dots are translucent gray circles.
   - Primary Action Button: "Get Started" (Full-width Coral pill button with bold CoralDeep text).
```

### 27.6 Stitch Prompt: Permission Denied Recovery Screen (V2.0)

```text
Create a high-fidelity mobile UI design in Google Stitch for an Android camera app named "Nyxia Glow".
Screen: Permanent Permission Denied Recovery Flow.

Style & Theme:
- Background: Deep dark background #171515 with an elevated centered card #242020.
- Accents: Coral #FF9A8B and Warning Amber #E7B46A.

Layout & Structure:
1. Center Illustration Card:
   - Circular icon container: Camera icon with a diagonal slash inside a soft Coral ring.
   - Headline: "Camera Access Required" (22sp SemiBold White).
   - Explanation: "Nyxia Glow requires camera permission to provide the live beauty viewfinder. Access was permanently disabled in Android settings." (14sp Muted Gray).
2. Step-by-Step Instruction Card (Clean 3-step numbered box):
   - Step 1: "Tap 'Open App Settings' below"
   - Step 2: "Select 'Permissions' -> 'Camera'"
   - Step 3: "Choose 'Allow only while using the app'"
3. Action Buttons:
   - Primary CTA Button: "Open System Settings" (Full-width Coral filled button).
   - Secondary Text Button: "Check Permission Again" (White text with subtle arrow).
```

### 27.7 Stitch Prompt: Preset & Looks Explorer / Store (V2.0)

```text
Create a high-fidelity mobile UI design in Google Stitch for an Android beauty camera app named "Nyxia Glow".
Screen: Curated Looks & Lighting Preset Explorer.

Style & Theme:
- Background: Studio dark #171515.
- Cards: Modern staggered editorial layout with glowing Coral #FF9A8B tags.

Layout & Structure:
1. Top Search & Category Bar:
   - Search bar: "Search looks, tones, moods..."
   - Filter Chips Row: "🔥 Trending", "✨ Golden Hour", "💄 Velvet Editorial", "🌸 K-Beauty", "🌙 Night Dewy".
2. Hero Featured Look Banner:
   - Full-bleed wide editorial portrait card titled "Sunset Silk Look".
   - Feature Badges: [Warm Glow 0.8] [Coral Lip Tint] [Soft Peach Blush].
   - Floating CTA button: "Try On Live in Camera" (Coral circle with camera icon).
3. Staggered 2-Column Catalog Grid:
   - Look Card Component:
     * Split Before/After interactive thumbnail.
     * Look Name (e.g. "Tokyo Neon Glow", "Minimalist Clean Girl").
     * Creator Tag: "By Nyxia Studio".
     * Quick-action pill button: "Apply".
```

