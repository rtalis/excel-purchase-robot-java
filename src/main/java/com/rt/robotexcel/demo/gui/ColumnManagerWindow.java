package com.rt.robotexcel.demo.gui;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.table.*;

import com.formdev.flatlaf.FlatLightLaf;
import com.rt.robotexcel.demo.config.ConfigurationManager;
import com.rt.robotexcel.demo.config.ExcelColumnConfig;

public class ColumnManagerWindow extends JFrame {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch(Exception ex) {
            System.err.println("Failed to initialize LaF");
        }
        FlatLightLaf.setup();

        ColumnManagerWindow window = new ColumnManagerWindow();
        window.setVisible(true);
    }
    
    private DefaultTableModel tableModel;
    private JTable table;
    private ArrayList<String> availableColumns;
    private ArrayList<String> selectedColumns;
    private JList<String> availableList;

    public ColumnManagerWindow() {
        setTitle("Gerenciador de Colunas");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Inicializa as listas
        availableColumns = new ArrayList<>();
        initializeAvailableColumns();
        
        selectedColumns = new ArrayList<>();
  
        // Inicializa componentes da interface
        initializeComponents();
        
        // Agora podemos carregar a configuração salva
        loadSavedConfiguration();
    }
    
    private void initializeAvailableColumns() {
        availableColumns.add("SOLIC.");
        availableColumns.add("PEDIDO");
        availableColumns.add("Cod. Empresa");
        availableColumns.add("FORNECEDOR");
        availableColumns.add("DT. PED.");
        availableColumns.add("VALOR ITENS");
        availableColumns.add("VALOR TOTAL COM DESC.");
        availableColumns.add("NF");
        availableColumns.add("CHEGADA");
        availableColumns.add("CONTATO");
        availableColumns.add("GENERO");
        availableColumns.add("PAGAMENTO");
        availableColumns.add("FUNCIONÁRIO");
        availableColumns.add("POSIÇÃO");
        availableColumns.add("VALOR LÍQUIDO");
        availableColumns.add("VALOR LÍQUIDO IPI");
        availableColumns.add("EMPRESA");
        availableColumns.add("ID FORNECEDOR");
        availableColumns.add("ID PEDIDO");
        availableColumns.add("OBSERVAÇÃO");
        availableColumns.add("[COLUNA EM BRANCO]");
        availableColumns.add("VALOR NOTA FISCAL");
        availableColumns.add("TRANSPORTADORA");

    }

    private void initializeComponents() {
        // Cria o layout
        setLayout(new BorderLayout());
        
        // Painel superior com botões
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Adicionar");
        JButton removeButton = new JButton("Remover");
        JButton moveUpButton = new JButton("↑");
        JButton moveDownButton = new JButton("↓");
        JButton saveButton = new JButton("Salvar");
        JButton addBlankButton = new JButton("+ Coluna em Branco");
        
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(moveUpButton);
        buttonPanel.add(moveDownButton);
        buttonPanel.add(addBlankButton);
        buttonPanel.add(saveButton);
        
        // Lista de colunas disponíveis
        availableList = new JList<>(availableColumns.toArray(new String[0]));
        availableList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Tabela de colunas selecionadas
        tableModel = new DefaultTableModel(new String[]{"Coluna", "Campo JSON"}, 0);
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Layout
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            new JScrollPane(availableList),
            new JScrollPane(table));
        
        add(buttonPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        
        // Configuração dos eventos
        setupEventListeners(addButton, removeButton, moveUpButton, moveDownButton, saveButton, addBlankButton);
    }

    private void setupEventListeners(JButton addButton, JButton removeButton, 
                                   JButton moveUpButton, JButton moveDownButton, 
                                   JButton saveButton, JButton addBlankButton) {
        addButton.addActionListener(e -> {
            String selected = availableList.getSelectedValue();
            if (selected != null) {
                String jsonField = getJsonFieldForColumn(selected);
                tableModel.addRow(new Object[]{selected, jsonField});
                selectedColumns.add(selected);
                refreshAvailableList(availableList);
            }
        });
        
        removeButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String column = (String) tableModel.getValueAt(row, 0);
                tableModel.removeRow(row);
                selectedColumns.remove(column);
                // Só adiciona de volta à lista de disponíveis se não for COLUNA EM BRANCO
                if (!column.equals("[COLUNA EM BRANCO]")) {
                    availableColumns.add(column);
                }
                refreshAvailableList(availableList);
            }
        });
        
        // Botão adicional para adicionar colunas em branco
        addBlankButton.addActionListener(e -> {
            tableModel.addRow(new Object[]{"[COLUNA EM BRANCO]", ""});
            selectedColumns.add("[COLUNA EM BRANCO]");
            // Não remove da lista de disponíveis para permitir adicionar várias
        });
        
        moveUpButton.addActionListener(e -> moveRow(-1));
        moveDownButton.addActionListener(e -> moveRow(1));
        
        saveButton.addActionListener(e -> saveConfiguration());
    }

    private void loadSavedConfiguration() {
        List<ExcelColumnConfig> savedConfigs = ConfigurationManager.loadConfiguration();
        if (savedConfigs != null && !savedConfigs.isEmpty()) {
            // Limpa a tabela
            tableModel.setRowCount(0);
            selectedColumns.clear();
            
            for (ExcelColumnConfig config : savedConfigs) {
                tableModel.addRow(new Object[]{config.getDisplayName(), config.getJsonField()});
                selectedColumns.add(config.getDisplayName());
                
                // Remove da lista de disponíveis apenas se não for COLUNA EM BRANCO
                if (!config.getDisplayName().equals("[COLUNA EM BRANCO]") && 
                    availableColumns.contains(config.getDisplayName())) {
                    availableColumns.remove(config.getDisplayName());
                }
            }
            
            // Atualiza a lista de colunas disponíveis
            refreshAvailableList(availableList);
        }
    }
    
    private String getJsonFieldForColumn(String column) {
        switch (column) {
            case "PEDIDO": return "cod_pedc";
            case "Cod. Empresa": return "cod_emp1_source";
            case "FORNECEDOR": return "fornecedor_descricao";
            case "DT. PED.": return "dt_emis";
            case "VALOR ITENS": return "total_bruto";
            case "VALOR TOTAL COM DESC.": return "adjusted_total";
            case "NF": return "nfes[0].num_nf";
            case "CHEGADA": return "nfes[0].dt_ent";
            case "SOLIC.": return "observacao (extrair número)";
            case "GENERO": return "genero (extrair do final da observação)";
            case "CONTATO": return "contato";
            case "PAGAMENTO": return "cf_pgto";
            case "FUNCIONÁRIO": return "func_nome";
            case "POSIÇÃO": return "posicao";
            case "VALOR LÍQUIDO": return "total_liquido";
            case "VALOR LÍQUIDO IPI": return "total_liquido_ipi";
            case "EMPRESA": return "cod_emp1";
            case "ID FORNECEDOR": return "fornecedor_id";
            case "ID PEDIDO": return "order_id";
            case "OBSERVAÇÃO": return "observacao";
            case "VALOR NOTA FISCAL": return "nfe_valor";
            case "TRANSPORTADORA": return "nfe_transportadora";
            default: return "";
        }
    }
    
    private void refreshAvailableList(JList<String> list) {
        // Filtramos a lista excluindo colunas já selecionadas,
        // exceto [COLUNA EM BRANCO] que deve sempre aparecer
        ArrayList<String> available = new ArrayList<>();
        for (String column : availableColumns) {
            if (!selectedColumns.contains(column) || column.equals("[COLUNA EM BRANCO]")) {
                available.add(column);
            }
        }
        list.setListData(available.toArray(new String[0]));
    }
    
    private void moveRow(int direction) {
        int row = table.getSelectedRow();
        if (row < 0) return;
        
        int newRow = row + direction;
        if (newRow < 0 || newRow >= tableModel.getRowCount()) return;
        
        // Move na tabela
        tableModel.moveRow(row, row, newRow);
        
        // Move na lista de selecionados
        String temp = selectedColumns.get(row);
        selectedColumns.set(row, selectedColumns.get(newRow));
        selectedColumns.set(newRow, temp);
        
        // Mantém a seleção
        table.setRowSelectionInterval(newRow, newRow);
    }
    
    private void saveConfiguration() {
        List<ExcelColumnConfig> configs = new ArrayList<>();
        int pedidoIndex = -1;
        
        // Encontra índice do PEDIDO
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 0).equals("PEDIDO")) {
                pedidoIndex = i;
                break;
            }
        }
        
        if (pedidoIndex == -1) {
            JOptionPane.showMessageDialog(this, "A coluna PEDIDO é obrigatória!");
            return;
        }
        
        // Cria configurações com posições relativas ao PEDIDO
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String displayName = (String) tableModel.getValueAt(i, 0);
            String jsonField = (String) tableModel.getValueAt(i, 1);
            int position = i - pedidoIndex;
            
            configs.add(new ExcelColumnConfig(displayName, jsonField, position));
        }
        
        // Salva apenas o arquivo de configuração
        ConfigurationManager.saveConfiguration(configs);
        
        JOptionPane.showMessageDialog(this, "Configuração salva com sucesso!");
    }
}