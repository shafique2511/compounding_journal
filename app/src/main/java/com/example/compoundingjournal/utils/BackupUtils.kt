package com.example.compoundingjournal.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object BackupUtils {

    private const val DB_NAME = "compounding_journal_db"

    fun backupDatabase(context: Context, destinationUri: Uri): Boolean {
        return try {
            val dbFile = context.getDatabasePath(DB_NAME)
            context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                FileInputStream(dbFile).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun restoreDatabase(context: Context, sourceUri: Uri): Boolean {
        return try {
            val dbFile = context.getDatabasePath(DB_NAME)
            // It's recommended to close the database before restoring, 
            // which should be handled by the caller/ViewModel.
            
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                FileOutputStream(dbFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            // Delete WAL/SHM files to ensure the restored DB is used cleanly
            val shmFile = File(dbFile.path + "-shm")
            val walFile = File(dbFile.path + "-wal")
            if (shmFile.exists()) shmFile.delete()
            if (walFile.exists()) walFile.delete()
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
