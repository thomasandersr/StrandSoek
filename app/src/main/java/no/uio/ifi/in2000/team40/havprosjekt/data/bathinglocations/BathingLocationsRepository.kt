package no.uio.ifi.in2000.team40.havprosjekt.data.bathinglocations

import android.util.Log
import no.uio.ifi.in2000.team40.havprosjekt.model.bathinglocations.BathingLocations
import no.uio.ifi.in2000.team40.havprosjekt.model.bathinglocations.BathingLocations.Location
import no.uio.ifi.in2000.team40.havprosjekt.model.bathinglocations.Facilities
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Class for administrating incoming data from DataSource(s) (but in this case none).
 * Mainly for providing BathingLocations in both modified, and un-modified states.
 */
class BathingLocationsRepository {
    /**
     * Helper method for calculating and saving distances between an origin point; 'coords' and
     * a list of BathingLocations
     *
     * @param coords Origin reference point.
     * @param bathingLocations List of BathingLocations, each containing a latitude and longitude.
     *
     * @return A sorted map containing the name of each locations to the distance from the origin point.
     */
    private fun coordsDistConv(coords: Map<String, Double>, bathingLocations: List<Location>): Map<String, Double> {
        // kommenter inn denne linjen for å override posisjon til emulator:
        // val coords = mapOf("lat" to 59.9375174, "lon" to 10.7552997)

        val map = mutableMapOf<String, Double>()

        bathingLocations.forEach {
            try {
                val lat2 = it.latitude
                val lon2 = it.longitude

                val lat1 = coords["lat"]!!.toDouble()
                val lon1 = coords["lon"]!!.toDouble()

                // Haversine formula, les mer om den her: https://www.movable-type.co.uk/scripts/latlong.html
                // målet er å finne korteste avstand mellom to punkter (koordinater) på en sfære, i dette tilfellet Jorda
                val r = 6367e3

                val phi1 = Math.toRadians(lat1)
                val phi2 = Math.toRadians(lat2)

                val deltaPhi = Math.toRadians(lat2 - lat1)
                val deltaLambda = Math.toRadians(lon2 - lon1)

                val a = sin(deltaPhi/2.0).pow(2) + cos(phi1) * cos(phi2) * sin(deltaLambda / 2.0).pow(2)
                val c = 2 * atan2(sqrt(a), sqrt(1-a))

                val d = (r * c) / 1000 // km

                Log.i("sortCoords", "distanceKm: $d")
                map[it.name] = d
            } catch (e: Exception) { Log.e("sortCoords", "ERROR: $e") }
        }

        // konverterer map til liste og tilbake for å kunne sortere på values istedenfor keys
        val sMap = map.toList().sortedBy { it.second }.toMap()
        Log.i("sortCoords", "Sorted: $sMap")

        return sMap
    }

    /**
     * Method for calculating and saving distances between an origin point; 'coords' and
     * a list of BathingLocations
     *
     * @param chosenDist Value containing the max distance allowed.
     * @param coords Map of coords, each containing a name and coords.
     *
     * @return A list containing appropriate BathingLocations.
     */
    fun getBathingLocationsByDistance(chosenDist: Float, coords: Map<String, Double>?): List<Location>? =
        if (chosenDist==0f || coords==null) null
        else coordsDistConv(coords, getBathingLocations().locations)
            .filter { it.value <= chosenDist }
            .map { getLocationByName(it.key)!! }

    /**
     * Method for including BathingLocations that each has a list of specific facilities, exclusively.
     *
     * @param facilitites List of facilities required.
     *
     * @return A filtered list of BathingLocations containing only specific facilities based on
     * the original list provided by the parameter.
     */
    fun getBathinglocationsByFacilities(facilitites: List<Facilities>): List<Location> =
        getBathingLocations().locations.filter { it.facilities.containsAll(facilitites) }

    /**
     * Method for finding a BathingLocation that has a specific name.
     *
     * @param name Name of the specific facility.
     *
     * @return The first location with the name specified. If none, then null.
     */
    fun getLocationByName(name: String): Location? =
        getBathingLocations().locations.firstOrNull { it.name == name }

