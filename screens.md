# Nyxai Glow - Screen Specification

## 1. מטרת המסמך

מסמך זה מתאר את כל המסכים, החלונות, האזורים, הפקדים והזרימות הקיימים באפליקציית Nyxai Glow כפי שהם ממומשים כרגע בקוד.

המסמך מתאר את ההתנהגות בפועל, ומבדיל בין יכולות שכבר פעילות לבין פקדים שהם כרגע state/UI בלבד.

## 2. מבנה הזרימה הכללי

```text
פתיחת האפליקציה
    |
    +-- אין הרשאת מצלמה --> Camera Permission Screen
    |
    +-- יש הרשאת מצלמה --> Camera Studio
                                  |
                                  +-- Looks --> Retouch Screen
                                  +-- Gallery --> Android Image Picker
                                  +-- Profile --> Camera Settings Dialog
                                  +-- Camera --> Camera Studio
```

הניווט מתבצע באמצעות state מקומי בתוך `GlowStudio`. אין כרגע Jetpack Navigation ואין destinations נפרדים עבור Gallery או Profile.

---

## 3. מסך הרשאת מצלמה

### שם פנימי

`PermissionPrompt`

### מתי המסך מוצג

המסך מוצג כאשר לא קיימת הרשאת `android.permission.CAMERA`.

### מבנה ויזואלי

- רקע בהיר בצבע Mist.
- תוכן ממורכז אנכית ואופקית.
- לוגו Aura עגול בחלק העליון של האזור.
- כותרת: `Camera access brings the glow to life`.
- טקסט הסבר: `Nyxai Glow needs your camera for the live beauty preview.`.
- כפתור פעולה: `Enable camera`.

### רכיבים ואחריות

#### Aura Logo

- לוגו עגול המורכב מטבעות Coral ונקודת מרכז.
- משמש כסימן המותג בלבד.
- אינו פקד אינטראקטיבי.

#### Enable camera

בלחיצה:

1. נשלחת בקשת הרשאה למצלמה באמצעות Android Activity Result API.
2. אם ההרשאה מאושרת, האפליקציה עוברת ל־Camera Studio.
3. אם ההרשאה נדחית, המסך נשאר מוצג.

### מצב שגיאה

אין כרגע מסך שגיאה ייעודי לדחיית הרשאה. המשתמש נשאר במסך ההרשאה ויכול לנסות שוב.

---

## 4. Camera Studio - מסך המצלמה הראשי

### שם פנימי

`GlowStudio` עם `CameraPreview`.

### מתי המסך מוצג

המסך מוצג לאחר קבלת הרשאת מצלמה, כאשר `activeTab == "Camera"`.

### מטרת המסך

- הצגת תצוגת מצלמה חיה.
- זיהוי פנים בזמן אמת באמצעות MediaPipe Face Landmarker.
- הצגת מסכת איפור על גבי תצוגת ה־GL כאשר נמצאות פנים.
- שליטה ב־zoom, flash, camera flip, presets וצילום תמונה.
- הצגת חיווי תאורה ומצב עיבוד.

### שכבות המסך

1. CameraX preview דרך `BeautyCameraRenderer` ו־`GLSurfaceView`.
2. שכבת gradient כהה לשיפור קריאות הפקדים.
3. Top bar.
4. Camera overlay מרכזי.
5. אזור presets, zoom ופעולות צילום.
6. Bottom navigation.
7. הודעת status זמנית, כאשר קיימת.
8. Settings dialog, כאשר פתוח.

---

## 5. Top Bar

### מיקום

בחלק העליון של Camera Studio.

### רכיבים

#### Aura Logo

- מופיע בצד שמאל.
- משמש כזיהוי המותג.

#### NYXAI-GLOW

- שם האפליקציה.
- מוצג באותיות גדולות עם letter spacing.
- אינו פקד.

#### Screen title

הכותרת משתנה לפי המסך הפעיל:

- `Camera` במסך המצלמה.
- `Retouch Looks` במסך הריטוש כאשר ה־Top Bar רלוונטי לזרימה.

#### Flash button

