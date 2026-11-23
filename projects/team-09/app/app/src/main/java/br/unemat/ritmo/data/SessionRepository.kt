package br.unemat.ritmo.data

import kotlinx.coroutines.flow.Flow

/**
 * Ponto único que a interface usa para salvar e ler o histórico.
 * A tela não conhece o Room: ela só chama as funções e observa os Flows.
 */
class SessionRepository(private val dao: SessionDao) {

    /** Todas as sessões; a tela mostra as 5 primeiras e o resto em "Ver todas". */
    val sessions: Flow<List<StudySession>> = dao.observeAll()
    val summary: Flow<HistorySummary> = dao.observeSummary()

    fun minutesSince(since: Long): Flow<Int> = dao.observeMinutesSince(since)

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

    suspend fun delete(session: StudySession) = dao.delete(session)

    /** Anotação do que foi estudado; texto vazio apaga a anotação. */
    suspend fun saveNotes(session: StudySession, text: String) =
        dao.updateNotes(session.id, text.trim().take(NOTES_MAX).ifEmpty { null })

    /** Desfazer a exclusão: grava de novo a mesma sessão, com o mesmo id e a mesma data. */
    suspend fun restore(session: StudySession) {
        dao.insert(session)
    }

    companion object {
        const val RECENT_LIMIT = 5
        const val NOTES_MAX = 300
    }
}
