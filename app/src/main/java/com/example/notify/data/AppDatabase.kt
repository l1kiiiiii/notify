// AppDatabase.kt
package com.example.notify.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Task::class], version = 3) // Version is now 3
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Your migration logic from version 1 to 2
                // Example: db.execSQL("ALTER TABLE tasks ADD COLUMN some_new_field_in_v2 TEXT")
                // If version 2 was the first version where 'isCompleted' was added,
                // and 'priority' (as Int perhaps) was there.
                // This is just a placeholder, replace with your actual V1 to V2 migration.
                // If V2 was just adding the 'isCompleted' column:
                // db.execSQL("ALTER TABLE tasks ADD COLUMN isCompleted INTEGER NOT NULL DEFAULT 0")
            }
        }

        // Migration from version 2 to version 3
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Add the new priority column as TEXT for the Enum.
                //    If you had an INTEGER priority before, this changes its type.
                //    If 'priority' didn't exist in v2, this adds it.
                //    If 'priority' was already TEXT from an earlier Enum attempt, this might just ensure default.
                db.execSQL("ALTER TABLE tasks ADD COLUMN priority TEXT NOT NULL DEFAULT '${Priority.MEDIUM.name}'")

                // 2. Add the new status column
                db.execSQL("ALTER TABLE tasks ADD COLUMN status TEXT NOT NULL DEFAULT '${TaskStatus.PENDING.name}'")

                // 3. Migrate 'isCompleted' (INTEGER) to 'status' (TEXT)
                //    This assumes 'isCompleted' column existed in version 2.
                db.execSQL("UPDATE tasks SET status = '${TaskStatus.COMPLETED.name}' WHERE isCompleted = 1")

                // 4. IMPORTANT: If you are changing the type of an existing 'priority' column
                //    (e.g., from INTEGER to TEXT) or removing 'isCompleted',
                //    you need a more complex migration strategy (create new table, copy data, drop old, rename new).
                //    The above 'ADD COLUMN' for priority might fail if a 'priority' column already exists with a different type.
                //
                //    If 'priority' was an INTEGER in v2 and needs to become TEXT for the Enum in v3:
                //    And if 'isCompleted' is to be removed:
                //
                //    db.execSQL("CREATE TABLE tasks_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, description TEXT, category TEXT NOT NULL, scheduledTimeMillis INTEGER NOT NULL, priority TEXT NOT NULL DEFAULT '${Priority.MEDIUM.name}', status TEXT NOT NULL DEFAULT '${TaskStatus.PENDING.name}')")
                //    db.execSQL("INSERT INTO tasks_new (id, title, description, category, scheduledTimeMillis, priority, status) SELECT id, title, description, category, scheduledTimeMillis, CASE priority WHEN 1 THEN '${Priority.URGENT.name}' WHEN 2 THEN '${Priority.HIGH.name}' ELSE '${Priority.MEDIUM.name}' END, CASE WHEN isCompleted = 1 THEN '${TaskStatus.COMPLETED.name}' ELSE '${TaskStatus.PENDING.name}' END FROM tasks")
                //    db.execSQL("DROP TABLE tasks")
                //    db.execSQL("ALTER TABLE tasks_new RENAME TO tasks")
                //
                //    If you only added 'isCompleted' in V2 and now adding 'priority' (TEXT) and 'status' (TEXT) in V3,
                //    and 'isCompleted' is effectively being replaced by 'status', the simpler ADD COLUMNs above and the UPDATE are fine.
                //    You could choose to drop 'isCompleted' later or in a subsequent migration if it's truly redundant.
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "task_database"
                )
                    // Add ALL necessary migrations up to the current version
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    // .fallbackToDestructiveMigration() // Use only during development if you don't care about data loss on schema change
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
