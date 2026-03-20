package control;

import javax.swing.Timer;
import model.Dades;
import vista.Gui;
import vista.dialegs.IntroduirPosicions;
import vista.dialegs.Modal;

/**
 * Classe principal de l'aplicació Escacs Algorítmics.
 *
 * Gestiona la interfície gràfica, la comunicació amb el model de dades,
 * l'execució de l'algorisme i la reproducció pas a pas de la solució.
 * 
 * @author Equip
 */
public class Main implements Notificar {

    private Gui gui;
    private Dades dat;
    private Joc joc;
    private Timer reproductor;
    private long darrerTempsAlgoritmeMs;
    private int pasActualReproduccio;

    // ───────────────────────────────── Inicialització
    /**
     * Inicialitza l'aplicació. Crea el model, la interfície i la mostra.
     */
    private void inici() {
        dat = new Dades();
        gui = construirInterficie();
        gui.actualitzarEstimacio(); // estimació inicial al arrancar
        gui.visualitzar();
    }

    /**
     * Construeix la interfície gràfica i configura la barra d'opcions.
     *
     * @return GUI inicialitzada
     */
    private Gui construirInterficie() {
        Gui g = new Gui("Escacs Algorítmics", 950, 680, dat, this);
        g.posarOpcio("bombeta.png", "barra:calcular",
                "Resoldre el problema", "Resoldre");
        g.posarOpcio("aturar.png", "barra:aturar",
                "Aturar l'algorisme en curs", "Aturar");
        g.posarOpcio("netejar.png", "barra:netejar",
                "Netejar el tauler", "Netejar");
        g.posarOpcio("sortir.png", "barra:sortir",
                "Tancar l'aplicació", "Sortir");
        return g;
    }

    /**
     * Punt d'entrada del programa.
     *
     * @param args arguments de línia de comandes
     */
    public static void main(String[] args) {
        (new Main()).inici();
    }

