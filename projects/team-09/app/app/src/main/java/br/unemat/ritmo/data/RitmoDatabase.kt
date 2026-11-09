package br.unemat.ritmo.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Banco local do app (arquivo ritmo.db, na área privada do app).
 * exportSchema = false: ainda não há migrações; a versão 1 é o primeiro esquema.
 */
@Database(entities = [StudySession::class], version = 1, exportSchema = false)
abstract class RitmoDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile private var instance: RitmoDatabase? = null

        /** Uma única instância por processo, criada na primeira chamada. */
        fun get(context: Context): RitmoDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context.applicationContext, RitmoDatabase::class.java, "ritmo.db")
                    .build()
                    .also { instance = it }
            }
    }
}
