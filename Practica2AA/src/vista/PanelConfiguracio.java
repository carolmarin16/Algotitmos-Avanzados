package vista;

import control.Notificar;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import model.Dades;
import model.peces.Peca;

/**
 * Panel lateral: selecció de N peces, mida del tauler i estadístiques.
 */
public class PanelConfiguracio extends JPanel {

    // ── Atributs ─────────────────────────────────────────────
    private Notificar prog;
    private Dades dat;
    private Peca[] disponibles;
    private JPanel slotPanel;
    private List<JComboBox<Peca>> combos = new ArrayList<>();
    private JComboBox<String> comboDim;
    private JComboBox<String> comboModeInici;
    private JButton btnIntroduirPosicions;
    private boolean actualizando = false;

    // ── Etiquetes estadístiques ─────────────────────────────
    private JPanel panelFactors;
    private JLabel lblTempsReal;
    private JLabel lblTempsPrevist;
    private JLabel lblConstant;

    // ── Colors per peça  ──────────────────────────────────────
    private static final Color[] COLORES = {
        new Color(20, 80, 200), new Color(170, 20, 20),
        new Color(20, 140, 50), new Color(150, 80, 0),
        new Color(100, 0, 150), new Color(0, 130, 150),
        new Color(180, 140, 0), new Color(100, 50, 20),};

    // ── Constructor ───────────────────────────────────────────
    /**
     * Inicialitza el panell de configuració amb totes les opcions.
     *
     * @param n objecte Notificar per avisar controladors
     * @param d dades del taulell i peces
     */
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

        // ── Secció de peces ──────────────────────────────────────────────
        center.add(seccionLabel("Peces del recorregut"));
        slotPanel = new JPanel();
        slotPanel.setLayout(new BoxLayout(slotPanel, BoxLayout.Y_AXIS));
        slotPanel.setAlignmentX(LEFT_ALIGNMENT);
        construirSlots();
        center.add(slotPanel);
        center.add(Box.createVerticalStrut(6));

        // ── Botons afegir / treure ───────────────────────────────────────
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

        // ── Secció de mida del tauler ───────────────────────────────────
        center.add(Box.createVerticalStrut(10));
        JSeparator sep1 = separador();
        sep1.setAlignmentX(LEFT_ALIGNMENT);
        center.add(sep1);
        center.add(Box.createVerticalStrut(6));
        center.add(seccionLabel("Mida del tauler (N×N)"));
        center.add(Box.createVerticalStrut(4));
        center.add(crearComboDim());

        // ── Secció de posicions inicials ────────────────────────────────
        center.add(Box.createVerticalStrut(8));
        center.add(seccionLabel("Posicions inicials"));
        center.add(Box.createVerticalStrut(4));
        center.add(crearPanelModeInici());

        // ── Secció d'estadístiques ──────────────────────────────────────
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

    /**
     * Crea el panell de selecció de mode inicial (fix, aleatori, usuari).
     */
    private JPanel crearPanelModeInici() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        String[] opcions = {"Fixes (cantons)", "Aleatori", "Usuari"};
        comboModeInici = new JComboBox<>(opcions);
        comboModeInici.setFont(comboModeInici.getFont().deriveFont(13f));
        comboModeInici.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        comboModeInici.setAlignmentX(LEFT_ALIGNMENT);
        comboModeInici.addActionListener(e -> {
            if (!actualizando) {
                String[] modes = {"fixes", "aleatori", "usuari"};
                prog.notificar("config:inicio-modo-" + modes[comboModeInici.getSelectedIndex()]);
            }
        });

        btnIntroduirPosicions = new JButton("Introduir posicions...");
        btnIntroduirPosicions.setFont(btnIntroduirPosicions.getFont().deriveFont(12f));
        btnIntroduirPosicions.setAlignmentX(LEFT_ALIGNMENT);
        btnIntroduirPosicions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        btnIntroduirPosicions.addActionListener(e -> prog.notificar("config:introduir-posicions"));
        btnIntroduirPosicions.setVisible("usuari".equals(dat.getModeInici()));

        panel.add(comboModeInici);
        panel.add(Box.createVerticalStrut(4));
        panel.add(btnIntroduirPosicions);

