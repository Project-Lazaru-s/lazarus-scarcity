<p align="center">
  <img src="docs/banner.png" alt="Lazarus Scarcity: sobrevivente procura os últimos suprimentos em uma cidade abandonada" width="100%">
</p>

<p align="center">
  <img alt="Minecraft 1.20.1" src="https://img.shields.io/badge/Minecraft-1.20.1-3c8527">
  <img alt="Forge 47.4.10" src="https://img.shields.io/badge/Forge-47.4.10-dd7d2b">
  <img alt="Java 17" src="https://img.shields.io/badge/Java-17-b07219">
  <img alt="Versão 0.2.0" src="https://img.shields.io/badge/vers%C3%A3o-0.2.0-6b5b3e">
</p>

# Lazarus Scarcity

**Lazarus Scarcity** é um mod para Minecraft Forge 1.20.1 criado para o modpack **Project Lazarus**. Conforme os dias passam, encontrar suprimentos em baús e pilhas de saque fica mais difícil. A escassez afeta comida, medicamentos, munição, equipamentos e outros itens encontrados durante a exploração.

O dia considerado é o maior já alcançado pelo mundo. Dormir avança a escassez; voltar o relógio não devolve os recursos. Uma pilha de saque desaparece definitivamente após ser vasculhada, mesmo quando a busca não rende nada.

Com **Locks Reforged**, baús de exploração podem aparecer trancados também nas cidades do CityWorld. A fechadura usa o minijogo e as gazuas do Locks; baús trancados preservam pelo menos um item quando a tabela original gera loot.

---

## Sumário

