<p align="center">
  <img src="docs/banner.png" alt="Lazarus Scarcity: sobrevivente procura os últimos suprimentos em uma cidade abandonada" width="100%">
</p>

<p align="center">
  <img alt="Minecraft 1.20.1" src="https://img.shields.io/badge/Minecraft-1.20.1-3c8527">
  <img alt="Forge 47.4.10" src="https://img.shields.io/badge/Forge-47.4.10-dd7d2b">
  <img alt="Java 17" src="https://img.shields.io/badge/Java-17-b07219">
  <img alt="Versão 0.1.0" src="https://img.shields.io/badge/vers%C3%A3o-0.1.0-6b5b3e">
</p>

# Lazarus Scarcity

**Lazarus Scarcity** é um mod para Minecraft Forge 1.20.1 criado para o modpack **Project Lazarus**. Conforme os dias passam, encontrar suprimentos em baús e pilhas de saque fica mais difícil. A escassez afeta comida, medicamentos, munição, equipamentos e outros itens encontrados durante a exploração.

O dia considerado é o maior já alcançado pelo mundo. Dormir avança a escassez; voltar o relógio não devolve os recursos. Uma pilha de saque desaparece definitivamente após ser vasculhada, mesmo quando a busca não rende nada.

---

## Sumário

- [Como funciona](#como-funciona)
- [Progressão](#progressão)
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

## Integrações e mundos existentes

- **LootPiles 1.1.4:** pilhas vasculhadas desaparecem permanentemente. Pilhas antigas já marcadas como esgotadas são removidas quando o chunk carrega. Se o cooldown antigo já terminou antes da atualização, a pilha não pode ser identificada como antiga e desaparecerá na próxima busca.
- **CityWorld 6.3.0:** a comida que o gerador coloca diretamente nas cozinhas segue a chance dos locais comuns.
- **Outros mods:** tabelas de baús e contêineres dos mods instalados entram na mesma progressão, incluindo tabelas utilizadas pelo Lazarus Events.

Em mundos existentes, baús cuja tabela ainda não foi sorteada recebem a escassez na primeira abertura. Baús já preenchidos permanecem intactos, pois o mod não consegue distinguir o saque antigo dos itens guardados pelo jogador. Novas regiões geradas após a instalação seguem a progressão normalmente.

---

## Configuração e teste

Após iniciar o jogo uma vez, edite `config/lazarusscarcity-common.toml` para alterar os dias de transição (`secondStageDay`, `thirdStageDay`, `fourthStageDay`) e as chances `commonChanceStage0` a `commonChanceStage3` e `riskyChanceStage0` a `riskyChanceStage3`.

Use `/lazarusscarcity status` para ver o dia persistido, a fase atual e as chances dos dois tipos de local. Em uma **cópia de teste** do mundo, avance até o dia 30 com `/time set 30d`, abra locais ainda não saqueados e vasculhe uma pilha. Ela deve desaparecer imediatamente. Depois use `/time set day`: a fase não deve regredir.

---

## Instalação

### Requisitos

- **Minecraft 1.20.1**
- **Minecraft Forge 47.4.10** ou superior na linha 47.x
- **Java 17**
- **LootPiles 1.1.4** para a integração com pilhas de saque
- **CityWorld 6.3.0** para a integração com cozinhas

### Jogadores e servidores

1. Baixe [`lazarusscarcity-0.1.0.jar`](dist/lazarusscarcity-0.1.0.jar).
2. Coloque o JAR na pasta `mods` do perfil e do servidor. Instale LootPiles e CityWorld para usar suas respectivas integrações.
3. Remova versões antigas de `lazarusscarcity` antes de iniciar o jogo.

### Compilar a partir do código

Com JDK 17 instalado, coloque os JARs **LootPiles 1.1.4** e **CityWorld 6.3.0** na pasta `libs/` e execute, na raiz do projeto:

```powershell
.\gradlew.bat build
```

O resultado estará em `build/libs/`. Os JARs de terceiros são necessários para compilar e testar as integrações, mas não são distribuídos neste repositório.

---

## Créditos e licença

- Desenvolvido por **Kaique Melo** para o **Project Lazarus**.
- Integrações com **LootPiles** e **CityWorld**; os respectivos mods pertencem a seus autores.
- Banner: arte gerada com IA para o Lazarus Scarcity.
- Não é afiliado à Mojang ou à Microsoft.

**Licença do código:** MIT. Veja [LICENSE](LICENSE).
