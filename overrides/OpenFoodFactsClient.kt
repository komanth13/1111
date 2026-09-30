package com.example.data.network

import com.example.BuildConfig
import com.example.data.model.FoodItem
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Barcode lookup backed by Open Food Facts.
 *
 * The remote service is used only after the local Room cache misses. Successful,
 * nutrition-complete products are cached locally by RoomFitnessRepository.
 */
class OpenFoodFactsClient : RemoteFoodDataSource {

    override suspend fun findByBarcode(barcode: String): FoodItem? = withContext(Dispatchers.IO) {
        val code = barcode.filter(Char::isDigit)
        if (code.isBlank()) return@withContext null

        val fields = listOf(
            "code",
            "product_name",
            "product_name_uk",
            "product_name_ru",
            "product_name_en",
            "brands",
            "categories",
            "categories_tags",
            "nutriments",
            "serving_quantity",
            "serving_size"
        ).joinToString(",")

        val endpoint = URL(
            "https://world.openfoodfacts.org/api/v3/product/$code" +
                "?product_type=food&cc=ua&lc=uk&tags_lc=uk&fields=$fields"
        )

        val connection = (endpoint.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            instanceFollowRedirects = true
            setRequestProperty(
                "User-Agent",
                "SlimTrack/${BuildConfig.VERSION_NAME} (Android; barcode lookup; contact: support@slimtrack.app)"
            )
            setRequestProperty("Accept", "application/json")
        }

        try {
            val status = connection.responseCode
            if (status !in 200..299) return@withContext null

            val json = connection.inputStream.bufferedReader().use(BufferedReader::readText)
            parseProduct(json, code)
        } catch (_: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }

    internal fun parseProduct(json: String, requestedBarcode: String): FoodItem? {
        val root = runCatching { JSONObject(json) }.getOrNull() ?: return null
        val product = root.optJSONObject("product") ?: root

        val productName = firstNonBlank(
            product.optString("product_name_uk"),
            product.optString("product_name_ru"),
            product.optString("product_name_en"),
            product.optString("product_name")
        ) ?: return null

        val brands = product.optString("brands").trim()
        val displayName = when {
            brands.isBlank() -> productName
            productName.contains(brands, ignoreCase = true) -> productName
            else -> "$productName — $brands"
        }

        val nutriments = product.optJSONObject("nutriments") ?: return null
        val calories = nutriments.floatOrNull("energy-kcal_100g") ?: return null
        val protein = nutriments.floatOrNull("proteins_100g") ?: return null
        val fat = nutriments.floatOrNull("fat_100g") ?: return null
        val carbs = nutriments.floatOrNull("carbohydrates_100g") ?: return null

        if (!nutritionLooksUsable(calories, protein, fat, carbs)) return null

        val serving = product.floatOrNull("serving_quantity")
            ?.takeIf { it > 0f && it <= 2000f }
            ?: 100f

        val categoriesText = buildString {
            append(product.optString("categories"))
            val tags = product.optJSONArray("categories_tags")
            if (tags != null) {
                append(' ')
                append(tags.joinStrings())
            }
        }

        val returnedCode = product.optString("code").filter(Char::isDigit)
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
        values.firstOrNull { it.isNotBlank() }?.trim()

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