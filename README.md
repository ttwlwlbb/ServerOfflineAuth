# ServerOfflineAuth

## 简介

------

为服务器上的离线玩家提供密码验证功能。




### 特点

- 内置加密数据存储

- Json与MySQL支持



### 配置

首次启动 ServerOfflineAuth 时，它会在 config/ 目录下创建 server_offline_auth-common.toml,内容如下

```toml

#Server Offline Auth 配置
[general]
    #登录超时时间（秒），0 为禁用
    #Range: 0 ~ 3600
    loginTimeout = 30
    #盐值长度（log rounds）。默认 10，范围 4-30。
    #Range: 4 ~ 30
    passwordHashWorkFactor = 10
    #玩家数据文件名（仅当存储类型为 json 时有效）
    dataFileName = "server_offline_auth_data.json"
    #玩家加入时是否发送提示
    joinMessageEnabled = true

[storage]
    #数据存储类型：json 或 mysql
    storageType = "json"
    #MySQL 主机地址
    mysqlHost = "localhost"
    #MySQL 端口
    #Range: 1 ~ 65535
    mysqlPort = 3306
    #MySQL 数据库名
    mysqlDatabase = "ServerOfflineAuth"
    #MySQL 用户名
    mysqlUsername = "root"
    #MySQL 密码
    mysqlPassword = "password"
    #MySQL 数据表名
    mysqlTable = "auth_players"

    [storage.migration]
    	#设置此项以执行数据迁移（如 "mysql" 或 "json"）。迁移成功后此项将自动清空，且 storageType 会被更新。
        migrateTo = ""

```
若数据存储模式为JSON,则会在服务端根目录下创建一个名为 server_offline_auth_data.json 的文件来存储玩家数据。



### 玩家命令

- `/register <passwd> <passwd>` 注册
- `/login <passwd>` 登陆
- `/logout` 登出
 
### 管理员命令

- `/serverofflineauth admin login <player_ID>` 强制登录
- `/serverofflineauth admin logout <player_ID>` 强制登出



## 要求

------

### 服务端要求

本插件基于Minecraft forge 1.20.1开发, 其他版本有效性未验证。



## 注意事项

------

**该mod处于早期测试阶段, 更多功能开发中, 请勿将测试版本用于重要场所, 以免造成可能的重要数据丢失**



## 贡献者

------

### 团队成员 ： ttwlwlbb51522, ChaosException, Archiveorigin