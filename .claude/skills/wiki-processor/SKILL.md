# Skill: wiki-processor

## 适用场景

当需要把从 Wikipedia HTML 页面复制出来的平台游戏列表转换为项目可消费的 JSON 时，使用本 skill。

注意：不同游戏平台的 Wikipedia 页面结构可能不同，所以**不要假设所有平台共用同一个解析脚本**。本 skill 只定义统一的处理思路和输出约定；具体解析逻辑应按平台分别实现。

当前已实现：

- `nes`：`.claude/skills/wiki-processor/generate_nes_wiki_json.py`

后续如果处理 `md`、`sfc`、`gba` 等平台，应新增对应脚本，例如：

- `generate_md_wiki_json.py`
- `generate_sfc_wiki_json.py`
- `generate_gba_wiki_json.py`

## 目录约定

```text
.claude/
  skills/
    wiki-processor/
      SKILL.md
      generate_nes_wiki_json.py
      generate_{platform}_wiki_json.py
ai/
  {platform}/
    {platform}_wiki.txt
    {platform}_wiki.json
    {platform}_error.txt
```

例如 NES：

- 输入：`ai/nes/nes_wiki.txt`
- 输出：`ai/nes/nes_wiki.json`
- 错误/诊断信息：`ai/nes/nes_error.txt`，该文件为追加模式
- 脚本：`.claude/skills/wiki-processor/generate_nes_wiki_json.py`

每次运行平台脚本前，本 skill 应先清理该平台上一次生成的 JSON 输出，例如 `ai/nes/nes_wiki.json`。错误/诊断信息文件不清理；脚本只检测文件是否存在，存在则追加记录，不存在则新建后追加记录。

## 输出结构

输出 JSON 直接是按地区分组的游戏发售记录对象。

每个游戏在某个地区的一次发售，作为一条独立记录，并放入对应地区数组。

结构为：

```json
{
  "JPN": [
    {
      "title": "游戏名称",
      "developer": "开发商",
      "publisher": "发行商",
      "releaseDate": "发售日期"
    }
  ],
  "USA": [],
  "PAL": []
}
```

字段名固定为：

- `title`：游戏名称；如果同一地区有多个标题，用 ` | ` 分隔，竖线两边各一个空格
- `developer`：开发商
- `publisher`：发行商
- `releaseDate`：发售日期

如果某个字段取不到信息，值置为 `null`。

标题规则：

- `JPN` 和 `USA` 每条记录的 `title` 只允许包含 1 个标题。
- `PAL` 每条记录的 `title` 允许包含多个标题，用来容纳 `PAL`、`FR`、`ESP` 等 PAL 区域本地化标题。
- 多标题使用 ` | ` 分隔，例如：`Adventure Island Classic | Adventure Island in the Pacific`。

示例：

```json
{
  "JPN": [
    {
      "title": "89 Dennō Kyūsei Uranai",
      "developer": "Micronics",
      "publisher": "Jingukan Polaris",
      "releaseDate": "19881210"
    }
  ],
  "USA": [
    {
      "title": "The 3-D Battles of WorldRunner",
      "developer": "Square",
      "publisher": "Acclaim Entertainment",
      "releaseDate": "198709"
    }
  ],
  "PAL": [
    {
      "title": "Adventure Island Classic | Adventure Island in the Pacific",
      "developer": "Hudson Soft",
      "publisher": "Hudson Soft",
      "releaseDate": "1992"
    }
  ]
}
```

## 地区规范

常见 Wikipedia 地区标记映射为：

| Wikipedia | 输出 |
| --- | --- |
| `JP` | `JPN` |
| `NA` | `USA` |
| `PAL` | `PAL` |

如果某个平台出现其他地区标记，应在该平台脚本中单独定义补充映射规则。

## 通用解析规则

平台脚本应根据对应 Wikipedia 页面实际表格结构实现解析逻辑，不要假设不同平台共用相同列结构。

- 从平台页面中识别游戏标题、开发商、发行商、地区发行日期等信息。
- `Unreleased` 不生成记录。
- 每个已发售地区生成一条记录。
- 地区标记应按“地区规范”映射到输出地区；平台特有地区由对应平台脚本补充处理。
- 日期尽量标准化：
  - `December 10, 1988` -> `19881210`
  - `September 1987` -> `198709`
  - `1992` -> `1992`
