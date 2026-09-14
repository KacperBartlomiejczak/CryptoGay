# 🪙 CryptoTracker (CryptoGay) — Aplikacja Android

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com/)
[![Language](https://img.shields.io/badge/Language-Java%2011-orange.svg)](https://www.oracle.com/java/)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM-blue.svg)](https://developer.android.com/topic/architecture)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24%20(Android%207.0)-informational.svg)](https://developer.android.com/about/versions/nougat)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-37-informational.svg)](https://developer.android.com/)
[![Database](https://img.shields.io/badge/Database-Room%20(SQLite)-brightgreen.svg)](https://developer.android.com/training/data-storage/room)
[![Network](https://img.shields.io/badge/Network-Retrofit2%20%2B%20OkHttp3-purple.svg)](https://square.github.io/retrofit/)

Aplikacja mobilna na platformę **Android** napisana w całości w języku **Java**, służąca do śledzenia rynków kryptowalut w czasie rzeczywistym, zarządzania listą ulubionych monet oraz definiowania alertów progów cenowych z powiadomieniami push w tle.

---

## 📋 Spis treści

- [Główne Funkcjonalności](#-główne-funkcjonalności)
- [Architektura Aplikacji](#-architektura-aplikacji)
- [Struktura Projektu](#-struktura-projektu)
- [Stos Technologiczny i Zależności](#-stos-technologiczny-i-zależności)
- [Wymagania Systemowe](#-wymagania-systemowe)
- [Instrukcja Uruchomienia](#-instrukcja-uruchomienia)
  - [Opcja 1: Uruchomienie w Android Studio (Zalecane)](#opcja-1-uruchomienie-w-android-studio-zalecane)
  - [Opcja 2: Uruchomienie z poziomu konsoli (Gradle / CLI)](#opcja-2-uruchomienie-z-poziomu-konsoli-gradle--cli)
- [Weryfikacja i Testy](#-weryfikacja-i-testy)
- [Jak Przetestować Funkcjonalności w Aplikacji](#-jak-przetestować-funkcjonalności-w-aplikacji)
- [Integracja z API (CoinGecko)](#-integracja-z-api-coingecko)

---

## 🚀 Główne Funkcjonalności

Aplikacja oparta jest o architekturę Single Activity z dolnym paskiem nawigacji (**BottomNavigationView**) oraz dedykowanym ekranem szczegółów:

### 1. 📈 Rynek (Market)
- Pobieranie listy 100 najpopularniejszych kryptowalut według kapitalizacji rynkowej z API CoinGecko.
- Podgląd aktualnego kursu w USD, 24-godzinnej zmiany procentowej (dynamiczna kolorystyka: zielony/czerwony) oraz kapitalizacji rynkowej.
- Wyszukiwanie w czasie rzeczywistym (`SearchView`) filtrujące monety po pełnej nazwie lub symbolu (np. `BTC`, `Bitcoin`).
- Obsługa gestu **Pull-to-Refresh** (`SwipeRefreshLayout`) do ręcznego odświeżania listy.
- Szybkie dodawanie/usuwanie z ulubionych bezpośrednio z poziomu kafelka listy.

### 2. ⭐ Ulubione (Favorites)
- Podzbiór kryptowalut oznaczonych przez użytkownika gwiazdką.
- Zapis do lokalnej bazy danych **Room (SQLite)** — dane są trwałe i przetrwają zamknięcie lub restart aplikacji oraz brak połączenia z siecią.
- Automatyczna synchronizacja stanu ulubionych w czasie rzeczywistym między wszystkimi ekranami (dzięki reaktywnym strumieniom `LiveData`).
- Przycisk szybkiego przejścia do Rynku, gdy lista ulubionych jest pusta.

### 3. 📊 Szczegóły Kryptowaluty (Details)
- Dedykowany ekran otwierany po kliknięciu wybranej monety z listy Rynku, Ulubionych lub Alerty (z automatycznym ukrywaniem paska dolnej nawigacji).
- Interaktywny wykres liniowy (**MPAndroidChart**) prezentujący historię cen.
- Przełącznik przedziałów czasowych wykresu: **24H**, **7D**, **30D**, **1Y**.
- Szczegółowe metryki: cena bieżąca, 24-godzinne minimum i maksimum (Low / High), kapitalizacja rynkowa, 24-godzinny wolumen obrotu.
- Przycisk akcji pozwalający na dodanie/usunięcie monety z ulubionych oraz skonfigurowanie progu cenowego (alertu).

### 4. 🔔 Alerty Cenowe i Powiadomienia w Tle (Price Alerts & Background Worker)
- Możliwość ustawienia progu cenowego w USD:
  - Próg **Powyżej** (Wzrost powyżej ceny X)
  - Próg **Poniżej** (Spadek poniżej ceny X)
- Zintegrowany ekran zarządzania wszystkimi zdefiniowanymi alertami (przeglądanie, usuwanie, przejście do szczegółów monety).
- Tło i zadania cykliczne z wykorzystaniem **AndroidX WorkManager** (`PriceAlertWorker` uruchamiany w tle co 15 minut).
- Systemowe powiadomienia push (`NotificationCompat` + dedykowany `NotificationChannel`) po przekroczeniu zadanego kursu.
- **Deep Linking**: kliknięcie powiadomienia w systemie otwiera aplikację bezpośrednio na ekranie szczegółów danej kryptowaluty.

### 5. 🛡️ Spójna Obsługa Stanów UI
Każdy ekran obsługuje pełen cykl życia i 4 stany interfejsu użytkownika:
- **Ładowanie (Loading)**: widoczny wskaźnik postępu (`ProgressBar`).
- **Sukces (Success)**: poprawnie wyrenderowana lista elementów lub wykres.
- **Pusty (Empty)**: estetyczny widok informacyjny z wezwaniem do działania (np. brak wyników wyszukiwania, brak ulubionych monet, brak alertów).
- **Błąd (Error)**: czytelny komunikat błędu (np. brak internetu, limit API) wraz z przyciskiem **„Spróbuj ponownie”**.

---

## 🏗️ Architektura Aplikacji

Projekt został zaprojektowany zgodnie z oficjalnymi zaleceniami Google dla aplikacji na platformę Android, w oparciu o czysty wzorzec **MVVM (Model-View-ViewModel)**:

```
                      +-----------------------------+
                      |       Activity / Fragment   |
                      |   (ViewBinding, XML UI)     |
                      +--------------+--------------+
                                     | Obserwuje LiveData
                                     v
                      +-----------------------------+
                      |          ViewModel          |
                      |  (Utrzymuje stan UiState)   |
                      +--------------+--------------+
                                     |
                                     v
                      +-----------------------------+
                      |         Repository          |
                      |  (Single Source of Truth)   |
                      +--------------+--------------+
                                     |
                   +-----------------+-----------------+
                   |                                   |
                   v                                   v
+-----------------------------------+ +-----------------------------------+
|            Remote API             | |           Local Database          |
|    Retrofit2 + OkHttp3 + Gson     | |          Room ORM (SQLite)        |
|          (CoinGecko)              | |     (Favorites, Alerts, Cache)    |
+-----------------------------------+ +-----------------------------------+
```

### Kluczowe komponenty architektoniczne:
- **View Binding**: Bezpieczne typowo i wolne od błędów `NullPointerException` wiązanie widoków XML z kodem Java.
- **LiveData & ViewModel**: Przetrwanie zmian konfiguracji (np. obrót ekranu) oraz bezwyciekowa komunikacja View <-> ViewModel.
- **Single Source of Truth (Repozytoria)**: Separacja logiki danych od warstwy prezentacji (`CoinRepository`, `FavoritesRepository`, `AlertsRepository`).
- **Jetpack Navigation Component**: Scentralizowany graf nawigacji (`nav_graph.xml`) z obsługą przejść i przekazywania argumentów (`SafeArgs` / `Bundle`).

---

## 📁 Struktura Projektu

```
com.example.cryptogay
├── MainActivity.java                // Główna aktywność aplikacji, obsługa BottomNav i DeepLinków
├── data
│   ├── local                        // Baza danych Room (SQLite)
│   │   ├── AppDatabase.java         // Definicja bazy danych Room (singleton)
│   │   ├── FavoriteCoin.java        // Encja ulubionej monety
│   │   ├── FavoriteCoinDao.java     // DAO operacji na ulubionych
│   │   ├── PriceAlert.java          // Encja progu cenowego (alertu)
│   │   ├── PriceAlertDao.java       // DAO operacji na alertach
│   │   ├── CachedCoin.java          // Encja pamięci podręcznej (offline cache)
│   │   └── CachedCoinDao.java       // DAO pamięci podręcznej
│   ├── model                        // Modele danych / POJO
│   │   ├── Coin.java                // Główny model kryptowaluty
│   │   └── MarketChartResponse.java // Model odpowiedzi wykresu cenowego
│   ├── remote                       // Komunikacja sieciowa
│   │   ├── ApiClient.java           // Konfiguracja Retrofit, OkHttp i interceptorów
│   │   └── CoinGeckoApiService.java // Interfejs zapytań HTTP do API CoinGecko
│   └── repository                   // Warstwa dostępu do danych
│       ├── CoinRepository.java      // Pobieranie rynku, szczegółów i wykresów
│       ├── FavoritesRepository.java // Zarządzanie ulubionymi w Room
│       └── AlertsRepository.java    // Zarządzanie alertami w Room
├── notification
│   └── AlertNotificationHelper.java // Tworzenie kanału powiadomień i wysyłka alertów push
├── ui                               // Warstwa prezentacji (MVVM)
│   ├── market                       // Ekran 1: Rynek i wyszukiwarka
│   │   ├── MarketFragment.java
│   │   ├── MarketViewModel.java
│   │   ├── MarketUiState.java
│   │   └── CryptoAdapter.java
│   ├── favorites                    // Ekran 2: Ulubione kryptowaluty
│   │   ├── FavoritesFragment.java
│   │   ├── FavoritesViewModel.java
│   │   └── FavoritesUiState.java
│   ├── alerts                       // Ekran 3: Alerty cenowe
│   │   ├── AlertsFragment.java
│   │   ├── AlertsViewModel.java
│   │   ├── AlertsUiState.java
│   │   └── AlertsAdapter.java
│   └── details                      // Ekran 4: Szczegóły kryptowaluty i wykres
│       ├── DetailsFragment.java
│       ├── DetailsViewModel.java
│       └── DetailsUiState.java
├── util                             // Narzędzia pomocnicze
│   ├── CurrencyFormatter.java       // Bezpieczne formatowanie walut (USD) i liczb
│   └── NavigationHelper.java        // Logika widoczności pasków nawigacji
└── worker                           // Zadania w tle
    └── PriceAlertWorker.java        // Cykliczny Worker sprawdzający ceny w tle
```

---

## 🛠️ Stos Technologiczny i Zależności

| Komponent | Biblioteka / Narzędzie | Zastosowanie |
|---|---|---|
| **Język programowania** | Java 11 | Zgodnie z wytycznymi projektu |
| **Minimalny SDK** | Android 7.0 (API 24) | Szeroka kompatybilność z urządzeniami |
| **Docelowy SDK** | Android 14+ (API 37) | Najnowsze standardy bezpieczeństwa |
| **Baza danych** | Room Persistence Library `2.6.1` | Lokalna baza SQLite na ulubione i alerty |
| **Sieć HTTP** | Retrofit2 `2.9.0` + OkHttp3 `4.12.0` | Klient REST API z konwerterem Gson |
| **Zadania w tle** | AndroidX WorkManager `2.9.0` | Periodyczne sprawdzanie kursów w tle |
| **Architektura** | AndroidX Lifecycle (ViewModel & LiveData) | Reaktywne zarządzanie stanem UI |
| **Nawigacja** | Jetpack Navigation Component `2.7.7` | Zarządzanie grafem i stosem fragmentów |
| **Wykresy** | MPAndroidChart `v3.1.0` | Interaktywny wykres liniowy historii cen |
| **Ładowanie obrazów**| Glide `4.16.0` | Asynchroniczne pobieranie i buforowanie ikon |
| **Testy jednostkowe** | JUnit4, Mockito, AndroidX Core Testing | Pokrycie logiki ViewModel i Repository |

---

## 💻 Wymagania Systemowe

Przed uruchomieniem upewnij się, że posiadasz:
- **System operacyjny:** macOS, Linux lub Windows.
- **Środowisko:** [Android Studio](https://developer.android.com/studio) (wersja Iguana, Jellyfish, Koala, Ladybug lub nowsza).
- **Java Development Kit (JDK):** JDK 11 lub JDK 17 (dostarczane domyślnie z Android Studio).
- **Android SDK:** Zainstalowane API 24-34/37 oraz Android SDK Build-Tools.
- **Urządzenie:** Fizyczny smartfon z systemem Android 7.0+ (włączone debugowanie USB) LUB skonfigurowany Emulator Android Virtual Device (AVD).

---

## 🚀 Instrukcja Uruchomienia

### Opcja 1: Uruchomienie w Android Studio (Zalecane)

1. **Sklonuj repozytorium** lub otwórz katalog projektu na dysku:
   ```bash
   git clone <url-repozytorium>
   ```
2. **Uruchom Android Studio** i wybierz opcję **Open...**, a następnie wskaż katalog projektu:
   ```
   /sciezka/do/projektu/Cryptogay
   ```
3. Poczekaj na zakończenie synchronizacji Gradle (**Gradle Sync**). Android Studio automatycznie pobierze wymagane biblioteki.
4. Podłącz fizyczny telefon kablem USB (lub uruchom Emulator z poziomu *Device Manager*).
5. W górnym pasku narzędzi wybierz urządzenie docelowe i kliknij zielony przycisk **Run** (skrót: `Shift + F10` lub `Ctrl + R` na macOS).

---

### Opcja 2: Uruchomienie z poziomu konsoli (Gradle / CLI)

Jeśli wolisz pracę w terminalu, możesz skompilować i zainstalować aplikację bezpośrednio za pomocą wrappera Gradle:

1. **Przejdź do katalogu głównego projektu:**
   ```bash
   cd /sciezka/do/projektu/Cryptogay
   ```

2. **Nadaj uprawnienia do wykonywania dla gradlew (macOS / Linux):**
   ```bash
   chmod +x gradlew
   ```

3. **Zbuduj wersję Debug:**
   ```bash
   ./gradlew assembleDebug
   ```
   *(Plik APK zostanie wygenerowany w lokalizacji: `app/build/outputs/apk/debug/app-debug.apk`)*.

4. **Upewnij się, że urządzenie jest widoczne przez ADB:**
   ```bash
   adb devices
   ```

5. **Zainstaluj aplikację na podłączonym urządzeniu:**
   ```bash
   ./gradlew installDebug
   ```

6. **Uruchom aplikację na telefonie przez ADB:**
   ```bash
   adb shell am start -n com.example.cryptogay/.MainActivity
   ```

---

## 🧪 Weryfikacja i Testy

Projekt posiada zestaw testów jednostkowych pokrywających logikę repozytoriów, mapperów, formaterów walut oraz stanów ViewModeli (z użyciem JUnit4, Mockito oraz InstantTaskExecutorRule).

Aby uruchomić testy jednostkowe:

```bash
./gradlew testDebugUnitTest
```

Raport z wykonania testów w formacie HTML znajduje się w:
```
app/build/reports/tests/testDebugUnitTest/index.html
```

Aby uruchomić statyczną analizę kodu (Android Lint):
```bash
./gradlew lint
```

---

## 📱 Jak Przetestować Funkcjonalności w Aplikacji

Po uruchomieniu aplikacji na telefonie lub emulatorze możesz przetestować poszczególne scenariusze:

1. **Przeglądanie i Wyszukiwanie:**
   - Na ekranie głównym zobaczysz listę kryptowalut (Bitcoin, Ethereum itp.).
   - Wpisz w pasek wyszukiwania np. `Solana` lub `sol` — lista natychmiast przefiltruje pozycje.
   - Wpisz ciąg nieistniejący (np. `xyz123`) — aplikacja pokaże stan **Empty** z informacją o braku wyników.
   - Pociągnij listę w dół — aktywuje się animacja **Swipe-to-Refresh** i nastąpi odświeżenie danych.

2. **Ulubione:**
   - Kliknij ikonę gwiazdki przy wybranej kryptowalucie na liście.
   - Przejdź do zakładki **Ulubione** w dolnym menu — dodana moneta znajduje się na liście.
   - Zamknij aplikację (zabij proces) i uruchom ją ponownie — ulubione pozostają zapisane w bazie Room.

3. **Szczegóły i Wykres:**
   - Kliknij w wiersz dowolnej monety — nastąpi płynne przejście do ekranu szczegółów, a dolny pasek nawigacji schowa się automatycznie.
   - Zobacz interaktywny wykres liniowy. Przełączaj filtry czasowe: `24H`, `7D`, `30D`, `1Y` — wykres zaktualizuje się dynamicznie.

4. **Alerty Cenowe i Powiadomienia w Tle:**
   - Na ekranie szczegółów kliknij przycisk **„Ustaw alert cenowy”**.
   - Wpisz interesującą Cię cenę w USD oraz wybierz warunek (*Wzrost powyżej* lub *Spadek poniżej*), a następnie zatwierdź.
   - Przejdź do zakładki **Alerty** w dolnym menu — Twój alert jest widoczny z aktualnym statusem.
   - W tle systemowy `PriceAlertWorker` co 15 minut monitoruje kursy walut. Po spełnieniu warunku wyśle powiadomienie na pasek systemowy telefonu.
   - Kliknięcie w powiadomienie otworzy aplikację od razu na ekranie wybranej monety!

---

## 🌐 Integracja z API (CoinGecko)

Aplikacja integruje się z publicznym, darmowym REST API **CoinGecko**:
- `GET /coins/markets?vs_currency=usd` — pobieranie listy top kryptowalut.
- `GET /coins/{id}` — pobieranie szczegółowych informacji o pojedynczej monecie.
- `GET /coins/{id}/market_chart?vs_currency=usd&days={days}` — dane historyczne do rysowania wykresów.

> [!NOTE]
> Publiczne darmowe API CoinGecko posiada limit zapytań (około 10–30 req/min). Aplikacja obsługuje kody błędów sieciowych (w tym HTTP 429 Too Many Requests) oraz oferuje mechanizm pamięci podręcznej i buforowania w bazie SQLite.
