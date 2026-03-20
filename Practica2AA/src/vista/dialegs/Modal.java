package vista.dialegs;

import control.Notificar;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * Diàleg modal personalitzat per mostrar missatges a l'usuari.
 *
 * @author Equip
 */
public class Modal extends JDialog implements ActionListener {

    private JButton ok;
    private Notificar prog;
    private String msg;
    private JPanel interior;

    /**
     * Mètode constructor.
     *
     * @param p
     * @param m
     */
    public Modal(Notificar p, String m) {
        super((java.awt.Frame) null, "Resultat", true);
        prog = p;
        constructA();
        this.add(new Alerta(prog, m), BorderLayout.CENTER);
        constructB();
    }

    /**
     * Configuració inicial del diàleg.
     */
    private void constructA() {
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        this.setLayout(new BorderLayout());
    }

    /**
     * Configuració del panell inferior amb el botó "Acceptar".
     */
    private void constructB() {
        ok = new JButton("Acceptar");
        ok.addActionListener(this);
        JPanel botPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER));
        botPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 0, 6, 0));
        botPanel.add(ok);
        this.add(botPanel, BorderLayout.SOUTH);
        this.pack();
        this.setLocationRelativeTo(null);
    }

    /**
     * Accions a realitzar quan es prem un botó.
     *
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == ok) {
            this.dispose();
        }
    }
}
