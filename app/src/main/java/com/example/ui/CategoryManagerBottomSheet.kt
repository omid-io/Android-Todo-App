package com.example.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.R
import com.example.data.Category
import com.example.util.SoundManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagerBottomSheet(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onDeleteCategory: (Category) -> Unit,
    onAddCategoryClick: () -> Unit,
    onEditCategoryClick: (Category) -> Unit,
    onReorderDone: (List<Category>) -> Unit,
    isDarkTheme: Boolean = true
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val categoryList = remember(categories) {
        mutableStateListOf<Category>().apply {
            addAll(categories.filter { it.id != -1 })
        }
    }

    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    val itemHeightPx = with(LocalDensity.current) { 62.dp.toPx() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.manage_categories),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDarkTheme) Color.White else Color(0xFF0F172A)
                    )
                    Text(
                        text = "برای جابجایی، آیکون ☰ را گرفته و به بالا یا پایین بکشید",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDarkTheme) Color.White.copy(alpha = 0.55f) else Color.Black.copy(alpha = 0.55f)
                    )
                }
                Button(
                    onClick = onAddCategoryClick,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.add_new_category), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(
                    items = categoryList,
                    key = { _, cat -> cat.id }
                ) { index, category ->
                    val isCurrentDragging = draggingIndex == index
                    val catColor = try {
                        Color(android.graphics.Color.parseColor(category.colorHex))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.primary
                    }

                    val animatedScale by animateFloatAsState(
                        targetValue = if (isCurrentDragging) 1.04f else 1f,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "scale"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(if (isCurrentDragging) 10f else 1f)
                            .graphicsLayer {
                                translationY = if (isCurrentDragging) dragOffsetY else 0f
                                scaleX = animatedScale
                                scaleY = animatedScale
                                shadowElevation = if (isCurrentDragging) 24f else 0f
                            }
                            .then(
                                if (isCurrentDragging) {
                                    Modifier
                                        .shadow(12.dp, RoundedCornerShape(16.dp))
                                        .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                                } else Modifier
                            )
                            .glassCard(shape = RoundedCornerShape(16.dp), isDarkTheme = isDarkTheme)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Touch Reorder Drag Handle
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isCurrentDragging) {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                        } else if (isDarkTheme) {
                                            Color.White.copy(alpha = 0.06f)
                                        } else {
                                            Color.Black.copy(alpha = 0.04f)
                                        }
                                    )
                                    .pointerInput(category.id) {
                                        detectDragGestures(
                                            onDragStart = {
                                                val foundIdx = categoryList.indexOfFirst { it.id == category.id }
                                                if (foundIdx != -1) {
                                                    draggingIndex = foundIdx
                                                    dragOffsetY = 0f
                                                    SoundManager.playTap()
                                                }
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                dragOffsetY += dragAmount.y
                                                val currentIdx = draggingIndex ?: return@detectDragGestures
                                                val threshold = itemHeightPx * 0.5f

                                                if (dragOffsetY > threshold && currentIdx < categoryList.size - 1) {
                                                    val item = categoryList.removeAt(currentIdx)
                                                    categoryList.add(currentIdx + 1, item)
                                                    draggingIndex = currentIdx + 1
                                                    dragOffsetY -= itemHeightPx
                                                    SoundManager.playTap()
                                                } else if (dragOffsetY < -threshold && currentIdx > 0) {
                                                    val item = categoryList.removeAt(currentIdx)
                                                    categoryList.add(currentIdx - 1, item)
                                                    draggingIndex = currentIdx - 1
                                                    dragOffsetY += itemHeightPx
                                                    SoundManager.playTap()
                                                }
                                            },
                                            onDragEnd = {
                                                draggingIndex = null
                                                dragOffsetY = 0f
                                                onReorderDone(categoryList.toList())
                                            },
                                            onDragCancel = {
                                                draggingIndex = null
                                                dragOffsetY = 0f
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.DragHandle,
                                    contentDescription = null,
                                    tint = if (isCurrentDragging) {
                                        MaterialTheme.colorScheme.primary
                                    } else if (isDarkTheme) {
                                        Color.White.copy(alpha = 0.65f)
                                    } else {
                                        Color.Black.copy(alpha = 0.55f)
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(catColor)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = category.name,
                                modifier = Modifier.weight(1f),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isDarkTheme) Color.White else Color(0xFF0F172A)
                            )
                            
                            IconButton(
                                onClick = { onEditCategoryClick(category) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.edit),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = { onDeleteCategory(category) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.delete),
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
