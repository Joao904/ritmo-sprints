package br.unemat.ritmo.model

import kotlin.math.max

/**
 * Cronômetro da sessão, em regressiva. Guarda só números, para sobreviver a rotação
 * e a o app ir para segundo plano: com o cronômetro rodando, vale o horário de término (endAt);
 * parado ou pausado, vale o tempo que faltava (leftMs).
 */
data class StudyTimer(val totalMs: Long, val leftMs: Long = totalMs, val endAt: Long? = null) {
    val running: Boolean get() = endAt != null

    fun remaining(now: Long): Long = endAt?.let { max(0L, it - now) } ?: leftMs

    fun started(now: Long): Boolean = running || remaining(now) < totalMs

    fun finished(now: Long): Boolean = remaining(now) == 0L

    /** Começar ou continuar. */
    fun start(now: Long): StudyTimer = if (finished(now)) this else copy(endAt = now + leftMs)

    fun pause(now: Long): StudyTimer = copy(leftMs = remaining(now), endAt = null)

    fun reset(): StudyTimer = StudyTimer(totalMs)

    fun toList(): List<Long> = listOf(totalMs, leftMs, endAt ?: -1L)

    companion object {
        fun ofMinutes(minutes: Int) = StudyTimer(minutes * 60_000L)

        fun fromList(values: List<Long>) = StudyTimer(values[0], values[1], values[2].takeIf { it >= 0 })

        /** 25:00, 04:09; acima de uma hora, 1:30:00. */
        fun format(ms: Long): String {
            val seconds = (ms + 999) / 1000 // arredonda para cima: 0,4 s ainda mostra 00:01
            val h = seconds / 3600
            val m = seconds % 3600 / 60
            val s = seconds % 60
            return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
        }
    }
}
