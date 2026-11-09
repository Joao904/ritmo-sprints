# SPRINT 02 — State & User Interaction

**Equipe 09:** Fernando Henrique Cobianchi e João Victor R. Peres.

## Objetivo e resultado

Adicionamos a escolha entre 15, 25 e 45 minutos. A opção inicial é 25 minutos, e o resumo acompanha a seleção. Apenas uma opção pode ficar selecionada por vez.

## Definição do produto

**Nome:** Ritmo.

**Problema:** Estudantes com tempo limitado podem adiar o estudo por não saberem estruturar uma sessão curta.

**Público:** Estudantes universitários.

**Objetivo:** Ajudar a transformar minutos disponíveis em um plano de estudo.

**Funcionalidades iniciais:** apresentação, escolha de duração e consulta ao plano, introduzidas nas sprints 1, 2 e 3.

## Especificação

[SPEC-002](docs/specs/SPEC-002.md). A seção de rastreabilidade relaciona cada requisito ao código, critério de aceitação e evidência.

## Implementação

`selectedMinutes` é um `Int` criado em `WelcomeScreen` com `rememberSaveable { mutableStateOf(25) }`. O toque em um `FilterChip` chama `onSelect`, que atualiza esse valor. O Compose recompõe o chip selecionado e o resumo. `DurationPicker` recebe o estado e o callback; `rememberSaveable` preserva a escolha durante a recriação da Activity.

## Validação

Compilação e execução aprovadas. Foram executados 3 testes instrumentados, sem falhas. [Resultados por critério](evidence/sprint-02/validation.md), [log de compilação e testes](evidence/sprint-02/build-and-tests.txt) e [resultado JUnit](evidence/sprint-02/instrumented-tests.xml).

![Estado inicial: 25 minutos](evidence/sprint-02/before-interaction.png)

![Estado após selecionar 45 minutos](evidence/sprint-02/after-interaction.png)

## Ajustes e limitações

Com apoio do Codex, adotamos rememberSaveable e testes de seleção repetida e recriação da Activity.

A escolha altera o estado da interface; não há navegação ou armazenamento permanente nesta sprint.

## Uso de IA

| Item | Resposta |
| --- | --- |
| LLM/tool used | Codex |
| Task supported by the LLM | Especificação, implementação, configuração, testes e documentação |
| Main suggestion received | Estado único para a duração, seleção exclusiva e callbacks para atualizar a interface. |
| What the team changed manually | Não houve alterações manuais adicionais; utilizamos o Codex nos ajustes descritos acima. |
| How the result was validated | Gradle, execução no emulador, capturas via ADB e testes instrumentados |

## Entrega

Branch: `team-09/sprint-02`. [Pull Request #21](https://github.com/brenofeliix/mobile-development-2026-2/pull/21), com destino à `main` do repositório da disciplina.

Este incremento inclui o conteúdo das sprints anteriores; a revisão pode seguir a ordem dos PRs.

## Definition of Done

- [x] Especificação e funcionalidade implementadas.
- [x] Compilação e execução verificadas.
- [x] Evidências e resultados registrados.
- [x] Uso de IA documentado.
- [x] Branch publicada e PR enviado.
