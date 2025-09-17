// AppDatabase.kt
package com.example.notify.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Task::class], version = 4, exportSchema = false) // Added exportSchema = false
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Your migration logic from version 1 to 2
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN priority TEXT NOT NULL DEFAULT '${Priority.MEDIUM.name}'")
                db.execSQL("ALTER TABLE tasks ADD COLUMN status TEXT NOT NULL DEFAULT '${TaskStatus.ACTIVE.name}'")
                db.execSQL("UPDATE tasks SET status = '${TaskStatus.COMPLETED.name}' WHERE isCompleted = 1")
                // Potentially drop 'isCompleted' if it's fully replaced and no longer needed:
                // db.execSQL("CREATE TABLE tasks_temp (...) ...") 
                // db.execSQL("INSERT INTO tasks_temp (...) SELECT ... FROM tasks")
                // db.execSQL("DROP TABLE tasks")
                // db.execSQL("ALTER TABLE tasks_temp RENAME TO tasks")
            }
        }

        // Migration from version 3 to version 4
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // scheduledTimeMillis changed from NOT NULL to NULLABLE
                // details column constraint was missing (should be NOT NULL based on Task entity)
                // 1. Create a new table with the correct schema for version 4
                db.execSQL("""
                    CREATE TABLE tasks_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        details TEXT NOT NULL,  -- Corrected: details must be NOT NULL
                        scheduledTimeMillis INTEGER,  -- Changed to nullable
                        category TEXT NOT NULL,
                        priority TEXT NOT NULL,
                        status TEXT NOT NULL
                    )
                """.trimIndent())

                // 2. Copy data from the old 'tasks' table to 'tasks_new'
                db.execSQL("""
                    INSERT INTO tasks_new (id, title, details, scheduledTimeMillis, category, priority, status)
                    SELECT id, title, details, scheduledTimeMillis, category, priority, status FROM tasks
                """.trimIndent())

                // 3. Drop the old 'tasks' table
                db.execSQL("DROP TABLE tasks")

                // 4. Rename 'tasks_new' to 'tasks'
                db.execSQL("ALTER TABLE tasks_new RENAME TO tasks")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "task_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4) // Added MIGRATION_3_4
                    .fallbackToDestructiveMigrationOnDowngrade() // CORRECTED: Allows destructive downgrade
                    // .fallbackToDestructiveMigration() // Keep commented out if using manual migrations
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
