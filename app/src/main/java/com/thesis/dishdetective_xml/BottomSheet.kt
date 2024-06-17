package com.thesis.dishdetective_xml

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


class BottomSheet {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun PartialBottomSheet(show: Boolean) {
        var showBottomSheet by remember { mutableStateOf(show) }
        val sheetState = rememberModalBottomSheetState(

            skipPartiallyExpanded = false,
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                onClick = { showBottomSheet = true }
            ) {
                Icon(Icons.Default.List, contentDescription = "List")
            }

            if (showBottomSheet) {
                ModalBottomSheet(
                    modifier = Modifier.fillMaxHeight(0.9f),
                    sheetState = sheetState,
                    onDismissRequest = { showBottomSheet = false }
                ) {
                    // Custom content based on the image provided
                    BottomSheetContent()
                }
            }
        }
    }
    @Composable
    fun BottomSheetContent() {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            item {
                Text(
                    text = "Order of recommended dishes based on goals:",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Recommended dishes
            item {
                DishItem(order = 1, name = "Pork Sinigang")
                DishItem(order = 2, name = "Pork Adobo")
                DishItem(order = 3, name = "Chicken Adobo")
            }

            // Goals
            item {
                Text(
                    text = "Your Goals:",
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    text = "Goal 1: Weight loss\n\nTo lose weight, you need to consume fewer calories than your body burns, creating a calorie deficit. This forces your body to use stored fat for energy, leading to weight loss.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Nutrients considered
            item {
                Text(
                    text = "Nutrients considered:",
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    text = "1. Calories\n\nCalories are a measure of energy provided by food. The balance between calories consumed and calories expended determines your body weight.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // How we make our recommendations
            item {
                Text(
                    text = "How we make our recommendations:",
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    text = "Prioritizing lower calorie content.\n\nWe prioritize dishes that are low to moderate in calories based on your daily allowance. This helps you achieve a calorie deficit while keeping within safe and healthy limits.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    @Composable
    fun DishItem(order: Int, name: String) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Text(
                text = "$order.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
