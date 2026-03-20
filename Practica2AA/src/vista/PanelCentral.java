package vista;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.swing.*;
import model.Dades;

/**
 * PanelCentral és el JPanel que representa visualment el tauler del joc.
 * Dibuixa el tauler quadrat, les peces amb les seves imatges, els números de
 * pas i les fletxes que indiquen moviments.
 */
public class PanelCentral extends JPanel {

    private Dades dat;
    private ImageIcon[] imatgesPeces;
    private boolean mostrarFletxa = false;
    private int fletxaFilaIni;
    private int fletxaColIni;
    private int fletxaFilaFi;
    private int fletxaColFi;
    private Color fletxaColor = new Color(20, 20, 20, 200);

    // Paleta de colors per als números de pas, un color per peça.
    private static final Color[] COLORS_NUM = {
        new Color(20, 80, 200), // blau
        new Color(170, 20, 20), // vermell
        new Color(20, 140, 50), // verd
        new Color(150, 80, 0), // taronja
        new Color(100, 0, 150), // morat
        new Color(0, 130, 150), // cian
        new Color(180, 140, 0), // daurat
        new Color(100, 50, 20), // marró
    };

    // Colors clars i foscos del tauler 
    private static final Color COLOR_CLAR = new Color(240, 217, 181);
    private static final Color COLOR_OBSCUR = new Color(181, 136, 99);
    private static final Color COLOR_NUM_BG = new Color(255, 255, 255, 190);

    // ── Constructor ───────────────────────────────────────────
    /**
     * Inicialitza el panell central del tauler.
     *
     * @param d dades del taulell i configuració
     */
    public PanelCentral(Dades d) {
        dat = d;
        carregarImatges();
        setPreferredSize(new Dimension(600, 600));
        setBackground(new Color(240, 240, 240));
        setOpaque(true);
    }

    /**
     * Retorna la mida preferida del panell, sempre quadrada.
     */
    @Override
    public Dimension getPreferredSize() {
        int w = getWidth();
        int h = getHeight();
        int size = Math.min(w, h);
        if (size <= 0) {
            size = 600;
        }
        return new Dimension(size, size);
    }

    /**
     * Actualitza les dades i recarrega les imatges de les peces.
     *
     * @param d noves dades del tauler
     */
    public void setImatgePeca(Dades d) {
        dat = d;
        carregarImatges();
    }

    /**
     * Carrega les imatges de totes les peces a partir de les dades.
     */
    private void carregarImatges() {
        int n = dat.getNumPeces();
        imatgesPeces = new ImageIcon[n];
        for (int i = 0; i < n; i++) {
            URL url = getClass().getResource("/imatges/" + dat.getImatge(i));
            if (url != null) {
                imatgesPeces[i] = new ImageIcon(url);
            }
        }
    }

    /**
     * Redibuixa el panell utilitzant paint directe.
     */
    @Override
    public void repaint() {
        if (this.getGraphics() != null) {
            paint(this.getGraphics());
        }
    }

    // ── Pintar el panell ────────────────────────────────────
    /**
     * Dibuixa el tauler, les peces, els números i la fletxa si cal.
     *
     * @param gr objecte Graphics on dibuixar
     */
    @Override
    public void paint(Graphics gr) {
        int w = this.getWidth();
        int h = this.getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }

        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();

        // ── Millora de renderitzat ───────────────────────────
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Fons gris clar
        g.setColor(new Color(240, 240, 240));
        g.fillRect(0, 0, w, h);

        int dim = dat.getDimensio();
        int numP = dat.getNumPeces();

        // ── Tauler quadrat centrat, sense marge negre ───────────────────
        int boardSize = Math.min(w, h);   // ocupar tot l'espai disponible
        int cel = boardSize / dim;        // mida de casella en enters
        boardSize = cel * dim;            // ajust exacte al múltiple de la dimensió
        int offX = (w - boardSize) / 2;
        int offY = (h - boardSize) / 2;

