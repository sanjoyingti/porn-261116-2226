package com.aibuilt.porn.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogDao {
    @Query("SELECT * FROM catalog ORDER BY date DESC")
    fun observeAll(): Flow<List<VideoEntity>>

    @Query("SELECT COUNT(*) FROM catalog")
    suspend fun count(): Int

    @Query("SELECT * FROM catalog WHERE id = :id LIMIT 1")
    suspend fun byId(id: String): VideoEntity?

    @Query("SELECT * FROM catalog WHERE category = :category AND id != :id ORDER BY views DESC LIMIT 12")
    suspend fun related(id: String, category: String): List<VideoEntity>

    @Query("SELECT category, COUNT(*) AS total FROM catalog GROUP BY category")
    fun observeCounts(): Flow<List<CategoryCount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<VideoEntity>)

    @Query("DELETE FROM catalog")
    suspend fun clear()
}

data class CategoryCount(val category: String, val total: Int)

@Dao
interface TorDao {
    @Query("SELECT * FROM tor_links ORDER BY groupName ASC, name ASC")
    fun observeAll(): Flow<List<TorEntity>>

    @Query("SELECT COUNT(*) FROM tor_links")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TorEntity>)

    @Query("DELETE FROM tor_links")
    suspend fun clear()
}

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(item: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE id = :id")
    suspend fun remove(id: String)

    @Query("SELECT id FROM favorites")
    fun observeIds(): Flow<List<String>>

    @Query("SELECT catalog.* FROM catalog INNER JOIN favorites ON catalog.id = favorites.id ORDER BY favorites.addedAt DESC")
    fun observeFavorites(): Flow<List<VideoEntity>>
}

@Dao
interface HiddenDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(item: HiddenEntity)

    @Query("DELETE FROM hidden WHERE id = :id")
    suspend fun remove(id: String)

    @Query("DELETE FROM hidden")
    suspend fun clear()

    @Query("SELECT id FROM hidden")
    fun observeIds(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM hidden")
    fun observeCount(): Flow<Int>
}

@Dao
interface RecentSearchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(item: RecentSearchEntity)

    @Query("SELECT * FROM recent_searches ORDER BY searchedAt DESC LIMIT 10")
    fun observeAll(): Flow<List<RecentSearchEntity>>

    @Query("DELETE FROM recent_searches")
    suspend fun clear()
}

@Dao
interface DeadLinkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(item: DeadLinkEntity)

    @Query("SELECT * FROM dead_links")
    fun observeAll(): Flow<List<DeadLinkEntity>>
}