- אייקון Flash.
- מפעיל או מכבה את ה־torch של המצלמה הפעילה.
- פעיל רק אם למצלמה המחוברת יש יחידת flash.
- מושבת ויזואלית ופונקציונלית כאשר אין flash זמין.
- צבע Coral מציין flash פעיל.
- שינוי המצלמה מאפס את flash ל־off כאשר אין flash במצלמה החדשה.

#### Settings button

- אייקון Settings.
- פותח את Camera Settings Dialog.
- אינו משנה את המסך הראשי.

---

## 6. Camera Preview ו־Face Tracking

### תצוגת המצלמה

- תופסת את כל שטח המסך.
- מוזנת מ־CameraX Preview.
- התצוגה עוברת דרך `BeautyCameraRenderer` המשתמש ב־OpenGL ES.
- renderer משלב את תמונת המצלמה עם טקסטורת makeup mask.

### CameraX use cases

המסך מחבר שלושה use cases:

- `Preview` - תצוגת המצלמה.
- `ImageCapture` - צילום תמונה.
- `ImageAnalysis` - תאורה וזיהוי פנים.

### Face Landmarker

ה־analyzer:

- עובד במצב live stream.
- מנסה GPU ומבצע fallback ל־CPU במקרה הצורך.
- מזהה עד פנים אחת.
- מעביר את rotation של `ImageProxy` לתוצאת הזיהוי.
- סוגר כל `ImageProxy` ב־`finally`.
- מזהה תוצאות פנים בערך עד 10 פעמים בשנייה.

### Makeup Mask

כאשר קיימות פנים:

1. landmarks מועברים ל־`MakeupMaskGenerator`.
2. הקואורדינטות עוברות המרה עבור rotation של 0, 90, 180 או 270 מעלות.
3. במצלמה קדמית מתבצע mirror בציר X.
4. המסכה נוצרת לפי ממדי ה־preview שה־renderer קיבל בפועל.
5. מסכת השפתיים מקבלת ערוץ אדום.
6. אזורי הלחיים מקבלים ערוץ ירוק עם feathering.
7. ה־bitmap מועבר ל־GL thread.
8. ה־renderer מעלה אותו לטקסטורת GL.
9. ה־shader משתמש במסכה כדי לחשב את אפקט האיפור.

### מניעת עבודה מיותרת

- נשמרים landmarks מהתוצאה הקודמת.
- אם השינוי קטן מסף מוגדר, לא נוצרת מסכה חדשה ולא מתבצע upload חדש.
- bitmap זמני ממוחזר לאחר העלאה ל־GL.

### מצב ללא פנים

כאשר Face Landmarker מחזיר תוצאה ללא פנים:

- landmarks קודמים נמחקים.
- מסכת האיפור מתנקה.
- טקסטורה שקופה מחליפה את המסכה הקודמת.
- לא נשאר אפקט ישן על פריים ללא פנים.

כאשר מסך המצלמה נסגר או מחליפים lens:

- המסכה מתנקה מיד.
- analyzer נסגר.
- CameraX מתנתק ומתחבר מחדש לפי הצורך.

---

## 7. Camera Overlay

### שם פנימי

`CameraOverlay`

### אזור AI ACTIVE / 4K RAW

בחלק העליון של אזור ה־overlay מוצג capsule עם:

- נקודה Coral.
- טקסט `AI ACTIVE`.
- טקסט `4K RAW`.

זהו חיווי ויזואלי. הוא אינו משנה את איכות הצילום או מפעיל מצב 4K אמיתי.

### Neural Focus Reticle

מוצג במרכז האזור לזמן מוגבל לאחר שינוי באחד מהפרמטרים המרכזיים.

ה־reticle כולל:

- glow רדיאלי.
- arc Coral המציין את חוזק ה־glow.
- טבעת חיצונית.
- טבעת מקווקוות.
- Aura logo קטן במרכז.
- טקסט `NEURAL FOCUS`.
- תיאור תאורה דינמי.

ה־reticle מוצג מחדש למשך כשתי שניות לאחר שינוי preset, zoom, flash או camera facing.

### Lighting status

