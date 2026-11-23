# SPEC-007 — Qualidade final do Ritmo

> **Team:** Team 09<br>
> **Sprint:** Sprint 07<br>
> **Status:** Validada<br>
> **Related Sprint:** [SPRINT-07.md](../../SPRINT-07.md)

## 1. Context

**Problema:** Ao longo de seis sprints, o Ritmo ganhou várias funções: tela inicial, escolha da duração, navegação para o plano, formulário com validação, histórico salvo no aparelho, aviso por notificação, cronômetro e meta da semana. Cada sprint foi validada de forma isolada. Falta confirmar que o produto inteiro funciona em conjunto, do momento em que o app abre até a sessão ser salva.<br>
**Usuários:** Estudantes universitários, incluindo alunos da UNEMAT.<br>
**Contexto:** Entrega final da disciplina. Esta SPEC define os critérios de qualidade que o app completo precisa cumprir. Ao usar o app em um celular real, a equipe encontrou três falhas de uso que os testes anteriores não cobriam: (1) nada tocava quando o cronômetro chegava a zero; (2) o app era uma tela longa só, sem como alternar entre planejar, cronometrar e ver o histórico; (3) sair da tela do plano descartava o cronômetro. As correções entram nos requisitos FR-09 a FR-12. A equipe também pediu um lugar para registrar o que foi feito em cada sessão (FR-13).

## 2. Objective

Garantir que o fluxo principal funcione a partir de uma abertura limpa, sem travamentos, com navegação, validação, persistência e integração se comportando como nas SPECs anteriores, e registrar as limitações conhecidas.

## 3. User Scenario

**Dado** que o estudante acabou de instalar o Ritmo e ainda não tem nenhuma sessão salva,<br>
**quando** abre o app, escolhe 40 minutos, informa o assunto “Cálculo I — limites”, planeja, começa o cronômetro, vai para a aba Histórico e volta, pausa, continua e salva a sessão,<br>
**então** o cronômetro não parou ao trocar de aba, o alarme do fim foi marcado sozinho, a aba Histórico mostra “1 sessão salva · 40 minutos no total” e a meta da semana com 40 min, e o app não trava em nenhum momento.

**Dado** que o cronômetro está rodando e o estudante fechou o app,<br>
**quando** o tempo acaba,<br>
**então** o alarme toca por até 1 minuto ou até ele tocar em Parar.

## 4. Final Quality Requirements

### FR-01 — Abertura

O app abre direto na tela inicial, sem erro, mesmo sem dados salvos. A tela mostra a marca, a proposta, a duração, o formulário, o botão Planejar meu estudo e o histórico, com o estado vazio explicado.

### FR-02 — Navegação

Planejar meu estudo leva a Seu plano só com dados válidos. Voltar (na tela ou no Android) e Ajustar duração retornam à tela inicial com duração, assunto e objetivo preservados. Na tela inicial, o Voltar do Android sai do app.

### FR-03 — Validação de entrada

As regras VR-01 a VR-05 (SPEC-004 e SPEC-006) continuam valendo: mensagens visíveis abaixo do campo e o plano não abre com dados inválidos.

### FR-04 — Persistência

Salvar no histórico grava a sessão no Room (SPEC-005). O histórico e a meta da semana continuam iguais depois de recriar a tela e depois de fechar e abrir o app. A semana da meta vira na segunda-feira, mesmo com o app aberto.

### FR-05 — Estados da integração

O cartão do alarme mostra os estados: marcado sozinho ao começar, marcado para HH:mm, pausado junto com o cronômetro, tempo acabou (com Parar alarme) e sem permissão. Sem permissão, o plano e o cronômetro continuam funcionando.

### FR-06 — Jornada sem travamento

O roteiro de regressão (seção 8) termina sem exceção e o log de travamentos do Android fica vazio durante toda a validação.

### FR-07 — Consistência visual e retorno ao usuário

Mesmas cores (Paper, Forest, Lime), cantos arredondados e termos em todas as telas (“sessão”, “plano”, “histórico”, “aviso”). Toda ação tem resposta visível: mensagem de erro, “Sessão salva no histórico.”, “Sessão excluída.” com Desfazer, “Aviso marcado para HH:mm.” Botões principais com pelo menos 48 dp de altura.

### FR-09 — Abas