    /**
     * Method for retrieving all BathingLocations.
     *
     * @return A list of all BathingLocations.
     */
    fun getBathingLocations(): BathingLocations {
        /* data hentet herfra:
          - https://www.visitoslo.com/no/oslo-for-deg/sommer/bading-i-byen/
          - https://www.oslo.kommune.no/natur-kultur-og-fritid/tur-og-friluftsliv/badeplasser-og-temperaturer/
         */
        return BathingLocations(
            locations = listOf(
                // Bergen, Trondheim, Tromsø
                Location(
                    name = "Grønevika badestrand",
                    facilities = listOf(
                        Facilities.BARNEVENNLIG,
                        Facilities.BRYGGE,
                        Facilities.TILPASSET,
                        Facilities.TOALETT
                    ),
                    img = "https://www.bergen.kommune.no/api/rest/bilder/V20091476?scaleWidth=1400&cropX1=0&cropY1=50&cropX2=900&cropY2=560",
                    latitude = 60.2557837,
                    longitude = 5.1359323
                ),
                Location(
                    name = "Helleneset",
                    facilities = listOf(
                        Facilities.BARNEVENNLIG,
                        Facilities.TILPASSET,
                        Facilities.BRYGGE
                    ),
                    img = "https://www.bergen.kommune.no/api/rest/bilder/V20041646?scaleWidth=1400",
                    latitude = 60.435539,
                    longitude = 5.1840409
                ),
                Location(
                    name = "Toppesanden",
                    facilities = listOf(Facilities.GRILL, Facilities.TOALETT),
                    img = "https://www.bergen.kommune.no/api/rest/bilder/V20091981?scaleWidth=1400&cropX1=0&cropY1=201&cropX2=3873&cropY2=2392",
                    latitude = 60.4928223,
                    longitude = 5.1476127
                ),
                Location(
                    name = "Korsvika",
                    facilities = listOf(Facilities.TILPASSET, Facilities.GRILL),
                    img = "https://images.citybreakcdn.com/image.aspx?ImageId=4662702&width=1000&height=600&fitaspect=1",
                    latitude = 63.4501544,
                    longitude = 10.4097456
                ),
                Location(
                    name = "Munkholmen",
                    facilities = listOf(Facilities.GRILL, Facilities.TOALETT),
                    img = "https://visittrondheim.no/wp-content/uploads/2021/05/Munkholmen.jpg",
                    latitude = 63.4518825,
                    longitude = 10.3814928
                ),
                Location(
                    name = "Sjøbadet",
                    facilities = listOf(Facilities.TILPASSET, Facilities.BRYGGE, Facilities.BRYGGE),
                    img = "https://vcdn.polarismedia.no/0725c4f9-ac2d-4711-96c1-ed06bdb5eea8?fit=crop&h=600&q=80&tight=false&w=1000",
                    latitude = 63.435685,
                    longitude = 10.3592204
                ),
                Location(
                    name = "Telegrafbukta",
                    facilities = listOf(Facilities.GRILL, Facilities.KIOSK, Facilities.BRYGGE),
                    img = "https://www.linnsreise.no/wp-content/uploads/2023/07/Telegrafbukta-2023-scaled.jpg",
                    latitude = 69.6298984,
                    longitude = 18.8804589
                ),
                Location(
                    name = "Grøtfjord",
                    facilities = listOf(Facilities.TOALETT),
                    img = "https://www.linnsreise.no/wp-content/uploads/2023/07/Grotfjorden-oveenfra.jpg",
                    latitude = 69.7789653,
                    longitude = 18.5274836
                ),
                Location(
                    name = "Sommarøya",
                    facilities = listOf(),
                    img = "https://www.linnsreise.no/wp-content/uploads/2023/07/steinsvika-sommaroy-scaled.jpg",
                    latitude = 69.6337869,
                    longitude = 18.0013544
                ),
                //
                Location(
                    name = "Badstuene på Langkaia",
                    facilities = listOf(),
                    img = "https://g.acdn.no/obscura/API/dynamic/r1/ece5/tr_1000_2000_s_f/0000/avio/2021/7/15/16/AVISA%2BOSLO-BADSTUE%2BBADEMASCHINEN-15.07.2021-17.jpg?chk=97F3F9",
                    latitude = 59.9080322,
                    longitude = 10.7494909
                ),
                Location(
                    name = "Badstuene på Sukkerbiten",
                    facilities = listOf(),
                    img = "https://www.godeidrettsanlegg.no/sites/default/files/bilder/Munken%20og%20Munch.jpg",
                    latitude = 59.904931,
                    longitude = 10.7530656
                ),
                Location(
                    name = "Bekkelagsbadet",
                    facilities = listOf(
                        Facilities.BARNEVENNLIG,
                        Facilities.GRILL,
                        Facilities.KIOSK,
                        Facilities.TILPASSET,
                        Facilities.TOALETT,
                        Facilities.BRYGGE
                    ),
                    img = "https://img.oslo.kommune.no/_prod_/13332477-1561984495/Tjenester%20og%20tilbud/Natur%2C%20kultur%20og%20fritid/Badeplasser%20og%20temperaturer/Bekkelagsbadet/Galleri/Hovedbilde.JPG?w=792&aspect_ratio=16%3A9&gravity=face",
                    latitude = 59.8802127,
                    longitude = 10.7652386
                ),
                Location(
                    name = "Bekkensten",
                    facilities = listOf(Facilities.BRYGGE),
                    img = "https://www.dnt.no/globalassets/fotoware/2023/11/stian-pa-stien-ekspedisjon-strandsone-118.jpg?width=600&amp;height=600&amp;rmode=crop&amp;format=webp",
                    latitude = 59.791936,
                    longitude = 10.734552
                ),
                Location(
                    name = "Bestemorstranda",
                    facilities = listOf(Facilities.TOALETT, Facilities.BRYGGE),
                    img = "https://www.oslofjorden.com/bilde_badestrand/akershus/bestemorstranda_bunnefjorden_oversikt.JPG",
                    latitude = 59.8270535,
                    longitude = 10.7592003
                ),
                Location(
                    name = "Bygdøy sjøbad",
                    facilities = listOf(),
                    img = "https://i0.wp.com/reisekick.no/wp-content/uploads/2020/10/Bygdoy-strand.jpg?resize=960%2C720&ssl=1",
                    latitude = 59.9107295,
                    longitude = 10.6659127
                ),
                Location(
                    name = "Fiskevollbukta",
                    facilities = listOf(),
                    img = "https://www.oslofjorden.com/bilde_badestrand/oslo/fiskevollbukta_badeplass_info2.JPG",
                    latitude = 59.8425427,
                    longitude = 10.7733261
                ),
                Location(
                    name = "Gressholmen",
                    facilities = listOf(Facilities.TOALETT),
                    img = "https://www.oslofjorden.com/bilde_badestrand/oslo/gressholmen_badeplass_oslo_info1.JPG",
                    latitude = 59.8844439,
                    longitude = 10.7202778
                ),
                Location(
                    name = "Hovedøya",
                    facilities = listOf(
                        Facilities.BARNEVENNLIG,
                        Facilities.KIOSK,
                        Facilities.TOALETT
                    ),
                    img = "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b8/Hoved%C3%B8ya_aerial.jpg/1200px-Hoved%C3%B8ya_aerial.jpg",
                    latitude = 59.895146,
                    longitude = 10.732618
                ),
                Location(
                    name = "Huk",
                    facilities = listOf(
                        Facilities.BADEVAKT,
                        Facilities.GRILL,
                        Facilities.KIOSK,
                        Facilities.TOALETT
                    ),
                    img = "https://dynamic-media-cdn.tripadvisor.com/media/photo-o/0f/f1/6e/de/huk.jpg?w=1200&h=-1&s=1",
                    latitude = 59.8988725,
                    longitude = 10.6713689
                ),
                Location(
                    name = "Hvervenbukta",
                    facilities = listOf(
                        Facilities.BADEVAKT,
                        Facilities.BARNEVENNLIG,
                        Facilities.TILPASSET,
                        Facilities.TOALETT
                    ),
                    img = "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a5/Hvervenbukta_-_2014-04-28_at_19-31-25.jpg/1200px-Hvervenbukta_-_2014-04-28_at_19-31-25.jpg",
                    latitude = 59.8329619,
                    longitude = 10.7712197
                ),
                Location(
                    name = "Håøya",
                    facilities = listOf(Facilities.TOALETT),
                    img = "https://www.visitgreateroslo.com/globalassets/bilder-akershus/follo/frogn-kommune-vdo-bilder/2022---var/haoya-foto-paul-hughson-visitdrobakoscarsborg.no_5.jpg",
                    latitude = 59.6979625,
                    longitude = 10.5740849
                ),
                Location(
                    name = "Ingierstrand",
                    facilities = listOf(
                        Facilities.BADEVAKT,
                        Facilities.KIOSK,
                        Facilities.TILPASSET,
                        Facilities.TILPASSET,
                        Facilities.TOALETT
                    ),
                    img = "https://assets.simpleviewcms.com/simpleview/image/fetch/c_limit,h_1200,q_75,w_1200/https://media.newmindmedia.com/TellUs/image/%3Ffile%3DScreen_Shot_2018-11-12_at_12.09.12_1930375555.png%26dh%3D520%26dw%3D800%26t%3D4",
                    latitude = 59.8183055,
                    longitude = 10.7490928
                ),
                Location(
                    name = "Katten",
                    facilities = listOf(
                        Facilities.BARNEVENNLIG,
                        Facilities.GRILL,
                        Facilities.TOALETT
                    ),
                    img = "https://img.oslo.kommune.no/_prod_/13492517-1697799745/Tjenester%20og%20tilbud/Natur%2C%20kultur%20og%20fritid/Badeplasser%20og%20temperaturer/Katten/Galleri/Katten%204%20-%20Bymilj%C3%B8etaten.jpg?w=792&aspect_ratio=16%3A9&gravity=face",
                    latitude = 59.8552145,
                    longitude = 10.7832094
                ),
                Location(
                    name = "Langøyene",
                    facilities = listOf(
                        Facilities.BARNEVENNLIG,
                        Facilities.KIOSK,
                        Facilities.TILPASSET,
                        Facilities.TOALETT,
                        Facilities.BRYGGE
                    ),
                    img = "https://tellusdmsmedia.newmindmedia.com/wsimgs/DJI_0749_112534413.jpg[ProductImage][4D037919A6C329D04191FE6E4EBC]",
                    latitude = 59.870694,
                    longitude = 10.7217348
                ),
                Location(
                    name = "Nordstrand bad",
                    facilities = listOf(Facilities.TOALETT, Facilities.BRYGGE),
                    img = "https://img.oslo.kommune.no/_prod_/13494546-1699019281/Tjenester%20og%20tilbud/Natur%2C%20kultur%20og%20fritid/Badeplasser%20og%20temperaturer/Nordstrand%20bad/Galleri/Nordstrand%20bad4.jpg%20%2816_10%29.jpg?w=792&aspect_ratio=16%3A9&gravity=face",
                    latitude = 59.8688127,
                    longitude = 10.7819728
                ),
                Location(
                    name = "Operastranda",
                    facilities = listOf(Facilities.TOALETT),
                    img = "https://assets.simpleviewcms.com/simpleview/image/fetch/c_fill,h_1080,w_1920/f_jpg/q_65/https://media.newmindmedia.com/TellUs/image/%3Ffile%3Doperastranda-mot-operaen_218887326.jpg&dh%3D600&dw%3D800&cropX%3D146&cropY%3D9&cropH%3D765&cropW%3D1020&t%3D4",
                    latitude = 59.9065786,
                    longitude = 10.7526328
                ),
                Location(
                    name = "Paradisbukta",
                    facilities = listOf(),
                    img = "https://listerfriluft.no/media/83804/img_7429rm.jpg?anchor=center&mode=crop&width=768&height=432&rnd=133161849450000000",
                    latitude = 59.9019505,
                    longitude = 10.6656571
                ),
                Location(
                    name = "Rambergøya",
                    facilities = listOf(),
                    img = "https://www.oslofjorden.com/bilde_badestrand/oslo/rambergoeya_badeplass_oslo_info3.JPG",
                    latitude = 59.8825479,
                    longitude = 10.7185014
                ),
                Location(
                    name = "Skinnerbukta (Malmøya)",
                    facilities = listOf(),
                    img = "https://www.norskhavneguide.no/static/images/skinnerbukta-0hr0qh-1200.webp",
                    latitude = 59.8673255,
                    longitude = 10.7538509
                ),
                Location(
                    name = "Sollerudstranda",
                    facilities = listOf(),
                    img = "https://landskapsarkitektur.no/prosjekter/sollerudstranda?iid=233030&pid=NLA-Prosjekt-Bilder.NLA-ProsjektBilde-Prosjektbilde&r_n_d=45795_&adjust=1&x=320&y=220&from=0&zmode=fill",
                    latitude = 59.9138771,
                    longitude = 10.6471785
                ),
                Location(
                    name = "Solvikbukta på Malmøya",
                    facilities = listOf(
                        Facilities.KIOSK,
                        Facilities.TILPASSET,
                        Facilities.TOALETT,
                        Facilities.BRYGGE
                    ),
                    img = "https://www.norskhavneguide.no/static/images/skinnerbukta-0hr0qh-1200.webp",
                    latitude = 59.8930334,
                    longitude = 10.5492851
                ),
                Location(
                    name = "Sørenga sjøbad",
                    facilities = listOf(
                        Facilities.BADEVAKT,
                        Facilities.BARNEVENNLIG,
                        Facilities.KIOSK,
                        Facilities.TILPASSET,
                        Facilities.TOALETT,
                        Facilities.BRYGGE
                    ),
                    img = "https://assets.simpleviewcms.com/simpleview/image/fetch/c_fill,h_1080,w_1920/f_jpg/q_65/https://media.newmindmedia.com/TellUs/image/%3Ffile%3DS_renga_sj_bad_2Katrine_Lunke_593782387.jpg&dh%3D534&dw%3D800&cropX%3D0&cropY%3D0&cropH%3D4910&cropW%3D7360&t%3D4",
                    latitude = 59.9011363,
                    longitude = 10.751058
                ),
                Location(
                    name = "Ulvøya",
                    facilities = listOf(),
                    img = "https://tellusdmsmedia.newmindmedia.com/wsimgs/Tjuvholmen_Bystrand_VISITOSLO_Torgny_Gustafsson_973882663.jpg",
                    latitude = 59.8695107,
                    longitude = 10.771873
                ),
                Location(
                    name = "Tangen badestrand",
                    facilities = listOf(),
                    img = "https://www.fredrikstad.kommune.no/globalassets/bilder/kmb/miljo-og-landbruk/badeplasser---gallerier/tangen/tangen7.jpg",
                    latitude = 59.14037284157208,
                    longitude = 10.953739082199487
                ),
                Location(
                    name = "Foten badeplass",
                    facilities = listOf(),
                    img = "https://res.cloudinary.com/ssp/image/fetch/w_710,c_fill/https://www.fredrikstad.kommune.no//globalassets/bilder/kmb/miljo-og-landbruk/badeplasser---gallerier/foten/foten-6.jpg",
                    latitude = 59.17113705410745,
                    longitude = 10.828209484321992
                ),
                Location(
                    name = "Fuglevikstand",
                    facilities = listOf(),
                    img = "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ffuglevikstranda.no%2Fwp-content%2Fuploads%2F2022%2F11%2Fforside-3.jpg&f=1&nofb=1&ipt=e9ac5ebd0cba8b573e940826aca19178315dc140edb1aa3b380a528de5ed1642&ipo=images",
                    latitude = 59.18912769898772,
                    longitude = 10.945551687782135
                ),
                Location(
                    name = "Enhuskilen",
                    facilities = listOf(),
                    img = "https://www.fredrikstad.kommune.no/globalassets/bilder/kmb/miljo-og-landbruk/badeplasser---gallerier/enhus/enhuus-2.jpg",
                    latitude = 59.18575521687046,
                    longitude = 10.908972688790987
                ),
                Location(
                    name = "Bendiksbukta",
                    facilities = listOf(),
                    img = "https://assets.simpleviewcms.com/simpleview/image/fetch/c_limit,h_1200,q_75,w_1200/https://media.newmindmedia.com/TellUs/image/%3Ffile%3DEC12405F760034D7287794D9597E7A1FC787D7F7.jpg%26dh%3D532%26dw%3D800%26cropX%3D0%26cropY%3D158%26cropH%3D2172%26cropW%3D3264%26t%3D4",
                    latitude = 58.14071651665333,
                    longitude = 8.00265393731935
                ),
                Location(
                    name = "Buøyna",
                    facilities = listOf(),
                    img = "https://lh5.googleusercontent.com/p/AF1QipOrWtIM4LDWkOfj2Qtl0umNx29M6-Pi3AWc3tBY=w480-h300-k-n-rw",
                    latitude = 58.19237572716935,
                    longitude = 8.075285187469499
                ),
                Location(
                    name = "Bystranda",
                    facilities = listOf(),
                    img = "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fassets.simpleviewcms.com%2Fsimpleview%2Fimage%2Ffetch%2Fc_limit%2Ch_1200%2Cq_75%2Cw_1200%2Fhttps%3A%2F%2Fmedia.newmindmedia.com%2FTellUs%2Fimage%2F%253Ffile%253DPalmer_p_bystranda_1786689711.JPG%2526dh%253D800%2526dw%253D800%2526t%253D4&f=1&nofb=1&ipt=dc22a93f1eb5ba01af6ebbc5619292dde2a2e47c035e91b64cbec2c835e3f2b3&ipo=images",
                    latitude = 58.146117609470544,
                    longitude = 8.007074290782567
                ),
                Location(
                    name = "Gamlestranda",
                    facilities = listOf(),
                    img = "https://lh5.googleusercontent.com/p/AF1QipOYHB_QDWNpLZPFmxyhit3h_iJ-bSfH-sp8bRwo=w408-h306-k-no",
                    latitude = 58.07853509430875,
                    longitude = 8.017397479221716
                ),
                Location(
                    name = "Godalen strand",
                    facilities = listOf(),
                    img = "https://www.stavanger.kommune.no/siteassets/kultur-og-fritid/parker-og-friomrader/badeplasser-friomrader-parker/rosenli/20220616-img_0664.jpg?width=1920&height=1080&transform=DownFit&h=26a7d2ca3b5b7512a6bbac565b0199799fe489b2",
                    latitude = 58.954067598203125,
                    longitude = 5.75672611652455
                ),
                Location(
                    name = "Sjøbadet",
                    facilities = listOf(),
                    img = "https://assets.simpleviewcms.com/simpleview/image/fetch/c_fill,h_1080,w_1920/f_jpg/q_65/https://res.cloudinary.com/djew0njor/image/upload/v1625829917/GYmIHu2McG7i2zlp2YlE9.jpg?_a=BATCtdAA0",
                    latitude = 58.94735184488721,
                    longitude = 5.570332286475677
                ),
                Location(
                    name = "Vaulen badeplass",
                    facilities = listOf(),
                    img = "https://www.stavanger.kommune.no/siteassets/kultur-og-fritid/parker-og-friomrader/badeplasser-friomrader-parker/vaulen/20220616-img_0694.jpg?width=1280&height=600&transform=DownFit&h=cb9a0c82c10b9442f95cd5643eca7859005690c2",
                    latitude = 58.92597432235386,
                    longitude = 5.747490919194079
                ),
                Location(
                    name = "Holmavika badeplass",
                    facilities = listOf(),
                    img = "https://delivery.twentythree.com/11513685/28086013/large?revision=4&domain=video.tvvest.no&Expires=1715490000&Signature=cQqqxYhp39LjYar9A1WpuKRTUr%7eP0NW9tG%2dGSviVtyz1Gb0SPOoUHzzmJcsL7SpHEcG5uQZwvka0sAHBqCqNX0Il8b9U8JZNLKuqFVh9XKGDWXLiS2EzxWa16lM5WvJJ555QSxlkyCn3dQikX%7eTV%2dcbA7v71VYz3mNUlg9jqlnOeEUerw479PujNXWFfLxevz4a5l6VPFIvg4iXPVbuwM8msRx1rN7j6fwk6C6u5laQWn97kxwnDivhgincpks3dFrnamUewTjIwF7fclTqdZuMSTZD2EvMIBHmNaHLJaKO8xphW9P4gArkWZQHRrWAp3ACW7wFFpwwkQsnSsEUkRQ%5f%5f&Key-Pair-Id=K2RKIY3YYBD5LB",
                    latitude = 58.88745521487193,
                    longitude = 5.775742868177744
                ),
            )
        )
    }
}