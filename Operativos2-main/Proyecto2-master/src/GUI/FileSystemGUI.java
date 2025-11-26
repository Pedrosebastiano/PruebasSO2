/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GUI;

import SISTEMA.*;
import EDD.DirectoryEntry;
import EDD.FileEntry;
import EDD.ListaEnlazada;
import EDD.Nodo;
import javax.swing.table.DefaultTableModel;
import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import javax.swing.Timer;

public class FileSystemGUI extends JFrame {
    private FileSystem fileSystem;
    private JTree fileTree;
    private JTable fileTable, infoTable, processTable, bufferTable;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;
    private JButton btnCrearArchivo, btnEliminarArchivo, btnActualizarArchivo, btnRestaurarArchivo, 
                    btnCrearDirectorio, btnMoverArchivo, btnCambiarUsuario, btnGuardar, btnBorrarSistema,
                    btnProcesarCola, btnLimpiarBuffer, btnVerGraficosBuffer;
    private JLabel lblModo, lblInfo, lblPolicy, lblQueueSize, lblBufferPolicy, lblBufferStats;
    private DiskPanel diskPanel;
    private boolean isAdmin = true;
    private JPanel infoPanel, processPanel, bufferPanel;
    private DefaultTableModel infoTableModel, processTableModel, bufferTableModel;
    private JComboBox<SchedulingPolicy> policyComboBox;
    private JComboBox<CachePolicy> cachePolicyComboBox;
    private Timer processTimer;

    public FileSystemGUI() {

    fileSystem = new FileSystem(100);

    setTitle("Simulador de Sistema de Archivos - Proyecto SO");
    setSize(1400, 800);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout(10, 10));
    getContentPane().setBackground(new Color(0xF8810)); 
    Font titleFont = new Font("Segoe UI", Font.BOLD, 22);
    Font panelTitleFont = new Font("Segoe UI", Font.PLAIN, 14);
    Font normalFont = new Font("Segoe UI", Font.PLAIN, 12);

    Color pastelPanel = new Color(0xFFFFFF);
    Color pastelBorder = new Color(0xE5E5F0);
    Color pastelAccent = new Color(0x6C8BF0); 
    Color pastelButton = new Color(0xEDF0FF);

    JMenuBar menuBar = new JMenuBar();
    JMenu menuArchivo = new JMenu("Archivo");
    JMenu menuVer = new JMenu("Ver");

    JMenuItem menuSalir = new JMenuItem("Salir");
    menuSalir.addActionListener(e -> {
        fileSystem.guardarEnArchivo();
        System.exit(0);
    });

    JMenuItem menuEstadisticas = new JMenuItem("Ver Estadísticas");
    menuEstadisticas.addActionListener(e -> mostrarEstadisticas());

    JMenuItem menuGraficosBuffer = new JMenuItem("Ver Gráficos del Buffer");
    menuGraficosBuffer.addActionListener(e -> mostrarGraficosBuffer());

    menuArchivo.add(menuSalir);
    menuVer.add(menuEstadisticas);
    menuVer.add(menuGraficosBuffer);

    menuBar.add(menuArchivo);
    menuBar.add(menuVer);
    setJMenuBar(menuBar);

    // Header
    JPanel header = new JPanel(new BorderLayout(10, 10));
    header.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
    header.setBackground(new Color(0xFAFAFF));

    JLabel headerTitle = new JLabel("Simulador de Sistema de Archivos");
    headerTitle.setFont(titleFont);
    headerTitle.setForeground(new Color(0x333355));

    lblModo = new JLabel(" Modo: Administrador");
    lblModo.setFont(normalFont);
    lblModo.setForeground(pastelAccent);

    lblInfo = new JLabel(" | Sistema de archivos listo");
    lblInfo.setFont(normalFont);
    lblInfo.setForeground(new Color(0x555566));

    JPanel statusRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    statusRight.setOpaque(false);
    statusRight.add(lblModo);
    statusRight.add(lblInfo);

    header.add(headerTitle, BorderLayout.WEST);
    header.add(statusRight, BorderLayout.EAST);

    add(header, BorderLayout.NORTH);

