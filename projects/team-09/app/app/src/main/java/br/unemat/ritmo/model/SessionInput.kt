package br.unemat.ritmo.model

/**
 * Regras do formulário da sessão (SPEC-004).
 * Ficam fora da tela para que a mesma regra seja usada pela UI e pelos testes.
 */
object SessionRules {
    const val SUBJECT_MIN = 3
    const val SUBJECT_MAX = 40
    const val GOAL_MAX = 80

    /** VR-01, VR-02 e VR-03: assunto obrigatório, de 3 a 40 caracteres (sem contar espaços nas pontas). */
    fun subjectError(raw: String): String? {
        val subject = raw.trim()
        return when {
            subject.isEmpty() -> "Informe o assunto que você vai estudar."
            subject.length < SUBJECT_MIN -> "Use pelo menos $SUBJECT_MIN caracteres."
            subject.length > SUBJECT_MAX -> "Use no máximo $SUBJECT_MAX caracteres."
            else -> null
        }
    }

    /** VR-04: objetivo opcional; se preenchido, até 80 caracteres. */
    fun goalError(raw: String): String? =
        if (raw.trim().length > GOAL_MAX) "Use no máximo $GOAL_MAX caracteres." else null

    fun isValid(subject: String, goal: String): Boolean =
        subjectError(subject) == null && goalError(goal) == null
}
