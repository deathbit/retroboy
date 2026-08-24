# ss-processor

`ss` 是 ScreenScraper 的缩写。本 skill 用于根据 ScreenScraper 导出的游戏列表 CSV，逐个拉取游戏详情 JSON，并保存为本地文件。

## 目标

- 输入：平台目录下的 ScreenScraper CSV，例如 `ai/nes/nes_ss.csv`。
- 输出：平台目录下的 `games` 目录，例如 `ai/nes/games/`。
- 聚合输出：平台目录下的 `*_ss.json`，例如 `ai/nes/nes_ss.json`。
- 每个 ScreenScraper `gameId` 保存为一个 JSON 文件：`{gameId}.json`。
- 如果 `{gameId}.json` 已存在，默认跳过该记录，不再调用接口。
- 每次运行都会重新生成执行报告，例如 `ai/nes/nes_ss_report.txt`。
- 报告一行对应一个游戏，记录该游戏是 dry-run、跳过已有文件、下载成功还是失败。
- 对已有 JSON 和新下载 JSON 都要校验文件结构：必须是 JSON 对象，必须包含 `id` 字段，且 `id` 与当前 `gameId` 一致。
- 每个文件只保存接口返回结果中的 `response.jeu` 字段，不保存 `header`、`serveurs`、`ssuser` 等外层信息。
- 请求必须单线程、按 CSV 顺序逐个执行。
- 任意请求或解析失败时必须 fail-fast：立即停止，不继续处理后续 `gameId`。

## NES 输入格式

`ai/nes/nes_ss.csv` 使用分号分隔，字段带双引号：

```csv
"Game ID";"Game Name"
"559736";" Mr. Dragon's Short Quest"
"18551";"'89 Denn Kysei Uranai"
"420831";"0-to-X"
"1658";"10-yard Fight"
```

其中 `Game ID` 是关键字段，脚本会按 CSV 行顺序读取并处理。

## 脚本

脚本位于：`.claude/skills/ss-processor/ss_processor.py`。

脚本使用 Python 标准库实现，不依赖第三方包。脚本是通用的：默认会根据输入 CSV 推导输出目录和报告路径。

例如输入为 `ai/nes/nes_ss.csv` 时：

- JSON 输出目录默认为 `ai/nes/games/`
- 执行报告默认为 `ai/nes/nes_ss_report.txt`
- 聚合 JSON 默认为 `ai/nes/nes_ss.json`

## API

调用 ScreenScraper `jeuInfos.php` 接口，固定查询参数保持不变，只替换最后的 `gameid` 参数。

请求方式：`GET`

关键规则：

- `output=json`
- 每次只请求一个 `gameid`
- 请求顺序与 CSV 中 `Game ID` 顺序一致
- 响应中 `header.success` 必须为 `true`
- 响应中必须存在 `response.jeu`
- `response.jeu.id` 必须与当前请求的 `gameid` 一致

## 输出示例

如果请求 `gameid=18551`，输出文件为：

```text
ai/nes/games/18551.json
```

文件内容只包含 `response.jeu`，形如：

```json
{
  "id": "18551",
  "notgame": "false"
}
```

实际文件会保留接口返回的 `jeu` 对象完整内容。

## 运行

在仓库根目录执行：

```bash
cd /Users/jzhang52/personal/retroboy
python3 .claude/skills/ss-processor/ss_processor.py
```

等价显式参数：

```bash
python3 .claude/skills/ss-processor/ss_processor.py \
  --input ai/nes/nes_ss.csv \
  --output-dir ai/nes/games \
  --report ai/nes/nes_ss_report.txt \
  --aggregate-output ai/nes/nes_ss.json
```

## 验证 CSV，不调用 API

```bash
cd /Users/jzhang52/personal/retroboy
python3 .claude/skills/ss-processor/ss_processor.py --dry-run
```

## 小批量试跑

只处理前 1 个 `gameId`：

```bash
cd /Users/jzhang52/personal/retroboy
python3 .claude/skills/ss-processor/ss_processor.py --limit 1
```

