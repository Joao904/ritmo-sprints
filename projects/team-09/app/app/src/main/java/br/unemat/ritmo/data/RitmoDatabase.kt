package br.unemat.ritmo.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Banco local do app (arquivo ritmo.db, na área privada do app).
 * Versão 1 (Sprint 05): primeiro esquema. Versão 2 (Sprint 07): coluna notes, com a anotação do que foi estudado.
 * A migração 1 → 2 mantém as sessões que já estavam salvas no aparelho.
 */
@Database(entities = [StudySession::class], version = 2, exportSchema = false)
abstract class RitmoDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao

    companion object {
        /** Acrescenta a coluna da anotação; as sessões antigas ficam sem anotação (null). */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE study_sessions ADD COLUMN notes TEXT")
            }
        }

        @Volatile private var instance: RitmoDatabase? = null

        /** Uma única instância por processo, criada na primeira chamada. */
        fun get(context: Context): RitmoDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context.applicationContext, RitmoDatabase::class.java, "ritmo.db")
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
    }
}
