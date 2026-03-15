/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vista.dialegs;

import control.Notificar;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author mascport
 */
public class ParaInteger extends JPanel implements ActionListener {

    private JTextField numero;
    private Notificar prog;
    private String mensaje;

    public ParaInteger(Notificar n, String m, int dim) {
        prog = n;
        mensaje = m;
        this.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 12));
        this.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 10, 4, 10));
        JLabel lbl = new JLabel("Nueva dimensión: ");
        lbl.setFont(lbl.getFont().deriveFont(13f));
        this.add(lbl);
        numero = new JTextField(5);
        numero.setFont(numero.getFont().deriveFont(13f));
        numero.setText(Integer.toString(dim));
        numero.addActionListener(this);
        this.add(numero);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == numero) {
            notificar();
        }
    }

    public void notificar() {
        try {
            prog.notificar(mensaje + Integer.parseInt(numero.getText()));
        } catch (Exception e) {
            // por si la entrada no es buena
        }
    }
}
