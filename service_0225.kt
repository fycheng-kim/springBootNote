package com.example.mongoapi.service

import com.example.mongoapi.exception.DocumentNotFoundException
import com.example.mongoapi.exception.DuplicateRowException
import com.example.mongoapi.model.MyDocument
import com.example.mongoapi.model.RowEntry
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service

@Service
class DocumentService(private val mongoTemplate: MongoTemplate) {

    fun addRowToDocument(name: String, entry: RowEntry): MyDocument {
        // Validation: rowName prefix check
        if (!entry.rowName.startsWith("$name/")) {
            throw IllegalArgumentException("rowName prefix must match document name '$name'")
        }

        // 1. Check if Document exists
        val query = Query(Criteria.where("name").`is`(name))
        val existingDoc = mongoTemplate.findOne(query, MyDocument::class.java) 
            ?: throw DocumentNotFoundException("Document with name '$name' not found")

        // 2. Check if rowName already exists in the rows array
        val rowExists = existingDoc.rows.any { it.rowName == entry.rowName }
        if (rowExists) {
            throw DuplicateRowException("Row with name '${entry.rowName}' already exists")
        }

        // 3. Perform atomic push and return the updated document
        val update = Update().push("rows", entry)
        
        return mongoTemplate.findAndModify(
            query,
            update,
            FindAndModifyOptions.options().returnNew(true), // Return the doc AFTER update
            MyDocument::class.java
        ) ?: throw DocumentNotFoundException("Document was deleted during processing")
    }
}