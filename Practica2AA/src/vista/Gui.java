package vista;

import control.Notificar;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.Hashtable;
import javax.swing.*;
import javax.swing.event.*;
import model.Dades;
import vista.dialegs.IntroduirPosicions;
import vista.dialegs.Modal;

/**
 * Classe principal de la interfície gràfica. Gestiona la finestra principal,
 * els panells, la barra d'eines i els controls de reproducció de la solució.
 */
public class Gui implements ActionListener, ChangeListener {

    private JFrame finestra;
    private JPanel contenidor;

    private PanelCentral central;
    private PanelConfiguracio config;
    private JSplitPane split;

    private JToolBar toolbar;
    private JSlider slider;
    private JLabel statusLabel;
    private JLabel dimLabel;
    private JButton btnAturar;
    private JCheckBox chkPasAPas;
    private JCheckBox chkMostrarFletxa;
    private JButton btnPasInici;
    private JButton btnPasAnterior;
    private JButton btnPasPlay;
    private JButton btnPasSeguent;
    private JButton btnPasFinal;
    private JLabel lblPasInfo;
    private boolean actualitzantControls = false;

    private Dades dat;
    private Notificar prog;

    // ── Constructor ────────────────────────────────────────────────────────
    /**
     * Inicialitza la GUI principal.
     *
     * @param titulo títol de la finestra
     * @param w amplada inicial
     * @param h altura inicial
     * @param d dades del taulell i configuració
     * @param p objecte Notificar per avisar controladors
     */
    public Gui(String titulo, int w, int h, Dades d, Notificar p) {
        dat = d;
        prog = p;
        finestra = new JFrame(titulo);
        finestra.setPreferredSize(new Dimension(w, h));
        crear();
    }

    // ── Construcció de la GUI ─────────────────────────────────────────────
    private void crear() {
        // ── Finestra
        finestra.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        finestra.setMinimumSize(new Dimension(780, 550));

        contenidor = new JPanel(new BorderLayout());
        finestra.setContentPane(contenidor);

        // ── Menú
        finestra.setJMenuBar(crearMenuBar());

        // ── Barra d'eines (NORTH)
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        contenidor.add(toolbar, BorderLayout.NORTH);
        inicialitzarOpcionsBarra();

        // ── Panell central (tauler) + panell lateral (configuració)
        central = new PanelCentral(dat);
        config = new PanelConfiguracio(prog, dat);

        config.setMinimumSize(new Dimension(210, 200));
        config.setMaximumSize(new Dimension(270, Integer.MAX_VALUE));

        split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, central, config);
        split.setResizeWeight(1.0);       // el tauler absorbeix tot l'espai extra
        split.setDividerSize(0);
        split.setBorder(null);
        split.setOneTouchExpandable(false);
        split.setEnabled(false);

        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.add(split, BorderLayout.CENTER);
        centerWrap.add(crearPanelReproduccio(), BorderLayout.SOUTH);
        contenidor.add(centerWrap, BorderLayout.CENTER);

