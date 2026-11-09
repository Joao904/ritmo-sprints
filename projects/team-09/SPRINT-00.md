# SPRINT 00 — Development Environment & Git Workflow

**Equipe 09:** Fernando Henrique Cobianchi e João Victor R. Peres.

## Objetivo e resultado

Configuramos o ambiente Android e criamos uma aplicação mínima em Kotlin com Jetpack Compose. O projeto foi compilado e executado no emulador.

## Definição do produto

**Nome:** Ritmo.

**Problema:** Estudantes com tempo limitado podem adiar o estudo por não saberem estruturar uma sessão curta.

**Público:** Estudantes universitários.

**Objetivo:** Ajudar a transformar minutos disponíveis em um plano de estudo.

**Funcionalidades iniciais:** apresentação, escolha de duração e consulta ao plano, introduzidas nas sprints 1, 2 e 3.

## Especificação

Não se aplica: a sprint 0 trata da configuração do ambiente.

## Implementação

A `MainActivity` define a interface com `setContent`. O Gradle compila o código e gera o APK, que é instalado pelo ADB e executado no emulador. O wrapper mantém a versão do Gradle consistente entre máquinas.

## Ambiente

- Android Studio Quail 4, versão 2026.1.4.7, Windows 10.
- SDK Platform 35; Build Tools 35.0.0; Platform Tools; Command-line Tools 19.0.
- JDK Temurin 21.0.10; Gradle 8.11.1; AGP 8.9.1; Kotlin 2.1.20.
- Pixel 6 virtual, Android 15/API 35, x86_64, WHPX, 2 GB de RAM.
- Git, GitHub CLI e GitHub Desktop 3.6.6. Fork: [botist/mobile-development-2026-2](https://github.com/botist/mobile-development-2026-2).

## Validação

| Critério | Resultado | Evidência |
| --- | --- | --- |
| AC-01 — Fork | PASS | Fork indicado acima |
| AC-02 — Clone | PASS | Cópia local com remotes origin e upstream |
| AC-03 — Branch | PASS | team-09/sprint-00 |
| AC-04 — Projeto Android | PASS | app/settings.gradle.kts |
| AC-05 — Build | PASS | [Log](evidence/sprint-00/build.txt) |
| AC-06 — Execução | PASS | Aplicativo instalado e aberto no emulador |
| AC-07 — Evidência | PASS | Captura abaixo |

O projeto foi clonado e a branch foi criada antes da implementação.

![Aplicação mínima em execução](evidence/sprint-00/android-running.png)

## Ajustes e limitações

Com apoio do Codex, ajustamos a versão do Command-line Tools e aguardamos o boot completo antes de instalar o APK.

Aplicação mínima para verificar o ambiente.

## Uso de IA

| Item | Resposta |
| --- | --- |
| LLM/tool used | Codex |
| Task supported by the LLM | Especificação, implementação, configuração, testes e documentação |
| Main suggestion received | Configuração do SDK, versões do projeto e execução com Gradle e ADB. |
| What the team changed manually | Não houve alterações manuais adicionais; utilizamos o Codex nos ajustes descritos acima. |
| How the result was validated | Gradle, execução no emulador, capturas via ADB |

## Entrega

Branch: `team-09/sprint-00`. [Pull Request #19](https://github.com/brenofeliix/mobile-development-2026-2/pull/19), com destino à `main` do repositório da disciplina.

## Definition of Done

- [x] Ambiente configurado e projeto criado.
- [x] Compilação e execução verificadas.
- [x] Evidências e resultados registrados.
- [x] Uso de IA documentado.
- [x] Branch publicada e PR enviado.
