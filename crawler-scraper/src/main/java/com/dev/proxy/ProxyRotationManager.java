package com.dev.proxy;


import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ProxyRotationManager{
    // Danh sách Proxy dạng: "http://user:pass@ip:port" hoặc "http://ip:port"
    private final List<String> proxyList = new CopyOnWriteArrayList<>();
    private final AtomicInteger currentIndex = new AtomicInteger(0);

    public void setProxies(List<String> proxies) {
        this.proxyList.clear();
        if (proxies != null) {
            this.proxyList.addAll(proxies);
        }
    }

    /**
     * Lấy Proxy tiếp theo theo cơ chế Round-Robin
     */
    public String getNextProxy() {
        if (proxyList.isEmpty()) {
            return null; // Không dùng proxy
        }
        int index = Math.abs(currentIndex.getAndIncrement() % proxyList.size());
        return proxyList.get(index);
    }
}
