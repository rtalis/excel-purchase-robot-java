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
    private JLabel statusLabel;

    public ConfigWindow(Frame owner) {
        super(owner, "Configuração", true);
        initUI();
        loadValues();
        setResizable(false);
        setSize(600, 260);
        setLocationRelativeTo(null);
    }

    private void initUI() {
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(12, 12, 12, 12));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.25;
        JLabel tokenLabel = new JLabel("Chave (TOKEN):");
        tokenLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        content.add(tokenLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.75;
        tokenField = new JTextField();
        tokenField.setPreferredSize(new Dimension(320, 30));
        content.add(tokenField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.25;
        JLabel urlLabel = new JLabel("Site (BASE_URL):");
        urlLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        content.add(urlLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.75;
        baseUrlField = new JTextField();
        baseUrlField.setPreferredSize(new Dimension(320, 30));
        content.add(baseUrlField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        statusLabel = new JLabel("");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusLabel.setHorizontalAlignment(JLabel.CENTER);
        content.add(statusLabel, gbc);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        buttons.setBackground(Color.WHITE);

        JButton testBtn = new JButton("🔌 Testar");
        JButton saveBtn = new JButton("💾 Salvar");
        JButton colsBtn = new JButton("⚙️ Colunas");
        JButton closeBtn = new JButton("Fechar");

        testBtn.addActionListener(e -> testConnection());
        saveBtn.addActionListener(e -> saveValues());
        colsBtn.addActionListener(e -> openColumns());
        closeBtn.addActionListener(e -> dispose());

        buttons.add(testBtn);
        buttons.add(saveBtn);
        buttons.add(colsBtn);
        buttons.add(closeBtn);

        gbc.gridy = 3;
        gbc.gridwidth = 2;
        content.add(buttons, gbc);

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
            }
        } catch (Exception e) {
            statusLabel.setText("Erro ao carregar: " + e.getMessage());
        }
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

}