        // ── Panell inferior: control d'animació + barra d'estat (SOUTH)
        contenidor.add(crearPanelSur(), BorderLayout.SOUTH);
    }

    /**
     * Afegeix les opcions estàndard de la barra principal.
     */
    private void inicialitzarOpcionsBarra() {
        posarOpcio("bombeta.png", "barra:calcular",
                "Resoldre el problema", "Resoldre");
        posarOpcio("aturar.png", "barra:aturar",
                "Aturar l'algorisme en curs", "Aturar");
        posarOpcio("netejar.png", "barra:netejar",
                "Netejar el tauler", "Netejar");
        posarOpcio("sortir.png", "barra:sortir",
                "Tancar l'aplicació", "Sortir");
    }

    /**
     * Creació del menú superior.
     */
    private JMenuBar crearMenuBar() {
        JMenuBar mb = new JMenuBar();

        JMenu mAlg = new JMenu("Algorisme");

        // Opcions del menú
        JMenuItem miCalc = new JMenuItem("Resoldre");
        miCalc.setActionCommand("barra:calcular");
        miCalc.addActionListener(this);
        JMenuItem miParar = new JMenuItem("Aturar");
        miParar.setActionCommand("barra:aturar");
        miParar.addActionListener(this);
        JMenuItem miNetejar = new JMenuItem("Netejar");
        miNetejar.setActionCommand("barra:netejar");
        miNetejar.addActionListener(this);
        JMenuItem miSortir = new JMenuItem("Sortir");
        miSortir.setActionCommand("barra:sortir");
        miSortir.addActionListener(this);
        mAlg.add(miCalc);
        mAlg.add(miParar);
        mAlg.addSeparator();
        mAlg.add(miNetejar);
        mAlg.add(miSortir);

        mb.add(mAlg);
        return mb;
    }

    /**
     * Panell inferior amb slider i barra d'estat.
     */
    private JPanel crearPanelSur() {
        JPanel south = new JPanel(new BorderLayout());

        // Control lliscant de l'animació
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

        // Barra d'estat
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        statusLabel = new JLabel("Llest");
        dimLabel = new JLabel("  |  Dimensió: " + dat.getDimensio() + "×" + dat.getDimensio());
        statusBar.add(statusLabel);
        statusBar.add(dimLabel);

        south.add(sliderPanel, BorderLayout.CENTER);
        south.add(statusBar, BorderLayout.SOUTH);
        return south;
    }

    /**
     * Panell de reproducció amb controls pas a pas.
     */
    private JPanel crearPanelReproduccio() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        panel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        // Checkbox activar pas a pas
        chkPasAPas = new JCheckBox("Activar pas a pas");
        chkPasAPas.setSelected(dat.isPasAPasActivat());
        chkPasAPas.addActionListener(e -> {
            if (!actualitzantControls) {
                prog.notificar("config:pas-a-pas-" + chkPasAPas.isSelected());
            }
        });

        // Checkbox mostrar fletxa
        chkMostrarFletxa = new JCheckBox("Mostrar fletxa");
        chkMostrarFletxa.setSelected(dat.isMostrarFletxaMoviment());
        chkMostrarFletxa.addActionListener(e -> {
            if (!actualitzantControls) {
                prog.notificar("config:mostrar-fletxa-" + chkMostrarFletxa.isSelected());
            }
        });

        // Botons reproducció
        btnPasInici = new JButton("⏮");
        btnPasInici.setActionCommand("repro:inici");
        btnPasInici.addActionListener(this);
        btnPasInici.setToolTipText("Anar al principi");
        btnPasAnterior = new JButton("<");
        btnPasAnterior.setActionCommand("repro:anterior");
        btnPasAnterior.addActionListener(this);
        btnPasPlay = new JButton("▶");
        btnPasPlay.setActionCommand("repro:play");
        btnPasPlay.addActionListener(this);
        btnPasPlay.setToolTipText("Play / Pausa");
        btnPasSeguent = new JButton(">");
        btnPasSeguent.setActionCommand("repro:seguent");
        btnPasSeguent.addActionListener(this);
        btnPasFinal = new JButton("⏭");
        btnPasFinal.setActionCommand("repro:final");
        btnPasFinal.addActionListener(this);
        btnPasFinal.setToolTipText("Anar al final");
        lblPasInfo = new JLabel(" ");

        // Afegir components al panell
        panel.add(chkPasAPas);
        panel.add(chkMostrarFletxa);
        panel.add(Box.createHorizontalStrut(16));
        panel.add(btnPasInici);
        panel.add(btnPasAnterior);
        panel.add(btnPasPlay);
        panel.add(btnPasSeguent);
        panel.add(btnPasFinal);
        panel.add(lblPasInfo);
        setPlaybackDisponible(false);

        return panel;
    }

    // ── API pública ────────────────────────────────────────────────────────
    /**
     * Afegeix un botó a la barra d'eines. Si és el botó d'aturar, el desa i el
     * deshabilita.
     */
    public void posarOpcio(String imageName, String actionCommand, String tooltip, String text) {
        JButton btn = makeNavigationButton(imageName, actionCommand, tooltip, text);
        if ("barra:aturar".equals(actionCommand)) {
            btnAturar = btn;
            btnAturar.setEnabled(false);
            btnAturar.setForeground(new Color(180, 30, 30));
        }
        toolbar.add(btn);
    }

    /**
     * Mètode per mostrar la finestra.
     */
    public void visualitzar() {
        finestra.pack();
        finestra.setLocationRelativeTo(null);
        finestra.setVisible(true);
    }

    /**
     * Refresca el PanelCentral.
     */
    public void repintar() {
        central.repaint();
    }

    /**
     * Mostrar la fletxa de moviment al taulell.
     */
    public void mostrarFletxaMoviment(int filaIni, int colIni, int filaFi, int colFi) {
        central.mostrarFletxaMoviment(filaIni, colIni, filaFi, colFi);
    }

    /**
     * Mostrar la fletxa de moviment al taulell.
     */
    public void mostrarFletxaMoviment(int filaIni, int colIni, int filaFi, int colFi, int pieceIdx) {
        central.mostrarFletxaMoviment(filaIni, colIni, filaFi, colFi, pieceIdx);
    }

    /**
     * Amagar la fletxa de moviment al taulell.
     */
    public void amagarFletxaMoviment() {
        central.amagarFletxaMoviment();
    }

    /**
     * Actualitza la imatge de cada peça.
     */
    public void setImatgePeca(Dades d) {
        central.setImatgePeca(d);
    }

    /**
     * Actualitzar tot l’estat de la GUI.
     */
    public void setDades(Dades d) {
        dat = d;
        central.setDades(d);
        config.actualizar(d);
        if (chkPasAPas != null) {
            actualitzantControls = true;
            chkPasAPas.setSelected(d.isPasAPasActivat());
            chkMostrarFletxa.setSelected(d.isMostrarFletxaMoviment());
            actualitzantControls = false;
        }
        if (dimLabel != null) {
            dimLabel.setText("  |  Dimensió: " + d.getDimensio() + "×" + d.getDimensio());
        }
        actualitzarEstimacio();
    }

    /**
     * Actualitza la barra d’estat.
     */
    public void setStatus(String msg) {
        if (statusLabel != null) {
            statusLabel.setText(msg);
        }
    }

    /**
     * Mostra un diàleg modal informatiu.
     */
    public void mostrarModal(String missatge) {
        new Modal(prog, missatge).setVisible(true);
    }

    /**
     * Demana a l'usuari la posició inicial d'una peça.
     */
    public int[] demanarPosicioInicial(int numPeca, int totalPeces, int dimMax) {
        return IntroduirPosicions.demanarPosicio(numPeca, totalPeces, dimMax);
    }

    /**
     * Mostra un error de validació d'entrada de posicions.
     */
    public void mostrarErrorPosicio(String msg) {
        IntroduirPosicions.mostrarError(msg);
    }

    /**
     * Habilita el botó d’aturar només quan s’està calculant.
     */
    public void setCalculant(boolean calc) {
        if (btnAturar != null) {
            btnAturar.setEnabled(calc);
        }
    }

    /**
     * Actualitza estimació passos.
     */
    public void actualitzarEstimacio() {
        if (config != null) {
            config.actualizarEstimacion();
        }
    }

    /**
     * Actualitza velocitat.
     */
    public void actualitzarVelocitat(long nps) {
        if (config != null) {
            config.actualizarVelocitat(nps);
        }
    }

    /**
     * Actualitza estadistica.
     */
    public void actualitzarEstadistica(double tempsReal, double constant, double tempsPrevision) {
        if (config != null) {
            config.actualizarEstadistica(tempsReal, constant, tempsPrevision);
        }
    }

    /**
     * Habilita controls de reproducció.
     */
    public void setPlaybackDisponible(boolean disponible) {
        if (btnPasAnterior != null) {
            btnPasInici.setEnabled(disponible);
            btnPasAnterior.setEnabled(disponible);
            btnPasPlay.setEnabled(disponible);
            btnPasSeguent.setEnabled(disponible);
            btnPasFinal.setEnabled(disponible);
        }
        if (lblPasInfo != null && !disponible) {
            lblPasInfo.setText(" ");
        }
    }

    /**
     * Gestiona estat reproducció.
     */
    public void setPlaybackEnMarxa(boolean enMarxa) {
        if (btnPasPlay != null) {
            btnPasPlay.setText(enMarxa ? "⏸" : "▶");
        }
    }

    /**
     * Gestiona estat reproducció.
     */
    public void setPlaybackPasInfo(int actual, int total) {
        if (lblPasInfo != null) {
            lblPasInfo.setText("Pas: " + actual + "/" + total);
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    /**
     * Escala la imatge a 22x22 px.
     */
    private JButton makeNavigationButton(String imageName, String actionCommand,
            String tooltip, String altText) {
        JButton button = new JButton();
        button.setActionCommand(actionCommand);
        button.setToolTipText(tooltip);
        button.addActionListener(this);

        URL imageURL = getClass().getResource("/imatges/" + imageName);
        if (imageURL != null) {
            Image scaled = new ImageIcon(imageURL).getImage()
                    .getScaledInstance(22, 22, Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(scaled));
        } else {
            System.err.println("Imatge no trobada: /" + imageName);
        }
        if (!altText.isEmpty()) {
            button.setText(altText);
            button.setHorizontalTextPosition(SwingConstants.RIGHT);
        }
        return button;
    }

    // ── Listeners ──────────────────────────────────────────────────────────
    /**
     * Envia accions al controlador.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        prog.notificar(e.getActionCommand());
    }

    /**
     * Envia cancis al slider de velocitat.
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        int v = ((JSlider) e.getSource()).getValue();
        prog.notificar("velocitat:" + v);
    }
}