הטקסט במרכז משתנה לפי luminance שנמדד מה־Y plane של המצלמה:

| טווח | טקסט |
|---|---|
| פחות מ־0.20 | `Low light - glow boosted` |
| 0.20 עד פחות מ־0.60 | `Balanced light` |
| 0.60 עד פחות מ־0.85 | `Bright - highlights softened` |
| 0.85 ומעלה | `Harsh light - smoothing adjusted` |

### Preserve natural texture

פקד תחתון באזור ה־overlay:

- אייקון AutoAwesome.
- טקסט `Preserve natural texture`.
- ערך `On` או `Off`.
- ניתן ללחוץ על כל השורה כדי לשנות את המצב.
- הפקד חושף semantics של Switch.

הערך משותף עם מסך Retouch. בפועל הוא משנה את עוצמת smoothing שמועברת ל־renderer, אך אינו מפעיל texture mask נפרד.

---

## 8. Camera Deck

### Presets

שורת presets אופקית וניתנת לגלילה:

- `Soft`
- `Radiant`
- `Velvet`
- `Defined`

הבחירה:

- משנה את ה־preset הנבחר.
- משנה את עוצמת ה־glow של ה־reticle.
- מציגה את ה־preset הנבחר בצבע Coral.
- מציגה אייקון AutoAwesome ליד הבחירה.

מיפוי עוצמות glow:

| Preset | Glow |
|---|---:|
| Soft | 0.68 |
| Radiant | 0.86 |
| Velvet | 0.34 |
| Defined | 0.57 |

ה־preset אינו משנה כרגע ישירות את פיקסלי תצוגת המצלמה מעבר להשפעה הקיימת של ה־renderer וה־glow state.

### Zoom selector

אפשרויות:

- `0.5x`
- `1x`
- `2x`
- `3x`

בלחיצה:

1. הערך נשמר כ־state.
2. הערך נבדק מול טווח ה־ZoomState של המצלמה הפעילה.
3. הערך נחתך לטווח הנתמך על ידי החומרה.
4. CameraX מקבל את ה־zoom ratio.

הערך הנבחר מוצג עם רקע Coral וטקסט כהה.

### Gallery button

- אייקון Photo Library.
- פותח את Android Image Picker עבור `image/*`.
- אינו מציג gallery grid פנימי.

### Capture button

- כפתור עגול מרכזי עם אייקון מצלמה.
- מפעיל צילום דרך `ImageCapture`.
- מוגן מפני double tap באמצעות atomic capture lock.
- יוצר קובץ JPEG ב־MediaStore.
- לאחר צילום מוצלח, ה־URI נשמר והודעת status מוצגת.

#### Android 10 ומעלה

- שימוש ב־scoped MediaStore storage.
- שמירה תחת `Pictures/Nyxai Glow`.
- שימוש ב־`IS_PENDING` במהלך הכתיבה.

#### Android 8 ו־9

- בקשת `WRITE_EXTERNAL_STORAGE` מתבצעת רק בעת ניסיון צילום.
- אם ההרשאה נדחית, הצילום לא מתחיל ומוצגת הודעת שגיאה.

### Flip camera button

- אייקון Flip Camera.
- מחליף בין front camera ל־rear camera.
- מבצע unbind ו־bind מחדש של CameraX.
- מתאים את ה־mirror של המסכה למצלמה הקדמית.
- בודק מחדש זמינות flash.

---

## 9. Bottom Navigation

### מיקום

בתחתית המסך, מעל אזור הניווט של Android.

### מבנה

- רקע כהה.
- ארבעה פריטי ניווט רגילים.
- כפתור Camera מרכזי מוגבה בצבע Coral.

### Gallery

- אייקון Photo Library.
- פתיחת Android Image Picker.
- שמירת ה־URI שנבחר ב־Compose state.
- הצגת הודעת `Photo selected`.
- אין מסך Gallery פנימי ואין רשימת תמונות בתוך האפליקציה.

### Looks

- אייקון AutoAwesome.
- משנה את ה־active tab ל־Retouch.
- מציג את Retouch Screen.

### Camera