    // --------------------------------------------------------------------------
    // PANEL ORIGINAL IZQUIERDO → AHORA A LA DERECHA
    // --------------------------------------------------------------------------
    rootNode = new DefaultMutableTreeNode("root");
    treeModel = new DefaultTreeModel(rootNode);
    fileTree = new JTree(treeModel);
    fileTree.setFont(normalFont);
    JScrollPane treeScrollPane = new JScrollPane(fileTree);
    treeScrollPane.setPreferredSize(new Dimension(280, 350));

    JPanel treeCard = new JPanel(new BorderLayout());
    treeCard.setBackground(pastelPanel);
    treeCard.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(pastelBorder),
        BorderFactory.createEmptyBorder(10, 10, 10, 10)
    ));
    treeCard.add(new JLabel("📂 Estructura de Archivos", SwingConstants.LEFT), BorderLayout.NORTH);
    treeCard.add(treeScrollPane, BorderLayout.CENTER);

    infoTableModel = new DefaultTableModel(
        new String[]{"Nombre", "Bloque Inicial", "Longitud", "Color"}, 0
    );
    infoTable = new JTable(infoTableModel);
    JScrollPane infoScrollPane = new JScrollPane(infoTable);

    JPanel infoCard = new JPanel(new BorderLayout());
    infoCard.setBackground(pastelPanel);
    infoCard.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(pastelBorder),
        BorderFactory.createEmptyBorder(10, 10, 10, 10)
    ));
    infoCard.add(new JLabel("📊 Tabla de Asignación"), BorderLayout.NORTH);
    infoCard.add(infoScrollPane, BorderLayout.CENTER);

    JPanel rightColumn = new JPanel();
    rightColumn.setLayout(new BoxLayout(rightColumn, BoxLayout.Y_AXIS));
    rightColumn.setBackground(new Color(0xFAFAFF));
    rightColumn.add(treeCard);
    rightColumn.add(Box.createRigidArea(new Dimension(0, 14)));
    rightColumn.add(infoCard);
    rightColumn.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

    actualizarJTree();

    fileTree.addTreeSelectionListener(e -> {
        DefaultMutableTreeNode selected = 
            (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
        if (selected != null) actualizarInfoTabla(selected.toString());
    });

    // --------------------------------------------------------------------------
    // PANEL ORIGINAL CENTRAL → AHORA A LA IZQUIERDA
    // --------------------------------------------------------------------------

    // POLICIES
    JPanel policyPanel = new JPanel(new GridBagLayout());
    policyPanel.setBackground(pastelPanel);
    policyPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(pastelBorder),
        BorderFactory.createEmptyBorder(10, 10, 10, 10)
    ));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 8, 5, 8);
    gbc.anchor = GridBagConstraints.WEST;

    JLabel planLbl = new JLabel("📋 Planificación:");
    policyComboBox = new JComboBox<>(SchedulingPolicy.values());
    policyComboBox.addActionListener(e -> cambiarPolitica());
    lblPolicy = new JLabel("Política Disco: FIFO");
    
    // 🔧 INICIALIZAR lblQueueSize que faltaba
    lblQueueSize = new JLabel("Cola I/O: 0");

    gbc.gridx = 0; gbc.gridy = 0;
    policyPanel.add(planLbl, gbc);
    gbc.gridx = 1; policyPanel.add(policyComboBox, gbc);
    gbc.gridx = 2; policyPanel.add(lblPolicy, gbc);
    gbc.gridx = 3; policyPanel.add(lblQueueSize, gbc);  // 🔧 Agregado

    // Buffer
    JLabel bufLbl = new JLabel("💾 Buffer:");
    cachePolicyComboBox = new JComboBox<>(CachePolicy.values());
    cachePolicyComboBox.addActionListener(e -> cambiarPoliticaBuffer());
    lblBufferPolicy = new JLabel("Política Buffer: LRU");
    lblBufferStats = new JLabel("Hits: 0 | Misses: 0");

    btnLimpiarBuffer = new JButton("Limpiar");
    btnLimpiarBuffer.addActionListener(e -> limpiarBuffer());  // 🔧 Agregado listener
    styleButton(btnLimpiarBuffer, pastelButton, pastelAccent);

    gbc.gridx = 0; gbc.gridy = 1;
    policyPanel.add(bufLbl, gbc);
    gbc.gridx = 1; policyPanel.add(cachePolicyComboBox, gbc);
    gbc.gridx = 2; policyPanel.add(lblBufferPolicy, gbc);
    gbc.gridx = 3; policyPanel.add(lblBufferStats, gbc);
    gbc.gridx = 4; policyPanel.add(btnLimpiarBuffer, gbc);

    // DISK PANEL
    diskPanel = new DiskPanel(fileSystem.getDisk());
    JPanel diskCard = new JPanel(new BorderLayout());
    diskCard.setBackground(pastelPanel);
    diskCard.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(pastelBorder),
        BorderFactory.createEmptyBorder(10, 10, 10, 10)
    ));
    diskCard.add(new JLabel("💿 Simulación del Disco"), BorderLayout.NORTH);
    diskCard.add(diskPanel, BorderLayout.CENTER);

    JPanel leftColumn = new JPanel(new BorderLayout(10, 10));
    leftColumn.setBackground(new Color(0xFAFAFF));
    leftColumn.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    leftColumn.add(policyPanel, BorderLayout.NORTH);
    leftColumn.add(diskCard, BorderLayout.CENTER);

    // --------------------------------------------------------------------------
    // PANEL ORIGINAL DERECHO → AHORA AL CENTRO
    // --------------------------------------------------------------------------
    processTableModel = new DefaultTableModel(
        new String[]{"PID", "Nombre", "Estado", "Operación", "Archivo"}, 0
    );
    processTable = new JTable(processTableModel);

    bufferTableModel = new DefaultTableModel(
        new String[]{"Bloque", "Archivo", "Estado"}, 0
    );
    bufferTable = new JTable(bufferTableModel);
    
    // 🔧 INICIALIZAR bufferPanel que faltaba
    bufferPanel = new JPanel(new BorderLayout());
    bufferPanel.add(new JScrollPane(bufferTable), BorderLayout.CENTER);

    JTabbedPane middleTabs = new JTabbedPane();
    middleTabs.addTab("Archivos", new JScrollPane(fileTable = new JTable(new DefaultTableModel(
        new String[]{"Archivo", "Tamaño (KB)", "Bloques"}, 0
    ))));
    middleTabs.addTab("Cola I/O", new JScrollPane(processTable));
    middleTabs.addTab("Buffer", bufferPanel);  // 🔧 Usar el panel en lugar del scrollpane directo

    JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
    centerPanel.setBackground(new Color(0xFAFAFF));
    centerPanel.add(middleTabs, BorderLayout.CENTER);

    // --------------------------------------------------------------------------
    // HERMOSA BARRA DE BOTONES JUNTO AL PANEL CENTRAL
    // --------------------------------------------------------------------------

    JPanel buttonPanel = new JPanel(new GridLayout(2, 6, 10, 10));
    buttonPanel.setBackground(new Color(0xFAFAFF));
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    btnCrearArchivo = new JButton("Crear");
    btnCrearDirectorio = new JButton("Directorio");
    btnEliminarArchivo = new JButton("Eliminar");
    btnActualizarArchivo = new JButton("Actualizar");
    btnMoverArchivo = new JButton("Mover");
    btnRestaurarArchivo = new JButton("Restaurar");
    btnGuardar = new JButton("Guardar");
    btnCambiarUsuario = new JButton("Usuario");
    btnBorrarSistema = new JButton("Borrar");
    btnProcesarCola = new JButton("Procesar");
    btnVerGraficosBuffer = new JButton("Gráficos");

    JButton[] buttons = {
        btnCrearArchivo, btnCrearDirectorio, btnEliminarArchivo, btnActualizarArchivo,
        btnMoverArchivo, btnRestaurarArchivo, btnGuardar, btnCambiarUsuario,
        btnBorrarSistema, btnProcesarCola, btnVerGraficosBuffer
    };

    for (JButton b : buttons) {
        styleButton(b, pastelButton, pastelAccent);
        buttonPanel.add(b);
    }
    
    btnCrearArchivo.addActionListener(e -> crearArchivo());
    btnCrearDirectorio.addActionListener(e -> crearDirectorio());
    btnEliminarArchivo.addActionListener(e -> eliminarArchivo());
    btnActualizarArchivo.addActionListener(e -> actualizarArchivo());
    btnMoverArchivo.addActionListener(e -> moverArchivo());
    btnRestaurarArchivo.addActionListener(e -> restaurarArchivo());
    btnGuardar.addActionListener(e -> guardarSistema());
    btnCambiarUsuario.addActionListener(e -> cambiarModoUsuario());
    btnBorrarSistema.addActionListener(e -> borrarSistema());
    btnProcesarCola.addActionListener(e -> procesarCola());
    btnVerGraficosBuffer.addActionListener(e -> mostrarGraficosBuffer());

    centerPanel.add(buttonPanel, BorderLayout.NORTH);

    // --------------------------------------------------------------------------
    // SPLIT FINAL: IZQUIERDA – CENTRO – DERECHA
    // --------------------------------------------------------------------------
    JSplitPane splitLeftCenter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftColumn, centerPanel);
    splitLeftCenter.setResizeWeight(0.35);

    JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, splitLeftCenter, rightColumn);
    mainSplit.setResizeWeight(0.75);

    add(mainSplit, BorderLayout.CENTER);

    // --------------------------------------------------------------------------
    // TIMER
    // --------------------------------------------------------------------------
    processTimer = new Timer(300, e -> {
        actualizarProcessTable();
        actualizarQueueSize();
        actualizarBufferTable();
        actualizarBufferStats();
    });
    processTimer.start();

    SwingUtilities.invokeLater(() -> {
        fileTree.expandRow(0);
        actualizarJTree();
    });
}
// --------------------------------------------------------
// BOTÓN ESTILO SUAVE
// --------------------------------------------------------
private void styleButton(JButton btn, Color bg, Color fgAccent) {
    btn.setFocusPainted(false);
    btn.setBackground(bg);
    btn.setForeground(new Color(0x6E8B3D));
    btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    btn.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(0x6E8B3D)),
        BorderFactory.createEmptyBorder(6, 10, 6, 10)
    ));
    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    btn.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseEntered(java.awt.event.MouseEvent e) {
            btn.setBackground(new Color(0x636b2f ));
        }

        public void mouseExited(java.awt.event.MouseEvent e) {
            btn.setBackground(bg);
        }
    });
}


    private void cambiarPolitica() {
        SchedulingPolicy selected = (SchedulingPolicy) policyComboBox.getSelectedItem();
        fileSystem.setSchedulingPolicy(selected);
        lblPolicy.setText("Política Disco: " + selected);
    }

    private void cambiarPoliticaBuffer() {
        CachePolicy selected = (CachePolicy) cachePolicyComboBox.getSelectedItem();
        fileSystem.setCachePolicy(selected);
        lblBufferPolicy.setText("Política Buffer: " + selected);
        JOptionPane.showMessageDialog(this, "✅ Política de buffer cambiada a: " + selected + "\n⚠️ El buffer ha sido limpiado.");
    }

    private void limpiarBuffer() {
        fileSystem.getBuffer().clear();
        actualizarBufferTable();
        actualizarBufferStats();
        JOptionPane.showMessageDialog(this, "✅ Buffer limpiado correctamente.");
    }

    private void mostrarGraficosBuffer() {
        JDialog dialog = new JDialog(this, "📊 Estadísticas Gráficas del Buffer", true);
        dialog.setSize(900, 700);
        dialog.setLocationRelativeTo(this);
        
        BufferStatsPanel statsPanel = new BufferStatsPanel(fileSystem.getBuffer());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnActualizar = new JButton("🔄 Actualizar");
        JButton btnCerrar = new JButton("✖ Cerrar");
        
        btnActualizar.addActionListener(e -> statsPanel.actualizarGraficos());
        btnCerrar.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(btnActualizar);
        buttonPanel.add(btnCerrar);
        
        dialog.setLayout(new BorderLayout());
        dialog.add(statsPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }

    private void actualizarBufferTable() {
        bufferTableModel.setRowCount(0);
        BufferCache buffer = fileSystem.getBuffer();
        
        for (Integer blockNum : buffer.getCache().keySet()) {
            CacheBlock block = buffer.getCache().get(blockNum);
            bufferTableModel.addRow(new Object[]{
                blockNum,
                block.getFileName(),
                "Cargado"
            });
        }
        
        int used = buffer.getSize();
        int capacity = buffer.getCapacity();
        bufferPanel.setBorder(BorderFactory.createTitledBorder(
            String.format("💾 Estado del Buffer (%d/%d bloques)", used, capacity)
        ));
    }

    private void actualizarBufferStats() {
        BufferCache buffer = fileSystem.getBuffer();
        lblBufferStats.setText(String.format(
            "Hits: %d | Misses: %d | Hit Rate: %.1f%%",
            buffer.getHits(),
            buffer.getMisses(),
            buffer.getHitRate()
        ));
    }

    private void mostrarEstadisticas() {
        BufferCache buffer = fileSystem.getBuffer();
        int totalArchivos = contarArchivosRecursivo(fileSystem.getRoot());
        int bloquesUsados = contarBloquesUsados();
        int bloquesLibres = fileSystem.getDisk().getTotalBlocks() - bloquesUsados;
        
        String stats = String.format(
            "═══════════════════════════════════════\n" +
            "📊 ESTADÍSTICAS DEL SISTEMA\n" +
            "═══════════════════════════════════════\n\n" +
            "📁 ARCHIVOS Y DISCO:\n" +
            "   • Archivos totales: %d\n" +
            "   • Bloques usados: %d / %d\n" +
            "   • Bloques libres: %d\n" +
            "   • Uso del disco: %.1f%%\n\n" +
            "⚙️ PROCESOS I/O:\n" +
            "   • En cola: %d\n" +
            "   • Política actual: %s\n\n" +
            "💾 BUFFER/CACHÉ:\n" +
            "   • Capacidad: %d bloques\n" +
            "   • En uso: %d bloques\n" +
            "   • Hits totales: %d\n" +
            "   • Misses totales: %d\n" +
            "   • Tasa de aciertos: %.1f%%\n" +
            "   • Política: %s\n\n" +
            totalArchivos,
            bloquesUsados,
            fileSystem.getDisk().getTotalBlocks(),
            bloquesLibres,
            (bloquesUsados * 100.0 / fileSystem.getDisk().getTotalBlocks()),
            fileSystem.getProcessQueue().size(),
            fileSystem.getScheduler().getPolicy(),
            buffer.getCapacity(),
            buffer.getSize(),
            buffer.getHits(),
            buffer.getMisses(),
            buffer.getHitRate(),
            buffer.getPolicy()
        );
        
        JTextArea textArea = new JTextArea(stats);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), 
            "Estadísticas del Sistema", JOptionPane.INFORMATION_MESSAGE);
    }

    private int contarArchivosRecursivo(DirectoryEntry dir) {
        int count = dir.files.contarElementos();
        Nodo<DirectoryEntry> actualDir = dir.subDirectories.getCabeza();
        while (actualDir != null) {
            count += contarArchivosRecursivo(actualDir.dato);
            actualDir = actualDir.siguiente;
        }
        return count;
    }

    private int contarBloquesUsados() {
        int count = 0;
        boolean[] blockMap = fileSystem.getDisk().getBlockMap();
        for (boolean usado : blockMap) {
            if (usado) count++;
        }
        return count;
    }

    private void procesarCola() {
        if (fileSystem.getProcessQueue().isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ No hay procesos de I/O en la cola.", "Cola vacía", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnProcesarCola.setEnabled(false);
        btnProcesarCola.setText("⏳ Procesando...");

        Thread processThread = new Thread(() -> {
            while (!fileSystem.getProcessQueue().isEmpty()) {
                fileSystem.processNextIO();
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SwingUtilities.invokeLater(() -> actualizarInterfaz());
            }
            SwingUtilities.invokeLater(() -> {
                btnProcesarCola.setEnabled(true);
                btnProcesarCola.setText("▶️ Procesar Cola");
                JOptionPane.showMessageDialog(this, "✅ Todos los procesos I/O han sido ejecutados.", 
                                            "Procesamiento completo", JOptionPane.INFORMATION_MESSAGE);
            });
        });
        processThread.start();
    }

    private void actualizarProcessTable() {
        processTableModel.setRowCount(0);
        ListaEnlazada<IOProcess> queue = fileSystem.getProcessQueue().getQueue();
        Nodo<IOProcess> actual = queue.getCabeza();
        
        while (actual != null) {
            IOProcess p = actual.dato;
            processTableModel.addRow(new Object[]{
                "P" + p.getId(),
                p.getName(),
                p.getState().toString(),
                p.getIoRequest().getOperation().toString(),
                p.getIoRequest().getFileName()
            });
            actual = actual.siguiente;
        }
    }

    private void actualizarQueueSize() {
        int size = fileSystem.getProcessQueue().size();
        lblQueueSize.setText("Procesos de I/O en cola: " + size);
    }

    private void crearArchivo() { 
        if (!isAdmin) {
            JOptionPane.showMessageDialog(this, "🚫 Solo el Administrador puede crear archivos.", "Acceso Denegado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String path = JOptionPane.showInputDialog(this, "Ingrese la ruta del directorio (Ejemplo: / o /documentos):");
        if (path == null) return;
        
        String fileName = JOptionPane.showInputDialog(this, "Ingrese el nombre del archivo:");
        if (fileName == null || fileName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "❌ Debes ingresar un nombre válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int fileSize;
        try {
            String input = JOptionPane.showInputDialog(this, "Ingrese el tamaño en bloques:");
            if (input == null) return;
            fileSize = Integer.parseInt(input);
            if (fileSize <= 0) {
                JOptionPane.showMessageDialog(this, "❌ El tamaño debe ser mayor a 0.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "❌ Tamaño inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        fileSystem.createFile(path, fileName, fileSize, "Administrador");
        JOptionPane.showMessageDialog(this, "✅ Proceso de I/O creado. Use 'Procesar Cola' para ejecutar.", 
                                    "Proceso en cola", JOptionPane.INFORMATION_MESSAGE);
        actualizarInterfaz();
    }

    private void borrarSistema() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "⚠️ ¿Estás seguro de que quieres borrar todo?\nEsta acción no se puede deshacer.",
                "Confirmar Borrado",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            fileSystem.borrarTodo();
            actualizarInterfaz();
            JOptionPane.showMessageDialog(this, "✅ Sistema borrado exitosamente.");
        }
    }

    private void crearDirectorio() { 
        if (!isAdmin) {
            JOptionPane.showMessageDialog(this, "🚫 Solo el Administrador puede crear directorios.", "Acceso Denegado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String path = JOptionPane.showInputDialog(this, "Ingrese la ruta:");
        if (path == null) return;
        
        String dirName = JOptionPane.showInputDialog(this, "Ingrese el nombre del directorio:");
        if (dirName == null || dirName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "❌ Nombre inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        fileSystem.createDirectory(path, dirName, "Administrador");
        actualizarInterfaz();
    }

    private void eliminarArchivo() { 
        if (!isAdmin) {
            JOptionPane.showMessageDialog(this, "🚫 Solo el Administrador puede eliminar.", "Acceso Denegado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String path = JOptionPane.showInputDialog(this, "Ruta del archivo:");
        if (path == null) return;
        
        String fileName = JOptionPane.showInputDialog(this, "Nombre del archivo:");
        if (fileName == null) return;

        if (!fileSystem.existeArchivo(path, fileName)) {
            JOptionPane.showMessageDialog(this, "❌ El archivo no existe.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        fileSystem.deleteFile(path, fileName, "Administrador");
        JOptionPane.showMessageDialog(this, "✅ Proceso de eliminación I/O creado.", "En cola", JOptionPane.INFORMATION_MESSAGE);
        actualizarInterfaz();
    }

    private void actualizarArchivo() { 
        if (!isAdmin) {
            JOptionPane.showMessageDialog(this, "🚫 Solo el Administrador puede actualizar.", "Acceso Denegado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String path = JOptionPane.showInputDialog(this, "Ruta del archivo:");
        if (path == null) return;
        
        String fileName = JOptionPane.showInputDialog(this, "Nombre del archivo:");
        if (fileName == null) return;

        if (!fileSystem.existeArchivo(path, fileName)) {
            JOptionPane.showMessageDialog(this, "❌ El archivo no existe.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String newContent = JOptionPane.showInputDialog(this, "Nuevo contenido:");
        if (newContent == null) return;
        
        fileSystem.updateFile(path, fileName, newContent, "Administrador");
        JOptionPane.showMessageDialog(this, "✅ Proceso de actualización I/O creado.", "En cola", JOptionPane.INFORMATION_MESSAGE);
    }

    private void restaurarArchivo() { 
        String fileName = JOptionPane.showInputDialog(this, "Nombre del archivo:");
        if (fileName == null) return;
        
        String versionFile = JOptionPane.showInputDialog(this, "Nombre del backup:");
        if (versionFile == null) return;

        String restoredContent = fileSystem.restoreFile(fileName, "backups/" + versionFile);
        if (restoredContent != null) {
            JOptionPane.showMessageDialog(this, "✅ Contenido restaurado:\n" + restoredContent);
        } else {
            JOptionPane.showMessageDialog(this, "⚠️ No se encontró el backup.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void moverArchivo() { 
        if (!isAdmin) {
            JOptionPane.showMessageDialog(this, "🚫 Solo el Administrador puede mover archivos.", "Acceso Denegado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String pathOrigen = JOptionPane.showInputDialog(this, "Ruta origen:");
        if (pathOrigen == null) return;
        
        String fileName = JOptionPane.showInputDialog(this, "Nombre del archivo:");
        if (fileName == null) return;
        
        String pathDestino = JOptionPane.showInputDialog(this, "Ruta destino:");
        if (pathDestino == null) return;

        if (!fileSystem.existeArchivo(pathOrigen, fileName)) {
            JOptionPane.showMessageDialog(this, "❌ El archivo no existe.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        fileSystem.moverArchivo(pathOrigen, fileName, pathDestino, "Administrador");
        actualizarInterfaz();
    }

    private void cambiarModoUsuario() { 
        isAdmin = !isAdmin;
        btnCambiarUsuario.setText(isAdmin ? " Administrador" : " Usuario");
        lblModo.setText(isAdmin ? " Modo: Administrador" : " Modo: Usuario");
        fileSystem.changeUserMode(isAdmin);
    }

    private void guardarSistema() { 
        fileSystem.guardarEnArchivo();
        JOptionPane.showMessageDialog(this, "✅ Sistema guardado correctamente.");     
    }

    private void actualizarInterfaz() { 
        actualizarJTree();
        actualizarTabla();
        diskPanel.actualizarDisco();
        actualizarProcessTable();
        actualizarQueueSize();
        actualizarBufferTable();
        actualizarBufferStats();
    }

    private void actualizarTabla() {
        Object[][] data = obtenerDatosTabla();
        fileTable.setModel(new DefaultTableModel(
                data, new String[]{"Archivo", "Tamaño (KB)", "Bloques"}));
    }

    private Object[][] obtenerDatosTabla() {
        ListaEnlazada<FileEntry> todosLosArchivos = new ListaEnlazada<>();
        recolectarArchivosRecursivo(fileSystem.getRoot(), todosLosArchivos);
        
        int size = todosLosArchivos.contarElementos();
        Object[][] data = new Object[size][3];

        Nodo<FileEntry> actual = todosLosArchivos.getCabeza();
        int i = 0;
        while (actual != null) {
            FileEntry file = actual.dato;
            data[i][0] = file.name;
            data[i][1] = file.size;
            data[i][2] = file.blocks.contarElementos();
            i++;
            actual = actual.siguiente;
        }
        return data;
    }

    private void recolectarArchivosRecursivo(DirectoryEntry dir, ListaEnlazada<FileEntry> listaArchivos) {
        Nodo<FileEntry> actualArchivo = dir.files.getCabeza();
        while (actualArchivo != null) {
            listaArchivos.agregar(actualArchivo.dato);
            actualArchivo = actualArchivo.siguiente;
        }

        Nodo<DirectoryEntry> actualDir = dir.subDirectories.getCabeza();
        while (actualDir != null) {
            recolectarArchivosRecursivo(actualDir.dato, listaArchivos);
            actualDir = actualDir.siguiente;
        }
    }

    private void actualizarInfoTabla(String nombre) {
    infoTableModel.setRowCount(0);
    DirectoryEntry root = fileSystem.getRoot();

    FileEntry archivo = buscarArchivoRecursivo(root, nombre);
    if (archivo != null) {
        if (archivo.blocks.getCabeza() != null) {
            int primerBloque = archivo.blocks.obtener(0);
            int cantidadBloques = archivo.blocks.contarElementos();
            
            // 🔧 OBTENER COLOR DEL ARCHIVO
            Color color = diskPanel.obtenerColorArchivo(archivo.name);
            String colorStr = String.format("RGB(%d, %d, %d)", 
                color.getRed(), color.getGreen(), color.getBlue());
            
            infoTableModel.addRow(new Object[]{
                archivo.name, 
                primerBloque, 
                cantidadBloques, 
                colorStr  // ← Agregamos el color aquí
            });
        }
        return;
    }

    DirectoryEntry directorio = buscarDirectorioRecursivo(root, nombre);
    if (directorio != null) {
        int tamañoTotal = fileSystem.calcularTamañoDirectorio(directorio);
        int primerBloque = fileSystem.obtenerPrimerBloqueDirectorio(directorio);
        
        // Para directorios, también podemos mostrar un color o dejar vacío
        infoTableModel.addRow(new Object[]{
            directorio.name, 
            primerBloque == -1 ? "N/A" : primerBloque, 
            tamañoTotal,
            "Directorio" // ← Indicamos que es un directorio
        });
    }
}
    private FileEntry buscarArchivoRecursivo(DirectoryEntry dir, String nombre) {
        Nodo<FileEntry> actualArchivo = dir.files.getCabeza();
        while (actualArchivo != null) {
            if (actualArchivo.dato.name.equals(nombre)) {
                return actualArchivo.dato;
            }
            actualArchivo = actualArchivo.siguiente;
        }

        Nodo<DirectoryEntry> actualDir = dir.subDirectories.getCabeza();
        while (actualDir != null) {
            FileEntry encontrado = buscarArchivoRecursivo(actualDir.dato, nombre);
            if (encontrado != null) return encontrado;
            actualDir = actualDir.siguiente;
        }
        return null;
    }

    private DirectoryEntry buscarDirectorioRecursivo(DirectoryEntry dir, String nombre) {
        if (dir.name.equals(nombre)) return dir;

        Nodo<DirectoryEntry> actualDir = dir.subDirectories.getCabeza();
        while (actualDir != null) {
            DirectoryEntry encontrado = buscarDirectorioRecursivo(actualDir.dato, nombre);
            if (encontrado != null) return encontrado;
            actualDir = actualDir.siguiente;
        }
        return null;
    }

    private void actualizarJTree() {
        rootNode.removeAllChildren();
        construirArbolDesdeEstructura(rootNode, fileSystem.getRoot());
        treeModel.reload();
    }

    private void construirArbolDesdeEstructura(DefaultMutableTreeNode nodoPadre, DirectoryEntry directorio) {
        Nodo<FileEntry> actualArchivo = directorio.files.getCabeza();
        while (actualArchivo != null) {
            nodoPadre.add(new DefaultMutableTreeNode(actualArchivo.dato.name));
            actualArchivo = actualArchivo.siguiente;
        }

        Nodo<DirectoryEntry> actualDirectorio = directorio.subDirectories.getCabeza();
        while (actualDirectorio != null) {
            DefaultMutableTreeNode nodoDirectorio = new DefaultMutableTreeNode(actualDirectorio.dato.name);
            nodoPadre.add(nodoDirectorio);
            construirArbolDesdeEstructura(nodoDirectorio, actualDirectorio.dato);
            actualDirectorio = actualDirectorio.siguiente;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            FileSystemGUI gui = new FileSystemGUI();
            gui.setVisible(true);
        });
    }
}