# Plan Działania — Ekran 4: Alerty Cenowe (Price Alerts / Thresholds)

## 1. Przeczytane pliki i cel ich analizy

1. `AGENTS.md` — Główne zasady projektu, stos technologiczny (Java, Room, WorkManager, NotificationManager, MVVM, ViewBinding), reguły dotyczące thresholdów ("Threshold notification: logika porównania ceny ma być w Worker, nie w UI. UI tylko zapisuje ustawiony próg do Room") oraz Definition of Done.
2. `prompts/Favorites/plan.md` i `prompts/Details/plan.md` — Wzorce struktury planu, podziału warstw, standardów testów jednostkowych i obsługi stanów UI.
3. `app/src/main/res/menu/bottom_nav_menu.xml` — Dodanie 4. pozycji menu dolnego (`alertsFragment`).
4. `app/src/main/res/navigation/nav_graph.xml` — Konfiguracja nawigacji Jetpack dla nowego fragmentu (`AlertsFragment`) i przejścia do `DetailsFragment`.
5. `app/src/main/java/com/example/cryptogay/MainActivity.java` — Weryfikacja podłączenia `BottomNavigationView` z `NavController` (używa `NavigationUI.setupWithNavController`, obsłuży 4 taby automatycznie po dopasowaniu ID).
6. `app/src/main/java/com/example/cryptogay/data/local/AppDatabase.java` — Rozszerzenie schematu bazy danych Room o nową encję `PriceAlert` i podbicie wersji bazy.
7. `app/src/main/java/com/example/cryptogay/ui/details/DetailsFragment.java` i `fragment_details.xml` — Analiza sekcji szczegółów w celu dodania możliwości ustawiania/edycji progu cenowego (threshold).
8. `gradle/libs.versions.toml` i `app/build.gradle.kts` — Weryfikacja zależności dla `androidx.work:work-runtime` (zgodnie ze stosem z `AGENTS.md`).
9. `app/src/main/res/values/strings.xml` — Identyfikacja potrzebnych tekstów (brak zahardkodowanych stringów: stany pusty, ładowania, błędu, dialogi progów cenowych, powiadomienia).

---

## 2. Konsultacja bibliotek (Zgodnie z zasadą: "Nie dodawaj bibliotek spoza wymienionego stacku bez wcześniejszej konsultacji z użytkownikiem")

Wszystkie potrzebne komponenty znajdują się w zadeklarowanym stosie w `AGENTS.md`:
- `Room` (`androidx.room:room-runtime`) — już skonfigurowany w projekcie, dodamy nową encję `PriceAlert`.
- `WorkManager` (`androidx.work:work-runtime:2.9.0`) — jawnie wymieniony w `AGENTS.md` ("Tło / powiadomienia: WorkManager z periodycznym Worker-em sprawdzającym cenę i porównującym z thresholdem; NotificationManager + kanał powiadomień do wysyłki alertu").
- `NotificationManager` / `NotificationCompat` — standardowe API Androida do wysyłania powiadomień push po przekroczeniu progu.
- `Lifecycle ViewModel & LiveData` — reaktywne przekazywanie listy alertów z bazy danych Room.
- `Navigation Component` — integracja z 4. tabem dolnej nawigacji i przejście do szczegółów.

---

## 3. Co będziemy tworzyć

### Struktura plików w pakiecie `com.example.cryptogay`:
```
com.example.cryptogay
├── data
│   ├── local
│   │   ├── AppDatabase.java            // Wersja 2, dodanie PriceAlert.class i fallbackToDestructiveMigration
│   │   ├── PriceAlert.java             // Encja Room: coinId (PK), coinSymbol, coinName, coinImage, targetPrice, isAbove, isActive, createdAt
│   │   └── PriceAlertDao.java          // DAO: CRUD (insert/update, delete, getAllAlerts LiveData, getActiveAlerts List, getAlertForCoin)
│   └── repository
│       └── AlertsRepository.java       // Operacje na alertach (Single Source of Truth, asynchroniczny zapis/odczyt)
├── ui
│   ├── alerts
│   │   ├── AlertsFragment.java         // 4. ekran w nawigacji: lista alertów, obsługa stanów, usuwanie, przejście do szczegółów
│   │   ├── AlertsViewModel.java        // ViewModel zarządzający stanem UI alertów (LiveData z Room)
│   │   ├── AlertsUiState.java          // Stany UI: LOADING, SUCCESS, EMPTY, ERROR
│   │   └── AlertsAdapter.java          // Adapter RecyclerView z kafelkami alertów, statusem i przyciskiem usunięcia
│   └── details
│       ├── DetailsFragment.java        // Dodanie sekcji/dialogu ustawiania progu cenowego (threshold)
│       └── DetailsViewModel.java       // Obsługa zapisywania/usuwania progu cenowego dla wybranej monety
├── worker
│   └── PriceAlertWorker.java           // Worker WorkManager sprawdzający w tle ceny i porównujący z thresholdem
├── notification
│   └── AlertNotificationHelper.java    // Tworzenie kanału powiadomień i wysyłanie notyfikacji push
└── res/
    ├── drawable/ic_nav_alerts.xml      // Ikona dzwonka dla 4. zakładki menu
    ├── drawable/ic_notification.xml    // Ikona do powiadomień w systemie
    ├── layout/fragment_alerts.xml      // Layout ekranu alertów ze stanami loading, success, empty, error
    ├── layout/item_alert.xml           // Kafelek pojedynczego alertu cenowego
    └── layout/dialog_set_alert.xml     // Dialog/BottomSheet do ustawiania progu cenowego
```

