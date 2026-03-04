# ServerOfflineAuth
[简体中文](README_CN.md)
## Introduction

------

Provides password authentication for offline players on the server.




### Features

- Built-in encrypted data storage

- Json and MySQL support



### Configuration

When ServerOfflineAuth is first started, it creates the `server_offline_auth-common.toml` file in the `config/` directory with the following content:

```toml

#Server Offline Auth Configuration
[general]
    #Login timeout (seconds), set to 0 to disable
    #Range: 0 ~ 3600
    loginTimeout = 30
    #Salt length (log rounds). Default 10, range 4-30.
    #Range: 4 ~ 30
    passwordHashWorkFactor = 10
    #Player data file name (only effective when storage type is json)
    dataFileName = "server_offline_auth_data.json"
    #Whether to send a prompt when a player joins
    joinMessageEnabled = true

[storage]
    #Data storage type: json or mysql
    storageType = "json"
    #MySQL host address
    mysqlHost = "localhost"
    #MySQL port
    #Range: 1 ~ 65535
    mysqlPort = 3306
    #MySQL database name
    mysqlDatabase = "ServerOfflineAuth"
    #MySQL username
    mysqlUsername = "root"
    #MySQL password
    mysqlPassword = "password"
    #MySQL table name
    mysqlTable = "auth_players"

    [storage.migration]
    	#Set this to perform data migration (e.g., "mysql" or "json"). After successful migration, this value will be automatically cleared, and storageType will be updated.
        migrateTo = ""
        
```
- If the data storage mode is JSON, a file named `server_offline_auth_data.json` will be created in the server root directory to store player data.
- If the data storage mode is MySQL, you need to ensure that the MySQL server is properly configured and running, and that the provided connection information is correct.

### Player Commands

- `/register <passwd> <passwd>` Register
- `/login <passwd>` Login
- `/logout` Logout

### Admin Commands

- `/serverofflineauth admin login <player_ID>` Force login
- `/serverofflineauth admin logout <player_ID>` Force logout



## Requirements

------

### Server Requirements

This mod is developed based on Minecraft Forge 1.20.1; its effectiveness on other versions has not been verified.



## Precautions

------

**This mod is in an early testing stage, with more features under development. Do not use test versions in important environments.**

**If you encounter bugs during use, please submit an issue on GitHub.**



## Contributors

------

### Team Members: ttwlwlbb51522, ChaosException, Archiveorigin
