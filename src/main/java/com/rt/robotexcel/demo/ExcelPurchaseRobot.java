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

            // Aguarda posicionar o cursor na primeira célula
            String searchType = searchByNF ? "nota fiscal" : "pedido";
            System.out.println("Posicione o cursor na coluna do " + searchType + "...");
            Thread.sleep(1000);
            RobotUtil robot = new RobotUtil();
            while (true) {
                ClipboardManager.clear();
                robot.enterCell();
                robot.selectAll();
                robot.copyToClipboard();

                String numero = ClipboardManager.getContent();
                System.out.println(searchType + " copiado: " + numero);
                
                if (numero != null && numero.trim().matches("\\d+")) {
                    String numeroTrimmed = numero.trim();
                    String response;
                    
                    // Decide qual API usar com base na opção de busca
                    if (searchByNF) {
                        response = api.searchPurchaseByInvoice(numeroTrimmed);
                    } else {
                        response = api.searchPurchaseOrder(numeroTrimmed);
                    }
                    
                    if (response != null) {
                        int success = excelUpdater.updatePurchaseOrder(response);
                        if (success == 0) {
                            System.out.println(searchType + " " + numeroTrimmed + " atualizado com sucesso.");
                       
                        } else if (success == 1) {
                            System.out.println("Nenhum " + searchType + " encontrado para o número: " + numeroTrimmed);
                        }   else {
                            System.out.println("Falha ao atualizar o " + searchType + " " + numeroTrimmed + ".");
                            break;
                        }
                    } 
                } else {
                    System.out.println("Número do " + searchType + " inválido: " + numero);
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