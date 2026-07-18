package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val documentDao: DocumentDao,
    private val aiNoteDao: AINoteDao
) {
    val allDocuments: Flow<List<Document>> = documentDao.getAllDocuments()
    val allNotes: Flow<List<AINote>> = aiNoteDao.getAllNotes()

    suspend fun insertDocument(document: Document) = documentDao.insertDocument(document)
    suspend fun deleteDocumentById(id: Int) = documentDao.deleteDocumentById(id)

    suspend fun insertNote(note: AINote) = aiNoteDao.insertNote(note)
    suspend fun deleteNoteById(id: Int) = aiNoteDao.deleteNoteById(id)
}
