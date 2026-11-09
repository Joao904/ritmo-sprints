# SPEC-001 — Apresentação do Ritmo

> **Team:** Team 09<br>
> **Sprint:** Sprint 01<br>
> **Status:** Validada<br>
> **Related Sprint:** [SPRINT-01.md](../../SPRINT-01.md)

## 1. Context

**Problema:** Estudantes com pouco tempo disponível podem adiar o estudo por não saberem como organizar uma sessão curta.<br>
**Usuários:** Estudantes universitários, incluindo alunos da UNEMAT.<br>
**Contexto:** Intervalos entre atividades em que o estudante quer planejar um período de concentração.

## 2. Objective

Apresentar o propósito do produto e uma ação principal em uma tela inicial organizada.

## 3. User Scenario

Dado que o estudante deseja organizar um período de estudo, ao abrir o app, o estudante encontra nome, slogan, descrição e o botão Planejar meu estudo.

## 4. Functional Requirements

### FR-01

Exibir o nome Ritmo.

### FR-02

Exibir o slogan Estude no seu ritmo. e uma descrição do objetivo.

### FR-03

Exibir o botão principal Planejar meu estudo.

### FR-04

Organizar conteúdo com Compose, tema Material, margens e rolagem em telas menores.

## 5. Constraints

Kotlin, Jetpack Compose, Material 3, Android Studio, SDK mínimo 24 e compilação com SDK 35. Sem acesso à rede, permissões de dispositivo ou armazenamento permanente. Dependências com versões fixas.

## 6. Out of Scope

Interação, navegação, cronômetro, banco, login, notificações, APIs e formulários.

## 7. Acceptance Criteria

### AC-01

**Related requirement:** FR-01<br>
**Condition:** O texto Ritmo é visível na abertura.

### AC-02

**Related requirement:** FR-02<br>
**Condition:** O slogan Estude no seu ritmo. e a descrição Uma sessão de cada vez. estão visíveis.

### AC-03

**Related requirement:** FR-03<br>
**Condition:** Planejar meu estudo está visível; o toque não navega nem causa erro nesta sprint.

### AC-04

**Related requirement:** FR-04<br>
**Condition:** Tela executa sem crash, respeita as barras do sistema e permite alcançar o conteúdo por rolagem.

## 8. Requirement Traceability

| Requirement | Implemented In | Acceptance Criterion | Evidence |
| --- | --- | --- | --- |
| FR-01 | `app/app/src/main/java/br/unemat/ritmo/ui/WelcomeScreen.kt / Brand` | AC-01 | [Validação](../../evidence/sprint-01/validation.md) e capturas abaixo |
| FR-02 | `app/app/src/main/java/br/unemat/ritmo/ui/WelcomeScreen.kt / WelcomeScreen` | AC-02 | [Validação](../../evidence/sprint-01/validation.md) e capturas abaixo |
| FR-03 | `app/app/src/main/java/br/unemat/ritmo/ui/WelcomeScreen.kt / Button` | AC-03 | [Validação](../../evidence/sprint-01/validation.md) e capturas abaixo |
| FR-04 | `app/app/src/main/java/br/unemat/ritmo/ui/Theme.kt e WelcomeScreen.kt / Column` | AC-04 | [Validação](../../evidence/sprint-01/validation.md) e capturas abaixo |

## 9. Implementation Plan

Separar Activity, tema e WelcomeScreen; usar Column, Text, Surface, Button, Spacer e Modifier.

**Estado e fluxo:** Sem estado mutável ou navegação; o botão possui callback vazio nesta etapa.

## 10. Validation Plan

Compilar com `gradlew.bat assembleDebug`, instalar no emulador e executar:

1. Instalar e abrir o APK.
2. Verificar nome, slogan e descrição.
3. Localizar e tocar Planejar meu estudo; nesta sprint não há comportamento associado.
4. Comparar a tela com os requisitos e capturar first-screen.png.

## 11. Validation Results

Resultados executados e registrados em [validation.md](../../evidence/sprint-01/validation.md).

### Environment Used for Validation

Pixel 6 virtual, Android 15/API 35, x86_64, Windows/WHPX. Compilação Gradle 8.11.1, JDK 21. Logs de build e testes em `evidence/sprint-01/`.

## 12. Evidence

- [first-screen.png](../../evidence/sprint-01/first-screen.png)

## 13. AI-Assisted Development

**Ferramenta:** Codex.

Utilizamos o Codex na especificação, geração de código, configuração, depuração, testes e documentação. A validação incluiu compilação, execução no emulador, testes instrumentados quando aplicáveis e capturas via ADB.

**Revisão e alterações:** Com apoio do Codex, organizamos a tela com margens, tema e rolagem. Nenhuma alteração manual adicional registrada.

## 14. Suggested Prompt for AI Assistance

Solicitação de apoio: implementar os requisitos desta SPEC em Kotlin e Compose, explicar o fluxo de estado ou navegação e propor cenários de validação, respeitando o escopo da sprint.

## 15. Deliverables

Projeto Android atualizado, esta SPEC, SPRINT-01.md, capturas de execução e resultados de validação em evidence/sprint-01/.

## 16. Specification Status

- [x] Contexto, objetivo, FRs, limites e critérios mensuráveis definidos.
- [x] Plano de implementação e de validação definidos.
- [x] Implementação validada e evidências registradas.