        // ── Dibuixar cel·les ───────────────────────────────
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                g.setColor(((i + j) % 2 == 0) ? COLOR_CLAR : COLOR_OBSCUR);
                g.fillRect(offX + j * cel, offY + i * cel, cel, cel);
            }
        }

        // ── Peces i números ──────────────────────────────────────────────
        int fontSize = Math.max(9, Math.min(cel / 3, 16));
        g.setFont(new Font("SansSerif", Font.BOLD, fontSize));
        FontMetrics fm = g.getFontMetrics();
        int margen = Math.max(2, cel / 10);

        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                if (!dat.hiHaPeca(i, j)) {
                    continue;
                }

                int step = dat.getVal(i, j);
                int pieceIdx = (step - 1) % numP;

                // ── Dibuixar imatge ───────────────────────
                if (imatgesPeces != null && pieceIdx < imatgesPeces.length
                        && imatgesPeces[pieceIdx] != null) {
                    g.drawImage(imatgesPeces[pieceIdx].getImage(),
                            offX + j * cel + margen,
                            offY + i * cel + margen,
                            cel - 2 * margen,
                            cel - 2 * margen, this);
                }

                // ── Número amb insígnia ──────────────────
                String num = String.valueOf(step);
                int tw = fm.stringWidth(num);
                int th = fm.getAscent();
                int nx = offX + j * cel + 3;
                int ny = offY + i * cel + th + 1;

                g.setColor(COLOR_NUM_BG);
                g.fillRoundRect(nx - 1, ny - th, tw + 4, th + 3, 5, 5);
                g.setColor(COLORS_NUM[pieceIdx % COLORS_NUM.length]);
                g.drawString(num, nx, ny);
            }
        }

        // ── Fletxa de moviment ─────────────────────────────
        if (mostrarFletxa) {
            int x1 = offX + fletxaColIni * cel + cel / 2;
            int y1 = offY + fletxaFilaIni * cel + cel / 2;
            int x2 = offX + fletxaColFi * cel + cel / 2;
            int y2 = offY + fletxaFilaFi * cel + cel / 2;

            g.setColor(fletxaColor);
            g.setStroke(new BasicStroke(Math.max(2f, cel / 10f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(x1, y1, x2, y2);

            double angle = Math.atan2(y2 - y1, x2 - x1);
            int head = Math.max(8, cel / 4);
            int hx1 = (int) (x2 - head * Math.cos(angle - Math.PI / 6));
            int hy1 = (int) (y2 - head * Math.sin(angle - Math.PI / 6));
            int hx2 = (int) (x2 - head * Math.cos(angle + Math.PI / 6));
            int hy2 = (int) (y2 - head * Math.sin(angle + Math.PI / 6));

            Polygon punta = new Polygon();
            punta.addPoint(x2, y2);
            punta.addPoint(hx1, hy1);
            punta.addPoint(hx2, hy2);
            g.fillPolygon(punta);
        }

        // ── Vora del tauler ───────────────────────────────────────────────
        g.setColor(new Color(80, 50, 20));
        g.setStroke(new BasicStroke(2));
        g.drawRect(offX, offY, boardSize, boardSize);

        g.dispose();
        gr.drawImage(bi, 0, 0, this);
    }

    // ── Actualitzar dades ───────────────────────────────────
    /**
     * Canvia les dades i recarrega les imatges.
     *
     * @param d noves dades del taulell
     */
    public void setDades(Dades d) {
        dat = d;
        carregarImatges();
    }

    /**
     * Mostra una fletxa negra de moviment.
     *
     * @param filaIni fila inicial
     * @param colIni columna inicial
     * @param filaFi fila final
     * @param colFi columna final
     */
    public void mostrarFletxaMoviment(int filaIni, int colIni, int filaFi, int colFi) {
        this.fletxaFilaIni = filaIni;
        this.fletxaColIni = colIni;
        this.fletxaFilaFi = filaFi;
        this.fletxaColFi = colFi;
        this.fletxaColor = new Color(20, 20, 20, 200);
        this.mostrarFletxa = true;
    }

    /**
     * Mostra una fletxa de moviment amb color segons la peça.
     *
     * @param filaIni fila inicial
     * @param colIni columna inicial
     * @param filaFi fila final
     * @param colFi columna final
     * @param pieceIdx índex de la peça per triar color
     */
    public void mostrarFletxaMoviment(int filaIni, int colIni, int filaFi, int colFi, int pieceIdx) {
        this.fletxaFilaIni = filaIni;
        this.fletxaColIni = colIni;
        this.fletxaFilaFi = filaFi;
        this.fletxaColFi = colFi;
        Color base = COLORS_NUM[Math.floorMod(pieceIdx, COLORS_NUM.length)];
        this.fletxaColor = new Color(base.getRed(), base.getGreen(), base.getBlue(), 220);
        this.mostrarFletxa = true;
    }

    /**
     * Amaga la fletxa de moviment.
     */
    public void amagarFletxaMoviment() {
        this.mostrarFletxa = false;
    }

    /**
     * Converteix coordenades en píxels a indices de fila i columna.
     *
     * @param x posició horitzontal en píxels
     * @param y posició vertical en píxels
     * @return array [fila, columna] o null si fora del tauler
     */
    public int[] casellaDePixel(int x, int y) {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) {
            return null;
        }

        int dim = dat.getDimensio();
        int boardSize = Math.min(w, h);
        int cel = boardSize / dim;
        if (cel <= 0) {
            return null;
        }
        boardSize = cel * dim;
        int offX = (w - boardSize) / 2;
        int offY = (h - boardSize) / 2;

        if (x < offX || y < offY || x >= offX + boardSize || y >= offY + boardSize) {
            return null;
        }

        int col = (x - offX) / cel;
        int fila = (y - offY) / cel;
        return new int[]{fila, col};
    }
}
