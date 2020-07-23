package me.lee.adaway.sina.hooker.info;

import me.lee.adaway.sina.constant.HookConstant;
import me.lee.adaway.sina.utils.LogUtil;

public class Classes {
    public static Class<?> FeedRedPacket;
    private static ClassLoader loader;

    public static void initClass(ClassLoader classLoader) {
        loader = classLoader;
        if(FeedRedPacket == null){
            findClass("com.sina.weibo.models.FeedRedPacket");
        }
    }

    private static Class<?> findClass(String className) {
        if (loader == null || className.isEmpty()) {
            return null;
        }
        try {
            LogUtil.log("加载类");
            return loader.loadClass(className);
        } catch (Throwable e) {
            LogUtil.log( String.format("Can't find the Class of name: %s!", className));
            return null;
        }
    }
}
