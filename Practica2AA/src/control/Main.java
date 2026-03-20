package control;

import javax.swing.Timer;
import model.Dades;
import vista.Gui;

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
        return new Gui("Escacs Algorítmics", 950, 680, dat, this);
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
        if (s == null || s.isEmpty()) {
            return;
        }

        if (gestionarComandesExactes(s)) {
            return;
        }

        gestionarComandesAmbPrefix(s);
    }

    private boolean gestionarComandesExactes(String s) {
        switch (s) {
            case "barra:calcular":
                onBarraCalcular();
                return true;
            case "barra:aturar":
                onBarraAturar();
                return true;
            case "repintar":
                gui.repintar();
                return true;
            case "config:pieza-remove":
                onConfigPiezaRemove();
                return true;
            case "config:introduir-posicions":
                onConfigIntroduirPosicions();
                return true;
            case "repro:anterior":
                onReproAnterior();
                return true;
            case "repro:inici":
                onReproInici();
                return true;
            case "repro:play":
                onReproPlay();
                return true;
            case "repro:seguent":
                onReproSeguent();
                return true;
            case "repro:final":
                onReproFinal();
                return true;
            case "barra:netejar":
                onBarraNetejar();
                return true;
            case "barra:sortir":
                System.exit(0);
                return true;
            default:
                return false;
        }
    }

    private void gestionarComandesAmbPrefix(String s) {
        if (s.startsWith("ponalerta:")) {
            onPonalerta(s);
        } else if (s.startsWith("estadistica:nps-")) {
            onEstadisticaNps(s);
        } else if (s.startsWith("temps:alg-")) {
            onTempsAlg(s);
        } else if (s.startsWith("velocitat:")) {
            onVelocitat(s);
        } else if (s.startsWith("config:pas-a-pas-")) {
            onConfigPasAPas(s);
        } else if (s.startsWith("config:mostrar-fletxa-")) {
            onConfigMostrarFletxa(s);
        } else if (s.startsWith("config:pieza-set-")) {
            onConfigPiezaSet(s);
        } else if (s.startsWith("config:pieza-add-")) {
            onConfigPiezaAdd(s);
        } else if (s.startsWith("config:dim-")) {
            onConfigDim(s);
        } else if (s.startsWith("config:inicio-modo-")) {
            onConfigInicioModo(s);
        }
    }

    private void onBarraCalcular() {
        resetReproduccio();
        String modeInici = dat.getModeInici();
        int[][] posicionsUsuari = null;
        if ("usuari".equals(modeInici)) {
            posicionsUsuari = dat.getPosicionsIniciUsuari();
        }
        if ("usuari".equals(dat.getModeInici()) && !dat.posicionsIniciUsuariCompletes()) {
            gui.setStatus("Introdueix " + dat.getNumPeces() + " posicions inicials");
            return;
        }
        dat.regenerar(dat.getDimensio());
        dat.setModeInici(modeInici);

        if ("usuari".equals(modeInici) && posicionsUsuari != null
                && posicionsUsuari.length == dat.getNumPeces()) {
            for (int i = 0; i < posicionsUsuari.length; i++) {
                dat.setPosicioIniciUsuari(i, posicionsUsuari[i][0], posicionsUsuari[i][1]);
            }
        }
        if ("usuari".equals(dat.getModeInici())) {
            visualitzarPosicionsUsuari();
        }
        gui.repintar();
        gui.setCalculant(true);
        darrerTempsAlgoritmeMs = 0L;
        gui.setStatus("Calculant... " + formatMs(darrerTempsAlgoritmeMs)
                + "  |  " + dat.getNumPeces() + " peces · "
                + dat.getDimensio() + "×" + dat.getDimensio());

        joc = new Joc(dat, this);
        joc.start();
    }

    private void onBarraAturar() {
        if (joc != null && joc.isAlive()) {
            joc.aturar();
            gui.setStatus("Aturat - " + formatMs(darrerTempsAlgoritmeMs));
            gui.setCalculant(false);
        }
    }

    private void onPonalerta(String s) {
        String aux = s.substring(s.indexOf(':') + 1);
        gui.setStatus(aux);
        gui.setCalculant(false);
        boolean teSolucio = aux.startsWith("Solució trobada");

        if (teSolucio) {
            dat.guardarSolucio();
            if (dat.isPasAPasActivat()) {
                prepararReproduccio();
            } else {
                gui.setPlaybackDisponible(false);
            }
        }

        gui.mostrarModal(aux);

        if (teSolucio && dat.isPasAPasActivat()) {
            mostrarPasSolucio(pasActualReproduccio);
            gui.setStatus("Solució trobada. Reproducció pas a pas disponible");
        }
    }

    private void onEstadisticaNps(String s) {
        String resto = s.substring("estadistica:nps-".length());

        try {
            String[] partes = resto.split("-");
            if (partes.length >= 5) {
                long nps = Long.parseLong(partes[0]);
                int nodos = Integer.parseInt(partes[2]);
                long ms = Long.parseLong(partes[4]);

                double tempsReal = ms / 1000.0;
                double c = ms / (double) nodos;

                int totalCaselles = dat.getDimensio() * dat.getDimensio() - dat.getNumPeces();
                double tempsPrevisio = (nps > 0) ? (totalCaselles / (double) nps) : Double.POSITIVE_INFINITY;

                gui.actualitzarEstadistica(tempsReal, c / 1000.0, tempsPrevisio);
                gui.actualitzarVelocitat(nps);
            }
        } catch (Exception e) {
            MeuErrors.informaError("Error en parsejar l'estadística rebuda: " + s, e);
        }
    }

    private void onTempsAlg(String s) {
        try {
            darrerTempsAlgoritmeMs = Long.parseLong(s.substring("temps:alg-".length()));
            gui.setStatus("Calculant... " + formatMs(darrerTempsAlgoritmeMs)
                    + "  |  " + dat.getNumPeces() + " peces · "
                    + dat.getDimensio() + "×" + dat.getDimensio());
        } catch (NumberFormatException e) {
            MeuErrors.informaError("Error en parsejar el temps d'algorisme: " + s, e);
        }
    }

    private void onVelocitat(String s) {
        dat.setVelocitat(Integer.parseInt(s.substring(s.indexOf(':') + 1)));
    }

    private void onConfigPasAPas(String s) {
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
    }

    private void onConfigMostrarFletxa(String s) {
        boolean mostrar = Boolean.parseBoolean(s.substring("config:mostrar-fletxa-".length()));
        dat.setMostrarFletxaMoviment(mostrar);
        if (!mostrar) {
            gui.amagarFletxaMoviment();
            gui.repintar();
        } else if (dat.getSolucio() != null && dat.isPasAPasActivat()) {
            actualitzarFletxaMoviment(pasActualReproduccio);
            gui.repintar();
        }
    }

    private void onConfigPiezaSet(String s) {
        resetReproduccio();
        String resta = s.substring("config:pieza-set-".length());
        int guio = resta.indexOf('-');
        dat.setPecaEn(Integer.parseInt(resta.substring(0, guio)),
                resta.substring(guio + 1));
        gui.setImatgePeca(dat);
        gui.repintar();
        gui.actualitzarEstimacio();
    }

    private void onConfigPiezaAdd(String s) {
        resetReproduccio();
        dat.afegirPeca(s.substring("config:pieza-add-".length()));
        if ("usuari".equals(dat.getModeInici())) {
            dat.netejarTaulell();
        }
        gui.setDades(dat);
        gui.repintar();
    }

    private void onConfigPiezaRemove() {
        resetReproduccio();
        dat.llevarDarreraPeca();
        if ("usuari".equals(dat.getModeInici())) {
            dat.netejarTaulell();
        }
        gui.setDades(dat);
        gui.repintar();
    }

    private void onConfigDim(String s) {
        resetReproduccio();
        dat.regenerar(Integer.parseInt(s.substring("config:dim-".length())));
        if ("usuari".equals(dat.getModeInici())) {
            dat.netejarPosicionsIniciUsuari();
            dat.netejarTaulell();
        }
        gui.setDades(dat);
        gui.repintar();
    }

    private void onConfigInicioModo(String s) {
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
    }

    private void onConfigIntroduirPosicions() {
        resetReproduccio();
        dat.netejarPosicionsIniciUsuari();
        dat.netejarTaulell();
        introduirPosicionsManuals();
    }

    private void onReproAnterior() {
        if (dat.getSolucio() != null) {
            aturarReproduccio();
            pasActualReproduccio = Math.max(dat.getNumPeces(), pasActualReproduccio - 1);
            mostrarPasSolucio(pasActualReproduccio);
        }
    }

    private void onReproInici() {
        if (dat.getSolucio() != null) {
            aturarReproduccio();
            pasActualReproduccio = Math.min(dat.getNumPeces(), dat.getTotalPassosSolucio());
            mostrarPasSolucio(pasActualReproduccio);
        }
    }

    private void onReproPlay() {
        if (dat.getSolucio() != null && dat.isPasAPasActivat()) {
            if (reproductor != null && reproductor.isRunning()) {
                aturarReproduccio();
            } else {
                iniciarReproduccio();
            }
        }
    }

    private void onReproSeguent() {
        if (dat.getSolucio() != null) {
            aturarReproduccio();
            pasActualReproduccio = Math.min(dat.getTotalPassosSolucio(), pasActualReproduccio + 1);
            mostrarPasSolucio(pasActualReproduccio);
        }
    }

    private void onReproFinal() {
        if (dat.getSolucio() != null) {
            aturarReproduccio();
            pasActualReproduccio = dat.getTotalPassosSolucio();
            mostrarPasSolucio(pasActualReproduccio);
        }
    }

    private void onBarraNetejar() {
        resetReproduccio();
        if (joc != null && joc.isAlive()) {
            joc.aturar();
        }
        dat.netejarTaulell();
        gui.repintar();
        gui.actualitzarEstimacio();
        gui.setStatus("Tauler netejat");
        gui.setCalculant(false);
    }

    // ───────────────────────── Posicions manuals   
    /**
     * Permet introduir manualment les posicions inicials mitjançant un diàleg
     * de la vista.
     */
    private void introduirPosicionsManuals() {
        int n = dat.getNumPeces();
        int dimMax = dat.getDimensio() - 1;

        // Bucle per demanar posició per cada peça       
        for (int i = 0; i < n; i++) {
            // Bucle fins que l'usuari introdueix una posició vàlida           
            while (true) {
                int[] posicio = gui.demanarPosicioInicial(i + 1, n, dimMax);
                if (posicio == null) {
                    gui.setStatus("Introducció cancel·lada");
                    return;
                }
                if (posicio[0] == Integer.MIN_VALUE) {
                    gui.mostrarErrorPosicio("Introdueix un número enter vàlid");
                    continue;
                }

                int fila = posicio[0];
                int col = posicio[1];
                String errorValidacio = dat.validarPosicioIniciUsuari(i, fila, col);
                if (errorValidacio != null) {
                    gui.mostrarErrorPosicio(errorValidacio);
                    continue;
                }
                // Guardar la posició a les dades
                if (!dat.setPosicioIniciUsuari(i, fila, col)) {
                    gui.mostrarErrorPosicio("No s'ha pogut guardar la posició.");
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
