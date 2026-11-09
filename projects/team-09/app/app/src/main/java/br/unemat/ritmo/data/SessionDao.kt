package br.unemat.ritmo.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Operações de acesso à tabela study_sessions. O Room gera a implementação. */
@Dao
interface SessionDao {
    @Insert
    suspend fun insert(session: StudySession): Long

    /** Sessões mais recentes primeiro. O Flow emite de novo sempre que a tabela muda. */
    @Query("SELECT * FROM study_sessions ORDER BY created_at DESC, id DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<StudySession>>

    @Query("SELECT COUNT(*) AS count, COALESCE(SUM(minutes), 0) AS totalMinutes FROM study_sessions")
    fun observeSummary(): Flow<HistorySummary>
}
