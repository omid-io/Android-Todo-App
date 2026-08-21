package com.example.ui

import com.example.R

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.Category
import com.example.data.Subtask
import com.example.data.Task
import com.example.util.JalaliCalendar
import com.example.util.toPersianDigits
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTaskSheet(
    viewModel: TodoViewModel,
    taskToEdit: Task? = null,
    existingSubtasks: List<Subtask> = emptyList(),
    categories: List<Category>,
    initialCategoryId: Int? = null,
    onDismiss: () -> Unit,
    onSave: (
        taskId: Int?,
        title: String,
        description: String,
        categoryId: Int,
        reminderTime: Long?,
        repeatType: String?,
        subtasks: List<Subtask>
    ) -> Unit,
    onAddCategory: (String, String) -> Unit
) {
    val context = LocalContext.current
    val isDark by viewModel.isDarkTheme.collectAsState()

    var title by remember { mutableStateOf(taskToEdit?.title ?: "") }
    var description by remember { mutableStateOf(taskToEdit?.description ?: "") }
    
    // Default category choice
    var selectedCategoryId by remember { 
        mutableStateOf(taskToEdit?.categoryId ?: initialCategoryId ?: categories.firstOrNull()?.id ?: 0) 
    }
    
    // Ensure we sync when categories list changes/seeds
    LaunchedEffect(categories) {
        if (selectedCategoryId == 0 && categories.isNotEmpty()) {
            selectedCategoryId = categories.first().id
        }
    }

    var isReminderEnabled by remember { mutableStateOf(taskToEdit?.reminderTime != null) }

    // Jalali Date Picker States
    val initialDateJalali = remember(taskToEdit) {
        if (taskToEdit?.reminderTime != null) {
            val cal = Calendar.getInstance()
            cal.timeInMillis = taskToEdit.reminderTime
            JalaliCalendar.g2j(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
        } else {
            JalaliCalendar.getNowJalali()
        }
    }
    var jalaliYear by remember { mutableStateOf(initialDateJalali.year) }
    var jalaliMonth by remember { mutableStateOf(initialDateJalali.month) }
    var jalaliDay by remember { mutableStateOf(initialDateJalali.day) }
    var isDatePickerShown by remember { mutableStateOf(false) }
    var isTimePickerShown by remember { mutableStateOf(false) }

    // Time Picker States
    val initialCalendar = remember(taskToEdit) {
        val cal = Calendar.getInstance()
        if (taskToEdit?.reminderTime != null) {
            cal.timeInMillis = taskToEdit.reminderTime
        }
        cal
    }
    var hour by remember { mutableStateOf(initialCalendar.get(Calendar.HOUR_OF_DAY)) }
    var minute by remember { mutableStateOf(initialCalendar.get(Calendar.MINUTE)) }

    // Repeat Frequency
    var repeatType by remember { mutableStateOf(taskToEdit?.repeatType?.substringBefore(':') ?: "none") } 
    var everyXDays by remember { 
        mutableStateOf(
            if (taskToEdit?.repeatType?.startsWith("every_other_day") == true) {
                taskToEdit.repeatType.substringAfter(':', "2").toIntOrNull() ?: 2
            } else 2
        ) 
    }

    // Inline custom category dialog
    var isAddCategoryDialogShown by remember { mutableStateOf(false) }

    // Nested Subtasks being created
    var newSubtaskTitle by remember { mutableStateOf("") }
    val tempSubtasks = remember { mutableStateListOf<Subtask>().apply { addAll(existingSubtasks) } }

    if (isDatePickerShown) {
        ShamsiDatePickerDialog(
            initialYear = jalaliYear,
            initialMonth = jalaliMonth,
            initialDay = jalaliDay,
            onDismiss = { isDatePickerShown = false },
            onDateSelected = { y, m, d ->
                jalaliYear = y
                jalaliMonth = m
                jalaliDay = d
            }
        )
    }

    if (isTimePickerShown) {
        AppTimePickerDialog(
            initialHour = hour,
            initialMinute = minute,
            onDismiss = { isTimePickerShown = false },
            onTimeSelected = { h, m ->
                hour = h
                minute = m
            }
        )
    }

    if (isAddCategoryDialogShown) {
        AddCategoryDialog(
            onDismiss = { isAddCategoryDialogShown = false },
            onSave = { name, colorHex ->
                onAddCategory(name, colorHex)
            }
        )
    }

    val locale = java.util.Locale.getDefault()
    val layoutDirection = if (locale.language == "fa") androidx.compose.ui.unit.LayoutDirection.Rtl else androidx.compose.ui.unit.LayoutDirection.Ltr

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides layoutDirection) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = if (isDark) Color(0xFF0F172A) else Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.add_task_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color.Black
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).background(if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(R.string.close), tint = if (isDark) Color.White else Color.Black, modifier = Modifier.size(18.dp))
                    }
                }

                // General Info Card
                Box(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).glassCard(shape = RoundedCornerShape(16.dp), isDarkTheme = isDark)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.main_info), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = if (isDark) Color.White else Color.Black)
                        }

                        TextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text(stringResource(R.string.task_title_label), fontWeight = FontWeight.Medium, fontSize = 12.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            textStyle = MaterialTheme.typography.bodyMedium,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC),
                                unfocusedContainerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.5f) else Color.White,
                                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = if (isDark) Color.White else Color.Black,
                                unfocusedTextColor = if (isDark) Color.White else Color.Black
                            ),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).heightIn(min = 52.dp).border(1.dp, if(isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        )

                        TextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text(stringResource(R.string.task_desc_label), fontWeight = FontWeight.Medium, fontSize = 12.sp) },
                            maxLines = 2,
                            shape = RoundedCornerShape(12.dp),
                            textStyle = MaterialTheme.typography.bodySmall,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC),
                                unfocusedContainerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.5f) else Color.White,
                                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = if (isDark) Color.White else Color.Black,
                                unfocusedTextColor = if (isDark) Color.White else Color.Black
                            ),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).border(1.dp, if(isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        )
                    }
                }

                // Category selection
                Box(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).glassCard(shape = RoundedCornerShape(16.dp), isDarkTheme = isDark)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                            Icon(Icons.Default.Menu, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.task_category), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = if (isDark) Color.White else Color.Black)
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            categories.forEach { category ->
                                val isSelected = category.id == selectedCategoryId
                                val parsedColor = try {
                                    Color(android.graphics.Color.parseColor(category.colorHex))
                                } catch (e: Exception) {
                                    MaterialTheme.colorScheme.primary
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) parsedColor.copy(alpha = 0.2f)
                                            else (if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f))
                                        )
                                        .border(
                                            width = if (isSelected) 1.dp else 0.5.dp,
                                            color = if (isSelected) parsedColor else Color.Transparent,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { selectedCategoryId = category.id }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = category.name,
                                        color = if (isSelected) parsedColor else (if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f)),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            IconButton(
                                onClick = { isAddCategoryDialogShown = true },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = stringResource(R.string.add_category_content_desc),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Subtasks creation section
                Box(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).glassCard(shape = RoundedCornerShape(16.dp), isDarkTheme = isDark)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.subtasks_chain), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = if (isDark) Color.White else Color.Black)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(
                                value = newSubtaskTitle,
                                onValueChange = { newSubtaskTitle = it },
                                label = { Text(stringResource(R.string.subtask_title_label), fontSize = 12.sp) },
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                                ),
                                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                    onDone = {
                                        if (newSubtaskTitle.isNotBlank()) {
                                            tempSubtasks.add(Subtask(taskId = taskToEdit?.id ?: 0, title = newSubtaskTitle.trim()))
                                            newSubtaskTitle = ""
                                        }
                                    }
                                ),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = MaterialTheme.typography.bodySmall,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC),
                                    unfocusedContainerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.5f) else Color.White,
                                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = if (isDark) Color.White else Color.Black,
                                    unfocusedTextColor = if (isDark) Color.White else Color.Black
                                ),
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp).border(1.dp, if(isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = {
                                    if (newSubtaskTitle.isNotBlank()) {
                                        tempSubtasks.add(Subtask(taskId = taskToEdit?.id ?: 0, title = newSubtaskTitle.trim()))
                                        newSubtaskTitle = ""
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text(stringResource(R.string.add), fontSize = 13.sp)
                            }
                        }

                        if (tempSubtasks.isNotEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                tempSubtasks.forEachIndexed { index, subtask ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f))
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${(index + 1).toPersianDigits()}- ${subtask.title}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isDark) Color.White else Color.Black
                                        )
                                        IconButton(
                                            onClick = { tempSubtasks.removeAt(index) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
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

                // Reminder Activation Switch
                Box(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).glassCard(shape = RoundedCornerShape(16.dp), isDarkTheme = isDark)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.reminder_alarm),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else Color.Black
                                )
                            }
                            Switch(
                                checked = isReminderEnabled,
                                onCheckedChange = { isReminderEnabled = it },
                                modifier = Modifier.scale(0.8f)
                            )
                        }

                        if (isReminderEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))

                            // Date & Time Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = { isDatePickerShown = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    val timeMillis = JalaliCalendar.j2g(jalaliYear, jalaliMonth, jalaliDay).timeInMillis
                                    Text(
                                        text = "📅 " + JalaliCalendar.formatShamsiDateShort(timeMillis),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Button(
                                    onClick = { isTimePickerShown = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    val formattedTime = String.format(java.util.Locale.US, "%02d:%02d", hour, minute).toPersianDigits()
                                    Text(
                                        text = stringResource(R.string.clock_hour) + formattedTime,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Repeat choice
                            Text(
                                text = stringResource(R.string.repeat_reminder),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp),
                                color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val repeats = listOf(
                                    "none" to stringResource(R.string.no_repeat),
                                    "daily" to stringResource(R.string.daily),
                                    "every_other_day" to stringResource(R.string.repeat_every_other_day),
                                    "weekly" to stringResource(R.string.weekly)
                                )

                                repeats.forEach { (type, label) ->
                                    val selected = repeatType == type
                                    ElevatedFilterChip(
                                        selected = selected,
                                        onClick = { repeatType = type },
                                        label = { Text(label, fontSize = 9.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            
                            if (repeatType == "every_other_day") {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.every_x_days_label),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isDark) Color.White else Color.Black
                                    )
                                    
                                    var textValue by remember { mutableStateOf(everyXDays.toString()) }
                                    TextField(
                                        value = textValue,
                                        onValueChange = { 
                                            if (it.length <= 3 && it.all { char -> char.isDigit() }) {
                                                textValue = it
                                                everyXDays = it.toIntOrNull() ?: 1
                                            }
                                        },
                                        modifier = Modifier.width(60.dp).height(42.dp),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp),
                                        textStyle = MaterialTheme.typography.bodySmall.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                                        colors = TextFieldDefaults.colors(
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            focusedContainerColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f),
                                            unfocusedContainerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f)
                                        ),
                                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                        )
                                    )
                                    
                                    Text(
                                        text = stringResource(R.string.days_suffix),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isDark) Color.White else Color.Black
                                    )
                                }
                            }

                            if (repeatType != "none") {
                                Spacer(modifier = Modifier.height(6.dp))
                                val repeatDesc = when (repeatType) {
                                    "daily" -> stringResource(R.string.repeat_desc_daily)
                                    "every_other_day" -> stringResource(R.string.repeat_desc_x_days, everyXDays.toPersianDigits())
                                    "weekly" -> stringResource(R.string.repeat_desc_weekly)
                                    else -> ""
                                }
                                Text(
                                    text = repeatDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text(stringResource(R.string.cancel), fontSize = 14.sp)
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                var finalReminderTime: Long? = null
                                if (isReminderEnabled) {
                                    val cal = JalaliCalendar.j2g(jalaliYear, jalaliMonth, jalaliDay)
                                    cal.set(Calendar.HOUR_OF_DAY, hour)
                                    cal.set(Calendar.MINUTE, minute)
                                    cal.set(Calendar.SECOND, 0)
                                    cal.set(Calendar.MILLISECOND, 0)
                                    finalReminderTime = cal.timeInMillis
                                }

                                val finalRepeatType = if (isReminderEnabled) {
                                    if (repeatType == "every_other_day") "every_other_day:$everyXDays"
                                    else repeatType
                                } else null

                                onSave(
                                    taskToEdit?.id,
                                    title.trim(),
                                    description.trim(),
                                    selectedCategoryId,
                                    finalReminderTime,
                                    finalRepeatType,
                                    tempSubtasks.toList()
                                )
                                onDismiss()
                            }
                        },
                        enabled = title.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text(stringResource(R.string.save_task), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onTimeSelected: (Int, Int) -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.time),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                TimePicker(state = state)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                    TextButton(onClick = {
                        onTimeSelected(state.hour, state.minute)
                        onDismiss()
                    }) {
                        Text(stringResource(R.string.confirm))
                    }
                }
            }
        }
    }
}