    // ───────────────────────────────── Notificacions GUI / fil 
    /**
     * Rep notificacions des de la GUI o del fil de càlcul.
     *
     * @param s missatge d'esdeveniment
     */
    @Override
    public void notificar(String s) {
        if (s.equals("barra:calcular")) {
            // Reiniciar qualsevol reproducció activa
            resetReproduccio();
            String modeInici = dat.getModeInici();
            int[][] posicionsUsuari = null;
            // Recuperar posicions manuals si escau
            if ("usuari".equals(modeInici)) {
                posicionsUsuari = dat.getPosicionsIniciUsuari();
            }
            // Validar que totes les posicions estiguin definides
            if ("usuari".equals(dat.getModeInici()) && !posicionsUsuariCompletes()) {
                gui.setStatus("Introdueix " + dat.getNumPeces() + " posicions inicials");
                return;
            }
            // Regenerar model
            dat.regenerar(dat.getDimensio());
            dat.setModeInici(modeInici);

            // Restaurar posicions introduïdes
            if ("usuari".equals(modeInici) && posicionsUsuari != null
                    && posicionsUsuari.length == dat.getNumPeces()) {
                for (int i = 0; i < posicionsUsuari.length; i++) {
                    dat.setPosicioIniciUsuari(i, posicionsUsuari[i][0], posicionsUsuari[i][1]);
                }
            }
            // Mostrar posicions inicials si són manuals
            if ("usuari".equals(dat.getModeInici())) {
                visualitzarPosicionsUsuari();
            }
            gui.repintar();
            gui.setCalculant(true);
                darrerTempsAlgoritmeMs = 0L;
                gui.setStatus("Calculant... " + formatMs(darrerTempsAlgoritmeMs)
                    + "  |  " + dat.getNumPeces() + " peces · "
                    + dat.getDimensio() + "×" + dat.getDimensio());

            // Llançar fil de càlcul
            joc = new Joc(dat, this);
            joc.start();

            // ─────────────── Aturar càlcul ───────────────
        } else if (s.equals("barra:aturar")) {
            if (joc != null && joc.isAlive()) {
                joc.aturar();
                gui.setStatus("Aturat - " + formatMs(darrerTempsAlgoritmeMs));
                gui.setCalculant(false);
            }

            // ─────────────── Repintar ───────────────
        } else if (s.equals("repintar")) {
            gui.repintar();

            // ─────────────── Resultat final ───────────────
        } else if (s.startsWith("ponalerta:")) {
            String aux = s.substring(s.indexOf(':') + 1);
            gui.setStatus(aux);
            gui.setCalculant(false);
            boolean teSolucio = aux.startsWith("Solució trobada");

            // Guardar solució
            if (teSolucio) {
                dat.guardarSolucio();
                if (dat.isPasAPasActivat()) {
                    prepararReproduccio();
                } else {
                    gui.setPlaybackDisponible(false);
                }
            }

            // Mostrar diàleg modal
            new Modal(this, aux).setVisible(true);

            // Mostrar primer pas
            if (teSolucio && dat.isPasAPasActivat()) {
                mostrarPasSolucio(pasActualReproduccio);
                gui.setStatus("Solució trobada. Reproducció pas a pas disponible");
            }

            // ─────────────── Canvi velocitat ───────────────
        } else if (s.startsWith("estadistica:nps-")) {
            String resto = s.substring("estadistica:nps-".length());

            try {
                String[] partes = resto.split("-");
                if (partes.length >= 5) {
                    long nps = Long.parseLong(partes[0]);
                    int nodos = Integer.parseInt(partes[2]);
                    long ms = Long.parseLong(partes[4]);

                    // Calcular temps real i constants
                    double tempsReal = ms / 1000.0;
                    double c = ms / (double) nodos;

                    // Temps previst per completar tot el tauler
                    int totalCaselles = dat.getDimensio() * dat.getDimensio() - dat.getNumPeces();
                    double tempsPrevisio = totalCaselles / (double) nps;

                    gui.actualitzarEstadistica(tempsReal, c / 1e9, tempsPrevisio);
                    gui.actualitzarVelocitat(nps);
                }
            } catch (Exception e) {
               MeuErrors.informaError("Error en parsejar l'estadística rebuda: " + s, e);
            }

        } else if (s.startsWith("temps:alg-")) {
            try {
                darrerTempsAlgoritmeMs = Long.parseLong(s.substring("temps:alg-".length()));
                gui.setStatus("Calculant... " + formatMs(darrerTempsAlgoritmeMs)
                        + "  |  " + dat.getNumPeces() + " peces · "
                        + dat.getDimensio() + "×" + dat.getDimensio());
            } catch (NumberFormatException e) {
                MeuErrors.informaError("Error en parsejar el temps d'algorisme: " + s, e);
            }

            // ── Velocitat de animación ────────────────────────────────────────
        } else if (s.startsWith("velocitat:")) {
            dat.setVelocitat(Integer.parseInt(s.substring(s.indexOf(':') + 1)));

           // ── Configuració pas a pas ────────────────────────────────────────
        } else if (s.startsWith("config:pas-a-pas-")) {
            boolean activat = Boolean.parseBoolean(s.substring("config:pas-a-pas-".length()));
            dat.setPasAPasActivat(activat);
            aturarReproduccio();
            if (activat && dat.getSolucio() != null) {
                prepararReproduccio();
                mostrarPasSolucio(pasActualReproduccio);
            } else {
                gui.setPlaybackDisponible(false);
                gui.amagarFletxaMoviment();
                if (dat.getSolucio() != null) {
                    mostrarPasSolucio(dat.getTotalPassosSolucio());
                }
            }

            // ── Mostram moviment fletxes ────────────────────────────────────────
        } else if (s.startsWith("config:mostrar-fletxa-")) {
            boolean mostrar = Boolean.parseBoolean(s.substring("config:mostrar-fletxa-".length()));
            dat.setMostrarFletxaMoviment(mostrar);
            if (!mostrar) {
                gui.amagarFletxaMoviment();
                gui.repintar();
            } else if (dat.getSolucio() != null && dat.isPasAPasActivat()) {
                actualitzarFletxaMoviment(pasActualReproduccio);
                gui.repintar();
            }

            // ── Configuració: canviar peça ───────────────────────
        } else if (s.startsWith("config:pieza-set-")) {
            resetReproduccio();
            String resta = s.substring("config:pieza-set-".length());
            int guio = resta.indexOf('-');
            dat.setPecaEn(Integer.parseInt(resta.substring(0, guio)),
                    resta.substring(guio + 1));
            gui.setImatgePeca(dat);
            gui.repintar();
            gui.actualitzarEstimacio();

            // ── Configuració: afegir peça ───────────────────────
        } else if (s.startsWith("config:pieza-add-")) {
            resetReproduccio();
            dat.afegirPeca(s.substring("config:pieza-add-".length()));
            if ("usuari".equals(dat.getModeInici())) {
                dat.netejarTaulell();
            }
            gui.setDades(dat);
            gui.repintar();

            // ── Configuració: eliminar peça ───────────────────────
        } else if (s.equals("config:pieza-remove")) {
            resetReproduccio();
            dat.llevarDarreraPeca();
            if ("usuari".equals(dat.getModeInici())) {
                dat.netejarTaulell();
            }
            gui.setDades(dat);
            gui.repintar();

           // ── Configuració: dimensió tauler ───────────────────────
        } else if (s.startsWith("config:dim-")) {
            resetReproduccio();
            dat.regenerar(Integer.parseInt(s.substring("config:dim-".length())));
            if ("usuari".equals(dat.getModeInici())) {
                dat.netejarPosicionsIniciUsuari();
                dat.netejarTaulell();
            }
            gui.setDades(dat);
            gui.repintar();

            // ── Configuració: tipus de reproducció ───────────────────────
        } else if (s.startsWith("config:inicio-modo-")) {
            resetReproduccio();
            String mode = s.substring("config:inicio-modo-".length());
            dat.setModeInici(mode);
            dat.netejarPosicionsIniciUsuari();
            dat.netejarTaulell();
            gui.repintar();
            if ("usuari".equals(mode)) {
                introduirPosicionsManuals();
            } else if ("aleatori".equals(mode)) {
                gui.setStatus("Mode inicial: aleatori");
            } else {
                gui.setStatus("Mode inicial: fixes (esquines)");
            }

             // ── Configuració: editar posicions ───────────────────────       
        } else if (s.equals("config:introduir-posicions")) {
            resetReproduccio();
            dat.netejarPosicionsIniciUsuari();
            dat.netejarTaulell();
            introduirPosicionsManuals();

            // ── Tornar enrere ───────────────────────
        } else if (s.equals("repro:anterior")) {
            if (dat.getSolucio() != null) {
                aturarReproduccio();
                pasActualReproduccio = Math.max(dat.getNumPeces(), pasActualReproduccio - 1);
                mostrarPasSolucio(pasActualReproduccio);
            }

            // ── Iniciar solució ───────────────────────
        } else if (s.equals("repro:inici")) {
            if (dat.getSolucio() != null) {
                aturarReproduccio();
                pasActualReproduccio = Math.min(dat.getNumPeces(), dat.getTotalPassosSolucio());
                mostrarPasSolucio(pasActualReproduccio);
            }

            // ── Visualizació solució ───────────────────────
        } else if (s.equals("repro:play")) {
            if (dat.getSolucio() != null && dat.isPasAPasActivat()) {
                if (reproductor != null && reproductor.isRunning()) {
                    aturarReproduccio();
                } else {
                    iniciarReproduccio();
                }
            }

            // ── Pas següent ───────────────────────
        } else if (s.equals("repro:seguent")) {
            if (dat.getSolucio() != null) {
                aturarReproduccio();
                pasActualReproduccio = Math.min(dat.getTotalPassosSolucio(), pasActualReproduccio + 1);
                mostrarPasSolucio(pasActualReproduccio);
            }

            // ── Final ───────────────────────
        } else if (s.equals("repro:final")) {
            if (dat.getSolucio() != null) {
                aturarReproduccio();
                pasActualReproduccio = dat.getTotalPassosSolucio();
                mostrarPasSolucio(pasActualReproduccio);
            }

            // ── Reset: netejar tauler ────────────────────────────────────           
        } else if (s.equals("barra:netejar")) {
            resetReproduccio();
            if (joc != null && joc.isAlive()) {
                joc.aturar();
            }
            dat.netejarTaulell();
            gui.repintar();
            gui.actualitzarEstimacio();
            gui.setStatus("Tauler netejat");
            gui.setCalculant(false);

             // ── Sortir: tancar l'aplicació ────────────────────────────────────           
        } else if (s.equals("barra:sortir")) {
            System.exit(0);
        }
    }

