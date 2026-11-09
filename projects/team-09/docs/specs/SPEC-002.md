# SPEC-002 — Escolha da duração de estudo

> **Team:** Team 09<br>
> **Sprint:** Sprint 02<br>
> **Status:** Validada<br>
> **Related Sprint:** [SPRINT-02.md](../../SPRINT-02.md)

## 1. Context

**Problema:** Estudantes com pouco tempo disponível podem adiar o estudo por não saberem como organizar uma sessão curta.<br>
**Usuários:** Estudantes universitários, incluindo alunos da UNEMAT.<br>
**Contexto:** Intervalos entre atividades em que o estudante quer planejar um período de concentração.

## 2. Objective

Permitir ajustar a duração de uma sessão e perceber imediatamente a mudança no resumo.

## 3. User Scenario

Dado que o estudante deseja organizar um período de estudo, ao abrir o app, 25 minutos estão selecionados; ao tocar 15 min ou 45 min, a opção marcada e o resumo refletem a nova duração.

## 4. Functional Requirements

### FR-01

Iniciar com 25 minutos selecionados.

### FR-02

Oferecer opções de 15, 25 e 45 minutos, com seleção exclusiva.

### FR-03

Atualizar o resumo Duração escolhida: N minutos. após cada toque.

### FR-04

Preservar a apresentação da sprint 1 e permitir selecionar repetidamente sem acumular valores.

## 5. Constraints

Kotlin, Jetpack Compose, Material 3, Android Studio, SDK mínimo 24 e compilação com SDK 35. Sem acesso à rede, permissões de dispositivo ou armazenamento permanente. Dependências com versões fixas.

## 6. Out of Scope

Navegação, cronômetro, banco, histórico, login, APIs e formulários.

## 7. Acceptance Criteria

### AC-01

**Related requirement:** FR-01<br>
**Condition:** Após limpar os dados e abrir o app, 25 min está selecionado e o resumo informa 25 minutos.

### AC-02

**Related requirement:** FR-02<br>
**Condition:** Selecionar 15 min marca apenas essa opção; selecionar 45 min marca apenas 45 min.

### AC-03

**Related requirement:** FR-03<br>
**Condition:** Após cada toque, o resumo mostra exatamente a duração selecionada.

### AC-04

**Related requirement:** FR-04<br>
**Condition:** Repetir 15 → 45 → 25 três vezes mantém o resultado correto; nome, slogan e ação principal continuam presentes.

## 8. Requirement Traceability

Esta SPEC descreve a branch da sprint 2. Na sprint 3, `selectedMinutes` foi elevado de `WelcomeScreen` para `RitmoApp` para compartilhar a escolha entre destinos; os critérios de seleção e resumo continuam cobertos por `DurationTest`.

| Requirement | Implemented In | Acceptance Criterion | Evidence |
| --- | --- | --- | --- |
| FR-01 | `app/app/src/main/java/br/unemat/ritmo/ui/WelcomeScreen.kt / rememberSaveable e mutableStateOf` | AC-01 | [Validação](../../evidence/sprint-02/validation.md) e capturas abaixo |
| FR-02 | `app/app/src/main/java/br/unemat/ritmo/ui/DurationPicker.kt / FilterChip` | AC-02 | [Validação](../../evidence/sprint-02/validation.md) e capturas abaixo |
| FR-03 | `app/app/src/main/java/br/unemat/ritmo/ui/DurationPicker.kt / Text` | AC-03 | [Validação](../../evidence/sprint-02/validation.md) e capturas abaixo |
| FR-04 | `app/app/src/main/java/br/unemat/ritmo/ui/WelcomeScreen.kt / WelcomeScreen` | AC-04 | [Validação](../../evidence/sprint-02/validation.md) e capturas abaixo |

## 9. Implementation Plan

Adicionar DurationPicker com estado elevado à WelcomeScreen. Usar FilterChip e callback onSelect; não duplicar estado nos chips.

**Estado e fluxo:** Um Int selectedMinutes inicia em 25, mantido por rememberSaveable { mutableStateOf(25) }. onClick substitui o valor; recomposição atualiza seleção e resumo. rememberSaveable também salva o valor em recriações de Activity; não é banco de dados.

## 10. Validation Plan

Compilar com `gradlew.bat assembleDebug`, instalar no emulador e executar:

1. Limpar os dados, abrir e capturar before-interaction.png (25 minutos).
2. Tocar 15 min e verificar seleção e resumo.
3. Tocar 45 min, verificar seleção exclusiva e capturar after-interaction.png.
4. Repetir a sequência 15 → 45 → 25 três vezes.
5. Conferir os elementos da SPEC-001.

## 11. Validation Results

Resultados executados e registrados em [validation.md](../../evidence/sprint-02/validation.md).

### Environment Used for Validation

Pixel 6 virtual, Android 15/API 35, x86_64, Windows/WHPX. Compilação Gradle 8.11.1, JDK 21. Logs de build e testes em `evidence/sprint-02/`.

## 12. Evidence

- [before-interaction.png](../../evidence/sprint-02/before-interaction.png)
- [after-interaction.png](../../evidence/sprint-02/after-interaction.png)

## 13. AI-Assisted Development

**Ferramenta:** Codex.

Utilizamos o Codex na especificação, geração de código, configuração, depuração, testes e documentação. A validação incluiu compilação, execução no emulador, testes instrumentados quando aplicáveis e capturas via ADB.

**Revisão e alterações:** Com apoio do Codex, adotamos rememberSaveable e testes de seleção repetida e recriação da Activity. Nenhuma alteração manual adicional registrada.

## 14. Suggested Prompt for AI Assistance

Solicitação de apoio: implementar os requisitos desta SPEC em Kotlin e Compose, explicar o fluxo de estado ou navegação e propor cenários de validação, respeitando o escopo da sprint.

## 15. Deliverables

Projeto Android atualizado, esta SPEC, SPRINT-02.md, capturas de execução e resultados de validação em evidence/sprint-02/.

## 16. Specification Status

- [x] Contexto, objetivo, FRs, limites e critérios mensuráveis definidos.
- [x] Plano de implementação e de validação definidos.
- [x] Implementação validada e evidências registradas.
