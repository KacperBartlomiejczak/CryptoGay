# Plan Działania — Ekran Ulubionych (Favorites)

## 1. Przeczytane pliki i cel ich analizy

1. `AGENTS.md` — Zbiór reguł projektu, opis 2. ekranu ("Ulubione (Favorites) — podzbiór kryptowalut oznaczonych przez użytkownika jako ulubione na ekranie 1. Dane muszą przetrwać restart aplikacji"), stos technologiczny (Java, Room, LiveData, MVVM, ViewBinding) oraz workflow (plan -> testy -> implementacja -> weryfikacja).
2. `prompts/Market/plan.md` oraz `prompts/Details/plan.md` — Wzorce struktury projektu, podziału warstw, standardów testów jednostkowych i obsługi stanów UI.
3. `app/src/main/res/navigation/nav_graph.xml` — Analiza konfiguracji Jetpack Navigation; przygotowanie akcji nawigacji `action_favoritesFragment_to_detailsFragment` z przekazywaniem `coin_id`.
4. `app/src/main/res/menu/bottom_nav_menu.xml` — Weryfikacja menu dolnej nawigacji (pozycja `favoritesFragment` z etykietą i ikoną).
5. `app/src/main/java/com/example/cryptogay/ui/favorites/FavoritesFragment.java` — Analiza aktualnego, pustego szkieletu zawierającego tylko prosty `TextView`.
6. `app/src/main/java/com/example/cryptogay/data/local/FavoriteCoin.java`, `FavoriteCoinDao.java`, `AppDatabase.java` — Analiza schematu bazy Room przechowującej ulubione monety (`id`, `symbol`, `name`, `image`, `currentPrice`, `priceChangePercentage24h`, `addedAt`) oraz zapytania `getAllFavorites()`.
7. `app/src/main/java/com/example/cryptogay/data/repository/FavoritesRepository.java` — Sprawdzenie istniejących metod operacji na ulubionych (`getAllFavorites()`, `isFavorite()`, `toggleFavorite()`) oraz dodanie metody pobierania identyfikatorów ulubionych (`getAllFavoriteIds()`) dla synchronizacji gwiazdek na liście Market.
8. `app/src/main/java/com/example/cryptogay/ui/market/CryptoAdapter.java` i `item_crypto.xml` — Analiza możliwości współdzielenia widoku kafelka kryptowaluty oraz dodania bezpośredniego oznaczania gwiazdką również z poziomu listy.
9. `app/src/main/res/values/strings.xml` — Zidentyfikowanie brakujących napisów dla stanów ekranu Ulubionych (stan pusty, nagłówek, zachęta do dodania do ulubionych, przycisk przejścia do rynku).

---

## 2. Konsultacja bibliotek (Zgodnie z zasadą: "Nie dodawaj bibliotek spoza wymienionego stacku bez wcześniejszej konsultacji z użytkownikiem")

Wszystkie potrzebne komponenty znajdują się już w projekcie i są w 100% zgodne ze zadeklarowanym stackiem z `AGENTS.md`:
- `Room` (`androidx.room:room-runtime`) — już skonfigurowany, przechowuje ulubione.
- `Lifecycle ViewModel & LiveData` — do reaktywnego przekazywania danych z bazy Room do widoku.
- `Navigation Component` — do nawigacji z kafelka ulubionego do ekranu `DetailsFragment`.
- `Glide` — do wyświetlania ikon krypto.
- `ViewBinding` — obsługa layoutu XML w Javie.

Nie dodajemy żadnych nowych zewnętrznych bibliotek.

---

## 3. Co będziemy tworzyć

### Struktura plików w pakiecie `com.example.cryptogay`:
```
com.example.cryptogay
├── data
│   ├── local
│   │   └── FavoriteCoinDao.java        // Dodanie zapytania getAllFavoriteIds()
│   ├── model
│   │   └── FavoriteCoin.java           // Dodanie metody toCoin() ułatwiającej mapowanie
│   └── repository
│       └── FavoritesRepository.java    // Dodanie metody getAllFavoriteIds() oraz refreshFavoritesPrices()
├── ui
│   ├── favorites
│   │   ├── FavoritesFragment.java      // Pełny ekran ulubionych z obsługą stanów, odświeżania i przejścia do Details
│   │   ├── FavoritesViewModel.java     // ViewModel reagujący na LiveData z Room + odświeżanie cen
│   │   └── FavoritesUiState.java       // Reprezentacja stanów: LOADING, SUCCESS, EMPTY, ERROR
│   └── market
│       └── CryptoAdapter.java          // (Opcjonalnie) obsługa stanu ulubionego / gwiazdki
```

