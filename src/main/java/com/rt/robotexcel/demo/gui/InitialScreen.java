package com.rt.robotexcel.demo.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import com.formdev.flatlaf.FlatLightLaf;
import com.rt.robotexcel.demo.ExcelPurchaseRobot;
import com.rt.robotexcel.demo.api.ApiClient;
import io.github.cdimascio.dotenv.Dotenv;

public class InitialScreen extends JFrame {

    private final Color primary = new Color(52, 120, 246);
    private final Color surface = new Color(248, 250, 252);
    private final Color border = new Color(225, 229, 235);
    private final Color textPrimary = new Color(55, 65, 81);
    private final Color muted = new Color(90, 102, 117);

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
        setTitle("Excel Purchase Robot - Versão " + loadVersion());
        setSize(960, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initializeComponents();
    }

    private void initializeComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(14, 14));
        mainPanel.setBackground(surface);
        mainPanel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(surface);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weighty = 0.65;
        center.add(createInstructionsPanel(), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weighty = 0.35;
        center.add(createSearchOptionsPanel(), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        center.add(createControlPanel(), gbc);

        mainPanel.add(center, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(surface);

        JPanel titles = new JPanel(new BorderLayout());
        titles.setBackground(surface);
        JLabel titleLabel = new JLabel("Excel Purchase Robot");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(textPrimary);
        JLabel subtitle = new JLabel("Configuração inicial e início rápido do robô");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(muted);
        titles.add(titleLabel, BorderLayout.NORTH);
        titles.add(subtitle, BorderLayout.CENTER);

        JButton gearButton = createButton("Configurar", primary, Color.WHITE, primary);
        gearButton.addActionListener(e -> {
            ConfigWindow cfg = new ConfigWindow(InitialScreen.this);
            cfg.setVisible(true);
        });

        headerPanel.add(titles, BorderLayout.WEST);
        headerPanel.add(gearButton, BorderLayout.EAST);
        return headerPanel;
    }

    private JPanel createSearchOptionsPanel() {
        JPanel panel = createCardPanel("Opções de Busca");
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        searchByPedidoRadio = new JRadioButton("Buscar por Nº do Pedido", true);
        searchByNfRadio = new JRadioButton("Buscar por Nº da NF", false);

        for (JRadioButton rb : new JRadioButton[] { searchByPedidoRadio, searchByNfRadio }) {
            rb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            rb.setForeground(textPrimary);
            rb.setBackground(Color.WHITE);
            rb.setFocusPainted(false);
        }

        ButtonGroup searchTypeGroup = new ButtonGroup();
        searchTypeGroup.add(searchByPedidoRadio);
        searchTypeGroup.add(searchByNfRadio);

        panel.add(Box.createRigidArea(new Dimension(0, 4)));
        panel.add(searchByPedidoRadio);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(searchByNfRadio);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createInstructionsPanel() {
        JPanel panel = createCardPanel("Instruções");
        panel.setLayout(new BorderLayout());

        JTextArea instructionsText = new JTextArea();
        instructionsText.setEditable(false);
        instructionsText.setWrapStyleWord(true);
        instructionsText.setLineWrap(true);
        instructionsText.setBackground(Color.WHITE);
        instructionsText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        instructionsText.setForeground(textPrimary);
        instructionsText.setText(
                "1. Configure o token e a URL em Configurar.\n\n" +
                        "2. Ajuste as colunas da planilha em Configurar Colunas.\n\n" +
                        "3. Abra a planilha com o layout configurado.\n\n" +
                        "4. Escolha se vai buscar por Pedido ou NF.\n\n" +
                        "5. Clique em Iniciar Robô e clique na célula com o número.\n\n" +
                        "6. Não use o mouse/teclado durante a execução.\n\n" +
                        "7. O robô preenche a linha e segue para o próximo registro automaticamente.");

        JScrollPane scroll = new JScrollPane(instructionsText);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = createCardPanel("Execução do Robô");
        panel.setLayout(new BorderLayout(10, 10));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttonPanel.setOpaque(false);

        JButton startButton = createButton("Iniciar Robô", primary, Color.WHITE, primary);
        startButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        startButton.addActionListener(e -> startCountdown());

        JButton configColumns = createButton("Configurar Colunas", new Color(243, 244, 246), textPrimary, border);
        configColumns.addActionListener(e -> {
            ColumnManagerWindow cols = new ColumnManagerWindow();
            cols.setLocationRelativeTo(InitialScreen.this);
            cols.setVisible(true);
        });

        buttonPanel.add(configColumns);
        buttonPanel.add(startButton);

        countdownLabel = new JLabel("Pronto para iniciar");
        countdownLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        countdownLabel.setHorizontalAlignment(JLabel.LEFT);
        countdownLabel.setForeground(textPrimary);

        panel.add(countdownLabel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
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

        if (!areCredentialsSet()) {
            JOptionPane.showMessageDialog(InitialScreen.this,
                    "Configure e salve os dados de acesso (Token e URL)!",
                    "Configuração Necessária", JOptionPane.WARNING_MESSAGE);

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

                            SwingUtilities.invokeLater(() -> countdownLabel.setText("Robô em execução!"));

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

    private String loadVersion() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (Exception ignored) {
            // Best-effort; fallback below
        }
        String raw = props.getProperty("app.version", "0.0.0");
        return raw.replace("v", "");
    }

    private JPanel createCardPanel(String title) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border, 1, true),
                new EmptyBorder(14, 14, 14, 14)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(textPrimary);
        titleLabel.setBorder(new EmptyBorder(0, 0, 8, 0));
        card.add(titleLabel, BorderLayout.NORTH);
        return card;
    }

    private JButton createButton(String text, Color background, Color foreground, Color borderColor) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1, true),
                new EmptyBorder(8, 14, 8, 14)));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
}
