# AGENTS.md — CryptoTracker (Android, Java)

Ten plik zawiera kontekst i zasady dla agenta AI (Claude Code / inny agent) pracującego nad tym repozytorium. Czytaj go przed rozpoczęciem jakiejkolwiek pracy nad kodem.

## Opis projektu

Prosta aplikacja na Androida napisana w **Javie**, wyświetlająca kryptowaluty w 3 ekranach:

1. **Market (Lista + wyszukiwanie)** — lista aktualnych kryptowalut (nazwa, symbol, cena, zmiana %) z polem wyszukiwania filtrującym listę po nazwie/symbolu.
2. **Ulubione (Favorites)** — podzbiór kryptowalut oznaczonych przez użytkownika jako ulubione na ekranie 1. Dane muszą przetrwać restart aplikacji.
3. **Szczegóły (Details)** — szczegółowe dane wybranej kryptowaluty + możliwość ustawienia progu cenowego (threshold), po przekroczeniu którego użytkownik dostaje powiadomienie push + wykres tego coina.

## Stack technologiczny

- **Język:** Java (nie Kotlin)
- **Min SDK:** 24 (Android 7.0), **Target SDK:** najnowszy stabilny
- **Architektura:** MVVM (Activity/Fragment → ViewModel → Repository → DataSource)
- **UI:** XML layouts + View Binding (bez Jetpack Compose — projekt ma być prosty)
- **Nawigacja:** Bottom Navigation View + Jetpack Navigation Component (3 taby = 3 ekrany)
- **Sieć:** Retrofit2 + OkHttp3, Gson do parsowania JSON
- **API danych krypto:** CoinGecko API (https://www.coingecko.com/en/api/documentation) — darmowe, bez klucza API dla podstawowych endpointów (`/coins/markets`, `/search`, `/coins/{id}`)
- **Baza lokalna:** Room (przechowuje ulubione oraz ustawione thresholdy)
- **Tło / powiadomienia:** WorkManager z periodycznym `Worker`-em sprawdzającym cenę i porównującym z thresholdem; `NotificationManager` + kanał powiadomień do wysyłki alertu
- **Async:** LiveData + ViewModel (ewentualnie RxJava2, jeśli agent uzna za bardziej naturalne — ale nie mieszać obu w tym samym module)
- **Testy:** JUnit4 (logika, ViewModel, Repository), Espresso (podstawowe testy UI opcjonalnie)
- **Wykres**: MPAndroidChart

## Struktura pakietów

```
com.kacper.cryptotracker
├── data
│   ├── remote        // Retrofit API service, DTO
│   ├── local          // Room: Entity, Dao, Database
│   └── repository     // łączy remote + local, single source of truth
├── ui
│   ├── market         // Ekran 1: lista + wyszukiwanie
│   ├── favorites       // Ekran 2: ulubione
│   └── details         // Ekran 3: szczegóły + threshold
├── worker             // WorkManager: sprawdzanie cen w tle
├── notification       // budowa i wysyłka powiadomień
└── util               // formatery, extension-podobne helpery
```

## Zasady dla agenta

- Pisz kod w **Javie**, nie proponuj Kotlina, chyba że użytkownik wyraźnie o to poprosi.
- Preferuj proste, czytelne rozwiązania nad "sprytnymi" skrótami — to projekt na uczelnie, kod ma być łatwy do zrozumienia.
- Nie dodawaj bibliotek spoza wymienionego stacku bez wcześniejszej konsultacji z użytkownikiem.
- Każdy nowy ekran/feature: najpierw layout XML + minimalny szkielet Activity/Fragment, potem ViewModel, potem podłączenie danych. Nie pisz wszystkiego naraz w jednym dużym commicie.
- Threshold notification: logika porównania ceny ma być w `Worker`, nie w UI. UI tylko zapisuje ustawiony próg do Room.
- Zawsze obsługuj stany: loading / error / empty (szczególnie brak wyników wyszukiwania) w UI list.
- Klucz API (jeśli w przyszłości potrzebny wyższy tier CoinGecko) nigdy nie trafia do repo — trzymaj go w `local.properties` / `BuildConfig`.
- Commity: małe, opisowe, po angielsku, w stylu `feat: add favorites screen list adapter`.

## Jak dzialamy
0. Piszesz mi plan dzialania w `prompts/<Feature nazwa>/plan.md`, piszesz mi jakie pliki przeczytales po co one Ci sa potrzebne, co bedziesz tworzyl, kroki jakie podejmiesz zeby przejsc do tego kroku, ktore bedziesz pozniej aktualizowal z czasem trwania projektu
1. Po akceptacji zaczynasz implementacje zgodnie z planem dzialania 1) Piszesz testy za nim ruszysz kod 2) Piszesz kod
2. Zanim napiszesz gotowe, przejzyj plan jeszcze raz i sprawdz czy zaimplementowales wszystko zgodnie z `plan.md`
3. Po napisaniu gotowe piszesz mi co zrobiles i w jaki sposob moge to sprawdzic na swoim fizycznym telefonie

## Komendy

```bash
./gradlew build
./gradlew testDebugUnitTest
./gradlew installDebug
./gradlew lint
```

## Definicja "gotowe" (Definition of Done) dla feature'a

- Kod się kompiluje i przechodzi `./gradlew lint` bez nowych błędów.
- Podstawowy happy-path pokryty testem jednostkowym (ViewModel/Repository).
- UI obsługuje minimum 3 stany: ładowanie, błąd, sukces/pusta lista.
- Brak zahardkodowanych stringów w kodzie — wszystko w `strings.xml`.