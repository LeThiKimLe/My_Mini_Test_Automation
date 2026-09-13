package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Central logging facade shared by pages, flows, and fixtures. */
public class Loggers {
    private final Logger logger = LoggerFactory.getLogger(Loggers.class);

    public void info(String message) { logger.info(message); }
    public void warn(String message) { logger.warn(message); }
    public void error(String message) { logger.error(message); }
    public void debug(String message) { logger.debug(message); }
    public void passed(String message) { logger.info("[PASSED] {}", message); }
    public void failed(String message) { logger.error("[FAILED] {}", message); }
}
