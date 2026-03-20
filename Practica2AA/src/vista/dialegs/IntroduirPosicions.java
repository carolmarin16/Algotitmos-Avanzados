package vista.dialegs;

import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Diàleg que demana a l'usuari les posicions inicials de cada peça.
 *
 * Retorna una matriu [numPeces][2] amb les posicions introduïdes,
 * o null si l'usuari cancel·la.
 * 
 * @author Equip
 */
public class IntroduirPosicions {

    /**
     * Mostra el diàleg per introduir la posició d'una peça concreta.
     *
     * @param numPeca  número de peça (1-based, per mostrar a l'usuari)
     * @param totalPeces total de peces
     * @param dimMax dimensió del tauler - 1 (rang màxim)
     * @return array [fila, columna] introduïts, o null si es cancel·la
     */
    public static int[] demanarPosicio(int numPeca, int totalPeces, int dimMax) {
        JTextField txtFila = new JTextField(4);
        JTextField txtCol = new JTextField(4);

        JPanel panel = new JPanel(new GridLayout(2, 2, 4, 6));
        panel.add(new JLabel("Fila (0-" + dimMax + "):"));
        panel.add(txtFila);
        panel.add(new JLabel("Columna (0-" + dimMax + "):"));
        panel.add(txtCol);

        int res = JOptionPane.showConfirmDialog(null, panel,
                "Peça " + numPeca + " de " + totalPeces + " — posició inicial",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res != JOptionPane.OK_OPTION) {
            return null;
        }

        try {
            int fila = Integer.parseInt(txtFila.getText().trim());
            int col  = Integer.parseInt(txtCol.getText().trim());
            return new int[]{fila, col};
        } catch (NumberFormatException e) {
            return new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE}; // senyal d'entrada no vàlida
        }
    }

    /**
     * Mostra un missatge d'error a l'usuari.
     *
     * @param msg text del missatge
     */
    public static void mostrarError(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
