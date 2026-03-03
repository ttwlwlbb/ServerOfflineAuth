package cn.citprobe.ServerOfflineAuth;

public class PasswordUtils {
    public static String hashPassword(String plainPassword) {
        try {
            int factor = Config.SERVER.passwordHashWorkFactor.get();
            return PasswordHash.hashpw(plainPassword, PasswordHash.gensalt(factor));
        } catch (Throwable t) {  // 捕获所有异常和错误
            ServerOfflineAuth.LOGGER.error("密码哈希失败", t);
            throw new RuntimeException("密码哈希失败", t); // 包装为运行时异常，便于上层处理
        }
    }

    public static boolean checkPassword(String plainPassword, String hashed) {
        return PasswordHash.checkpw(plainPassword, hashed);
    }
}
