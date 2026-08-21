package com.example.data

import android.content.Context
import com.example.util.ReminderScheduler
import com.example.util.SoundManager
import kotlinx.coroutines.flow.Flow

class TodoRepository(
    private val todoDao: TodoDao,
    private val context: Context
) {
    val categories: Flow<List<Category>> = todoDao.getCategories()
    val tasks: Flow<List<Task>> = todoDao.getTasks()
    val subtasks: Flow<List<Subtask>> = todoDao.getSubtasks()

    suspend fun getCategoriesList(): List<Category> {
        return todoDao.getCategoriesList()
    }

    suspend fun getAllTasksList(): List<Task> {
        return todoDao.getAllTasksList()
    }

    suspend fun getAllSubtasksList(): List<Subtask> {
        val list = mutableListOf<Subtask>()
        // We need a DAO method for all subtasks if not present
        return todoDao.getAllSubtasksList()
    }

    suspend fun insertTaskDirectly(task: Task) {
        todoDao.insertTask(task)
    }

    suspend fun insertCategory(category: Category): Long {
        return todoDao.insertCategory(category)
    }

    suspend fun updateCategory(category: Category) {
        todoDao.updateCategory(category)
    }

    suspend fun deleteCategory(category: Category) {
        todoDao.deleteSubtasksByCategoryId(category.id)
        todoDao.deleteTasksByCategoryId(category.id)
        todoDao.deleteCategory(category)
        SoundManager.playDelete()
    }

    suspend fun insertTask(task: Task, subtasksList: List<String> = emptyList()) {
        val taskId = todoDao.insertTask(task).toInt()
        val createdTask = task.copy(id = taskId)
        
        for (subName in subtasksList) {
            if (subName.isNotBlank()) {
                todoDao.insertSubtask(Subtask(taskId = taskId, title = subName))
            }
        }
        
        if (createdTask.reminderTime != null) {
            ReminderScheduler.schedule(context, createdTask)
        }
        SoundManager.playTap()
    }

    suspend fun updateTask(task: Task) {
        todoDao.updateTask(task)
        if (task.isCompleted) {
            ReminderScheduler.cancel(context, task.id)
        } else if (task.reminderTime != null) {
            ReminderScheduler.schedule(context, task)
        } else {
            ReminderScheduler.cancel(context, task.id)
        }
    }

    suspend fun toggleTaskCompleted(task: Task) {
        val updated = task.copy(isCompleted = !task.isCompleted)
        todoDao.updateTask(updated)
        
        if (updated.isCompleted) {
            ReminderScheduler.cancel(context, task.id)
            SoundManager.playSuccess()
        } else {
            if (updated.reminderTime != null) {
                ReminderScheduler.schedule(context, updated)
            }
            SoundManager.playTap()
        }
    }

    suspend fun deleteTask(task: Task) {
        todoDao.deleteTask(task)
        todoDao.deleteSubtasksByTaskId(task.id)
        ReminderScheduler.cancel(context, task.id)
        SoundManager.playDelete()
    }

    suspend fun insertSubtask(subtask: Subtask) {
        todoDao.insertSubtask(subtask)
        SoundManager.playTap()
    }

    suspend fun updateSubtask(subtask: Subtask) {
        todoDao.updateSubtask(subtask)
    }

    suspend fun toggleSubtaskCompleted(subtask: Subtask) {
        val updated = subtask.copy(isCompleted = !subtask.isCompleted)
        todoDao.updateSubtask(updated)
        
        if (updated.isCompleted) {
            SoundManager.playSuccess()
        } else {
            SoundManager.playTap()
        }
    }

    suspend fun deleteSubtask(subtask: Subtask) {
        todoDao.deleteSubtask(subtask)
        SoundManager.playDelete()
    }

    suspend fun seedDefaultCategoriesIfEmpty() {
        val count = todoDao.getCategoryCount()
        if (count == 0) {
            val isPersian = java.util.Locale.getDefault().language.startsWith("fa")
            
            val categoriesToSeed = if (isPersian) {
                listOf(
                    Category(name = "🔴 فوری", colorHex = "#E53935", isDefault = true),
                    Category(name = "🏠 کارهای خانه", colorHex = "#8D6E63", isDefault = true),
                    Category(name = "💻 کاری", colorHex = "#1E88E5", isDefault = true)
                )
            } else {
                listOf(
                    Category(name = "🔴 Urgent", colorHex = "#E53935", isDefault = true),
                    Category(name = "🏠 Household", colorHex = "#8D6E63", isDefault = true),
                    Category(name = "💻 Work", colorHex = "#1E88E5", isDefault = true)
                )
            }

            val catIds = mutableListOf<Long>()
            for (cat in categoriesToSeed) {
                catIds.add(todoDao.insertCategory(cat))
            }

            val cats = todoDao.getCategoriesList()
            if (cats.isNotEmpty()) {
                val catFuriId = cats.find { it.name.contains("فوری") || it.name.contains("Urgent") }?.id ?: cats[0].id
                val catKhoneId = cats.find { it.name.contains("خانه") || it.name.contains("Household") }?.id ?: cats[0].id
                val catKarId = cats.find { it.name.contains("کاری") || it.name.contains("Work") }?.id ?: cats[0].id

                if (isPersian) {
                    val taskId1 = todoDao.insertTask(Task(title = "برنامه‌ریزی هفته جدید", description = "تنظیم اهداف و بررسی کارهای هفته آینده", categoryId = catKarId))
                    todoDao.insertSubtask(Subtask(taskId = taskId1.toInt(), title = "تعیین اولویت‌ها"))
                    todoDao.insertSubtask(Subtask(taskId = taskId1.toInt(), title = "ارسال گزارش هفتگی"))

                    todoDao.insertTask(Task(title = "خرید مواد غذایی", description = "شیر، نان، و میوه تازه", categoryId = catKhoneId))
                } else {
                    val taskId1 = todoDao.insertTask(Task(title = "Weekly Planning", description = "Set goals and review upcoming tasks", categoryId = catKarId))
                    todoDao.insertSubtask(Subtask(taskId = taskId1.toInt(), title = "Define priorities"))
                    todoDao.insertSubtask(Subtask(taskId = taskId1.toInt(), title = "Send weekly report"))

                    todoDao.insertTask(Task(title = "Buy Groceries", description = "Milk, bread, and fresh fruits", categoryId = catKhoneId))
                }
            }
        }
    }
}
