package com.rt.robotexcel.demo.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.nio.file.Files;
import java.nio.file.Paths;

import com.formdev.flatlaf.FlatLightLaf;
import com.rt.robotexcel.demo.ExcelPurchaseRobot;
import com.rt.robotexcel.demo.api.ApiClient;
import io.github.cdimascio.dotenv.Dotenv;

public class InitialScreen extends JFrame {

    private JLabel countdownLabel;
    private Timer countdownTimer;
    private int secondsRemaining = 8;
    private boolean isRunning = false;
    private JRadioButton searchByPedidoRadio;
    private JRadioButton searchByNfRadio;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }

        SwingUtilities.invokeLater(() -> {
            InitialScreen screen = new InitialScreen();
            screen.setVisible(true);
        });
    }

    public InitialScreen() {
        setTitle("Excel Purchase Robot - Configuração Inicial");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initializeComponents();
    }

    private void initializeComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = createHeaderPanel();
        JPanel instructionsPanel = createInstructionsPanel();
        JPanel searchOptionsPanel = createSearchOptionsPanel();
        JPanel controlPanel = createControlPanel();

        // Adiciona os painéis ao layout principal
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(instructionsPanel, BorderLayout.CENTER);
        mainPanel.add(searchOptionsPanel, BorderLayout.WEST);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        // Define o painel principal como conteúdo da janela
        setContentPane(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Excel Purchase Robot");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);

        headerPanel.add(titleLabel, BorderLayout.CENTER);
        // Gear/settings button on the right
        JButton gearButton = new JButton("⚙️");
        gearButton.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        gearButton.setForeground(Color.WHITE);
        gearButton.setBackground(new Color(66, 133, 244));
        gearButton.setFocusPainted(false);
        gearButton.setBorderPainted(false);
        gearButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gearButton.addActionListener(e -> {
            ConfigWindow cfg = new ConfigWindow(InitialScreen.this);
            cfg.setVisible(true);
        });
        headerPanel.add(gearButton, BorderLayout.EAST);
        return headerPanel;
    }

    private JPanel createSearchOptionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Opções de Busca"));

        searchByPedidoRadio = new JRadioButton("Buscar por Nº do Pedido", true);
        searchByNfRadio = new JRadioButton("Buscar por Nº da NF", false);

        ButtonGroup searchTypeGroup = new ButtonGroup();
        searchTypeGroup.add(searchByPedidoRadio);
        searchTypeGroup.add(searchByNfRadio);

        panel.add(searchByPedidoRadio);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(searchByNfRadio);

        return panel;
    }

    private JPanel createInstructionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Instruções"));

        JTextArea instructionsText = new JTextArea();
        instructionsText.setEditable(false);
        instructionsText.setWrapStyleWord(true);
        instructionsText.setLineWrap(true);
        instructionsText.setBackground(panel.getBackground());
        instructionsText.setText(
                "1. Configure os dados de acesso à API nos campos ao lado.\n\n" +
                        "2. Verifique se as colunas foram configuradas corretamente. " +
                        "Se necessário, use o botão \"Configurar Colunas\".\n\n" +
                        "3. Abra sua planilha Excel de acordo com as colunas configuradas.\n\n" +
                        "4. Selecione se deseja buscar por número de pedido ou por NF.\n\n" +
                        "5. Clique em \"Iniciar Robô\" e posicione o cursor na coluna com o número.\n\n" +
                        "6. Quando o robô iniciar, não mexa no mouse ou teclado para não interferir na automação.\n\n" +
                        "7. O robô irá ler o número, buscar os dados na API e preencher a linha, " +
                        "depois moverá para o próximo registro automaticamente.");

        panel.add(new JScrollPane(instructionsText), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton startButton = new JButton("Iniciar Robô");
        startButton.setFont(new Font(startButton.getFont().getName(), Font.BOLD, startButton.getFont().getSize()));

        startButton.addActionListener(e -> startCountdown());

        countdownLabel = new JLabel("Aguardando...");
        countdownLabel.setFont(new Font("Arial", Font.BOLD, 16));
        countdownLabel.setHorizontalAlignment(JLabel.CENTER);

        buttonPanel.add(startButton);

        panel.add(buttonPanel, BorderLayout.CENTER);
        panel.add(countdownLabel, BorderLayout.SOUTH);

        return panel;
    }

    private void openColumnManager() {
        ColumnManagerWindow columnManager = new ColumnManagerWindow();
        columnManager.setVisible(true);
    }

    private boolean areCredentialsSet() {
        try {
            if (!Files.exists(Paths.get(".env"))) {
                return false;
            }

            Dotenv dotenv = Dotenv.load();
            String token = dotenv.get("TOKEN");
            String baseUrl = dotenv.get("BASE_URL");

            return token != null && !token.trim().isEmpty() &&
                    baseUrl != null && !baseUrl.trim().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private void startCountdown() {
        if (isRunning)
            return;

        // Check if credentials are set
        if (!areCredentialsSet()) {
            JOptionPane.showMessageDialog(InitialScreen.this,
                    "Configure e salve os dados de acesso (Token e URL)!",
                    "Configuração Necessária", JOptionPane.WARNING_MESSAGE);

            // Open ConfigWindow
            ConfigWindow cfg = new ConfigWindow(InitialScreen.this);
            cfg.setVisible(true);
            return;
        }

        isRunning = true;

        String searchType = searchByPedidoRadio.isSelected() ? "pedido" : "nota fiscal";

        secondsRemaining = 8;
        countdownLabel.setText("Clique no n° do " + searchType + " " + secondsRemaining + "...");

        countdownTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                secondsRemaining--;

                if (secondsRemaining > 0) {
                    countdownLabel
                            .setText("Clique no n° do " + searchType + ", Iniciando em " + secondsRemaining + "...");
                } else {
                    countdownTimer.stop();
                    countdownLabel.setText("Testando conexão...");

                    new Thread(() -> {
                        try {
                            Dotenv dotenv = Dotenv.load();
                            String token = dotenv.get("TOKEN");
                            String baseUrl = dotenv.get("BASE_URL");

                            // Test connection
                            ApiClient api = new ApiClient(baseUrl);
                            boolean authenticated = api.authenticateWithToken(token);

                            if (!authenticated) {
                                SwingUtilities.invokeLater(() -> {
                                    JOptionPane.showMessageDialog(InitialScreen.this,
                                            "Erro de autenticação!\nVerifique se o token está válido.",
                                            "Falha na Autenticação", JOptionPane.ERROR_MESSAGE);
                                    isRunning = false;
                                    countdownLabel.setText("Aguardando...");
                                });
                                return;
                            }

                            boolean connected = api.testConnection();

                            if (!connected) {
                                SwingUtilities.invokeLater(() -> {
                                    JOptionPane.showMessageDialog(InitialScreen.this,
                                            "Erro ao conectar à API!\nVerifique se a URL está correta e se o servidor está disponível.",
                                            "Falha na Conexão", JOptionPane.ERROR_MESSAGE);
                                    isRunning = false;
                                    countdownLabel.setText("Aguardando...");
                                });
                                return;
                            }

                            SwingUtilities.invokeLater(() -> {
                                countdownLabel.setText("Robô em execução!");
                            });

                            String[] args;
                            if (searchByNfRadio.isSelected()) {
                                args = new String[] { "--search-by-nf" };
                            } else {
                                args = new String[] { "--search-by-pedido" };
                            }

                            ExcelPurchaseRobot.main(args);

                            SwingUtilities.invokeLater(() -> {
                                isRunning = false;
                                countdownLabel.setText("Concluído!");
                            });
                        } catch (Exception ex) {
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(InitialScreen.this,
                                        "Erro: " + ex.getMessage(),
                                        "Erro", JOptionPane.ERROR_MESSAGE);
                                isRunning = false;
                                countdownLabel.setText("Erro!");
                            });
                        }
                    }).start();
                }
            }
        });

        countdownTimer.start();
    }
}