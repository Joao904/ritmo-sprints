package br.unemat.ritmo.timer

import android.content.Context
import androidx.core.content.edit
import br.unemat.ritmo.model.StudyTimer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Sessão planejada: o que estudar e o cronômetro dela. */
data class ActiveSession(val subject: String, val goal: String, val minutes: Int, val timer: StudyTimer)

/**
 * Sprint 07: dono do cronômetro, fora da tela.
 * O estado fica salvo nas preferências do app, então o cronômetro continua contando ao trocar de aba,
 * ao sair do app e até com o app fechado: só para quando o estudante toca em Pausar.
 * Toda mudança também acerta o alarme do fim (AlarmScheduler) e a notificação com a contagem (TimerNotifications).
 */
object SessionClock {
    private const val PREFS = "cronometro"
    private val _state = MutableStateFlow<ActiveSession?>(null)
    val state: StateFlow<ActiveSession?> = _state
    @Volatile private var loaded = false

    /** Lê o estado salvo (uma vez por processo) e refaz alarme e notificação. Chamado ao abrir o app. */
    fun init(context: Context) {
        ensureLoaded(context)
        applyEffects(context, _state.value)
    }

    fun plan(context: Context, subject: String, goal: String, minutes: Int) {
        TimerNotifications.cancelAlarm(context)
        update(context, ActiveSession(subject.trim(), goal.trim(), minutes, StudyTimer.ofMinutes(minutes)))
    }

    /** Começar ou continuar. */
    fun start(context: Context, now: Long = System.currentTimeMillis()) =
        change(context) { it.copy(timer = it.timer.start(now)) }

    fun pause(context: Context, now: Long = System.currentTimeMillis()) =
        change(context) { if (it.timer.running) it.copy(timer = it.timer.pause(now)) else it }

    fun reset(context: Context) {
        TimerNotifications.cancelAlarm(context)
        change(context) { it.copy(timer = it.timer.reset()) }
    }

    /** Sessão salva no histórico ou descartada: some o cronômetro, o alarme e as notificações. */
    fun clear(context: Context) {
        TimerNotifications.cancelAlarm(context)
        update(context, null)
    }

    /** O AlarmManager disparou no fim do tempo: zera o cronômetro e toca o alarme. */
    fun onAlarm(context: Context) {
        ensureLoaded(context)
        val session = _state.value ?: return
        if (!session.timer.running) return // pausado ou recomeçado depois de marcar: nada a tocar
        update(context, session.copy(timer = StudyTimer(session.timer.totalMs, leftMs = 0L, endAt = null)))
        TimerNotifications.showAlarm(context, session)
    }

    fun stopAlarm(context: Context) = TimerNotifications.cancelAlarm(context)

    /** Refaz alarme e notificação (ex.: logo depois de a permissão de notificação ser concedida). */
    fun refresh(context: Context) = applyEffects(context, _state.value)

    private fun change(context: Context, block: (ActiveSession) -> ActiveSession) {
        ensureLoaded(context)
        val current = _state.value ?: return
        update(context, block(current))
    }

    private fun update(context: Context, session: ActiveSession?) {
        ensureLoaded(context)
        save(context, session)
        _state.value = session
        applyEffects(context, session)
    }

    private fun applyEffects(context: Context, session: ActiveSession?) {
        val now = System.currentTimeMillis()
        val timer = session?.timer
        when {
            session == null || timer == null -> {
                AlarmScheduler.cancel(context)
                TimerNotifications.cancelOngoing(context)
            }
            timer.running && !timer.finished(now) -> {
                AlarmScheduler.schedule(context, timer.endAt!!)
                TimerNotifications.showRunning(context, session)
            }
            timer.running -> {
                // O fim passou sem o alarme (ex.: o aparelho foi reiniciado): marca como encerrado, sem tocar.
                AlarmScheduler.cancel(context)
                TimerNotifications.cancelOngoing(context)
                val finished = session.copy(timer = StudyTimer(timer.totalMs, leftMs = 0L, endAt = null))
                save(context, finished)
                _state.value = finished
            }
            timer.started(now) && !timer.finished(now) -> {
                AlarmScheduler.cancel(context)
                TimerNotifications.showPaused(context, session)
            }
            else -> {
                AlarmScheduler.cancel(context)
                TimerNotifications.cancelOngoing(context)
            }
        }
    }

    private fun ensureLoaded(context: Context) {
        if (loaded) return
        synchronized(this) {
            if (!loaded) {
                _state.value = load(context)
                loaded = true
            }
        }
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun save(context: Context, session: ActiveSession?) = prefs(context).edit(commit = true) {
        clear()
        if (session != null) {
            putString("subject", session.subject)
            putString("goal", session.goal)
            putInt("minutes", session.minutes)
            putLong("total", session.timer.totalMs)
            putLong("left", session.timer.leftMs)
            putLong("endAt", session.timer.endAt ?: -1L)
        }
    }

    private fun load(context: Context): ActiveSession? {
        val p = prefs(context)
        val subject = p.getString("subject", null) ?: return null
        val timer = StudyTimer.fromList(listOf(p.getLong("total", 0L), p.getLong("left", 0L), p.getLong("endAt", -1L)))
        return ActiveSession(subject, p.getString("goal", "") ?: "", p.getInt("minutes", 0), timer)
    }

    /** Só para testes: esquece o estado em memória, como se o processo do app tivesse sido encerrado. */
    fun forgetInMemoryState() {
        synchronized(this) { loaded = false; _state.value = null }
    }
}
