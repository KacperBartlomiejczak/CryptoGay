# Plan Działania — Ekran Market (Lista kryptowalut + Wyszukiwarka)

## 1. Przeczytane pliki i cel ich analizy

1. `AGENTS.md` — Zbiór reguł projektu, stos technologiczny (Java, MVVM, Room, Retrofit, LiveData, ViewBinding), konwencja nazewnictwa i struktury pakietów oraz kroki procesu pracy (plan -> testy -> implementacja -> weryfikacja).
2. `app/build.gradle.kts` i `gradle/libs.versions.toml` — Sprawdzenie aktualnych zależności, wersji SDK (minSdk 24, targetSdk 37), wersji Javy (Java 11) oraz zidentyfikowanie brakujących bibliotek (Retrofit, Gson, Lifecycle LiveData/ViewModel, Navigation Component, ViewBinding).
3. `AndroidManifest.xml` — Weryfikacja uprawnień aplikacji (konieczne dodanie `android.permission.INTERNET`) oraz konfiguracji `MainActivity`.
4. `app/src/main/java/com/example/cryptogay/MainActivity.java` — Analiza aktualnego punktu wejścia aplikacji (obecnie domyślny szablon Android Studio).
5. `app/src/main/res/layout/activity_main.xml` — Szablon widoku głównego (obecnie pojedynczy TextView "Hello World!").
6. `app/src/main/res/values/strings.xml`, `colors.xml`, `themes.xml` — Sprawdzenie istniejących zasobów stylów, motywu Material3 oraz przygotowanie do braku zahardkodowanych stringów.

---

## 2. Konsultacja bibliotek (Zgodnie z zasadą: "Nie dodawaj bibliotek spoza wymienionego stacku bez wcześniejszej konsultacji z użytkownikiem")

Do realizacji ekranu Market potrzebujemy bibliotek z zadeklarowanego stacku oraz chcemy skonsultować biblioteki pomocnicze:
- **Zadeklarowane w AGENTS.md:**
  - `Retrofit2` (`com.squareup.retrofit2:retrofit`) + konwerter `converter-gson`
  - `OkHttp3` (`com.squareup.okhttp3:logging-interceptor`)
  - `Lifecycle ViewModel & LiveData` (`androidx.lifecycle:lifecycle-viewmodel`, `androidx.lifecycle:lifecycle-livedata`)
  - `Navigation Component` (`androidx.navigation:navigation-fragment`, `androidx.navigation:navigation-ui`)
  - `ViewBinding` (wbudowane w Android Gradle Plugin: `buildFeatures { viewBinding = true }`)
- **Konsultacja / Propozycja bibliotek pomocniczych:**
  - **`Glide`** (`com.github.bumptech.glide:glide:4.16.0`): CoinGecko API zwraca adres URL do ikon kryptowalut (`image`). W czystej Javie bez biblioteki ładowanie asynchroniczne grafik z cache'owaniem wymagałoby pisania własnego cache'a i wątków w tle. Glide jest standardem w projektach Android Java.
  - **`Mockito`** (`org.mockito:mockito-core:5.11.0`) & **`androidx.arch.core:core-testing:2.2.0`**: Do pisania izolowanych testów jednostkowych dla ViewModel i Repozytorium (obsługa `InstantTaskExecutorRule` dla LiveData).

---

## 3. Co będziemy tworzyć

### Struktura pakietów (w `com.example.cryptogay`):
```
com.example.cryptogay
├── data
│   ├── model
│   │   └── Coin.java               // Model domeny / DTO dla kryptowaluty
│   ├── remote
│   │   ├── CoinGeckoApiService.java // Interfejs Retrofit (/coins/markets)
│   │   └── ApiClient.java          // Singleton / dostawca instancji Retrofit
│   └── repository
│       └── CoinRepository.java     // Repozytorium pobierające dane z API + filtrowanie
├── ui
│   └── market
│       ├── MarketFragment.java     // Ekran listy + search input
│       ├── MarketViewModel.java    // ViewModel zarządzający stanem UI i filtrowaniem
│       ├── MarketUiState.java      // Reprezentacja stanów: Loading, Success, Error, Empty
│       └── CryptoAdapter.java      // RecyclerView Adapter z DiffUtil
└── util
    └── CurrencyFormatter.java      // Formater cen USD oraz zmian procentowych 24h
```

