package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.AppDatabase
import com.example.data.Category
import com.example.data.Subtask
import com.example.data.Task
import com.example.data.TodoRepository
import com.example.util.JalaliCalendar
import com.example.util.SoundManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupData(
    val categories: List<Category>,
    val tasks: List<Task>,
    val subtasks: List<Subtask>
)

class TodoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TodoRepository
    private val sharedPrefs = application.getSharedPreferences("todo_prefs", Context.MODE_PRIVATE)

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _appLanguage = MutableStateFlow("system")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    val categories: StateFlow<List<Category>>
    val tasks: StateFlow<List<Task>>
    val subtasks: StateFlow<List<Subtask>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TodoRepository(database.todoDao(), application)

        categories = repository.categories.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        tasks = repository.tasks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        subtasks = repository.subtasks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        _isDarkTheme.value = sharedPrefs.getBoolean("dark_theme", false)
        _appLanguage.value = sharedPrefs.getString("app_language", "system") ?: "system"
        viewModelScope.launch {
            repository.seedDefaultCategoriesIfEmpty()
        }
    }

    fun toggleTheme() {
        SoundManager.playTap()
        val newValue = !_isDarkTheme.value
        _isDarkTheme.value = newValue
        sharedPrefs.edit().putBoolean("dark_theme", newValue).apply()
    }

    fun setLanguage(language: String) {
        SoundManager.playTap()
        _appLanguage.value = language
        sharedPrefs.edit().putString("app_language", language).apply()
    }

    fun addTask(
        title: String,
        description: String,
        categoryId: Int,
        reminderTime: Long?,
        repeatType: String?,
        subtasksList: List<String>
    ) {
        viewModelScope.launch {
            val task = Task(
                title = title,
                description = description,
                categoryId = categoryId,
                reminderTime = reminderTime,
                repeatType = repeatType
            )
            repository.insertTask(task, subtasksList)
        }
    }

    fun editTask(
        taskId: Int,
        title: String,
        description: String,
        categoryId: Int,
        reminderTime: Long?,
        repeatType: String?,
        subtasksList: List<Subtask>
    ) {
        viewModelScope.launch {
            val existingTask = tasks.value.find { it.id == taskId } ?: return@launch
            val updatedTask = existingTask.copy(
                title = title,
                description = description,
                categoryId = categoryId,
                reminderTime = reminderTime,
                repeatType = repeatType
            )
            repository.updateTask(updatedTask)

            val oldSubtasks = subtasks.value.filter { it.taskId == taskId }
            val newIds = subtasksList.map { it.id }.filter { it != 0 }.toSet()

            for (old in oldSubtasks) {
                if (old.id !in newIds) {
                    repository.deleteSubtask(old)
                }
            }

            for (newSub in subtasksList) {
                if (newSub.id == 0) {
                    repository.insertSubtask(newSub)
                } else {
                    val old = oldSubtasks.find { it.id == newSub.id }
                    if (old != null && old.title != newSub.title) {
                        repository.updateSubtask(newSub)
                    }
                }
            }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun toggleTaskCompleted(task: Task) {
        viewModelScope.launch {
            repository.toggleTaskCompleted(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun addSubtask(taskId: Int, title: String) {
        viewModelScope.launch {
            val sub = Subtask(taskId = taskId, title = title)
            repository.insertSubtask(sub)
        }
    }

    fun toggleSubtaskCompleted(subtask: Subtask) {
        viewModelScope.launch {
            repository.toggleSubtaskCompleted(subtask)
        }
    }

    fun deleteSubtask(subtask: Subtask) {
        viewModelScope.launch {
            repository.deleteSubtask(subtask)
        }
    }

    fun addCategory(name: String, colorHex: String) {
        viewModelScope.launch {
            val cat = Category(name = name, colorHex = colorHex)
            repository.insertCategory(cat)
            SoundManager.playTap()
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            repository.updateCategory(category)
            SoundManager.playTap()
        }
    }

    fun moveCategoryUp(category: Category) {
        viewModelScope.launch {
            val list = categories.value.filter { it.id != -1 }.toMutableList()
            val index = list.indexOfFirst { it.id == category.id }
            if (index > 0) {
                val temp = list[index - 1]
                list[index - 1] = list[index]
                list[index] = temp
                
                list.forEachIndexed { i, cat ->
                    if (cat.orderIndex != i) {
                        repository.updateCategory(cat.copy(orderIndex = i))
                    }
                }
                SoundManager.playTap()
            }
        }
    }

    fun moveCategoryDown(category: Category) {
        viewModelScope.launch {
            val list = categories.value.filter { it.id != -1 }.toMutableList()
            val index = list.indexOfFirst { it.id == category.id }
            if (index != -1 && index < list.size - 1) {
                val temp = list[index + 1]
                list[index + 1] = list[index]
                list[index] = temp
                
                list.forEachIndexed { i, cat ->
                    if (cat.orderIndex != i) {
                        repository.updateCategory(cat.copy(orderIndex = i))
                    }
                }
                SoundManager.playTap()
            }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    // --- Backup & Restore ---
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val backupAdapter = moshi.adapter(BackupData::class.java)

    fun exportBackup() {
        viewModelScope.launch {
            try {
                val data = BackupData(
                    categories = repository.getCategoriesList(),
                    tasks = repository.getAllTasksList(),
                    subtasks = repository.getAllSubtasksList()
                )
                val json = backupAdapter.toJson(data)
                
                withContext(Dispatchers.IO) {
                    val fileName = "MyTasks_Backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.json"
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val file = File(downloadsDir, fileName)
                    file.writeText(json)
                }
                
                Toast.makeText(getApplication(), getApplication<Application>().getString(R.string.backup_success), Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(getApplication(), getApplication<Application>().getString(R.string.backup_failed), Toast.LENGTH_LONG).show()
            }
        }
    }

    fun importBackup(json: String) {
        viewModelScope.launch {
            try {
                val data = backupAdapter.fromJson(json) ?: throw Exception("Invalid data")
                
                data.categories.forEach { repository.insertCategory(it) }
                data.tasks.forEach { repository.insertTaskDirectly(it) }
                data.subtasks.forEach { repository.insertSubtask(it) }
                
                Toast.makeText(getApplication(), getApplication<Application>().getString(R.string.restore_success), Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(getApplication(), getApplication<Application>().getString(R.string.restore_failed), Toast.LENGTH_LONG).show()
            }
        }
    }
}
