# Modeling

### Use Case Diagram
```plantuml
@startuml
left to right direction
skinparam packageStyle rectangle

actor User

package "Application" {
usecase "Browse Home Screen" as BHS
usecase "View Weather Alerts" as VWA
usecase "Explore Location Details" as ELD
usecase "Filter Locations" as FL
usecase "Search Locations" as SL
usecase "Find Location on Map" as LP
}

User --> BHS : Launches & uses
User --> VWA : Taps on alerts
User --> ELD : Selects location
User --> FL : Uses filters
User --> SL : Searches locations
User --> LP : Locations on Map

@enduml
```

## Diagram Forklaring
Actorer:
Bruker: Den primære aktøren som samhandler med systemet.
Use Caser:
- Bla gjennom Home Screen: Brukeren samhandler med Home Screen for å se alle stedsnavn, bruke Search Bar og se en Alert Bar.
- Vis Værvarsler: Brukeren ser detaljerte varsler ved å gå til Alert Screen.
- Utforsk Posisjonsdetaljer: Brukeren viser detaljert informasjon om en posisjon, inkludert bilder og temperatur, på posisjonsskjermen.
- Filter Lokasjoner: Brukeren filtrerer lokasjoner basert på avstand og tilgjengelige fasiliteter.
- Brukeren kan finne lokasjoner på kart.
Relasjoner: Hvert brukstilfelle er direkte knyttet til brukeren, noe som indikerer at brukeren initierer og samhandler med disse funksjonene.
Dette diagrammet gir en klar visuell representasjon av nøkkelinteraksjonene i applikasjonen vår, i samsvar med use casene som er diskutert. Hvert use casene presenteres som en vei brukeren kan ta i applikasjonen, som gjenspeiler rekkefølgen og omfanget av interaksjoner.


## Use Case 1: Bla gjennom Home Screen

- Navn: Bla gjennom Home Screen
- Skuespiller: Bruker
- Beskrivelse: Brukeren blar gjennom Home Screen for å se alle stedsnavn, bruke en Search Bar for spesifikke steder og se en Alert Bar.
- Forutsetninger: Brukeren har applikasjonen installert og har åpnet Home Screen.
- Trigger: Applikasjonen åpnes.
- Hoved Flyt:
   1. Brukeren starter applikasjonen.
   2.  Home Screen lastes inn og viser alle stedsnavn.
   3. Alert Bar øverst viser de siste værvarslene.
   4. Brukeren bruker Search Bar for å finne et bestemt sted.
- Alternativer:
  Hvis ingen plassering samsvarer med søket, prøv en annen Location.
- Postbetingelser: Brukeren har bla gjennom Home Screen og benyttet søkefunksjonen.

# Klasse Diagram
```mermaid
classDiagram
class HomeScreen {
+displayLocations()
+showAlerts()
}
class AlertsBar {
+displayCurrentAlerts()
}
class SearchBar {
+searchLocation(input)
}
class Location {
+name
+description
}
class User {
+launchApp()
+search(input)
+viewLocations()
}

User o-- HomeScreen : uses ->
HomeScreen o-- AlertsBar : displays ->
HomeScreen o-- SearchBar : includes ->
HomeScreen "1" *-- "many" Location : contains ->
SearchBar "1" *-- "0..*" Location : searches ->
```

# Sekvensdiagram

```mermaid
sequenceDiagram
    actor User
    participant HS as HomeScreen
    participant AB as AlertsBar
    participant SB as SearchBar

    User->>HS: Launch application
    HS->>User: Display all location names
    HS->>AB: Show latest weather alerts
    loop Search for Locations
        User->>SB: Input search query
        SB->>HS: Lookup location
        alt Location found
            HS->>User: Display search results
        else Location not found
            SB->>User: Try another search
        end
    end
```
## Use Case 2: Utforsk posisjonsdetaljer

- Navn: Utforsk posisjonsdetaljer
- Skuespiller: Bruker
- Beskrivelse: Brukeren viser detaljer om et sted, inkludert navn, bilder, badetemperatur og kartikon .
- Forutsetninger: Brukeren har valgt en lokasjon fra Home Screen.
- Trigger: Brukeren trykker på et stedsnavn.
- Hoved Flyt:
  1. Brukeren velger en Lokasjon ved å trykke på navnet.
  2. Applikasjonen går over til Location Screen.
  3. Location Screen viser stedsnavn, bilder, badetemperatur og kartikon.
- Postbetingelser: Brukeren har utforsket detaljene til et sted.

# Sekvensdiagram

```mermaid
sequenceDiagram
participant User as User
participant App as Application
participant HS as HomeScreen
participant LS as LocationScreen

    User->>HS: Tap on location name
    HS->>App: Navigate to Location Screen
    App->>LS: Display location details
    LS-->>User: Display location details
```
# Klassediagram

```mermaid
classDiagram
    class User {
        +tapOnLocationName()
    }
    class Application {
        +navigateToLocationScreen()
    }
    class HomeScreen {
        +displayLocationNames()
    }
    class Location {
        -name
        -temperature
        -mapIcon
        -photo
    }
    class LocationScreen {
        -location: Location
        
    }

    User --> Application : uses
    Application --> HomeScreen : uses
    Application --> LocationScreen : uses
    LocationScreen "1" *-- "1" Location : contains
```
## Use Case 3: Filtrer Lokasjoner

- Navn: Filtrer Lokasjoner
- Skuespiller: Bruker
- Beskrivelse: Brukeren filtrerer steder basert på avstand og tilgjengelige fasiliteter.
- Forutsetninger: Brukeren er på Home Screen med plasseringsalternativer vist.
- Trigger: Brukeren åpner Filter Bar.
- Hoved Flyt:
  1. Brukeren får tilgang til filter alternativene på Home Screen.
  2. Brukeren setter filterkriterier for avstand og fasiliteter.
  3. Applikasjonen filtrerer plasseringsalternativene basert på de valgte kriteriene.
  4. Applikasjonen viser de filtrerte lokasjon resultatene til brukeren.
- Alternativer:
  -Hvis ingen steder oppfyller kriteriene, kan brukeren prøve andre kriterier.
- Postbetingelser: Brukeren har en liste over steder som samsvarer med deres filter preferanser.

# Sekvensdiagram

```mermaid
sequenceDiagram
participant User as User
participant App as Application
participant HS as HomeScreen

    User->>HS: Open filter options
    HS->>App: Set filter criteria
    alt Locations meet criteria
        App->>App: Filter location options
        App-->>HS: Display filtered location results
    else No locations meet criteria
        App-->>HS: Notify no matching locations
        HS->>User: Try other criteria
    end
```

# Klassediagram

```mermaid
classDiagram
class User {
+openFilterOptions()
+setFilterCriteria()
+tryOtherCriteria()
}
class Application {
+filterLocationOptions()
}
class HomeScreen {
+displayFilteredLocationResults()
}

User --> Application : uses
Application --> HomeScreen : uses
```
# Aktivitetsdiagram

```mermaid
graph TD
    A[User] -->|1. Open filter options| B((Home Screen))
    B -->|2. Set filter criteria| C[Application]
    C -->|3. Filter location options| D((Home Screen))
    D -->|4a. Locations meet criteria| E{Display filtered location results}
    D -->|4b. No locations meet criteria| F[User]
    F -->|5. Try other criteria| B
```