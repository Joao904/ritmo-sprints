package br.unemat.ritmo.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

/**
 * Agenda o aviso de fim da sessão com o WorkManager (SPEC-006).
 * O trabalho fica registrado pelo sistema e roda mesmo com o app fechado.
 * Só existe um aviso por vez: agendar de novo substitui o anterior.
 */
object SessionReminder {
    const val WORK_NAME = "fim-da-sessao"
    const val KEY_SUBJECT = "subject"
    const val KEY_MINUTES = "minutes"

    /**
     * Agenda o aviso e devolve o horário previsto (ms desde 1970).
     * [delayMs] é o tempo até o aviso: a sessão inteira ou, com o cronômetro rodando, o que falta.
     */
    fun schedule(
        context: Context,
        subject: String,
        minutes: Int,
        now: Long = System.currentTimeMillis(),
        delayMs: Long = TimeUnit.MINUTES.toMillis(minutes.toLong()),
    ): Long {
        val request = OneTimeWorkRequestBuilder<SessionEndWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(KEY_SUBJECT to subject, KEY_MINUTES to minutes))
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
        return now + delayMs
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}

/** Executado pelo WorkManager, em segundo plano, quando o tempo da sessão termina. */
class SessionEndWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val subject = inputData.getString(SessionReminder.KEY_SUBJECT) ?: return Result.failure()
        val minutes = inputData.getInt(SessionReminder.KEY_MINUTES, 0)
        // Sem permissão no momento do aviso, não há o que mostrar: o trabalho termina sem erro.
        SessionNotifier.show(applicationContext, subject, minutes)
        return Result.success()
    }
}
