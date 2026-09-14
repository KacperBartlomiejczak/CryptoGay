# Plan Działania — Ekran Szczegółów (Details: Tytuł + Ulubione + Wykres + Informacje)

## 1. Przeczytane pliki i cel ich analizy

1. `AGENTS.md` — Główne reguły projektu, stos technologiczny (Java, MVVM, Room, MPAndroidChart, Retrofit, LiveData, ViewBinding), Definition of Done (brak zahardkodowanych stringów, testy przed kodem, obsługa stanów loading/error/empty).
2. `prompts/Market/plan.md` — Punkt odniesienia dla struktury planu, sprawdzonych wzorców projektowych i standardów w projekcie.
3. `app/build.gradle.kts` i `gradle/libs.versions.toml` — Sprawdzenie aktualnych bibliotek i wersji; identyfikacja potrzeby dodania:
   - `MPAndroidChart` (`com.github.PhilJay:MPAndroidChart:v3.1.0`) do rysowania wykresów cenowych,
   - `Room` (`androidx.room:room-runtime:2.6.1` i `room-compiler:2.6.1`) do trwałego zapisu ulubionych kryptowalut w lokalnej bazie SQLite.
4. `settings.gradle.kts` — Analiza konfiguracji repozytoriów (`dependencyResolutionManagement`); konieczność dopisania repozytorium JitPack (`https://jitpack.io`) wymaganego przez bibliotekę `MPAndroidChart`.
5. `app/src/main/res/navigation/nav_graph.xml` — Analiza konfiguracji Jetpack Navigation; przygotowanie akcji nawigacji `action_marketFragment_to_detailsFragment` z przekazywaniem argumentu `coinId`.
6. `app/src/main/java/com/example/cryptogay/ui/market/MarketFragment.java` i `CryptoAdapter.java` — Analiza miejsca wywołania nawigacji po kliknięciu na wiersz danej kryptowaluty.
7. `app/src/main/java/com/example/cryptogay/ui/details/DetailsFragment.java` — Analiza obecnego, tymczasowego szkieletu ekranu szczegółów.
8. `app/src/main/java/com/example/cryptogay/data/model/Coin.java` — Analiza istniejących pól modelu i rozszerzenie o dodatkowe statystyki rynkowe zwracane przez CoinGecko (ATH, 24h High, 24h Low, Market Cap, Total Volume, Circulating Supply, Total Supply).
9. `app/src/main/java/com/example/cryptogay/data/remote/CoinGeckoApiService.java` i `CoinRepository.java` — Weryfikacja endpointów; dodanie zapytania o historię cen `/api/v3/coins/{id}/market_chart` oraz szczegółowe dane pojedynczego coina `/api/v3/coins/markets?ids={id}`.
10. `app/src/main/res/values/strings.xml` i `colors.xml` — Weryfikacja zasobów tekstowych i kolorystycznych; przygotowanie etykiet dla statystyk rynkowych, stanów błędów/ładowania oraz opisów dostępności (contentDescription).

---

## 2. Konsultacja bibliotek (Zgodnie z zasadą: "Nie dodawaj bibliotek spoza wymienionego stacku bez wcześniejszej konsultacji z użytkownikiem")

Wszystkie potrzebne biblioteki znajdują się wprost w zadeklarowanym stosie technologicznym w `AGENTS.md`:
- **Zadeklarowane w AGENTS.md:**
  - `Wykres: MPAndroidChart` (`com.github.PhilJay:MPAndroidChart:v3.1.0`)
  - `Baza lokalna: Room (przechowuje ulubione oraz ustawione thresholdy)` (`androidx.room:room-runtime:2.6.1`, `androidx.room:room-compiler:2.6.1`)
- **Konfiguracja repozytoriów:**
  - Dodanie `maven { url = uri("https://jitpack.io") }` w `settings.gradle.kts` dla pobrania `MPAndroidChart`.

---

## 3. Co będziemy tworzyć

