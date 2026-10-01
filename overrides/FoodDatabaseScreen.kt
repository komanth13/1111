package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FoodItem
import com.example.data.catalog.PreparedDishCatalog
import com.example.ui.components.CatalogFilters
import com.example.ui.components.CatalogSelection
import com.example.ui.components.DishEstimateNote
import com.example.data.model.MealType
import com.example.ui.components.AddFoodDialog
import com.example.ui.theme.CarbRose
import com.example.ui.theme.FatAmber
import com.example.ui.theme.ProteinBlue
import com.example.ui.viewmodel.FitnessViewModel
import com.example.util.LocalizationManager
import com.example.util.StringKey
import com.example.util.appString
import com.example.util.localizedCategory
import com.example.util.localizedFoodName
import kotlin.math.roundToInt

@Composable
fun FoodDatabaseScreen(
    viewModel: FitnessViewModel,
    modifier: Modifier = Modifier
) {
    val foods by viewModel.foodCatalog.collectAsStateWithLifecycle()
    val currentLang = com.example.util.appLanguage()
    var searchQuery by remember { mutableStateOf("") }
    var catalogSelection by remember { mutableStateOf(CatalogSelection()) }
    var showCreateDialogForMeal by remember { mutableStateOf<MealType?>(null) }

    val filteredFoods = remember(foods, searchQuery, catalogSelection, currentLang) {
        foods.filter { catalogSelection.accepts(it) && PreparedDishCatalog.matches(it, searchQuery) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
            .testTag("food_database_screen_list"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appString(StringKey.FOODS_TITLE),
                        style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = appString(StringKey.FOODS_SUBTITLE),
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showCreateDialogForMeal = MealType.SNACK },
                    shape = androidx.compose.material3.MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(com.example.ui.theme.SlimTrackSizes.buttonCompact)
                        .testTag("create_custom_food_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(appString(StringKey.ADD), fontWeight = FontWeight.Bold, style = androidx.compose.material3.MaterialTheme.typography.labelMedium)
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("catalog_search_field"),
                placeholder = { Text(appString(StringKey.FOODS_SEARCH_HINT), style = androidx.compose.material3.MaterialTheme.typography.labelMedium) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = appString(StringKey.SEARCH), modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                        }) {
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
        }

        item { CatalogFilters(foods, catalogSelection) { catalogSelection = it } }
        if (filteredFoods.isEmpty()) {
            item { Text(appString(StringKey.CATALOG_EMPTY_HINT), modifier = Modifier.padding(12.dp)) }
        }

        // Foods list
        items(filteredFoods, key = { it.id }) { food ->
            FoodDetailCard(
                food = food,
                onDelete = if (food.isCustom) {
                    { viewModel.deleteFoodItem(food.id) }
                } else null
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    showCreateDialogForMeal?.let { mealType ->
        AddFoodDialog(
            mealType = mealType,
            foods = foods,
            onDismiss = { showCreateDialogForMeal = null },
            onAddMeal = { name, grams, kcal, p, f, c ->
                viewModel.addMeal(mealType, name, grams, kcal, p, f, c)
            },
            onCreateCustomFood = { name, category, kcal, p, f, c ->
                viewModel.saveCustomFood(name, category, kcal, p, f, c)
            }
        )
    }
}

@Composable
private fun FoodDetailCard(
    food: FoodItem,
    onDelete: (() -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("food_catalog_item_${food.id}"),
        shape = androidx.compose.material3.MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = localizedFoodName(food.name),
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (food.isCustom) {
                            Box(
                                modifier = Modifier
                                    .clip(androidx.compose.material3.MaterialTheme.shapes.extraSmall)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = appString(StringKey.FOOD_CUSTOM_BADGE),
                                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                    Text(
                        text = localizedCategory(food.category),
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${food.calories.roundToInt()} ${appString(StringKey.UNIT_KCAL)}",
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "/ 100 ${appString(StringKey.UNIT_GRAM)}",
                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (onDelete != null) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = appString(StringKey.DELETE),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            DishEstimateNote(food)
            PreparedDishCatalog.cuisine(food)?.let { cuisine ->
                Text(PreparedDishCatalog.cuisineName(cuisine, com.example.util.appLanguage()),
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(8.dp))

            // 3 Equal-Width Centered Macro Pills (БЖУ)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MacroTag(
                    modifier = Modifier.weight(1f),
                    label = appString(StringKey.DIARY_PROTEIN),
                    value = "${food.protein} ${appString(StringKey.UNIT_GRAM)}",
                    color = ProteinBlue
                )
                MacroTag(
                    modifier = Modifier.weight(1f),
                    label = appString(StringKey.DIARY_FAT),
                    value = "${food.fat} ${appString(StringKey.UNIT_GRAM)}",
                    color = FatAmber
                )
                MacroTag(
                    modifier = Modifier.weight(1f),
                    label = appString(StringKey.DIARY_CARBS),
                    value = "${food.carbs} ${appString(StringKey.UNIT_GRAM)}",
                    color = CarbRose
                )
            }
        }
    }
}

@Composable
private fun MacroTag(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(androidx.compose.material3.MaterialTheme.shapes.small)
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
        Text(
            text = value,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}
