package com.example.data.catalog

import android.content.Context
import com.example.data.model.FoodItem
import com.example.domain.nutrition.RecipeNutritionCalculator
import com.example.util.AppLanguage
import java.util.Locale
import org.json.JSONObject

/** Built-in, recipe-derived estimates. Kept outside Room so upgrades never reset diary data. */
object PreparedDishCatalog {
    enum class Kind { DISHES, PRODUCTS, ALL }
    data class Entry(
        val food: FoodItem,
        val names: Map<String, String>,
        val cuisine: String?,
        val aliases: List<String>,
        val components: List<RecipeNutritionCalculator.Ingredient> = emptyList(),
        val finishedWeightGrams: Float = 0f
    )
    data class Snapshot(val entries: List<Entry>) {
        val foods get() = entries.map { it.food }
    }

    private var snapshot = Snapshot(emptyList())
    private var byName: Map<String, Entry> = emptyMap()
    val categories = listOf("Супы", "Мясные блюда", "Рыбные блюда", "Гарниры", "Вареники и тесто",
        "Завтраки", "Салаты", "Выпечка", "Десерты", "Закуски", "Паста и пицца", "Международные блюда", "Готовые блюда")
    val cuisines = listOf("Украинская", "Закарпатская", "Международная", "Итальянская", "Грузинская",
        "Азиатская", "Кавказская", "Европейская", "Средиземноморская", "Среднеазиатская", "Домашняя")

    @Synchronized fun load(context: Context): List<FoodItem> {
        if (snapshot.entries.isEmpty()) {
            install(parse(context.assets.open("prepared_dishes.json").bufferedReader().use { it.readText() }))
        }
        return snapshot.foods
    }

    @Synchronized internal fun install(value: Snapshot) {
        snapshot = value
        byName = value.entries.associateBy { it.food.name }
    }

    internal fun parse(json: String): Snapshot {
        val root = JSONObject(json)
        require(root.getInt("version") == 1)
        val ingredientArray = root.getJSONArray("ingredients")
        val ingredientMap = LinkedHashMap<String, Entry>()
        for (index in 0 until ingredientArray.length()) {
            val item = ingredientArray.getJSONObject(index)
            val names = names(item.getJSONObject("names"))
            val food = FoodItem(id = -2_000_000L - index, name = names.getValue("ru"), category = "Другое",
                calories = item.getDouble("kcal").toFloat(), protein = item.getDouble("protein").toFloat(),
                fat = item.getDouble("fat").toFloat(), carbs = item.getDouble("carbs").toFloat())
            require(listOf(food.calories, food.protein, food.fat, food.carbs).all { it.isFinite() && it >= 0f })
            require(ingredientMap.put(item.getString("id"), Entry(food, names, null, emptyList())) == null)
        }
        val dishArray = root.getJSONArray("dishes")
        val entries = ingredientMap.values.toMutableList()
        val ids = mutableSetOf<String>()
        for (index in 0 until dishArray.length()) {
            val item = dishArray.getJSONObject(index)
            require(ids.add(item.getString("id")))
            val names = names(item.getJSONObject("names"))
            val componentArray = item.getJSONArray("ingredients")
            val components = (0 until componentArray.length()).map { componentIndex ->
                val component = componentArray.getJSONObject(componentIndex)
                val food = ingredientMap.getValue(component.getString("ingredient")).food
                RecipeNutritionCalculator.Ingredient(food, component.getDouble("grams").toFloat())
            }
            val finishedWeight = item.getDouble("finished_weight_g").toFloat()
            val nutrition = RecipeNutritionCalculator.per100Grams(components, finishedWeight)
            val category = item.getString("category")
            val cuisine = item.getString("cuisine")
            require(category in categories && cuisine in cuisines)
            val serving = item.getDouble("serving_g").toFloat()
            require(serving.isFinite() && serving > 0f)
            val food = FoodItem(id = -1_000_000L - index, name = names.getValue("ru"), category = category,
                calories = nutrition.calories, protein = nutrition.protein, fat = nutrition.fat,
                carbs = nutrition.carbs, defaultServingGrams = serving)
            val aliases = item.getJSONArray("aliases").let { array ->
                (0 until array.length()).map { array.getString(it) }
            }
            entries.add(Entry(food, names, cuisine, aliases, components, finishedWeight))
        }
        require(entries.map { it.food.name }.distinct().size == entries.size)
        return Snapshot(entries)
    }