    // ───────────────────────── Posicions manuals   
    /**
     * Permet introduir manualment les posicions inicials mitjançant un diàleg
     * de la vista.
     */
    private void introduirPosicionsManuals() {
        int n = dat.getNumPeces();
        int dim = dat.getDimensio();

        // Bucle per demanar posició per cada peça       
        for (int i = 0; i < n; i++) {
             // Bucle fins que l'usuari introdueix una posició vàlida           
            while (true) {
                int[] posicio = IntroduirPosicions.demanarPosicio(i + 1, n, dim - 1);
                if (posicio == null) {
                    gui.setStatus("Introducció cancel·lada");
                    return;
                }
                if (posicio[0] == Integer.MIN_VALUE) {
                    IntroduirPosicions.mostrarError("Introdueix un número enter vàlid");
                    continue;
                }

                int fila = posicio[0];
                int col = posicio[1];
                // Comprovar rang vàlid           
                if (fila < 0 || fila >= dim || col < 0 || col >= dim) {
                    IntroduirPosicions.mostrarError("Valors fora de rang (0 - " + (dim - 1) + ")");
                    continue;
                }

                int[][] posicions = dat.getPosicionsIniciUsuari();
                boolean error = false;
                for (int j = 0; j < i; j++) {
                    if (posicions[j][0] == fila && posicions[j][1] == col) {
                        IntroduirPosicions.mostrarError("Casella ja usada per la peça " + (j + 1));
                        error = true;
                        break;
                    }
                    // Captura mutua entre peces
                    if (dat.capturaMutua(i, fila, col, j, posicions[j][0], posicions[j][1])) {
                        IntroduirPosicions.mostrarError("Posició no vàlida: la peça " + (i + 1)
                                + " capturaria la peça " + (j + 1));
                        error = true;
                        break;
                    }
                }
                if (error) {
                    continue;
                }
                // Guardar la posició a les dades
                if (!dat.setPosicioIniciUsuari(i, fila, col)) {
                    IntroduirPosicions.mostrarError("No s'ha pogut guardar la posició.");
                    continue;
                }
                break;
            }
        }
        // Visualitzar totes les posicions introduïdes
        visualitzarPosicionsUsuari();
        gui.setStatus("Posicions completes. Ja pots prémer Resoldre");
    }

