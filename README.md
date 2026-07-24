# XyMythicItemGui

桥接 MythicMobs 的物品/怪物 GUI 插件。插件通过 MythicMobs API 读取已加载的 MM 物品和怪物配置，并提供可视化 GUI、搜索、领取、怪物蛋图鉴和可控的物品模板更新能力。

## 常用命令

```text
/xygui open                         打开总 GUI
/xygui i open [页码]                 打开 MythicMobs 物品 GUI
/xygui i search <关键词>             搜索 MythicMobs 物品
/xygui e open [页码]                 打开 MythicMobs 怪物蛋 GUI
/xygui give <玩家> <物品名> [数量]    直接给予 MM 物品
/xygui reload                       重载 MythicMobs 并同步刷新 XyGui
/xygui update check <玩家>           预览玩家身上可更新的 MM 物品数量
/xygui update scan <玩家>            立即更新玩家身上的 MM 物品
```

## MythicMobs 物品自动更新

自动更新系统用于解决“MM 物品配置改了，但玩家身上的旧物品还是旧属性”的问题。

插件会给通过 XyGui 生成、且启用了更新功能的 MM 物品写入隐藏标记：

```text
xygui_mythic_id
xygui_template_hash
xygui_update_enabled
xygui_update_mode
xygui_mark_version
```

当玩家身上的物品标记 hash 与当前 MythicMobs 物品模板 hash 不一致时，插件会重新生成该 MM 物品并替换旧物品，保留原数量。

### MM 物品配置写法

推荐写法：

```yml
DragonSword:
  Id: DIAMOND_SWORD
  Display: '&c龙血剑'
  Lore:
  - '&7这把剑会跟随 MythicMobs 配置更新'
  XyUpdate:
    Enabled: true
    Mode: replace
```

简写也支持：

```yml
DragonSword:
  Id: DIAMOND_SWORD
  Display: '&c龙血剑'
  XyUpdate: true
```

当前 `Mode` 支持：

```text
replace    完整重新生成 MM 物品并替换旧物品，保留数量
```

> 说明：为了轻量和安全，当前只实现 `replace`。未来如果需要保留强化、宝石、绑定等玩家后期数据，再扩展 `safe` / `meta-only` 模式。

### 触发时机

自动更新不是高频扫描，默认只在这些时机触发：

```text
玩家进服后延迟扫描
/xygui reload 后扫描在线玩家
/mm reload 或 /mythicmobs reload 后扫描在线玩家
管理员手动 /xygui update scan <玩家>
```

### 性能配置

`config.yml` 中的配置：

```yml
item-update:
  enabled: true
  scan-on-join: true
  scan-after-reload: true
  join-delay-ticks: 60
  players-per-tick: 1
  queue-interval-ticks: 1
  notify-player: true
```

说明：

```text
enabled             是否启用更新扫描系统
scan-on-join        玩家进服后是否扫描
scan-after-reload   /xygui reload 或 /mm reload 后是否扫描在线玩家
join-delay-ticks    玩家进服后延迟多少 tick 再扫描
players-per-tick    每次队列处理几个玩家，大服建议保持 1
queue-interval-ticks 队列处理间隔
notify-player       更新玩家物品后是否提示玩家
```

## 更新记录

```text
v1.0  初始版本：AI 和插件帮助下完成基础 GUI。
v1.2  修复背包满了也可以获取物品的问题。
v1.3  修改插件使用权限为 OP/admin 使用。
v2.2  新增语言文件 Message.yml，可自定义给予物品等提示信息。
v2.3  新增 /xygui open 总菜单、物品 GUI、怪物蛋 GUI，并支持 MythicMobs 重载后同步刷新 XyGui。
v2.4  新增可控 MythicMobs 物品模板更新系统，支持 XyUpdate 标记、进服/重载/手动轻量扫描。
```
