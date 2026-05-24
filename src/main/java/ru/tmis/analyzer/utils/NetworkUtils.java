// utils/NetworkUtils.java
package ru.tmis.analyzer.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public class NetworkUtils {

    private static final int PING_TIMEOUT = 2000; // 2 секунды
    private static final int SOCKET_TIMEOUT = 2000; // 2 секунды

    // Кэш доступности хостов (чтобы не пинговать каждый раз)
    private static final java.util.concurrent.ConcurrentHashMap<String, Boolean> reachableCache = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.concurrent.ConcurrentHashMap<String, Long> lastCheckCache = new java.util.concurrent.ConcurrentHashMap<>();
    private static final long CACHE_TTL = 30000; // 30 секунд кэшируем результат

    /**
     * Быстрая проверка доступности хоста с кэшированием
     */
    public static boolean isHostReachableWithCache(String host) {
        if (host == null || host.isEmpty()) return false;

        String cleanHost = extractHostFromUrl(host);
        Long lastCheck = lastCheckCache.get(cleanHost);

        // Если проверяли недавно, возвращаем кэшированный результат
        if (lastCheck != null && (System.currentTimeMillis() - lastCheck) < CACHE_TTL) {
            Boolean cached = reachableCache.get(cleanHost);
            if (cached != null) {
                return cached;
            }
        }

        boolean reachable = isHostReachable(cleanHost);
        reachableCache.put(cleanHost, reachable);
        lastCheckCache.put(cleanHost, System.currentTimeMillis());
        return reachable;
    }

    /**
     * Проверяет доступность хоста через ping (ICMP)
     * @param host хост (IP или доменное имя)
     * @return true если хост отвечает на ping
     */
    public static boolean isHostReachable(String host) {
        if (host == null || host.isEmpty()) return false;

        String cleanHost = extractHostFromUrl(host);

        try {
            // Пробуем InetAddress.isReachable (использует ICMP если есть права)
            InetAddress inet = InetAddress.getByName(cleanHost);
            boolean reachable = inet.isReachable(PING_TIMEOUT);
            System.out.println("[Network] Ping " + cleanHost + ": " + (reachable ? "✅ ДОСТУПЕН" : "❌ НЕДОСТУПЕН"));
            return reachable;
        } catch (UnknownHostException e) {
            System.err.println("[Network] Неизвестный хост: " + cleanHost);
            return false;
        } catch (IOException e) {
            System.err.println("[Network] Ошибка ping: " + e.getMessage());
            return false;
        }
    }

    /**
     * Проверяет доступность порта через socket connect (TCP)
     * @param host хост
     * @param port порт
     * @return true если порт доступен
     */
    public static boolean isPortReachable(String host, int port) {
        if (host == null || port <= 0) return false;

        String cleanHost = extractHostFromUrl(host);

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(cleanHost, port), SOCKET_TIMEOUT);
            System.out.println("[Network] Порт " + port + " на " + cleanHost + ": ✅ ДОСТУПЕН");
            return true;
        } catch (IOException e) {
            System.out.println("[Network] Порт " + port + " на " + cleanHost + ": ❌ НЕДОСТУПЕН (" + e.getMessage() + ")");
            return false;
        }
    }

    /**
     * Быстрая проверка доступности сервера БД (ping + порт)
     * @param url JDBC URL
     * @return true если сервер доступен
     */
    public static boolean isDatabaseServerAvailable(String url) {
        if (url == null || url.isEmpty()) return false;

        String host = extractHostFromUrl(url);
        int port = extractPortFromUrl(url);

        if (host.isEmpty() || port <= 0) {
            System.err.println("[Network] Не удалось извлечь хост/порт из URL: " + url);
            return false;
        }

        // Сначала быстрый ping
        if (!isHostReachable(host)) {
            System.out.println("[Network] Сервер " + host + " не отвечает на ping, пропускаем проверку порта");
            return false;
        }

        // Если ping успешен, проверяем порт
        return isPortReachable(host, port);
    }

    /**
     * Быстрая проверка доступности сервера БД с кэшированием
     */
    public static boolean isDatabaseServerAvailableWithCache(String url) {
        if (url == null || url.isEmpty()) return false;

        String key = extractHostFromUrl(url) + ":" + extractPortFromUrl(url);
        Long lastCheck = lastCheckCache.get(key);

        if (lastCheck != null && (System.currentTimeMillis() - lastCheck) < CACHE_TTL) {
            Boolean cached = reachableCache.get(key);
            if (cached != null) {
                return cached;
            }
        }

        boolean available = isDatabaseServerAvailable(url);
        reachableCache.put(key, available);
        lastCheckCache.put(key, System.currentTimeMillis());
        return available;
    }

    /**
     * Извлекает хост из JDBC URL
     */
    public static String extractHostFromUrl(String url) {
        if (url == null) return "";

        // Oracle: jdbc:oracle:thin:@192.168.241.141:1521/med2dev
        if (url.contains("jdbc:oracle:thin:@")) {
            String afterAt = url.substring(url.indexOf("@") + 1);
            if (afterAt.contains(":")) {
                return afterAt.substring(0, afterAt.indexOf(":"));
            }
        }

        // PostgreSQL: jdbc:postgresql://192.168.241.137:5432/med2dev
        if (url.contains("jdbc:postgresql://")) {
            String afterProtocol = url.substring(url.indexOf("//") + 2);
            if (afterProtocol.contains(":")) {
                return afterProtocol.substring(0, afterProtocol.indexOf(":"));
            }
        }

        return url;
    }

    /**
     * Извлекает порт из JDBC URL
     */
    private static int extractPortFromUrl(String url) {
        if (url == null) return -1;

        try {
            // Oracle
            if (url.contains("jdbc:oracle:thin:@")) {
                String afterAt = url.substring(url.indexOf("@") + 1);
                if (afterAt.contains(":")) {
                    String portPart = afterAt.substring(afterAt.indexOf(":") + 1);
                    if (portPart.contains("/")) {
                        return Integer.parseInt(portPart.substring(0, portPart.indexOf("/")));
                    }
                    if (portPart.contains(":")) {
                        return Integer.parseInt(portPart.substring(0, portPart.indexOf(":")));
                    }
                }
            }

            // PostgreSQL
            if (url.contains("jdbc:postgresql://")) {
                String afterProtocol = url.substring(url.indexOf("//") + 2);
                if (afterProtocol.contains(":")) {
                    String portPart = afterProtocol.substring(afterProtocol.indexOf(":") + 1);
                    if (portPart.contains("/")) {
                        return Integer.parseInt(portPart.substring(0, portPart.indexOf("/")));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[Network] Ошибка извлечения порта: " + e.getMessage());
        }

        // Порты по умолчанию
        if (url.contains("oracle")) return 1521;
        if (url.contains("postgresql")) return 5432;

        return -1;
    }

    /**
     * Очистить кэш доступности
     */
    public static void clearCache() {
        reachableCache.clear();
        lastCheckCache.clear();
    }
}