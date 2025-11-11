package igu;
import com.mycompany.proyecto2.Directory;
import com.mycompany.proyecto2.File;
import com.mycompany.proyecto2.FileSystem;
import com.mycompany.proyecto2.StorageDisk;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author beacardozo
 */
public class MainView extends javax.swing.JFrame {
    private SDBlockPanel sdBlocksPanel;
    private JPanel diskStatusArea; 
    private String[] blocks; 
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;
    private StorageDisk storage;
    private FileSystem fileSystem;

    /**
     * Creates new form MainView
     */
    public MainView() {
        initComponents();
        fileSystem = new FileSystem(64, 100, 100);
        storage = new StorageDisk(64);
        rootNode = new DefaultMutableTreeNode(new Directory("Root")); 
        treeModel = new DefaultTreeModel(rootNode); 
        StructureJTree.setModel(treeModel);


        blocks = new String[64]; 
        Color[] blockColors = new Color[64];
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = null;
            blockColors[i] = Color.GREEN;
        }
        sdBlocksPanel = new SDBlockPanel(blocks,blockColors); 
        diskStatusArea = DiskStatusAreaPanel; 
        diskStatusArea.setLayout(new BorderLayout()); 
        diskStatusArea.add(sdBlocksPanel, BorderLayout.CENTER); 
        
