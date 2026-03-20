package control;

import model.Dades;

/**
 * Classe que gestiona l'execució del joc mitjançant un fil d'execució. Controla
 * la col·locació de peces, el recorregut Hamiltonià i la comunicació amb la
 * interfície gràfica.
 *
 * @author Equip
 */
public class Joc extends Thread {

    private Dades dat;
    private Notificar prog;
    private long perpintar;
    private volatile boolean aturar = false;

    // ───────────────────────── Paràmetres de mesura de velocitat
    private static final int SPEED_SAMPLE = 300;
    private long tempsExecucio;
    private long tempsIniciAlgoritmeNano;
    private boolean velocitatReportada = false;


    // ───────────────────────── Constructor
    /**
     * Mètode constructor.
     *
     * @param d dades del model del joc
     * @param n sistema de notificació amb la interfície gràfica
     */
    public Joc(Dades d, Notificar n) {
        dat = d;
        prog = n;
    }

    /**
     * Atura l'execució del fil.
     */
    public void aturar() {
        aturar = true;
    }

    // ───────────────────────── Execució principal
    /**
     * Mètode principal del fil.
     *
     * Inicialitza el taulell, calcula les posicions inicials i inicia el procés
     * de cerca del recorregut.
     */
    @Override
    public void run() {
        dat.resetCaselles();
        perpintar = System.currentTimeMillis();
        tempsExecucio = System.currentTimeMillis();
        velocitatReportada = false;

        tempsIniciAlgoritmeNano = System.nanoTime();
        int numPeces = dat.getNumPeces();
        int[] files = new int[numPeces];
        int[] columnes = new int[numPeces];

        // Calcula les posicions inicials de les peces
        int[][] startPos = dat.calcularPosicionsInicials();
        for (int i = 0; i < numPeces; i++) {
            files[i] = startPos[i][0];
            columnes[i] = startPos[i][1];
        }

        // Comprova que les posicions inicials siguen segures
        if (!dat.posicionsSegures(files, columnes)) {
            String conflicto = dat.detectarConflicte(files, columnes);
            prog.notificar("ponalerta:Posicions inicials no vàlides: " + conflicto);
            return;
        }

        // Col·loca les peces inicials al tauler
        for (int i = 0; i < numPeces; i++) {
            dat.posarPeca(files[i], columnes[i], dat.incrementarCaselles());
        }
        prog.notificar("repintar");

        // Inicia el backtracking
        posarPeca(files, columnes, 0);

        prog.notificar("repintar");
        if (aturar) {
            return;
        }

        long ms = (System.nanoTime() - tempsIniciAlgoritmeNano) / 1_000_000;
        prog.notificar("temps:alg-" + ms);

        // Calcula estadístiques finals si no s'han reportat abans
        if (!velocitatReportada) {
            int backtracted = dat.getCaselles() - dat.getNumPeces();
            if (backtracted > 0 && ms > 0) {
                long nps = backtracted * 1000L / ms;
                prog.notificar("estadistica:nps-" + nps + "-nodos-" + backtracted + "-ms-" + ms);
            }
        }

        // Notifica resultat
        // aixo canviat
        int nm = dat.getDimensio() * dat.getDimensio();
        if (dat.getCaselles() < nm) {
            prog.notificar("ponalerta:No hi ha solució per al recorregut Hamiltonà amb aquestes posicions.");
        } else {
            prog.notificar("ponalerta:Solució trobada en " + ms + " ms");
        }
    }

    // ───────────────────────── Backtracking
    /**
     * Realitza la col·locació recursiva de peces mitjançant backtracking.
     *
     * @param files files actuals de les peces
     * @param cols columnes actuals de les peces
     * @param torn torn actual del moviment
     */
    private void posarPeca(int[] files, int[] cols, int torn) {
        if (aturar) {
            return;
        }
        int nm = dat.getDimensio() * dat.getDimensio();
        if (dat.getCaselles() >= nm) {
            return;
        }

        int idxPeca = torn % dat.getNumPeces();
        int filaActual = files[idxPeca];
        int columnaActual = cols[idxPeca];
        int numMovs = dat.getNumMovs(idxPeca);

        // Prova tots els moviments possibles
        for (int i = 0; i < numMovs; i++) {
            if (aturar) {
                return;
            }
            int filaSeguent = filaActual + dat.getMovX(idxPeca, i);
            int columnaSeguent = columnaActual + dat.getMovY(idxPeca, i);

            // Comprova si la casella és vàlida
            if (dat.noTrepitjadaYEnTaulell(filaSeguent, columnaSeguent)) {
                files[idxPeca] = filaSeguent;
                cols[idxPeca] = columnaSeguent;

                if (!dat.posicionsSegures(files, cols)) {
                    files[idxPeca] = filaActual;
                    cols[idxPeca] = columnaActual;
                    continue;
                }
                dat.posarPeca(filaSeguent, columnaSeguent, dat.incrementarCaselles());

                // Repintat periòdic
                long ara = System.currentTimeMillis();
                long umbral = 75L * (100 - dat.getVelocitat());
                if ((ara - perpintar) > umbral) {
                    prog.notificar("repintar");
                    long elapsedMs = (System.nanoTime() - tempsIniciAlgoritmeNano) / 1_000_000;
                    prog.notificar("temps:alg-" + elapsedMs);
                    perpintar = ara;
                }

                // Mesura inicial de velocitat
                if (!velocitatReportada && (dat.getCaselles() - dat.getNumPeces()) >= SPEED_SAMPLE) {
                    long elapsedMs = System.currentTimeMillis() - tempsExecucio;
                    if (elapsedMs > 0) {
                        long nps = SPEED_SAMPLE * 1000L / elapsedMs;
                        prog.notificar("estadistica:nps-" + nps + "-nodos-" + SPEED_SAMPLE + "-ms-" + elapsedMs);
                    }
                    velocitatReportada = true;
                }

                posarPeca(files, cols, torn + 1);

                if (aturar) {
                    return;
                }

                // Backtracking
           
                if (dat.getCaselles() < nm) {
                    dat.llevarPeca(filaSeguent, columnaSeguent);
                    dat.decrementarCaselles();
                    files[idxPeca] = filaActual;
                    cols[idxPeca] = columnaActual;
                } else {
                    return;
                }
            }
        }
    }

    // ───────────────────────── Posicions inicials
    /**
     * Actualitza les dades del model utilitzades pel joc.
     *
     * @param d noves dades del joc
     */
    public void setDades(Dades d) {
        dat = d;
    }
}
