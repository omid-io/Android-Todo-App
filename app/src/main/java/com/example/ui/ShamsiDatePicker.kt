package com.example.ui

import com.example.R

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.util.JalaliCalendar
import com.example.util.toPersianDigits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShamsiDatePickerDialog(
    initialYear: Int,
    initialMonth: Int,
    initialDay: Int,
    onDismiss: () -> Unit,
    onDateSelected: (Int, Int, Int) -> Unit
) {
    val locale = java.util.Locale.getDefault()
    if (locale.language != "fa") {
        val initialTime = remember { JalaliCalendar.j2g(initialYear, initialMonth, initialDay).timeInMillis }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialTime)
        
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = java.util.Calendar.getInstance()
                        cal.timeInMillis = millis
                        val jDate = JalaliCalendar.g2j(cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH) + 1, cal.get(java.util.Calendar.DAY_OF_MONTH))
                        onDateSelected(jDate.year, jDate.month, jDate.day)
                    }
                    onDismiss()
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
        return
    }

    var selectedYear by remember { mutableStateOf(initialYear) }
    var selectedMonth by remember { mutableStateOf(initialMonth) }
    var selectedDay by remember { mutableStateOf(initialDay) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val months = (1..12).map { it to JalaliCalendar.getPersianMonthName(context, it) }
    val years = (1400..1420).toList()

    val daysInMonth = when (selectedMonth) {
        in 1..6 -> 31
        in 7..11 -> 30
        12 -> {
            // Simple leap year approximation
            val isLeap = (selectedYear % 33) in listOf(1, 5, 9, 13, 17, 22, 26, 30)
            if (isLeap) 30 else 29
        }
        else -> 30
    }

    if (selectedDay > daysInMonth) {
        selectedDay = daysInMonth
    }

    val layoutDirection = if (locale.language == "fa") androidx.compose.ui.unit.LayoutDirection.Rtl else androidx.compose.ui.unit.LayoutDirection.Ltr

    Dialog(onDismissRequest = onDismiss) {
        androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides layoutDirection) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.select_shamsi_date),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Year Selector Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text(text = stringResource(R.string.year), style = MaterialTheme.typography.bodyMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (selectedYear > 1390) selectedYear-- },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text("<", fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = selectedYear.toPersianDigits(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        IconButton(
                            onClick = { if (selectedYear < 1430) selectedYear++ },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text(">", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Month Selector Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text(text = stringResource(R.string.month), style = MaterialTheme.typography.bodyMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (selectedMonth > 1) selectedMonth-- else selectedMonth = 12 },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text("<", fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = JalaliCalendar.getPersianMonthName(context, selectedMonth),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(100.dp).padding(horizontal = 4.dp)
                        )
                        IconButton(
                            onClick = { if (selectedMonth < 12) selectedMonth++ else selectedMonth = 1 },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text(">", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Days Grid
                val firstDayCalendar = remember(selectedYear, selectedMonth) { JalaliCalendar.j2g(selectedYear, selectedMonth, 1) }
                val firstDayOffset = remember(firstDayCalendar) { firstDayCalendar.get(java.util.Calendar.DAY_OF_WEEK) % 7 }
                val weekDays = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    weekDays.forEach { day ->
                        Text(
                            text = day,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (day == "ج") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.height(260.dp).fillMaxWidth()
                ) {
                    items(firstDayOffset) {
                        Spacer(modifier = Modifier.padding(2.dp).aspectRatio(1f))
                    }
                    items(daysInMonth) { index ->
                        val dayNum = index + 1
                        val isSelected = dayNum == selectedDay
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .padding(2.dp)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else Color.Transparent
                                )
                                .clickable { selectedDay = dayNum }
                        ) {
                            Text(
                                text = dayNum.toPersianDigits(),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.outline)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onDateSelected(selectedYear, selectedMonth, selectedDay)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(stringResource(R.string.confirm))
                    }
                }
            }
        }
    }
    }
}
