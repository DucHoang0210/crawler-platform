package com.dev.util;

import java.net.URI;
import java.util.Locale;

public class UrlUtils {

    public static String extractDomain(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host == null) return "";

            host = host.toLowerCase(Locale.ROOT);
            // Loại bỏ 'www.' nếu có
            return host.startsWith("www.") ? host.substring(4) : host;
        } catch (Exception e) {
            return "";
        }
    }
}