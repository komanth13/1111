package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FoodItem
import com.example.data.model.MealType
import com.example.ui.theme.CarbRose
import com.example.ui.theme.FatAmber
import com.example.ui.theme.ProteinBlue
import com.example.util.AppLanguage
import com.example.util.LocalizationManager
import com.example.util.StringKey
import com.example.util.appString
import com.example.util.localizedCategory
import com.example.util.localizedFoodName
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodDialog(
    mealType: MealType,
    foods: List<FoodItem>,
    initialFood: FoodItem? = null,
    onDismiss: () -> Unit,
    onAddMeal: (foodName: String, grams: Float, calories: Float, protein: Float, fat: Float, carbs: Float) -> Unit,
    onCreateCustomFood: (name: String, category: String, calories: Float, protein: Float, fat: Float, carbs: Float) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val currentLang = com.example.util.appLanguage()
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Все") }

    val categories = listOf("Все", "Мясо и птица", "Рыба", "Крупы", "Молочные продукты", "Овощи", "Фрукты", "Орехи и снеки")

    val localizedMealTitle = when (mealType) {
        MealType.BREAKFAST -> appString(StringKey.DIARY_MEAL_BREAKFAST)
        MealType.LUNCH -> appString(StringKey.DIARY_MEAL_LUNCH)
        MealType.DINNER -> appString(StringKey.DIARY_MEAL_DINNER)
        MealType.SNACK -> appString(StringKey.DIARY_MEAL_SNACKS)
    }

    // Filter foods with multilingual support
    val filteredFoods = remember(foods, searchQuery, selectedCategory, currentLang) {
        foods.filter { item ->
            val translatedName = LocalizationManager.translateFoodName(item.name, currentLang)
            val matchesQuery = searchQuery.isBlank() ||
                item.name.contains(searchQuery, ignoreCase = true) ||
                translatedName.contains(searchQuery, ignoreCase = true)
            val matchesCat = selectedCategory == "Все" || item.category == selectedCategory
            matchesQuery && matchesCat
        }
    }

    var selectedFood by remember(initialFood) { mutableStateOf(initialFood) }
    var portionGrams by remember(initialFood) {
        mutableFloatStateOf(initialFood?.defaultServingGrams ?: 100f)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("add_food_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = mealType.icon, style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
                    Column {
                        Text(
                            text = "${appString(StringKey.ADD)}: $localizedMealTitle",
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = when (currentLang) {
                                AppLanguage.UK -> "Оберіть продукт або вкажіть порцію"
                                AppLanguage.EN -> "Select a product or enter portion size"
                                else -> "Выберите продукт или укажите порцию"
                            },
                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_add_food_dialog")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = appString(StringKey.CLOSE))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clip(androidx.compose.material3.MaterialTheme.shapes.medium)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(appString(StringKey.FOODS_TITLE), fontWeight = FontWeight.SemiBold, style = androidx.compose.material3.MaterialTheme.typography.labelMedium, maxLines = 1) },
                    modifier = Modifier.testTag("tab_food_catalog")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(appString(StringKey.FOOD_CREATE_CUSTOM), fontWeight = FontWeight.SemiBold, style = androidx.compose.material3.MaterialTheme.typography.labelMedium, maxLines = 1) },
                    modifier = Modifier.testTag("tab_custom_food")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTab == 0) {
                if (selectedFood == null) {
                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("food_search_input"),
                        placeholder = { Text(appString(StringKey.FOODS_SEARCH_HINT), style = androidx.compose.material3.MaterialTheme.typography.labelMedium) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = appString(StringKey.SEARCH), modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = appString(StringKey.CLOSE), modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = androidx.compose.material3.MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Categories chip row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { cat ->
                            val isSelected = selectedCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(androidx.compose.material3.MaterialTheme.shapes.medium)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = localizedCategory(cat),
                                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Foods list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredFoods) { food ->
                            FoodListItemCard(
                                food = food,
                                onSelect = {
                                    selectedFood = food
                                    portionGrams = food.defaultServingGrams
                                }
                            )
                        }
                    }
                } else {
                    // Portion & Macro Calculator for Selected Food
                    val food = selectedFood!!
                    val multiplier = portionGrams / 100f
                    val currentKcal = food.calories * multiplier
                    val currentProtein = food.protein * multiplier
                    val currentFat = food.fat * multiplier
                    val currentCarbs = food.carbs * multiplier
                    val gramUnit = appString(StringKey.UNIT_GRAM)
                    val kcalUnit = appString(StringKey.UNIT_KCAL)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.material3.MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = localizedFoodName(food.name),
                                        style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = localizedCategory(food.category),
                                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { selectedFood = null }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = appString(StringKey.CLOSE))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Calories and macros badges
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                MacroMiniBadge(
                                    modifier = Modifier.weight(1f),
                                    title = kcalUnit,
                                    value = "${currentKcal.roundToInt()}",
                                    color = MaterialTheme.colorScheme.primary
                                )
                                MacroMiniBadge(
                                    modifier = Modifier.weight(1f),
                                    title = appString(StringKey.DIARY_PROTEIN),
                                    value = "${(currentProtein * 10).roundToInt() / 10f}$gramUnit",
                                    color = ProteinBlue
                                )
                                MacroMiniBadge(
                                    modifier = Modifier.weight(1f),
                                    title = appString(StringKey.DIARY_FAT),
                                    value = "${(currentFat * 10).roundToInt() / 10f}$gramUnit",
                                    color = FatAmber
                                )
                                MacroMiniBadge(
                                    modifier = Modifier.weight(1f),
                                    title = appString(StringKey.DIARY_CARBS),
                                    value = "${(currentCarbs * 10).roundToInt() / 10f}$gramUnit",
                                    color = CarbRose
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Portion Slider & Buttons
                            Text(
                                text = "${appString(StringKey.DIALOG_GRAMS_LABEL)}: ${portionGrams.roundToInt()} $gramUnit",
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Slider(
                                value = portionGrams,
                                onValueChange = { portionGrams = it },
                                valueRange = 10f..500f,
                                steps = 48,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("portion_slider")
                            )

                            // Quick Portion Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(50f, 100f, 150f, 200f, 250f).forEach { g ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(androidx.compose.material3.MaterialTheme.shapes.small)
                                            .background(
                                                if (portionGrams.roundToInt() == g.roundToInt())
                                                    MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surface
                                            )
                                            .clickable { portionGrams = g }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${g.roundToInt()}$gramUnit",
                                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (portionGrams.roundToInt() == g.roundToInt())
                                                MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    onAddMeal(
                                        food.name,
                                        portionGrams,
                                        currentKcal,
                                        currentProtein,
                                        currentFat,
                                        currentCarbs
                                    )
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(com.example.ui.theme.SlimTrackSizes.button)
                                    .testTag("confirm_add_food_button"),
                                shape = androidx.compose.material3.MaterialTheme.shapes.medium,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${appString(StringKey.ADD)} (${currentKcal.roundToInt()} $kcalUnit)",
                                    fontWeight = FontWeight.Bold,
                                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            } else {
                // Custom food creator form
                CustomFoodForm(
                    onSave = { name, cat, kcal, p, f, c ->
                        onCreateCustomFood(name, cat, kcal, p, f, c)
                        selectedTab = 0
                        searchQuery = name
                    }
                )
            }
        }
    }
}

@Composable
private fun FoodListItemCard(
    food: FoodItem,
    onSelect: () -> Unit
) {
    val gramUnit = appString(StringKey.UNIT_GRAM)
    val kcalUnit = appString(StringKey.UNIT_KCAL)
    val pShort = appString(StringKey.DIARY_PROTEIN).take(1)
    val fShort = appString(StringKey.DIARY_FAT).take(1)
    val cShort = appString(StringKey.DIARY_CARBS).take(1)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .testTag("food_item_${food.id}"),
        shape = androidx.compose.material3.MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = localizedFoodName(food.name),
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$pShort: ${food.protein}$gramUnit • $fShort: ${food.fat}$gramUnit • $cShort: ${food.carbs}$gramUnit",
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${food.calories.roundToInt()} $kcalUnit",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "/ 100 $gramUnit",
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MacroMiniBadge(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(androidx.compose.material3.MaterialTheme.shapes.small)
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 4.dp, vertical = 5.dp)
    ) {
        Text(text = title, style = androidx.compose.material3.MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(text = value, style = androidx.compose.material3.MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
    }
}

@Composable
internal fun CustomFoodForm(
    initialName: String = "",
    saving: Boolean = false,
    onSave: (name: String, category: String, calories: Float, protein: Float, fat: Float, carbs: Float) -> Unit
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    var category by remember { mutableStateOf("Другое") }
    var caloriesStr by remember { mutableStateOf("") }
    var proteinStr by remember { mutableStateOf("") }
    var fatStr by remember { mutableStateOf("") }
    var carbsStr by remember { mutableStateOf("") }

    fun number(raw: String): Float? = raw.trim().replace(',', '.').toFloatOrNull()
        ?.takeIf { it.isFinite() && it >= 0f }
    val calories = number(caloriesStr)?.takeIf { it <= 1000f }
    val protein = number(proteinStr)?.takeIf { it <= 100f }
    val fat = number(fatStr)?.takeIf { it <= 100f }
    val carbs = number(carbsStr)?.takeIf { it <= 100f }
    val valid = name.isNotBlank() && calories != null && protein != null && fat != null && carbs != null

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(appString(StringKey.FOOD_NAME_LABEL), style = androidx.compose.material3.MaterialTheme.typography.labelMedium) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("custom_food_name_input"),
            singleLine = true,
            shape = androidx.compose.material3.MaterialTheme.shapes.medium
        )

        OutlinedTextField(
            value = caloriesStr,
            onValueChange = { caloriesStr = it },
            label = { Text(appString(StringKey.FOOD_KCAL_100G_LABEL), style = androidx.compose.material3.MaterialTheme.typography.labelMedium) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("custom_food_calories_input"),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = androidx.compose.material3.MaterialTheme.shapes.medium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = proteinStr,
                onValueChange = { proteinStr = it },
                label = { Text(appString(StringKey.DIARY_PROTEIN), style = androidx.compose.material3.MaterialTheme.typography.labelSmall) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("custom_food_protein_input"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = androidx.compose.material3.MaterialTheme.shapes.medium
            )
            OutlinedTextField(
                value = fatStr,
                onValueChange = { fatStr = it },
                label = { Text(appString(StringKey.DIARY_FAT), style = androidx.compose.material3.MaterialTheme.typography.labelSmall) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("custom_food_fat_input"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = androidx.compose.material3.MaterialTheme.shapes.medium
            )
            OutlinedTextField(
                value = carbsStr,
                onValueChange = { carbsStr = it },
                label = { Text(appString(StringKey.DIARY_CARBS), style = androidx.compose.material3.MaterialTheme.typography.labelSmall) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("custom_food_carbs_input"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = androidx.compose.material3.MaterialTheme.shapes.medium
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Button(
            onClick = {
                if (valid && !saving) {
                    onSave(name.trim(), category, calories!!, protein!!, fat!!, carbs!!)
                }
            },
            enabled = valid && !saving,
            modifier = Modifier
                .fillMaxWidth()
                .height(com.example.ui.theme.SlimTrackSizes.button)
                .testTag("save_custom_food_button"),
            shape = androidx.compose.material3.MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(appString(StringKey.SAVE), fontWeight = FontWeight.Bold)
        }
    }
}
