# wiki-db-matcher

用于把各平台 `{platform}_wiki.json` 中的 Wiki 条目与 `{platform}_db.json` 中的 DB/ROM 条目做一一匹配，并输出 `match`、`mismatch`、`extra` 三段 JSON。

本 skill 的目标不是简单字符串相等，而是让 AI agent 基于候选、上下文、别名、罗马音差异、地区差异和游戏标题变体做最终判断。脚本只负责候选生成、已确认映射的应用和一一对应校验。

## 使用要求

- 用户必须指定平台，例如 `nes`。
- 默认输入：
  - Wiki：`src/main/resources/platform/{platform}/{platform}_wiki.json`
  - DB：`src/main/resources/platform/{platform}/{platform}_db.json`
- 默认输出：`src/main/resources/platform/{platform}/{platform}_wiki_db_match.json`
- AI 确认过的特殊别名/变体映射应保存为 overrides 文件：`src/main/resources/platform/{platform}/{platform}_wiki_db_overrides.json`。
- 候选报告默认输出到：`src/main/resources/platform/{platform}/{platform}_wiki_db_candidates.json`。
- 通用脚本：`scripts/wiki_db_matcher.py`。
- 不同平台的地区 key 可能不同，调用时必须显式处理地区映射。例如 NES Wiki 使用 `PAL`，DB 侧可能使用 `EUR` 和少量 `AUS`。
- DB JSON 中的条目可能包含别名信息，格式为 `rom name | alias`，例如 `JJ (Japan) (En) (Famicom 3D System) | Tobidase Daisakusen Part II`。匹配时应把 `rom name` 和 `alias` 都作为候选标题参与判断，但输出和唯一性校验仍以完整 DB 原始字符串为准。

## 输出格式

输出 JSON 固定为：

```json
{
  "match": {
    "JPN": [
      "'89 Dennō Kyūsei Uranai | '89 Dennou Kyuusei Uranai (Japan)"
    ]
  },
  "mismatch": {
    "JPN": [
      "10-Yard Fight"
    ]
  },
  "extra": {
    "JPN": [
      "10-Yard Fight (Japan) (En)"
    ]
  }
}
```

含义：

- `match`：Wiki 条目和 DB 条目已经匹配，格式为 `wiki title | db title`。
- `mismatch`：Wiki 中尚未匹配到唯一 DB 记录的条目。
- `extra`：DB 中尚未被任何 Wiki 条目使用的记录。

## AI agent 使用流程

当用户要求运行本 skill 时，AI agent 应按以下流程执行：

1. 确认平台名，例如 `nes`。
2. 定位输入文件：
   - `src/main/resources/platform/{platform}/{platform}_wiki.json`
   - `src/main/resources/platform/{platform}/{platform}_db.json`
3. 检查两份 JSON 的地区 key 和每个地区的数量；如果 key 不同，先制定地区映射。
4. 运行 `scripts/wiki_db_matcher.py` 生成初版匹配结果和候选报告。
5. 阅读 `mismatch`、`extra` 和 candidates 中分数靠前的候选，使用 AI 能力判断：
   - 标题翻译差异，例如日版标题和英文标题；
   - 罗马音差异，例如 `ō/ou/oo/o`、`ū/uu/u`；
   - No-Intro 命名差异，例如地区、语言、修订版、排序冠词 `, The`；
   - 副标题、系列名、简称和重命名；
   - DB 条目中的 `rom name | alias` 别名信息，例如 Wiki 的 `JJ: Tobidase Daisakusen Part II` 可以借助 DB 别名 `Tobidase Daisakusen Part II` 判断；
   - 同名但不同游戏、续作编号或平台变体，不能误配。
6. 把 AI 确认的映射写入 overrides JSON，格式为：

```json
{
  "JPN": {
    "Takahashi Meijin no Bōken Jima": "Takahashi Meijin no Bouken Shima (Japan)"
  },
  "USA": {
    "Indiana Jones and the Last Crusade": [
      "Indiana Jones and the Last Crusade (USA)",
      "Indiana Jones and the Last Crusade - The Action Game (USA)"
    ]
  },
  "PAL": {
    "The Legend of Zelda": "Legend of Zelda, The (Europe)"
  }
}
```

