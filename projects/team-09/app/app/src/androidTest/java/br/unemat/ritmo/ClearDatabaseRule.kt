package br.unemat.ritmo

import androidx.test.platform.app.InstrumentationRegistry
import br.unemat.ritmo.data.RitmoDatabase
import org.junit.rules.ExternalResource

/** Esvazia o banco antes e depois do teste, para um teste não depender do outro. */
class ClearDatabaseRule : ExternalResource() {
    override fun before() = clear()
    override fun after() = clear()

    companion object {
        fun clear() {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            RitmoDatabase.get(context).clearAllTables()
        }
    }
}
