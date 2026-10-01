package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.catalog.PreparedDishCatalog
import com.example.data.model.FoodItem
import com.example.domain.nutrition.RecipeNutritionCalculator
import com.example.util.StringKey
import com.example.util.appString
import com.example.util.localizedFoodName
import kotlin.math.roundToInt

private data class RecipeIngredientDraft(val food: FoodItem, val grams: String = "100")

@Composable fun RecipeBuilderForm(foods: List<FoodItem>, onSave: (FoodItem) -> Unit) {
    var name by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    var finishedWeight by remember { mutableStateOf("") }
    var drafts by remember { mutableStateOf<List<RecipeIngredientDraft>>(emptyList()) }
    val results = remember(foods, query) {
        if (query.isBlank()) emptyList() else foods.filter {
            !PreparedDishCatalog.isDish(it) && PreparedDishCatalog.matches(it, query)
        }.take(10)
    }
    fun number(value: String): Float? = value.trim().replace(',', '.').toFloatOrNull()
        ?.takeIf { it.isFinite() && it > 0f && it <= 100_000f }
    val weight = number(finishedWeight)
    val ingredients = drafts.mapNotNull { draft ->
        number(draft.grams)?.let { RecipeNutritionCalculator.Ingredient(draft.food, it) }
    }
    val nutrition = if (weight != null && ingredients.size == drafts.size && drafts.isNotEmpty()) {
        runCatching { RecipeNutritionCalculator.per100Grams(ingredients, weight) }.getOrNull()
    } else null

    LazyColumn(modifier = Modifier.fillMaxWidth().height(440.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Text(appString(StringKey.RECIPE_BUILDER_HINT), style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
        }
        item {
            OutlinedTextField(name, { name = it }, label = { Text(appString(StringKey.RECIPE_NAME)) },
                singleLine = true, modifier = Modifier.fillMaxWidth().testTag("recipe_name"))
        }
        item {
            OutlinedTextField(query, { query = it }, label = { Text(appString(StringKey.RECIPE_ADD_INGREDIENT)) },
                singleLine = true, modifier = Modifier.fillMaxWidth().testTag("recipe_ingredient_search"))
        }
        if (query.isNotBlank() && results.isEmpty()) {
            item { Text(appString(StringKey.RECIPE_INGREDIENT_NOT_FOUND)) }
        }
        items(results, key = { "result_${it.id}" }) { food ->
            Card(modifier = Modifier.fillMaxWidth().clickable {
                if (drafts.none { it.food.id == food.id }) drafts = drafts + RecipeIngredientDraft(food)
                query = ""
            }) {
                Text(localizedFoodName(food.name), modifier = Modifier.padding(10.dp))
            }
        }
        items(drafts, key = { "ingredient_${it.food.id}" }) { draft ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(localizedFoodName(draft.food.name))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(draft.grams, { value ->
                        drafts = drafts.map { if (it.food.id == draft.food.id) it.copy(grams = value) else it }
                    }, label = { Text(appString(StringKey.DIALOG_GRAMS_LABEL)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true,
                        modifier = Modifier.weight(1f))
                    TextButton(onClick = { drafts = drafts.filterNot { it.food.id == draft.food.id } }) {
                        Text(appString(StringKey.DELETE))
                    }
                }
            }
        }
        item {
            OutlinedTextField(finishedWeight, { finishedWeight = it },
                label = { Text(appString(StringKey.RECIPE_FINISHED_WEIGHT)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("recipe_finished_weight"))
        }
        item {
            if (nutrition != null) {
                Text("${nutrition.calories.roundToInt()} ${appString(StringKey.UNIT_KCAL)} / 100 ${appString(StringKey.UNIT_GRAM)}")
                Text("${appString(StringKey.DIARY_PROTEIN)}: ${"%.1f".format(nutrition.protein)} • " +
                    "${appString(StringKey.DIARY_FAT)}: ${"%.1f".format(nutrition.fat)} • " +
                    "${appString(StringKey.DIARY_CARBS)}: ${"%.1f".format(nutrition.carbs)}")
            } else if (drafts.isNotEmpty()) Text(appString(StringKey.RECIPE_VALIDATION_HINT))
        }
        item {
            Button(enabled = name.isNotBlank() && nutrition != null, modifier = Modifier.fillMaxWidth().testTag("save_recipe"),
                onClick = {
                    nutrition?.let {
                        onSave(FoodItem(name = name.trim(), category = "Готовые блюда",
                            calories = it.calories, protein = it.protein, fat = it.fat, carbs = it.carbs,
                            isCustom = true, defaultServingGrams = 100f))
                    }
                }) { Text(appString(StringKey.RECIPE_SAVE)) }
        }
    }
}
