package com.rt.robotexcel.demo;

import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLightLaf;
import com.rt.robotexcel.demo.api.ApiClient;
import com.rt.robotexcel.demo.config.ConfigurationManager;
import com.rt.robotexcel.demo.config.ExcelColumnConfig;
import com.rt.robotexcel.demo.excel.ExcelUpdater;
import com.rt.robotexcel.demo.gui.ColumnManagerWindow;
import com.rt.robotexcel.demo.gui.InitialScreen;
import com.rt.robotexcel.demo.robot.RobotUtil;
import com.rt.robotexcel.demo.util.ClipboardManager;

import io.github.cdimascio.dotenv.Dotenv;

public class ExcelPurchaseRobot {
    private static boolean searchByNF = false;
    
    public static void main(String[] args) {
        // Parse arguments
        for (String arg : args) {
            if (arg.equals("--search-by-nf")) {
                searchByNF = true;
            } else if (arg.equals("--search-by-pedido")) {
                searchByNF = false; 
            } else if (arg.equals("--nogui")) {
                runRobot();
                return;
            }
        }
        
        // If no arguments or not the --nogui flag, open the initial screen
        if (args.length == 0) {
            openInitialScreen();
        } else {
            runRobot();
        }
    }
    
    public static void openInitialScreen() {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new FlatLightLaf());
            } catch (Exception ex) {
                System.err.println("Failed to initialize LaF");
            }
            FlatLightLaf.setup();
            
            InitialScreen screen = new InitialScreen();
            screen.setVisible(true);
        });
    }
    
    public static void runRobot() {
        System.setProperty("java.awt.headless", "false");
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }
        FlatLightLaf.setup();

        try {
            Dotenv dotenv = Dotenv.load();
            ApiClient api = new ApiClient(dotenv.get("BASE_URL"));

            ExcelUpdater excelUpdater = new ExcelUpdater();

            // Set API client for NFE data fetching
            excelUpdater.setApiClient(api);

            // Carrega a configuração salva no ExcelUpdater
            List<ExcelColumnConfig> savedConfigs = ConfigurationManager.loadConfiguration();
            if (savedConfigs != null) {
                excelUpdater.setColumnConfigs(savedConfigs);
            } else {
                System.out.println("Nenhuma configuração encontrada. Configure as colunas primeiro.");
                ColumnManagerWindow window = new ColumnManagerWindow();
                window.setVisible(true);
                return;
            }

            // Login
            if (!api.login(dotenv.get("EMAIL"), dotenv.get("PASSWORD"))) {
                System.out.println("Falha no login!");
                return;
            }

            // Find position of cod_emp1_source and cod_pedc columns
            int codPedcPosition = -1;
            int codEmp1Position = -1;
            
            for (ExcelColumnConfig config : savedConfigs) {
                if (config.getJsonField().equals("cod_pedc")) {
                    codPedcPosition = config.getPosition();
                } else if (config.getJsonField().equals("cod_emp1_source")) {
                    codEmp1Position = config.getPosition();
                }
            }
            
            // Aguarda posicionar o cursor na primeira célula
            String searchType = searchByNF ? "nota fiscal" : "pedido";
            System.out.println("Posicione o cursor na coluna do " + searchType + "...");
            Thread.sleep(1000);
            RobotUtil robot = new RobotUtil();
            while (true) {
                // Read cod_pedc from current cell
                ClipboardManager.clear();
                robot.enterCell();
                robot.selectAll();
                robot.copyToClipboard();

                String codPedc = ClipboardManager.getContent();
                System.out.println(searchType + " copiado: " + codPedc);
                
                if (codPedc != null && codPedc.trim().matches("\\d+")) {
                    String codPedcTrimmed = codPedc.trim();
                    String codEmp1 = null;
                    
                    // If searching by pedido, read cod_emp1 from configured column
                    if (!searchByNF && codEmp1Position != 0 && codPedcPosition != -1) {
                        robot.pressEsc();
                        
                        // Navigate to cod_emp1 column
                        int moves = codPedcPosition - codEmp1Position;
                        if (moves > 0) {
                            for (int i = 0; i < moves; i++) {
                                robot.pressLeftArrow();
                            }
                        } else if (moves < 0) {
                            for (int i = 0; i < Math.abs(moves); i++) {
                                robot.pressRightArrow();
                            }
                        }
                        
                        // Read cod_emp1
                        ClipboardManager.clear();
                        robot.enterCell();
                        robot.selectAll();
                        robot.copyToClipboard();
                        codEmp1 = ClipboardManager.getContent();
                        System.out.println("cod_emp1 copiado: " + codEmp1);
                        
                        // Move back to cod_pedc column
                        robot.pressEsc();
                        if (moves > 0) {
                            for (int i = 0; i < moves; i++) {
                                robot.pressRightArrow();
                            }
                        } else if (moves < 0) {
                            for (int i = 0; i < Math.abs(moves); i++) {
                                robot.pressLeftArrow();
                            }
                        }
                    }
                    
                    String response;
                    
                    // Decide qual API usar com base na opção de busca
                    if (searchByNF) {
                        response = api.searchPurchaseByInvoice(codPedcTrimmed);
                    } else {
                        response = api.searchPurchaseOrder(codEmp1, codPedcTrimmed);
                    }
                    
                    if (response != null) {
                        int success = excelUpdater.updatePurchaseOrder(response);
                        if (success == 0) {
                            System.out.println(searchType + " " + codPedcTrimmed + " atualizado com sucesso.");
                       
                        } else if (success == 1) {
                            System.out.println("Nenhum " + searchType + " encontrado para o número: " + codPedcTrimmed);
                        }   else {
                            System.out.println("Falha ao atualizar o " + searchType + " " + codPedcTrimmed + ".");
                            break;
                        }
                    } 
                } else {
                    System.out.println("Número do " + searchType + " inválido: " + codPedc);
                    break;
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}