package br.unemat.ritmo.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Casos-limite das regras VR-01 a VR-04 da SPEC-004. */
class SessionRulesTest {

    @Test fun emptyOrBlankSubjectIsRequired() {
        assertEquals("Informe o assunto que você vai estudar.", SessionRules.subjectError(""))
        assertEquals("Informe o assunto que você vai estudar.", SessionRules.subjectError("    "))
    }

    @Test fun subjectBelowMinimumIsRejected() {
        assertEquals("Use pelo menos 3 caracteres.", SessionRules.subjectError("ab"))
        assertEquals("Use pelo menos 3 caracteres.", SessionRules.subjectError("  ab  "))
    }

    @Test fun subjectLimitsAreInclusive() {
        assertNull(SessionRules.subjectError("abc"))
        assertNull(SessionRules.subjectError("a".repeat(40)))
        assertEquals("Use no máximo 40 caracteres.", SessionRules.subjectError("a".repeat(41)))
    }

    @Test fun spacesAtTheEndsDoNotCount() {
        assertNull(SessionRules.subjectError("  " + "a".repeat(40) + "  "))
    }

    @Test fun goalIsOptionalWithMaximum() {
        assertNull(SessionRules.goalError(""))
        assertNull(SessionRules.goalError("b".repeat(80)))
        assertEquals("Use no máximo 80 caracteres.", SessionRules.goalError("b".repeat(81)))
    }

    @Test fun formIsValidOnlyWhenBothFieldsAre() {
        assertTrue(SessionRules.isValid("Cálculo I", ""))
        assertFalse(SessionRules.isValid("", ""))
        assertFalse(SessionRules.isValid("Cálculo I", "b".repeat(81)))
    }
}
