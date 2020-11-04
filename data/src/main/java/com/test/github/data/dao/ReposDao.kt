package com.test.github.data.dao

import androidx.room.*
import com.test.github.data.entity.HistoryEntity
import com.test.github.data.entity.RepoEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ReposDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertRepos(repos: List<RepoEntity>): List<Long>

    @Query("select * from repos")
    abstract fun getRepos(): Flow<List<RepoEntity>>

    @Query("delete from repos")
    abstract suspend fun clearRepos(): Int



    @Query("""
        delete from history 
        where viewedTimestamp = (select min(viewedTimestamp) from history)
        and (select count(serverId) from history) > 20
        """)
    abstract suspend fun deleteOldestViewed(): Int

    @Query("select * from history order by viewedTimestamp desc")
    abstract fun getHistory(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun createOrUpdateHistoryRepo(item: HistoryEntity)

    @Transaction
    open suspend fun updateHistory(item: HistoryEntity) {
        createOrUpdateHistoryRepo(item)
        deleteOldestViewed()
    }
}