- [Como funciona](#como-funciona)
- [Progressão](#progressão)
- [Baús trancados](#baús-trancados)
- [Integrações e mundos existentes](#integrações-e-mundos-existentes)
- [Configuração e teste](#configuração-e-teste)
- [Instalação](#instalação)
- [Créditos e licença](#créditos-e-licença)

---

## Como funciona

O mod avalia cada local de saque quando seu conteúdo é sorteado. Se o local ainda tiver suprimentos, também reduz a variedade e a quantidade dos itens conforme a fase do mundo. Locais militares, médicos e outros identificados como arriscados preservam uma chance maior de recompensa.

São afetadas as tabelas de exploração `chests/` e `containers/`, inclusive de outros mods, além das pilhas do **LootPiles** e da comida colocada diretamente nas cozinhas do **CityWorld**. Recompensas entregues diretamente por missões, inventários do jogador, mineração, cultivo e drops de combate não são alterados. Um evento que usa uma tabela de baú para entregar sua recompensa pode ser afetado.

As pilhas do LootPiles são de uso único: depois da busca, o bloco some e não volta após o antigo período de espera.

---

## Progressão

| Dia do mundo | Local comum | Local arriscado | Máximo de tipos por busca bem-sucedida |
|---|---:|---:|---:|
| 0–6 | 100% | 100% | Tabela original |
| 7–14 | 72% | 86% | 3 |
| 15–29 | 42% | 62% | 2 |
| 30+ | 16% | 36% | 1 |

As porcentagens são chances **por local**, não por item. Uma busca bem-sucedida ainda passa pela redução de itens e quantidades. Esses são os valores iniciais; os dias de transição e as chances podem ser ajustados na configuração.

---

## Baús trancados

O **Locks Reforged** tenta gerar fechaduras em baús com uma tabela de loot ainda pendente. O CityWorld não executa a feature padrão do Locks na maioria dos lotes urbanos; por isso, o Lazarus Scarcity avalia esses baús depois que o chunk termina de carregar, usando a própria API do Locks.

- A chance vem de `Lock Generation Chance` em `config/locks-common.toml` (18% no perfil Lazarus). O valor do loot determina o nível da fechadura.
- O Lazarus usa `Loot Value Tiers = [1.0, 3.0, 8.0]` para madeira, cobre e ferro. Os limites antigos começavam em 5.0 e excluíam a tabela comum `cityworld:chests/building`, avaliada em 1.0 pelo Locks.
- O baú é avaliado uma vez. Arrombar ou remover a fechadura não faz outra surgir; baús comuns colocados pelo jogador não recebem fechadura.
- Em mundos existentes, baús de exploração ainda não abertos podem ser avaliados quando seus chunks carregarem. Baús já abertos ficam intocados.
- Se a tabela original produzir itens, a escassez preserva pelo menos um deles num baú que recebeu fechadura. Tabelas originalmente vazias continuam vazias.

A integração pode ser desligada em `config/lazarusscarcity-common.toml` com `lockGeneratedChests = false`. A feature original do Locks continua independente para estruturas onde ela já funciona.

---

## Integrações e mundos existentes

- **LootPiles 1.1.4:** pilhas vasculhadas desaparecem permanentemente. Pilhas antigas já marcadas como esgotadas são removidas quando o chunk carrega. Se o cooldown antigo já terminou antes da atualização, a pilha não pode ser identificada como antiga e desaparecerá na próxima busca.
- **CityWorld 6.3.0:** a comida que o gerador coloca diretamente nas cozinhas segue a chance dos locais comuns.
- **Outros mods:** tabelas de baús e contêineres dos mods instalados entram na mesma progressão, incluindo tabelas utilizadas pelo Lazarus Events.
- **Locks Reforged 1.7.5:** usa as fechaduras, níveis e gazuas do mod para baús de exploração gerados. Não tranca barris nem caixas de outros tipos nesta versão.

Em mundos existentes, baús cuja tabela ainda não foi sorteada recebem a escassez na primeira abertura. Baús já preenchidos permanecem intactos, pois o mod não consegue distinguir o saque antigo dos itens guardados pelo jogador. Novas regiões geradas após a instalação seguem a progressão normalmente.

---

## Configuração e teste

Após iniciar o jogo uma vez, edite `config/lazarusscarcity-common.toml` para alterar os dias de transição (`secondStageDay`, `thirdStageDay`, `fourthStageDay`) e as chances `commonChanceStage0` a `commonChanceStage3` e `riskyChanceStage0` a `riskyChanceStage3`.

Use `/lazarusscarcity status` para ver o dia persistido, a fase atual e as chances dos dois tipos de local. Em uma **cópia de teste** do mundo, avance até o dia 30 com `/time set 30d`, abra locais ainda não saqueados e vasculhe uma pilha. Ela deve desaparecer imediatamente. Depois use `/time set day`: a fase não deve regredir.

Use `/lazarusscarcity locks status` para contar baús com loot pendente, baús trancados e baús já avaliados nos chunks carregados próximos. Com permissão de operador, `/lazarusscarcity locks scan` avalia imediatamente o chunk atual. O processamento normal acontece automaticamente no carregamento dos chunks.

---

## Instalação

### Requisitos

- **Minecraft 1.20.1**
- **Minecraft Forge 47.4.10** ou superior na linha 47.x
- **Java 17**
- **LootPiles 1.1.4** para a integração com pilhas de saque
- **CityWorld 6.3.0** para a integração com cozinhas
- **Locks Reforged 1.7.5** para a integração com baús trancados

### Jogadores e servidores

1. Baixe [`lazarusscarcity-0.2.0.jar`](dist/lazarusscarcity-0.2.0.jar).
2. Coloque o JAR na pasta `mods` do perfil e do servidor. Instale LootPiles, CityWorld e Locks Reforged para usar suas respectivas integrações.
3. Remova versões antigas de `lazarusscarcity` antes de iniciar o jogo.

### Compilar a partir do código

Com JDK 17 instalado, coloque os JARs **LootPiles 1.1.4**, **CityWorld 6.3.0** e **Locks Reforged 1.7.5** na pasta `libs/` e execute, na raiz do projeto:

```powershell
.\gradlew.bat build
```

O resultado estará em `build/libs/`. Os JARs de terceiros são necessários para compilar e testar as integrações, mas não são distribuídos neste repositório.

---

## Créditos e licença

- Desenvolvido por **Kaique Melo** para o **Project Lazarus**.
- Integrações com **LootPiles**, **CityWorld** e **Locks Reforged**; os respectivos mods pertencem a seus autores.
- Banner: arte gerada com IA para o Lazarus Scarcity.
- Não é afiliado à Mojang ou à Microsoft.

**Licença do código:** MIT. Veja [LICENSE](LICENSE).
