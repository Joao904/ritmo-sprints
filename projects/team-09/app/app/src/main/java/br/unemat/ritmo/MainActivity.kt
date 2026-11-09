package br.unemat.ritmo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.unemat.ritmo.data.RitmoDatabase
import br.unemat.ritmo.data.SessionRepository
import br.unemat.ritmo.ui.RitmoTheme
import br.unemat.ritmo.ui.RitmoApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Sprint 05: the repository gives the UI access to the local Room database.
        val repository = SessionRepository(RitmoDatabase.get(this).sessionDao())
        setContent {
            RitmoTheme { RitmoApp(repository) }
        }
    }
}
