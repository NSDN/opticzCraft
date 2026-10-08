# opticzcraft Batch 2b 暂存（未被编译）

这两个文件由子代理在「Batch 2b：实体与两个投掷物物品」中途写入，但该代理随后失败退出，
**它引用的注册类 `OpticzEntities` 从未被创建**，因此无法编译。为恢复可编译状态，
把它们移出 Gradle 源集（放在这里而不是删掉，以保住已有工作）。

## 恢复步骤（交给负责 Batch 2b 的代理）
1. 先按 1.12.2 侧 `entity/{EntityArrow,EntityThrow,EntityLoader,EntityModelBinder}.java` 与
   `item/{ItemArrow,ItemThrow}.java` 把实体/物品补齐；
2. 新建 `cn.ac.nya.opticzcraft.OpticzEntities`（`DeferredRegister<EntityType<?>>`）；
   契约实测（见 `docs/09-NEOFORGE-API-CHEATSHEET.md` 末节）：
   - `EntityType.Builder.of(EntityFactory<T>, MobCategory)`，其中 `EntityFactory<T>` 是
     **`T create(EntityType<T>, Level)`（含类型，与 `BlockEntityType` 的两参不含类型相反）**；
   - 原版 **有 `AbstractMinecart` 基类**可继承（本模块无关，但 railway 需要）；
   - `Entity` 的三个 abstract：`defineSynchedData(SynchedEntityData.Builder)`、
     `readAdditionalSaveData`、`addAdditionalSaveData`；
   - `getAddEntityPacket(ServerEntity)` 带参数。
3. 把这两个文件移回 `src/main/java/cn/ac/nya/opticzcraft/{entity,event}/`（保持包名不变即可编译）；
4. 用 `tools/agent-build.ps1 -Task ":opticzCraft:compileJava"` 验证。

注意：`event/EventsHandler.java` 同时引用了 `OpticzEntities`，若最终不需要它，可只恢复 `EntityThrow`。