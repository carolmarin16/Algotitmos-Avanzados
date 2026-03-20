package vista.dialegs;

import java.awt.*;
import javax.swing.*;
import control.Notificar;

/**
 * Panell personalitzat per mostrar un missatge d'alerta dins l'aplicació.
 *
 * @author Equip
 */
public class Alerta extends JPanel {

    /**
     * Mètode constructor.
     *
     * @param n
     * @param m
     */
    public Alerta(Notificar n, String m) {
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(14, 20, 10, 20));

        JLabel label = new JLabel("<html><body style='text-align:center'>" + m + "</body></html>");
        label.setFont(label.getFont().deriveFont(13f));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(label, BorderLayout.CENTER);
    }
}
