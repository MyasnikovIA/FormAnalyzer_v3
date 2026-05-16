// core/log/ILogger.java
package ru.tmis.analyzer.core.log;

public interface ILogger {
    void log(String message);
    void error(String message);
    void debug(String message);
}