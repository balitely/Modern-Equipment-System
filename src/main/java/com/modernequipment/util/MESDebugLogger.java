package com.modernequipment.util;

import com.modernequipment.config.MESConfig;
import org.apache.logging.log4j.Logger;

/**
 * 调试日志工具类，根据 MESConfig.debugLogging 开关控制是否输出日志
 */
public class MESDebugLogger {

    public static void info(Logger logger, String message) {
        if (MESConfig.isDebugLoggingEnabled()) {
            logger.info(message);
        }
    }

    public static void info(Logger logger, String format, Object... args) {
        if (MESConfig.isDebugLoggingEnabled()) {
            logger.info(format, args);
        }
    }

    public static void warn(Logger logger, String message) {
        if (MESConfig.isDebugLoggingEnabled()) {
            logger.warn(message);
        }
    }

    public static void warn(Logger logger, String format, Object... args) {
        if (MESConfig.isDebugLoggingEnabled()) {
            logger.warn(format, args);
        }
    }

    public static void error(Logger logger, String message) {
        logger.error(message);
    }

    public static void error(Logger logger, String format, Object... args) {
        logger.error(format, args);
    }

    public static void error(Logger logger, String message, Throwable t) {
        logger.error(message, t);
    }
}