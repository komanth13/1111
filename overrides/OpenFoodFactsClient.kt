package com.example.data.network

import com.example.BuildConfig
import com.example.data.model.FoodItem
import com.example.domain.model.BarcodeLookupException
import com.example.domain.model.BarcodeLookupException.Reason
import com.example.util.BarcodeUtils
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/** Online product lookup; Room is an offline cache, not the product catalogue. */
class OpenFoodFactsClient(
    private val connectionFactory: (URL) -> HttpURLConnection = {
        it.openConnection() as HttpURLConnection
    }
) : RemoteFoodDataSource {

    override suspend fun findByBarcode(barcode: String): FoodItem? = withContext(Dispatchers.IO) {
        val code = BarcodeUtils.normalize(barcode)
        if (code.length !in setOf(8, 12, 13, 14)) return@withContext null

        val fields = listOf(
            "code", "product_name", "product_name_uk", "product_name_ru", "product_name_en",
            "brands", "categories", "categories_tags", "nutriments", "serving_quantity",
            "serving_quantity_unit", "serving_size"
        ).joinToString(",")
        val endpoint = URL(
            "https://world.openfoodfacts.org/api/v3/product/$code" +
                "?product_type=food&cc=ua&fields=$fields"
        )

        var connection: HttpURLConnection? = null
        try {
            currentCoroutineContext().ensureActive()
            connection = connectionFactory(endpoint).apply {
                requestMethod = "GET"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                instanceFollowRedirects = true
                setRequestProperty(
                    "User-Agent",
                    "SlimTrack/${BuildConfig.VERSION_NAME} (https://github.com/komanth13/1111)"
                )
                setRequestProperty("Accept", "application/json")
            }
            val status = connection.responseCode
            currentCoroutineContext().ensureActive()
            if (status == 404) return@withContext null
            if (status !in 200..299) throw BarcodeLookupException(Reason.SERVICE)
            val json = connection.inputStream.bufferedReader().use { it.readText() }
            currentCoroutineContext().ensureActive()
            parseProduct(json, code)
        } catch (error: CancellationException) {
            throw error
        } catch (error: BarcodeLookupException) {
            throw error
        } catch (error: IOException) {
            currentCoroutineContext().ensureActive()
            throw BarcodeLookupException(Reason.NETWORK, cause = error)
        } finally {
            connection?.disconnect()
        }
    }

    internal fun parseProduct(json: String, requestedBarcode: String): FoodItem? {
        val root = try {
            JSONObject(json)
        } catch (error: org.json.JSONException) {
            throw BarcodeLookupException(Reason.INVALID_RESPONSE, cause = error)
        }
        if (root.opt("status") == 0 || root.optJSONObject("result")?.optString("id") == "product_not_found") {
            return null
        }
        val product = root.optJSONObject("product")
            ?: throw BarcodeLookupException(Reason.INVALID_RESPONSE)
        val returnedCode = BarcodeUtils.normalize(product.optString("code"))
        if (returnedCode.isNotBlank() &&
            BarcodeUtils.lookupCandidates(requestedBarcode).none { it == returnedCode }) {
            throw BarcodeLookupException(Reason.INVALID_RESPONSE)
        }

        val productName = firstNonBlank(
            product.optString("product_name_uk"),
            product.optString("product_name_ru"),
            product.optString("product_name_en"),
            product.optString("product_name")
        ) ?: throw BarcodeLookupException(Reason.INCOMPLETE_NUTRITION)

        val brands = product.optString("brands").trim()
        val displayName = when {
            brands.isBlank() -> productName
            productName.contains(brands, ignoreCase = true) -> productName
            else -> "$productName — $brands"
        }

        val nutriments = product.optJSONObject("nutriments")
            ?: throw BarcodeLookupException(Reason.INCOMPLETE_NUTRITION, displayName)
        // Some labels publish energy only in kJ. Missing macros must never become zero.
        val calories = nutriments.floatOrNull("energy-kcal_100g")
            ?: nutriments.floatOrNull("energy-kj_100g")?.div(4.184f)
            ?: nutriments.floatOrNull("energy_100g")?.takeIf {
                nutriments.optString("energy_unit").equals("kJ", ignoreCase = true)
            }?.div(4.184f)
            ?: throw BarcodeLookupException(Reason.INCOMPLETE_NUTRITION, displayName)
        val protein = nutriments.floatOrNull("proteins_100g")
            ?: throw BarcodeLookupException(Reason.INCOMPLETE_NUTRITION, displayName)
        val fat = nutriments.floatOrNull("fat_100g")
            ?: throw BarcodeLookupException(Reason.INCOMPLETE_NUTRITION, displayName)
        val carbs = nutriments.floatOrNull("carbohydrates_100g")
            ?: throw BarcodeLookupException(Reason.INCOMPLETE_NUTRITION, displayName)
        if (!nutritionLooksUsable(calories, protein, fat, carbs)) {
            throw BarcodeLookupException(Reason.INCOMPLETE_NUTRITION, displayName)
        }

        // FoodItem stores grams. Do not treat a volume serving (ml) as grams.
        val serving = if (product.optString("serving_quantity_unit").equals("g", true)) {
            product.floatOrNull("serving_quantity")
                ?.takeIf { it.isFinite() && it > 0f && it <= 2000f } ?: 100f
        } else 100f

        val categoriesText = buildString {
            append(product.optString("categories"))
            val tags = product.optJSONArray("categories_tags")
            if (tags != null) {
                append(' ')
                append(tags.joinStrings())
            }
        }

        val normalizedCode = returnedCode.ifBlank { requestedBarcode }

        return FoodItem(
            name = displayName.trim(),
            category = mapCategory(categoriesText),
            calories = calories,
            protein = protein,
            fat = fat,
            carbs = carbs,
            defaultServingGrams = serving,
            isCustom = false,
            barcode = normalizedCode
        )
    }

    private fun nutritionLooksUsable(
        calories: Float,
        protein: Float,
        fat: Float,
        carbs: Float
    ): Boolean {
        val values = listOf(calories, protein, fat, carbs)
        if (values.any { !it.isFinite() || it < 0f }) return false
        if (calories > 1000f) return false
        if (protein > 100f || fat > 100f || carbs > 100f) return false
        return true
    }

    private fun mapCategory(raw: String): String {
        val value = raw.lowercase()
        return when {
            containsAny(value, "fish", "seafood", "рыб", "риба", "морепродукт") -> "Рыба"
            containsAny(value, "meat", "poultry", "chicken", "turkey", "beef", "pork", "мяс", "м'яс", "кур") -> "Мясо и птица"
            containsAny(value, "milk", "dairy", "cheese", "yogurt", "yoghurt", "kefir", "egg", "молоч", "сыр", "сир", "йогурт", "яйц") -> "Молочные продукты"
            containsAny(value, "vegetable", "vegetables", "овощ", "овоч") -> "Овощи"
            containsAny(value, "fruit", "fruits", "berry", "berries", "фрукт", "ягод") -> "Фрукты"
            containsAny(value, "cereal", "grain", "rice", "pasta", "bread", "porridge", "круп", "рис", "макарон", "хлеб", "хліб", "каша") -> "Крупы"
            containsAny(value, "nut", "snack", "chocolate", "candy", "sweet", "chips", "seed", "орех", "горіх", "снек", "шоколад", "конфет", "цукер", "чипс", "семеч", "насін") -> "Орехи и снеки"
            else -> "Другое"
        }
    }

    private fun containsAny(value: String, vararg tokens: String): Boolean =
        tokens.any(value::contains)

    private fun JSONObject.floatOrNull(key: String): Float? {
        if (!has(key) || isNull(key)) return null
        return when (val value = opt(key)) {
            is Number -> value.toFloat()
            is String -> value.replace(',', '.').toFloatOrNull()
            else -> null
        }
    }

    private fun firstNonBlank(vararg values: String): String? =
        values.firstOrNull { it.isNotBlank() && !it.equals("null", true) }?.trim()

    private fun JSONArray.joinStrings(): String = buildString {
        for (index in 0 until length()) {
            if (index > 0) append(' ')
            append(optString(index))
        }
    }

    private companion object {
        const val CONNECT_TIMEOUT_MS = 6_000
        const val READ_TIMEOUT_MS = 8_000
    }
}
