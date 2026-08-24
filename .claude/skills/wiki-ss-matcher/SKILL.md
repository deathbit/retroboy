# wiki-ss-matcher

用于将 Wikipedia 处理结果与 ScreenScraper 处理结果进行人工/AI 判断式匹配。该 skill 是平台通用的，NES 只是当前示例。

## 目标

- 基准文件：`ai/<platform>/<platform>_wiki.json`，例如 `ai/nes/nes_wiki.json`。
- 候选文件：`ai/<platform>/<platform>_ss.json`，例如 `ai/nes/nes_ss.json`。
- 输出最终匹配文件：`ai/<platform>/<platform>_wiki_ss.json`，例如 `ai/nes/nes_wiki_ss.json`。
- 输出详情文件：`ai/<platform>/<platform>_wiki_ss_detail.json`，例如 `ai/nes/nes_wiki_ss_detail.json`。

## 重要原则

匹配必须由 AI 自身综合判断，不能使用脚本自动判定最终匹配。

可以阅读、筛选、排序、抽样数据来辅助判断，但最终 `wikiId -> gameId` 的确认必须由 AI 根据多字段信息和自身知识综合判断。

判断时需要综合考虑：

- 标题：`titles`
- 开发商：`developers`
- 发行商：`publishers`
- 发售地区：`releaseAreas`
- 发售日期：`releaseDates`
- AI 对游戏别名、地区名、移植版、盗版/非授权版本、拼写差异和罗马字差异的知识

## 输入文件

### `<platform>_wiki.json`

Wikipedia 侧 JSON，是匹配基准。字段：

```json
{
  "id": 1,
  "titles": ["'89 Dennō Kyūsei Uranai"],
  "developers": ["Micronics"],
  "publishers": ["Jingukan Polaris"],
  "releaseAreas": ["jp"],
  "releaseDates": ["1988-12-10"]
}
```

这里的 `id` 是 wiki 侧顺序 ID，从 `1` 开始。

### `<platform>_ss.json`

ScreenScraper 侧聚合 JSON。字段与 wiki 文件一致：

```json
{
  "id": 18551,
  "titles": ["'89 Dennou Kyuusei Uranai"],
  "developers": ["Micronics"],
  "publishers": ["Induction Produce"],
  "releaseAreas": ["jp"],
  "releaseDates": ["1988-12-10"]
}
```

这里的 `id` 是 ScreenScraper `gameId`，必须是整数。

## 最终匹配文件

`<platform>_wiki_ss.json` 是最终匹配结果，只记录 `wikiId -> gameId` 映射。

格式：

```json
[
  {
    "wikiId": 1,
    "gameId": 18551
  }
]
```

规则：

1. `wikiId` 来自 `nes_wiki.json[].id`。
2. `gameId` 来自 `nes_ss.json[].id`。
3. 每个 `wikiId` 有且仅出现一次。
4. `gameId` 可以使用多次。
5. 同一个 `gameId` 可以分配给多个 wiki 游戏。
6. 不确定时不要强行匹配；可以先留空，后续再判断，但最终完成版应覆盖所有 `wikiId`。

## 匹配判断建议

优先级不是绝对的，应综合判断：

1. 标题高度一致或是明确别名/罗马字差异。
2. 发售日期完全一致或非常接近。
3. 发售地区重合。
4. 开发商/发行商一致或符合历史事实。
5. 若标题相同但开发商、发行商、日期、地区明显冲突，需要谨慎，可能是同名不同游戏、盗版、非授权、Homebrew、Prototype 或平台外版本。
6. ScreenScraper 中 `ss` 地区名称已在 `nes_ss.json` 生成时排除，不应作为真实发售地区使用。

## 评分匹配脚本

脚本位于：`.claude/skills/wiki-ss-matcher/wiki_ss_match.py`。脚本是平台通用的，可通过 `--platform` 推导默认路径。

该脚本将评分规则显式写入代码，用来生成：

- `<platform>_wiki_ss.json`：最终 `wikiId -> gameId` 映射。
- `<platform>_wiki_ss_review.json`：每个 wiki 记录的评分详情和 Top 候选，便于复核。

运行：

```bash
cd /Users/jzhang52/personal/retroboy
python3 .claude/skills/wiki-ss-matcher/wiki_ss_match.py --platform nes
```

显式参数：

```bash
python3 .claude/skills/wiki-ss-matcher/wiki_ss_match.py \
  --platform nes \
  --wiki ai/nes/nes_wiki.json \
  --ss ai/nes/nes_ss.json \
  --mapping ai/nes/nes_wiki_ss.json \
  --review ai/nes/nes_wiki_ss_review.json
```

