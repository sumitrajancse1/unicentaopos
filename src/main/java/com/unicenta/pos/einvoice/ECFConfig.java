package com.unicenta.pos.einvoice;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ECFConfig {

    private static final Properties props = new Properties();

    static {
        try {
            props.load(new FileInputStream("einvoice-config.properties"));
            System.out.println("✅ Loaded einvoice-config.properties");
        } catch (IOException e) {
            System.err.println("❌ Failed to load einvoice-config.properties: " + e.getMessage());
        }
    }

    // ✅ Standard get(key)
    public static String get(String key) {
        return props.getProperty(key, "");
    }

    // ✅ Overloaded get(key, default)
    public static String get(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }
}
