# pc-processor

`pc` 是 Parent/Clone 的缩写。本 skill 用于处理 Parent/Clone TXT 文件，生成一个同格式的清理版本。

## 目标

- 输入：平台目录下的 `*_pc.txt`，例如 `ai/nes/nes_pc.txt`。
- 输出：同目录下的 `*_pc_clean.txt`，例如 `ai/nes/nes_pc_clean.txt`。
- 输出必须保持 Parent/Clone TXT 原格式：父游戏行不缩进，Clone 行保留原始缩进，其他文本不做重排或改写。
- 删除所有包含 `(Unl)` 的行记录。
- 删除所有包含 `(Pirate)` 的行记录。
- 不重新选择 parent，不提升 clone，不合并分组；只做逐行过滤。
- 处理后必须校验输出文件中不再包含 `(Unl)` 或 `(Pirate)`。

## Parent/Clone TXT 输入格式

文件按游戏分组排列：

```text
Parent1
	Clone1
	Clone2
Parent2
	Clone1
	Clone2
```

NES 示例：

```text
0003 - 10-Yard Fight (USA, Europe)|(EUR PARENT) (USA PARENT)
	0002 - 10-Yard Fight (Japan) (En)
	2601 - 10-Yard Fight (Japan) (En) (Rev 1)|(JPN PARENT)
```

处理时每一行都是一条记录。只要该行文本包含任一排除标签，就删除整行。

## 脚本

脚本位于：`.claude/skills/pc-processor/pc_processor.py`。

脚本使用 Python 标准库实现，不依赖第三方包。默认会根据输入 TXT 推导输出路径：

- 输入 `ai/nes/nes_pc.txt`
- 输出 `ai/nes/nes_pc_clean.txt`

## NES 默认转换

在仓库根目录执行：

```bash
cd /Users/jzhang52/personal/retroboy
python3 .claude/skills/pc-processor/pc_processor.py
```

等价显式参数：

```bash
python3 .claude/skills/pc-processor/pc_processor.py \
  --input ai/nes/nes_pc.txt \
  --output ai/nes/nes_pc_clean.txt \
  --exclude-tags '(Unl),(Pirate)'
```

## 只查看统计，不写文件

```bash
cd /Users/jzhang52/personal/retroboy
python3 .claude/skills/pc-processor/pc_processor.py --dry-run
```

## 新平台转换方式

如果后续加入其他平台，指定对应输入即可：

```bash
cd /Users/jzhang52/personal/retroboy
python3 .claude/skills/pc-processor/pc_processor.py \
  --input ai/snes/snes_pc.txt \
  --output ai/snes/snes_pc_clean.txt
```

如果需要扩展排除标签，用英文逗号分隔：

```bash
python3 .claude/skills/pc-processor/pc_processor.py \
  --input ai/nes/nes_pc.txt \
  --output ai/nes/nes_pc_clean.txt \
  --exclude-tags '(Unl),(Pirate),(Proto)'
```

## Fail-fast 条件

出现以下任一情况时脚本必须立即失败并返回非零退出码：

1. 输入文件不存在。
2. 输入文件不能按 UTF-8 文本读取。
3. 输出路径与输入路径相同。
4. `--exclude-tags` 为空。
5. 输出文件写入后仍包含任一排除标签。

## 转换后校验

生成文件后至少检查：

1. 输出文件存在且是文本文件。
2. 输出中不包含 `(Unl)`。
3. 输出中不包含 `(Pirate)`。
4. 输出行数等于输入行数减去被删除的行数。

可用以下命令快速校验 NES 输出：

```bash
cd /Users/jzhang52/personal/retroboy
python3 - <<'PY'
from pathlib import Path

input_path = Path('ai/nes/nes_pc.txt')
output_path = Path('ai/nes/nes_pc_clean.txt')
tags = ('(Unl)', '(Pirate)')

input_lines = input_path.read_text(encoding='utf-8-sig').splitlines()
output_lines = output_path.read_text(encoding='utf-8').splitlines()
removed = sum(any(tag in line for tag in tags) for line in input_lines)

assert output_path.is_file(), output_path
assert all(tag not in output_path.read_text(encoding='utf-8') for tag in tags)
assert len(output_lines) == len(input_lines) - removed, (len(input_lines), len(output_lines), removed)

print(f'OK: input={len(input_lines)} output={len(output_lines)} removed={removed}')
PY
```
