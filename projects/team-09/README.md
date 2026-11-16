# Ritmo — Team 09

**Estude no seu ritmo. Uma sessão de cada vez.**

Projeto da disciplina Desenvolvimento de Softwares para Dispositivos Móveis (FALECT-CC-040), UNEMAT/AIA, 2026.2. Professor: Breno Felix de Sousa.

| Integrante | Matrícula |
| --- | --- |
| Fernando Henrique Cobianchi | 20220059497 |
| João Victor R. Peres | 20230079303 |

## Proposta

Estudantes com pouco tempo disponível podem adiar o estudo por não saberem como organizar uma sessão curta. O Ritmo ajuda a transformar esse tempo em um plano simples de concentração. O público-alvo são estudantes universitários.

## Versão desta branch

Sprint 06: o estudante digita quantos minutos vai estudar (de 5 a 180, com atalhos de 15, 25, 45 e 60). No plano, o botão Avisar quando acabar pede a permissão de notificação e agenda, com o WorkManager, um aviso para o fim da sessão, que chega mesmo com o app fechado. Se a permissão for negada, o app explica e oferece abrir as configurações.

Também na Sprint 06: cronômetro em regressiva no plano (Começar, Pausar, Continuar e Recomeçar), com o aviso de fim acompanhando o cronômetro; meta da semana na tela inicial; exclusão de sessões do histórico, com Desfazer; e Ver todas, para listar mais que as 5 sessões recentes.

O escopo até a sprint 6 é apresentar o aplicativo, informar duração, assunto e objetivo, consultar o plano com cronômetro, salvar e excluir sessões do histórico, acompanhar a meta da semana e receber o aviso de fim da sessão. Edição de sessões, sincronização e login ficam fora desta entrega.

## Executar

1. Abra a pasta `app/`, que contém `settings.gradle.kts`, no Android Studio.
2. Instale SDK Platform 35, Build Tools 35.0.0 e Platform Tools pelo SDK Manager.
3. Use JDK 17 ou 21 e aguarde a sincronização do Gradle.
4. Execute o módulo `app` em um dispositivo com API 24 ou superior.

O projeto usa Kotlin 2.1.20, AGP 8.9.1 e Gradle 8.11.1. A validação foi realizada em um Pixel 6 virtual com Android 15/API 35.

A partir de `app/`, no PowerShell:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat connectedDebugAndroidTest
.\gradlew.bat lintDebug
```

## Entregas

| Sprint | Documentação | Pull Request |
| --- | --- | --- |
| 00 | [Relatório](SPRINT-00.md) | [PR #19](https://github.com/brenofeliix/mobile-development-2026-2/pull/19) |
| 01 | [Relatório](SPRINT-01.md) | [PR #20](https://github.com/brenofeliix/mobile-development-2026-2/pull/20) |
| 02 | [Relatório](SPRINT-02.md) | [PR #21](https://github.com/brenofeliix/mobile-development-2026-2/pull/21) |
| 03 | [Relatório](SPRINT-03.md) | [PR #22](https://github.com/brenofeliix/mobile-development-2026-2/pull/22) |
| 04 | [Relatório](SPRINT-04.md) | [PR #41](https://github.com/brenofeliix/mobile-development-2026-2/pull/41) |
| 05 | [Relatório](SPRINT-05.md) | [PR #42](https://github.com/brenofeliix/mobile-development-2026-2/pull/42) |
| 06 | [Relatório](SPRINT-06.md) | [PR #45](https://github.com/brenofeliix/mobile-development-2026-2/pull/45) |

Cada relatório reúne a especificação, as decisões de implementação, os resultados de validação e as evidências do respectivo incremento.

## Uso de IA

Sprints 00 a 03: utilizamos o Codex na especificação, geração de código, configuração, depuração, testes e documentação. Sprints 04 a 06: utilizamos o Claude (Claude Code) para as mesmas tarefas. A validação incluiu compilação, execução no emulador, testes instrumentados e capturas de tela.

## Referências

- [Repositório da disciplina](https://github.com/brenofeliix/mobile-development-2026-2)
- Material de aula: *Sprint 01 — Product Definition & First Screen*, exemplo BCN Move.
- [Estado em Compose](https://developer.android.com/develop/ui/compose/state)
- [Navigation Compose](https://developer.android.com/develop/ui/compose/navigation)
- [Text fields em Compose](https://developer.android.com/develop/ui/compose/text/user-input)
- [Room](https://developer.android.com/training/data-storage/room)
