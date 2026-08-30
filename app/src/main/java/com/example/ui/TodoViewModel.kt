package com.example.ui

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.Keep
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.AppDatabase
import com.example.data.Category
import com.example.data.Subtask
import com.example.data.Task
import com.example.data.TodoRepository
import com.example.util.JalaliCalendar
import com.example.util.ReminderScheduler
import com.example.util.SoundManager
import com.squareup.moshi.Json
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
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Keep
data class BackupData(
    @Json(name = "categories") val categories: List<Category> = emptyList(),
    @Json(name = "tasks") val tasks: List<Task> = emptyList(),
    @Json(name = "subtasks") val subtasks: List<Subtask> = emptyList()
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

    private fun parseBackupJson(json: String): BackupData? {
        try {
            val root = JSONObject(json)
            val categories = mutableListOf<Category>()
            val tasks = mutableListOf<Task>()
            val subtasks = mutableListOf<Subtask>()

            // 1. Categories (Standard: "categories", Obfuscated: "a")
            val catArray = when {
                root.has("categories") -> root.optJSONArray("categories")
                root.has("a") -> root.optJSONArray("a")
                else -> null
            }
            if (catArray != null) {
                for (i in 0 until catArray.length()) {
                    val obj = catArray.getJSONObject(i)
                    val id = if (obj.has("id")) obj.optInt("id", 0) else obj.optInt("a", 0)
                    val name = if (obj.has("name")) obj.optString("name", "") else obj.optString("b", "")
                    val colorHex = if (obj.has("colorHex")) obj.optString("colorHex", "#94A3B8") else obj.optString("c", "#94A3B8")
                    val isDefault = if (obj.has("isDefault")) obj.optBoolean("isDefault", false) else obj.optBoolean("d", false)
                    val orderIndex = if (obj.has("orderIndex")) obj.optInt("orderIndex", 0) else obj.optInt("e", 0)
                    if (name.isNotBlank()) {
                        categories.add(Category(id, name, colorHex, isDefault, orderIndex))
                    }
                }
            }

            // 2. Tasks (Standard: "tasks", Obfuscated: "b")
            val taskArray = when {
                root.has("tasks") -> root.optJSONArray("tasks")
                root.has("b") -> root.optJSONArray("b")
                else -> null
            }
            if (taskArray != null) {
                for (i in 0 until taskArray.length()) {
                    val obj = taskArray.getJSONObject(i)
                    val id = if (obj.has("id")) obj.optInt("id", 0) else obj.optInt("a", 0)
                    val title = if (obj.has("title")) obj.optString("title", "") else obj.optString("b", "")
                    val description = if (obj.has("description")) obj.optString("description", "") else obj.optString("c", "")
                    val categoryId = if (obj.has("categoryId")) obj.optInt("categoryId", 0) else obj.optInt("d", 0)
                    val isCompleted = if (obj.has("isCompleted")) obj.optBoolean("isCompleted", false) else obj.optBoolean("e", false)
                    
                    val reminderTime = when {
                        obj.has("reminderTime") && !obj.isNull("reminderTime") -> obj.optLong("reminderTime")
                        obj.has("f") && !obj.isNull("f") -> obj.optLong("f")
                        else -> null
                    }
                    val repeatType = when {
                        obj.has("repeatType") && !obj.isNull("repeatType") -> obj.optString("repeatType")
                        obj.has("g") && !obj.isNull("g") -> obj.optString("g")
                        else -> null
                    }
                    val createdAt = when {
                        obj.has("createdAt") -> obj.optLong("createdAt", System.currentTimeMillis())
                        obj.has("h") -> obj.optLong("h", System.currentTimeMillis())
                        else -> System.currentTimeMillis()
                    }
                    
                    if (title.isNotBlank()) {
                        tasks.add(Task(id, title, description, categoryId, isCompleted, reminderTime, repeatType, createdAt))
                    }
                }
            }

            // 3. Subtasks (Standard: "subtasks", Obfuscated: "c")
            val subArray = when {
                root.has("subtasks") -> root.optJSONArray("subtasks")
                root.has("c") -> root.optJSONArray("c")
                else -> null
            }
            if (subArray != null) {
                for (i in 0 until subArray.length()) {
                    val obj = subArray.getJSONObject(i)
                    val id = if (obj.has("id")) obj.optInt("id", 0) else obj.optInt("a", 0)
                    val taskId = if (obj.has("taskId")) obj.optInt("taskId", 0) else obj.optInt("b", 0)
                    val title = if (obj.has("title")) obj.optString("title", "") else obj.optString("c", "")
                    val isCompleted = if (obj.has("isCompleted")) obj.optBoolean("isCompleted", false) else obj.optBoolean("d", false)
                    if (title.isNotBlank()) {
                        subtasks.add(Subtask(id, taskId, title, isCompleted))
                    }
                }
            }

            if (categories.isNotEmpty() || tasks.isNotEmpty() || subtasks.isNotEmpty()) {
                return BackupData(categories, tasks, subtasks)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fallback to standard Moshi parser
        return try {
            backupAdapter.fromJson(json)
        } catch (e: Exception) {
            null
        }
    }

    fun exportBackup() {
        viewModelScope.launch {
            try {
                val data = BackupData(
                    categories = repository.getCategoriesList(),
                    tasks = repository.getAllTasksList(),
                    subtasks = repository.getAllSubtasksList()
                )
                val json = backupAdapter.toJson(data)
                
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val fileName = "MyTasks_Backup_$timeStamp.json"
                
                withContext(Dispatchers.IO) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val contentValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                            put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                        }
                        val resolver = getApplication<Application>().contentResolver
                        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                            ?: throw Exception("Failed to create MediaStore entry in Downloads")
                        resolver.openOutputStream(uri)?.use { stream ->
                            stream.write(json.toByteArray(Charsets.UTF_8))
                        }
                    } else {
                        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        if (!downloadsDir.exists()) downloadsDir.mkdirs()
                        val file = File(downloadsDir, fileName)
                        file.writeText(json, Charsets.UTF_8)
                    }
                }
                
                SoundManager.playSuccess()
                Toast.makeText(
                    getApplication(),
                    "${getApplication<Application>().getString(R.string.backup_success)}\n$fileName",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(getApplication(), getApplication<Application>().getString(R.string.backup_failed), Toast.LENGTH_LONG).show()
            }
        }
    }

    fun importBackup(json: String) {
        viewModelScope.launch {
            try {
                val data = parseBackupJson(json) ?: throw Exception("Invalid data format")
                
                data.categories.forEach { repository.insertCategory(it) }
                data.tasks.forEach { task ->
                    repository.insertTaskDirectly(task)
                    if (!task.isCompleted && task.reminderTime != null && task.reminderTime > System.currentTimeMillis()) {
                        ReminderScheduler.schedule(getApplication(), task)
                    }
                }
                data.subtasks.forEach { repository.insertSubtask(it) }
                
                SoundManager.playSuccess()
                Toast.makeText(getApplication(), getApplication<Application>().getString(R.string.restore_success), Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(getApplication(), getApplication<Application>().getString(R.string.restore_failed), Toast.LENGTH_LONG).show()
            }
        }
    }

    private val _updateCheckResult = MutableStateFlow<UpdateCheckResult?>(null)
    val updateCheckResult: StateFlow<UpdateCheckResult?> = _updateCheckResult.asStateFlow()

    private val _isCheckingUpdate = MutableStateFlow(false)
    val isCheckingUpdate: StateFlow<Boolean> = _isCheckingUpdate.asStateFlow()

    fun clearUpdateResult() {
        _updateCheckResult.value = null
    }

    fun checkForUpdates(isManual: Boolean = true) {
        if (_isCheckingUpdate.value) return
        _isCheckingUpdate.value = true
        _updateCheckResult.value = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentVersionName = com.example.BuildConfig.VERSION_NAME
                val url = java.net.URL("https://api.github.com/repos/omid-io/Android-Todo-App/releases/latest")
                val connection = (url.openConnection() as java.net.HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", "MyTasks-Android-App")
                    setRequestProperty("Accept", "application/vnd.github.v3+json")
                    connectTimeout = 8000
                    readTimeout = 8000
                }

                if (connection.responseCode == 200) {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(responseText)
                    val tagName = json.optString("tag_name", "").trim()
                    val remoteVersion = tagName.removePrefix("v").removePrefix("V").trim()
                    val body = json.optString("body", "")
                    val htmlUrl = json.optString("html_url", "https://github.com/omid-io/Android-Todo-App/releases/latest")
                    
                    var apkDownloadUrl = htmlUrl
                    val assets = json.optJSONArray("assets")
                    if (assets != null) {
                        for (i in 0 until assets.length()) {
                            val assetObj = assets.getJSONObject(i)
                            val name = assetObj.optString("name", "")
                            if (name.endsWith(".apk", ignoreCase = true)) {
                                apkDownloadUrl = assetObj.optString("browser_download_url", htmlUrl)
                                break
                            }
                        }
                    }

                    val isNewer = isVersionGreater(remoteVersion, currentVersionName)
                    withContext(Dispatchers.Main) {
                        _isCheckingUpdate.value = false
                        if (isNewer) {
                            _updateCheckResult.value = UpdateCheckResult.NewVersionAvailable(
                                latestVersion = remoteVersion,
                                releaseNotes = body,
                                downloadUrl = apkDownloadUrl
                            )
                        } else {
                            if (isManual) {
                                _updateCheckResult.value = UpdateCheckResult.UpToDate(currentVersionName)
                                Toast.makeText(
                                    getApplication(),
                                    getApplication<Application>().getString(R.string.latest_version_installed, currentVersionName),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _isCheckingUpdate.value = false
                        if (isManual) {
                            _updateCheckResult.value = UpdateCheckResult.Error("HTTP ${connection.responseCode}")
                            Toast.makeText(
                                getApplication(),
                                getApplication<Application>().getString(R.string.check_update_failed),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _isCheckingUpdate.value = false
                    if (isManual) {
                        _updateCheckResult.value = UpdateCheckResult.Error(e.localizedMessage ?: "Error")
                        Toast.makeText(
                            getApplication(),
                            getApplication<Application>().getString(R.string.check_update_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun isVersionGreater(remote: String, local: String): Boolean {
        try {
            val remoteParts = remote.split(".").mapNotNull { it.toIntOrNull() }
            val localParts = local.split(".").mapNotNull { it.toIntOrNull() }
            val maxLen = maxOf(remoteParts.size, localParts.size)
            for (i in 0 until maxLen) {
                val r = remoteParts.getOrElse(i) { 0 }
                val l = localParts.getOrElse(i) { 0 }
                if (r > l) return true
                if (r < l) return false
            }
            return false
        } catch (e: Exception) {
            return remote != local
        }
    }
}

sealed class UpdateCheckResult {
    data class UpToDate(val currentVersion: String) : UpdateCheckResult()
    data class NewVersionAvailable(
        val latestVersion: String,
        val releaseNotes: String,
        val downloadUrl: String
    ) : UpdateCheckResult()
    data class Error(val message: String) : UpdateCheckResult()
}
