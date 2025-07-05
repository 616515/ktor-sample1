package com.example

import com.example.model.Task
import com.example.repository.TaskRepository
import kotlin.test.*

class TaskRepositoryTest {

    @BeforeTest
    fun setup() {
        // Reset task repository to initial state before each test
        val originalTasks = listOf(
            Task(id = 1, content = "Learn Ktor", isDone = true),
            Task(id = 2, content = "Build a REST API", isDone = false),
            Task(id = 3, content = "Write Unit Tests", isDone = false)
        )

        // Use reflection to reset the tasks list
        val tasksField = TaskRepository::class.java.getDeclaredField("tasks")
        tasksField.isAccessible = true
        val tasksList = tasksField.get(TaskRepository) as MutableList<Task>
        tasksList.clear()
        tasksList.addAll(originalTasks)
    }

    @Test
    fun testGetAll() {
        val tasks = TaskRepository.getAll()
        assertEquals(3, tasks.size)
        assertEquals("Learn Ktor", tasks[0].content)
        assertEquals("Build a REST API", tasks[1].content)
        assertEquals("Write Unit Tests", tasks[2].content)
    }

    @Test
    fun testGetById() {
        val task = TaskRepository.getById(1)
        assertNotNull(task)
        assertEquals("Learn Ktor", task.content)
        assertTrue(task.isDone)

        val nonExistentTask = TaskRepository.getById(999)
        assertNull(nonExistentTask)
    }

    @Test
    fun testAdd() {
        val newTask = Task(id = 4, content = "New Task", isDone = false)
        TaskRepository.add(newTask)

        val tasks = TaskRepository.getAll()
        assertEquals(4, tasks.size)

        val addedTask = TaskRepository.getById(4)
        assertNotNull(addedTask)
        assertEquals("New Task", addedTask.content)
    }

    @Test
    fun testUpdate() {
        val updatedTask = Task(id = 2, content = "Updated Task", isDone = true)
        TaskRepository.update(2, updatedTask)

        val task = TaskRepository.getById(2)
        assertNotNull(task)
        assertEquals("Updated Task", task.content)
        assertTrue(task.isDone)

        // Try to update non-existent task
        val nonExistentTask = Task(id = 999, content = "Non-existent", isDone = false)
        TaskRepository.update(999, nonExistentTask)

        // Verify it was not added
        val tasks = TaskRepository.getAll()
        assertEquals(3, tasks.size)
        assertNull(TaskRepository.getById(999))
    }

    @Test
    fun testDelete() {
        TaskRepository.delete(3)

        val tasks = TaskRepository.getAll()
        assertEquals(2, tasks.size)
        assertNull(TaskRepository.getById(3))

        // Try to delete already deleted task
        TaskRepository.delete(3)
        assertEquals(2, TaskRepository.getAll().size) // Should remain unchanged
    }

    @Test
    fun testGetNewId() {
        assertEquals(4, TaskRepository.getNewId()) // Max ID is 3, so next should be 4

        // Add a task with non-sequential ID
        val newTask = Task(id = 10, content = "High ID Task", isDone = false)
        TaskRepository.add(newTask)

        assertEquals(11, TaskRepository.getNewId()) // Now max ID is 10, so next should be 11
    }
}