Uma barra inferior com as abas **Planejar** (duração, assunto e objetivo), **Cronômetro** (plano, cronômetro e alarme) e **Histórico** (meta da semana e sessões). Planejar meu estudo abre a aba Cronômetro e Salvar no histórico abre a aba Histórico. O Voltar do Android, em qualquer aba, leva a Planejar; em Planejar, sai do app. Sem sessão planejada, a aba Cronômetro explica o que fazer.

### FR-10 — Cronômetro que não para sozinho

O cronômetro continua contando ao trocar de aba, ao sair do app e com o app fechado. Só para com Pausar ou Recomeçar, ou quando o tempo acaba. Ao abrir o app de novo, mostra o tempo certo. Planejar outra sessão com o cronômetro já começado pede confirmação.

### FR-11 — Alarme automático no fim

Começar ou Continuar marcam o alarme no horário exato do fim (AlarmManager); Pausar e Recomeçar desmarcam. No fim, o som de alarme se repete por até 1 minuto ou até o estudante tocar em Parar (na notificação ou em Parar alarme, no app). Sem permissão de notificação, o cronômetro funciona e o cartão avisa que o alarme não vai tocar, com Abrir configurações e Permitir.

### FR-12 — Contagem na notificação

Com o cronômetro rodando, uma notificação fixa mostra o assunto, a contagem regressiva e o botão Pausar. Pausado, mostra o tempo que falta e o botão Continuar. Tocar nela abre a aba Cronômetro.

### FR-13 — Anotação do que foi estudado

Na aba Histórico, tocar em uma sessão abre uma janela com tempo, data, objetivo e **o que o estudante fez**. O botão Anotar (ou Editar anotação) abre outra janela para escrever até 300 caracteres. Na lista, a sessão continua compacta: só aparece a marca “Com anotação”. A anotação fica no banco (coluna `notes`, versão 2); a migração da versão 1 mantém as sessões já salvas.

### FR-08 — Repositório limpo e reproduzível

Sem `.idea/`, `.gradle/`, `build/`, `local.properties`, chaves, tokens ou senhas. O Gradle Wrapper fica no repositório, e o README explica como compilar e executar.

## 5. Constraints

Kotlin, Jetpack Compose, Material 3, SDK mínimo 24 e compilação com SDK 35. As telas mantêm o visual das sprints anteriores (cores, fontes e cartões); a mudança é na organização em abas. O aviso por WorkManager da SPEC-006 foi substituído pelo alarme do AlarmManager, que toca no horário exato.

## 6. Out of Scope

Login, sincronização em nuvem, edição de sessões, várias sessões ao mesmo tempo e remarcar o alarme sozinho depois de reiniciar o celular (ele é remarcado ao abrir o app).

## 7. Acceptance Criteria

| AC | Requisito | Condição |
| --- | --- | --- |
| AC-01 | — | SPEC-007 existe. |
| AC-02 | — | As SPEC-001 a SPEC-006 foram revisadas (tabela em SPRINT-07.md). |
| AC-03 | FR-01 | O fluxo principal funciona a partir da abertura limpa. |
| AC-04 | FR-02 | A navegação funciona. |
| AC-05 | FR-01 | A função de estado/interação funciona. |
| AC-06 | FR-03 | A validação do formulário funciona. |
| AC-07 | FR-04 | A persistência funciona como na SPEC-005. |
| AC-08 | FR-05 | A integração funciona ou falha de forma tratada, como na SPEC-006. |
| AC-09 | FR-06 | Nenhum travamento crítico no fluxo documentado. |
| AC-10 | — | `final-home.png`, `final-feature.png` e `final-validation.md` existem. |
| AC-11 | — | O README da equipe está completo. |
| AC-12 | FR-08 | Nenhum segredo ou pasta gerada no repositório. |
| AC-13 | — | Os requisitos principais podem ser rastreados até o código e a evidência. |
| AC-14 | — | Os dois integrantes conseguem explicar o projeto. |
| AC-15 | FR-09 | As três abas alternam e o Voltar leva a Planejar. |
| AC-16 | FR-10 | O cronômetro continua ao trocar de aba e depois de o processo do app ser encerrado. |
| AC-17 | FR-11 | Começar marca o alarme, Pausar desmarca, e no fim o alarme toca (som repetido, até 1 minuto) e para com Parar. |
| AC-18 | FR-12 | A notificação mostra a contagem com Pausar/Continuar. |
| AC-19 | FR-13 | A anotação é escrita, editada e mantida; um banco da versão 1 é migrado sem perder sessões. |

## 8. Regression Script

Executado a partir de uma abertura limpa (dados do app apagados com `pm clear`):

