package com.example.ui

import com.example.R

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Category
import com.example.data.Subtask
import com.example.data.Task
import com.example.util.JalaliCalendar
import com.example.util.SoundManager
import com.example.util.toPersianDigits
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTodoScreen(
    viewModel: TodoViewModel,
    onNavigateToReminders: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val subtasks by viewModel.subtasks.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    var isAddTaskSheetShown by remember { mutableStateOf(false) }
    var isSettingsSheetShown by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var preSelectedCategoryId by remember { mutableStateOf<Int?>(null) }
    
    var isCategoryManagerShown by remember { mutableStateOf(false) }
    var selectedCategoryForAction by remember { mutableStateOf<Category?>(null) }
    var isCategoryActionDialogShown by remember { mutableStateOf(false) }
    var isEditCategoryNameDialogShown by remember { mutableStateOf(false) }
    var isAddCategoryDialogShown by remember { mutableStateOf(false) }

    val onPlayTap = { SoundManager.playTap() }

    if (isCategoryManagerShown) {
        CategoryManagerBottomSheet(
            categories = categories,
            onDismiss = { isCategoryManagerShown = false },
            onDeleteCategory = { cat ->
                selectedCategoryForAction = cat
                isCategoryActionDialogShown = true
            },
            onAddCategoryClick = { isAddCategoryDialogShown = true },
            onEditCategoryClick = { cat ->
                selectedCategoryForAction = cat
                isEditCategoryNameDialogShown = true
            },
            onReorder = { from, to -> viewModel.reorderCategories(from, to) },
            isDarkTheme = isDarkTheme
        )
    }

    if (isAddCategoryDialogShown) {
        AddCategoryDialog(
            onDismiss = { isAddCategoryDialogShown = false },
            onSave = { name, colorHex -> viewModel.addCategory(name, colorHex) }
        )
    }

    if (isAddTaskSheetShown) {
        AddTaskSheet(
            viewModel = viewModel,
            taskToEdit = taskToEdit,
            existingSubtasks = taskToEdit?.let { t -> subtasks.filter { it.taskId == t.id } } ?: emptyList(),
            categories = categories,
            initialCategoryId = preSelectedCategoryId,
            onDismiss = { 
                isAddTaskSheetShown = false 
                preSelectedCategoryId = null
                taskToEdit = null
            },
            onSave = { taskId, title, desc, catId, reminderTime, repeatType, subs ->
                if (taskId != null) {
                    viewModel.editTask(taskId, title, desc, catId, reminderTime, repeatType, subs)
                } else {
                    viewModel.addTask(title, desc, catId, reminderTime, repeatType, subs.map { it.title })
                }
            },
            onAddCategory = { name, colorHex -> viewModel.addCategory(name, colorHex) }
        )
    }


    if (isSettingsSheetShown) {
        SettingsBottomSheet(
            viewModel = viewModel,
            onDismiss = { isSettingsSheetShown = false },
            onManageCategories = {
                isSettingsSheetShown = false
                isCategoryManagerShown = true
            }
        )
    }


    // Category Options Dialog
    if (isCategoryActionDialogShown && selectedCategoryForAction != null) {
        val cat = selectedCategoryForAction!!
        val locale = java.util.Locale.getDefault()
        val layoutDirection = if (locale.language == "fa") androidx.compose.ui.unit.LayoutDirection.Rtl else androidx.compose.ui.unit.LayoutDirection.Ltr
        androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides layoutDirection) {
            AlertDialog(
                onDismissRequest = { isCategoryActionDialogShown = false },
                title = { Text(stringResource(R.string.manage_category_prefix) + " " + cat.name, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                text = { Text(stringResource(R.string.category_options_desc)) },
                confirmButton = {
                    Button(
                        onClick = {
                            isCategoryActionDialogShown = false
                            isEditCategoryNameDialogShown = true
                        }
                    ) {
                        Text(stringResource(R.string.edit_name))
                    }
                },
                dismissButton = {
                    Row {
                        TextButton(
                            onClick = {
                                viewModel.deleteCategory(cat)
                                isCategoryActionDialogShown = false
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text(stringResource(R.string.delete_category))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = { isCategoryActionDialogShown = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                }
            )
        }
    }

    // Edit Category Name Dialog
    if (isEditCategoryNameDialogShown && selectedCategoryForAction != null) {
        val cat = selectedCategoryForAction!!
        var newName by remember { mutableStateOf(cat.name) }
        val locale = java.util.Locale.getDefault()
        val layoutDirection = if (locale.language == "fa") androidx.compose.ui.unit.LayoutDirection.Rtl else androidx.compose.ui.unit.LayoutDirection.Ltr
        androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides layoutDirection) {
            AlertDialog(
                onDismissRequest = { isEditCategoryNameDialogShown = false },
                title = { Text(stringResource(R.string.edit_category_title), fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text(stringResource(R.string.new_name)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newName.isNotBlank()) {
                                viewModel.updateCategory(cat.copy(name = newName.trim()))
                                isEditCategoryNameDialogShown = false
                            }
                        },
                        enabled = newName.isNotBlank()
                    ) {
                        Text(stringResource(R.string.save))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isEditCategoryNameDialogShown = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val bgColor = if (isDarkTheme) Color(0xFF0B1120) else Color(0xFFE2E8F0)
        val meshColor1 = if (isDarkTheme) Color(0xFF3B82F6).copy(alpha = 0.35f) else Color(0xFF3B82F6).copy(alpha = 0.25f)
        val meshColor2 = if (isDarkTheme) Color(0xFF8B5CF6).copy(alpha = 0.35f) else Color(0xFF8B5CF6).copy(alpha = 0.25f)
        val meshColor3 = if (isDarkTheme) Color(0xFF10B981).copy(alpha = 0.35f) else Color(0xFF10B981).copy(alpha = 0.25f)

        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = bgColor)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(meshColor1, Color.Transparent),
                    center = Offset(size.width * -0.1f, size.height * -0.1f),
                    radius = size.width * 1.2f
                ),
                center = Offset(size.width * -0.1f, size.height * -0.1f),
                radius = size.width * 1.2f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(meshColor2, Color.Transparent),
                    center = Offset(size.width * 1.1f, size.height * 0.4f),
                    radius = size.width * 1.0f
                ),
                center = Offset(size.width * 1.1f, size.height * 0.4f),
                radius = size.width * 1.0f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(meshColor3, Color.Transparent),
                    center = Offset(size.width * 0.2f, size.height * 1.1f),
                    radius = size.width * 0.9f
                ),
                center = Offset(size.width * 0.2f, size.height * 1.1f),
                radius = size.width * 0.9f
            )
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        val appLanguage by viewModel.appLanguage.collectAsState()
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(
                                text = stringResource(R.string.my_tasks),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            val context = androidx.compose.ui.platform.LocalContext.current
                            val dateStr = remember(appLanguage) { 
                                JalaliCalendar.formatShamsiDate(context, System.currentTimeMillis())
                            }
                            Text(
                                text = dateStr,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            onPlayTap()
                            onNavigateToReminders()
                        }) {
                            Icon(Icons.Rounded.Notifications, contentDescription = stringResource(R.string.manage_reminders))
                        }
                        IconButton(onClick = {
                            onPlayTap()
                            isSettingsSheetShown = true
                        }) {
                            Icon(Icons.Rounded.Settings, contentDescription = stringResource(R.string.settings))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val activeTasks = remember(tasks) { tasks.filter { !it.isCompleted } }
                val completedTasks = remember(tasks) { tasks.filter { it.isCompleted } }
                
                // Pre-group subtasks by taskId for O(1) access during scroll
                val subtasksGrouped = remember(subtasks) { subtasks.groupBy { it.taskId } }
                
                // Pre-group active tasks by categoryId
                val activeTasksGrouped = remember(activeTasks) { activeTasks.groupBy { it.categoryId } }

                if (tasks.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                        EmptyStateView(isDarkTheme = isDarkTheme)
                    }
                } else {
                    var isCompletedSectionExpanded by remember { mutableStateOf(false) }
                    val fallbackPrimary = MaterialTheme.colorScheme.primary
                    val uncategorizedStr = stringResource(R.string.uncategorized)
                    
                    // Memoize categories to display
                    val allCategoriesToDisplay = remember(categories, activeTasksGrouped) {
                        val list = categories.toMutableList()
                        if (activeTasksGrouped[-1]?.isNotEmpty() == true) {
                            list.add(Category(id = -1, name = uncategorizedStr, colorHex = "#94A3B8"))
                        }
                        list
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize().weight(1f).padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 120.dp)
                    ) {
                        allCategoriesToDisplay.forEach { category ->
                            val catId = category.id
                            val catTasks = activeTasksGrouped[catId] ?: emptyList()
                            
                            // Memoize color parsing
                            val catColor = try { Color(android.graphics.Color.parseColor(category.colorHex)) } catch (e: Exception) { fallbackPrimary }

                            if (catTasks.isNotEmpty() || category.id != -1) {
                                item(key = "category_card_$catId") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(Brush.verticalGradient(listOf(catColor.copy(alpha = 0.08f), catColor.copy(alpha = 0.02f))))
                                            .border(1.dp, Brush.linearGradient(listOf(catColor.copy(alpha = 0.2f), Color.Transparent)), RoundedCornerShape(24.dp))
                                            .padding(10.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            CategorySectionHeader(
                                                category = category,
                                                catColor = catColor,
                                                taskCount = catTasks.size,
                                                onLongPress = {
                                                    if (category.id != -1) {
                                                        selectedCategoryForAction = category
                                                        isCategoryActionDialogShown = true
                                                        SoundManager.playTap()
                                                    }
                                                },
                                                onAddClick = {
                                                    if (category.id != -1) {
                                                        preSelectedCategoryId = category.id
                                                        SoundManager.playTap()
                                                        isAddTaskSheetShown = true
                                                    }
                                                }
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            if (catTasks.isEmpty()) {
                                                Text(stringResource(R.string.no_tasks_in_category), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.padding(start = 16.dp))
                                            } else {
                                                catTasks.forEach { task ->
                                                    TaskRowItem(
                                                        task = task,
                                                        subtasks = subtasksGrouped[task.id] ?: emptyList(),
                                                        accentColor = catColor,
                                                        isDarkTheme = isDarkTheme,
                                                        onToggleComplete = { viewModel.toggleTaskCompleted(task) },
                                                        onEdit = { taskToEdit = task; isAddTaskSheetShown = true },
                                                        onDelete = { viewModel.deleteTask(task) },
                                                        onToggleSubtask = { sub -> viewModel.toggleSubtaskCompleted(sub) }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (completedTasks.isNotEmpty()) {
                            item(key = "completed_section_header") {
                                Box(
                                    modifier = Modifier.fillMaxWidth().glassCard(isDarkTheme = isDarkTheme).clickable { isCompletedSectionExpanded = !isCompletedSectionExpanded }
                                ) {
                                    Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = if (isCompletedSectionExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = stringResource(R.string.completed_tasks), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        }
                                        Box(modifier = Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                                            Text(text = completedTasks.size.toPersianDigits(), fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            
                            if (isCompletedSectionExpanded) {
                                items(completedTasks, key = { "completed_${it.id}" }) { task ->
                                    TaskRowItem(
                                        task = task,
                                        subtasks = subtasksGrouped[task.id] ?: emptyList(),
                                        accentColor = MaterialTheme.colorScheme.outline,
                                        isDarkTheme = isDarkTheme,
                                        onToggleComplete = { viewModel.toggleTaskCompleted(task) },
                                        onEdit = { taskToEdit = task; isAddTaskSheetShown = true },
                                        onDelete = { viewModel.deleteTask(task) },
                                        onToggleSubtask = { sub -> viewModel.toggleSubtaskCompleted(sub) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Glassmorphism FAB
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 24.dp, bottom = 24.dp)
                .size(64.dp)
                .glassFab(isDarkTheme = isDarkTheme)
                .clickable { preSelectedCategoryId = null; onPlayTap(); isAddTaskSheetShown = true },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.add_task_title),
                modifier = Modifier.size(32.dp),
                tint = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategorySectionHeader(
    category: Category,
    catColor: Color,
    taskCount: Int,
    onLongPress: () -> Unit,
    onAddClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).combinedClickable(onClick = {}, onLongClick = onLongPress),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(catColor))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = category.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.clip(CircleShape).background(catColor.copy(alpha = 0.15f)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                Text(text = taskCount.toPersianDigits() + stringResource(R.string.tasks_suffix), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = if (category.id == -1) MaterialTheme.colorScheme.onSurfaceVariant else catColor, fontSize = 11.sp)
            }
            if (category.id != -1) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onAddClick, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = catColor, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun TaskRowItem(
    task: Task,
    subtasks: List<Subtask>,
    accentColor: Color,
    isDarkTheme: Boolean,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleSubtask: (Subtask) -> Unit
) {
    var isExpanded by rememberSaveable(task.id) { mutableStateOf(true) }
    val hasSubtasks = subtasks.isNotEmpty()
    val completedSubtasksCount = subtasks.count { it.isCompleted }
    val progressFraction = if (hasSubtasks) completedSubtasksCount.toFloat() / subtasks.size else 0f
    val clockSuffixStr = stringResource(R.string.clock_suffix)
    val formattedReminder = task.reminderTime?.let {
        val dateStr = JalaliCalendar.formatShamsiDateShort(it)
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val timeStr = timeFormatter.format(Date(it)).toPersianDigits()
        "⏰ $dateStr" + clockSuffixStr + timeStr
    }

    Column(
        modifier = Modifier.fillMaxWidth().glassCard(shape = RoundedCornerShape(12.dp), isDarkTheme = isDarkTheme).clickable { if (hasSubtasks) isExpanded = !isExpanded else SoundManager.playTap() }.padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleComplete() },
                colors = CheckboxDefaults.colors(checkedColor = accentColor),
                modifier = Modifier.size(32.dp).scale(0.85f)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )
                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 1.dp),
                        lineHeight = 14.sp
                    )
                }
                Row(modifier = Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (formattedReminder != null && !task.isCompleted) {
                        Text(text = formattedReminder, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                    }
                    if (hasSubtasks) {
                        Text(text = "🔗 " + completedSubtasksCount.toPersianDigits() + stringResource(R.string.of) + subtasks.size.toPersianDigits() + stringResource(R.string.subtasks_suffix), fontSize = 10.sp, color = accentColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) { Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), modifier = Modifier.size(16.dp)) }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) { Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f), modifier = Modifier.size(16.dp)) }
            }
        }
        if (hasSubtasks && !task.isCompleted) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                LinearProgressIndicator(progress = { progressFraction }, modifier = Modifier.weight(1f).height(4.dp).clip(CircleShape), color = accentColor, trackColor = accentColor.copy(alpha = 0.15f))
                Text(text = "${(progressFraction * 100).toInt().toPersianDigits()}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = accentColor)
            }
            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    subtasks.forEach { sub ->
                        Row(modifier = Modifier.fillMaxWidth().padding(start = 12.dp, top = 1.dp, bottom = 1.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.width(16.dp).height(20.dp)) {
                                Box(modifier = Modifier.align(Alignment.CenterStart).width(1.dp).fillMaxHeight().background(accentColor.copy(alpha = 0.3f)))
                                Box(modifier = Modifier.align(Alignment.CenterStart).width(10.dp).height(1.dp).background(accentColor.copy(alpha = 0.3f)))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)).clickable { onToggleSubtask(sub) }.padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Checkbox(checked = sub.isCompleted, onCheckedChange = { onToggleSubtask(sub) }, modifier = Modifier.size(20.dp).scale(0.85f), colors = CheckboxDefaults.colors(checkedColor = accentColor))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = sub.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    textDecoration = if (sub.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                    color = if (sub.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(isDarkTheme: Boolean) {
    Box(modifier = Modifier.fillMaxWidth().padding(32.dp).glassCard(isDarkTheme = isDarkTheme)) {
        Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(imageVector = Icons.Default.TaskAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(R.string.no_tasks_yet), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = stringResource(R.string.add_first_task_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, lineHeight = 20.sp)
        }
    }
}
