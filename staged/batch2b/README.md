# opticzcraft Batch 2b 暂存目录 —— 已清空（保留本说明）

本目录曾暂存前一个失败代理写到一半的两个文件（`entity/EntityThrow.java`、`event/EventsHandler.java`）。
**Batch 2b 已完成**，两个文件都已移回正式源集，`cn/**` 子树已删除，只留下本说明。

## 恢复结果（本目录现在为空的原因）
| 暂存文件 | 去向 | 处理 |
|---|---|---|
| `cn/ac/nya/opticzcraft/event/EventsHandler.java` | `src/main/java/cn/ac/nya/opticzcraft/event/EventsHandler.java` | **原样恢复**（内容未改，编译通过）。`event.EventRegister` 不需要恢复：它的 5 处 `MinecraftForge.EVENT_BUS.register` 已由 `DeferredRegister` 各表 + `@EventBusSubscriber` 取代，`onEntityInteract` 在 1.12.2 就是空方法体，故不再移植。 |
| `cn/ac/nya/opticzcraft/entity/EntityThrow.java` | `src/main/java/cn/ac/nya/opticzcraft/entity/EntityThrow.java` | **恢复并补全**：新增 1.12.2 的 `(World, EntityLivingBase)` 构造器（`ItemThrow` 需要），其余内容与暂存版一致。 |
| `cn/ac/nya/opticzcraft/entity/EntityArrow.java` | `src/main/java/cn/ac/nya/opticzcraft/entity/EntityArrow.java` | 暂存版**原本就缺失**（前一个代理还没写到它），本轮新建。 |

## Batch 2b 实际产出（全部在 `opticzCraft/src/main/java/cn/ac/nya/opticzcraft/`）
- 新建 `OpticzEntities.java` —— `DeferredRegister<EntityType<?>>`，取代 1.12.2 的 `entity/EntityLoader`；
- 新建 `entity/EntityArrow.java`（集束导弹）、`item/ItemArrow.java`、`item/ItemThrow.java`；
- 恢复/补全 `entity/EntityThrow.java`（NSF 压力炸弹）；
- 修改 `OpticzItems.java`（新增 `THROW` = `stacksTo(42)`、`ARROW`）、`opticzCraft.java`（构造器里注册 `OpticzEntities`）。

## 仍未移植（有意延后，勿当作缺口）
- `render/ArrowRenderer.java`、`render/ThrowRenderer.java`：依赖尚未搬运的
  `assets/opticzcraft/**`（`models/item/*.json`、`textures/entity/arrow.png`、`textures/item/*.png`
  在 v3 里还不存在）。渲染器本身每帧引用这些资产，先写会得到一组必然悬空的引用，
  故与 `OpticzRenderers`（`EntityRenderersEvent.RegisterRenderers` 注册表）一起留到资产搬运那一批。
- `entity/EntityModelBinder.java`：1.12.2 的 `ModelRegistryEvent` + `IRenderFactory` 均已消失，
  其对应物就是上面那个 `OpticzRenderers` 注册表，随渲染器一起落地，因此不单独恢复这个类。

验证：`tools/agent-build.ps1 -Task ":opticzCraft:compileJava"` → `BUILD SUCCESSFUL`（exit=0）。
