# Projektplan samarbetande robotar
## Problemformulering
producera en robot som kan kartlägga ett koordinatsystem av lego vägplattor

Roboten ska även kunna samarbeta med andra robotar i samma "karta" för att snabbare täcka/kartlägga allt

## Hårdvarubeskrivning
### styrning
Vi har tänkt använda två motorer (t.ex TT-motorer) kopplade till däck med separata axlar. För att tillåta oss att kunna använda differentiell styrning (ofta kallad tank styrning), som ger en effektiv svängradie av 0.

Vi har tänkt driva motorerna med hjälp av antingen en motor shield, eller ett externt motor drivnings kort.

För att kunna få en exakt gissning av hur långt vi har åkt tänker vi även använda en tachometer (antingen hall-sensor eller optisk) kopplad direkt på motorns axel.

Med hjälp av tachometern kan vi även implementera relativt exakt hastighets kontroll med hjälp av en simpel PID-controller
### Visions system
Vi kommer använda Det vi blir tilldelade, mer info ska läggas till i den här delen  
I simplaste form är det en kamera monterad på roboten

## Beteende beskrivning
### Kartläggning
Roboten skall söka sig till och identifiera närliggande okända noder, ideellt vis ska även servern kunna tilldela olika zoner. Där en robot t.ex prioriterar att utforska noder med låga Y koordinater, medans en annan prioriterar noder med höga Y koordinater.
### Navigering
Roboten ska med t.ex A-star navigera till en arbirtär nod som den vet går att komma till genom att använda noderna som roboten både själv har hittat, såväl som noder som andra robotar har hittat.

Denna del kommer kartläggningen använda för att navigera mellan noder.
### Nod detektering
Genom Computer Vision systemet så kommer roboten försöka identifiera noder som är inom dens synfält. Och gissa sig fram till vilken position den har.
### Server kommunikation
Vi tänker att roboten kommer prata med en central server med hjälp av json skickat över MQTT.
Där kommer den skicka vart den är på väg, och om den hittar några nya noder.

Såväl som dens nuvarande status som t.ex gissad rotation och koordinat, hastighet osv.
# terminologi
## Karta
En samling av noder som bygger upp robotens "omgivning"
## nod
En "del" av kartan, om man har en karta som är t.ex 5x5 i storlek, så utgår den från att det finns 5x5 = 25 separata noder. så t.ex skulle noden hos koordinaten (x=1, y=4) kunna vara en högersväng.  
En nod måste inte vara en väg, den kan även vara helt tom eller okänd.
## Tachometer
Mätverktyg som mäter rotation
https://en.wikipedia.org/wiki/Tachometer
## PID-controller
kontroll system för att reglera styrning av en output med hjälp av ett feedback system
https://en.wikipedia.org/wiki/PID_controller