package com.rt.robotexcel.demo.config;


public class ExcelColumnConfig {
    private String displayName;
    private String jsonField;
    private int position; // relativa ao PEDIDO
    
    public ExcelColumnConfig(String displayName, String jsonField, int position) {
        this.displayName = displayName;
        this.jsonField = jsonField;
        this.position = position;
    }
    
    // Getters e setters
    public String getDisplayName() { return displayName; }
    public String getJsonField() { return jsonField; }
    public int getPosition() { return position; }
}