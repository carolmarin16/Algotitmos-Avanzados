package vista;

import control.Notificar;
import model.Dades;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.Hashtable;
import javax.swing.*;
import javax.swing.event.*;

public class Gui implements ActionListener, ChangeListener {

    private JFrame vent;
    private JPanel contenedor;

    private PanelCentral central;
    private PanelConfiguracio config;
    private JSplitPane split;

    private JToolBar toolbar;
    private JSlider slider;
    private JLabel statusLabel;
    private JLabel dimLabel;
    private JButton btnParar;

    private Dades dat;
    private Notificar prog;

    // ── Constructor ────────────────────────────────────────────────────────
    public Gui(String titulo, int w, int h, Dades d, Notificar p) {
        dat = d;
        prog = p;
        vent = new JFrame(titulo);
        vent.setPreferredSize(new Dimension(w, h));
        crear();
    }

    // ── Construcción ───────────────────────────────────────────────────────
    private void crear() {
        // ── Ventana
        vent.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        vent.setMinimumSize(new Dimension(780, 550));

        contenedor = new JPanel(new BorderLayout());
        vent.setContentPane(contenedor);

        // ── Menú
        vent.setJMenuBar(crearMenuBar());

        // ── Toolbar (NORTH)
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        contenedor.add(toolbar, BorderLayout.NORTH);

        // ── Panel central (tablero) + panel lateral (configuración)
        central = new PanelCentral(dat);
        config = new PanelConfiguracio(prog, dat);

        config.setMinimumSize(new Dimension(210, 200));
        config.setMaximumSize(new Dimension(270, Integer.MAX_VALUE));

        split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, central, config);
        split.setResizeWeight(1.0);       // el tablero absorbe todo el espacio extra
        split.setDividerSize(6);
        split.setBorder(null);
        split.setOneTouchExpandable(true);
        contenedor.add(split, BorderLayout.CENTER);

        // ── Panel inferior: slider animación + barra de estado (SOUTH)
        contenedor.add(crearPanelSur(), BorderLayout.SOUTH);
    }

    private JMenuBar crearMenuBar() {
        JMenuBar mb = new JMenuBar();

        JMenu mAlg = new JMenu("Algoritmo");
        JMenuItem miCalc = new JMenuItem("Resolver");
        miCalc.setActionCommand("barra:calcular");
        miCalc.addActionListener(this);
        JMenuItem miParar = new JMenuItem("Detener");
        miParar.setActionCommand("barra:parar");
        miParar.addActionListener(this);
        mAlg.add(miCalc);
        mAlg.addSeparator();
        mAlg.add(miParar);

        mb.add(mAlg);
        return mb;
    }

    private JPanel crearPanelSur() {
        JPanel south = new JPanel(new BorderLayout());

        // Slider de animación
        JPanel sliderPanel = new JPanel(new BorderLayout(6, 0));
        sliderPanel.setBorder(BorderFactory.createEmptyBorder(6, 12, 4, 12));

        JLabel lblAnim = new JLabel("Animació del recorregut: ");
        lblAnim.setToolTipText("Esquerra = càlcul ràpid  |  Dreta = animació pas a pas");
        sliderPanel.add(lblAnim, BorderLayout.WEST);

        slider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
        slider.addChangeListener(this);
        slider.setMajorTickSpacing(50);
        slider.setMinorTickSpacing(10);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        Hashtable<Integer, JLabel> labels = new Hashtable<>();
        labels.put(0, new JLabel("Ràpida"));
        labels.put(50, new JLabel("Normal"));
        labels.put(100, new JLabel("Detallada"));
        slider.setLabelTable(labels);
        sliderPanel.add(slider, BorderLayout.CENTER);

        // Barra de estado
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        statusLabel = new JLabel("Llest");
        dimLabel = new JLabel("  |  Dimensió: " + dat.getDimension() + "×" + dat.getDimension());
        statusBar.add(statusLabel);
        statusBar.add(dimLabel);

        south.add(sliderPanel, BorderLayout.CENTER);
        south.add(statusBar, BorderLayout.SOUTH);
        return south;
    }

    // ── API pública ────────────────────────────────────────────────────────
    /**
     * Añade un botón a la toolbar. Si es el botón de parar, lo guarda y lo
     * deshabilita.
     */
    public void ponOpcion(String imageName, String actionCommand, String tooltip, String text) {
        JButton btn = makeNavigationButton(imageName, actionCommand, tooltip, text);
        if ("barra:parar".equals(actionCommand)) {
            btnParar = btn;
            btnParar.setEnabled(false);
            btnParar.setForeground(new Color(180, 30, 30));
        }
        toolbar.add(btn);
    }

    public void visualizar() {
        vent.pack();
        vent.setLocationRelativeTo(null);
        vent.setVisible(true);
    }

    public void repintar() {
        central.repaint();
    }

    public void setImagenPieza(Dades d) {
        central.setImagenPeca(d);
    }

    public void setDatos(Dades d) {
        dat = d;
        central.setDatos(d);
        config.actualizar(d);
        if (dimLabel != null) {
            dimLabel.setText("  |  Dimensió: " + d.getDimension() + "×" + d.getDimension());
        }
        actualizarEstimacion();
    }

    public void setStatus(String msg) {
        if (statusLabel != null) {
            statusLabel.setText(msg);
        }
    }

    public void setCalculando(boolean calc) {
        if (btnParar != null) {
            btnParar.setEnabled(calc);
        }
    }

    public void actualizarEstimacion() {
        if (config != null) {
            config.actualizarEstimacion();
        }
    }

    public void actualizarVelocitat(long nps) {
        if (config != null) {
            config.actualizarVelocitat(nps);
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    private JButton makeNavigationButton(String imageName, String actionCommand,
            String tooltip, String altText) {
        JButton button = new JButton();
        button.setActionCommand(actionCommand);
        button.setToolTipText(tooltip);
        button.addActionListener(this);

        URL imageURL = getClass().getResource("/" + imageName);
        if (imageURL != null) {
            Image scaled = new ImageIcon(imageURL).getImage()
                    .getScaledInstance(22, 22, Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(scaled));
        } else {
            System.err.println("Imagen no encontrada: /" + imageName);
        }
        if (!altText.isEmpty()) {
            button.setText(altText);
            button.setHorizontalTextPosition(SwingConstants.RIGHT);
        }
        return button;
    }

    // ── Listeners ──────────────────────────────────────────────────────────
    @Override
    public void actionPerformed(ActionEvent e) {
        prog.notificar(e.getActionCommand());
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        int v = ((JSlider) e.getSource()).getValue();
        prog.notificar("velocidad:" + v);
    }
}