### Warstwa UI (XML):
- `activity_main.xml` — `FragmentContainerView` dla Jetpack Navigation + `BottomNavigationView`.
- `navigation/nav_graph.xml` — Graf nawigacji z `marketFragment` jako startDestination.
- `fragment_market.xml`:
  - Pole wyszukiwania (`SearchView` / `TextInputLayout` z `TextInputEditText`)
  - `SwipeRefreshLayout` (do odświeżania gestem pull-to-refresh)
  - `RecyclerView` z listą monet
  - `ProgressBar` (stan ładowania)
  - Layout błędu (ikona + tekst + przycisk ponów / Retry)
  - Layout pustego stanu ("Brak wyników wyszukiwania")
- `item_crypto.xml` — Wiersz kryptowaluty:
  - Ikona (ImageView)
  - Nazwa i symbol (np. "Bitcoin", "BTC")
  - Aktualna cena (np. "$64,250.00")
  - Zmiana 24h (np. "+2.45%" zielony / "-1.12%" czerwony)

---

## 4. Kroki realizacji (Krok po kroku)

### Faza 1: Przygotowanie środowiska i konfiguracji
- [x] 1.1 Aktualizacja `gradle/libs.versions.toml` i `app/build.gradle.kts` o wymagane zależności (Retrofit, Gson, Navigation, Lifecycle, ViewBinding, testy, Glide).
- [x] 1.2 Dodanie uprawnienia `android.permission.INTERNET` do `AndroidManifest.xml`.
- [x] 1.3 Definicja stringów i kolorów w `strings.xml` i `colors.xml` (zielony dla zysków, czerwony dla spadków, teksty stanów).

### Faza 2: Szkielet UI i Nawigacji (Zgodnie z zasadą: "najpierw layout XML + minimalny szkielet")
- [x] 2.1 Przygotowanie menu `bottom_nav_menu.xml` oraz grafu nawigacji `nav_graph.xml`.
- [x] 2.2 Przebudowa `activity_main.xml` (NavHostFragment + BottomNavigationView) i spięcie w `MainActivity.java`.
- [x] 2.3 Utworzenie `item_crypto.xml`.
- [x] 2.4 Utworzenie `fragment_market.xml` ze wszystkimi stanami (loading, error, empty, content).
- [x] 2.5 Minimalny szkielet `MarketFragment.java` z ViewBinding.

### Faza 3: Testy Jednostkowe (Zgodnie z zasadą: "1) Piszesz testy zanim ruszysz kod")
- [x] 3.1 Napisanie testów jednostkowych dla `CoinRepositoryTest` (parsowanie danych, scenariusze błędu sieciowego).
- [x] 3.2 Napisanie testów jednostkowych dla `MarketViewModelTest` (emisja stanu Loading, Success, Error, poprawne filtrowanie po nazwie i symbolu).
- [x] 3.3 Uruchomienie testów (potwierdzenie, że nie przechodzą przed implementacją).

### Faza 4: Implementacja logiki i warstwy danych
- [x] 4.1 Implementacja modelu `Coin.java` z adnotacjami Gson.
- [x] 4.2 Implementacja `CoinGeckoApiService.java` oraz `ApiClient.java`.
- [x] 4.3 Implementacja `CoinRepository.java`.
- [x] 4.4 Implementacja `CurrencyFormatter.java` (formatowanie walut i kolorowanie zmian).
- [x] 4.5 Implementacja `MarketUiState.java` oraz `MarketViewModel.java`.
- [x] 4.6 Implementacja `CryptoAdapter.java` z DiffUtil.
- [x] 4.7 Podłączenie `MarketFragment.java`:
  - Obserwacja LiveData z ViewModel
  - Obsługa pola wyszukiwania z natychmiastowym filtrowaniem
  - Obsługa przycisku Retry oraz SwipeRefreshLayout
  - Przełączanie widoczności widoków (Loading / Content / Error / Empty)

### Faza 5: Weryfikacja i kryteria Definition of Done
- [x] 5.1 Uruchomienie testów jednostkowych: `./gradlew testDebugUnitTest` (wszystkie muszą przechodzić).
- [x] 5.2 Uruchomienie `./gradlew lint` i `./gradlew build`.
- [x] 5.3 Sprawdzenie braku zahardkodowanych stringów w kodzie Java/XML.
- [x] 5.4 Przygotowanie instrukcji dla użytkownika jak przetestować ekran na fizycznym telefonie.
