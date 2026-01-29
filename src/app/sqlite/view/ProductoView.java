package app.sqlite.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

import app.sqlite.controller.ProductoController;
import app.sqlite.model.Producto;
import java.sql.SQLException;

public class ProductoView extends javax.swing.JFrame {

    private final ProductoController controller;

    // Formato fecha (lo tienes, aunque acá no lo usas en Producto)
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    // Inputs (formulario)
    private JTextField txtId;     // id solo para actualizar/eliminar/buscar
    private JTextField txtNombre;
    private JTextField txtPrecio;
    private JTextField txtStock;

    // Tabla y modelo
    private JTable table;
    private DefaultTableModel tableModel; // modelo con las columnas de la tabla

    // Botones (acciones)
    private JButton btnAgregar;
    private JButton btnActualizar;
    private JButton btnEliminar;
    private JButton btnLimpiar;
    private JButton btnBuscar;

    public ProductoView() throws SQLException {
        setTitle("Gestión de Productos (Swing - SQLite)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        sdf.setLenient(false);

        initComponents();

        // IMPORTANTE: controller después de initComponents es OK,
        // pero normalmente se crea antes. Lo dejo como tú lo tenías.
        controller = new ProductoController();

        // Carga inicial
        cargarTabla();
    }

    private void initComponents() {
        // Panel superior: formulario
        JPanel panelTop = new JPanel(new BorderLayout());
        panelTop.add(buildFormPanel(), BorderLayout.WEST);

        // Tabla + modelo definido (ANTES: table = new JTable(); sin columnas)
        buildTable();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder("Productos"));

        setLayout(new BorderLayout());
        add(panelTop, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    /**
     * Crea la JTable con su DefaultTableModel y columnas.
     * Sin esto, table.getModel() no tendrá columnas y no podrás agregar filas bien.
     */
    private void buildTable() {
        // Columnas de la tabla (orden debe coincidir con lo que agregas en cargarTabla)
        String[] cols = {"ID", "Nombre", "Precio", "Stock"};

        // Modelo NO editable (evita que editen directo en la tabla)
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Listener de selección: al seleccionar una fila, cargamos datos al formulario
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            onSeleccionarFila();
        });

        // Ajustes visuales opcionales
        table.getColumnModel().getColumn(0).setPreferredWidth(60);  // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(400); // Nombre
        table.getColumnModel().getColumn(2).setPreferredWidth(120); // Precio
        table.getColumnModel().getColumn(3).setPreferredWidth(120); // Stock
    }

    private JPanel buildFormPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Producto"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;

        // Campos
        txtId = new JTextField(8);      // NUEVO
        txtId.setEditable(false);       // El ID lo tomas desde la tabla o búsqueda
        txtNombre = new JTextField(30);
        txtPrecio = new JTextField(10);
        txtStock = new JTextField(10);

        // Botones
        btnAgregar = new JButton("Agregar");
        btnActualizar = new JButton("Actualizar");
        btnEliminar = new JButton("Eliminar");
        btnLimpiar = new JButton("Limpiar");
        btnBuscar = new JButton("Buscar por ID"); // opcional

        // Layout
        int row = 0;

        c.gridx = 0; c.gridy = row; p.add(new JLabel("ID:"), c);
        c.gridx = 1; c.gridy = row; p.add(txtId, c); row++;

        c.gridx = 0; c.gridy = row; p.add(new JLabel("Nombre:"), c);
        c.gridx = 1; c.gridy = row; p.add(txtNombre, c); row++;

        c.gridx = 0; c.gridy = row; p.add(new JLabel("Precio:"), c);
        c.gridx = 1; c.gridy = row; p.add(txtPrecio, c); row++;

        c.gridx = 0; c.gridy = row; p.add(new JLabel("Stock:"), c);
        c.gridx = 1; c.gridy = row; p.add(txtStock, c); row++;

        // Panel de botones (más ordenado)
        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelButtons.add(btnAgregar);
        panelButtons.add(btnActualizar);
        panelButtons.add(btnEliminar);
        panelButtons.add(btnBuscar);
        panelButtons.add(btnLimpiar);

        c.gridx = 1; c.gridy = row; p.add(panelButtons, c);

