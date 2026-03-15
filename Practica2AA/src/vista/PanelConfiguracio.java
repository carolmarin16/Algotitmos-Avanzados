package vista;

import model.Dades;
import model.peces.Peca;
import control.Notificar;
import java.awt.*;
import static java.awt.Component.LEFT_ALIGNMENT;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/**
 * Panel lateral: selección de N Peces, tamaño del tablero y estadísticas.
 */
public class PanelConfiguracio extends JPanel {

    private Notificar prog;
    private Dades dat;
    private Peca[] disponibles;
    private JPanel slotPanel;
    private List<JComboBox<Peca>> combos = new ArrayList<>();
    private JComboBox<String> comboDim;
    private boolean actualizando = false;

    // Etiquetas de estadísticas
    private JPanel panelFactors;    // filas dinámicas de branching factors
    private JLabel lblNodos;
    private JLabel lblVelocitat;
    private JLabel lblTempsPrevist;

    // Último nps medido (persistente entre intentos)
    private long ultimNps = 0;

    // Colores por Peca (sincronizados con PanelCentral.COLORES_NUM)
    private static final Color[] COLORES = {
        new Color(20, 80, 200), new Color(170, 20, 20),
        new Color(20, 140, 50), new Color(150, 80, 0),
        new Color(100, 0, 150), new Color(0, 130, 150),
        new Color(180, 140, 0), new Color(100, 50, 20),};