### Struktura pakietów (w `com.example.cryptogay`):
```
com.example.cryptogay
├── data
│   ├── local
│   │   ├── AppDatabase.java          // Baza Room (wersja 1)
│   │   ├── FavoriteCoin.java         // Encja Room dla ulubionej monety
│   │   └── FavoriteCoinDao.java      // DAO (CRUD dla ulubionych, LiveData sprawdzające czy coin jest ulubiony)
│   ├── model
│   │   ├── Coin.java                 // Rozszerzenie o: marketCap, totalVolume, high24h, low24h, ath, circulatingSupply
│   │   ├── MarketChartResponse.java  // Model odpowiedzi /coins/{id}/market_chart (lista punktów [timestamp, price])
│   │   └── ChartPoint.java           // Pojedynczy punkt wykresu (timestamp, price)
│   ├── remote
│   │   └── CoinGeckoApiService.java  // Rozszerzenie o getCoinDetails(...) oraz getMarketChart(...)
│   └── repository
│       ├── CoinRepository.java       // Pobieranie szczegółów i danych wykresu
│       └── FavoritesRepository.java  // Zarządzanie ulubionymi w bazie Room (isFavorite, toggleFavorite)
├── ui
│   └── details
│       ├── DetailsFragment.java      // Ekran szczegółów
│       ├── DetailsViewModel.java     // ViewModel zarządzający stanem UI, ładowaniem wykresu i ulubionymi
│       ├── DetailsUiState.java       // Reprezentacja stanów: Loading, Success, Error
│       └── ChartTimeSpan.java        // Przedziały czasowe wykresu (np. 1D, 7D, 30D, 90D)
└── util
    ├── CurrencyFormatter.java        // Rozszerzenie o formatowanie dużych liczb (Market Cap, Volume)
    └── DateFormatter.java            // Formater dat/godzin na osi X wykresu
```

### Warstwa UI (XML):
- `fragment_details.xml`:
  - **Nagłówek:**
    - Przycisk powrotu (Back Arrow) do listy Market
    - Ikona kryptowaluty (`ImageView`, ładowana przez Glide)
    - Tytuł (Nazwa) i symbol (np. "Bitcoin", badge "BTC", ranga "#1")
    - Przycisk "Dodaj do ulubionych" / gwiazdka (`ImageButton` ze stanem włączony/wyłączony)
  - **Sekcja cenowa:**
    - Aktualna cena (np. `$79,416.00`)
    - Zmiana 24h w procentach (`+2.74%`) w kolorowym badge oraz zmiana kwotowa
  - **Sekcja wykresu (MPAndroidChart):**
    - Selektor zakresu czasowego (`ChipGroup` z opcjami: 24H, 7D, 30D, 90D — domyślnie 7D)
    - Wykres liniowy `LineChart` ze stylizacją (płynna krzywa Beziera, gradient wypełnienia poniżej linii, kolor zielony/czerwony w zależności od trendu)
    - ProgressBar dla ładowania samego wykresu przy zmianie zakresu
  - **Sekcja statystyk rynkowych (siatka kart / szczegółowych informacji):**
    - Kapitalizacja rynkowa (Market Cap)
    - Wolumen 24h (24h Trading Volume)
    - Maksimum 24h (24h High)
    - Minimum 24h (24h Low)
    - Najwyższa cena w historii (All-Time High - ATH)
    - Podaż w obiegu (Circulating Supply)
  - **Obsługa stanów:**
    - Layout ładowania (`ProgressBar` + napis)
    - Layout błędu (ikona + komunikat + przycisk `Ponów próbę`)
    - Layout pusty (gdy brak ID coina z nawigacji — z przyciskiem powrotu do Marketu)
- `res/drawable`:
  - `ic_star_filled.xml`, `ic_star_outline.xml` (dla przycisku ulubionych)
  - `ic_arrow_back.xml` (dla nawigacji powrotnej)
  - Style chipów i tła kart statystyk

---

## 4. Kroki realizacji (Krok po kroku)

### Faza 1: Zależności i konfiguracja
- [x] 1.1 Dodanie repozytorium JitPack w `settings.gradle.kts`.
- [x] 1.2 Dodanie zależności `MPAndroidChart` oraz `Room` (runtime + compiler annotationProcessor) w `gradle/libs.versions.toml` i `app/build.gradle.kts`.
- [x] 1.3 Dodanie definicji stringów i kolorów w `strings.xml` i `colors.xml`.
- [x] 1.4 Dodanie wektorów `ic_star_filled`, `ic_star_outline`, `ic_arrow_back`.

