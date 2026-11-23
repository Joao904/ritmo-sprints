package br.unemat.ritmo

import android.database.sqlite.SQLiteDatabase
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import br.unemat.ritmo.data.RitmoDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/** Sprint 07: anotar o que foi estudado depois de salvar a sessão. */
class NotesTest {
    @get:Rule(order = 0) val clear = ClearDatabaseRule()
    @get:Rule(order = 1) val compose = createAndroidComposeRule<MainActivity>()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    private fun saveSession(subject: String) {
        compose.planSession("30", subject)
        compose.tap("Salvar no histórico")
        compose.waitForText("1 sessão salva", substring = true)
        // A mensagem "Sessão salva no histórico." cobre o fim da tela por alguns segundos.
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Sessão salva no histórico.").fetchSemanticsNodes().isEmpty() }
    }

    @Test fun tappingASessionShowsDetailsAndNotesCanBeWrittenAndEdited() {
        saveSession("Bioquímica")
        compose.onNodeWithText("Bioquímica").performScrollTo().performClick()
        compose.onNodeWithText("Nada anotado ainda.").assertIsDisplayed()
        compose.onNodeWithText("Anotar").performClick()
        compose.onNodeWithText("O que você estudou?").assertIsDisplayed()
        compose.onNodeWithText("Anotação").performTextInput("revisei enzimas e fiz o resumo")
        compose.onNodeWithText("Salvar anotação").performClick()
        // Volta para os detalhes, já com a anotação.
        compose.waitForText("revisei enzimas e fiz o resumo")
        compose.onNodeWithText("Fechar").performClick()
        // Na lista, só a marca: o texto da anotação não ocupa espaço.
        compose.onNodeWithText("Com anotação").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("revisei enzimas e fiz o resumo").assertDoesNotExist()

        // Editar: a caixa abre com o texto atual.
        compose.onNodeWithText("Bioquímica").performScrollTo().performClick()
        compose.onNodeWithText("Editar anotação").performClick()
        compose.onNodeWithText("Anotação").performTextReplacement("revisei enzimas e fiz 10 questões")
        compose.onNodeWithText("Salvar anotação").performClick()
        compose.waitForText("revisei enzimas e fiz 10 questões")

        // A anotação fica no banco: continua depois de recriar a tela.
        compose.onNodeWithText("Fechar").performClick()
        compose.activityRule.scenario.recreate()
        compose.waitForText("Com anotação")
        compose.onNodeWithText("Bioquímica").performScrollTo().performClick()
        compose.waitForText("revisei enzimas e fiz 10 questões")
    }

    @Test fun cancelKeepsTheSessionWithoutNotes() {
        saveSession("Física II")
        compose.onNodeWithText("Física II").performScrollTo().performClick()
        compose.onNodeWithText("Anotar").performClick()
        compose.onNodeWithText("Anotação").performTextInput("não deve ser salvo")
        compose.onNodeWithText("Cancelar").performClick()
        compose.onNodeWithText("Nada anotado ainda.").assertIsDisplayed()
        compose.onNodeWithText("Fechar").performClick()
        compose.onNodeWithText("Com anotação").assertDoesNotExist()
    }

    /** Um banco criado pela versão anterior do app (sem a coluna notes) é atualizado sem perder as sessões. */
    @Test fun databaseFromTheSprint05VersionIsMigrated() {
        val name = "migracao-teste.db"
        context.deleteDatabase(name)
        val file = context.getDatabasePath(name).apply { parentFile?.mkdirs() }
        SQLiteDatabase.openOrCreateDatabase(file, null).use { db ->
            // Mesmo esquema que o Room criava na versão 1 (Sprints 05 e 06).
            db.execSQL("CREATE TABLE IF NOT EXISTS `study_sessions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `subject` TEXT NOT NULL, `goal` TEXT, `minutes` INTEGER NOT NULL, `created_at` INTEGER NOT NULL)")
            db.execSQL("INSERT INTO study_sessions (subject, goal, minutes, created_at) VALUES ('Cálculo I', 'limites', 45, 1000)")
            db.version = 1
        }
        val migrated = Room.databaseBuilder(context, RitmoDatabase::class.java, name)
            .addMigrations(RitmoDatabase.MIGRATION_1_2)
            .build()
        try {
            val sessions = runBlocking { migrated.sessionDao().observeAll().first() }
            assertEquals(1, sessions.size)
            assertEquals("Cálculo I", sessions[0].subject)
            assertEquals(45, sessions[0].minutes)
            assertNull(sessions[0].notes)
            runBlocking { migrated.sessionDao().updateNotes(sessions[0].id, "fiz a lista 2") }
            assertEquals("fiz a lista 2", runBlocking { migrated.sessionDao().observeAll().first() }[0].notes)
        } finally {
            migrated.close()
            context.deleteDatabase(name)
        }
    }
}
