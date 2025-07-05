package com.example

import com.example.model.ApiResponse
import com.example.model.Task
import com.example.model.TaskRequest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.*

class SerializationTest {

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    @Test
    fun testTaskSerialization() {
        val task = Task(id = 1, content = "Test Task", isDone = true)
        val serialized = json.encodeToString(task)

        // Check that the serialized JSON contains expected fields
        assertTrue(serialized.contains("\"id\":"))
        assertTrue(serialized.contains("\"content\":"))
        assertTrue(serialized.contains("\"isDone\":"))

        // Deserialize and check that the values match
        val deserialized = json.decodeFromString<Task>(serialized)
        assertEquals(task.id, deserialized.id)
        assertEquals(task.content, deserialized.content)
        assertEquals(task.isDone, deserialized.isDone)
    }

    @Test
    fun testTaskRequestSerialization() {
        val taskRequest = TaskRequest(content = "Test Request", isDone = false)
        val serialized = json.encodeToString(taskRequest)

        // Check serialized fields
        assertTrue(serialized.contains("\"content\":"))
        assertTrue(serialized.contains("\"isDone\":"))

        // Deserialize and verify
        val deserialized = json.decodeFromString<TaskRequest>(serialized)
        assertEquals(taskRequest.content, deserialized.content)
        assertEquals(taskRequest.isDone, deserialized.isDone)
    }

    @Test
    fun testApiResponseSerialization() {
        val task = Task(id = 1, content = "Test Task", isDone = true)
        val apiResponse = ApiResponse(success = true, data = task, message = "Success message")

        val serialized = json.encodeToString(apiResponse)

        // Check serialized fields
        assertTrue(serialized.contains("\"success\":"))
        assertTrue(serialized.contains("\"data\":"))
        assertTrue(serialized.contains("\"message\":"))

        // Deserialize and verify
        val deserialized = json.decodeFromString<ApiResponse<Task>>(serialized)
        assertEquals(apiResponse.success, deserialized.success)
        assertEquals(apiResponse.message, deserialized.message)
        assertEquals(apiResponse.data?.id, deserialized.data?.id)
        assertEquals(apiResponse.data?.content, deserialized.data?.content)
        assertEquals(apiResponse.data?.isDone, deserialized.data?.isDone)
    }

    @Test
    fun testListApiResponseSerialization() {
        val tasks = listOf(
            Task(id = 1, content = "Task 1", isDone = true),
            Task(id = 2, content = "Task 2", isDone = false)
        )

        val apiResponse = ApiResponse(success = true, data = tasks, message = "List response")
        val serialized = json.encodeToString(apiResponse)

        // Deserialize and verify
        val deserialized = json.decodeFromString<ApiResponse<List<Task>>>(serialized)
        assertEquals(apiResponse.success, deserialized.success)
        assertEquals(apiResponse.message, deserialized.message)
        assertEquals(2, deserialized.data?.size)
        assertEquals("Task 1", deserialized.data?.get(0)?.content)
        assertEquals("Task 2", deserialized.data?.get(1)?.content)
    }

    @Test
    fun testTaskRequestDefaultValues() {
        // TaskRequest allows isDone to be omitted with a default of false
        val json = "{\"content\":\"Test Default Value\"}" 

        val deserialized = Json.decodeFromString<TaskRequest>(json)
        assertEquals("Test Default Value", deserialized.content)
        assertEquals(false, deserialized.isDone) // Default value should be false
    }
}
