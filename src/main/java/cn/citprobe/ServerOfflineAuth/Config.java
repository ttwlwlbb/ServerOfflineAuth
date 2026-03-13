package cn.citprobe.ServerOfflineAuth;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class Config {
    public static class Server {
        public final ForgeConfigSpec.IntValue loginTimeout;
        public final ForgeConfigSpec.ConfigValue<String> dataFileName;
        public final ForgeConfigSpec.IntValue passwordHashWorkFactor;
        public final ForgeConfigSpec.BooleanValue joinMessageEnabled;
        public final ForgeConfigSpec.ConfigValue<String> storageType; // "json" 或 "mysql"
        public final ForgeConfigSpec.ConfigValue<String> mysqlHost;
        public final ForgeConfigSpec.IntValue mysqlPort;
        public final ForgeConfigSpec.ConfigValue<String> mysqlDatabase;
        public final ForgeConfigSpec.ConfigValue<String> mysqlUsername;
        public final ForgeConfigSpec.ConfigValue<String> mysqlPassword;
        public final ForgeConfigSpec.ConfigValue<String> mysqlTable;
        public final ForgeConfigSpec.ConfigValue<String> migrateTo;
        public final ForgeConfigSpec.IntValue tokenExpiryDays;


        Server(ForgeConfigSpec.Builder builder) {
            builder.comment("Server Offline Auth 配置").push("general");

            loginTimeout = builder
                    .comment("登录超时时间（秒），0 为禁用")
                    .defineInRange("loginTimeout", 30, 0, 3600);

            passwordHashWorkFactor = builder
                    .comment("盐值长度（log rounds）。默认 10，范围 4-30。")
                    .defineInRange("passwordHashWorkFactor", 10, 4, 30);


            dataFileName = builder
                    .comment("玩家数据文件名（仅当存储类型为 json 时有效）")
                    .define("dataFileName", "server_offline_auth_data.json");

            joinMessageEnabled = builder
                    .comment("玩家加入时是否发送提示")
                    .define("joinMessageEnabled", true);

            tokenExpiryDays = builder
                    .comment("Token 有效天数（0 表示永不过期）")
                    .defineInRange("tokenExpiryDays", 3, 0, 3650);

            builder.pop().push("storage");

            storageType = builder
                    .comment("数据存储类型：json 或 mysql")
                    .define("storageType", "json");

            mysqlHost = builder
                    .comment("MySQL 主机地址")
                    .define("mysqlHost", "localhost");

            mysqlPort = builder
                    .comment("MySQL 端口")
                    .defineInRange("mysqlPort", 3306, 1, 65535);

            mysqlDatabase = builder
                    .comment("MySQL 数据库名")
                    .define("mysqlDatabase", "ServerOfflineAuth");

            mysqlUsername = builder
                    .comment("MySQL 用户名")
                    .define("mysqlUsername", "root");

            mysqlPassword = builder
                    .comment("MySQL 密码")
                    .define("mysqlPassword", "password");

            mysqlTable = builder
                    .comment("MySQL 数据表名")
                    .define("mysqlTable", "auth_players");
            builder.pop();

            builder.push("migration");
            migrateTo = builder
                    .comment("设置此项以执行数据迁移（如 \"mysql\" 或 \"json\"）。迁移成功后此项将自动清空，且 storageType 会被更新。")
                    .define("migrateTo", "");
            builder.pop();
        }
    }

    public static final Server SERVER;
    public static final ForgeConfigSpec SERVER_SPEC;

    static {
        final Pair<Server, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Server::new);
        SERVER_SPEC = specPair.getRight();
        SERVER = specPair.getLeft();
    }
}