    // ────────────────────────────────────────────────────────────── Constructor
    public PanelConfiguracio(Notificar n, Dades d) {
        prog = n;
        dat = d;
        disponibles = dat.getPecesDisponibles();

        setLayout(new BorderLayout(0, 4));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        setPreferredSize(new Dimension(230, 0));

        JLabel titulo = new JLabel("Configuració");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 14f));
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        add(titulo, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setAlignmentX(LEFT_ALIGNMENT);

        // ── Sección Peces ──────────────────────────────────────────────
        center.add(seccionLabel("Peces del recorregut"));
        slotPanel = new JPanel();
        slotPanel.setLayout(new BoxLayout(slotPanel, BoxLayout.Y_AXIS));
        slotPanel.setAlignmentX(LEFT_ALIGNMENT);
        construirSlots();
        center.add(slotPanel);
        center.add(Box.createVerticalStrut(6));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        btnPanel.setAlignmentX(LEFT_ALIGNMENT);
        btnPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JButton btnAdd = new JButton("+ Afegir");
        JButton btnRem = new JButton("− Treure");
        btnAdd.setFont(btnAdd.getFont().deriveFont(12f));
        btnRem.setFont(btnRem.getFont().deriveFont(12f));
        btnAdd.addActionListener(e -> agregarPeca());
        btnRem.addActionListener(e -> quitarPeca());
        btnPanel.add(btnAdd);
        btnPanel.add(btnRem);
        center.add(btnPanel);

        // ── Sección tamaño tablero ──────────────────────────────────────
        center.add(Box.createVerticalStrut(10));
        JSeparator sep1 = separador();
        sep1.setAlignmentX(LEFT_ALIGNMENT);
        center.add(sep1);
        center.add(Box.createVerticalStrut(6));
        center.add(seccionLabel("Mida del tauler (N×N)"));
        center.add(Box.createVerticalStrut(4));
        center.add(crearComboDim());

        // ── Sección estadísticas ─────────────────────────────────────────
        center.add(Box.createVerticalStrut(10));
        JSeparator sep2 = separador();
        sep2.setAlignmentX(LEFT_ALIGNMENT);
        center.add(sep2);
        center.add(Box.createVerticalStrut(6));
        center.add(seccionLabel("Estimació de complexitat"));
        center.add(Box.createVerticalStrut(4));
        center.add(crearPanelEstadisticas());

        JScrollPane scroll = new JScrollPane(center,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);
    }

    // ────────────────────────────────────── Combo de tamaño del tablero (N×N)
    private JPanel crearComboDim() {
        String[] opciones = new String[18];
        for (int i = 0; i < opciones.length; i++) {
            int n = i + 3;
            opciones[i] = n + "×" + n;
        }
        comboDim = new JComboBox<>(opciones);
        comboDim.setEditable(true);
        comboDim.setFont(comboDim.getFont().deriveFont(13f));
        comboDim.setSelectedItem(dat.getDimension() + "×" + dat.getDimension());
        comboDim.setPreferredSize(new Dimension(80, 30));
        comboDim.setMaximumSize(new Dimension(80, 30));
        comboDim.addActionListener(e -> {
            if (!actualizando) {
                int val = parseDim(comboDim.getSelectedItem().toString());
                if (val >= 3 && val <= 20) {
                    prog.notificar("config:dim-" + val);
                }
            }
        });
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        p.add(comboDim);
        p.add(Box.createHorizontalGlue());
        return p;
    }

    // ────────────────────────────────────── Panel de estadísticas / estimación
    private JPanel crearPanelEstadisticas() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Filas de factors de ramificació (dinámicas según Peces)
        panelFactors = new JPanel();
        panelFactors.setLayout(new BoxLayout(panelFactors, BoxLayout.Y_AXIS));
        panelFactors.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(panelFactors);
        panel.add(Box.createVerticalStrut(4));

        // Nodos estimados
        lblNodos = miniLabel("Nodos teòrics: —");
        lblNodos.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(lblNodos);
        panel.add(Box.createVerticalStrut(2));

        // Velocidad medida
        lblVelocitat = miniLabel("Velocitat: —");
        lblVelocitat.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(lblVelocitat);
        panel.add(Box.createVerticalStrut(2));

        // Tiempo previsto
        lblTempsPrevist = miniLabel("Temps previst: —");
        lblTempsPrevist.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(lblTempsPrevist);

        return panel;
    }

    // ────────────────────────────────────────────────── Cálculo de estimación
    /**
     * Recalcula y muestra los factores de ramificación y nodos estimados.
     */
    public void actualizarEstimacion() {
        if (panelFactors == null) {
            return;
        }

        // ── Factores de ramificación por Peca
        panelFactors.removeAll();
        List<String> clases = dat.getClasesPeces();
        for (int i = 0; i < clases.size(); i++) {
            int movs = dat.getNumMovs(i);
            String nombre = simpleName(clases.get(i));
            JLabel lbl = new JLabel("  ● " + nombre + ": " + movs + " mov.");
            lbl.setFont(lbl.getFont().deriveFont(11f));
            lbl.setForeground(COLORES[i % COLORES.length]);
            lbl.setAlignmentX(LEFT_ALIGNMENT);
            panelFactors.add(lbl);
        }
        panelFactors.revalidate();
        panelFactors.repaint();

        // ── Nodos teóricos estimados
        double nodos = calcularNodosEstimados();
        lblNodos.setText("Nodos teòrics: " + formatarNodos(nodos));

        // ── Tiempo estimado (si tenemos velocidad previa)
        if (ultimNps > 0) {
            lblTempsPrevist.setText("Temps previst: " + estimarTiempo(nodos, ultimNps));
        } else {
            lblTempsPrevist.setText("Temps previst: (cal executar 1 cop)");
        }
        lblVelocitat.setText(ultimNps > 0
                ? "Velocitat: " + formatarNps(ultimNps)
                : "Velocitat: —");
    }

    /**
     * Actualiza la velocidad medida (nps) y recalcula el tiempo previsto.
     */
    public void actualizarVelocitat(long nps) {
        ultimNps = nps;
        if (lblVelocitat == null) {
            return;
        }
        lblVelocitat.setText("Velocitat: " + formatarNps(nps));
        double nodos = calcularNodosEstimados();
        lblTempsPrevist.setText("Temps previst: " + estimarTiempo(nodos, nps));
    }

    // ────────────────────────────────────────────────────── Cálculo numérico
    private double calcularNodosEstimados() {
        int dim = dat.getDimension();
        int N = dat.getNumPeces();
        int total = dim * dim;

        double nodos = 1.0;
        for (int step = 0; step < (total - N); step++) {
            int pieceIdx = step % N;
            double B = dat.getNumMovs(pieceIdx);
            int remaining = total - N - step;
            double effB = B * remaining / (double) total;
            nodos *= Math.max(1.0, effB);
            if (nodos > 1e18) {
                nodos = 1e18;
                break;
            } // evitar overflow
        }
        return nodos;
    }

    private String formatarNodos(double n) {
        if (n < 1_000) {
            return String.format("%.0f", n);
        }
        if (n < 1_000_000) {
            return String.format("%.1f K", n / 1_000);
        }
        if (n < 1e9) {
            return String.format("%.2f M", n / 1e6);
        }
        int exp = (int) Math.floor(Math.log10(n));
        double m = n / Math.pow(10, exp);
        return String.format("≈ %.2f × 10^%d", m, exp);
    }

    private String formatarNps(long nps) {
        if (nps < 1_000) {
            return nps + " nods/s";
        }
        if (nps < 1_000_000) {
            return String.format("%.1f K nods/s", nps / 1_000.0);
        }
        return String.format("%.2f M nods/s", nps / 1_000_000.0);
    }

    private String estimarTiempo(double nodos, long nps) {
        if (nps <= 0) {
            return "—";
        }
        double sec = nodos / nps;
        if (sec < 1) {
            return "< 1 s";
        }
        if (sec < 60) {
            return String.format("%.1f s", sec);
        }
        if (sec < 3600) {
            return String.format("%.1f min", sec / 60);
        }
        if (sec < 86400) {
            return String.format("%.1f h", sec / 3600);
        }
        return String.format("%.1f dies", sec / 86400);
    }

    // ────────────────────────────────────────────────── Construcción de slots
    private void construirSlots() {
        actualizando = true;
        slotPanel.removeAll();
        combos.clear();
        List<String> clases = dat.getClasesPeces();
        for (int i = 0; i < clases.size(); i++) {
            slotPanel.add(crearFila(i, clases.get(i)));
            slotPanel.add(Box.createVerticalStrut(4));
        }
        actualizando = false;
        slotPanel.revalidate();
        slotPanel.repaint();
    }

    private JPanel crearFila(int idx, String claseActual) {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        fila.setAlignmentX(LEFT_ALIGNMENT);

        JLabel badge = new JLabel(String.valueOf(idx + 1));
        badge.setForeground(Color.WHITE);
        badge.setBackground(COLORES[idx % COLORES.length]);
        badge.setOpaque(true);
        badge.setFont(badge.getFont().deriveFont(Font.BOLD, 12f));
        badge.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        fila.add(badge);

        JComboBox<Peca> combo = new JComboBox<>(disponibles);
        combo.setRenderer(new PecaRenderer());
        combo.setFont(combo.getFont().deriveFont(12f));
        for (Peca p : disponibles) {
            if (p.getNombre().equals(claseActual)) {
                combo.setSelectedItem(p);
                break;
            }
        }

        final int index = idx;
        combo.addActionListener(e -> {
            if (!actualizando) {
                Peca sel = (Peca) combo.getSelectedItem();
                if (sel != null) {
                    prog.notificar("config:pieza-set-" + index + "-" + sel.getNombre());
                }
            }
        });
        combos.add(combo);
        fila.add(combo);
        return fila;
    }

    // ────────────────────────────────────────────────── Añadir / quitar
    private void agregarPeca() {
        if (disponibles.length > 0) {
            prog.notificar("config:pieza-add-" + disponibles[0].getNombre());
        }
    }

    private void quitarPeca() {
        if (dat.getNumPeces() > 2) {
            prog.notificar("config:pieza-remove");
        }
    }

    // ────────────────────────────────────────────────── Actualización pública
    public void actualizar(Dades d) {
        dat = d;
        disponibles = dat.getPecesDisponibles();
        construirSlots();
        actualizando = true;
        comboDim.setSelectedItem(dat.getDimension() + "×" + dat.getDimension());
        actualizando = false;
        actualizarEstimacion();
    }

    // ────────────────────────────────────────────────── Helpers de UI
    private JLabel seccionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 12f));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JLabel miniLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(11f));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JSeparator separador() {
        JSeparator s = new JSeparator();
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        return s;
    }

    private static String simpleName(String fqn) {
        int dot = fqn.lastIndexOf('.');
        return dot >= 0 ? fqn.substring(dot + 1) : fqn;
    }

    /**
     * Extrae el primer entero de un String como "8×8", "8x8", "8" → 8
     */
    private static int parseDim(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (Character.isDigit(c)) {
                sb.append(c);
            } else if (sb.length() > 0) {
                break;
            }
        }
        if (sb.length() == 0) {
            return -1;
        }
        return Integer.parseInt(sb.toString());
    }

    // ────────────────────────────────────────────────── Renderer de Peces
    private class PecaRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Peca) {
                Peca p = (Peca) value;
                setText(simpleName(p.getNombre()));
                URL url = getClass().getResource(p.getImagen());
                if (url != null) {
                    Image sc = new ImageIcon(url).getImage()
                            .getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                    setIcon(new ImageIcon(sc));
                }
            }
            return this;
        }
    }
}
