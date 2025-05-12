package com.rt.robotexcel.demo.config;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class ConfigurationManager {
    private static final String CONFIG_FILE = "column_config.json";
    
    public static void saveConfiguration(List<ExcelColumnConfig> configs) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (ExcelColumnConfig config : configs) {
                JSONObject configObj = new JSONObject();
                configObj.put("displayName", config.getDisplayName());
                configObj.put("jsonField", config.getJsonField());
                configObj.put("position", config.getPosition());
                jsonArray.put(configObj);
            }
            
            Files.write(Paths.get(CONFIG_FILE), jsonArray.toString(2).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static List<ExcelColumnConfig> loadConfiguration() {
        try {
            if (!Files.exists(Paths.get(CONFIG_FILE))) {
                return null;
            }
            
            String content = new String(Files.readAllBytes(Paths.get(CONFIG_FILE)));
            JSONArray jsonArray = new JSONArray(content);
            List<ExcelColumnConfig> configs = new ArrayList<>();
            
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                configs.add(new ExcelColumnConfig(
                    obj.getString("displayName"),
                    obj.getString("jsonField"),
                    obj.getInt("position")
                ));
            }
            
            return configs;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}