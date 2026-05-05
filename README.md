# 微缩天体星门 (Shrunk Gateway)

[![License](https://img.shields.io/badge/License-ARR-blue.svg)](LICENSE)

一个Minecraft 1.12.2的模组，作为星辉魔法（Astral Sorcery）的附属模组。

## 功能介绍

手持"微缩天体星门"物品时，可以使用天体星门的传送功能，无需建造多方块结构。

### 特性

- ✅ 显示所有可用的目标星门（星星UI）
- ✅ 对准星星 + 按住Shift传送
- ✅ 支持跨维度传送
- ✅ 粒子效果和视野变化
- ✅ 物品放在星辉魔法创造标签栏

## 安装

1. 安装Minecraft 1.12.2
2. 安装Fo**rge 14.23.5.284**7或更高版本
3. 安装星辉魔法模组
4. 将本模组放入`.minecraft/mods`文件夹

## 开发环境搭建

### 前置要求

- JDK 8
- Gradle 4.9

### 构建步骤

```bash
# 克隆仓库
git clone https://github.com/NNYYOONNIIOO/shrunkgateway.git
cd shrunkgateway

# 设置开发环境
./gradlew setupDecompWorkspace

# 构建模组
./gradlew build
```

构建产物位于 `build/libs/` 目录。

## 法律声明

本模组是星辉魔法（Astral Sorcery）的附属模组。

### 实现说明

- 所有游戏机制（星星UI、对准+按住Shift传送等）均通过游戏内体验得知
- 仅使用星辉魔法提供的公共API
- 代码为原创实现，未复制任何源代码

### 致谢

感谢星辉魔法作者HellFirePvP创建的优秀模组！

## 许可证

本项目采用ARR许可证。详见 [LICENSE](LICENSE) 文件。

## 联系方式

如有问题或建议，请提交Issue。
