# Validação final — Sprint 07

## Dispositivos de teste

| Onde | Aparelho | Como |
| --- | --- | --- |
| Emulador | Pixel 6 virtual, Android 15 / API 35 (x86_64), executado pelo GitHub Actions (Ubuntu, KVM), em 23/11/2026 | Testes automáticos e capturas; fuso America/Cuiaba nas capturas ([device-date.txt](device-date.txt)) |
| Celular real | Celular Android de um integrante | Uso manual do app antes das correções (onde foram achados os problemas) e depois delas |

## Comandos

A partir de `app/`:

- `gradlew assembleDebug testDebugUnitTest lintDebug --console=plain`: **BUILD SUCCESSFUL** ([build-unit-lint.txt](build-unit-lint.txt))
- `gradlew connectedDebugAndroidTest --console=plain`: **BUILD SUCCESSFUL** ([instrumented-tests.txt](instrumented-tests.txt))

| Conjunto | Resultado |
| --- | --- |
| Testes locais | 17 aprovados, 0 falhas: `SessionRulesTest` 9, `StudyTimerTest` 5, `WeeklyGoalTest` 3 |
| Testes instrumentados | 40 aprovados, 0 falhas; 6 testes de captura ignorados nessa execução, conforme previsto ([instrumented-tests.xml](instrumented-tests.xml)) |
| Lint | 0 erros e 15 avisos ([lint.txt](lint.txt)) |
| Travamentos | Log de travamentos do Android vazio durante toda a execução ([crash-log.txt](crash-log.txt)) |

Testes instrumentados por classe: `FormTest` 7, `HistoryTest` 7, `NavigationTest` 6, `DurationTest` 5, `TimerTest` 5, `AlarmTest` 5, `NotesTest` 3, `FinalRegressionTest` 1 e `WelcomeTest` 1.

## Roteiro de regressão (a partir de uma abertura limpa)

| Passo | Como foi executado | Resultado |
| --- | --- | --- |
| 1. App abre | `pm clear` + `am start -W`: `Status: ok`, `LaunchState: COLD`, `TotalTime: 2958` ms, `MainActivity` em primeiro plano ([clean-launch.txt](clean-launch.txt)) | PASS |
| 2. Tela principal utilizável | `FinalRegressionTest` RS-01/RS-02: marca, formulário, botão, três abas e histórico vazio com a dica | PASS |
| 3. Navegação | RS-05 e `NavigationTest` (6): abas alternam, Planejar meu estudo abre o Cronômetro no topo, Ajustar duração volta a Planejar | PASS |
| 4. Estado/interação | RS-03 e `DurationTest` (5) | PASS |
| 5. Validação | RS-04 e `FormTest` (7) | PASS |
| 6. Persistência | RS-06, `HistoryTest` (7) e `NotesTest` (3, incluindo a migração do banco da versão 1 para a 2) | PASS |
| 7. Integração | RS-07, `AlarmTest` (5) e `TimerTest` (5): alarme marcado ao começar, desmarcado ao pausar, tocando no fim com som repetido e limite de 1 minuto, Parar, contagem na notificação | PASS |
| 8. Voltar | RS-08 e `NavigationTest.androidBackFromAnyTabReturnsToPlan`; na aba Planejar, a tecla Voltar leva ao launcher do Android ([clean-launch.txt](clean-launch.txt)) | PASS |
| 9. Sem travamento | Todos os testes e capturas sem exceção; [crash-log.txt](crash-log.txt) vazio | PASS |

## Capturas (cada passo em um processo novo do app)

Antes de cada passo, o script fecha o app e retira a permissão de notificação, para a janela real do sistema aparecer ([evidence-run.txt](evidence-run.txt)).

| Passo | O que mostra | Arquivo |
| --- | --- | --- |
| `finalHome` | Aba Planejar logo depois da abertura limpa | [final-home.png](final-home.png) |
| `timerStart` | Começar com permissão concedida na janela do sistema: cronômetro rodando e “Alarme marcado para 18:51.” | [final-feature.png](final-feature.png) |
| `timerStart` | Notificação fixa com a contagem e o botão Pausar | [final-notification-countdown.png](final-notification-countdown.png) |
| `timerAfterRestart` | Processo novo (o app foi fechado no passo anterior): o cronômetro continuou rodando. O cartão mostra “Sem permissão” porque o script retirou a permissão entre os passos | [final-timer-after-restart.png](final-timer-after-restart.png) |
| `finalAlarm` | Fim do tempo (o mesmo aviso que o AlarmManager entrega, sem esperar 50 minutos): “Sessão concluída: Cálculo I — limites” com o botão Parar | [final-alarm.png](final-alarm.png) |
| `finalHistory` | Aba Histórico com a meta da semana e 5 sessões (4 gravadas direto no banco para a captura e 1 salva pela interface) | [final-history.png](final-history.png) |
| `finalNotes` | Detalhes de uma sessão com a anotação “O que você fez” e o botão Editar anotação | [final-notes.png](final-notes.png) |

## Critérios da SPEC-007

| Critério | Resultado | Como foi verificado |
| --- | --- | --- |
| AC-01 | PASS | [SPEC-007](../../docs/specs/SPEC-007.md) |
| AC-02 | PASS | Tabela de revisão em [SPRINT-07.md](../../SPRINT-07.md) |
| AC-03 | PASS | `FinalRegressionTest`; `clean-launch.txt` |
| AC-04 | PASS | `NavigationTest` |
| AC-05 | PASS | `DurationTest`; RS-03 |
| AC-06 | PASS | `FormTest`; RS-04 |
| AC-07 | PASS | `HistoryTest`; `NotesTest` (migração) |
| AC-08 | PASS | `AlarmTest`; cartão “Sem permissão” em `final-timer-after-restart.png` |
| AC-09 | PASS | `crash-log.txt` vazio |
| AC-10 | PASS | Capturas desta pasta |
| AC-11 | PASS | [README](../../README.md) |
| AC-12 | PASS | `.gitignore`; nenhuma chave, token ou senha |
| AC-13 | PASS | Seção 9 da SPEC-007 |
| AC-14 | PASS | Divisão da demonstração em SPRINT-07.md |
| AC-15 | PASS | `NavigationTest` |
| AC-16 | PASS | `TimerTest.timerKeepsRunningWhenLeavingTheTab`, `TimerTest.runningTimerIsSavedOutsideTheScreen`; `final-timer-after-restart.png` |
| AC-17 | PASS | `AlarmTest.startSchedulesTheAlarmAndPauseCancelsIt`, `alarmRingsAtTheEndAndStops`, `stopButtonOnTheNotificationSilencesTheAlarm` |
| AC-18 | PASS | `AlarmTest.notificationButtonsPauseAndContinueTheTimer`; `final-notification-countdown.png` |
| AC-19 | PASS | `NotesTest` |

## Observações

- O alarme real depende do relógio: nos testes, o fim do tempo é simulado entregando ao app o mesmo aviso do AlarmManager. O alarme marcado de verdade foi conferido no celular, com uma sessão curta.
- Em uma das execuções, uma janela do sistema do emulador tirou o foco do app e derrubou testes sem relação com o código. O script passou a esperar o Android terminar de ligar e a registrar a janela em foco antes dos testes.
- Na primeira execução com as abas, um teste mostrou que a aba Cronômetro abria na rolagem da visita anterior ao planejar uma sessão nova; corrigido para abrir no topo.
- As capturas das sprints 04 a 06 continuam nas pastas de cada sprint como registro; os passos de captura antigos foram substituídos pelos da Sprint 07, porque as telas mudaram.
