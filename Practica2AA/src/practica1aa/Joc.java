package practica1aa;

import model.Dades;
import control.Notificar;

public class Joc extends Thread {

    private Dades dat;
    private Notificar prog;
    private int casillas;
    private int nm;
    private long perpintar;
    private int numPeces;
    private volatile boolean parar = false;

    // Medición de velocidad —————————————————
    private static final int SPEED_SAMPLE = 300; // muestra tras N colocaciones
    private long tiempoMedicion;
    private boolean velocidadReportada = false;

    public Joc(Dades d, Notificar n) {
        dat = d;
        prog = n;
        nm = dat.getDimension() * dat.getDimension();
        numPeces = dat.getNumPeces();
    }

    public void parar() {
        parar = true;
    }

    @Override
    public void run() {
        casillas = 0;
        perpintar = System.currentTimeMillis();
        tiempoMedicion = System.currentTimeMillis();
        velocidadReportada = false;

        long tiempoInicio = System.nanoTime();
        int dim = dat.getDimension();
        int[] rows = new int[numPeces];
        int[] cols = new int[numPeces];

        int[][] startPos = calcularPosicionesIniciales(numPeces, dim);
        for (int i = 0; i < numPeces; i++) {
            rows[i] = startPos[i][0];
            cols[i] = startPos[i][1];
        }
        for (int i = 0; i < numPeces; i++) {
            dat.ponPeca(rows[i], cols[i], ++casillas);
        }
        prog.notificar("repintar");

        ponerPeca(rows, cols, 0);

        prog.notificar("repintar");
        if (parar) {
            return;
        }

        long ms = (System.nanoTime() - tiempoInicio) / 1_000_000;

        // Si no se reportó velocidad durante el cómputo, calculamos al final
        if (!velocidadReportada) {
            int backtracted = casillas - numPeces;
            if (backtracted > 0 && ms > 0) {
                long nps = backtracted * 1000L / ms;
                prog.notificar("estadistica:nps-" + nps);
            }
        }

        // Notificar resultado
        if (casillas < nm) {
            prog.notificar("ponalerta:Solución no encontrada  (" + ms + " ms)");
        } else {
            prog.notificar("ponalerta:Solució trobada en " + ms + " ms");
        }
    }

    private void ponerPeca(int[] rows, int[] cols, int turno) {
        if (parar) {
            return;
        }
        if (casillas >= nm) {
            return;
        }

        int pieceIdx = turno % numPeces;
        int curRow = rows[pieceIdx];
        int curCol = cols[pieceIdx];
        int numMovs = dat.getNumMovs(pieceIdx);

        for (int i = 0; i < numMovs; i++) {
            if (parar) {
                return;
            }
            int nextRow = curRow + dat.getMovX(pieceIdx, i);
            int nextCol = curCol + dat.getMovY(pieceIdx, i);

            if (dat.noPisadaYEnTablero(nextRow, nextCol)) {
                dat.ponPeca(nextRow, nextCol, ++casillas);
                rows[pieceIdx] = nextRow;
                cols[pieceIdx] = nextCol;

                // Animación periódica
                long ahora = System.currentTimeMillis();
                long umbral = 75L * (100 - dat.getVelocidad());
                if ((ahora - perpintar) > umbral) {
                    prog.notificar("repintar");
                    perpintar = ahora;
                }

                // Muestreo de velocidad (una vez, tras SPEED_SAMPLE colocaciones)
                if (!velocidadReportada && (casillas - numPeces) >= SPEED_SAMPLE) {
                    long elapsedMs = System.currentTimeMillis() - tiempoMedicion;
                    if (elapsedMs > 0) {
                        long nps = SPEED_SAMPLE * 1000L / elapsedMs;
                        prog.notificar("estadistica:nps-" + nps);
                    }
                    velocidadReportada = true;
                }

                ponerPeca(rows, cols, turno + 1);

                if (casillas < nm) {
                    dat.quitaPeca(nextRow, nextCol);
                    casillas--;
                    rows[pieceIdx] = curRow;
                    cols[pieceIdx] = curCol;
                } else {
                    return;
                }
            }
        }
    }

    private int[][] calcularPosicionesIniciales(int numP, int dim) {
        int[][] result = new int[numP][2];
        if (numP == 1) {
            result[0] = new int[]{0, 0};
            return result;
        }
        int[][] preferidas = {
            {0, 0}, {dim - 1, dim - 1}, {0, dim - 1}, {dim - 1, 0},
            {0, dim / 2}, {dim - 1, dim / 2}, {dim / 2, 0}, {dim / 2, dim - 1},};
        for (int i = 0; i < numP; i++) {
            result[i] = preferidas[i % preferidas.length];
        }
        return result;
    }

    public void setDatos(Dades d) {
        dat = d;
    }
}
