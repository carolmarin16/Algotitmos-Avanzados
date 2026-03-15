package practica1aa;

import model.Dades;

import control.Notificar;
import vista.Gui;
import vista.dialegs.Modal;

import javax.swing.Timer;

public class Main implements Notificar {

    private Gui gui;
    private Dades dat;
    private Joc juego;
    private Timer reloj;          // timer Swing — actualiza el tiempo transcurrido
    private long tiempoInicio;   // ms en que se pulsó Resolver

    private void inicio() {
        dat = new Dades();
        gui = construirInterfaz();
        gui.actualizarEstimacion(); // estimación inicial al arrancar
        gui.visualizar();
    }

    private Gui construirInterfaz() {
        Gui g = new Gui("Visitar todas las casillas", 950, 680, dat, this);
        g.ponOpcion("imagenes/cerebro.png", "barra:calcular",
                "Resolver el problema", "Resolver");
        g.ponOpcion("imagenes/cerebro.png", "barra:parar",
                "Detener el algoritmo en curso", "Detener");
        return g;
    }

    public static void main(String[] args) {
        (new Main()).inicio();
    }

    @Override
    public void notificar(String s) {

        // ── Algoritmo ─────────────────────────────────────────────────────
        if (s.equals("barra:calcular")) {
            dat.regenerar(dat.getDimension());
            gui.repintar();
            gui.setCalculando(true);
            tiempoInicio = System.currentTimeMillis();
            iniciarReloj();
            juego = new Joc(dat, this);
            juego.start();

        } else if (s.equals("barra:parar")) {
            if (juego != null && juego.isAlive()) {
                juego.parar();
                detenerReloj();
                long transcurrido = System.currentTimeMillis() - tiempoInicio;
                gui.setStatus("Detenido — " + formatMs(transcurrido));
                gui.setCalculando(false);
            }

        } else if (s.equals("repintar")) {
            gui.repintar();

            // ── Resultado ─────────────────────────────────────────────────────
        } else if (s.startsWith("ponalerta:")) {
            detenerReloj();
            String aux = s.substring(s.indexOf(":") + 1);
            long transcurrido = System.currentTimeMillis() - tiempoInicio;
            String estado = aux + "  [" + formatMs(transcurrido) + "]";
            gui.setStatus(estado);
            gui.setCalculando(false);
            new Modal(this, aux).setVisible(true);

            // ── Estadística: velocidad de cómputo ─────────────────────────────
        } else if (s.startsWith("estadistica:nps-")) {
            long nps = Long.parseLong(s.substring("estadistica:nps-".length()));
            gui.actualizarVelocitat(nps);

            // ── Velocidad de animación ────────────────────────────────────────
        } else if (s.startsWith("velocidad:")) {
            dat.setVelocidad(Integer.parseInt(s.substring(s.indexOf(":") + 1)));

            // ── Configuración: cambiar pieza ──────────────────────────────────
        } else if (s.startsWith("config:pieza-set-")) {
            String resto = s.substring("config:pieza-set-".length());
            int guion = resto.indexOf('-');
            dat.setPecaEn(Integer.parseInt(resto.substring(0, guion)),
                    resto.substring(guion + 1));
            gui.setImagenPieza(dat);
            gui.repintar();
            gui.actualizarEstimacion();

        } else if (s.startsWith("config:pieza-add-")) {
            dat.agregarPeca(s.substring("config:pieza-add-".length()));
            gui.setDatos(dat);
            gui.repintar();

        } else if (s.equals("config:pieza-remove")) {
            dat.quitarUltimaPeca();
            gui.setDatos(dat);
            gui.repintar();

        } else if (s.startsWith("config:dim-")) {
            dat.regenerar(Integer.parseInt(s.substring("config:dim-".length())));
            gui.setDatos(dat);
            gui.repintar();
        }
    }

    // ── Reloj Swing (EDT) ─────────────────────────────────────────────────
    private void iniciarReloj() {
        if (reloj != null) {
            reloj.stop();
        }
        reloj = new Timer(250, e -> {
            long ms = System.currentTimeMillis() - tiempoInicio;
            gui.setStatus("Calculant... " + formatMs(ms)
                    + "  |  " + dat.getNumPeces() + " peces · "
                    + dat.getDimension() + "×" + dat.getDimension());
        });
        reloj.start();
    }

    private void detenerReloj() {
        if (reloj != null) {
            reloj.stop();
            reloj = null;
        }
    }

    private static String formatMs(long ms) {
        if (ms < 1000) {
            return ms + " ms";
        }
        return String.format("%.2f s", ms / 1000.0);
    }
}
