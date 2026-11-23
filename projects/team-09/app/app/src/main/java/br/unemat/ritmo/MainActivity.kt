package br.unemat.ritmo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import br.unemat.ritmo.data.RitmoDatabase
import br.unemat.ritmo.data.SessionRepository
import br.unemat.ritmo.timer.SessionClock
import br.unemat.ritmo.timer.TimerNotifications
import br.unemat.ritmo.ui.RitmoTheme
import br.unemat.ritmo.ui.RitmoApp

class MainActivity : ComponentActivity() {
    /** Aba pedida por uma notificação (tocar na contagem ou no alarme abre o Cronômetro). */
    private val requestedTab = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Sprint 07: carrega o cronômetro salvo e refaz o alarme e a notificação, se houver sessão rodando.
        SessionClock.init(this)
        if (savedInstanceState == null) requestedTab.value = intent.getStringExtra(TimerNotifications.EXTRA_TAB)
        // Sprint 05: the repository gives the UI access to the local Room database.
        val repository = SessionRepository(RitmoDatabase.get(this).sessionDao())
        setContent {
            RitmoTheme {
                RitmoApp(repository, requestedTab = requestedTab.value, onTabOpened = { requestedTab.value = null })
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.getStringExtra(TimerNotifications.EXTRA_TAB)?.let { requestedTab.value = it }
    }
}
