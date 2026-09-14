# Plan Działania — Ustawienie Logo / Ikony Aplikacji (AppIcon)

## 1. Przeczytane pliki i cel ich analizy

1. `AGENTS.md` — Zrozumienie wytycznych projektu (Java, struktura, przepływ pracy: plan -> akceptacja -> testy -> kod -> weryfikacja -> instrukcja testowania na fizycznym telefonie).
2. `app/src/main/AndroidManifest.xml` — Weryfikacja deklaracji ikony aplikacji: `android:icon="@mipmap/ic_launcher"` oraz `android:roundIcon="@mipmap/ic_launcher_round"`.
3. `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` oraz `ic_launcher_round.xml` — Analiza konfiguracji ikon adaptacyjnych (Adaptive Icons) wprowadzonych od Android 8.0 (API 26+).
4. Istniejące zasoby w `app/src/main/res/mipmap-*` — Sprawdzenie aktualnych gęstości ekranu (`mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`) oraz formatu plików (`.webp`).
5. `icon.png` — Sprawdzenie dostarczonego pliku graficznego (format PNG, rozdzielczość 1254x1254 px, przezroczyste tło / pełny kolor).

---

## 2. Konsultacja bibliotek

Operacja nie wymaga dodawania żadnych bibliotek zewnętrznych ani zależności Gradle. 
Do wygenerowania zestawu ikon we wszystkich wymaganych gęstościach Androida (`mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`, `anydpi-v26`) wykorzystamy natywne narzędzie systemowe macOS `sips` (Scriptable Image Processing System).

---

## 3. Co będziemy tworzyć i modyfikować

### Zasoby aplikacji:
1. **Generowanie plików mipmap dla ikon tradycyjnych i zaokrąglonych (Legacy & Round icons)**:
   - `mipmap-mdpi/ic_launcher.png` oraz `ic_launcher_round.png` (48x48 px)
   - `mipmap-hdpi/ic_launcher.png` oraz `ic_launcher_round.png` (72x72 px)
   - `mipmap-xhdpi/ic_launcher.png` oraz `ic_launcher_round.png` (96x96 px)
   - `mipmap-xxhdpi/ic_launcher.png` oraz `ic_launcher_round.png` (144x144 px)
   - `mipmap-xxxhdpi/ic_launcher.png` oraz `ic_launcher_round.png` (192x192 px)
   - Zastąpienie/usunięcie starych domyślnych plików `ic_launcher.webp` i `ic_launcher_round.webp`, aby uniknąć konfliktów nazw zasobów.

2. **Obsługa ikon adaptacyjnych (Adaptive Icons, API 26+)**:
   - Przygotowanie grafiki pierwszego planu (foreground) z zachowaniem bezpiecznej strefy (safe zone 66/108 = ok. 61% centralnego obszaru, aby ikona nie została ucięta w okręgu, squirclu lub zaokrąglonym kwadracie launchera):
     - `mipmap-mdpi/ic_launcher_foreground.png` (108x108 px)
     - `mipmap-hdpi/ic_launcher_foreground.png` (162x162 px)
     - `mipmap-xhdpi/ic_launcher_foreground.png` (216x216 px)
     - `mipmap-xxhdpi/ic_launcher_foreground.png` (324x324 px)
     - `mipmap-xxxhdpi/ic_launcher_foreground.png` (432x432 px)
   - Dostosowanie `mipmap-anydpi-v26/ic_launcher.xml` i `ic_launcher_round.xml` do wskazywania na tło i foreground.
   - Kolor tła dopasowany do stylistyki ikony w `res/values/colors.xml` lub `res/drawable/ic_launcher_background.xml`.

3. **Przeniesienie / organizacja pliku źródłowego**:
   - Przeniesienie pliku `icon.png` do dedykowanego katalogu projektowego lub zachowanie kopii wysokiej rozdzielczości w `app/src/main/res/drawable-xxxhdpi/` lub `app/src/main/ic_launcher-web.png` (standardowy 512x512 dla Google Play Store).
   - Usunięcie luźnego pliku `icon.png` z głównego katalogu repozytorium.

### Testy:
1. `app/src/test/java/com/example/cryptogay/AppIconTest.java` — Test jednostkowy weryfikujący poprawność konfiguracji manifestu, istnienie wszystkich wymaganych zasobów ikon launcher (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi, anydpi-v26) oraz ich obecność w plikach wynikowych.

---

## 4. Kroki realizacji (Krok po kroku)

### Faza 1: Przygotowanie testu weryfikacyjnego (TDD)
- [x] 1.1 Napisanie testu `AppIconTest.java` sprawdzającego obecność zasobów ikon launcher we wszystkich wymaganych gęstościach.
- [x] 1.2 Uruchomienie testu i potwierdzenie aktualnego stanu.

### Faza 2: Generowanie i umieszczenie ikon w strukturze projektu
- [x] 2.1 Przygotowanie tła i bezpiecznej strefy foreground dla Adaptive Icons (Android 8.0+).
- [x] 2.2 Wygenerowanie ikon standardowych i okrągłych dla wszystkich rozdzielczości (`mdpi` 48px, `hdpi` 72px, `xhdpi` 96px, `xxhdpi` 144px, `xxxhdpi` 192px).
- [x] 2.3 Wygenerowanie warstw foreground dla ikon adaptacyjnych (`mdpi` 108px, `hdpi` 162px, `xhdpi` 216px, `xxhdpi` 324px, `xxxhdpi` 432px).
- [x] 2.4 Zaktualizowanie `res/mipmap-anydpi-v26/ic_launcher.xml` i `ic_launcher_round.xml` oraz koloru tła.
- [x] 2.5 Usunięcie starych plików `.webp` domyślnej ikony Androida.
- [x] 2.6 Usunięcie pliku `icon.png` z głównego katalogu projektu (przeniesienie do zasobów).

### Faza 3: Weryfikacja i Definition of Done
- [x] 3.1 Uruchomienie testów jednostkowych: `./gradlew testDebugUnitTest`.
- [x] 3.2 Sprawdzenie kompilacji: `./gradlew build` / `./gradlew assembleDebug`.
- [x] 3.3 Sprawdzenie lintera: `./gradlew lintDebug`.
- [x] 3.4 Przejrzenie planu pod kątem kompletności.
- [x] 3.5 Przygotowanie instrukcji weryfikacji na telefonie użytkownika.
