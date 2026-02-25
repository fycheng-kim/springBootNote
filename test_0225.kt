package com.example.mongoapi

import com.example.mongoapi.model.MyDocument
import com.example.mongoapi.model.RowEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.data.mongodb.core.MongoTemplate
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class DocumentApiLogicTest {

    companion object {
        @Container
        @ServiceConnection
        val mongodb = MongoDBContainer("mongo:7.0")
    }

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var mongoTemplate: MongoTemplate

    @BeforeEach
    fun setup() {
        mongoTemplate.dropCollection(MyDocument::class.java)
        // Seed initial data
        mongoTemplate.save(MyDocument(name = "a", rows = listOf(RowEntry("a/init", "first"))))
    }

    @Test
    fun `logic 1 - should return 404 when document name does not exist`() {
        val entry = RowEntry("nonexistent/row", "xxx")
        val response = restTemplate.postForEntity("/api/rows/nonexistent", entry, Map::class.java)

        assertThat(response.statusCode.value()).isEqualTo(404)
    }

    @Test
    fun `logic 2 - should return 409 when rowName already exists`() {
        // "a/init" already exists from @BeforeEach
        val duplicateEntry = RowEntry("a/init", "new content")
        val response = restTemplate.postForEntity("/api/rows/a", duplicateEntry, Map::class.java)

        assertThat(response.statusCode.value()).isEqualTo(409)
    }

    @Test
    fun `should succeed when document exists and row is unique`() {
        val validEntry = RowEntry("a/new-row", "unique content")
        val response = restTemplate.postForEntity("/api/rows/a", validEntry, MyDocument::class.java)

        assertThat(response.statusCode.is2xxSuccessful).isTrue()
        assertThat(response.body?.rows).hasSize(2)
        assertThat(response.body?.rows?.any { it.rowName == "a/new-row" }).isTrue()
    }
}