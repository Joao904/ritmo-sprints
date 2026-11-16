package br.unemat.ritmo.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

/** Meta da semana: início da semana, limites da meta e textos. */
class WeeklyGoalTest {
    private val zone = TimeZone.getTimeZone("America/Cuiaba")

    private fun at(year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0): Long =
        Calendar.getInstance(zone).apply { clear(); set(year, month - 1, day, hour, minute) }.timeInMillis

    @Test fun weekStartsOnMondayAtMidnight() {
        val monday = at(2026, 11, 16)
        assertEquals(monday, WeeklyGoal.startOfWeek(at(2026, 11, 16, 0, 0), zone))
        assertEquals(monday, WeeklyGoal.startOfWeek(at(2026, 11, 18, 14, 30), zone)) // quarta
        assertEquals(monday, WeeklyGoal.startOfWeek(at(2026, 11, 22, 23, 59), zone)) // domingo
        assertEquals(at(2026, 11, 9), WeeklyGoal.startOfWeek(at(2026, 11, 15, 23, 59), zone)) // domingo anterior
    }

    @Test fun goalChangesBy30MinutesWithinLimits() {
        assertEquals(330, WeeklyGoal.increase(300))
        assertEquals(270, WeeklyGoal.decrease(300))
        assertEquals(WeeklyGoal.MIN, WeeklyGoal.decrease(WeeklyGoal.MIN))
        assertEquals(WeeklyGoal.MAX, WeeklyGoal.increase(WeeklyGoal.MAX))
    }

    @Test fun formatsAndProgress() {
        assertEquals("45 min", WeeklyGoal.format(45))
        assertEquals("5 h", WeeklyGoal.format(300))
        assertEquals("2 h 30 min", WeeklyGoal.format(150))
        assertEquals(0.5f, WeeklyGoal.progress(150, 300))
        assertEquals(1f, WeeklyGoal.progress(400, 300))
    }
}
