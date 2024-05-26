# StrandSøk

Dette er en applikasjon for å finne badeplasser i rundt om i norge og få informasjon om vann, temperatur, vær, og farevarsler. Appen er utviklet i Kotlin med Jetpack Compose for brukergrensesnitt. 

Dokumentasjon finner du [her](https://pages.github.uio.no/audunpo/team-40-docs/)

Arkitekturskisse finner du [her](https://github.uio.no/IN2000-V24/team-40/tree/main/src/Dataflyt.png)

Modelleringsskisse finner du [her](https://github.uio.no/IN2000-V24/team-40/blob/main/MODELING.md)

## Funksjoner

- **Home**: Viser en liste over badeplasser i nærheten med informasjon om lufttemperatur, vanntemperatur og værikon. Du kan trykke på en badeplass for å se detaljert informasjon om badeplassen.
- **Filter**: Filtrer badeplasser basert på avstand, fasiliteter og vanntemperatur.
- **Map**: Viser et kart med markører for badeplasser. Du kan trykke på en markør for å se detaljert informasjon om den badeplassen.
- **Location**: Viser detaljert informasjon om en badeplass, inkludert bølgehøyde, vannhastighet, lufttemperatur, nedbørsmengde og vindhastighet. Du kan også velge et bestemt tidspunkt for å se data for det tidspunktet.
- **Alert**: Viser en liste over aktive farevarsler for dagen.

## Biblioteker og API-er

- **Jetpack Compose** for brukergrensesnitt
- **Ktor** for HTTP-klienter
- **Gson** for JSON-parsing
- **Mapbox** for kartvisning
- **Accompanist** for håndtering av permissions
- **Coil** for asynkron bildehåndtering
- **Firebase** for testing
- **JUnit** for enhetstesting
- **Espresso** for enhetstesting
- **OceanForecast** for badevannsdata
- **LocationForecast** for værdata
- **MetAlerts** for farevarsler
- **Dokka** for dokumentasjon

## Installasjon

1. Klon dette repositoryet: `git clone https://github.uio.no/IN2000-V24/team-40.git`
2. Åpne prosjektet i Android Studio.
3. Bygg og kjør appen på en emulator eller en fysisk enhet.
    - Hvis emulator, husk å sette posisjon til et fornuftig sted på forhånd, for å kunne filtrere på avstand.

## Bidragsytere

- Mohammad Zuhur Afshar - mohamafs
- Mehmet Capa - mehmetc
- Audun Osborg - audunpo
- Thomas Anders Reed - thomaree
- Amardeep Singh - amardees
- Abdullah Numan Uskudar - abdullau

## Warnings

Unused Resources - 94 Warnings. Vi får denne fordi .png filer vi har i drawable ikke blir referert 
til direkte i koden. Måten vi aksesserer bildene er ved å bruke ved en string match fra LocationForecast
API-et. Siden IDE-en ikke fanger opp at vi referer til bildene direkte for vi opp denne feilen, og vi
har også derfor konkludert at dette er noe vi kan ignorere.

Obsolete Gradle Dependency - 8 Warnings. Alle disse kommer fordi det finnes nyere versjoner av gradle
dependencies som finnes, enn de vi bruker. Siden appen vår fungerer stabilt på de versjonene vi har
nå har vi valgt å ignorere dette. 

Android Lint: Usability - 94 Warnings. Denne oppstår fordi filer IDE-en anbefaler å kun a "density
independent" filer i drawable mappen. Siden bildene sin oppløsning ikke er problematisk for appen
har vi valgt å la disse stå og ikke eksplisitt definere deres oppløsning. 

Compose preview - 1 Warning. Denne oppstår fordi vi ikke har satt parameter for "label" for en
animateFloatAsState. Siden fraværet av en parameter her ikke har skapt noen problemer 
har vi valgt å ignorere dette.

Unstable API usage - 5 Warnings. Disse er alle relatert til samme linje med kode i settings.gradle.kts.
Disse oppstår fordi 'getRepositoriesMode()' er markert som ustabil. Siden vi ikke har opplevd noen
ustabilitet i egen testing har vi valgt å ignorere dette.

Unused declaration: 7 Warnings. 4 av disse kommer fra OnClick i FilterScreen, FilterScreenBackground og 
ReOpenAlert. Ved inspeksjon fant at onClick faktisk brukes og da vi prøvde å safe delete dette fikk vi beskjed om
at onClick er i bruk i funksjonene, så vi konkluderte dette med at det virker som at 
IDE-en gjør en feil og ignorerer derfor dette. Det samme er tilfellet for callback i Network.kt, den brukes
men IDE tror den ikke gjør det for en grunn.

Migration - 6 Warnings. Disse kommer av at vi bruker deprecated symboler. Siden vi ikke har funnet noen
erstatning for disse måtte vi bruke disse symbolene. 

Style Issues - 3 Warnings. Disse for vi fordi vi har boolean uttrykk som kan forenkles
vi velger å ikke endre dette siden måten vi har skrevet ting på synes vi er mer lesbart for
oss. 
