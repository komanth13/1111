package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.catalog.PreparedDishCatalog
import com.example.data.model.FoodItem
import com.example.util.AppLanguage
import com.example.util.StringKey
import com.example.util.appLanguage
import com.example.util.appString
import com.example.util.localizedCategory

data class CatalogSelection(
    val kind: PreparedDishCatalog.Kind = PreparedDishCatalog.Kind.DISHES,
    val category: String = "Все",
    val cuisine: String = "Все"
) {
    fun accepts(food: FoodItem): Boolean = PreparedDishCatalog.accepts(food, kind) &&
        (category == "Все" || food.category == category) &&
        (cuisine == "Все" || PreparedDishCatalog.cuisine(food) == cuisine)
}

@Composable fun CatalogFilters(foods: List<FoodItem>, selection: CatalogSelection, onChange: (CatalogSelection) -> Unit) {
    val language = appLanguage()
    val categories = listOf("Все") + foods.filter { PreparedDishCatalog.accepts(it, selection.kind) }
        .map { it.category }.distinct()
    val cuisines = listOf("Все") + PreparedDishCatalog.cuisines.filter { cuisine ->
        foods.any { PreparedDishCatalog.cuisine(it) == cuisine }
    }
    Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.fillMaxWidth()) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(PreparedDishCatalog.Kind.entries) { kind ->
                val key = when (kind) {
                    PreparedDishCatalog.Kind.DISHES -> StringKey.CATALOG_DISHES
                    PreparedDishCatalog.Kind.PRODUCTS -> StringKey.CATALOG_PRODUCTS
                    PreparedDishCatalog.Kind.ALL -> StringKey.FOODS_CAT_ALL
                }
                FilterChip(selected = selection.kind == kind,
                    onClick = { onChange(CatalogSelection(kind)) }, label = { Text(appString(key)) },
                    modifier = Modifier.testTag("catalog_kind_${kind.name}"))
            }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(categories) { category ->
                FilterChip(selected = selection.category == category,
                    onClick = { onChange(selection.copy(category = category)) },
                    label = { Text(localizedCategory(category)) })
            }
        }
        if (selection.kind != PreparedDishCatalog.Kind.PRODUCTS) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(cuisines) { cuisine ->
                    FilterChip(selected = selection.cuisine == cuisine,
                        onClick = { onChange(selection.copy(cuisine = cuisine)) },
                        label = { Text(if (cuisine == "Все") appString(StringKey.CATALOG_ALL_CUISINES)
                            else PreparedDishCatalog.cuisineName(cuisine, language)) })
                }
            }
        }
    }
}

@Composable fun DishEstimateNote(food: FoodItem) {
    if (PreparedDishCatalog.isDish(food)) {
        Text(appString(if (food.isCustom) StringKey.RECIPE_CALCULATED_NOTE else StringKey.DISH_ESTIMATE_NOTE),
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
