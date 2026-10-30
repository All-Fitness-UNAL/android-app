package com.allfitness.app.core.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import javax.inject.Inject
import javax.inject.Singleton

/** Caché de solo lectura para uso sin conexión: una fila JSON por pantalla (research R4). */
enum class SnapshotKey { PROFILE, ROUTINE, WORKOUT_WEEK, DIET, WEIGHTS, SUMMARY }

@Entity(tableName = "snapshots")
data class SnapshotEntity(@PrimaryKey val id: String, val json: String, val savedAt: Long)

@Dao
interface SnapshotDao {
    @Query("SELECT * FROM snapshots WHERE id = :id")
    fun observe(id: String): Flow<SnapshotEntity?>

    @Query("SELECT * FROM snapshots WHERE id = :id")
    suspend fun get(id: String): SnapshotEntity?

    @Upsert
    suspend fun upsert(entity: SnapshotEntity)

    @Query("DELETE FROM snapshots WHERE id = :id")
    suspend fun delete(id: String)
}

@Database(entities = [SnapshotEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun snapshotDao(): SnapshotDao
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "allfitness.db").build()

    @Provides
    fun snapshotDao(db: AppDatabase): SnapshotDao = db.snapshotDao()
}

data class Cached<T>(val data: T, val savedAt: Long)

@Singleton
class SnapshotStore @Inject constructor(private val dao: SnapshotDao, private val json: Json) {

    fun <T> observe(key: SnapshotKey, serializer: KSerializer<T>): Flow<Cached<T>?> =
        dao.observe(key.name).map { entity ->
            entity?.let { runCatching { Cached(json.decodeFromString(serializer, it.json), it.savedAt) }.getOrNull() }
        }

    suspend fun <T> get(key: SnapshotKey, serializer: KSerializer<T>): T? =
        dao.get(key.name)?.let { runCatching { json.decodeFromString(serializer, it.json) }.getOrNull() }

    suspend fun <T> save(key: SnapshotKey, serializer: KSerializer<T>, value: T) =
        dao.upsert(SnapshotEntity(key.name, json.encodeToString(serializer, value), System.currentTimeMillis()))

    suspend fun delete(key: SnapshotKey) = dao.delete(key.name)

    inline fun <reified T> observe(key: SnapshotKey): Flow<Cached<T>?> = observe(key, serializer<T>())

    suspend inline fun <reified T> get(key: SnapshotKey): T? = get(key, serializer<T>())

    suspend inline fun <reified T> save(key: SnapshotKey, value: T) = save(key, serializer<T>(), value)
}

/** Convierte el DTO guardado al modelo de dominio; si el mapeo falla se trata como si no hubiera caché. */
inline fun <T, R> Flow<Cached<T>?>.mapData(crossinline transform: (T) -> R): Flow<Cached<R>?> = map { cached ->
    cached?.let { runCatching { Cached(transform(it.data), it.savedAt) }.getOrNull() }
}
