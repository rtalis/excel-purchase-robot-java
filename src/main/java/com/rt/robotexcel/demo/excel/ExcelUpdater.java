package com.rt.robotexcel.demo.excel;

import com.rt.robotexcel.demo.config.ExcelColumnConfig;
import com.rt.robotexcel.demo.robot.RobotUtil;
import com.rt.robotexcel.demo.util.ClipboardManager;

import java.text.SimpleDateFormat;
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
                    Date dtEnt = inputFormat.parse(nfObj.getString(field));
                    return outputFormat.format(dtEnt);
                }
                return nfObj.optString(field, "");
            }
            
            if (config.getJsonField().equals("dt_emis")) {
                Date dt = inputFormat.parse(purchase.getString(config.getJsonField()));
                return outputFormat.format(dt);
            }
            
            if (config.getJsonField().startsWith("total_")) {
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

    

    public int updatePurchaseOrder(String jsonResponse) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            JSONObject purchase;
            
            if (json.has("purchases")) {
                purchase = json.getJSONArray("purchases")
                    .getJSONObject(0)
                    .getJSONObject("order");
            } else {
                purchase = json;
            }
            
            // Encontra posição do PEDIDO
            int pedidoIndex = -1;
            for (int i = 0; i < columnConfigs.size(); i++) {
                if (columnConfigs.get(i).getJsonField().equals("cod_pedc")) {
                    pedidoIndex = i;
                    break;
                }
            }
            
            if (pedidoIndex == -1) {
                throw new IllegalStateException("Coluna PEDIDO é obrigatória");
            }
            
            // Posiciona no PEDIDO
            robot.pressEsc();
            int actualPosition = pedidoIndex - 1;

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
                if (!value.isEmpty()) {
                    ClipboardManager.setContent(value);
               robot.pasteFromClipboard();
                }
            }
            // Posiciona no PEDIDO novamente
            
            while (actualPosition + 1 > pedidoIndex) {
                    robot.pressLeftArrow();
                    actualPosition--;
                
            }      
            while (actualPosition + 1 < pedidoIndex) {
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