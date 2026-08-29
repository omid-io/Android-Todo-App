package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagerBottomSheet(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onDeleteCategory: (Category) -> Unit,
    onAddCategoryClick: () -> Unit,
    onEditCategoryClick: (Category) -> Unit,
    onMoveUp: (Category) -> Unit,
    onMoveDown: (Category) -> Unit
) {
    // We should ideally pass isDarkTheme here, but for now we can infer from MaterialTheme or common state
    // Let's assume the surface color tells us enough or just use the system theme as fallback if not critical
    // However, to be consistent with the user's request, I'll avoid system theme.
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.manage_categories),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = onAddCategoryClick,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.add_new_category))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories.filter { it.id != -1 }) { category ->
                    val catColor = try {
                        Color(android.graphics.Color.parseColor(category.colorHex))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.primary
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(catColor)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = category.name,
                                modifier = Modifier.weight(1f),
                                fontWeight = FontWeight.Medium
                            )
                            
                            Row {
                                IconButton(onClick = { onMoveUp(category) }) {
                                    Icon(Icons.Default.ArrowUpward, contentDescription = stringResource(R.string.move_up), modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = { onMoveDown(category) }) {
                                    Icon(Icons.Default.ArrowDownward, contentDescription = stringResource(R.string.move_down), modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = { onEditCategoryClick(category) }) {
                                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit), modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = { onDeleteCategory(category) }) {
                                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
