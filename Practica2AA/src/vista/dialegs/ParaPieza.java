package vista.dialegs;

import model.Dades;
import model.peces.Peca;
import control.Notificar;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;
import javax.swing.*;

public class ParaPieza extends JPanel implements ActionListener {

    private Notificar prog;
    private String mensaje;

    public ParaPieza(Notificar n, String m, Dades d) {
        prog = n;
        mensaje = m;
        this.setLayout(new BorderLayout(0, 8));
        this.setBorder(BorderFactory.createEmptyBorder(10, 14, 6, 14));

        JLabel titulo = new JLabel("Selecciona la pieza para el recorrido:");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD));
        this.add(titulo, BorderLayout.NORTH);

        hacerRadios(d);
    }

    private void hacerRadios(Dades d) {
        Package pack = d.getClasePeca().getClass().getPackage();
        String paquete = pack.getName();
        URL path = getClass().getResource("/" + paquete.replaceAll("\\.", "/"));
        File dir = new File(path.getPath());
        String[] nombres = dir.list();

        // Filtramos la clase base Pieza.class
        String[] aux = new String[nombres.length - 1];
        int pos = 0;
        for (int i = 0; i < nombres.length; i++) {
            if (!nombres[i].contentEquals("Peca.class")) {
                aux[pos++] = nombres[i].substring(0, nombres[i].indexOf(".class"));
            }
        }
        nombres = aux;

        Peca[] piezas = new Peca[nombres.length];
        for (int i = 0; i < piezas.length; i++) {
            try {
                Class c = Class.forName(paquete + "." + nombres[i]);
                piezas[i] = (Peca) c.newInstance();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        ButtonGroup group = new ButtonGroup();
        JPanel botonesrad = new JPanel();
        botonesrad.setLayout(new BoxLayout(botonesrad, BoxLayout.Y_AXIS));

        for (int i = 0; i < piezas.length; i++) {
            URL imageURL = getClass().getResource(piezas[i].getImagen());
            ImageIcon imgpieza = escalar(new ImageIcon(imageURL), 36, 36);

            // Radio button con el nombre simple de la pieza
            JRadioButton rbut = new JRadioButton(nombres[i]);
            rbut.setActionCommand(piezas[i].getNombre());
            rbut.addActionListener(this);
            rbut.setFont(rbut.getFont().deriveFont(13f));
            group.add(rbut);

            JLabel imbut = new JLabel(imgpieza);
            imbut.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));

            JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
            fila.add(rbut);
            fila.add(imbut);
            botonesrad.add(fila);

            if (piezas[i].getNombre().contentEquals(d.getPeca())) {
                rbut.setSelected(true);
            }
        }

        JScrollPane scroll = new JScrollPane(botonesrad,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEtchedBorder());
        this.add(scroll, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        prog.notificar(mensaje + e.getActionCommand());
    }

    private ImageIcon escalar(ImageIcon im, int w, int h) {
        return new ImageIcon(im.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }
}
