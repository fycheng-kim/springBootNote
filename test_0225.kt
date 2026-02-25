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
class DocumentUpdateIntegrationTest {

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
        // Setup initial document with one row
        val initialRows = listOf(
            RowEntry("a/row1", "original content"),
            RowEntry("a/row2", "do not touch")
        )
        mongoTemplate.save(MyDocument(name = "a", rows = initialRows))
    }

    @Test
    fun `PUT should update existing row content while preserving other rows`() {
        // Data to update
        val updateRequest = RowEntry("a/row1", "updated content")

        // Using TestRestTemplate.put (Note: put returns void, use exchange if you need the body)
        restTemplate.put("/api/documents/a/rows", updateRequest)

        // Verify in Database
        val updatedDoc = mongoTemplate.findOne(
            org.springframework.data.mongodb.core.query.Query(
                org.springframework.data.mongodb.core.query.Criteria.where("name").`is`("a")
            ),
            MyDocument::class.java
        )

        val row1 = updatedDoc?.rows?.find { it.rowName == "a/row1" }
        val row2 = updatedDoc?.rows?.find { it.rowName == "a/row2" }

        assertThat(row1?.otherFields).isEqualTo("updated content")
        assertThat(row2?.otherFields).isEqualTo("do not touch") // Assert isolation
    }

    @Test
    fun `PUT should return 404 when rowName does not exist`() {
        val nonExistentUpdate = RowEntry("a/ghost-row", "some data")
        
        // Use exchange to capture the 404 response
        val response = restTemplate.exchange(
            "/api/documents/a/rows",
            org.springframework.http.HttpMethod.PUT,
            org.springframework.http.HttpEntity(nonExistentUpdate),
            Map::class.java
        )

        assertThat(response.statusCode.value()).isEqualTo(404)
    }
}