如果本机 Python 出现系统证书链校验异常，可仅在本地调试时显式跳过 TLS 校验：

```bash
cd /Users/jzhang52/personal/retroboy
python3 .claude/skills/ss-processor/ss_processor.py --limit 1 --insecure-skip-tls-verify
```

正常全量执行仍应优先使用默认 TLS 校验。

脚本默认支持断点续跑：已存在的 JSON 文件会直接跳过，不再调用接口。只有需要强制重新请求已有文件时，才使用 `--force-refresh`：

```bash
cd /Users/jzhang52/personal/retroboy
python3 .claude/skills/ss-processor/ss_processor.py --force-refresh
```

## Fail-fast 条件

出现以下任一情况时脚本必须立即失败并返回非零退出码：

1. CSV 表头不是 `Game ID`;`Game Name`。
2. `Game ID` 为空、非数字或重复。
3. HTTP 请求失败或超时。
4. 响应不是合法 JSON。
5. `header.success` 不为 `true`。
6. 缺少 `response.jeu`。
7. `response.jeu.id` 与请求的 `gameid` 不一致。
8. 本地文件写入失败。
9. 已有或新下载的 `{gameId}.json` 不是 JSON 对象、缺少 `id` 字段，或 `id` 与当前 `gameId` 不一致。

## 执行报告

报告文件每次运行都会从头生成。NES 默认报告路径为：`ai/nes/nes_ss_report.txt`。

报告为 tab 分隔文本，字段为：

```text
index	gameId	status	jsonPath	message
```

常见 `status`：

- `DRY_RUN`：只校验 CSV，不调用 API。
- `SKIP_EXISTING_VALID`：本地 JSON 已存在，已校验 `id` 字段并跳过接口调用。
- `DOWNLOAD_OK`：调用接口成功，已写入 JSON，并已校验 `id` 字段。
- `FAIL_EXISTING_INVALID`：已有 JSON 校验失败，脚本 fail-fast。
- `FAIL`：下载、解析、写入或校验失败，脚本 fail-fast。

## 聚合 JSON

所有游戏文件下载并校验完成后，脚本会生成 `*_ss.json`。NES 默认输出为 `ai/nes/nes_ss.json`。

聚合 JSON 是一个列表，字段格式与 `ai/nes/nes_wiki.json` 一致：

- `id`：使用 ScreenScraper `gameId`，类型为整数。
- `titles`：来自游戏 JSON 的 `noms[].text`，去重；不包含 `region == "ss"` 的名称。
- `developers`：来自 `developpeur.text`，输出为数组；缺失时为空数组。
- `publishers`：来自 `editeur.text`，输出为数组；缺失时为空数组。
- `releaseAreas`：来自 `noms[].region`，排除 `ss`，去重并保留原始顺序。
- `releaseDates`：来自 `dates[].text`，去重并保留原始顺序。

如果某个游戏的 `noms` 中只有 `ss` 地区，说明没有真实发售地区名称，该游戏不会写入 `*_ss.json`。

## 转换后校验

全量执行后，`ai/nes/games` 下的 JSON 文件数量应与 `ai/nes/nes_ss.csv` 中的游戏数量一致。

可用以下命令快速校验：

```bash
cd /Users/jzhang52/personal/retroboy
python3 - <<'PY'
import csv
import json
from pathlib import Path

csv_path = Path('ai/nes/nes_ss.csv')
games_dir = Path('ai/nes/games')

with csv_path.open(encoding='utf-8-sig', newline='') as f:
    ids = [row['Game ID'].strip() for row in csv.DictReader(f, delimiter=';')]

missing = [game_id for game_id in ids if not (games_dir / f'{game_id}.json').is_file()]
assert not missing, missing[:10]

for game_id in ids:
    data = json.loads((games_dir / f'{game_id}.json').read_text(encoding='utf-8'))
    assert isinstance(data, dict), game_id
    assert str(data.get('id')) == game_id, game_id

print(f'OK: {len(ids)} games')
PY
```