        // Acciones
        btnAgregar.addActionListener(e -> onAgregar());
        btnActualizar.addActionListener(e -> onActualizar());
        btnEliminar.addActionListener(e -> onEliminar());
        btnLimpiar.addActionListener(e -> onLimpiar());
        btnBuscar.addActionListener(e -> {
            try {
                onBuscarPorId();
            } catch (SQLException ex) {
                System.getLogger(ProductoView.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        });

        // Estado inicial de botones
        setModoNuevo();

        return p;
    }

    /**
     * Acción: Agregar producto
     */
    private void onAgregar() {
        try {
            String nombre = getNombre();
            double precio = getPrecio();
            int stock = getStock();

            // Reglas de negocio mínimas
            if (nombre.isEmpty()) {
                showError("Debe completar Nombre.");
                return;
            }
            if (precio < 0) {
                showError("Precio no puede ser negativo.");
                return;
            }
            if (stock < 0) {
                showError("Stock no puede ser negativo.");
                return;
            }

            controller.guardarProducto(nombre, precio, stock);

            showInfo("Producto creado.");
            refreshAll();

        } catch (Exception ex) {
            showError("No se pudo agregar: " + ex.getMessage());
        }
    }

    /**
     * Acción: Actualizar (requiere ID seleccionado)
     */
    private void onActualizar() {
        try {
            Integer id = getIdSeleccionado();
            if (id == null) {
                showError("Debe seleccionar un producto en la tabla para actualizar.");
                return;
            }

            String nombre = getNombre();
            double precio = getPrecio();
            int stock = getStock();

            if (nombre.isEmpty()) {
                showError("Debe completar Nombre.");
                return;
            }

            controller.actualizarProducto(id, nombre, precio, stock);

            showInfo("Producto actualizado.");
            refreshAll();

        } catch (Exception ex) {
            showError("No se pudo actualizar: " + ex.getMessage());
        }
    }

    /**
     * Acción: Eliminar (requiere ID seleccionado)
     */
    private void onEliminar() {
        try {
            Integer id = getIdSeleccionado();
            if (id == null) {
                showError("Debe seleccionar un producto en la tabla para eliminar.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "¿Eliminar producto ID " + id + "?",
                    "Confirmar",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm != JOptionPane.YES_OPTION) return;

            controller.eliminarProducto(id);

            showInfo("Producto eliminado.");
            refreshAll();

        } catch (Exception ex) {
            showError("No se pudo eliminar: " + ex.getMessage());
        }
    }

    /**
     * Acción: Limpiar formulario y volver a modo "nuevo"
     */
    private void onLimpiar() {
        clearForm();
        table.clearSelection();
        setModoNuevo();
    }

    /**
     * Acción: Buscar por ID (opcional, útil si quieres buscar sin tabla)
     * - Acá pedimos un ID por diálogo.
     */
    private void onBuscarPorId() throws SQLException {
        try {
            String input = JOptionPane.showInputDialog(this, "Ingrese ID a buscar:");
            if (input == null) return; // cancel

            int id = Integer.parseInt(input.trim());
            Producto p = controller.buscarProducto(id);

            if (p == null) {
                showInfo("No existe producto con ID " + id);
                return;
            }

            // Carga al formulario
            fillForm(p);
            setModoEdicion();

        } catch (NumberFormatException ex) {
            showError("ID inválido. Debe ser número entero.");
        }
    }

    /**
     * Cuando el usuario selecciona una fila en la JTable, cargamos sus datos al formulario.
     */
    private void onSeleccionarFila() {
        int row = table.getSelectedRow();
        if (row < 0) {
            setModoNuevo();
            return;
        }

        // Obtiene valores del modelo (ojo: row es vista, pero como no hay sorter, es igual)
        int id = (int) tableModel.getValueAt(row, 0);
        String nombre = (String) tableModel.getValueAt(row, 1);
        double precio = (double) tableModel.getValueAt(row, 2);
        int stock = (int) tableModel.getValueAt(row, 3);

        txtId.setText(String.valueOf(id));
        txtNombre.setText(nombre);
        txtPrecio.setText(String.valueOf(precio));
        txtStock.setText(String.valueOf(stock));

        setModoEdicion();
    }

    /**
     * Carga los productos en la tabla desde el controlador.
     */
    private void cargarTabla() throws SQLException {
        List<Producto> productos = controller.listarProductos();
        // Limpia modelo y vuelve a cargar
        tableModel.setRowCount(0);
        for (Producto p : productos) {
            Object[] fila = {
                p.getId(),
                p.getNombre(),
                p.getPrecio(),
                p.getStock()
            };
            tableModel.addRow(fila);
        }
    }

    /**
     * Refresca todo: recarga tabla + limpia form + modo nuevo.
     */
    private void refreshAll() throws SQLException {
        cargarTabla();
        clearForm();
        table.clearSelection();
        setModoNuevo();
    }

    /**
     * Limpia inputs del formulario.
     */
    private void clearForm() {
        txtId.setText("");
        txtNombre.setText("");
        txtPrecio.setText("");
        txtStock.setText("");
    }

    /**
     * Setea estado UI para modo "nuevo" (solo agregar habilitado).
     */
    private void setModoNuevo() {
        btnAgregar.setEnabled(true);
        btnActualizar.setEnabled(false);
        btnEliminar.setEnabled(false);
        txtId.setText("");
    }

    /**
     * Setea estado UI para modo "edición" (actualizar/eliminar habilitados).
     */
    private void setModoEdicion() {
        btnAgregar.setEnabled(false);
        btnActualizar.setEnabled(true);
        btnEliminar.setEnabled(true);
    }

    /**
     * Rellena formulario desde un Producto.
     */
    private void fillForm(Producto p) {
        txtId.setText(String.valueOf(p.getId()));
        txtNombre.setText(p.getNombre());
        txtPrecio.setText(String.valueOf(p.getPrecio()));
        txtStock.setText(String.valueOf(p.getStock()));
    }

    /**
     * Obtiene ID desde el formulario (si hay selección/edición).
     */
    private Integer getIdSeleccionado() {
        String s = txtId.getText().trim();
        if (s.isEmpty()) return null;
        return Integer.parseInt(s);
    }

    // ===== Helpers para leer inputs con parseo y mensajes consistentes =====

    private String getNombre() {
        return txtNombre == null ? "" : txtNombre.getText().trim();
    }

    private double getPrecio() {
        String s = txtPrecio == null ? "" : txtPrecio.getText().trim();
        if (s.isEmpty()) return 0; // o podrías exigir obligatorio
        return Double.parseDouble(s);
    }

    private int getStock() {
        String s = txtStock == null ? "" : txtStock.getText().trim();
        if (s.isEmpty()) return 0; // o podrías exigir obligatorio
        return Integer.parseInt(s);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    // Método main opcional para probar rápido
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new ProductoView().setVisible(true);
            } catch (SQLException ex) {
                System.getLogger(ProductoView.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        });
    }
}
