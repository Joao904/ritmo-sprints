package br.unemat.ritmo.data

import android.content.Context
import br.unemat.ritmo.model.WeeklyGoal

/** Guarda a meta da semana nas preferências do app (um número só não precisa de tabela). */
class GoalStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("ritmo", Context.MODE_PRIVATE)

    fun get(): Int = prefs.getInt(KEY, WeeklyGoal.DEFAULT)

    fun set(minutes: Int) = prefs.edit().putInt(KEY, minutes).apply()

    fun clear() = prefs.edit().remove(KEY).apply()

    private companion object { const val KEY = "weekly_goal_minutes" }
}
