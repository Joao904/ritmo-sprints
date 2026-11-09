package br.unemat.ritmo

import android.graphics.Bitmap
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assume.assumeTrue
import java.io.File

/**
 * Apoio para gerar as capturas de evidência.
 * As classes *Evidence só executam com o argumento `-e evidence true`
 * (passado pelo script de validação); no connectedDebugAndroidTest normal elas são ignoradas.
 */
object Evidence {
    fun requireEvidenceRun() {
        val args = InstrumentationRegistry.getArguments()
        assumeTrue("Execução de evidência não solicitada", args.getString("evidence") == "true")
    }

    /** Captura a tela inteira do emulador e salva em files/evidence/<nome>.png do app. */
    fun screenshot(name: String) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.waitForIdleSync()
        Thread.sleep(600) // deixa terminar animações de foco e de navegação
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        val dir = File(instrumentation.targetContext.filesDir, "evidence").apply { mkdirs() }
        File(dir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
