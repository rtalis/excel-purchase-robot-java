package com.rt.robotexcel.demo.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.rt.robotexcel.demo.api.ApiClient;
import io.github.cdimascio.dotenv.Dotenv;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ConfigWindow extends JDialog {
    private JTextField tokenField;
    private JTextField baseUrlField;
    private JSlider delaySlider;
    private JLabel delayValueLabel;
    private JLabel statusLabel;

    public ConfigWindow(Frame owner) {
        super(owner, "Configuração", true);
        initUI();
        loadValues();
        setResizable(false);
        setSize(680, 400);
        setLocationRelativeTo(null);
    }

    private void initUI() {
        Color primary = new Color(52, 120, 246);
        Color surface = new Color(248, 250, 252);
        Color border = new Color(225, 229, 235);

        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setBackground(surface);
        content.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(surface);
        JLabel title = new JLabel("Configurações de Acesso");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        JLabel subtitle = new JLabel("Token, endpoint da API e velocidade");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(90, 102, 117));
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.CENTER);
        content.add(header, BorderLayout.NORTH);

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border, 1, true),
                new EmptyBorder(16, 16, 16, 16)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.25;
        JLabel tokenLabel = new JLabel("Chave (TOKEN)");
        tokenLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tokenLabel.setForeground(new Color(55, 65, 81));
        formCard.add(tokenLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.75;
        tokenField = new JTextField();
        tokenField.setPreferredSize(new Dimension(360, 34));
        formCard.add(tokenField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.25;
        JLabel urlLabel = new JLabel("Site (BASE_URL)");
        urlLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        urlLabel.setForeground(new Color(55, 65, 81));
        formCard.add(urlLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.75;
        baseUrlField = new JTextField();
        baseUrlField.setPreferredSize(new Dimension(360, 34));
        formCard.add(baseUrlField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.25;
        JLabel delayLabel = new JLabel("Velocidade");
        delayLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        delayLabel.setForeground(new Color(55, 65, 81));
        formCard.add(delayLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.75;
        JPanel delayPanel = new JPanel(new BorderLayout(10, 0));
        delayPanel.setBackground(Color.WHITE);

        delaySlider = new JSlider(JSlider.HORIZONTAL, 5, 20, 10);
        delaySlider.setPreferredSize(new Dimension(300, 40));
        delaySlider.addChangeListener(e -> updateDelayLabel());

        delayValueLabel = new JLabel("1.0x");
        delayValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        delayValueLabel.setForeground(primary);
        delayValueLabel.setPreferredSize(new Dimension(50, 20));
        delayValueLabel.setHorizontalAlignment(JLabel.CENTER);

        delayPanel.add(delaySlider, BorderLayout.CENTER);
        delayPanel.add(delayValueLabel, BorderLayout.EAST);
        formCard.add(delayPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        statusLabel = new JLabel("Pronto");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setHorizontalAlignment(JLabel.CENTER);
        statusLabel.setForeground(new Color(90, 102, 117));
        statusLabel.setBorder(new EmptyBorder(6, 0, 0, 0));
        formCard.add(statusLabel, gbc);

        content.add(formCard, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttons.setBackground(surface);

        JButton colsBtn = createButton("Configurar colunas", new Color(243, 244, 246), new Color(55, 65, 81), border);
        JButton testBtn = createButton("Testar conexão", new Color(234, 246, 255), primary, new Color(205, 228, 255));
        JButton saveBtn = createButton("Salvar", primary, Color.WHITE, primary);
        JButton closeBtn = createButton("Fechar", new Color(243, 244, 246), new Color(55, 65, 81), border);

        testBtn.addActionListener(e -> testConnection());
        saveBtn.addActionListener(e -> saveValues());
        colsBtn.addActionListener(e -> openColumns());
        closeBtn.addActionListener(e -> dispose());

        buttons.add(colsBtn);
        buttons.add(testBtn);
        buttons.add(saveBtn);
        buttons.add(closeBtn);

        content.add(buttons, BorderLayout.SOUTH);

        setContentPane(content);
    }

    private void loadValues() {
        try {
            if (Files.exists(Paths.get(".env"))) {
                Dotenv dotenv = Dotenv.load();
                String t = dotenv.get("TOKEN");
                if (t != null)
                    tokenField.setText(t);
                String b = dotenv.get("BASE_URL");
                if (b != null)
                    baseUrlField.setText(b);
                String d = dotenv.get("DELAY_TIME");
                if (d != null) {
                    try {
                        long delayMs = Long.parseLong(d);
                        // Convert milliseconds to slider value (5-20)
                        // Formula: slider = (1300 - delay_ms) / 60
                        int sliderValue = (int) ((1300 - delayMs) / 60.0);
                        sliderValue = Math.max(5, Math.min(20, sliderValue));
                        delaySlider.setValue(sliderValue);
                    } catch (NumberFormatException e) {
                        delaySlider.setValue(10); // default 1.0x
                    }
                } else {
                    delaySlider.setValue(10); // default 1.0x
                }
            } else {
                delaySlider.setValue(10); // default 1.0x
            }
        } catch (Exception e) {
            statusLabel.setText("Erro ao carregar: " + e.getMessage());
            delaySlider.setValue(10); // default 1.0x
        }
    }

    private void updateDelayLabel() {
        int sliderValue = delaySlider.getValue();
        double multiplier = sliderValue / 10.0;
        delayValueLabel.setText(String.format("%.1fx", multiplier));
    }

    private void saveValues() {
        try {
            StringBuilder env = new StringBuilder();
            env.append("TOKEN=").append(new String(tokenField.getText())).append("\n");
            String baseUrl = baseUrlField.getText().trim();
            if (baseUrl.startsWith("http://") || baseUrl.startsWith("https://")) {
                env.append("BASE_URL=").append(baseUrl).append("\n");
            } else {
                JOptionPane.showMessageDialog(this, "BASE_URL deve começar com http:// ou https://",
                        "Erro de Validação", JOptionPane.ERROR_MESSAGE);
                throw new IllegalArgumentException("BASE_URL deve começar com http:// ou https://");
            }

            // Convert slider value (5-20) to delay time in milliseconds
            // Formula: delay_ms = 1300 - 60 * slider_value
            // 0.5x (5) = 1000ms, 1.0x (10) = 700ms, 2.0x (20) = 100ms
            int sliderValue = delaySlider.getValue();
            long delayMs = 1300 - (sliderValue * 60);
            env.append("DELAY_TIME=").append(delayMs).append("\n");

            Files.write(Paths.get(".env"), env.toString().getBytes());
            statusLabel.setText("✅ Salvo");
            statusLabel.setForeground(new Color(40, 167, 69));
        } catch (IOException ex) {
            statusLabel.setText("❌ Erro ao salvar");
            statusLabel.setForeground(new Color(220, 53, 69));
        }
    }

    private void testConnection() {
        statusLabel.setText("Testando...");
        statusLabel.setForeground(new Color(255, 165, 0));
        new Thread(() -> {
            try {
                ApiClient api = new ApiClient(baseUrlField.getText());
                String token = new String(tokenField.getText());
                boolean ok = api.authenticateWithToken(token);
                boolean conn = ok && api.testConnection();
                SwingUtilities.invokeLater(() -> {
                    if (ok && conn) {
                        statusLabel.setText("✅ Conexão OK");
                        statusLabel.setForeground(new Color(40, 167, 69));
                    } else {
                        statusLabel.setText("❌ Falha na conexão/autenticação");
                        statusLabel.setForeground(new Color(220, 53, 69));
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("❌ Erro: " + ex.getMessage());
                    statusLabel.setForeground(new Color(220, 53, 69));
                });
            }
        }).start();
    }

    private void openColumns() {
        // Hide this modal dialog before opening the column manager so the
        // new window is interactive (modal dialogs block input to other windows).
        this.setVisible(false);
        ColumnManagerWindow cols = new ColumnManagerWindow();
        cols.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                // When the columns window closes, re-show this settings dialog.
                SwingUtilities.invokeLater(() -> ConfigWindow.this.setVisible(true));
            }
        });
        cols.setVisible(true);
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
