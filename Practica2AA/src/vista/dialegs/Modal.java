/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vista.dialegs;

import model.Dades;
import control.Notificar;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 *
 * @author mascport
 */
public class Modal extends JDialog implements ActionListener {

    private JButton ok;
    private Notificar prog;
    private String msg;
    private JPanel interior;

    public Modal(Notificar p, String m, int d) {
        super((java.awt.Frame) null, "Configurar tablero", true);
        prog = p;
        msg = m;
        interior = new ParaInteger(prog, msg, d);
        constructA();
        this.add(interior, BorderLayout.CENTER);
        constructB();
    }

    public Modal(Notificar p, String m, Dades d) {
        super((java.awt.Frame) null, "Seleccionar pieza", true);
        prog = p;
        msg = m;
        interior = new ParaPieza(prog, msg, d);
        constructA();
        this.add(interior, BorderLayout.CENTER);
        constructB();
    }

    public Modal(Notificar p, String m) {
        super((java.awt.Frame) null, "Resultado", true);
        prog = p;
        constructA();
        this.add(new Alerta(prog, m), BorderLayout.CENTER);
        constructB();
    }

    private void constructA() {
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        this.setLayout(new BorderLayout());
    }

    private void constructB() {
        ok = new JButton("Aceptar");
        ok.addActionListener(this);
        JPanel botPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER));
        botPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 0, 6, 0));
        botPanel.add(ok);
        this.add(botPanel, BorderLayout.SOUTH);
        this.pack();
        this.setLocationRelativeTo(null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == ok) {
            if (interior instanceof ParaInteger) {
                ((ParaInteger) interior).notificar();
            }
            this.dispose();
        }
    }
}
