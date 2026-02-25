package com.example.mongoapi.controller

import com.example.mongoapi.model.MyDocument
import com.example.mongoapi.model.RowEntry
import com.example.mongoapi.service.DocumentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/documents") // Refactored to follow resource hierarchy
class DocumentController(private val documentService: DocumentService) {

    @PostMapping("/{name}/rows")
    fun addRow(
        @PathVariable name: String,
        @RequestBody entry: RowEntry
    ): ResponseEntity<MyDocument> {
        val updatedDoc = documentService.addRowToDocument(name, entry)
        return ResponseEntity.ok(updatedDoc)
    }

    @PutMapping("/{name}/rows")
    fun updateRow(
        @PathVariable name: String,
        @RequestBody entry: RowEntry
    ): ResponseEntity<MyDocument> {
        val updatedDoc = documentService.updateRowInDocument(name, entry)
        return ResponseEntity.ok(updatedDoc)
    }
}