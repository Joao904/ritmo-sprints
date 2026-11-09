package br.unemat.ritmo.data

import kotlinx.coroutines.flow.Flow

/**
 * Ponto único que a interface usa para salvar e ler o histórico.
 * A tela não conhece o Room: ela só chama save() e observa os Flows.
 */
class SessionRepository(private val dao: SessionDao) {

    val recentSessions: Flow<List<StudySession>> = dao.observeRecent(RECENT_LIMIT)
    val summary: Flow<HistorySummary> = dao.observeSummary()

    /** Recebe valores já validados (SPEC-004); objetivo vazio é gravado como null. */
    suspend fun save(subject: String, goal: String, minutes: Int) {
        dao.insert(
            StudySession(
                subject = subject.trim(),
                goal = goal.trim().ifEmpty { null },
                minutes = minutes,
                createdAt = System.currentTimeMillis(),
            )
        )
    }

    companion object {
        const val RECENT_LIMIT = 5
    }
}
