# wiki-processor

用于把从 Wikipedia 拉取的游戏平台游戏列表 HTML 表格转换为后续程序更容易处理的 JSON 文本。

## 目标

- 输入：平台目录下的 `*_wiki.txt`，内容通常是 Wikipedia 的 `<table>` HTML 片段。
- 输出：同目录下的 `*_wiki.json`，内容是 JSON 数组。
- 一个游戏对应数组中的一个对象，顺序必须与 wiki 表格中的游戏行顺序一致。
- 每个游戏必须包含：
  - `id`：从 `1` 开始递增。
  - `titles`：游戏标题数组。
  - `developers`：开发商数组。
  - `publishers`：发行商数组。
  - `releaseAreas`：发行地区数组。
  - `releaseDates`：发行日期数组。
- `releaseAreas` 与 `releaseDates` 必须一一对应。
- 地区值统一使用：`us`、`jp`、`pal`。Wikipedia 表头中的 `NA` 需要归一为 `us`。
  - `pal`：PAL 制式发行版本，地区包括欧洲大部分地区、澳大利亚、新西兰以及亚洲部分地区。
- `releaseDates` 使用标准化格式：完整日期为 `YYYY-MM-DD`，仅年月为 `YYYY-MM`，仅年份为 `YYYY`。

## 脚本

脚本位于：`.claude/skills/wiki-processor/wiki_processor_nes.py`。

脚本使用 Python 标准库实现，不依赖第三方包。不同平台通过列配置适配，避免把 NES 的列结构写死到解析逻辑里。

## NES 默认转换

NES wiki 表格列结构为：

| 列索引 | 含义 |
| --- | --- |
| `0` | Title(s) |
| `1` | Developer(s) |
| `2` | Publisher(s) |
| `3` | First released，当前 JSON 不输出 |
| `4` | JP release date |
| `5` | NA release date，输出为 `us` |
| `6` | PAL release date |

运行：

```bash
cd /Users/jzhang52/personal/retroboy
python3 .claude/skills/wiki-processor/wiki_processor_nes.py ai/nes/nes_wiki.txt --output ai/nes/nes_wiki.json
```

等价显式参数：

```bash
python3 .claude/skills/wiki-processor/wiki_processor_nes.py \
  ai/nes/nes_wiki.txt \
  --output ai/nes/nes_wiki.json \
  --title-col 0 \
  --developers-col 1 \
  --publishers-col 2 \
  --areas jp:4,us:5,pal:6
```

## 新平台转换方式

如果后续加入其他平台，优先复用 `.claude/skills/wiki-processor/wiki_processor_nes.py` 的解析逻辑，仅根据该平台 Wikipedia 表格调整参数：

- `--title-col`：标题列，从 `0` 开始。
- `--developers-col`：开发商列。
- `--publishers-col`：发行商列。
- `--areas`：发行地区列，格式为 `area:index`，多个用英文逗号分隔；例如 `jp:3,us:4,pal:5`。

如果 Wikipedia 表格使用 `NA`，输出时仍应写成 `us`。

## 输出示例

```json
[
  {
  "id": 1,
	"titles": ["'89 Dennō Kyūsei Uranai"],
	"developers": ["Micronics"],
	"publishers": ["Jingukan Polaris"],
	"releaseAreas": ["jp"],
  "releaseDates": ["1988-12-10"]
  }
]
```

## 解析规则

- 忽略表头行，只处理具有足够列数且标题非空的游戏数据行。
- `<br>` 分隔的标题、开发商、发行商会转换为数组项。
- `<sup>` 中的脚注或区域标记不进入文本字段。
- 发行日期优先取日期 `<span>` 中的文本，以避免脚注污染。
- `Unreleased`、`Cancelled`、`N/A`、空值等不会写入 `releaseAreas` / `releaseDates`。
- 将日期从 Wikipedia 英文文本标准化，例如 `October 18, 1985` -> `1985-10-18`，`September 1987` -> `1987-09`，`1992` -> `1992`。
- 遇到不支持的发行日期格式时应报错，不应静默写入非标准日期。

## 转换后校验

生成 JSON 后至少检查：

1. 文件可以被 `json.loads` 正常读取。
2. 所有对象字段集合等于：`id`、`titles`、`developers`、`publishers`、`releaseAreas`、`releaseDates`。
3. `id` 从 `1` 开始连续递增。
4. `releaseAreas.length == releaseDates.length`。
5. 所有 `releaseAreas` 只包含 `us`、`jp`、`pal`。
6. `titles` 非空。

可用以下命令快速校验 NES 输出：

```bash
cd /Users/jzhang52/personal/retroboy
python3 - <<'PY'
import json
from pathlib import Path

data = json.loads(Path('ai/nes/nes_wiki.json').read_text(encoding='utf-8'))
required = {'id', 'titles', 'developers', 'publishers', 'releaseAreas', 'releaseDates'}

for i, item in enumerate(data, 1):
	assert set(item) == required, (i, item.keys())
  assert item['id'] == i, (i, item['id'])
	assert item['titles'], (i, item)
	assert len(item['releaseAreas']) == len(item['releaseDates']), (i, item)
	assert all(area in {'us', 'jp', 'pal'} for area in item['releaseAreas']), (i, item)
    assert all(__import__('re').fullmatch(r'\d{4}(-\d{2}(-\d{2})?)?', date) for date in item['releaseDates']), (i, item)

print(f'OK: {len(data)} games')
PY
```

