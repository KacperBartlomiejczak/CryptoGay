# Plan Działania — Rate Limiting i Pamięć Podręczna (10-minutowy Cache)

## 1. Przeczytane pliki i cel ich analizy

1. `AGENTS.md` — Główne zasady projektu (Java, Room, MVVM, brak zbędnych bibliotek, TDD, Definition of Done).
2. `app/src/main/java/com/example/cryptogay/data/repository/CoinRepository.java` — Analiza aktualnych wywołań API CoinGecko (`fetchCoins`, `fetchCoinDetails`, `fetchMarketChart`). Dotychczas każda metoda bezpośrednio odpytywała Retrofit/sieć bez żadnego buforowania ani ograniczenia częstości zapytań (rate limiting).
3. `app/src/main/java/com/example/cryptogay/ui/market/MarketViewModel.java` oraz `MarketFragment.java` — Analiza cyklu życia: po każdym przejściu między zakładkami (Market <-> Favorites / Details) `MarketFragment` wywoływał `loadCoins()`, co powodowało uderzanie w API co kilka sekund i blokowanie przez CoinGecko (HTTP 429 Too Many Requests).
4. `app/src/main/java/com/example/cryptogay/ui/details/DetailsViewModel.java` oraz `DetailsFragment.java` — Analiza pobierania szczegółów i wykresów: każde wejście w szczegóły lub zmiana zakresu czasu (24h, 7d, 30d, 1y) wykonywało nowe żądania sieciowe.
5. `app/src/main/java/com/example/cryptogay/data/local/AppDatabase.java` — Analiza bazy Room (wersja 2, encje `FavoriteCoin`, `PriceAlert`), zaplanowanie wersji 3 z encją `CachedCoin`.
6. `app/src/main/java/com/example/cryptogay/data/model/Coin.java` — Analiza struktury obiektu kryptowaluty na potrzeby mapowania do encji `CachedCoin`.
7. `app/src/test/java/com/example/cryptogay/CoinRepositoryTest.java` i `MarketViewModelTest.java` — Analiza istniejących testów jednostkowych w celu rozszerzenia ich o weryfikację cache'owania i rate limiting.

---

## 2. Konsultacja bibliotek

Wszystkie mechanizmy zostaną zrealizowane w oparciu o istniejący stos technologiczny zadeklarowany w `AGENTS.md`:
- `Room` (`androidx.room:room-runtime`) — trwała pamięć podręczna listy kryptowalut na dysku urządzenia (dane przetrwają restart aplikacji).
- `In-memory cache` (struktury danych Java w pamięci RAM: `Map`, znaczniki czasu `System.currentTimeMillis()`) — natychmiastowy dostęp do szczegółów i wykresów bez opóźnień I/O.
- `Java System Time / AtomicLong` — precyzyjny pomiar czasu ważności pamięci podręcznej (TTL = 10 minut) oraz minimalnego odstępu między zapytaniami sieciowymi (cooldown rate-limiter, np. 30 sekund).

Nie są wymagane żadne nowe biblioteki zewnętrzne.

---

## 3. Co będziemy tworzyć i modyfikować

### Nowe pliki:
1. `app/src/main/java/com/example/cryptogay/data/local/CachedCoin.java` — Encja Room przechowująca pobrane monety wraz ze znacznikiem czasu pobrania (`cachedAt`).
2. `app/src/main/java/com/example/cryptogay/data/local/CachedCoinDao.java` — Interfejs DAO do zapisu i odczytu listy z cache Room (`insertAll`, `getAllCachedCoins`, `deleteAll`, `getOldestCacheTimestamp`).
3. `app/src/test/java/com/example/cryptogay/RateLimitingCacheTest.java` — Nowe testy jednostkowe weryfikujące zachowanie rate limitera, 10-minutowego TTL cache'a oraz fallbacku w przypadku błędu sieci/429.

