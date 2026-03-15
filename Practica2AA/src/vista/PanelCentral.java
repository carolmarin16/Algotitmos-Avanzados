package vista;

import model.Dades;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.swing.*;

public class PanelCentral extends JPanel {

    private Dades dat;
    private ImageIcon[] imagenesPeces;

    // Paleta de colores para los números de paso (uno por Peca)
    private static final Color[] COLORES_NUM = {
        new Color(20, 80, 200), // azul
        new Color(170, 20, 20), // rojo
        new Color(20, 140, 50), // verde
        new Color(150, 80, 0), // naranja
        new Color(100, 0, 150), // morado
        new Color(0, 130, 150), // cian
        new Color(180, 140, 0), // dorado
        new Color(100, 50, 20), // marrón
    };

    private static final Color COLOR_CLARO = new Color(240, 217, 181);
    private static final Color COLOR_OSCURO = new Color(181, 136, 99);
    private static final Color COLOR_NUM_BG = new Color(255, 255, 255, 190);

    public PanelCentral(Dades d) {
        dat = d;
        cargarImagenes();
    }

    public void setImagenPeca(Dades d) {
        dat = d;
        cargarImagenes();
    }

    private void cargarImagenes() {
        int n = dat.getNumPeces();
        imagenesPeces = new ImageIcon[n];
        for (int i = 0; i < n; i++) {
            URL url = getClass().getResource(dat.getImagen(i));
            if (url != null) {
                imagenesPeces[i] = new ImageIcon(url);
            }
        }
    }

    @Override
    public void repaint() {
        if (this.getGraphics() != null) {
            paint(this.getGraphics());
        }
    }

    @Override
    public void paint(Graphics gr) {
        int w = this.getWidth();
        int h = this.getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }

        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Fondo oscuro para contraste
        g.setColor(new Color(50, 50, 50));
        g.fillRect(0, 0, w, h);

        int dim = dat.getDimension();
        int numP = dat.getNumPeces();

        // ── Tablero cuadrado centrado ─────────────────────────────────────
        int boardSize = Math.min(w, h) - 4;   // usar el lado más corto
        int cel = boardSize / dim;        // tamaño de casilla (entero)
        boardSize = cel * dim;              // ajustar al múltiplo exacto
        int offX = (w - boardSize) / 2;
        int offY = (h - boardSize) / 2;

        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                g.setColor(((i + j) % 2 == 0) ? COLOR_CLARO : COLOR_OSCURO);
                g.fillRect(offX + j * cel, offY + i * cel, cel, cel);
            }
        }

        // ── Peces y números ─────────────────────────────────────────────
        int fontSize = Math.max(9, Math.min(cel / 3, 16));
        g.setFont(new Font("SansSerif", Font.BOLD, fontSize));
        FontMetrics fm = g.getFontMetrics();
        int margen = Math.max(2, cel / 10);

        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                if (!dat.hayPeca(i, j)) {
                    continue;
                }

                int step = dat.getVal(i, j);
                int pieceIdx = (step - 1) % numP;

                if (imagenesPeces != null && pieceIdx < imagenesPeces.length
                        && imagenesPeces[pieceIdx] != null) {
                    g.drawImage(imagenesPeces[pieceIdx].getImage(),
                            offX + j * cel + margen,
                            offY + i * cel + margen,
                            cel - 2 * margen,
                            cel - 2 * margen, this);
                }

                // Número con insignia
                String num = String.valueOf(step);
                int tw = fm.stringWidth(num);
                int th = fm.getAscent();
                int nx = offX + j * cel + 3;
                int ny = offY + i * cel + th + 1;

                g.setColor(COLOR_NUM_BG);
                g.fillRoundRect(nx - 1, ny - th, tw + 4, th + 3, 5, 5);
                g.setColor(COLORES_NUM[pieceIdx % COLORES_NUM.length]);
                g.drawString(num, nx, ny);
            }
        }

        // ── Borde del tablero ─────────────────────────────────────────────
        g.setColor(new Color(80, 50, 20));
        g.setStroke(new BasicStroke(2));
        g.drawRect(offX, offY, boardSize, boardSize);

        g.dispose();
        gr.drawImage(bi, 0, 0, this);
    }

    public void setDatos(Dades d) {
        dat = d;
        cargarImagenes();
    }
}
