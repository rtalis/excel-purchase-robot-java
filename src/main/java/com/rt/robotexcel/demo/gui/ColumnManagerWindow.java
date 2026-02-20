package com.rt.robotexcel.demo.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import com.formdev.flatlaf.FlatLightLaf;
import com.rt.robotexcel.demo.config.ConfigurationManager;
import com.rt.robotexcel.demo.config.ExcelColumnConfig;

public class ColumnManagerWindow extends JFrame {

    private final Color surface = new Color(248, 250, 252);
    private final Color border = new Color(225, 229, 235);
    private final Color textPrimary = new Color(55, 65, 81);
    private final Color muted = new Color(90, 102, 117);
    private final Color primary = new Color(52, 120, 246);

    private DefaultTableModel tableModel;
    private JTable table;
    private DefaultListModel<String> availableModel;
    private JList<String> availableList;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }
        FlatLightLaf.setup();

        ColumnManagerWindow window = new ColumnManagerWindow();
        window.setVisible(true);
        window.setLocationRelativeTo(null);
    }

    public ColumnManagerWindow() {
        setTitle("Gerenciador de Colunas");
        setSize(960, 620);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
        loadSavedConfiguration();
    }

    private void initUI() {
        JPanel content = new JPanel(new BorderLayout(14, 14));
        content.setBackground(surface);
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        content.add(createHeader(), BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(surface);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        center.add(createAvailableCard(), gbc);

        gbc.gridx = 1;
        center.add(createSelectedCard(), gbc);

        content.add(center, BorderLayout.CENTER);
        content.add(createFooter(), BorderLayout.SOUTH);

        setContentPane(content);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(surface);

        JLabel title = new JLabel("Configurar colunas da planilha");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(textPrimary);
        JLabel subtitle = new JLabel("Escolha, ordene e mapeie campos antes de rodar o robô");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(muted);

        JPanel texts = new JPanel(new BorderLayout());
        texts.setBackground(surface);
        texts.add(title, BorderLayout.NORTH);
        texts.add(subtitle, BorderLayout.CENTER);

        header.add(texts, BorderLayout.WEST);
        return header;
    }

    private JPanel createAvailableCard() {
        JPanel card = createCardPanel("Colunas disponíveis");
        card.setLayout(new BorderLayout(8, 8));

        availableModel = new DefaultListModel<>();
        for (String col : defaultColumns()) {
            availableModel.addElement(col);
        }
        availableList = new JList<>(availableModel);
        availableList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        availableList.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        card.add(new JScrollPane(availableList), BorderLayout.CENTER);

        JLabel hint = new JLabel("Selecione e clique em Adicionar");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setForeground(muted);
        card.add(hint, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createSelectedCard() {
        JPanel card = createCardPanel("Colunas selecionadas (ordem na planilha)");
        card.setLayout(new BorderLayout(8, 8));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);

        JButton addBtn = createButton("Adicionar", primary, Color.WHITE, primary);
        JButton removeBtn = createButton("Remover", new Color(243, 244, 246), textPrimary, border);
        JButton upBtn = createButton("↑", new Color(243, 244, 246), textPrimary, border);
        JButton downBtn = createButton("↓", new Color(243, 244, 246), textPrimary, border);
        JButton blankBtn = createButton("+ Coluna em branco", new Color(243, 244, 246), textPrimary, border);

        toolbar.add(addBtn);
        toolbar.add(removeBtn);
        toolbar.add(upBtn);
        toolbar.add(downBtn);
        toolbar.add(blankBtn);

        tableModel = new DefaultTableModel(new String[] { "Coluna", "Campo JSON" }, 0);
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        addBtn.addActionListener(e -> addSelected());
        removeBtn.addActionListener(e -> removeSelected());
        upBtn.addActionListener(e -> moveRow(-1));
        downBtn.addActionListener(e -> moveRow(1));
        blankBtn.addActionListener(e -> addBlank());

        card.add(toolbar, BorderLayout.NORTH);
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(surface);

        JButton helpBtn = createButton("Ajuda", new Color(243, 244, 246), textPrimary, border);
        helpBtn.addActionListener(e -> showHelp());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton saveBtn = createButton("Salvar", primary, Color.WHITE, primary);
        JButton closeBtn = createButton("Fechar", new Color(243, 244, 246), textPrimary, border);

        saveBtn.addActionListener(e -> saveConfiguration());
        closeBtn.addActionListener(e -> dispose());

        footer.add(helpBtn, BorderLayout.WEST);
        actions.add(saveBtn);
        actions.add(closeBtn);
        footer.add(actions, BorderLayout.EAST);
        return footer;
    }

    private void addSelected() {
        String selected = availableList.getSelectedValue();
        if (selected == null)
            return;

        String jsonField = getJsonFieldForColumn(selected);
        tableModel.addRow(new Object[] { selected, jsonField });

        if (!"[COLUNA EM BRANCO]".equals(selected)) {
            availableModel.removeElement(selected);
        }
    }

    private void removeSelected() {
        int row = table.getSelectedRow();
        if (row < 0)
            return;

        String column = (String) tableModel.getValueAt(row, 0);
        tableModel.removeRow(row);

        if (!"[COLUNA EM BRANCO]".equals(column) && !availableModel.contains(column)) {
            availableModel.addElement(column);
        }
    }

    private void moveRow(int direction) {
        int row = table.getSelectedRow();
        if (row < 0)
            return;
        int target = row + direction;
        if (target < 0 || target >= tableModel.getRowCount())
            return;

        tableModel.moveRow(row, row, target);
        table.setRowSelectionInterval(target, target);
    }

    private void addBlank() {
        tableModel.addRow(new Object[] { "[COLUNA EM BRANCO]", "" });
    }

    private void loadSavedConfiguration() {
        List<ExcelColumnConfig> savedConfigs = ConfigurationManager.loadConfiguration();
        if (savedConfigs == null || savedConfigs.isEmpty())
            return;

        tableModel.setRowCount(0);
        availableModel.removeAllElements();
        for (String col : defaultColumns()) {
            availableModel.addElement(col);
        }

        for (ExcelColumnConfig config : savedConfigs) {
            tableModel.addRow(new Object[] { config.getDisplayName(), config.getJsonField() });
            if (!"[COLUNA EM BRANCO]".equals(config.getDisplayName())) {
                availableModel.removeElement(config.getDisplayName());
            }
        }
    }

    private void saveConfiguration() {
        List<ExcelColumnConfig> configs = new ArrayList<>();
        int pedidoIndex = -1;

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if ("PEDIDO".equals(tableModel.getValueAt(i, 0))) {
                pedidoIndex = i;
                break;
            }
        }

        if (pedidoIndex == -1) {
            JOptionPane.showMessageDialog(this, "A coluna PEDIDO é obrigatória!", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String displayName = (String) tableModel.getValueAt(i, 0);
            String jsonField = (String) tableModel.getValueAt(i, 1);
            int position = i - pedidoIndex;
            configs.add(new ExcelColumnConfig(displayName, jsonField, position));
        }

        ConfigurationManager.saveConfiguration(configs);
        JOptionPane.showMessageDialog(this, "Configuração salva com sucesso!", "Sucesso",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showHelp() {
        String msg = "1) Selecione uma coluna disponível e clique em Adicionar.\n" +
                "2) Use ↑ e ↓ para ordenar conforme a planilha.\n" +
                "3) PEDIDO precisa existir; outras são opcionais.\n" +
                "4) Campos vazios podem ser colunas em branco.\n" +
                "5) Clique em Salvar para gravar a configuração.";
        JOptionPane.showMessageDialog(this, msg, "Ajuda", JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel createCardPanel(String title) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(8, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border, 1, true),
                new EmptyBorder(14, 14, 14, 14)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(textPrimary);
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

    private List<String> defaultColumns() {
        List<String> cols = new ArrayList<>();
        cols.add("SOLIC.");
        cols.add("PEDIDO");
        cols.add("Cod. Empresa");
        cols.add("FORNECEDOR");
        cols.add("DT. PED.");
        cols.add("VALOR ITENS");
        cols.add("VALOR TOTAL COM DESC.");
        cols.add("NF");
        cols.add("CHEGADA");
        cols.add("CONTATO");
        cols.add("GENERO");
        cols.add("PAGAMENTO");
        cols.add("FUNCIONÁRIO");
        cols.add("POSIÇÃO");
        cols.add("VALOR LÍQUIDO");
        cols.add("VALOR LÍQUIDO IPI");
        cols.add("EMPRESA");
        cols.add("ID FORNECEDOR");
        cols.add("ID PEDIDO");
        cols.add("OBSERVAÇÃO");
        cols.add("[COLUNA EM BRANCO]");
        cols.add("VALOR NOTA FISCAL");
        cols.add("TRANSPORTADORA");
        return cols;
    }

    private String getJsonFieldForColumn(String column) {
        switch (column) {
            case "PEDIDO":
                return "cod_pedc";
            case "Cod. Empresa":
                return "cod_emp1_source";
            case "FORNECEDOR":
                return "fornecedor_descricao";
            case "DT. PED.":
                return "dt_emis";
            case "VALOR ITENS":
                return "total_bruto";
            case "VALOR TOTAL COM DESC.":
                return "adjusted_total";
            case "NF":
                return "nfes[0].num_nf";
            case "CHEGADA":
                return "nfes[0].dt_ent";
            case "SOLIC.":
                return "observacao (extrair número)";
            case "GENERO":
                return "genero (extrair do final da observação)";
            case "CONTATO":
                return "contato";
            case "PAGAMENTO":
                return "cf_pgto";
            case "FUNCIONÁRIO":
                return "func_nome";
            case "POSIÇÃO":
                return "posicao";
            case "VALOR LÍQUIDO":
                return "total_liquido";
            case "VALOR LÍQUIDO IPI":
                return "total_liquido_ipi";
            case "EMPRESA":
                return "cod_emp1";
            case "ID FORNECEDOR":
                return "fornecedor_id";
            case "ID PEDIDO":
                return "order_id";
            case "OBSERVAÇÃO":
                return "observacao";
            case "VALOR NOTA FISCAL":
                return "nfe_valor";
            case "TRANSPORTADORA":
                return "nfe_transportadora";
            default:
                return "";
        }
    }
}