### Modyfikowane pliki:
1. `app/src/main/java/com/example/cryptogay/data/local/AppDatabase.java` — Podbicie wersji do 3, dodanie encji `CachedCoin` oraz metody `cachedCoinDao()`.
2. `app/src/main/java/com/example/cryptogay/data/repository/CoinRepository.java` — Implementacja:
   - Stałej `CACHE_DURATION_MS = 10 * 60 * 1000L` (10 minut).
   - Rate limiting cooldown (`MIN_NETWORK_INTERVAL_MS = 30 * 1000L` — ochrona przed spamowaniem SwipeRefresh).
   - Obsługi pamięci podręcznej Room + RAM dla `fetchCoins(boolean forceRefresh, Callback<List<Coin>> callback)`.
   - Jeśli dane mają mniej niż 10 minut i `forceRefresh == false`, natychmiast zwracamy zapamiętane dane (zero zapytań HTTP).
   - Jeśli zapytanie sieciowe zwróci błąd (np. HTTP 429 Too Many Requests lub brak internetu), zwracamy zapamiętane dane z bazy (graceful fallback).
   - Pamięci podręcznej w RAM dla `fetchCoinDetails` i `fetchMarketChart` (klucz: `coinId + "_" + days`) z 10-minutowym TTL.
3. `app/src/main/java/com/example/cryptogay/ui/market/MarketViewModel.java` — Rozdzielenie `loadCoins()` (korzysta z cache < 10 minut) od `refreshCoins()` (wymuszenie odświeżenia z UI przy pociągnięciu w dół).
4. `app/src/main/java/com/example/cryptogay/ui/market/MarketFragment.java` — Podłączenie `swipeRefresh` pod `viewModel.refreshCoins()`.
5. `app/src/main/res/values/strings.xml` — Dodanie komunikatów informacyjnych (np. informacja o użyciu zapamiętanych danych z powodu limitu zapytań API).

---

## 4. Kroki realizacji (Krok po kroku)

### Faza 1: Baza Danych Room (Encja i DAO dla Cache)
- [x] 1.1 Utworzenie encji `CachedCoin.java` w pakiecie `data.local`.
- [x] 1.2 Utworzenie `CachedCoinDao.java`.
- [x] 1.3 Aktualizacja `AppDatabase.java` (wersja 3, dodanie `CachedCoin.class` i `cachedCoinDao()`).

### Faza 2: Testy Jednostkowe (TDD: testy przed implementacją kodu)
- [x] 2.1 Przygotowanie testów w `RateLimitingCacheTest.java`:
  - Test: Zwrócenie danych z cache bez wykonywania zapytania sieciowego, jeśli od pobrania minęło mniej niż 10 minut.
  - Test: Wykonanie zapytania sieciowego, jeśli dane w cache są starsze niż 10 minut.
  - Test: Wymuszenie zapytania sieciowego (`forceRefresh = true`), gdy użytkownik przeciągnie SwipeRefresh.
  - Test: Zwrócenie danych z cache w przypadku błędu sieci/429 (fallback).
  - Test: Pamięć podręczna dla szczegółów monety i wykresu z 10-minutowym TTL.
- [x] 2.2 Uruchomienie testów i potwierdzenie, że nie przechodzą przed implementacją.

### Faza 3: Implementacja w CoinRepository
- [x] 3.1 Rozbudowa `CoinRepository` o obsługę DAO bazy danych Room oraz pamięci RAM.
- [x] 3.2 Zaimplementowanie logiki sprawdzania wieku danych (10 minut TTL) i rate limiting cooldown.
- [x] 3.3 Zaimplementowanie mechanizmu fallbacku do danych z bazy Room przy błędach sieciowych lub 429.
- [x] 3.4 Dodanie pamięci podręcznej dla szczegółów i wykresów w `CoinRepository`.

### Faza 4: Integracja z ViewModel i UI
- [x] 4.1 Aktualizacja `MarketViewModel.java` (`loadCoins()` vs `refreshCoins()`).
- [x] 4.2 Aktualizacja `MarketFragment.java` (podpięcie `refreshCoins()` pod gest swipe-to-refresh).
- [x] 4.3 Zaktualizowanie `strings.xml`.

### Faza 5: Weryfikacja i Definition of Done
- [x] 5.1 Uruchomienie testów jednostkowych: `./gradlew testDebugUnitTest`.
- [x] 5.2 Weryfikacja kompilacji i analizy statycznej: `./gradlew lint` i `./gradlew build`.
- [x] 5.3 Przejrzenie planu pod kątem kompletności.
- [x] 5.4 Przygotowanie instrukcji dla użytkownika jak przetestować działanie na telefonie.
