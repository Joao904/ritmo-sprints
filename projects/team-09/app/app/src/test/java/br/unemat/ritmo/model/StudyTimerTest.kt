package br.unemat.ritmo.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Cronômetro da sessão: começar, pausar, continuar, terminar e recomeçar. */
class StudyTimerTest {
    private val min = 60_000L

    @Test fun startCountsDownFromTheEndTime() {
        val timer = StudyTimer.ofMinutes(25).start(now = 0)
        assertTrue(timer.running)
        assertEquals(25 * min, timer.remaining(0))
        assertEquals(15 * min, timer.remaining(10 * min))
    }

    @Test fun pauseKeepsWhatWasLeftAndResumeContinuesFromThere() {
        val paused = StudyTimer.ofMinutes(25).start(now = 0).pause(now = 10 * min)
        assertFalse(paused.running)
        assertEquals(15 * min, paused.remaining(99 * min)) // parado, o tempo não corre
        val resumed = paused.start(now = 100 * min)
        assertEquals(5 * min, resumed.remaining(110 * min))
    }

    @Test fun finishesAtZeroAndResetGoesBackToTheFullTime() {
        val timer = StudyTimer.ofMinutes(5).start(now = 0)
        assertTrue(timer.finished(6 * min))
        assertEquals(0L, timer.remaining(6 * min))
        val reset = timer.reset()
        assertFalse(reset.started(0))
        assertEquals(5 * min, reset.remaining(0))
    }

    @Test fun survivesSaveAndRestore() {
        val timer = StudyTimer.ofMinutes(30).start(now = 1_000)
        assertEquals(timer, StudyTimer.fromList(timer.toList()))
        val idle = StudyTimer.ofMinutes(30)
        assertEquals(idle, StudyTimer.fromList(idle.toList()))
    }

    @Test fun formatsMinutesAndHours() {
        assertEquals("25:00", StudyTimer.format(25 * min))
        assertEquals("04:09", StudyTimer.format(249_000))
        assertEquals("00:01", StudyTimer.format(400)) // arredonda para cima
        assertEquals("1:30:00", StudyTimer.format(90 * min))
    }
}