    // ───────────────────────── Visualització
    /**
     * Mostra al tauler les posicions inicials introduïdes.
     */
    private void visualitzarPosicionsUsuari() {
        dat.aplicarPosicionsIniciUsuariAlTaulell();
        gui.repintar();
    }

    /**
     * Comprova si totes les posicions d'usuari estan definides.
     */
    private boolean posicionsUsuariCompletes() {
        int[][] pos = dat.getPosicionsIniciUsuari();
        if (pos == null || pos.length != dat.getNumPeces()) {
            return false;
        }
        for (int i = 0; i < pos.length; i++) {
            if (pos[i][0] < 0 || pos[i][1] < 0) {
                return false;
            }
        }
        return true;
    }

    // ───────────────────────── Reproducció
    /**
     * Prepara la reproducció pas a pas de la solució.
     */
    private void prepararReproduccio() {
        if (dat.getSolucio() == null) {
            return;
        }
        pasActualReproduccio = Math.min(dat.getNumPeces(), dat.getTotalPassosSolucio());
        gui.setPlaybackDisponible(true);
        gui.setPlaybackEnMarxa(false);
    }

    /**
     * Inicia la reproducció automàtica de la solució.
     */
    private void iniciarReproduccio() {
        if (dat.getSolucio() == null) {
            return;
        }
        // Comprovar si el pas actual és superior al total
        if (pasActualReproduccio >= dat.getTotalPassosSolucio()) {
            pasActualReproduccio = Math.min(dat.getNumPeces(), dat.getTotalPassosSolucio());
            mostrarPasSolucio(pasActualReproduccio);
        }

        aturarReproduccio();
        int delay = 80 + dat.getVelocitat() * 8;
        reproductor = new Timer(delay, e -> {
            if (pasActualReproduccio >= dat.getTotalPassosSolucio()) {
                aturarReproduccio();
                return;
            }
            pasActualReproduccio++;
            mostrarPasSolucio(pasActualReproduccio);
        });
        gui.setPlaybackEnMarxa(true);
        reproductor.start();
    }

