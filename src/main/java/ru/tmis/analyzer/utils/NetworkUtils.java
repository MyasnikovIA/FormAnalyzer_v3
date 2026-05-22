package ru.tmis.analyzer.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class NetworkUtils {

    private static final int PING_TIMEOUT = 3000; // 3 секунды
    private static final int SOCKET_TIMEOUT = 2000; // 2 секунды

    /**
     * Проверяет доступность хоста через ping
     */
    public static boolean isHostReachable(String host) {
        if (host == null || host.isEmpty()) return false;

        // Извлекаем хост из URL (jdbc:oracle:thin:@192.168.241.141:1521/med2dev)
        String cleanHost = extractHostFromUrl(host);

        try {
            InetAddress inet = InetAddress.getByName(cleanHost);
            boolean reachable = inet.isReachable(PING_TIMEOUT);
            System.out.println("[Network] Ping " + cleanHost + ": " + (reachable ? "ДОСТУПЕН" : "НЕДОСТУПЕН"));
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
     * Проверяет доступность порта через socket connect
     */
    public static boolean isPortReachable(String host, int port) {
        if (host == null || port <= 0) return false;

        String cleanHost = extractHostFromUrl(host);

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(cleanHost, port), SOCKET_TIMEOUT);
            System.out.println("[Network] Порт " + port + " на " + cleanHost + ": ДОСТУПЕН");
            return true;
        } catch (IOException e) {
            System.out.println("[Network] Порт " + port + " на " + cleanHost + ": НЕДОСТУПЕН (" + e.getMessage() + ")");
            return false;
        }
    }

    /**
     * Извлекает хост из JDBC URL
     */
    private static String extractHostFromUrl(String url) {
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
    public static int extractPortFromUrl(String url) {
        if (url == null) return -1;

        try {
            // Oracle: jdbc:oracle:thin:@192.168.241.141:1521/med2dev
            if (url.contains("jdbc:oracle:thin:@")) {
                String afterAt = url.substring(url.indexOf("@") + 1);
                if (afterAt.contains(":")) {
                    String portPart = afterAt.substring(afterAt.indexOf(":") + 1);
                    if (portPart.contains("/")) {
                        return Integer.parseInt(portPart.substring(0, portPart.indexOf("/")));
                    }
                }
            }

            // PostgreSQL: jdbc:postgresql://192.168.241.137:5432/med2dev
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
     * Полная проверка доступности сервера БД
     */
    public static boolean isDatabaseServerAvailable(String url) {
        String host = extractHostFromUrl(url);
        int port = extractPortFromUrl(url);

        if (host.isEmpty() || port <= 0) {
            System.err.println("[Network] Не удалось извлечь хост/порт из URL: " + url);
            return false;
        }

        System.out.println("[Network] Проверка сервера " + host + ":" + port + "...");
        return isPortReachable(host, port);
    }
}