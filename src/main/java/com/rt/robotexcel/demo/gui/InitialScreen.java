package com.rt.robotexcel.demo.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.IOException;
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
    private JTextField emailField;
    private JPasswordField passwordField;
    private JTextField baseUrlField;
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
        loadEnvValues();
    }
    
    private void initializeComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel headerPanel = createHeaderPanel();
        JPanel instructionsPanel = createInstructionsPanel();
        JPanel configPanel = createConfigPanel();
        JPanel searchOptionsPanel = createSearchOptionsPanel();
        JPanel controlPanel = createControlPanel();
        
        // Adiciona os painéis ao layout principal
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(instructionsPanel, BorderLayout.CENTER);
        mainPanel.add(configPanel, BorderLayout.EAST);
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
            "depois moverá para o próximo registro automaticamente."
        );
        
        panel.add(new JScrollPane(instructionsText), BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createConfigPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, getMaximumSize().height/2));
        panel.setBorder(BorderFactory.createTitledBorder("Configuração API"));
        
        // Email
        JPanel emailPanel = new JPanel(new BorderLayout());
        emailPanel.add(new JLabel("Email:"), BorderLayout.NORTH);
        emailField = new JTextField(20);
        emailPanel.add(emailField, BorderLayout.CENTER);
        
        // Password
        JPanel passwordPanel = new JPanel(new BorderLayout());
        passwordPanel.add(new JLabel("Senha:"), BorderLayout.NORTH);
        passwordField = new JPasswordField(20);
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        
        // Base URL
        JPanel urlPanel = new JPanel(new BorderLayout());
        urlPanel.add(new JLabel("URL Base:"), BorderLayout.NORTH);
        baseUrlField = new JTextField(20);
        urlPanel.add(baseUrlField, BorderLayout.CENTER);
        
        // Salvar configuração
        JButton saveButton = new JButton("Salvar Configuração");
        JButton testConnection = new JButton("Testar Conexão");
        testConnection.addActionListener(e -> {
            try {
                
                Dotenv dotenv = Dotenv.load();
                ApiClient api = new ApiClient(dotenv.get("BASE_URL"));
                if (!api.login(dotenv.get("EMAIL"), dotenv.get("PASSWORD"))) {
                    System.out.println("Falha no login!");
                    JOptionPane.showMessageDialog(this, 
                        "Falha no login! Verifique suas credenciais.", 
                        "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (api.testConnection()) {
                    JOptionPane.showMessageDialog(this, 
                        "Conexão bem-sucedida!", 
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Falha na conexão!", 
                        "Erro", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Erro ao testar conexão: " + ex.getMessage(), 
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        saveButton.addActionListener(e -> saveEnvValues());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        // Adiciona componentes ao painel
        panel.add(emailPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(passwordPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(urlPanel);      
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(testConnection);
        buttonPanel.add(saveButton);
        panel.add(buttonPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        return panel;
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        JButton configColumnButton = new JButton("Configurar Colunas");
        configColumnButton.addActionListener(e -> openColumnManager());
        
        JButton startButton = new JButton("Iniciar Robô");
        startButton.setFont(new Font(startButton.getFont().getName(), Font.BOLD, startButton.getFont().getSize()));
        
        startButton.addActionListener(e -> startCountdown());

        countdownLabel = new JLabel("Aguardando...");
        countdownLabel.setFont(new Font("Arial", Font.BOLD, 16));
        countdownLabel.setHorizontalAlignment(JLabel.CENTER);
        
        buttonPanel.add(configColumnButton);
        buttonPanel.add(startButton);
        
        panel.add(buttonPanel, BorderLayout.CENTER);
        panel.add(countdownLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void loadEnvValues() {
        try {
            if (Files.exists(Paths.get(".env"))) {
                Dotenv dotenv = Dotenv.load();
                emailField.setText(dotenv.get("EMAIL"));
                passwordField.setText(dotenv.get("PASSWORD"));
                baseUrlField.setText(dotenv.get("BASE_URL"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao carregar configurações: " + e.getMessage(), 
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void saveEnvValues() {
        try {
            StringBuilder envContent = new StringBuilder();
            envContent.append("EMAIL=").append(emailField.getText()).append("\n");
            envContent.append("PASSWORD=").append(new String(passwordField.getPassword())).append("\n");
            envContent.append("BASE_URL=").append(baseUrlField.getText()).append("\n");
            
            Files.write(Paths.get(".env"), envContent.toString().getBytes());
            
            JOptionPane.showMessageDialog(this, 
                "Configurações salvas com sucesso!", 
                "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao salvar configurações: " + e.getMessage(), 
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void openColumnManager() {
        ColumnManagerWindow columnManager = new ColumnManagerWindow();
        columnManager.setVisible(true);
    }
    
    private void startCountdown() {
        if (isRunning) return;
        
        isRunning = true;
        
        String searchType = searchByPedidoRadio.isSelected() ? "pedido" : "nota fiscal";
        
        secondsRemaining = 8;
        countdownLabel.setText("Clique no n° do " + searchType + " " + secondsRemaining + "...");
        
        countdownTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                secondsRemaining--;
                
                if (secondsRemaining > 0) {
                    countdownLabel.setText("Clique no n° do " + searchType + ", Iniciando em " + secondsRemaining + "...");
                } else {
                    countdownTimer.stop();
                    countdownLabel.setText("Robô em execução!");
                    
                    new Thread(() -> {
                        try {
                            if (!Files.exists(Paths.get(".env"))) {
                                SwingUtilities.invokeLater(() -> {
                                    JOptionPane.showMessageDialog(InitialScreen.this, 
                                        "Configure e salve os dados de acesso primeiro!", 
                                        "Configuração Necessária", JOptionPane.WARNING_MESSAGE);
                                    isRunning = false;
                                    countdownLabel.setText("Aguardando...");
                                });
                                return;
                            }
                            
                            String[] args;
                            if (searchByNfRadio.isSelected()) {
                                args = new String[]{"--search-by-nf"};
                            } else {
                                args = new String[]{"--search-by-pedido"};
                            }
                            
                            ExcelPurchaseRobot.main(args);
                            
                            SwingUtilities.invokeLater(() -> {
                                isRunning = false;
                                countdownLabel.setText("Concluído!");
                            });
                        } catch (Exception ex) {
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(InitialScreen.this, 
                                    "Erro ao executar o robô: " + ex.getMessage(), 
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