    /**
     * Atura la reproducció automàtica.
     */
    private void aturarReproduccio() {
        if (reproductor != null) {
            reproductor.stop();
            reproductor = null;
        }
        gui.setPlaybackEnMarxa(false);
    }

    /**
     * Reseteja la reproducció i elimina la solució guardada.
     */
    private void resetReproduccio() {
        aturarReproduccio();
        dat.esborrarSolucio();
        pasActualReproduccio = 0;
        gui.setPlaybackDisponible(false);
        gui.amagarFletxaMoviment();
    }

    /**
     * Mostra la solució fins al pas indicat.
     */
    private void mostrarPasSolucio(int pas) {
        if (dat.getSolucio() == null) {
            return;
        }
        dat.aplicarPasSolucio(pas);
        actualitzarFletxaMoviment(pas);
        gui.setPlaybackPasInfo(pas, dat.getTotalPassosSolucio());
        gui.repintar();
    }

    /**
     * Actualitza la fletxa de moviment segons el pas actual.
     */
    private void actualitzarFletxaMoviment(int pas) {
        if (!dat.isPasAPasActivat() || !dat.isMostrarFletxaMoviment()) {
            gui.amagarFletxaMoviment();
            return;
        }
        int pasAnteriorMateixaPeca = pas - dat.getNumPeces();
        if (pasAnteriorMateixaPeca < 1) {
            gui.amagarFletxaMoviment();
            return;
        }
        int[] ini = dat.getCoordenadaPasSolucio(pasAnteriorMateixaPeca);
        int[] fi = dat.getCoordenadaPasSolucio(pas);
        if (ini == null || fi == null) {
            gui.amagarFletxaMoviment();
            return;
        }
        int pieceIdx = Math.floorMod(pas - 1, dat.getNumPeces());
        gui.mostrarFletxaMoviment(ini[0], ini[1], fi[0], fi[1], pieceIdx);
    }

    /**
     * Converteix mil·lisegons a text llegible.
     *
     * @param ms temps en mil·lisegons
     * @return cadena formatada
     */
    private static String formatMs(long ms) {
        if (ms < 1000) {
            return ms + " ms";
        }
        return String.format("%.2f s", ms / 1000.0);
    }
}
