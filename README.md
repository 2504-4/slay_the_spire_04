# 杀戮尖塔 JavaFX 简化版

## 项目简介

使用 Java 17 和 JavaFX 实现的回合制卡牌战斗 Demo。玩家依次挑战三个小怪和最终 Boss，打出攻击、防御或复合效果卡牌；前三关胜利后，从三张随机奖励卡中选择一张，再进入下一关。

## 已实现功能

- 初始牌组：5 张打击 + 5 张防御；玩家初始生命 80。
- 每回合 3 点能量，抽 5 张牌；抽牌堆为空时回收并洗混弃牌堆。
- 展示生命、格挡、能量、各牌堆数量、敌人意图及战斗日志。
- 敌人按攻击、防御、攻防意图行动，格挡优先吸收伤害。
- 四关进度与奖励三选一；奖励等待期间禁止继续出牌或结束回合。
- 击败最终 Boss 才算通关；玩家生命归零则失败。

## 环境与构建入口

- 推荐 JDK 17（需完整 JDK，不是只有 JRE）；本次修复也验证 JDK 25 构建。
- JavaFX 17.0.2，由 Maven 管理。
- 项目自带 Maven Wrapper，固定 Maven 3.9.11，无需预先安装全局 Maven。
- 首次运行 Wrapper 需要联网下载 Maven 和项目依赖。后续使用本机缓存。
- Maven 项目入口是仓库根目录的 pom.xml，不是已迁移的 Slay/pom.xml。

### Windows PowerShell

在项目根目录执行：

~~~powershell
# 本机已安装 JDK 17 的路径。其他机器请改成实际 JDK 目录。
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'

# 查看实际使用的 Maven/JDK
.\mvnw.cmd -version

# 清理、编译、执行全部测试、生成覆盖率报告并打包
.\mvnw.cmd clean verify

# 启动 JavaFX 程序
.\mvnw.cmd javafx:run
~~~

JAVA_HOME 的上述设置只影响当前终端，不修改系统设置。若不设置，Wrapper 使用 PATH 中的 Java；请通过 -version 确认实际版本。

### macOS / Linux

~~~bash
# JAVA_HOME 指向本机 JDK 17
sh ./mvnw clean verify
sh ./mvnw javafx:run
~~~

当前自动验证仅覆盖 Windows；未验证 macOS / Linux 图形界面运行。

若全局 Maven 已配置，也可以使用 mvn clean verify 和 mvn javafx:run。

### IntelliJ IDEA

1. 打开或导入仓库根目录的 pom.xml，并重新加载 Maven 项目。
2. Project SDK 和语言级别使用 Java 17。
3. Maven 设置中选择项目 Wrapper（或 IDEA 内置 Maven），Runner JRE 建议使用 Project SDK / JDK 17。
4. 在 Maven 生命周期中执行 clean、verify。运行主类 org.example.Main 或执行 javafx:run。

## 构建产物与测试

以仓库根目录为基准：

- target/SlayProject-1.0-SNAPSHOT.jar：编译打包产物。
- target/surefire-reports/：JUnit 测试结果。
- target/site/jacoco/index.html：覆盖率报告。

本次验证结果：Windows 下 JDK 17.0.12 和 JDK 25.0.2 均通过 clean verify；每轮执行 18 个测试，失败、错误、跳过均为 0；另在 JDK 17 下连续重复运行 5 轮 test，全部通过。此次未执行图形界面交互验收。

当前 jar 是普通项目包，未配置为包含 JavaFX 和所有依赖的独立可执行包；不要将 java -jar 失败误认为 Maven 构建失败。推荐使用 Wrapper 的 javafx:run 启动。

测试覆盖：初始化、攻击与防御、非法索引、能量消耗和不足、回合结算、奖励选择与等待锁定、四关通关、玩家失败、终局操作限制、日志与牌堆守恒。

通关测试使用确定性前置状态，将当前敌人调整为一击可击败，然后通过真实控制器出牌和选奖励完成状态流转；它验证流程，不代表随机策略可以保证通关，也不是游戏平衡性或图形界面测试。

## 本次构建故障与修复（2026-09-15）

1. **mvn 命令不可用**：当前机器未把 Maven 加入 PATH。新增官方 Maven Wrapper，通过 .\mvnw.cmd 构建，不强制修改全局环境变量。
2. **测试失败**：旧 victoryCondition 忽略了奖励选择，第一关胜利后仍反复尝试出牌和结束回合，无法推进下一关。已修正为“击败小怪 → 选择奖励 → 下一关”，最后击败 Boss。
3. **测试可能空通过或受随机性影响**：去掉“如果恰好抽到某类卡/进入终局才断言”的做法；使用可重复的前置条件，并补充奖励等待、能量不足、失败终局测试。
4. **JDK 25 下覆盖率插桩异常**：实际日志出现 Unsupported class file major version 69。JaCoCo 从 0.8.10 更新为 0.8.14；未禁用覆盖率，也未跳过测试。
5. **编译与 IDE 语言级别不一致**：Maven 统一使用 release 17；IDE 项目语言级别由 25 调整为 17，保留已有项目迁移与业务代码。

遇到下载失败，请检查网络、代理和 Maven 仓库配置；不要用跳过测试作为修复方式。
