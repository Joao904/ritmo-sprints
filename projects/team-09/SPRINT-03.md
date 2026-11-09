# SPRINT 03 — Navigation & Multiple Screens

**Equipe 09:** Fernando Henrique Cobianchi e João Victor R. Peres.

## Objetivo e resultado

Adicionamos a tela “Seu plano”, que apresenta a duração escolhida e orientações para preparar o ambiente, concentrar-se e fazer uma pausa. O retorno à tela inicial preserva a duração selecionada.

## Definição do produto

**Nome:** Ritmo.

**Problema:** Estudantes com tempo limitado podem adiar o estudo por não saberem estruturar uma sessão curta.

**Público:** Estudantes universitários.

**Objetivo:** Ajudar a transformar minutos disponíveis em um plano de estudo.

**Funcionalidades iniciais:** apresentação, escolha de duração e consulta ao plano, introduzidas nas sprints 1, 2 e 3.

## Especificação

[SPEC-003](docs/specs/SPEC-003.md). A seção de rastreabilidade relaciona cada requisito ao código, critério de aceitação e evidência.

## Implementação

`RitmoApp` concentra o estado `selectedMinutes` e cria o `NavController`. O `NavHost` associa as rotas `inicio` e `plano` às telas. A ação principal chama `navigate("plano")` com `launchSingleTop`, evitando cópias consecutivas do destino. “Voltar” e “Ajustar duração” chamam `popBackStack`; o Voltar do Android usa a mesma pilha. Como o estado fica acima do `NavHost`, ambas as telas recebem a mesma duração.

## Validação

Compilação e execução aprovadas. Foram executados 7 testes instrumentados, sem falhas. [Resultados por critério](evidence/sprint-03/validation.md), [log de compilação e testes](evidence/sprint-03/build-and-tests.txt) e [resultado JUnit](evidence/sprint-03/instrumented-tests.xml). O lint terminou com 0 erros e 9 avisos: oito sobre versões de dependências e um sobre configuração de backup. Também foi verificado o acesso às ações por rolagem em uma tela de 360×640 dp.

![Tela inicial com duração selecionada](evidence/sprint-03/screen-a.png)

![Plano de estudo](evidence/sprint-03/screen-b.png)

![Retorno com a duração preservada](evidence/sprint-03/back-preserved.png)

## Ajustes e limitações

Com apoio do Codex, adicionamos a dependência de Espresso aos testes e isolamos windowLightNavigationBar em values-v27 para manter minSdk 24.

O plano apresenta orientações; não executa contagem de tempo nem mantém histórico.

## Uso de IA

| Item | Resposta |
| --- | --- |
| LLM/tool used | Codex |
| Task supported by the LLM | Especificação, implementação, configuração, testes e documentação |
| Main suggestion received | Estado compartilhado acima do NavHost e retorno pela pilha de navegação. |
| What the team changed manually | Não houve alterações manuais adicionais; utilizamos o Codex nos ajustes descritos acima. |
| How the result was validated | Gradle, execução no emulador, capturas via ADB e testes instrumentados |

## Entrega

Branch: `team-09/sprint-03`. [Pull Request #22](https://github.com/brenofeliix/mobile-development-2026-2/pull/22), com destino à `main` do repositório da disciplina.

Este incremento inclui o conteúdo das sprints anteriores; a revisão pode seguir a ordem dos PRs.

## Definition of Done

- [x] Especificação e funcionalidade implementadas.
- [x] Compilação e execução verificadas.
- [x] Evidências e resultados registrados.
- [x] Uso de IA documentado.
- [x] Branch publicada e PR enviado.