| Passo | O que é verificado | SPEC |
| --- | --- | --- |
| RS-01 | O app abre na tela inicial. | 001 |
| RS-02 | A tela inicial é utilizável e o histórico vazio mostra a dica. | 001, 005 |
| RS-03 | O atalho 45 min fica marcado e o resumo muda na hora. | 002, 006 |
| RS-04 | Minutos “2” e assunto vazio mostram as mensagens e não abrem o plano. | 004, 006 |
| RS-05 | Dados válidos abrem Seu plano com duração, assunto e objetivo. | 003, 004 |
| RS-06 | Salvar no histórico grava a sessão; o total e a meta da semana somam os minutos; os dados continuam após recriar a tela e após fechar o app. | 005 |
| RS-07 | Cronômetro e alarme: Começar marca o alarme sozinho; trocar de aba não para o cronômetro; Pausar desmarca o alarme e Continuar marca de novo; no fim, o alarme toca e para com Parar; o cronômetro continua depois de o app ser fechado. | 006, 007 |
| RS-08 | Voltar do Android na aba Cronômetro leva a Planejar com os dados preservados e o cronômetro rodando; em Planejar, sai do app. | 003, 007 |
| RS-09 | Nenhum travamento: jornada completa sem exceção e log de travamentos vazio. | 007 |

## 9. Traceability

| Requisito | Código | Teste / evidência |
| --- | --- | --- |
| Tela inicial (SPEC-001) | `ui/WelcomeScreen.kt` | `WelcomeTest`, `final-home.png` |
| Duração (SPEC-002, SPEC-006) | `ui/DurationPicker.kt`, `SessionRules.minutesError` | `DurationTest`, `SessionRulesTest` |
| Navegação (SPEC-003) | `ui/RitmoApp.kt` (`NavHost`) | `NavigationTest`, `clean-launch.txt` |
| Formulário (SPEC-004) | `ui/SessionForm.kt`, `model/SessionInput.kt` | `FormTest`, `SessionRulesTest` |
| Histórico (SPEC-005) | `data/` (Room), `ui/HistoryScreen.kt`, `ui/HistorySection.kt` | `HistoryTest`, `final-history.png` |
| Cronômetro (SPEC-006, FR-10) | `timer/SessionClock.kt`, `model/StudyTimer.kt`, `ui/PlanScreen.kt` | `TimerTest`, `StudyTimerTest`, `final-feature.png`, `final-timer-after-restart.png` |
| Abas (FR-09) | `ui/RitmoApp.kt` (`NavigationBar` + `NavHost`) | `NavigationTest`, `final-home.png` |
| Alarme (FR-11) | `timer/AlarmScheduler.kt` (`TimerReceiver`), `timer/TimerNotifications.kt` | `AlarmTest`, `final-alarm.png` |
| Contagem na notificação (FR-12) | `timer/TimerNotifications.kt` | `AlarmTest`, `final-notification-countdown.png` |
| Anotação (FR-13) | `ui/HistorySection.kt` (`SessionDetailsDialog`, `NotesDialog`), `data/RitmoDatabase.kt` (`MIGRATION_1_2`) | `NotesTest`, `final-notes.png` |
| Qualidade final (SPEC-007) | — | `FinalRegressionTest`, `crash-log.txt`, `final-validation.md` |

## 10. Validation Plan

1. Compilação, testes locais e lint (`assembleDebug testDebugUnitTest lintDebug`).
2. Todos os testes instrumentados, incluindo o novo `FinalRegressionTest.completeJourneyFromLaunch`, que segue os passos RS-01 a RS-09 em uma única jornada.
3. Abertura limpa e Voltar na tela raiz pelo `adb` (`clean-launch.txt`).
4. Passos de captura da Sprint 07, cada um em um processo novo do app; o passo `timerAfterRestart` confere o cronômetro depois de o app ser fechado.
5. Log de travamentos do Android ao final (`crash-log.txt`).

## 11. Validation Results

Resultados em [final-validation.md](../../evidence/sprint-07/final-validation.md).

## 12. Evidence

![Tela inicial](../../evidence/sprint-07/final-home.png)
![Cronômetro rodando com o alarme marcado](../../evidence/sprint-07/final-feature.png)
![Alarme do fim da sessão](../../evidence/sprint-07/final-alarm.png)

## 13. Specification Status

- [x] Requisitos de qualidade final definidos.
- [x] Roteiro de regressão definido.
- [x] Rastreabilidade definida.
- [x] Implementação validada e evidências registradas.