        pack();
        setVisible(true);
        setupJTreeSelectionListener();
        setupModeSelection();
        updateFieldsByComboBox();
        appendToDetails("FileSystem successfully started | Date: " + getTimeStamp());
    }
    
    public void updateDiskStatus(String[] newBlocks, Color[] newBlockColors) {
    sdBlocksPanel.updateBlocks(newBlocks, newBlockColors); 
}
    
    private void clearJTree() {
    rootNode.removeAllChildren(); 
    treeModel.reload(); 
    StructureJTree.setModel(treeModel);

    fileSystem.clear(); 

    storage.clear(); 

    Color[] blockColors = new Color[64]; 
    for (int i = 0; i < blockColors.length; i++) {
        blockColors[i] = Color.GREEN; 
    }

    updateDiskStatus(storage.getBlocks(), blockColors); 

    appendToDetails("FileSystem & StorageDisk successfully cleaned | Date: " + getTimeStamp());
}

    private void clearFieldsAndComboBox() {
        ObjectNameTextField.setText(""); 
        ObjectSizeTextField.setText(""); 
        ObjectTypeComboBox.setSelectedIndex(0); 
    }
    
    private void setupJTreeSelectionListener() {
        StructureJTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) StructureJTree.getLastSelectedPathComponent();

                if (selectedNode != null && selectedNode.getUserObject() != null) {
                    Object userObject = selectedNode.getUserObject();

                    if (userObject instanceof Directory) {
                        SelectedObjectTypeTextField.setText("Directory");
                        SelectedObjectTypeTextField.setEnabled(false);
                        SelectedObjectNameTextField.setText(((Directory) userObject).getName());
                        SelectedObjectSizeTextField.setVisible(false); 
                        SelectedObjectSizeText.setVisible(false);
                    } else if (userObject instanceof File) {
                        File selectedFile = (File) userObject;
                        SelectedObjectTypeTextField.setText("File");
                        SelectedObjectNameTextField.setText(selectedFile.getName());
                        SelectedObjectSizeTextField.setVisible(true); 
                        SelectedObjectSizeText.setVisible(true);
                        SelectedObjectSizeTextField.setText(String.valueOf(selectedFile.getSize()));
                    }
                } else {
                    SelectedObjectTypeTextField.setText("");
                    SelectedObjectNameTextField.setText("");
                    SelectedObjectSizeTextField.setText("");
                }
            }   
        });
    }
    
    private void setupModeSelection() {
        AdminModeRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleControlPanel(true);
                appendToDetails("Set to Administrator Mode | Date: " + getTimeStamp());
            }
        });
        
        UserModeRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleControlPanel(false);
                appendToDetails("Set to User Mode | Date: " + getTimeStamp());
            }
        });
    }
    
    private void toggleControlPanel(boolean enabled) {
        ControlPanel.setEnabled(enabled);
        for (java.awt.Component c : ControlPanel.getComponents()) {
            c.setEnabled(enabled);
        }
    }
    
    private void updateFieldsByComboBox() {
        ObjectTypeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ObjectTypeComboBox.getSelectedItem().equals("Directory")) {
                    ObjectSizeText.setVisible(false);
                    ObjectSizeTextField.setVisible(false);
                } else {
                    ObjectSizeText.setVisible(true);
                    ObjectSizeTextField.setVisible(true);
                }
            }
        });
    }
    
    private void createAndShowTable() {
        String[] columnNames = {"Color", "File Name", "Assigned Blocks", "First Block Direction"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);

        for (int i = 0; i < fileSystem.fileCount; i++) {
            File file = fileSystem.files[i];
            if (file != null) {
                Object[] rowData = new Object[4];
                Color color = file.getFileColor();

                JPanel colorPanel = new JPanel();
                colorPanel.setBackground(color);
                colorPanel.setPreferredSize(new Dimension(20, 20));

                rowData[0] = colorPanel; 
                rowData[1] = file.getName(); 
                rowData[2] = file.getSize(); 
                rowData[3] = file.getFirstBlock(); 

                tableModel.addRow(rowData);
            }
        }

        AllocationJTable.setModel(tableModel);
        AllocationJTable.getColumnModel().getColumn(0).setCellRenderer(new ColorCellRenderer());
    }

    class ColorCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof JPanel) {
                JPanel panel = (JPanel) value;
                panel.setOpaque(true);
                panel.setBorder(isSelected ? BorderFactory.createLineBorder(Color.BLUE) : BorderFactory.createLineBorder(Color.BLACK));
                return panel;
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
    
    public void appendToDetails(String text) {
        DetailsTextArea.append(text + "\n"); 
        DetailsTextArea.setCaretPosition(DetailsTextArea.getDocument().getLength()); 
    }
    
    public String getTimeStamp() {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(new Date());
        return timestamp;
    }

   public void saveTreeToTXT(Object node, String filePath, String indent) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            saveNodeToTXT(node, writer, indent);
            System.out.println("Estructura guardada en " + filePath); 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

   public void saveNodeToTXT(Object node, BufferedWriter writer, String indent) throws IOException {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
        Object userObject = treeNode.getUserObject();

        if (userObject instanceof File) {
            File file = (File) userObject;
            writer.write(indent + "Name: " + file.getName() + 
                     ", Size: " + file.getSize() + 
                     ", type: file" 
                     );
        } else if (userObject instanceof Directory) {
            Directory directory = (Directory) userObject;
            writer.write(indent + "Name: " + directory.getName() + "/" +
                       ", type: directory");
        }
        writer.newLine();

        for (int i = 0; i < treeNode.getChildCount(); i++) {
            saveNodeToTXT(treeNode.getChildAt(i), writer, indent + "  ");
        }
    }
   
public void loadTreeFromTXT(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Root");

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Name: Root")) {
                    rootNode = new DefaultMutableTreeNode(new Directory("Root"));
                } else {
                    addNodeToTree(rootNode, line.trim());
                }
            }

            StructureJTree.setModel(new DefaultTreeModel(rootNode));
            JOptionPane.showMessageDialog(null, "Structure loaded from " + filePath, "Load Confirmation", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addNodeToTree(DefaultMutableTreeNode parentNode, String line) {
        line = line.trim();
        String[] parts = line.split(", ");
        String name = parts[0].split(": ")[1]; 

    
        if (line.contains("type: directory")) {
            Directory newDir = new Directory(name.substring(0, name.length() - 1)); // Eliminar el '/' final
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newDir);
            parentNode.add(newNode);
            Directory currentDir = (Directory) ((DefaultMutableTreeNode) parentNode).getUserObject();
            currentDir.addDirectory(newDir); 
        } else if (line.contains("type: file")) {
            int size = extractSize(parts);
            if (size <= 0) {
                JOptionPane.showMessageDialog(null, "File size must be greater than zero.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) parentNode.getParent();
            if (size > storage.getAvailableBlocks()) {
                JOptionPane.showMessageDialog(null, "StorageDisk does not have enough space to store this file.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Color fileColor = new Color((int)(Math.random() * 0x1000000));
            File newFile = new File(null, name, size, storage.getFirstBlock(), fileColor); 

            if (storage.allocateBlocks(newFile.getName(), size, newFile.getFileColor())) {
                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newFile);
                parentNode.add(newNode);

                Directory currentDir = (Directory) ((DefaultMutableTreeNode) parentNode).getUserObject();
                currentDir.addFile(newFile); 
                fileSystem.createFile(currentDir, name, size, storage.getFirstBlock(), fileColor); 

            updateDiskStatus(storage.getBlocks(), storage.getBlockColors());
            createAndShowTable();
        } else {
            JOptionPane.showMessageDialog(null, "Failed to allocate blocks for the file.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }
}

private int extractSize(String[] parts) {
    for (String part : parts) {
        if (part.startsWith("Size: ")) {
            return Integer.parseInt(part.split(": ")[1]); 
        }
    }
    return 0; 
} 
   



    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        ModeButtonGroup = new javax.swing.ButtonGroup();
        MainPanel = new javax.swing.JPanel();
        JTreePanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        StructureJTree = new javax.swing.JTree();
        SaveTxtFile = new javax.swing.JButton();
        ClearJTreeButton = new javax.swing.JButton();
        LoadTxtButton = new javax.swing.JButton();
        FileAllocationTablePanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        AllocationJTable = new javax.swing.JTable();
        ControlPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        ObjectSizeText = new javax.swing.JLabel();
        CreateElementButton = new javax.swing.JButton();
        UpdateElementButton = new javax.swing.JButton();
        DeleteElementButton = new javax.swing.JButton();
        ObjectTypeComboBox = new javax.swing.JComboBox<>();
        ObjectNameTextField = new javax.swing.JTextField();
        ObjectSizeTextField = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        SelectedObjectSizeText = new javax.swing.JLabel();
        SelectedObjectTypeTextField = new javax.swing.JTextField();
        SelectedObjectSizeTextField = new javax.swing.JTextField();
        SelectedObjectNameTextField = new javax.swing.JTextField();
        DiskStatusAreaPanel = new javax.swing.JPanel();
        DetailsAreaPanel = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        DetailsTextArea = new javax.swing.JTextArea();
        AdminModeRadioButton = new javax.swing.JRadioButton();
        UserModeRadioButton = new javax.swing.JRadioButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        MainPanel.setBackground(new java.awt.Color(255, 255, 255));
        MainPanel.setMinimumSize(new java.awt.Dimension(1300, 725));
        MainPanel.setPreferredSize(new java.awt.Dimension(1300, 725));
        MainPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        JTreePanel.setBackground(new java.awt.Color(255, 255, 255));
        JTreePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "File and Directory Structure", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Geeza Pro", 3, 13))); // NOI18N
        JTreePanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jScrollPane1.setViewportView(StructureJTree);

        JTreePanel.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 420, 220));

        SaveTxtFile.setBackground(new java.awt.Color(169, 217, 241));
        SaveTxtFile.setFont(new java.awt.Font("Geneva", 1, 11)); // NOI18N
        SaveTxtFile.setText("Save to TXT");
        SaveTxtFile.setBorderPainted(false);
        SaveTxtFile.setOpaque(true);
        SaveTxtFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaveTxtFileActionPerformed(evt);
            }
        });
        JTreePanel.add(SaveTxtFile, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 270, 120, 30));

        ClearJTreeButton.setBackground(new java.awt.Color(204, 0, 0));
        ClearJTreeButton.setFont(new java.awt.Font("Geneva", 1, 11)); // NOI18N
        ClearJTreeButton.setForeground(new java.awt.Color(255, 255, 255));
        ClearJTreeButton.setText("Clear");
        ClearJTreeButton.setBorderPainted(false);
        ClearJTreeButton.setOpaque(true);
        ClearJTreeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ClearJTreeButtonActionPerformed(evt);
            }
        });
        JTreePanel.add(ClearJTreeButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 270, 110, 30));

        LoadTxtButton.setBackground(new java.awt.Color(169, 217, 241));
        LoadTxtButton.setFont(new java.awt.Font("Geneva", 1, 11)); // NOI18N
        LoadTxtButton.setText("Load TXT");
        LoadTxtButton.setBorderPainted(false);
        LoadTxtButton.setOpaque(true);
        LoadTxtButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LoadTxtButtonActionPerformed(evt);
            }
        });
        JTreePanel.add(LoadTxtButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 270, 120, 30));

        MainPanel.add(JTreePanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 460, 320));

        FileAllocationTablePanel.setBackground(new java.awt.Color(255, 255, 255));
        FileAllocationTablePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "File Allocation Table", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Geeza Pro", 3, 13))); // NOI18N
        FileAllocationTablePanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        AllocationJTable.setAutoCreateRowSorter(true);
        AllocationJTable.setFont(new java.awt.Font("Geneva", 1, 12)); // NOI18N
        AllocationJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Color", "File Name", "Asigned Blocks", "First Block Direction"
            }
        ));
        AllocationJTable.setSelectionBackground(new java.awt.Color(169, 217, 241));
        AllocationJTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane3.setViewportView(AllocationJTable);

        FileAllocationTablePanel.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 30, 730, 160));

        MainPanel.add(FileAllocationTablePanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 10, 790, 210));

        ControlPanel.setBackground(new java.awt.Color(255, 255, 255));
        ControlPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Control Panel", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Geeza Pro", 3, 13))); // NOI18N
        ControlPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Geeza Pro", 3, 12)); // NOI18N
        jLabel1.setText("CREATE");
        ControlPanel.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, 250, -1));

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
        ControlPanel.add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 20, 20, 190));

        jLabel2.setFont(new java.awt.Font("Geeza Pro", 3, 12)); // NOI18N
        jLabel2.setText("READ, UPDATE & DELETE");
        ControlPanel.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 20, 320, -1));

        jLabel4.setFont(new java.awt.Font("Geneva", 2, 13)); // NOI18N
        jLabel4.setText("Select an Object type:");
        ControlPanel.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 40, 170, -1));

        jLabel5.setFont(new java.awt.Font("Geneva", 2, 13)); // NOI18N
        jLabel5.setText("Object Name:");
        ControlPanel.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 90, -1, -1));

        ObjectSizeText.setFont(new java.awt.Font("Geneva", 2, 13)); // NOI18N
        ObjectSizeText.setText("Object Size:");
        ControlPanel.add(ObjectSizeText, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 140, -1, -1));

        CreateElementButton.setBackground(new java.awt.Color(0, 204, 0));
        CreateElementButton.setFont(new java.awt.Font("Geneva", 1, 11)); // NOI18N
        CreateElementButton.setForeground(new java.awt.Color(255, 255, 255));
        CreateElementButton.setText("Create");
        CreateElementButton.setBorderPainted(false);
        CreateElementButton.setOpaque(true);
        CreateElementButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CreateElementButtonActionPerformed(evt);
            }
        });
        ControlPanel.add(CreateElementButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 190, 150, 30));

        UpdateElementButton.setBackground(new java.awt.Color(153, 153, 255));
        UpdateElementButton.setFont(new java.awt.Font("Geneva", 1, 11)); // NOI18N
        UpdateElementButton.setForeground(new java.awt.Color(255, 255, 255));
        UpdateElementButton.setText("Update");
        UpdateElementButton.setBorderPainted(false);
        UpdateElementButton.setOpaque(true);
        UpdateElementButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateElementButtonActionPerformed(evt);
            }
        });
        ControlPanel.add(UpdateElementButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 190, 160, 30));

        DeleteElementButton.setBackground(new java.awt.Color(204, 0, 51));
        DeleteElementButton.setFont(new java.awt.Font("Geneva", 1, 11)); // NOI18N
        DeleteElementButton.setForeground(new java.awt.Color(255, 255, 255));
        DeleteElementButton.setText("Delete");
        DeleteElementButton.setBorderPainted(false);
        DeleteElementButton.setOpaque(true);
        DeleteElementButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeleteElementButtonActionPerformed(evt);
            }
        });
        ControlPanel.add(DeleteElementButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 190, 150, 30));

        ObjectTypeComboBox.setFont(new java.awt.Font("Geneva", 0, 13)); // NOI18N
        ObjectTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "File", "Directory" }));
        ControlPanel.add(ObjectTypeComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 60, 250, -1));

        ObjectNameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                ObjectNameTextFieldKeyTyped(evt);
            }
        });
        ControlPanel.add(ObjectNameTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 110, 250, -1));

        ObjectSizeTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                ObjectSizeTextFieldKeyTyped(evt);
            }
        });
        ControlPanel.add(ObjectSizeTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 160, 250, -1));

        jLabel7.setFont(new java.awt.Font("Geneva", 2, 13)); // NOI18N
        jLabel7.setText("Selected Object Type:");
        ControlPanel.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 40, 170, -1));

        jLabel8.setFont(new java.awt.Font("Geneva", 2, 13)); // NOI18N
        jLabel8.setText("Selected Object Name:");
        ControlPanel.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 90, -1, -1));

        SelectedObjectSizeText.setFont(new java.awt.Font("Geneva", 2, 13)); // NOI18N
        SelectedObjectSizeText.setText("Selected Object Size:");
        ControlPanel.add(SelectedObjectSizeText, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 140, -1, -1));
        ControlPanel.add(SelectedObjectTypeTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 60, 250, -1));
        ControlPanel.add(SelectedObjectSizeTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 160, 250, -1));
        ControlPanel.add(SelectedObjectNameTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 110, 250, -1));

        MainPanel.add(ControlPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 230, 790, 240));

        DiskStatusAreaPanel.setBackground(new java.awt.Color(255, 255, 255));
        DiskStatusAreaPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Disk Status Area", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Geeza Pro", 3, 13))); // NOI18N
        DiskStatusAreaPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        MainPanel.add(DiskStatusAreaPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 350, 460, 360));

        DetailsAreaPanel.setBackground(new java.awt.Color(255, 255, 255));
        DetailsAreaPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Details Area", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Geeza Pro", 3, 13))); // NOI18N
        DetailsAreaPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        DetailsTextArea.setEditable(false);
        DetailsTextArea.setColumns(20);
        DetailsTextArea.setFont(new java.awt.Font("Geneva", 0, 13)); // NOI18N
        DetailsTextArea.setRows(5);
        jScrollPane4.setViewportView(DetailsTextArea);

        DetailsAreaPanel.add(jScrollPane4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, 770, 140));

        MainPanel.add(DetailsAreaPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 480, 790, 170));

        ModeButtonGroup.add(AdminModeRadioButton);
        AdminModeRadioButton.setSelected(true);
        AdminModeRadioButton.setText("Administrator Mode");
        MainPanel.add(AdminModeRadioButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 670, -1, -1));

        ModeButtonGroup.add(UserModeRadioButton);
        UserModeRadioButton.setText("User Mode");
        MainPanel.add(UserModeRadioButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 670, -1, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(MainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(MainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void ObjectSizeTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ObjectSizeTextFieldKeyTyped
        char c = evt.getKeyChar();
        if(c < '0' || c > '9') evt.consume();
    }//GEN-LAST:event_ObjectSizeTextFieldKeyTyped

    private void ObjectNameTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ObjectNameTextFieldKeyTyped
        char c = evt.getKeyChar();
        if (!Character.isLetterOrDigit(c) && c != ' ') {
            evt.consume(); 
        }
    }//GEN-LAST:event_ObjectNameTextFieldKeyTyped

    private void CreateElementButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CreateElementButtonActionPerformed
        // Obtener los datos del formulario
    Color fileColor = new Color((int)(Math.random() * 0x1000000));
    String objectName = ObjectNameTextField.getText().trim();
    String objectSizeText = ObjectSizeTextField.getText().trim();
    int objectSize = 0;

    // Validar que el nombre no esté vacío
    if (objectName.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please fill all the fields!", "Warning", JOptionPane.WARNING_MESSAGE);
        return;
    }

    // Validar que el nombre no exista
    boolean nameExists = false;
    for (File file : fileSystem.files) {
        if (file != null && file.getName().equals(objectName)) {
            nameExists = true;
            break;
        }
    }
    for (Directory directory : fileSystem.directories) {
        if (directory != null && directory.getName().equals(objectName)) {
            nameExists = true;
            break;
        }
    }
    if (nameExists) {
        JOptionPane.showMessageDialog(this, "A file or directory with the name '" + objectName + "' already exists.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Obtener el tipo de objeto seleccionado (File o Directory)
    String selectedType = ObjectTypeComboBox.getSelectedItem().toString();

    // Validar el tamaño si es un archivo
    if (selectedType.equals("File")) {
        if (objectSizeText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all the fields!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            objectSize = Integer.parseInt(objectSizeText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid object size.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (objectSize <= 0) {
            JOptionPane.showMessageDialog(this, "Object size must be greater than zero.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }

    // Obtener el nodo seleccionado en el JTree (debe ser un directorio)
    DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) StructureJTree.getLastSelectedPathComponent();
    if (selectedNode == null || !(selectedNode.getUserObject() instanceof Directory)) {
        JOptionPane.showMessageDialog(this, "Please select a valid directory to add the object!", "Warning", JOptionPane.WARNING_MESSAGE);
        return;
    }

    Directory currentDir = (Directory) selectedNode.getUserObject();
    // Crear un directorio o un archivo según el tipo seleccionado
    if (selectedType.equals("Directory")) {
        // Crear un nuevo directorio
        Directory newDir = new Directory(objectName);
        DefaultMutableTreeNode newDirNode = new DefaultMutableTreeNode(newDir);
        selectedNode.add(newDirNode); // Agregar el nodo al JTree
        currentDir.addDirectory(newDir); // Agregar el directorio al sistema
        fileSystem.createDirectory(objectName); // Registrar el directorio en el sistema de archivos

        // Mostrar mensaje de éxito
        appendToDetails("Directory " + objectName + " successfully created | Date: " + getTimeStamp());
    } else if (selectedType.equals("File")) {
        // Verificar si hay suficiente espacio en el disco
        if (objectSize > storage.getAvailableBlocks()) {
            JOptionPane.showMessageDialog(this, "StorageDisk does not have enough space to store this file.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Crear un nuevo archivo con un color aleatorio
        File newFile = new File(currentDir, objectName, objectSize, storage.getFirstBlock(), fileColor);
        // Asignar bloques en el StorageDisk con el color del archivo
        if (storage.allocateBlocks(newFile.name, objectSize, newFile.getFileColor())) {
            DefaultMutableTreeNode newFileNode = new DefaultMutableTreeNode(newFile);
            selectedNode.add(newFileNode); // Agregar el nodo al JTree
            currentDir.addFile(newFile); // Agregar el archivo al directorio actual
            fileSystem.createFile(currentDir, objectName, objectSize, storage.getFirstBlock() - objectSize, newFile.getFileColor()); // Registrar el archivo en el sistema de archivos

            updateDiskStatus(storage.getBlocks(), storage.getBlockColors());
            createAndShowTable(); // Actualizar la tabla de asignación de archivos

            // Mostrar mensaje de éxito
            appendToDetails("File " + objectName + " (" + objectSize + " blocks) successfully created | Date: " + getTimeStamp());
        } else {
            JOptionPane.showMessageDialog(this, "Failed to allocate blocks for the file.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }

    // Actualizar el JTree y limpiar los campos del formulario
    treeModel.reload(); // Recargar el modelo del JTree
    clearFieldsAndComboBox(); // Limpiar los campos del formulario
    }//GEN-LAST:event_CreateElementButtonActionPerformed

    private void ClearJTreeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ClearJTreeButtonActionPerformed
       clearJTree();
       clearFieldsAndComboBox();
       createAndShowTable();
       LoadTxtButton.setEnabled(true);
    }//GEN-LAST:event_ClearJTreeButtonActionPerformed

    private void UpdateElementButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UpdateElementButtonActionPerformed
      DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) StructureJTree.getLastSelectedPathComponent();

    if (selectedNode != null && selectedNode.getUserObject() != null) {
        Object userObject = selectedNode.getUserObject();
        String newName = SelectedObjectNameTextField.getText().trim();
        String sizeText = SelectedObjectSizeTextField.getText().trim();

        // Validar que el nuevo nombre no exista
        boolean nameExists = false;
        for (File file : fileSystem.files) {
            if (file != null && file.getName().equals(newName)) {
                nameExists = true;
                break;
            }
        }
        for (Directory directory : fileSystem.directories) {
            if (directory != null && directory.getName().equals(newName)) {
                nameExists = true;
                break;
            }
        }
        if (nameExists) {
            JOptionPane.showMessageDialog(this, "A file or directory with the name '" + newName + "' already exists.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean isFile = userObject instanceof File;
        boolean isDirectory = userObject instanceof Directory;

        if (isDirectory) {
            Directory currentDir = (Directory) userObject;
            String currentName = currentDir.getName();
            fileSystem.deleteDirectory(currentName);
            currentDir.setName(newName);
            fileSystem.createDirectory(currentDir.getName());
            appendToDetails("Directory " + currentName + " updated to Name: " + newName + " | Date: " + getTimeStamp());
            selectedNode.setUserObject(currentDir);
        } else if (isFile) {
            if (sizeText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Size cannot be empty for files.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int newSize = Integer.parseInt(sizeText); // REVISAR AQUI
            if (newSize > 0 && newSize <= storage.getAvailableBlocks()) {
                // Eliminar el archivo
                File FileToDelete = (File) userObject;
                Color fileColor = FileToDelete.getFileColor();
                Directory fileDirectory = FileToDelete.getFileDirectory();
                fileSystem.deleteFile(FileToDelete.getName());
                storage.freeBlocks(FileToDelete.getName(), FileToDelete.getFirstBlock(), FileToDelete.getSize()); // Liberar los bloques del archivo
                // Crear el nuevo archivo
                File newFile = new File(fileDirectory, newName, newSize, storage.getFirstBlock(), fileColor);
                if (storage.allocateBlocks(newFile.name, newSize, newFile.getFileColor())) {
                    DefaultMutableTreeNode newFileNode = new DefaultMutableTreeNode(newFile);
                    selectedNode.setUserObject(newFile); // Para archivos
                    fileSystem.createFile(fileDirectory, newName, newSize, storage.getFirstBlock() - newSize, newFile.getFileColor()); // Registrar el archivo en el sistema de archivos
                    updateDiskStatus(storage.getBlocks(), storage.getBlockColors());
                }
                treeModel.reload();
                createAndShowTable();
            } else {
                JOptionPane.showMessageDialog(this, "Please select a valid object to update.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    }//GEN-LAST:event_UpdateElementButtonActionPerformed

    private void DeleteElementButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeleteElementButtonActionPerformed
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) StructureJTree.getLastSelectedPathComponent();

    if (selectedNode != null && selectedNode.getUserObject() != null) {
        Object userObject = selectedNode.getUserObject();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this item?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (userObject instanceof Directory) {
                Directory dirToDelete = (Directory) userObject;
                System.out.println("File count: "+fileSystem.fileCount);
                int fileCount = fileSystem.fileCount;
                for (int i = fileCount - 1; i >= 0; i--) { 
                    if(dirToDelete.getName().equals(fileSystem.files[i].getFileDirectory().getName())){
                        File file = fileSystem.files[i];
                        fileSystem.deleteFile(file.getName());
                        storage.freeBlocks(file.getName(), file.getFirstBlock(), file.getSize());
                        
                        updateDiskStatus(storage.getBlocks(), storage.getBlockColors());
                        appendToDetails("* The Child File " + file.getName() + " has been deleted along with its parent directory | Date: " + getTimeStamp());
                    }
                }
                for (int j = 0; j < dirToDelete.subDirCount; j++) {
                    Directory subDir = dirToDelete.subdirectories[j];
                    fileSystem.deleteDirectory(subDir.getName());
                    appendToDetails("* Subdirectory " + subDir.getName() + " successfully deleted | Date: " + getTimeStamp());
                }
                String currentName = dirToDelete.getName();
                fileSystem.deleteDirectory(currentName);
                appendToDetails("Directory " + currentName + " successfully deleted | Date: " + getTimeStamp());

            } else {
                File FileToDelete = (File) userObject;
                
                fileSystem.deleteFile(FileToDelete.getName());
                
                storage.freeBlocks(FileToDelete.getName(),FileToDelete.getFirstBlock(), FileToDelete.getSize());
                
                updateDiskStatus(storage.getBlocks(), storage.getBlockColors());
                appendToDetails("File " + FileToDelete.getName() + " successfully deleted | Date: " + getTimeStamp()); 
            }

            createAndShowTable();
            ((DefaultTreeModel) StructureJTree.getModel()).removeNodeFromParent(selectedNode);
            treeModel.reload();
        }
    } else {
        JOptionPane.showMessageDialog(this, "Please select a valid object to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
    }
    }//GEN-LAST:event_DeleteElementButtonActionPerformed

    private void SaveTxtFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveTxtFileActionPerformed
        String filename = System.getProperty("user.dir") + "/fileSystem_results.txt";
        saveTreeToTXT(StructureJTree.getModel().getRoot(), filename, "");
        JOptionPane.showMessageDialog(null, "Structure saved in " + filename, "Save Confirmation", JOptionPane.INFORMATION_MESSAGE);
        appendToDetails("The information has been saved in a TXT file | Date: " + getTimeStamp());
    }//GEN-LAST:event_SaveTxtFileActionPerformed

    private void LoadTxtButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LoadTxtButtonActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            java.io.File selectedFile = fileChooser.getSelectedFile();
            loadTreeFromTXT(selectedFile.getAbsolutePath());
            LoadTxtButton.setEnabled(false);
            appendToDetails("A TXT file has been uploaded | Date: " + getTimeStamp());
        }
    }//GEN-LAST:event_LoadTxtButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton AdminModeRadioButton;
    public javax.swing.JTable AllocationJTable;
    private javax.swing.JButton ClearJTreeButton;
    private javax.swing.JPanel ControlPanel;
    private javax.swing.JButton CreateElementButton;
    private javax.swing.JButton DeleteElementButton;
    private javax.swing.JPanel DetailsAreaPanel;
    private javax.swing.JTextArea DetailsTextArea;
    private javax.swing.JPanel DiskStatusAreaPanel;
    private javax.swing.JPanel FileAllocationTablePanel;
    private javax.swing.JPanel JTreePanel;
    private javax.swing.JButton LoadTxtButton;
    private javax.swing.JPanel MainPanel;
    private javax.swing.ButtonGroup ModeButtonGroup;
    private javax.swing.JTextField ObjectNameTextField;
    private javax.swing.JLabel ObjectSizeText;
    private javax.swing.JTextField ObjectSizeTextField;
    private javax.swing.JComboBox<String> ObjectTypeComboBox;
    private javax.swing.JButton SaveTxtFile;
    private javax.swing.JTextField SelectedObjectNameTextField;
    private javax.swing.JLabel SelectedObjectSizeText;
    private javax.swing.JTextField SelectedObjectSizeTextField;
    private javax.swing.JTextField SelectedObjectTypeTextField;
    private javax.swing.JTree StructureJTree;
    private javax.swing.JButton UpdateElementButton;
    private javax.swing.JRadioButton UserModeRadioButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSeparator jSeparator2;
    // End of variables declaration//GEN-END:variables
}