- כפתור מרכזי גדול.
- מחזיר את ה־active tab ל־Camera.
- מציג מחדש את Camera Studio.

### Profile

- אייקון Person.
- כרגע אינו פותח פרופיל משתמש.
- פותח את Camera Settings Dialog.

### Selected state

הפריט הפעיל מוצג באמצעות:

- צבע Coral לאייקון.
- צבע Coral לטקסט.
- משקל טקסט מודגש יותר.

---

## 10. Retouch Screen

### שם פנימי

`RetouchScreen`

### איך מגיעים אליו

- לחיצה על `Looks` ב־Bottom Navigation.
- ה־state משתנה ל־`activeTab == "Retouch"`.

### מטרת המסך

מסך שליטה ב־retouch, preset, smoothing ו־micro-texture.

המסך כבר פונקציונלי ברמת state ואינטראקציה. חלק מהפקדים עדיין אינם מבצעים עיבוד תמונה מלא.

### מבנה כללי

- רקע כהה.
- padding עליון ותחתון כדי להשאיר מקום ל־top/bottom navigation.
- אזור preview/reticle מדומה.
- שורת כלי retouch.
- שורת presets.
- slider לעוצמת smoothing.
- toggle לשימור texture.
- כפתורי Reset ו־Apply.

### Header

#### AI CORE V2.4 ACTIVE

- חיווי טקסטואלי שהמערכת פעילה.
- אינו פקד.

#### HOLD BEFORE

- capsule סטטי בצד ימין.
- משמש כאלמנט עיצובי/חיווי.
- אינו מפעיל פעולה כרגע.

### Retouch preview area

- שטח כהה בגובה קבוע.
- reticle מעגלי עם glow ו־arc.
- טקסט `98.4% NATURAL MATCH`.
- ה־preview אינו מציג כרגע תמונת gallery או תמונה שצולמה.
- זהו אזור המחשה ויזואלי של retouch.

### כלי retouch

ארבעה כלים זמינים:

| כלי | אייקון | פעולה |
|---|---|---|
| Skin | AutoAwesome | משנה את `selectedTool` ל־Skin |
| Shape | PhotoLibrary | משנה את `selectedTool` ל־Shape |
| Light | FlashOn | משנה את `selectedTool` ל־Light |
| Makeup | Palette | משנה את `selectedTool` ל־Makeup |

הכלי שנבחר מקבל:

- רקע Coral שקוף יותר.
- אייקון Coral.
- טקסט Coral.
- semantics של selected.

בחירת כלי אינה מפעילה כרגע pipeline נפרד של עיבוד.

### Presets & Tone

שורת presets אופקית:

- `Smooth`
- `Freckles`
- `Matte`
- `Dewy`
- `Refine`

כל preset מוצג כאריח קטן עם:

- מלבן צבעוני המשמש כ־thumbnail עיצובי.
- שם ה־preset.
- מצב selected באמצעות צבע Coral ו־semantics.

בחירה משנה את `selectedPreset` בלבד.

### Smoothing intensity

- Slider בטווח `0.0..1.0`.
- ערך ברירת מחדל: `0.45`.
- שינוי slider מעדכן את `smoothingIntensity`.
- במסך המצלמה הערך משמש לחישוב `smoothStrength` של ה־renderer.
- כאשר Preserve natural texture פעיל, עוצמת smoothing מוכפלת ב־0.5 לפני העברתה ל־renderer.

### Subtle Micro-Texture

שורת toggle עם:

- כותרת: `Subtle Micro-Texture`.
- הסבר: `Preserves natural pores & grain`.
- ערך `ON` או `OFF`.

בלחיצה:

- הערך `preserveTexture` מתהפך.
- אותו state משתקף גם ב־Camera Overlay.
- הפקד חושף semantics של Switch.

כרגע אין texture mask נפרד או מודל TFLite שמייצר micro-texture בפועל.

### RESET

בלחיצה:

- מוחזר `RetouchState` לברירות המחדל:
  - Preserve texture: `true`.
  - Smoothing intensity: `0.45`.
  - Tool: `Skin`.
  - Preset: `Smooth`.
