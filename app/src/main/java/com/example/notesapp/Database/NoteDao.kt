package com.example.notesapp.Database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.notesapp.Models.Note
import java.util.Date


@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("Select * from notes_table order by pin DESC, date DESC")
    fun getAllNotes() : LiveData<List<Note>>

    @Query("UPDATE notes_table Set title= :title, note = :note, date = :date ,pin =:pin WHERE id = :id")
    suspend fun update(id : Int?, title: String?, note: String?, date: String?, pin: Boolean): Int
}