---

## 4. Kroki realizacji (Krok po kroku)

### Faza 1: Zależności, Baza Danych Room i Repozytorium
- [x] 1.1 Dodanie `androidx.work:work-runtime` do `gradle/libs.versions.toml` i `app/build.gradle.kts`.
- [x] 1.2 Utworzenie encji `PriceAlert.java` (pola: `coinId`, `coinSymbol`, `coinName`, `coinImage`, `targetPrice`, `isAbove`, `isActive`, `createdAt`).
- [x] 1.3 Utworzenie interfejsu `PriceAlertDao.java` (`getAllAlerts()`, `getActiveAlerts()`, `getAlertForCoin()`, `insertOrUpdate()`, `delete()`, `deleteByCoinId()`).
- [x] 1.4 Aktualizacja `AppDatabase.java` (wersja 2, dodanie `PriceAlert.class` i `priceAlertDao()`).
- [x] 1.5 Implementacja `AlertsRepository.java`.

### Faza 2: WorkManager i Powiadomienia w Tle
- [x] 2.1 Implementacja `AlertNotificationHelper.java` (tworzenie `NotificationChannel`, budowa powiadomienia `NotificationCompat.Builder` otwierającego aplikację).
- [x] 2.2 Implementacja `PriceAlertWorker.java` (pobranie aktywnych alertów z bazy, zapytanie CoinGecko API o aktualne ceny, weryfikacja czy cena przekroczyła próg `isAbove` / `isBelow`, wysyłka notyfikacji).
- [x] 2.3 Konfiguracja rejestracji okresowego sprawdzania w tle w `WorkManager` (np. co 15 minut) oraz w `AndroidManifest.xml` (uprawnienie `POST_NOTIFICATIONS`).

### Faza 3: Testy Jednostkowe (Zgodnie z zasadą: "1) Piszesz testy zanim ruszysz kod")
- [x] 3.1 Testy jednostkowe w `AlertsViewModelTest`:
  - Test emisji stanu `EMPTY`, gdy brak alertów w bazie.
  - Test emisji stanu `SUCCESS` z listą alertów, gdy są zapisane w bazie.
  - Test usuwania alertu przez ViewModel.
- [x] 3.2 Testy jednostkowe w `AlertsRepositoryTest` (dodawanie, usuwanie i pobieranie alertu).
- [x] 3.3 Uruchomienie testów i potwierdzenie, że nie przechodzą przed implementacją.

### Faza 4: Implementacja ViewModel i Logiki UI
- [x] 4.1 Implementacja `AlertsUiState.java` (stany: LOADING, SUCCESS, EMPTY, ERROR).
- [x] 4.2 Implementacja `AlertsViewModel.java`.
- [x] 4.3 Rozszerzenie `DetailsViewModel.java` o obsługę alertu dla wyświetlanego coina (`getAlertForCoin()`, `saveAlert()`, `deleteAlert()`).

### Faza 5: Warstwa UI, Nawigacja i Layouty
- [x] 5.1 Utworzenie ikon `ic_nav_alerts.xml` i dodanie stringów do `strings.xml`.
- [x] 5.2 Aktualizacja `bottom_nav_menu.xml` o 4. pozycję menu (`alertsFragment`).
- [x] 5.3 Aktualizacja `nav_graph.xml` (definicja `alertsFragment` i akcji przejścia do `detailsFragment`).
- [x] 5.4 Utworzenie `item_alert.xml` oraz adaptera `AlertsAdapter.java`.
- [x] 5.5 Utworzenie layoutu `fragment_alerts.xml` (obsługa stanów: Loading, Success z RecyclerView i SwipeRefresh, Empty z przyciskiem przejścia do Rynku, Error).
- [x] 5.6 Implementacja `AlertsFragment.java`.
- [x] 5.7 Rozszerzenie `fragment_details.xml` i `DetailsFragment.java` o sekcję / przycisk ustawienia progu cenowego (otwierający dialog `dialog_set_alert.xml`).

### Faza 6: Weryfikacja i Definition of Done
- [x] 6.1 Uruchomienie testów jednostkowych: `./gradlew testDebugUnitTest`.
- [x] 6.2 Weryfikacja kompilacji i analizy statycznej: `./gradlew lint` i `./gradlew build`.
- [x] 6.3 Weryfikacja braku zahardkodowanych stringów.
- [x] 6.4 Podsumowanie i przygotowanie instrukcji weryfikacji na fizycznym telefonie dla użytkownika.