### Warstwa UI (XML):
- `fragment_favorites.xml`:
  - **Nagłówek:** Tytuł ekranu ("Ulubione"), licznik ulubionych monet (np. "3 monety").
  - **SwipeRefreshLayout + RecyclerView:** Lista ulubionych monet (z wykorzystaniem kafelków `item_crypto.xml`).
  - **Layout stanu pustego (`layout_empty`):**
    - Ikona gwiazdki
    - Tytuł: "Brak ulubionych kryptowalut"
    - Opis: "Nie masz jeszcze dodanych żadnych kryptowalut do ulubionych. Oznacz gwiazdką monety na ekranie Rynku lub w ich szczegółach."
    - Przycisk: "Przejdź do rynku" (przenosi do `MarketFragment`).
  - **Layout stanu ładowania (`layout_loading`):** ProgressBar + komunikat.
  - **Layout stanu błędu (`layout_error`):** Ikona błędu + komunikat + przycisk ponów próbę.
- `nav_graph.xml`:
  - Dodanie akcji nawigacji: `action_favoritesFragment_to_detailsFragment` z argumentem `coin_id`.

---

## 4. Kroki realizacji (Krok po kroku)

### Faza 1: Baza danych Room i Repozytorium
- [x] 1.1 Rozszerzenie `FavoriteCoinDao` o `LiveData<List<String>> getAllFavoriteIds()`.
- [x] 1.2 Dodanie metody `toCoin()` w klasie `FavoriteCoin`.
- [x] 1.3 Rozszerzenie `FavoritesRepository` o `getAllFavoriteIds()`.

### Faza 2: Zasoby tekstowe i Nawigacja
- [x] 2.1 Dodanie wymaganych stringów do `strings.xml` (dla nagłówka, liczników, stanu pustego ekranu ulubionych, opisów dostępności).
- [x] 2.2 Dodanie akcji `action_favoritesFragment_to_detailsFragment` do `nav_graph.xml`.

### Faza 3: Testy Jednostkowe (Zgodnie z zasadą: "1) Piszesz testy zanim ruszysz kod")
- [x] 3.1 Napisanie testów jednostkowych w `FavoritesViewModelTest`:
  - Test emisji stanu `EMPTY`, gdy baza Room zwraca pustą listę ulubionych.
  - Test emisji stanu `SUCCESS` z listą monet, gdy w bazie znajdują się ulubione krypto.
  - Test mapowania `FavoriteCoin` na model `Coin`.
  - Test usuwania/odznaczania z ulubionych.
- [x] 3.2 Uruchomienie testów i potwierdzenie, że wykrywają brak implementacji / kompilują się z nowymi wymaganiami.

### Faza 4: Implementacja logiki i ViewModel
- [x] 4.1 Implementacja `FavoritesUiState.java` (stany: LOADING, SUCCESS, EMPTY, ERROR).
- [x] 4.2 Implementacja `FavoritesViewModel.java` z wstrzykniętym/używanym `FavoritesRepository`.

### Faza 5: Warstwa UI (Layout i Fragment)
- [x] 5.1 Utworzenie `fragment_favorites.xml` ze wszystkimi 4 stanami (Loading, Success z RecyclerView, Empty z przyciskiem przejścia do Rynku, Error).
- [x] 5.2 Implementacja `FavoritesFragment.java`:
  - Inicjalizacja ViewBinding.
  - Konfiguracja `RecyclerView` z `CryptoAdapter`.
  - Obsługa kliknięcia kafelka -> przejście do `DetailsFragment` z `coin_id`.
  - Obsługa `SwipeRefreshLayout` do odświeżania danych.
  - Obsługa przycisku "Przejdź do rynku" w stanie pustym.
  - Obserwacja `FavoritesViewModel` i przełączanie widoczności widoków.

### Faza 6: Weryfikacja i kryteria Definition of Done
- [x] 6.1 Uruchomienie testów jednostkowych: `./gradlew testDebugUnitTest`.
- [x] 6.2 Weryfikacja kompilacji i analizy statycznej: `./gradlew lint` i `./gradlew build`.
- [x] 6.3 Sprawdzenie braku zahardkodowanych stringów.
- [x] 6.4 Podsumowanie i przygotowanie instrukcji weryfikacji na fizycznym telefonie dla użytkownika.
