package com.rt.robotexcel.demo.excel;

import com.rt.robotexcel.demo.config.ExcelColumnConfig;
import com.rt.robotexcel.demo.robot.RobotUtil;
import com.rt.robotexcel.demo.util.ClipboardManager;

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.LocalDateTime;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONObject;

public class ExcelUpdater {
    private final RobotUtil robot;
    private final SimpleDateFormat inputFormat;
    private final SimpleDateFormat outputFormat;

    public ExcelUpdater() throws Exception {
        this.robot = new RobotUtil();
        this.inputFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH);
        this.outputFormat = new SimpleDateFormat("dd/MM/yyyy");
    }
        private List<ExcelColumnConfig> columnConfigs;
    
    public void setColumnConfigs(List<ExcelColumnConfig> configs) {
        this.columnConfigs = configs;
    }
    private String extractValueFromJson(JSONObject purchase, ExcelColumnConfig config) {
        try {
            if (config.getJsonField().isEmpty()) return ""; // coluna em branco

            // Tratamentos especiais para alguns campos
            if (config.getJsonField().equals("cod_emp1_source")) {
                // This is the cod_emp1 source column - leave it as is, don't overwrite
                return "SKIP";
            }

            if (config.getDisplayName().equals("GENERO")) {
                String observacao = purchase.optString("observacao", "");
                if (!observacao.isEmpty()) {
                    String[] parts = observacao.split("\n");
                    return parts.length > 0 ? parts[parts.length - 1].trim() : "";
                }
                return "";
            }
            if (config.getDisplayName().equals("OBSERVAÇÃO")) {
                String observacao = purchase.optString("observacao", " ");
                if (!observacao.isEmpty()) {
                   observacao = observacao.replaceAll("\\r?\\n", " ");
                   return observacao;
                }
                return "";
            }

            if (config.getDisplayName().equals("SOLIC.")) {
                return extractSolicitacao(purchase.optString("observacao", ""));
            }

            if (config.getJsonField().contains("nfes[0]")) {
                if (!purchase.has("nfes") || purchase.getJSONArray("nfes").isEmpty()) {
                    return "";
                }
                JSONObject nfObj = purchase.getJSONArray("nfes").getJSONObject(0);
                String field = config.getJsonField().replace("nfes[0].", "");

                if (field.equals("dt_ent")) {
                    String dateStr = nfObj.getString(field);
                    Date dtEnt = null;
                    try {
                        dtEnt = inputFormat.parse(dateStr);
                    } catch (java.text.ParseException e) {
                        // Try ISO 8601 fallback
                        try {
                            if (dateStr.contains("T")) {
                                // Handles both with and without time
                                dtEnt = parseIsoToDate(dateStr);
                            } else {
                                // Fallback: yyyy-MM-dd
                                dtEnt = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
                            }
                        } catch (Exception ex2) {
                            ex2.printStackTrace();
                            return "";
                        }
                    }
                    return outputFormat.format(dtEnt);
                }
                return nfObj.optString(field, "");
            }

            if (config.getJsonField().equals("dt_emis")) {
                String dateStr = purchase.getString(config.getJsonField());
                Date dt = null;
                try {
                    dt = inputFormat.parse(dateStr);
                } catch (java.text.ParseException e) {
                    // Try ISO 8601 fallback
                    try {
                        if (dateStr.contains("T")) {
                            dt = parseIsoToDate(dateStr);
                        } else {
                            dt = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
                        }
                    } catch (Exception ex2) {
                        ex2.printStackTrace();
                        return "";
                    }
                }
                return outputFormat.format(dt);
            }

            if (config.getJsonField().equals("adjusted_total")) {
                return String.format("%.2f", purchase.getDouble(config.getJsonField()));
            }
            if (config.getJsonField().equals("total_bruto")) {
                return String.format("%.2f", purchase.getDouble(config.getJsonField()));
            }

            return purchase.optString(config.getJsonField(), "");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    
    private String extractSolicitacao(String observacao) {
        if (observacao == null) return "";
        
        // Remove acentos e converte para minúsculo
        String normalizedObs = observacao.toLowerCase()
            .replaceAll("[àáâãäå]", "a")
            .replaceAll("[èéêë]", "e")
            .replaceAll("[ìíîï]", "i")
            .replaceAll("[òóôõö]", "o")
            .replaceAll("[ùúûü]", "u")
            .replaceAll("[ç]", "c");

        // Procura por "solicitacao" seguido de números
        int index = normalizedObs.indexOf("solicitacao");
        if (index != -1) {
            String remaining = observacao.substring(index + 11).trim();
            String[] words = remaining.split("\\s+");
            if (words.length > 0 && words[0].matches("\\d+")) {
                return words[0];
            }
        }
        return "";
    }

    

    private Date parseIsoToDate(String dateStr) throws Exception {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            // Try full offset-aware parser first
            return Date.from(OffsetDateTime.parse(dateStr).toInstant());
        } catch (DateTimeParseException ex1) {
            try {
                // Try local date-time without offset (assume UTC)
                LocalDateTime ldt = LocalDateTime.parse(dateStr);
                return Date.from(ldt.atZone(ZoneOffset.UTC).toInstant());
            } catch (DateTimeParseException ex2) {
                // Fallback to Instant parsing (handles Z or offsets differently)
                return Date.from(Instant.parse(dateStr));
            }
        }
    }


    public int updatePurchaseOrder(String jsonResponse) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            JSONObject purchase;
            String searchMode = "nfes[0].num_nf";
            if (json.has("purchases")) {
                purchase = json.getJSONArray("purchases")
                    .getJSONObject(0)
                    .getJSONObject("order");
                searchMode = "cod_pedc";
            } else {
                purchase = json;
            }
            
           
            // Encontra posição do PEDIDO
            int referenceIndex = -1;
            for (int i = 0; i < columnConfigs.size(); i++) {
                if (columnConfigs.get(i).getJsonField().equals(searchMode)) {
                    referenceIndex = i;
                    break;
                }
            }
            
            if (referenceIndex == -1) {
                throw new IllegalStateException(String.format("Campo %s não encontrado na configuração.", searchMode));
            }
            
            // Posiciona no PEDIDO
            robot.pressEsc();
            int actualPosition = referenceIndex - 1;

            for (ExcelColumnConfig config : columnConfigs) {
                int moves = config.getPosition();
                System.out.println("Coluna: " + config.getDisplayName() + ", Posição: " + moves + ", Posição Atual: " + actualPosition);
                if (moves < actualPosition) {
                    for (int i = 0; i < actualPosition - moves; i++) {
                        robot.pressLeftArrow();
                        actualPosition--;
                    }
                } else if (moves > actualPosition) {
                    for (int i = 0; i < moves - actualPosition; i++) {
                        robot.pressRightArrow();
                        actualPosition++;
                    }
                }
                
                String value = extractValueFromJson(purchase, config);
                if (!value.isEmpty() && !value.equals("SKIP")) {
                    ClipboardManager.setContent(value);
                    robot.pasteFromClipboard();
                }
            }
            // Posiciona no PEDIDO novamente
            
            while (actualPosition + 1 > referenceIndex) {
                    robot.pressLeftArrow();
                    actualPosition--;
                
            }      
            while (actualPosition + 1 < referenceIndex) {
                robot.pressRightArrow();
                actualPosition++;
            
        }
            robot.pressDownArrow();
            Thread.sleep(200);
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}