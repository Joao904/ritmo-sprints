package br.unemat.ritmo.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Uma sessão de estudo salva no histórico (tabela study_sessions). */
@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    /** Objetivo é opcional (SPEC-004); fica null quando não foi informado. */
    val goal: String?,
    val minutes: Int,
    /** Momento em que a sessão foi salva, em milissegundos desde 1970 (System.currentTimeMillis). */
    @ColumnInfo(name = "created_at") val createdAt: Long,
    /** Sprint 07: o que o estudante fez de fato, anotado depois de salvar (opcional). */
    val notes: String? = null,
)

/** Totais do histórico, calculados pelo banco. */
data class HistorySummary(val count: Int, val totalMinutes: Int)
