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
    public static void main(String[] args) {
        // Verificar se deve abrir a tela inicial
        if (args.length > 0 && args[0].equals("--nogui")) {
            runRobot();
            return;
        }
        openInitialScreen();
      
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
            System.out.println("Posicione o cursor na coluna do pedido...");
            Thread.sleep(1000);
            RobotUtil robot = new RobotUtil();
            while (true) {
                ClipboardManager.clear();
                robot.enterCell();
                robot.selectAll();
                robot.copyToClipboard();

                String pedido = ClipboardManager.getContent();
                System.out.println("Pedido copiado: " + pedido);
                if (pedido != null && pedido.trim().matches("\\d+")) {
                    String pedidoTrimmed = pedido.trim();
                    String response = api.searchPurchaseOrder(pedidoTrimmed);
                    if (response != null) {
                        int success = excelUpdater.updatePurchaseOrder(response);
                        if (success == 0) {
                            System.out.println("Pedido " + pedidoTrimmed + " atualizado com sucesso.");
                        } else {
                            System.out.println("Falha ao atualizar o pedido " + pedidoTrimmed + ".");
                            break;
                        }
                    } else {
                        System.out.println("Pedido " + pedidoTrimmed + " não encontrado.");
                    }
                } else {
                    System.out.println("Número do pedido inválido: " + pedido);
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