### 评分规则

脚本对每个 wiki 记录和每个 SS 记录计算综合分：

```text
score = titleScore * 60
      + dateScore * 36
      + developerMatch * 12
      + publisherMatch * 12
      + areaMatch * 8
      + exactTitleBonus * 25
```

其中：

- `titleScore`：标题相似度，取 token Jaccard 与规范化字符串相似度的较高值。
- `dateScore`：发售日期相似度，完整日期相同为 `1.0`，年月相同为 `0.82`，年份相同为 `0.50`。
- `developerMatch`：开发商规范化后有交集则为 `1`，否则 `0`。
- `publisherMatch`：发行商规范化后有交集则为 `1`，否则 `0`。
- `areaMatch`：发售地区有交集则为 `1`，否则 `0`。
- `exactTitleBonus`：规范化标题完全一致时加分。

标题规范化会：

- 转 ASCII 小写，去除重音符号。
- 将 `&` 视为 `and`。
- 去除常见冠词 `the`、`a`、`an`。
- 去除日文罗马字中常见但来源不稳定的 `no`。
- 将非字母数字字符视为空格。

脚本会为每个 `wikiId` 选择最高分候选；`gameId` 允许复用。

## 多个 wiki 游戏指向同一个 gameId

如果某个平台的 ScreenScraper 聚合记录把多个 Wikipedia 地区条目或别名条目合并到同一个 `gameId`，允许多个 `wikiId` 指向同一个 `gameId`。

这种情况常见于：

- Wikipedia 将不同地区版本拆成多个条目。
- Wikipedia 将别名/地区名拆成多个条目。
- ScreenScraper 将同一游戏的多地区名称和日期聚合在同一条记录中。

此时应优先保证每个 `wikiId` 都有匹配，而不是强行寻找不同的 `gameId`。

## 详情生成脚本

脚本位于：`.claude/skills/wiki-ss-matcher/wiki_ss_detail.py`。脚本是平台通用的，可通过 `--platform` 推导默认路径。

该脚本不做匹配判断，只做以下事情：

1. 读取 `ai/nes/nes_wiki_ss.json`。
2. 校验 `wikiId` 和 `gameId` 都是整数。
3. 校验 `wikiId` 不重复。
4. 校验 `wikiId` 覆盖所有 wiki 记录。
5. 允许 `gameId` 重复。
6. 校验 `wikiId` 存在于 `ai/nes/nes_wiki.json`。
7. 校验 `gameId` 存在于 `ai/nes/nes_ss.json`。
8. 生成 `ai/nes/nes_wiki_ss_detail.json`。

运行：

```bash
cd /Users/jzhang52/personal/retroboy
python3 .claude/skills/wiki-ss-matcher/wiki_ss_detail.py --platform nes
```

显式参数：

```bash
python3 .claude/skills/wiki-ss-matcher/wiki_ss_detail.py \
  --platform nes \
  --wiki ai/nes/nes_wiki.json \
  --ss ai/nes/nes_ss.json \
  --mapping ai/nes/nes_wiki_ss.json \
  --output ai/nes/nes_wiki_ss_detail.json
```

## 详情输出格式

`ai/nes/nes_wiki_ss_detail.json` 是列表。每个对象包含：

```json
{
  "wikiId": 1,
  "gameId": 18551,
  "wikiRecord": {
    "id": 1,
    "titles": ["'89 Dennō Kyūsei Uranai"],
    "developers": ["Micronics"],
    "publishers": ["Jingukan Polaris"],
    "releaseAreas": ["jp"],
    "releaseDates": ["1988-12-10"]
  },
  "ssRecord": {
    "id": 18551,
    "titles": ["'89 Dennou Kyuusei Uranai"],
    "developers": ["Micronics"],
    "publishers": ["Induction Produce"],
    "releaseAreas": ["jp"],
    "releaseDates": ["1988-12-10"]
  }
}
```

## 推荐工作流

1. 运行 `wiki_ss_match.py` 生成 `nes_wiki_ss.json` 和 `nes_wiki_ss_review.json`。
2. 检查 `nes_wiki_ss_review.json` 中低分或候选接近的条目。
3. 必要时由 AI 综合标题、厂商、地区、日期和游戏知识修正 `nes_wiki_ss.json`。
4. 运行 `wiki_ss_detail.py` 生成详情。
5. 检查 detail 文件中 `wikiRecord` 与 `ssRecord` 的完整字段，发现错误时回到 `nes_wiki_ss.json` 修正映射。