- 游戏名称优先使用对应地区的地区标题。
  - 如果标题字段中有带地区标记的别名，则该地区使用对应别名。
  - 否则使用第一个标题作为默认游戏名。
  - `PAL` 地区允许同时使用多个标题：如果存在 `PAL`、`FR`、`ESP` 等 PAL 区域标题，则全部写入 `title`，并用 ` | ` 分隔。
  - `JPN` 和 `USA` 仍然只写入一个标题。
- 标题字段中解析出的每个 title 条目都应至少被某个地区记录使用一次。
  - 如果某个 title 条目没有被使用，应作为错误/诊断信息写入 `{platform}_error.txt`。
  - `{platform}_error.txt` 是通用错误/诊断信息文件，不只用于存储未使用标题报告。
  - `{platform}_error.txt` 是追加模式：如果文件不存在则创建；如果文件已存在，只追加新信息，不清空旧内容。
  - 未使用标题记录格式为：`row=<行号> primary=<主标题> unusedTitle=<未使用标题> regions=<地区标记>`。
  - 其他错误/诊断信息可由平台脚本按需要追加，但应保持单行一条记录，便于后续排查。
- 开发商、发行商如果有地区标记，则优先使用对应地区的公司。
  - 例如 `Irem JP`、`Nintendo NA/PAL`，则 JPN 使用 `Irem`，USA/PAL 使用 `Nintendo`。
  - 如果没有对应地区标记，则使用无地区标记的默认公司。

## 使用方法

在仓库根目录执行：

```bash
python3 .claude/skills/wiki-processor/generate_nes_wiki_json.py
```

等价于：

```bash
python3 .claude/skills/wiki-processor/generate_nes_wiki_json.py \
  --input ai/nes/nes_wiki.txt \
  --output ai/nes/nes_wiki.json \
  --error ai/nes/nes_error.txt
```

生成结果：

```text
ai/nes/nes_wiki.json
ai/nes/nes_error.txt
```

## 在 Copilot 中使用该 skill

在 Copilot Chat 中，参数通过自然语言传入即可。推荐写法：

```text
/skill:wiki-processor nes，请读取 ai/nes/nes_wiki.txt，生成 ai/nes/nes_wiki.json，并将错误/诊断信息追加写入 ai/nes/nes_error.txt
```

也可以更简短：

```text
/skill:wiki-processor nes，重新生成 wiki json
```

当收到平台参数 `nes` 时，AI 应使用 NES 专属脚本：

```bash
python3 .claude/skills/wiki-processor/generate_nes_wiki_json.py
```

如果需要指定路径，可以这样传：

```text
/skill:wiki-processor nes input=ai/nes/nes_wiki.txt output=ai/nes/nes_wiki.json error=ai/nes/nes_error.txt
```

对应命令：

```bash
python3 .claude/skills/wiki-processor/generate_nes_wiki_json.py \
  --input ai/nes/nes_wiki.txt \
  --output ai/nes/nes_wiki.json \
  --error ai/nes/nes_error.txt
```

## 后续新增平台的处理方式

如果要处理 `md` 平台，不要直接复用 NES 脚本；应先查看 `ai/md/md_wiki.txt` 的表格结构，然后新增平台专属脚本：

```text
.claude/skills/wiki-processor/generate_md_wiki_json.py
```

平台专属脚本应仍然输出同样的按地区分组结构：

```json
{
  "JPN": [
    {
      "title": "游戏名称",
      "developer": "开发商",
      "publisher": "发行商",
      "releaseDate": "发售日期"
    }
  ],
  "USA": [],
  "PAL": []
}
```

## AI 使用流程

当用户要求使用 `wiki-processor` 处理某个平台时：

1. 确认平台，例如 `nes`。不要要求或使用键名前缀。
2. 找到对应平台脚本，例如 `.claude/skills/wiki-processor/generate_nes_wiki_json.py`。
3. 如果平台脚本不存在，先读取该平台 wiki txt 的样本，再新增平台专属脚本。
4. 运行前清理旧的 `{platform}_wiki.json`。
5. 运行平台脚本生成新的 `{platform}_wiki.json`。
6. `{platform}_error.txt` 不清理；如果文件不存在则创建，如果存在则追加错误/诊断信息，包括但不限于未使用的 title 条目。
7. 校验 JSON 可解析。
8. 抽查几条记录，确认按地区分组，并且每条记录包含 `title`、`developer`、`publisher`、`releaseDate`；PAL 多标题必须使用 ` | ` 分隔。
