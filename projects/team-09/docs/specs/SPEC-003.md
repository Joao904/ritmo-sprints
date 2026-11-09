# SPEC-003 — Navegação para o plano da sessão

> **Team:** Team 09<br>
> **Sprint:** Sprint 03<br>
> **Status:** Validada<br>
> **Related Sprint:** [SPRINT-03.md](../../SPRINT-03.md)

## 1. Context

**Problema:** Estudantes com pouco tempo disponível podem adiar o estudo por não saberem como organizar uma sessão curta.<br>
**Usuários:** Estudantes universitários, incluindo alunos da UNEMAT.<br>
**Contexto:** Intervalos entre atividades em que o estudante quer planejar um período de concentração.

## 2. Objective

Levar o estudante da escolha de duração até orientações de uma sessão e permitir retornar para ajustar a escolha.

## 3. User Scenario

Dado que o estudante deseja organizar um período de estudo, ao escolher 45 min e tocar Planejar meu estudo, o estudante vê Seu plano com 45 minutos; ao voltar, encontra a escolha preservada.

## 4. Functional Requirements

### FR-01

Abrir na rota inicio, com apresentação e seleção da sprint 2.

### FR-02

Navegar para a rota plano pela ação Planejar meu estudo usando Navigation Compose.

### FR-03

Exibir Seu plano, duração escolhida e três orientações: preparar, concentrar e fazer uma pausa.

### FR-04

Retornar à tela inicial pelo botão Voltar, pela ação Ajustar duração e pelo Voltar do Android.

### FR-05

Preservar a escolha ao voltar e evitar destinos duplicados em toques repetidos.

## 5. Constraints

Kotlin, Jetpack Compose, Material 3, Android Studio, SDK mínimo 24 e compilação com SDK 35. Sem acesso à rede, permissões de dispositivo ou armazenamento permanente. Dependências com versões fixas.

## 6. Out of Scope

Cronômetro, execução de sessão, persistência entre novas sessões do app, banco, histórico, login, APIs e formulários.

## 7. Acceptance Criteria

### AC-01

**Related requirement:** FR-01<br>
**Condition:** Um lançamento novo abre inicio com Ritmo e seletor visíveis.

### AC-02

**Related requirement:** FR-02<br>
**Condition:** Tocar Planejar meu estudo abre plano, com Seu plano visível.

### AC-03

**Related requirement:** FR-03<br>
**Condition:** Selecionar 15, 25 ou 45 minutos e navegar mostra a duração correspondente e as três orientações.

### AC-04

**Related requirement:** FR-04<br>
**Condition:** Cada uma das três formas de retorno leva à tela inicial; o Voltar do Android na tela inicial encerra a Activity normalmente.

### AC-05

**Related requirement:** FR-05<br>
**Condition:** Após voltar de plano, a seleção permanece; repetir o ciclo e toques rápidos não cria uma cadeia de planos duplicados.

## 8. Requirement Traceability

| Requirement | Implemented In | Acceptance Criterion | Evidence |
| --- | --- | --- | --- |
| FR-01 | `app/app/src/main/java/br/unemat/ritmo/ui/RitmoApp.kt / NavHost(startDestination = inicio)` | AC-01 | [Validação](../../evidence/sprint-03/validation.md) e capturas abaixo |
| FR-02 | `app/app/src/main/java/br/unemat/ritmo/ui/RitmoApp.kt / navigate e WelcomeScreen.kt / onPlan` | AC-02 | [Validação](../../evidence/sprint-03/validation.md) e capturas abaixo |
| FR-03 | `app/app/src/main/java/br/unemat/ritmo/ui/PlanScreen.kt / PlanScreen` | AC-03 | [Validação](../../evidence/sprint-03/validation.md) e capturas abaixo |
| FR-04 | `app/app/src/main/java/br/unemat/ritmo/ui/RitmoApp.kt / popBackStack e PlanScreen.kt / onBack` | AC-04 | [Validação](../../evidence/sprint-03/validation.md) e capturas abaixo |
| FR-05 | `app/app/src/main/java/br/unemat/ritmo/ui/RitmoApp.kt / rememberSaveable e launchSingleTop` | AC-05 | [Validação](../../evidence/sprint-03/validation.md) e capturas abaixo |

## 9. Implementation Plan

Criar RitmoApp com rememberNavController e NavHost; separar WelcomeScreen e PlanScreen. Acrescentar apenas androidx.navigation:navigation-compose:2.8.9 para atender ao requisito explícito.

**Estado e fluxo:** selectedMinutes é elevado ao RitmoApp, acima do NavHost, e salvo por rememberSaveable. As telas recebem valores e callbacks. O NavController mantém a pilha; launchSingleTop evita duplicar plano; popBackStack retorna a inicio.

## 10. Validation Plan

Compilar com `gradlew.bat assembleDebug`, instalar no emulador e executar:

1. Abrir o app, selecionar 45 minutos e capturar screen-a.png.
2. Tocar Planejar meu estudo, conferir Seu plano, duração e orientações; capturar screen-b.png.
3. Testar Voltar da barra, Ajustar duração e Voltar do Android em ciclos separados.
4. Verificar preservação da seleção e repetir com 15 e 25 minutos.
5. Tocar rapidamente a ação de avanço e verificar que um único retorno chega à tela inicial.
6. Executar a regressão da sprint 2 e testes instrumentados.

## 11. Validation Results

Resultados executados e registrados em [validation.md](../../evidence/sprint-03/validation.md).

### Environment Used for Validation

Pixel 6 virtual, Android 15/API 35, x86_64, Windows/WHPX. Compilação Gradle 8.11.1, JDK 21. Logs de build e testes em `evidence/sprint-03/`.

## 12. Evidence

- [screen-a.png](../../evidence/sprint-03/screen-a.png)
- [screen-b.png](../../evidence/sprint-03/screen-b.png)
- [back-preserved.png](../../evidence/sprint-03/back-preserved.png)

## 13. AI-Assisted Development

**Ferramenta:** Codex.

Utilizamos o Codex na especificação, geração de código, configuração, depuração, testes e documentação. A validação incluiu compilação, execução no emulador, testes instrumentados quando aplicáveis e capturas via ADB.

**Revisão e alterações:** Com apoio do Codex, adicionamos a dependência de Espresso aos testes e isolamos windowLightNavigationBar em values-v27 para manter minSdk 24. Nenhuma alteração manual adicional registrada.

## 14. Suggested Prompt for AI Assistance

Solicitação de apoio: implementar os requisitos desta SPEC em Kotlin e Compose, explicar o fluxo de estado ou navegação e propor cenários de validação, respeitando o escopo da sprint.

## 15. Deliverables

Projeto Android atualizado, esta SPEC, SPRINT-03.md, capturas de execução e resultados de validação em evidence/sprint-03/.

## 16. Specification Status

- [x] Contexto, objetivo, FRs, limites e critérios mensuráveis definidos.
- [x] Plano de implementação e de validação definidos.
- [x] Implementação validada e evidências registradas.
