# Plan Działania — Usunięcie Ekranu Szczegółów z Dolnego Paska Nawigacji i Dostęp Wyłącznie po Kliknięciu

## 1. Przeczytane pliki i cel ich analizy

1. `AGENTS.md` — Główne zasady projektu, proces pracy (krok 0: plan, krok 1: testy przed kodem, krok 2: weryfikacja, krok 3: instrukcja dla użytkownika) oraz architektura (MVVM, Jetpack Navigation, Java).
2. `app/src/main/res/menu/bottom_nav_menu.xml` — Analiza aktualnych zakładek dolnego paska. Obecnie zawiera 4 pozycje: Market, Favorites, Details, Alerts. Zgodnie z wymaganiem usuwamy pozycję Details (`detailsFragment`).
3. `app/src/main/res/navigation/nav_graph.xml` — Analiza grafu nawigacji Jetpack. `detailsFragment` musi pozostać w grafie nawigacyjnym jako cel dla akcji przejścia z Market, Favorites i Alerts, jednak nie jest już zakładką główną w dolnym pasku.
4. `app/src/main/java/com/example/cryptogay/MainActivity.java` — Analiza konfiguracji nawigacji. Dodamy `OnDestinationChangedListener` do `NavController`, aby automatycznie ukrywać `bottomNav` (`View.GONE`), gdy użytkownik przejdzie do `DetailsFragment`, i przywracać widoczność (`View.VISIBLE`) na ekranach głównych.
5. `app/src/main/java/com/example/cryptogay/ui/details/DetailsFragment.java` oraz `fragment_details.xml` — Analiza obsługi przycisku powrotu (`btn_back`). Zapewnienie niezawodnego działania cofania: zarówno przez kliknięcie strzałki wstecz na pasku górnym (`navigateUp()` / `popBackStack()`), jak i przez gest / przycisk systemowy Androida.
6. `app/src/main/java/com/example/cryptogay/ui/market/MarketFragment.java`, `FavoritesFragment.java`, `AlertsFragment.java` — Weryfikacja akcji przejścia do szczegółów po kliknięciu na kafelek kryptowaluty.

---

## 2. Co będziemy modyfikować / tworzyć

### Modyfikowane pliki:
1. `app/src/main/res/menu/bottom_nav_menu.xml` — Usunięcie elementu `<item android:id="@+id/detailsFragment" .../>`. Pasek będzie zawierał teraz 3 zakładki: Rynek (Market), Ulubione (Favorites), Alerty (Alerts).
2. `app/src/main/java/com/example/cryptogay/MainActivity.java` — Konfiguracja dynamicznego ukrywania dolnej nawigacji na ekranie szczegółów (`addOnDestinationChangedListener`), aby ekran szczegółów działał jako dedykowany widok podrzędny (child/detail screen).
3. `app/src/main/java/com/example/cryptogay/ui/details/DetailsFragment.java` — Upewnienie się, że obsługa cofania (`btn_back` oraz systemowy back) zawsze prawidłowo wraca do poprzedniego ekranu na stosie nawigacji.
4. `app/src/test/java/com/example/cryptogay/NavigationBehaviorTest.java` — Testy jednostkowe sprawdzające zachowanie logiki widoczności nawigacji oraz przekazywania parametrów nawigacji.

---

## 3. Kroki realizacji

### Faza 1: Testy jednostkowe przed kodem (Zgodnie z zasadą: "1) Piszesz testy zanim ruszysz kod")
- [x] 1.1 Napisanie testów jednostkowych weryfikujących logikę widoczności BottomNavigationView dla poszczególnych ID destynacji (np. `R.id.detailsFragment` -> ukryty, inne -> widoczny) oraz logikę argumentu `coin_id`.
- [x] 1.2 Uruchomienie `./gradlew testDebugUnitTest` i weryfikacja.

### Faza 2: Usunięcie ekranu szczegółów z menu dolnego paska
- [x] 2.1 Modyfikacja `bottom_nav_menu.xml`: usunięcie `detailsFragment`. Pasek dolny posiada teraz przejrzyste 3 taby: Rynek, Ulubione, Alerty.

### Faza 3: Dynamiczne ukrywanie paska dolnego na ekranie szczegółów
- [x] 3.1 W `MainActivity.java` dodanie nasłuchiwania `addOnDestinationChangedListener`:
  - Jeżeli destination to `R.id.detailsFragment`: `binding.bottomNav.setVisibility(View.GONE)`
  - W przeciwnym razie: `binding.bottomNav.setVisibility(View.VISIBLE)`

### Faza 4: Weryfikacja i obsługa powrotu (Back Navigation)
- [x] 4.1 W `DetailsFragment.java` upewnienie się, że `btnBack` wywołuje `navigateUp()` / `popBackStack()`.
- [x] 4.2 Weryfikacja przejść do `detailsFragment` z:
  - `MarketFragment` (kliknięcie coina na liście)
  - `FavoritesFragment` (kliknięcie coina w ulubionych)
  - `AlertsFragment` (kliknięcie alertu)
  - Powiadomienia push (intent z `coin_id`)
- [x] 4.3 Przetestowanie obsługi gestu cofania i przycisku wstecz (popBackStack).

### Faza 5: Weryfikacja końcowa i Definition of Done
- [x] 5.1 Uruchomienie `./gradlew testDebugUnitTest` — wszystkie testy przechodzą.
- [x] 5.2 Uruchomienie `./gradlew lint` oraz `./gradlew build`.
- [x] 5.3 Przygotowanie instrukcji weryfikacji na telefonie.
