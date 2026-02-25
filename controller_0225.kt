package com.example.mongoapi.controller

import com.example.mongoapi.model.RowEntry
import com.example.mongoapi.service.DocumentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/rows")
class DocumentController(private val documentService: DocumentService) {

    @PostMapping("/{name}")
    fun addRow(
        @PathVariable name: String,
        @RequestBody entry: RowEntry
    ): ResponseEntity<Any> {
        // The service will throw DocumentNotFoundException or DuplicateRowException 
        // which Spring maps to 404 and 409 respectively.
        val updatedDoc = documentService.addRowToDocument(name, entry)
        return ResponseEntity.ok(updatedDoc)
    }
}