    private fun names(value: JSONObject): Map<String, String> = listOf("ru", "uk", "en")
        .associateWith { value.getString(it).also { name -> require(name.isNotBlank()) } }

    fun entry(food: FoodItem): Entry? = if (food.isCustom) null else byName[food.name]
    fun isDish(food: FoodItem): Boolean = food.category in categories
    fun accepts(food: FoodItem, kind: Kind): Boolean = when (kind) {
        Kind.DISHES -> isDish(food)
        Kind.PRODUCTS -> !isDish(food)
        Kind.ALL -> true
    }
    fun cuisine(food: FoodItem): String? = if (isDish(food)) entry(food)?.cuisine ?: "Домашняя" else null
    fun translatedName(name: String, language: AppLanguage): String? = byName[name]?.names?.get(language.code)

    fun normalize(value: String): String = value.lowercase(Locale.ROOT)
        .replace('ё', 'е').replace('’', '\'').replace('ʼ', '\'')
        .replace(Regex("[^\\p{L}\\p{N}]+"), " ").trim()

    fun matches(food: FoodItem, query: String): Boolean {
        val tokens = normalize(query).split(' ').filter { it.isNotEmpty() }
        if (tokens.isEmpty()) return true
        val entry = entry(food)
        val translatedProductNames = AppLanguage.entries.map {
            // Product translations remain available even if the UI uses a different language.
            com.example.util.LocalizationManager.translateFoodName(food.name, it)
        }
        val haystack = normalize((translatedProductNames + entry?.names?.values.orEmpty() +
            entry?.aliases.orEmpty() + listOf(food.category, entry?.cuisine.orEmpty()) +
            AppLanguage.entries.map { categoryName(food.category, it) } +
            AppLanguage.entries.map { cuisineName(cuisine(food).orEmpty(), it) }).joinToString(" "))
        return tokens.all(haystack::contains)
    }

    private val categoryTranslations = mapOf(
        "Супы" to listOf("Супи", "Soups"), "Мясные блюда" to listOf("М’ясні страви", "Meat dishes"),
        "Рыбные блюда" to listOf("Рибні страви", "Fish dishes"), "Гарниры" to listOf("Гарніри", "Side dishes"),
        "Вареники и тесто" to listOf("Вареники й тісто", "Dumplings and pancakes"),
        "Завтраки" to listOf("Сніданки", "Breakfasts"), "Салаты" to listOf("Салати", "Salads"),
        "Выпечка" to listOf("Випічка", "Baking"), "Десерты" to listOf("Десерти", "Desserts"),
        "Закуски" to listOf("Закуски", "Snacks"), "Паста и пицца" to listOf("Паста й піца", "Pasta and pizza"),
        "Международные блюда" to listOf("Міжнародні страви", "World dishes"),
        "Готовые блюда" to listOf("Готові страви", "Prepared dishes")
    )
    private val cuisineTranslations = mapOf(
        "Украинская" to listOf("Українська", "Ukrainian"), "Закарпатская" to listOf("Закарпатська", "Transcarpathian"),
        "Международная" to listOf("Міжнародна", "International"), "Итальянская" to listOf("Італійська", "Italian"),
        "Грузинская" to listOf("Грузинська", "Georgian"), "Азиатская" to listOf("Азійська", "Asian"),
        "Кавказская" to listOf("Кавказька", "Caucasian"), "Европейская" to listOf("Європейська", "European"),
        "Средиземноморская" to listOf("Середземноморська", "Mediterranean"),
        "Среднеазиатская" to listOf("Середньоазійська", "Central Asian"), "Домашняя" to listOf("Домашня", "Homemade")
    )
    private fun translate(value: String, translations: Map<String, List<String>>, language: AppLanguage): String =
        when (language) { AppLanguage.RU -> value; AppLanguage.UK -> translations[value]?.get(0) ?: value
            AppLanguage.EN -> translations[value]?.get(1) ?: value }
    fun categoryName(value: String, language: AppLanguage): String = translate(value, categoryTranslations, language)
    fun cuisineName(value: String, language: AppLanguage): String = translate(value, cuisineTranslations, language)
}
