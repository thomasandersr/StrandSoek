package no.uio.ifi.in2000.team40.havprosjekt.data.bathinglocations

//import org.junit.jupiter.api.Assertions.*
import no.uio.ifi.in2000.team40.havprosjekt.model.bathinglocations.Facilities
import org.junit.Test

import org.junit.Assert.*

class BathingLocationsRepositoryTest {
    private val repository = BathingLocationsRepository()


    @Test
    fun getBathingLocationsreturnsnonemptylist() {
        val locations = repository.getBathingLocations()
        assertTrue(locations.locations.isNotEmpty())
    }

    @Test
    fun `getBathingLocations returns expected number of locations`() {
        val locations = repository.getBathingLocations()
        assertEquals(24, locations.locations.size)
    }

    @Test
    fun `getBathingLocations includes specific location with correct details`() {
        val locations = repository.getBathingLocations()
        val specificLocation = locations.locations.find { it.name == "Huk" }

        assertNotNull(specificLocation)
        specificLocation?.let {
            assertEquals("Huk", it.name)
            assertTrue(it.facilities.containsAll(listOf(Facilities.BRYGGE, Facilities.KIOSK, Facilities.TOALETT)))
            assertEquals("https://dynamic-media-cdn.tripadvisor.com/media/photo-o/0f/f1/6e/de/huk.jpg?w=1200&h=-1&s=1", it.img)
            assertEquals(59.8988725, it.latitude, 0.000001)
            assertEquals(10.6713689, it.longitude, 0.000001)
        }
    }

    @Test
    fun `all locations have non-empty names and image URLs`() {
        val locations = repository.getBathingLocations()
        locations.locations.forEach {
            assertTrue(it.name.isNotEmpty())
            assertTrue(it.img.startsWith("http"))
        }
    }

    @Test
    fun `all locations have valid coordinates`() {
        val locations = repository.getBathingLocations()
        locations.locations.forEach {
            assertTrue(it.latitude >= 58.0 && it.latitude <= 60.0) // Rough bounding box for latitude in Oslo area
            assertTrue(it.longitude >= 9.0 && it.longitude <= 11.0) // Rough bounding box for longitude in Oslo area
        }
    }

    @Test
    fun `all locations have at least one facility`() {
        val locations = repository.getBathingLocations()
        assertTrue(locations.locations.all { it.facilities.isNotEmpty() })
    }

    @Test
    fun `locations contain expected facilities`() {
        val locations = repository.getBathingLocations()
        val facilitiesCount = locations.locations.flatMap { it.facilities }.distinct().size
        assertEquals(Facilities.values().size, facilitiesCount)
    }

    @Test
    fun `specific location facilities are correctly assigned`() {
        val locations = repository.getBathingLocations()
        val sorenga = locations.locations.find { it.name == "Sørenga sjøbad" }

        assertNotNull(sorenga)
        assertTrue(sorenga!!.facilities.containsAll(listOf(Facilities.BARNEVENNLIG, Facilities.TOALETT, Facilities.BADEVAKT, Facilities.BRYGGE)))
    }

    @Test
    fun `test unique names for all locations`() {
        val locations = repository.getBathingLocations().locations
        val uniqueNames = locations.map { it.name }.toSet()
        assertEquals("Each location should have a unique name",locations.size, uniqueNames.size)
    }

    @Test
    fun `test unique images for all locations`() {
        val locations = repository.getBathingLocations().locations
        val uniqueImages = locations.map { it.img }.toSet()
        assertEquals("Each location should have a unique image URL",locations.size, uniqueImages.size)
    }

    @Test
    fun `test all locations have valid facilities`() {
        val locations = repository.getBathingLocations().locations
        locations.forEach { location ->
            assertTrue("Location ${location.name} should have at least one facility",location.facilities.isNotEmpty() )
            location.facilities.forEach { facility ->
                assertTrue("Facility $facility in location ${location.name} is not recognized",Facilities.values().contains(facility))
            }
        }
    }

    @Test
    fun `test locations have expected facilities`() {
        val expectedFacilities = setOf(Facilities.BRYGGE, Facilities.KIOSK, Facilities.TOALETT)
        val locations = repository.getBathingLocations().locations
        assertTrue("At least one location should have all the expected facilities: $expectedFacilities",locations.any { location ->
            expectedFacilities.all { facility -> location.facilities.contains(facility) }
        } )
    }

    @Test
    fun `test filter locations by facility`() {
        val bathingLocations = repository.getBathingLocations().locations
        val locationsWithKiosk = bathingLocations.filter { it.facilities.contains(Facilities.KIOSK) }
        assertFalse("There should be locations with a KIOSK facility",locationsWithKiosk.isEmpty() )
    }

    @Test
    fun `test no negative coordinates for any location`() {
        val locations = repository.getBathingLocations().locations
        locations.forEach { location ->
                 assertTrue("Latitude should not be negative",location.latitude >= 0)
                 assertTrue("Longitude should not be negative",location.longitude >= 0)

        }
    }

    @Test
    fun `test location names are not blank`() {
        val locations = repository.getBathingLocations().locations
        locations.forEach { location ->
            assertNotEquals("", location.name.trim(), "Location names should not be blank")
        }
    }
}