如果同一个 Wiki 标题在同一地区出现多次，override 的值可以写成 DB 标题数组，脚本会按 Wiki 中出现顺序依次匹配。

如果 DB JSON 条目已经升级为 `rom name | alias`，override 中仍可以只写 `rom name`；脚本应在同地区 DB 中把该主名解析到唯一的完整 DB 原始字符串。若主名无法唯一解析，必须报错而不是猜测。

7. 再次运行脚本，应用 overrides 并重新校验一一对应。
8. 重复第 5-7 步，直到达到任务目标：
   - 如果用户要求“全部一一对应”，则 `mismatch` 和 `extra` 都应为空；
   - 如果源数据本身数量或范围不一致，则保留无法确认的 `mismatch`/`extra`，并说明原因。
9. 最后校验输出 JSON 可解析，并核对：
   - 同一地区中，Wiki 原始记录一条也不能少，必须全部出现在 `match` 或 `mismatch` 中；
   - 同一地区中，DB 记录最多只能被用于匹配一次；
   - `match + mismatch == wiki`，按 Wiki 原始记录逐条覆盖；如果 Wiki 源数据存在同名标题，应按条目出现次数做多重集合校验，不能因为标题相同而合并；
   - `matched DB + extra == db`，按 DB 原始记录逐条覆盖；
   - `matched DB` 中不能出现重复记录。

## 运行方式

以下以 `nes` 为例。NES 的 Wiki 地区 key 为 `JPN/USA/PAL`，DB 地区 key 可能为 `JPN/EUR/USA/AUS`，因此通常需要把 `PAL` 映射到 `EUR,AUS`。

```bash
cd /Users/jzhang52/personal/retroboy
python3 scripts/wiki_db_matcher.py \
  --platform nes \
  --area-map PAL=EUR,AUS \
  --overrides src/main/resources/platform/nes/nes_wiki_db_overrides.json \
  --candidates src/main/resources/platform/nes/nes_wiki_db_candidates.json \
  --output src/main/resources/platform/nes/nes_wiki_db_match.json
```

如果只是生成初版结果，可以省略 `--overrides`：

```bash
python3 scripts/wiki_db_matcher.py \
  --platform nes \
  --area-map PAL=EUR,AUS \
  --candidates src/main/resources/platform/nes/nes_wiki_db_candidates.json
```

## 匹配原则

- 以 `{platform}_wiki.json` 为基准；输出地区顺序和 Wiki 地区一致。
- 同一地区中，Wiki 原始记录一条也不能少：每条 Wiki 记录必须且只能进入 `match` 或 `mismatch`，不能遗漏，也不能因为同名而合并。
- 一个 Wiki 条目最多匹配一个 DB 条目。
- 同一地区中，一个 DB 条目最多只能被一个 Wiki 条目使用；已经进入 `match` 的 DB 条目不得再次匹配其它 Wiki 条目。
- 同一地区中，未被任何 Wiki 使用的 DB 条目必须保留在 `extra` 中。
- 如果 DB 标题包含 `rom name | alias`，匹配候选生成和相似度判断应同时使用主名和别名；但该 DB 条目仍是同一条记录，不能因为有多个别名而被多个 Wiki 条目重复使用。
- 不要因为字符串相似就自动确认；对于低置信度候选，AI agent 必须结合游戏常识、别名和上下文判断。
- 如果 DB 中存在多个版本，例如 `(Rev 1)`、语言标签或地区标签，优先选择与 Wiki 对应地区最合理、且不是 prototype/beta/sample 的正式版本。
- 无法确认时不要强行匹配，应保留在 `mismatch` 和 `extra` 中。

## 扩展其它平台

- 复用 `scripts/wiki_db_matcher.py`。
- 为新平台建立独立 overrides 文件：`src/main/resources/platform/{platform}/{platform}_wiki_db_overrides.json`。
- 如果平台存在特殊地区映射，调用脚本时用 `--area-map` 指定。
- 如果平台存在大量特殊标题规则，优先把最终确认关系写入 overrides，而不是把某个平台的别名写死到通用脚本。


