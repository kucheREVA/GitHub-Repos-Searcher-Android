package com.test.github.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.test.github.data.dao.ReposDao
import com.test.github.data.entity.HistoryEntity
import com.test.github.data.entity.RepoEntity

@Database(entities = [RepoEntity::class, HistoryEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getReposDao(): ReposDao
}