- מוצגת הודעת status: `Retouch reset`.

### APPLY TO PREVIEW

בלחיצה:

- מופעל callback של apply.
- מוצגת הודעת status בהתאם לשאלה אם קיים source image:
  - ללא source image: `No source image; retouch applied to live preview`.
  - עם source image: `Retouch applied to live preview; source image unchanged`.

הפעולה אינה שומרת תמונה ואינה משנה את קובץ המקור. היא מעדכנת state/status ומייצגת החלה על ה־live preview.

---

## 11. Android Image Picker

### איך נפתח

- לחיצה על Gallery ב־Camera Deck.
- לחיצה על Gallery ב־Bottom Navigation.

### סוג ה־picker

Android system picker דרך `GetContent` עם MIME type של `image/*`.

### לאחר בחירת תמונה

- ה־URI נשמר ב־`capturedUri`.
- מוצגת הודעת `Photo selected`.
- אין כרגע מסך פנימי להצגת התמונה שנבחרה.
- אין כרגע grid של תמונות, albums או עריכה על תמונת gallery.

### ביטול picker

אם המשתמש מבטל, אין שינוי ב־`capturedUri` ואין הודעת הצלחה.

---

## 12. Camera Settings Dialog

### איך נפתח

- לחיצה על Settings ב־Top Bar.
- לחיצה על Profile ב־Bottom Navigation.

### מבנה

- Dialog של Android Compose.
- רקע Mist.
- פינות מעוגלות.
- כותרת: `Camera settings`.
- טקסט מצב texture preservation.
- טקסט על התנהגות flash ו־zoom מול חומרת המצלמה.
- כפתור `Done`.

### תוכן דינמי

#### Texture preservation

מציג:

- `Texture preservation is on.`
- או `Texture preservation is off.`

#### Camera hardware

מציג:

`Flash and zoom follow the connected camera hardware.`

### Done

- סוגר את הדיאלוג.
- אינו משנה state נוסף.

### סגירה חיצונית

לחיצה מחוץ לדיאלוג או אירוע dismiss של Android סוגרים את הדיאלוג.

---

## 13. Status Message

### מיקום

בחלק העליון של התוכן, מתחת ל־Top Bar.

### התנהגות

- כאשר הערך הוא `Ready`, לא מוצגת הודעה.
- בכל מצב אחר מוצג capsule כהה עם טקסט לבן.

### הודעות קיימות

| הודעה | מתי מוצגת |
|---|---|
| `Face detected` | כאשר Face Landmarker מחזיר לפחות פנים אחת |
| `Photo captured` | לאחר צילום מוצלח |
| `Photo selected` | לאחר בחירת תמונה מה־picker |
| `Retouch reset` | לאחר Reset במסך Retouch |
| `No source image; retouch applied to live preview` | Apply ללא URI של תמונה |
| `Retouch applied to live preview; source image unchanged` | Apply כאשר קיים URI |
| `Camera unavailable: ...` | כאשר bind של CameraX נכשל |
| `Camera initialization failed: ...` | כאשר CameraProvider לא נטען |
| `Storage permission is required to save photos` | כאשר הרשאת storage נדרשת או נדחתה |
| `Could not prepare photo storage` | כאשר MediaStore לא מחזיר URI |
| `Capture failed` | כאשר צילום נכשל |
| הודעת analyzer/model | כאשר MediaPipe או המרת frame נכשלות |

---

## 14. מצבי שגיאה וחומרה

### מצלמה לא זמינה

- מוצגת הודעת status.
- אין כרגע מסך full-screen ייעודי.

### Flash לא זמין

- כפתור flash מושבת.
- אם flash היה פעיל, הוא מתאפס ל־off.

### Face Landmarker לא נטען

- מתבצע ניסיון GPU.
- לאחר כשל מתבצע ניסיון CPU.
- אם שניהם נכשלים, נשלחת הודעת שגיאה.
- המצלמה עדיין יכולה להציג preview, אך לא תיווצר מסכת פנים.

### אין פנים

