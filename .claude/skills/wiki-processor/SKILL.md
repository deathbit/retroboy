# wiki-processor

用于把各平台保存下来的 Wiki HTML 资源处理成 Retroboy 使用的地区分组 JSON。

该 skill 描述通用处理能力；具体平台的 HTML 结构、地区定义、字段映射和特殊规则应放在对应平台脚本中。

## 使用要求

- 这是全平台 wiki 处理 skill，不绑定某一个具体平台。
- 使用本 skill 时，用户必须指定要处理的平台，例如 `nes`。
- Python 脚本需要带平台标识，命名格式为 `scripts/wiki_processor_{platform}.py`。
- 默认输入路径：`src/main/resources/platform/{platform}/{platform}_wiki.html`。
- 默认输出路径：`src/main/resources/platform/{platform}/{platform}_wiki.json`。
- 平台支持的地区、Wiki 表格列映射、标题清洗规则、未发售关键字等信息，应由对应平台脚本维护，不应写死在本 skill 文档中。
- 如果同一平台、同一地区内存在同名游戏标题，平台脚本应使用 Wiki 中的发行商信息追加到标题后进行消歧，例如 `Indiana Jones and the Last Crusade` 可输出为 `Indiana Jones and the Last Crusade (Taito)`。

## AI agent 使用流程

当用户要求运行本 skill 时，AI agent 应按以下流程执行：

1. 确认用户指定了平台；如果没有指定平台，需要先让用户补充平台名。
2. 根据平台名定位脚本：`scripts/wiki_processor_{platform}.py`。
3. 确认默认输入文件存在：`src/main/resources/platform/{platform}/{platform}_wiki.html`。
4. 运行对应平台脚本，生成默认输出文件：`src/main/resources/platform/{platform}/{platform}_wiki.json`。
5. 校验生成的 JSON 是否可解析。
6. 如果平台脚本输出了地区数量，应核对脚本输出数量与生成 JSON 中各地区数组长度一致。
7. 如果平台脚本不存在，不要临时把其它平台脚本混用；应为该平台新增独立脚本，并把该平台的地区和 HTML 解析规则写入该脚本。

## 运行方式

以下以 `nes` 平台为例：

```bash
cd /Users/jzhang52/personal/retroboy
python3 scripts/wiki_processor_nes.py
```

也可以显式指定输入和输出文件：

```bash
python3 scripts/wiki_processor_nes.py \
  --input src/main/resources/platform/nes/nes_wiki.html \
  --output src/main/resources/platform/nes/nes_wiki.json
```

处理其它平台时，将平台名替换为对应平台，例如：

```text
scripts/wiki_processor_{platform}.py
src/main/resources/platform/{platform}/{platform}_wiki.html
src/main/resources/platform/{platform}/{platform}_wiki.json
```

## 输出格式

输出 JSON 与平台已有的 `{platform}_db.json` 类似，按地区聚合游戏名称。地区 key 由平台脚本决定，例如：

```json
{
  "AREA_1": [],
  "AREA_2": []
}
```

## 扩展其它平台

不同平台 Wiki HTML 结构不一定相同，因此每个平台都应新增独立脚本：

```text
scripts/wiki_processor_{platform}.py
```

新增平台脚本时，应至少在脚本中维护：

- 平台支持的地区列表和输出顺序。
- HTML 输入表格的定位方式。
- 标题字段解析规则。
- 同地区同名标题的发行商消歧规则。
- 地区发行状态判断规则。
- 未发售、取消发售等关键字或 class 判断规则。
- 默认输入/输出路径。
