package com.example

import com.example.model.ApiResponse
import com.example.model.Task
import com.example.model.TaskRequest
import com.example.repository.TaskRepository
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.*
import kotlin.test.*

class ApplicationTest {

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
    fun testGetAllTasks() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        val response = client.get("/tasks")
        assertEquals(HttpStatusCode.OK, response.status)

        val apiResponse = response.body<ApiResponse<List<Task>>>()
        assertTrue(apiResponse.success)
        assertEquals(3, apiResponse.data?.size)
        assertEquals("Successfully retrieved 3 tasks", apiResponse.message)
    }

    @Test
    fun testGetTaskById() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        // Test valid task ID
        val response = client.get("/tasks/1")
        assertEquals(HttpStatusCode.OK, response.status)

        val apiResponse = response.body<ApiResponse<Task>>()
        assertTrue(apiResponse.success)
        assertEquals(1, apiResponse.data?.id)
        assertEquals("Learn Ktor", apiResponse.data?.content)
        assertTrue(apiResponse.data?.isDone ?: false)

        // Test invalid task ID
        val notFoundResponse = client.get("/tasks/999")
        assertEquals(HttpStatusCode.NotFound, notFoundResponse.status)

        val notFoundApiResponse = notFoundResponse.body<ApiResponse<Task>>()
        assertFalse(notFoundApiResponse.success)
        assertEquals("Task not found", notFoundApiResponse.message)
    }

    @Test
    fun testCreateTask() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        // Create a new task
        val response = client.post("/tasks") {
            contentType(ContentType.Application.Json)
            setBody(TaskRequest(content = "New Test Task", isDone = false))
        }

        assertEquals(HttpStatusCode.Created, response.status)

        val apiResponse = response.body<ApiResponse<Task>>()
        assertTrue(apiResponse.success)
        assertEquals("New Test Task", apiResponse.data?.content)
        assertFalse(apiResponse.data?.isDone ?: true)
        assertEquals("Task created successfully", apiResponse.message)

        // Verify the task was added
        val getAllResponse = client.get("/tasks")
        val getAllApiResponse = getAllResponse.body<ApiResponse<List<Task>>>()
        assertEquals(4, getAllApiResponse.data?.size) // Now we should have 4 tasks
    }

    @Test
    fun testUpdateTask() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        // Update an existing task
        val response = client.put("/tasks/2") {
            contentType(ContentType.Application.Json)
            setBody(TaskRequest(content = "Updated Task", isDone = true))
        }

        assertEquals(HttpStatusCode.OK, response.status)

        val apiResponse = response.body<ApiResponse<Task>>()
        assertTrue(apiResponse.success)
        assertEquals("Updated Task", apiResponse.data?.content)
        assertTrue(apiResponse.data?.isDone ?: false)

        // Verify the task was updated
        val getTaskResponse = client.get("/tasks/2")
        val getTaskApiResponse = getTaskResponse.body<ApiResponse<Task>>()
        assertEquals("Updated Task", getTaskApiResponse.data?.content)
        assertTrue(getTaskApiResponse.data?.isDone ?: false)
    }

    @Test
    fun testDeleteTask() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        // Delete a task
        val response = client.delete("/tasks/3")
        assertEquals(HttpStatusCode.OK, response.status)

        val apiResponse = response.body<ApiResponse<Unit>>()
        assertTrue(apiResponse.success)
        assertEquals("Task deleted successfully", apiResponse.message)

        // Verify the task was deleted
        val getAllResponse = client.get("/tasks")
        val getAllApiResponse = getAllResponse.body<ApiResponse<List<Task>>>()
        assertEquals(2, getAllApiResponse.data?.size) // Now we should have 2 tasks

        // Try to get the deleted task
        val getDeletedResponse = client.get("/tasks/3")
        assertEquals(HttpStatusCode.NotFound, getDeletedResponse.status)
    }

    @Test
    fun testInvalidRequests() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        // Test invalid ID format
        val invalidIdResponse = client.get("/tasks/abc")
        assertEquals(HttpStatusCode.BadRequest, invalidIdResponse.status)

        val invalidIdApiResponse = invalidIdResponse.body<ApiResponse<Task>>()
        assertFalse(invalidIdApiResponse.success)
        assertEquals("Invalid ID format", invalidIdApiResponse.message)

        // Test updating non-existent task
        val updateNonExistentResponse = client.put("/tasks/999") {
            contentType(ContentType.Application.Json)
            setBody(TaskRequest(content = "Non-existent Task", isDone = false))
        }

        assertEquals(HttpStatusCode.NotFound, updateNonExistentResponse.status)

        // Test deleting non-existent task
        val deleteNonExistentResponse = client.delete("/tasks/999")
        assertEquals(HttpStatusCode.NotFound, deleteNonExistentResponse.status)
    }
}