        aplicarModeIniciSeleccionat();
        return panel;
    }

    /**
     * Aplicar mode d'inici seleccionat.
     */
    private void aplicarModeIniciSeleccionat() {
        if (comboModeInici == null) {
            return;
        }
        String mode = dat.getModeInici();
        actualizando = true;
        if ("aleatori".equals(mode)) {
            comboModeInici.setSelectedIndex(1);
        } else if ("usuari".equals(mode)) {
            comboModeInici.setSelectedIndex(2);
        } else {
            comboModeInici.setSelectedIndex(0);
        }
        actualizando = false;
        if (btnIntroduirPosicions != null) {
            btnIntroduirPosicions.setVisible("usuari".equals(mode));
        }
    }

    /**
     * Crear selector de mida del tauler.
     */
    private JPanel crearComboDim() {
        String[] opciones = new String[59];
        for (int i = 0; i < opciones.length; i++) {
            int n = i + 3;
            opciones[i] = n + "×" + n;
        }
        comboDim = new JComboBox<>(opciones);
        comboDim.setEditable(true);
        comboDim.setFont(comboDim.getFont().deriveFont(13f));
        comboDim.setSelectedItem(dat.getDimensio() + "×" + dat.getDimensio());
        comboDim.setPreferredSize(new Dimension(80, 30));
        comboDim.setMaximumSize(new Dimension(80, 30));
        comboDim.addActionListener(e -> {
            if (!actualizando) {
                int val = parseDim(comboDim.getSelectedItem().toString());
                if (val >= 3 && val <= 61) {
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

    /**
     * Crear panell d'estadístiques.
     */
    private JPanel crearPanelEstadisticas() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Files de factors de ramificació, dinàmiques segons les peces.
        panelFactors = new JPanel();
        panelFactors.setLayout(new BoxLayout(panelFactors, BoxLayout.Y_AXIS));
        panelFactors.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(panelFactors);
        panel.add(Box.createVerticalStrut(4));

        // Temps real
        lblTempsReal = miniLabel("Temps real: —");
        lblTempsReal.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(lblTempsReal);
        panel.add(Box.createVerticalStrut(2));

        // Constant multiplicativa
        lblConstant = miniLabel("Constant C (s/node): —");
        lblConstant.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(lblConstant);
        panel.add(Box.createVerticalStrut(2));

        // Temps previst
        lblTempsPrevist = miniLabel("Temps previst: —");
        lblTempsPrevist.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(lblTempsPrevist);

        return panel;
    }

    // ── Càlcul d'estimació ──────────────────────────────────────────────── 
    /**
     * Recalcula i mostra els factors de ramificació, el temps real i la
     * constant.
     */
    public void actualizarEstimacion() {
        if (panelFactors == null) {
            return;
        }

        // ── Factors de ramificació per peça
        panelFactors.removeAll();
        List<String> clases = dat.getClassesPeces();
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

        // ── Informació per defecte fins que s'executi
        lblTempsReal.setText("Temps real: —");
        lblConstant.setText("Constant C: —");
        lblTempsPrevist.setText("Temps previst: (cal executar 1 cop)");
    }

    /**
     * Rep l'actualització de velocitat mesurada per mantenir la UI
     * sincronitzada.
     */
    public void actualizarVelocitat(long nps) {
        if (lblTempsReal == null) {
            return;
        }
    }

    /**
     * Actualitza el temps real, la constant i el temps previst a la UI.
     */
    public void actualizarEstadistica(double tempsReal, double constant, double tempsPrevision) {
        if (lblTempsReal == null) {
            return;
        }

        lblTempsReal.setText("Temps real: " + formatTemps(tempsReal));
        lblConstant.setText("Constant C: " + formatConstantPerNode(constant));
        lblTempsPrevist.setText("Temps previst: " + formatTemps(tempsPrevision));
    }

    /**
     * Formata la constant per node amb unitats adaptatives.
     */
    private String formatConstantPerNode(double secondsPerNode) {
        if (!Double.isFinite(secondsPerNode) || secondsPerNode < 0) {
            return "—";
        }
        if (secondsPerNode < 1e-6) {
            return String.format("%.3f ns/node", secondsPerNode * 1e9);
        }
        if (secondsPerNode < 1e-3) {
            return String.format("%.3f us/node", secondsPerNode * 1e6);
        }
        if (secondsPerNode < 1) {
            return String.format("%.3f ms/node", secondsPerNode * 1e3);
        }
        return String.format("%.3f s/node", secondsPerNode);
    }

    /**
     * Formatea el temps en segons a un format llegible (ms o s).
     */
    private String formatTemps(double segundos) {
        if (!Double.isFinite(segundos)) {
            return "∞";
        }
        if (segundos < 0.001) {
            return String.format("%.4f ms", segundos * 1000);
        } else if (segundos < 1) {
            return String.format("%.2f ms", segundos * 1000);
        } else {
            return String.format("%.2f s", segundos);
        }
    }

    // ── Construcció de files ──────────────────────────────────────────────── 
    private void construirSlots() {
        actualizando = true;
        slotPanel.removeAll();
        combos.clear();
        List<String> clases = dat.getClassesPeces();
        for (int i = 0; i < clases.size(); i++) {
            slotPanel.add(crearFila(i, clases.get(i)));
            slotPanel.add(Box.createVerticalStrut(4));
        }
        actualizando = false;
        slotPanel.revalidate();
        slotPanel.repaint();
    }

    /**
     * Crea una fila amb un número de peça i un JComboBox per seleccionar la
     * peça.
     *
     * @param idx índex de la peça a la llista
     * @param claseActual nom de la classe de peça actual seleccionada
     * @return JPanel amb la fila completa
     */
    private JPanel crearFila(int idx, String claseActual) {
        // ── Panell principal de la fila
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        fila.setAlignmentX(LEFT_ALIGNMENT);

        // ── Badge amb número de peça
        JLabel badge = new JLabel(String.valueOf(idx + 1));
        badge.setForeground(Color.WHITE);
        badge.setBackground(COLORES[idx % COLORES.length]);
        badge.setOpaque(true);
        badge.setFont(badge.getFont().deriveFont(Font.BOLD, 12f));
        badge.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        fila.add(badge);

        // ── ComboBox amb peces disponibles
        JComboBox<Peca> combo = new JComboBox<>(disponibles);
        combo.setRenderer(new PecaRenderer());
        combo.setFont(combo.getFont().deriveFont(12f));

        // ── Seleccionar peça actual
        for (Peca p : disponibles) {
            if (p.getNombre().equals(claseActual)) {
                combo.setSelectedItem(p);
                break;
            }
        }

        // ── Notificar canvi al controlador
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

    // ── Afegir una peça ────────────────────────────────────────────────
    /**
     * Notifica al controlador que s'ha d'afegir una nova peça.
     */
    private void agregarPeca() {
        if (disponibles.length > 0) {
            prog.notificar("config:pieza-add-" + disponibles[0].getNombre());
        }
    }

    // ── Treure una peça ────────────────────────────────────────────────
    /**
     * Notifica al controlador que s'ha de treure una peça (si n'hi ha més
     * d'una).
     */
    private void quitarPeca() {
        if (dat.getNumPeces() > 1) {
            prog.notificar("config:pieza-remove");
        }
    }

    // ── Actualització pública ─────────────────────────────────────────
    /**
     * Actualitza les dades del panell, reconstruint els slots i ajustant les
     * seleccions.
     *
     * @param d noves dades del taulell i peces
     */
    public void actualizar(Dades d) {
        dat = d;
        disponibles = dat.getPecesDisponibles();
        construirSlots();

        // ── Actualitzar selector de dimensió
        actualizando = true;
        comboDim.setSelectedItem(dat.getDimensio() + "×" + dat.getDimensio());
        actualizando = false;
        aplicarModeIniciSeleccionat();
        actualizarEstimacion();
    }

    /**
     * Crea un JLabel amb estil de secció.
     *
     * @param text text a mostrar
     * @return JLabel formatat
     */
    private JLabel seccionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 12f));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    /**
     * Crea un JLabel petit per mostrar informació secundària.
     *
     * @param text text a mostrar
     * @return JLabel formatat
     */
    private JLabel miniLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(11f));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    /**
     * Crea un separador horitzontal amb amplada màxima.
     *
     * @return JSeparator formatat
     */
    private JSeparator separador() {
        JSeparator s = new JSeparator();
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        return s;
    }

    /**
     * Retorna el nom simple d'una classe donada la seva ruta completa.
     *
     * @param fqn nom complet de la classe
     * @return nom simple de la classe
     */
    private static String simpleName(String fqn) {
        int dot = fqn.lastIndexOf('.');
        return dot >= 0 ? fqn.substring(dot + 1) : fqn;
    }

    /**
     * Extreu el primer enter d'un text com "8×8", "8x8" o "8" i el retorna.
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

    // ── Renderitzador de peces ─────────────────────────────────────────
    /**
     * Personalitza el renderitzat de les peces amb text i icona.
     */
    private class PecaRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Peca) {
                Peca p = (Peca) value;
                setText(simpleName(p.getNombre()));
                URL url = getClass().getResource("/imatges/" + p.getImagen());
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