- תצוגת המצלמה ממשיכה לעבוד.
- מסכת האיפור מתנקה.
- לא נשארת מסכה ישנה.

### אין מקום/כשל שמירה

- ה־URI הזמני נמחק במקרה של כשל.
- מוצגת הודעת status מתאימה.

---

## 15. State מרכזי בין המסכים

`GlowStudio` מחזיק את ה־state הבא:

| State | תפקיד |
|---|---|
| `glow` | עוצמת ה־glow של ה־preset וה־reticle |
| `ambient` | ערך התאורה שנמדד מהמצלמה |
| `preset` | preset של Camera Studio |
| `zoom` | ערך zoom שנבחר |
| `facing` | front או rear camera |
| `flashOn` | מצב torch מבוקש |
| `flashAvailable` | האם חומרת המצלמה תומכת ב־flash |
| `activeTab` | Camera או Retouch בפועל |
| `retouchState` | מצב כלי, preset, smoothing ו־texture |
| `reticleVisible` | האם reticle מוצג כרגע |
| `captureRequest` | counter שמפעיל capture |
| `captureLock` | מניעת צילום כפול |
| `capturedUri` | התמונה האחרונה שנבחרה או צולמה |
| `statusMessage` | הודעת feedback למשתמש |
| `showSettings` | פתיחת או סגירת settings dialog |

ה־state אינו נשמר כרגע לאחר סגירת process ואינו נמצא בתוך ViewModel.

---

## 16. יכולות שאינן מסכים נפרדים כרגע

הפריטים הבאים קיימים בממשק אך אינם destinations מלאים:

- Gallery - פותח Android picker בלבד.
- Profile - פותח Camera Settings Dialog בלבד.
- Preview של תמונה נבחרת - אינו קיים כמסך נפרד.
- Gallery grid - אינו קיים.
- מסך עריכת תמונה - אינו קיים.
- מסך שמירת תמונה עם אפקט - אינו קיים.

---

## 17. מגבלות נוכחיות

- אין Jetpack Navigation.
- אין ViewModel למסכי האפליקציה.
- Retouch controls מחוברים ל־state ול־live preview, אך לא כל preset או tool מפעיל אפקט פיקסלי עצמאי.
- אין שמירת תמונה עם מסכת האיפור כחלק מ־ImageCapture.
- אין skin mask נפרד.
- אין texture preservation model נפרד.
- אין gallery פנימית.
- אין profile משתמש.
- אין מסך dedicated לטיפול בשגיאות מצלמה.
- ה־`4K RAW` הוא חיווי UI ואינו מבטיח צילום 4K RAW.

---

## 18. בדיקות קיימות הקשורות למסכים

### RetouchScreenTest

מכסה:

- נראות כלי הריטוש וה־presets.
- בחירת כל preset.
- שינוי slider בטווח תקין.
- callback של texture toggle.
- עדכון semantics של tool ו־preset.
- callback של Reset ו־Apply.

### RetouchStateTest

מכסה:

- ערכי ברירת מחדל.
- Reset לערכים המקוריים.
- הודעת Apply עם ובלי source image.

### CameraVisualSafetyTest

מכסה:

- המרת קואורדינטות עבור rotations.
- mirror של מצלמה קדמית.
- clamp לקצוות.
- סינון landmarks לא זמינים.
- סף שינוי landmarks.
- פרמטרי renderer.
- idempotent release של renderer.

---

## 19. סיכום קצר לפי מסך

| מסך/חלון | קיים | תפקיד |
|---|---|---|
| Camera Permission | כן | קבלת הרשאת מצלמה |
| Camera Studio | כן | preview, face tracking, controls וצילום |
| Retouch / Looks | כן | כלי retouch ו־state של אפקטים |
| Android Image Picker | כן, חיצוני | בחירת תמונה מהמכשיר |
| Camera Settings Dialog | כן | חיווי והגדרות מצלמה בסיסיות |
| Status Message | כן, overlay | feedback קצר למשתמש |
| Gallery Screen פנימי | לא | טרם מומש |
| Profile Screen | לא | כרגע מפנה ל־Settings |
| Image Editing Screen | לא | טרם מומש |
