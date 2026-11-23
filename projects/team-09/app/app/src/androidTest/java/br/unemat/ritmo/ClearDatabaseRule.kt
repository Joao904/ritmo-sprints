package br.unemat.ritmo

import androidx.test.platform.app.InstrumentationRegistry
import br.unemat.ritmo.data.GoalStore
import br.unemat.ritmo.data.RitmoDatabase
import br.unemat.ritmo.timer.SessionClock
import org.junit.rules.ExternalResource

/** Esvazia o banco, descarta o cronômetro salvo (Sprint 07) e volta a meta da semana ao padrão, antes e depois do teste, para um teste não depender do outro. */
class ClearDatabaseRule : ExternalResource() {
    override fun before() = clear()
    override fun after() = clear()

    companion object {
        fun clear() {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            RitmoDatabase.get(context).clearAllTables()
            GoalStore(context).clear()
            SessionClock.clear(context)
        }
    }
}
