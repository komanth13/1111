package com.example.data.network

import com.example.domain.model.BarcodeLookupException
import com.example.domain.model.BarcodeLookupException.Reason
import java.io.ByteArrayInputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class OpenFoodFactsClientTest {
    private val barcode = "4820001234567"
    private fun response(nutrients: String = """"energy-kcal_100g":240,"proteins_100g":8,"fat_100g":2,"carbohydrates_100g":47""") =
        """{"status":"success","product":{"code":"$barcode","product_name_uk":"Хліб","brands":"Пекарня","nutriments":{$nutrients}}}"""

    @Test fun parsesBreadFromOnlineResponse() {
        val food = OpenFoodFactsClient().parseProduct(response(), barcode)!!
        assertEquals("Хліб — Пекарня", food.name)
        assertEquals(240f, food.calories, 0.01f)
        assertEquals(barcode, food.barcode)
    }

    private fun modernResponse(per: String = "100g", preparation: String = "as_sold") = """
        {"status":"success","product":{"code":"$barcode","product_name_uk":"Хліб",
        "nutrition":{"aggregated_set":{"per":"$per","preparation":"$preparation","nutrients":{
        "energy-kcal":{"value":240,"unit":"kcal"},"proteins":{"value":8,"unit":"g"},
        "fat":{"value":2000,"unit":"mg"},"carbohydrates":{"value":47,"unit":"g"}}}}}}
    """.trimIndent()

    @Test fun currentApiV3NutritionIsParsedAndUnitsAreNormalized() {
        val food = OpenFoodFactsClient().parseProduct(modernResponse(), barcode)!!
        assertEquals(240f, food.calories, 0f)
        assertEquals(8f, food.protein, 0f)
        assertEquals(2f, food.fat, 0f)
        assertEquals(47f, food.carbs, 0f)
    }

    @Test fun valuesPerServingAreNeverTreatedAsPer100Grams() {
        val error = assertThrows(BarcodeLookupException::class.java) {
            OpenFoodFactsClient().parseProduct(modernResponse(per = "serving"), barcode)
        }
        assertEquals(Reason.INCOMPLETE_NUTRITION, error.reason)
    }

    @Test fun preparedNutritionIsNeverMixedWithAsSoldValues() {
        val error = assertThrows(BarcodeLookupException::class.java) {
            OpenFoodFactsClient().parseProduct(modernResponse(preparation = "prepared"), barcode)
        }
        assertEquals(Reason.INCOMPLETE_NUTRITION, error.reason)
    }

    @Test fun energyInKilojoulesIsConvertedToKcal() {
        val food = OpenFoodFactsClient().parseProduct(response(
            """"energy-kj_100g":1004.16,"proteins_100g":"8,5","fat_100g":0,"carbohydrates_100g":47"""
        ), barcode)!!
        assertEquals(240f, food.calories, 0.02f)
        assertEquals(8.5f, food.protein, 0.01f)
        assertEquals(0f, food.fat, 0f)
    }

    @Test fun missingMacrosAreIncompleteAndNeverInventedAsZero() {
        val error = assertThrows(BarcodeLookupException::class.java) {
            OpenFoodFactsClient().parseProduct(response(""""energy-kcal_100g":240"""), barcode)
        }
        assertEquals(Reason.INCOMPLETE_NUTRITION, error.reason)
        assertEquals("Хліб — Пекарня", error.productName)
    }

    @Test fun invalidJsonIsNotProductNotFound() {
        val error = assertThrows(BarcodeLookupException::class.java) {
            OpenFoodFactsClient().parseProduct("<html>Server error</html>", barcode)
        }
        assertEquals(Reason.INVALID_RESPONSE, error.reason)
    }

    @Test fun returnedDifferentBarcodeIsRejected() {
        val error = assertThrows(BarcodeLookupException::class.java) {
            OpenFoodFactsClient().parseProduct(response().replace(barcode, "4829999999999"), barcode)
        }
        assertEquals(Reason.INVALID_RESPONSE, error.reason)
    }

    @Test fun numericNotFoundStatusReturnsNull() {
        assertNull(OpenFoodFactsClient().parseProduct("""{"status":0,"status_verbose":"product not found"}""", barcode))
    }

    @Test fun networkFailureIsExplicit() = runTest {
        try {
            OpenFoodFactsClient { throw IOException("offline") }.findByBarcode(barcode)
            fail("Expected network failure")
        } catch (error: BarcodeLookupException) {
            assertEquals(Reason.NETWORK, error.reason)
        }
    }

    @Test fun serviceFailureIsNotMissingProduct() = runTest {
        val connection = StubConnection(503)
        try {
            OpenFoodFactsClient { connection }.findByBarcode(barcode)
            fail("Expected service failure")
        } catch (error: BarcodeLookupException) {
            assertEquals(Reason.SERVICE, error.reason)
        }
        assertTrue(connection.closed)
    }

    @Test fun http404MeansMissingProduct() = runTest {
        val connection = StubConnection(404)
        assertNull(OpenFoodFactsClient { connection }.findByBarcode(barcode))
        assertTrue(connection.closed)
    }

    @Test fun successfulRequestUsesOnlineEndpointAndIdentity() = runTest {
        val connection = StubConnection(200, response())
        var requestedUrl = ""
        val food = OpenFoodFactsClient {
            requestedUrl = it.toString()
            connection
        }.findByBarcode(barcode)
        assertNotNull(food)
        assertTrue(requestedUrl.startsWith("https://world.openfoodfacts.org/api/v3/product/$barcode?"))
        assertTrue(connection.getRequestProperty("User-Agent").startsWith("SlimTrack/"))
        assertTrue(connection.closed)
    }

    @Test fun cancellationIsNeverConvertedToNotFound() = runTest {
        try {
            OpenFoodFactsClient { throw CancellationException("dismissed") }.findByBarcode(barcode)
            fail("Expected cancellation")
        } catch (_: CancellationException) {
            // Expected.
        }
    }

    private class StubConnection(private val status: Int, private val body: String = "") :
        HttpURLConnection(URL("https://world.openfoodfacts.org")) {
        var closed = false
        override fun getResponseCode() = status
        override fun getInputStream() = ByteArrayInputStream(body.toByteArray())
        override fun connect() = Unit
        override fun disconnect() { closed = true }
        override fun usingProxy() = false
    }
}
