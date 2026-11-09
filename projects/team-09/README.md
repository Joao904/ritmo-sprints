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

Sprint 03: Adicionamos a tela “Seu plano”, que apresenta a duração escolhida e orientações para preparar o ambiente, concentrar-se e fazer uma pausa. O retorno à tela inicial preserva a duração selecionada.

O escopo até a sprint 3 é apresentar o aplicativo, selecionar a duração e consultar o plano de estudo. Cronômetro, histórico, banco de dados, login e integrações externas ficam fora desta entrega.

## Executar


Sprint 00: Configuramos o ambiente Android e criamos uma aplicação mínima em Kotlin com Jetpack Compose. O projeto foi compilado e executado no emulador.

O escopo até a sprint 3 é apresentar o aplicativo, selecionar a duração e consultar o plano de estudo. Cronômetro, histórico, banco de dados, login e integrações externas ficam fora desta entrega.

## Executar

1. Abra a pasta `app/`, que contém `settings.gradle.kts`, no Android Studio.
2. Instale SDK Platform 35, Build Tools 35.0.0 e Platform Tools pelo SDK Manager.
3. Use JDK 17 ou 21 e aguarde a sincronização do Gradle.
4. Execute o módulo `app` em um dispositivo com API 24 ou superior.

O projeto usa Kotlin 2.1.20, AGP 8.9.1 e Gradle 8.11.1. A validação foi realizada em um Pixel 6 virtual com Android 15/API 35.


O projeto usa Kotlin 2.1.20, AGP 8.9.1 e Gradle 8.11.1. A validação foi realizada em um Pixel 6 virtual com Android 15/API 35.

A partir de `app/`, no PowerShell:

```powershell
.\gradlew.bat assembleDebug
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

Cada relatório reúne a especificação, as decisões de implementação, os resultados de validação e as evidências do respectivo incremento.

## Uso de IA

Utilizamos o Codex na especificação, geração de código, configuração, depuração, testes e documentação. A validação incluiu compilação, execução no emulador, testes instrumentados quando aplicáveis e capturas via ADB.

## Referências

- [Repositório da disciplina](https://github.com/brenofeliix/mobile-development-2026-2)
- Material de aula: *Sprint 01 — Product Definition & First Screen*, exemplo BCN Move.
- [Estado em Compose](https://developer.android.com/develop/ui/compose/state)
- [Navigation Compose](https://developer.android.com/develop/ui/compose/navigation)
