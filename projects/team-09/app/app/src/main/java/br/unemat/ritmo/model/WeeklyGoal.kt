package br.unemat.ritmo.model

import java.util.Calendar
import java.util.TimeZone

/** Meta de estudo da semana (segunda a domingo), em minutos. */
object WeeklyGoal {
    const val DEFAULT = 300 // 5 horas
    const val STEP = 30
    const val MIN = 30
    const val MAX = 40 * 60

    fun increase(goal: Int) = (goal + STEP).coerceAtMost(MAX)
    fun decrease(goal: Int) = (goal - STEP).coerceAtLeast(MIN)

    /** Segunda-feira às 00:00 da semana de [now], no fuso do aparelho. */
    fun startOfWeek(now: Long, zone: TimeZone = TimeZone.getDefault()): Long =
        Calendar.getInstance(zone).apply {
            firstDayOfWeek = Calendar.MONDAY
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            if (timeInMillis > now) add(Calendar.WEEK_OF_YEAR, -1)
        }.timeInMillis

    /** 45 min, 2 h, 2 h 30 min. */
    fun format(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return when {
            h == 0 -> "$m min"
            m == 0 -> "$h h"
            else -> "$h h $m min"
        }
    }

    fun progress(done: Int, goal: Int): Float = if (goal <= 0) 0f else (done.toFloat() / goal).coerceIn(0f, 1f)
}
