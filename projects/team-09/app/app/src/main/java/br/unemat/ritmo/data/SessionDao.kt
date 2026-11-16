package br.unemat.ritmo.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Operações de acesso à tabela study_sessions. O Room gera a implementação. */
@Dao
interface SessionDao {
    @Insert
    suspend fun insert(session: StudySession): Long

    @Delete
    suspend fun delete(session: StudySession)

    /** Todas as sessões, mais recentes primeiro. O Flow emite de novo sempre que a tabela muda. */
    @Query("SELECT * FROM study_sessions ORDER BY created_at DESC, id DESC")
    fun observeAll(): Flow<List<StudySession>>

    @Query("SELECT COUNT(*) AS count, COALESCE(SUM(minutes), 0) AS totalMinutes FROM study_sessions")
    fun observeSummary(): Flow<HistorySummary>

    /** Minutos estudados a partir de um momento (usado na meta da semana). */
    @Query("SELECT COALESCE(SUM(minutes), 0) FROM study_sessions WHERE created_at >= :since")
    fun observeMinutesSince(since: Long): Flow<Int>
}