### Faza 2: Baza danych Room i modele danych
- [x] 2.1 Implementacja encji `FavoriteCoin.java` i interfejsu `FavoriteCoinDao.java`.
- [x] 2.2 Implementacja bazy danych `AppDatabase.java`.
- [x] 2.3 Rozszerzenie modelu `Coin.java` o dodatkowe pola statystyk (market_cap, high_24h, low_24h, total_volume, ath, circulating_supply).
- [x] 2.4 Utworzenie modelu `MarketChartResponse.java` dla punktów wykresu.
- [x] 2.5 Rozszerzenie `CoinGeckoApiService.java` o metody:
  - `getCoinDetails(@Query("vs_currency") String vsCurrency, @Query("ids") String ids)`
  - `getMarketChart(@Path("id") String coinId, @Query("vs_currency") String vsCurrency, @Query("days") String days)`

### Faza 3: Testy Jednostkowe (Zgodnie z zasadą: "1) Piszesz testy zanim ruszysz kod")
- [x] 3.1 Napisanie testów jednostkowych dla `CoinRepositoryTest` (obsługa pobierania szczegółów monety i danych wykresu, mapowanie punktów wykresu).
- [x] 3.2 Napisanie testów jednostkowych dla `FavoritesRepositoryTest` (dodawanie, usuwanie i sprawdzanie stanu ulubionych).
- [x] 3.3 Napisanie testów jednostkowych dla `DetailsViewModelTest` (sprawdzenie emisji stanów Loading, Success, Error, poprawne przełączanie ulubionych, zmiana zakresu dni wykresu).
- [x] 3.4 Uruchomienie testów i potwierdzenie, że nie przechodzą przed implementacją.

### Faza 4: Implementacja logiki i warstwy danych
- [x] 4.1 Implementacja metod pobierania wykresu i szczegółów w `CoinRepository.java`.
- [x] 4.2 Implementacja `FavoritesRepository.java`.
- [x] 4.3 Rozszerzenie `CurrencyFormatter.java` (formatowanie dużych kwot M/B/T dla kapitalizacji i wolumenu).
- [x] 4.4 Implementacja `DetailsUiState.java` oraz `DetailsViewModel.java`.

### Faza 5: Warstwa UI i Nawigacja
- [x] 5.1 Aktualizacja `nav_graph.xml` o akcję `action_marketFragment_to_detailsFragment` z argumentem `coinId`.
- [x] 5.2 Podłączenie kliknięcia elementu w `MarketFragment.java` do przejścia do `detailsFragment`.
- [x] 5.3 Utworzenie layoutu `fragment_details.xml` z kompletem widoków i stanów.
- [x] 5.4 Implementacja `DetailsFragment.java`:
  - Odczyt argumentu `coinId` z Bundle (lub domyślny fallback np. "bitcoin" przy wejściu z dolnego menu).
  - Obserwacja `DetailsViewModel` (stan UI, dane coina, punkty wykresu, stan ulubionego).
  - Konfiguracja wykresu `MPAndroidChart` (kolory, styl linii, siatka, etykiety, brak niepotrzebnych osi).
  - Obsługa przełączania zakresu czasowego (ChipGroup 24H, 7D, 30D, 90D).
  - Obsługa kliknięcia przycisku ulubionych (toggle ze zmianą ikony gwiazdki i powiadomieniem).
  - Obsługa przycisku powrotu (popBackStack).

### Faza 6: Weryfikacja i kryteria Definition of Done
- [x] 6.1 Uruchomienie testów jednostkowych: `./gradlew testDebugUnitTest`.
- [x] 6.2 Weryfikacja kompilacji i analizy statycznej: `./gradlew lint` i `./gradlew build`.
- [x] 6.3 Weryfikacja braku zahardkodowanych stringów.
- [x] 6.4 Przygotowanie instrukcji weryfikacji na fizycznym telefonie dla użytkownika.
