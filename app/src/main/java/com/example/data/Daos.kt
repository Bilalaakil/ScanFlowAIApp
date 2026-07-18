package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY timestamp DESC")
    fun getAllDocuments(): Flow<List<Document>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: Document)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocumentById(id: Int)
}

@Dao
interface AINoteDao {
    @Query("SELECT * FROM ai_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<AINote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: AINote)

    @Query("DELETE FROM ai_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Int)
}
