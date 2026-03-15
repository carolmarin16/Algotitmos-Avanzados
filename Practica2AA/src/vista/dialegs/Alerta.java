package vista.dialegs;

import java.awt.*;
import javax.swing.*;
import control.Notificar;

public class Alerta extends JPanel {

    public Alerta(Notificar n, String m) {
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(14, 20, 10, 20));

        JLabel label = new JLabel("<html><body style='text-align:center'>" + m + "</body></html>");
        label.setFont(label.getFont().deriveFont(13f));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(label, BorderLayout.CENTER);
    }
}
