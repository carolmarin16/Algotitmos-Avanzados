package model;

import control.MeuErrors;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import model.peces.Peca;

/**
 * Clase que gestiona les dades del joc dels escacs algorítmic.
 *
 * Manté la informació del taulell, les peces actives, les posicions inicials,
 * la configuració d'execució i els paràmetres de visualització.
 *
 * @author Equip
 */
public class Dades {

    private int dimensio;
    private List<Peca> peces = new ArrayList<>();
    private List<String> classesPeces = new ArrayList<>();
    private int[][] taulell;
    private int velocitat;
    private String modeInici = "fixes"; // fixes | aleatori | usuari
    private int[][] posicionsIniciUsuari;
    private boolean pasAPasActivat;
    private boolean mostrarFletxaMoviment = true;
    private final Random random = new Random();
    private int caselles;
    private int[][] solucio;

    // ──────────────────────────────────────── Constructores
    /**
     * Mètode constructor.
     *
     * Crea un objecte Dades amb configuracions per defecte: - velocitat inicial
     * de 50 - dimensió taulell 8x8 peces inicials predefinides (cavall i reina)
     */
    public Dades() {
        velocitat = 50;   // Normal (sincronizado con el slider inicial)
        List<String> def = new ArrayList<>();
        def.add("model.peces.Cavall");
        def.add("model.peces.Reina");
        inicitalitzar(8, def);
    }

    /**
     * Crea un objecte Dades amb una dimensió i llista de peces.
     *
     * @param n dimensió del taulell (nxn)
     * @param classes
     */
    public Dades(int n, List<String> classes) {
        velocitat = 50;
        inicitalitzar(n, classes);
    }

    /**
     * Inicialitza el model amb una dimensió i conjunt de peces.
     *
     * @param d
     * @param classes
     */
    private void inicitalitzar(int d, List<String> classes) {
        dimensio = d;
        peces.clear();
        classesPeces.clear();
        for (String c : classes) {
            instanciarAfegir(c);
        }
        sincronitzarPosicionsIniciUsuari();
        taulell = new int[dimensio][dimensio];
    }

    /**
     * Mètode inicialitzarPosicionsIniciUsuari.
     */
    private void inicialitzarPosicionsIniciUsuari() {
        posicionsIniciUsuari = new int[peces.size()][2];
        for (int i = 0; i < posicionsIniciUsuari.length; i++) {
            posicionsIniciUsuari[i][0] = -1;
            posicionsIniciUsuari[i][1] = -1;
        }
    }

    /**
     * Instancia una peça a partir del nombre de la seva classe i l'afegeix a
     * les peces actives.
     *
     * @param className
     */
    private void instanciarAfegir(String className) {
        try {
            Class<?> c = Class.forName(className);
            Peca p = (Peca) c.getDeclaredConstructor().newInstance();
            if (p.afectaDimensio()) {
                p = (Peca) c.getConstructor(int.class).newInstance(dimensio);
            }
            peces.add(p);
            classesPeces.add(className);
        } catch (Exception e) {
            MeuErrors.informaError("No s'ha pogut instanciar la peça " + className + ".", e);
        }
    }

    // --------------------- GESTIÓ DE PECES
    /**
     * Mètode per colocar peces.
     *
     * @param clases
     */
    public void setPeces(List<String> clases) {
        peces.clear();
        classesPeces.clear();
        for (String c : clases) {
            instanciarAfegir(c);
        }
        sincronitzarPosicionsIniciUsuari();
    }

    /**
     * Substitueix una peça existent per una altra.
     *
     * @param index
     * @param className Nombre de la classe de la nova peça.
     */
    public void setPecaEn(int index, String className) {
        if (index < 0 || index >= peces.size()) {
            return;
        }
        try {
            Class<?> c = Class.forName(className);
            Peca p = (Peca) c.getDeclaredConstructor().newInstance();
            if (p.afectaDimensio()) {
                p = (Peca) c.getConstructor(int.class).newInstance(dimensio);
            }
            peces.set(index, p);
            classesPeces.set(index, className);
        } catch (Exception e) {
            MeuErrors.informaError("No s'ha pogut substituir la peça per " + className + ".", e);
        }
    }

    /**
     * Afegeix una nova peça al conjunt actual.
     *
     * @param className
     */
    public void afegirPeca(String className) {
        instanciarAfegir(className);
        sincronitzarPosicionsIniciUsuari();
    }

    /**
     * Elimina la darrera peça afegida.
     */
    public void llevarDarreraPeca() {
        if (peces.size() > 1) {
            peces.remove(peces.size() - 1);
            classesPeces.remove(classesPeces.size() - 1);
            sincronitzarPosicionsIniciUsuari();
        }
    }

    /**
     * Mètode getNumPeces.
     *
     * @return
     */
    public int getNumPeces() {
        return peces.size();
    }

    /**
     * Mètode getClassesPeces.
     *
     * @return
     */
    public List<String> getClassesPeces() {
        return new ArrayList<>(classesPeces);
    }

    // -------- GESTIÓ TAULELL
    /**
     * Regenera el taulell amb una nova dimensió, reinstanciant les peces
     * actuals.
     *
     * @param d
     */
    public void regenerar(int d) {
        dimensio = d;
        List<String> antigues = new ArrayList<>(classesPeces);
        peces.clear();
        classesPeces.clear();
        for (String c : antigues) {
            instanciarAfegir(c);
        }
        sincronitzarPosicionsIniciUsuari();
        taulell = new int[dimensio][dimensio];
    }

    /**
     * Mètode getModeInici.
     *
     * @return
     */
    public String getModeInici() {
        return modeInici;
    }

    /**
     * Mètode que inidica el mode a jugar.
     *
     * @param mode
     */
    public void setModeInici(String mode) {
        if ("fixes".equals(mode) || "aleatori".equals(mode) || "usuari".equals(mode)) {
            modeInici = mode;
        }
    }

    /**
     * Mètode isPasAPasActivat.
     *
     * @return
     */
    public boolean isPasAPasActivat() {
        return pasAPasActivat;
    }

    /**
     * Mètode setPasAPasActivat.
     *
     * @param activat
     */
    public void setPasAPasActivat(boolean activat) {
        pasAPasActivat = activat;
    }

    /**
     * Mètode isMostrarFletxaMoviment.
     *
     * @return
     */
    public boolean isMostrarFletxaMoviment() {
        return mostrarFletxaMoviment;
    }

    /**
     * Mètode setMostrarFletxaMoviment.
     *
     * @param mostrar
     */
    public void setMostrarFletxaMoviment(boolean mostrar) {
        mostrarFletxaMoviment = mostrar;
    }

    /**
     * Mètode netejarPosicionsIniciUsuari.
     */
    public void netejarPosicionsIniciUsuari() {
        inicialitzarPosicionsIniciUsuari();
    }

    /**
     * Mètode netejarPosicionsIniciUsuari.
     *
     * @param idx
     * @return
     */
    public int[] getPosicioIniciUsuari(int idx) {
        if (idx < 0 || idx >= posicionsIniciUsuari.length) {
            return new int[]{0, 0};
        }
        return new int[]{posicionsIniciUsuari[idx][0], posicionsIniciUsuari[idx][1]};
    }

    /**
     * Mètode setPosicioIniciUsuari.
     *
     * @param idx
     * @param fila
     * @param col
     * @return
     */
    public boolean setPosicioIniciUsuari(int idx, int fila, int col) {
        if (idx < 0 || idx >= posicionsIniciUsuari.length) {
            return false;
        }
        if (fila < 0 || fila >= dimensio || col < 0 || col >= dimensio) {
            return false;
        }
        for (int i = 0; i < posicionsIniciUsuari.length; i++) {
            if (i == idx) {
                continue;
            }
            if (posicionsIniciUsuari[i][0] == fila && posicionsIniciUsuari[i][1] == col) {
                return false;
            }
        }
        posicionsIniciUsuari[idx][0] = fila;
        posicionsIniciUsuari[idx][1] = col;
        return true;
    }

    /**
     * Valida una posició inicial manual per a una peça concreta.
     *
     * @param idx índex de la peça a validar
     * @param fila fila proposada
     * @param col columna proposada
     * @return null si és vàlida; en cas contrari, missatge d'error
     */
    public String validarPosicioIniciUsuari(int idx, int fila, int col) {
        if (idx < 0 || idx >= posicionsIniciUsuari.length) {
            return "Índex de peça no vàlid.";
        }
        if (fila < 0 || fila >= dimensio || col < 0 || col >= dimensio) {
            return "Valors fora de rang (0 - " + (dimensio - 1) + ")";
        }

        for (int j = 0; j < posicionsIniciUsuari.length; j++) {
            if (j == idx) {
                continue;
            }

            int fAlt = posicionsIniciUsuari[j][0];
            int cAlt = posicionsIniciUsuari[j][1];
            if (fAlt < 0 || cAlt < 0) {
                continue;
            }

            if (fAlt == fila && cAlt == col) {
                return "Casella ja usada per la peça " + (j + 1);
            }

            if (capturaMutua(idx, fila, col, j, fAlt, cAlt)) {
                return "Posició no vàlida: la peça " + (idx + 1)
                        + " capturaria la peça " + (j + 1);
            }
        }
        return null;
    }

    /**
     * Mètode getPosicionsIniciUsuari.
     *
     * @return
     */
    public int[][] getPosicionsIniciUsuari() {
        int[][] copia = new int[posicionsIniciUsuari.length][2];
        for (int i = 0; i < posicionsIniciUsuari.length; i++) {
            copia[i][0] = posicionsIniciUsuari[i][0];
            copia[i][1] = posicionsIniciUsuari[i][1];
        }
        return copia;
    }

    /**
     * Indica si totes les posicions inicials manuals estan definides.
     *
     * @return true si totes les peces tenen fila i columna vàlides
     */
    public boolean posicionsIniciUsuariCompletes() {
        if (posicionsIniciUsuari == null || posicionsIniciUsuari.length != peces.size()) {
            return false;
        }
        for (int[] pos : posicionsIniciUsuari) {
            if (pos[0] < 0 || pos[1] < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Mètode que sincronitza on comencen pes peces al taulell.
     */
    private void sincronitzarPosicionsIniciUsuari() {
        int num = peces.size();
        int[][] nou = new int[num][2];
        boolean[][] usades = new boolean[dimensio][dimensio];

        for (int i = 0; i < num; i++) {
            int fila = -1;
            int col = -1;

            if (posicionsIniciUsuari != null && i < posicionsIniciUsuari.length) {
                int rf = posicionsIniciUsuari[i][0];
                int cf = posicionsIniciUsuari[i][1];
                if (rf >= 0 && rf < dimensio && cf >= 0 && cf < dimensio && !usades[rf][cf]) {
                    fila = rf;
                    col = cf;
                }
            }

            if (fila == -1) {
                int[] def = posicioDefecte(i);
                fila = def[0];
                col = def[1];
                if (usades[fila][col]) {
                    boolean trobada = false;
                    for (int r = 0; r < dimensio && !trobada; r++) {
                        for (int c = 0; c < dimensio && !trobada; c++) {
                            if (!usades[r][c]) {
                                fila = r;
                                col = c;
                                trobada = true;
                            }
                        }
                    }
                }
            }

            usades[fila][col] = true;
            nou[i][0] = fila;
            nou[i][1] = col;
        }

        posicionsIniciUsuari = nou;
    }

    /**
     * Mètode per saber la posició per defecte de la peça.
     *
     * @param index
     * @return
     */
    private int[] posicioDefecte(int index) {
        int[][] preferides = {
            {0, 0}, {dimensio - 1, dimensio - 1}, {0, dimensio - 1}, {dimensio - 1, 0},
            {0, dimensio / 2}, {dimensio - 1, dimensio / 2}, {dimensio / 2, 0}, {dimensio / 2, dimensio - 1},};
        if (index < preferides.length) {
            return preferides[index];
        }
        int p = index - preferides.length;
        return new int[]{(p / dimensio) % dimensio, p % dimensio};
    }

    /**
     * Mètode getDimensio.
     *
     * @return
     */
    public int getDimensio() {
        return dimensio;
    }

    /**
     * Comprova si una posició està dins del taulell.
     *
     * @param fila
     * @param columna
     * @return
     */
    public boolean noTrepitjadaYEnTaulell(int fila, int columna) {
        if (fila < 0 || fila >= dimensio || columna < 0 || columna >= dimensio) {
            return false;
        }
        return taulell[fila][columna] == 0;
    }

    /**
     * Indica si existeix una peça en una posició del taulell.
     *
     * @param fila
     * @param columna
     * @return
     */
    public boolean hiHaPeca(int fila, int columna) {
        return taulell[fila][columna] != 0;
    }

    /**
     *
     * @param fila
     * @param columna
     * @return
     */
    public int quinaPeca(int fila, int columna) {
        return taulell[fila][columna];
    }

    /**
     * Coloca una peçsa al taulell.
     *
     * @param fila
     * @param columna
     * @param num
     */
    public void posarPeca(int fila, int columna, int num) {
        taulell[fila][columna] = num;
    }

    /**
     * Elimina una peça d'una posició del taulell.
     *
     * @param fila
     * @param columna
     */
    public void llevarPeca(int fila, int columna) {
        taulell[fila][columna] = 0;
    }

    /**
     * Neteja el taulell, eliminant totes les peces.
     */
    public void netejarTaulell() {
        caselles = 0;
        for (int i = 0; i < dimensio; i++) {
            for (int j = 0; j < dimensio; j++) {
                taulell[i][j] = 0;
            }
        }
    }

    /**
     *
     * @param i
     * @param j
     * @return
     */
    public int getVal(int i, int j) {
        return taulell[i][j];
    }

    /**
     * Retorna el pas actual del recorregut.
     */
    public int getCaselles() {
        return caselles;
    }

    /**
     * Posa a zero el comptador de passos.
     */
    public void resetCaselles() {
        caselles = 0;
    }

    /**
     * Incrementa el comptador i retorna el nou valor (pre-increment).
     */
    public int incrementarCaselles() {
        return ++caselles;
    }

    /**
     * Decrementa el comptador de passos (backtracking).
     */
    public void decrementarCaselles() {
        caselles--;
    }

    /**
     * Copia i desa l'estat actual del taulell com a solució trobada.
     */
    public void guardarSolucio() {
        solucio = copiarTaulell();
    }

    /**
     * Retorna la solució desada (còpia defensiva).
     */
    public int[][] getSolucio() {
        if (solucio == null) {
            return null;
        }
        return copiarMatriu(solucio);
    }

    /**
     * Esborra la solució desada.
     */
    public void esborrarSolucio() {
        solucio = null;
    }

    /**
     * Retorna el nombre total de passos de la solució desada.
     */
    public int getTotalPassosSolucio() {
        if (solucio == null) {
            return 0;
        }
        int total = 0;
        for (int[] fila : solucio) {
            for (int v : fila) {
                total = Math.max(total, v);
            }
        }
        return total;
    }

    /**
     * Fa una còpia de l'estat actual del taulell.
     */
    public int[][] copiarTaulell() {
        return copiarMatriu(taulell);
    }

    /**
     * Aplica al taulell les posicions inicials introduïdes per l'usuari.
     */
    public void aplicarPosicionsIniciUsuariAlTaulell() {
        netejarTaulell();
        for (int i = 0; i < posicionsIniciUsuari.length; i++) {
            int fila = posicionsIniciUsuari[i][0];
            int col = posicionsIniciUsuari[i][1];
            if (fila >= 0 && col >= 0) {
                posarPeca(fila, col, i + 1);
            }
        }
    }

    /**
     * Aplica al taulell la solució fins al pas indicat.
     *
     * @param pas pas màxim a mostrar
     */
    public void aplicarPasSolucio(int pas) {
        if (solucio == null) {
            return;
        }
        netejarTaulell();
        for (int i = 0; i < solucio.length; i++) {
            for (int j = 0; j < solucio[i].length; j++) {
                int valor = solucio[i][j];
                if (valor > 0 && valor <= pas) {
                    posarPeca(i, j, valor);
                }
            }
        }
    }

    /**
     * Retorna la coordenada d'un pas dins la solució desada.
     *
     * @param pas pas a cercar
     * @return coordenada [fila, columna] o null si no existeix
     */
    public int[] getCoordenadaPasSolucio(int pas) {
        if (solucio == null) {
            return null;
        }
        for (int i = 0; i < solucio.length; i++) {
            for (int j = 0; j < solucio[i].length; j++) {
                if (solucio[i][j] == pas) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }

    private int[][] copiarMatriu(int[][] m) {
        int[][] copia = new int[m.length][];
        for (int i = 0; i < m.length; i++) {
            copia[i] = m[i].clone();
        }
        return copia;
    }

    // --------------- MOVIMENTS
    /**
     * Retorna el nombre de moviments possibles d'una peça.
     *
     * @param pieceIdx
     * @return
     */
    public int getNumMovs(int pieceIdx) {
        return peces.get(pieceIdx).getNumMovs();
    }

    /**
     * Obté el desplaçament horitzontal d'un moviment.
     *
     * @param pieceIdx
     * @param moveIdx
     * @return
     */
    public int getMovX(int pieceIdx, int moveIdx) {
        return peces.get(pieceIdx).getMovX(moveIdx);
    }

    /**
     * Obté el desplaçament vertical d'un moviment.
     *
     * @param pieceIdx
     * @param moveIdx
     * @return
     */
    public int getMovY(int pieceIdx, int moveIdx) {
        return peces.get(pieceIdx).getMovY(moveIdx);
    }

    // ------------ IMATGES
    /**
     * Obté la ruta de la imatge associada a una peça.
     *
     * @param pieceIdx
     * @return
     */
    public String getImatge(int pieceIdx) {
        return peces.get(pieceIdx).getImagen();
    }

    // ------ VELOCITAT
    /**
     * Retorna la velocitat d'execució de l'algoritme.
     *
     * @return
     */
    public int getVelocitat() {
        return velocitat;
    }

    /**
     * Estableix la velocitat d'execució.
     *
     * @param v
     */
    public void setVelocitat(int v) {
        velocitat = v;
    }

    /**
     * Retorna el nombre de la classe de la peça.
     *
     * @return
     */
    public String getPecesel() {
        return classesPeces.isEmpty() ? "" : classesPeces.get(0);
    }

    /**
     * Retorna el nombre de la peça.
     *
     * @return
     */
    public String getPeca() {
        return peces.isEmpty() ? "" : peces.get(0).getNombre();
    }

    /**
     * Primera instancia de Peca per descobrir el paquet.
     *
     * @return
     */
    public Peca getClassePeca() {
        return peces.isEmpty() ? null : peces.get(0);
    }

    /**
     *
     */
    public void imprimir() {
        for (int i = 0; i < dimensio; i++) {
            for (int j = 0; j < dimensio; j++) {
                System.out.println(taulell[i][j]);
            }
        }
    }

    // -------- PECES DISPONIBLES
    /**
     * Obté els noms de les classes de peces disponibles.
     *
     * @return
     */
    public String[] getNomsPecesDisponibles() {
        String paquet = "model.peces";
        URL path = getClass().getResource("/" + paquet.replaceAll("\\.", "/"));
        if (path == null) {
            return new String[0];
        }

        File dir;
        try {
            dir = new File(path.toURI());
        } catch (Exception e) {
            MeuErrors.informaError("No s'ha pogut resoldre la ruta de les peces disponibles amb URI. Es farà servir el camí directe.", e);
            dir = new File(path.getPath());
        }

        String[] arxius = dir.list();
        if (arxius == null) {
            return new String[0];
        }

        List<String> noms = new ArrayList<>();
        for (String f : arxius) {
            if (f.endsWith(".class") && !f.equals("Peca.class")) {
                noms.add(f.replace(".class", ""));
            }
        }
        return noms.toArray(new String[0]);
    }

    /**
     * Instancia totes les peces disponibles del sistema.
     *
     * @return
     */
    public Peca[] getPecesDisponibles() {
        String paquet = "model.peces";
        String[] noms = getNomsPecesDisponibles();
        List<Peca> llista = new ArrayList<>();
        for (String nom : noms) {
            try {
                Class<?> c = Class.forName(paquet + "." + nom);
                Peca p = (Peca) c.getDeclaredConstructor().newInstance();
                llista.add(p);
            } catch (Exception e) {
                MeuErrors.informaError("No s'ha pogut carregar la peça disponible " + nom + ".", e);
            }
        }
        return llista.toArray(new Peca[0]);
    }

    // -------- CÀLCUL DE POSICIONS INICIALS I VALIDACIÓ
    /**
     * Calcula les posicions inicials de les peces segons el mode configurat.
     *
     * @return matriu amb les posicions inicials de cada peça
     */
    public int[][] calcularPosicionsInicials() {
        int numP = peces.size();
        int dim = dimensio;
        if ("aleatori".equals(modeInici)) {
            return calcularPosicionsInicialsAleatories(numP, dim);
        }
        if ("usuari".equals(modeInici)) {
            return calcularPosicionsInicialsUsuari(numP, dim);
        }
        return calcularPosicionsInicialsFixes(numP, dim);
    }

    private int[][] calcularPosicionsInicialsFixes(int numP, int dim) {
        int[][] res = new int[numP][2];
        int[][] preferides = {
            {0, 0}, {dim - 1, dim - 1}, {0, dim - 1}, {dim - 1, 0},
            {0, dim / 2}, {dim - 1, dim / 2}, {dim / 2, 0}, {dim / 2, dim - 1},};

        boolean[] usades = new boolean[dim * dim];
        int idx = 0;

        for (int i = 0; i < preferides.length && idx < numP; i++) {
            int f = preferides[i][0];
            int c = preferides[i][1];
            if (!dinsTaulell(f, c, dim)) {
                continue;
            }
            int id = f * dim + c;
            if (usades[id]) {
                continue;
            }
            usades[id] = true;
            res[idx][0] = f;
            res[idx][1] = c;
            idx++;
        }

        for (int f = 0; f < dim && idx < numP; f++) {
            for (int c = 0; c < dim && idx < numP; c++) {
                int id = f * dim + c;
                if (usades[id]) {
                    continue;
                }
                usades[id] = true;
                res[idx][0] = f;
                res[idx][1] = c;
                idx++;
            }
        }
        return res;
    }

    private int[][] calcularPosicionsInicialsAleatories(int numP, int dim) {
        int total = dim * dim;
        int[] celles = new int[total];
        for (int i = 0; i < total; i++) {
            celles[i] = i;
        }
        // Fisher-Yates shuffle per a ordre aleatori
        for (int i = total - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = celles[i];
            celles[i] = celles[j];
            celles[j] = tmp;
        }
        int[][] res = new int[numP][2];
        if (backtrackInicials(celles, res, 0, numP, dim, new boolean[total])) {
            return res;
        }
        // Fallback
        return calcularPosicionsInicialsFixes(numP, dim);
    }

    private boolean backtrackInicials(int[] celles, int[][] res, int pieceIdx, int numP, int dim, boolean[] usades) {
        if (pieceIdx == numP) {
            return true;
        }
        for (int i = 0; i < celles.length; i++) {
            if (usades[i]) {
                continue;
            }
            int f = celles[i] / dim;
            int c = celles[i] % dim;
            res[pieceIdx][0] = f;
            res[pieceIdx][1] = c;

            boolean valid = true;
            for (int j = 0; j < pieceIdx && valid; j++) {
                if (capturaMutua(pieceIdx, f, c, j, res[j][0], res[j][1])) {
                    valid = false;
                }
            }

            if (valid) {
                usades[i] = true;
                if (backtrackInicials(celles, res, pieceIdx + 1, numP, dim, usades)) {
                    return true;
                }
                usades[i] = false;
            }
        }
        return false;
    }

    private int[][] calcularPosicionsInicialsUsuari(int numP, int dim) {
        int[][] sel = getPosicionsIniciUsuari();
        int[][] res = new int[numP][2];
        if (sel == null) {
            return calcularPosicionsInicialsFixes(numP, dim);
        }

        boolean[] usades = new boolean[dim * dim];
        int count = 0;

        for (int i = 0; i < sel.length && count < numP; i++) {
            int f = sel[i][0];
            int c = sel[i][1];
            if (!dinsTaulell(f, c, dim)) {
                continue;
            }
            int id = f * dim + c;
            if (usades[id]) {
                continue;
            }
            usades[id] = true;
            res[count][0] = f;
            res[count][1] = c;
            count++;
        }

        for (int f = 0; f < dim && count < numP; f++) {
            for (int c = 0; c < dim && count < numP; c++) {
                int id = f * dim + c;
                if (!usades[id]) {
                    usades[id] = true;
                    res[count][0] = f;
                    res[count][1] = c;
                    count++;
                }
            }
        }
        return res;
    }

    /**
     * Comprova que cap peça puga capturar una altra en les posicions donades.
     *
     * @param files files actuals de les peces
     * @param cols columnes actuals de les peces
     * @return true si totes les peces estan en posicions segures
     */
    public boolean posicionsSegures(int[] files, int[] cols) {
        int numP = peces.size();
        int[][] pos = new int[numP][2];
        for (int i = 0; i < numP; i++) {
            pos[i][0] = files[i];
            pos[i][1] = cols[i];
        }
        for (int i = 0; i < numP; i++) {
            for (int j = i + 1; j < numP; j++) {
                if (potCapturar(i, j, pos) || potCapturar(j, i, pos)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Detecta i descriu el conflicte entre peces inicials.
     *
     * @param files files de les peces
     * @param cols columnes de les peces
     * @return missatge descriptiu del conflicte detectat
     */
    public String detectarConflicte(int[] files, int[] cols) {
        int numP = peces.size();
        int[][] pos = new int[numP][2];
        for (int i = 0; i < numP; i++) {
            pos[i][0] = files[i];
            pos[i][1] = cols[i];
        }
        for (int i = 0; i < numP; i++) {
            for (int j = i + 1; j < numP; j++) {
                if (potCapturar(i, j, pos)) {
                    String nom1 = classesPeces.get(i).substring(classesPeces.get(i).lastIndexOf('.') + 1);
                    String nom2 = classesPeces.get(j).substring(classesPeces.get(j).lastIndexOf('.') + 1);
                    return nom1 + " mata a " + nom2;
                }
                if (potCapturar(j, i, pos)) {
                    String nom1 = classesPeces.get(j).substring(classesPeces.get(j).lastIndexOf('.') + 1);
                    String nom2 = classesPeces.get(i).substring(classesPeces.get(i).lastIndexOf('.') + 1);
                    return nom1 + " mata a " + nom2;
                }
            }
        }
        return "dues peces es capturen mútuament";
    }

    /**
     * Comprova si dues peces es poden capturar mútuament.
     *
     * @param idxA índex de la primera peça
     * @param fA fila de la primera peça
     * @param cA columna de la primera peça
     * @param idxB índex de la segona peça
     * @param fB fila de la segona peça
     * @param cB columna de la segona peça
     * @return true si una de les dues pot capturar l'altra
     */
    public boolean capturaMutua(int idxA, int fA, int cA, int idxB, int fB, int cB) {
        return potCapturarDirect(idxA, fA, cA, idxB, fB, cB)
                || potCapturarDirect(idxB, fB, cB, idxA, fA, cA);
    }

    private boolean potCapturar(int idxAtacant, int idxObjectiu, int[][] pos) {
        int df = pos[idxObjectiu][0] - pos[idxAtacant][0];
        int dc = pos[idxObjectiu][1] - pos[idxAtacant][1];
        int numMovs = getNumMovs(idxAtacant);
        for (int m = 0; m < numMovs; m++) {
            if (getMovX(idxAtacant, m) == df && getMovY(idxAtacant, m) == dc) {
                return true;
            }
        }
        return false;
    }

    private boolean potCapturarDirect(int idxA, int fA, int cA, int idxB, int fB, int cB) {
        int df = fB - fA;
        int dc = cB - cA;
        int numMovs = getNumMovs(idxA);
        for (int m = 0; m < numMovs; m++) {
            if (getMovX(idxA, m) == df && getMovY(idxA, m) == dc) {
                return true;
            }
        }
        return false;
    }

    private boolean dinsTaulell(int f, int c, int dim) {
        return f >= 0 && f < dim && c >= 0 && c < dim;
    }
}
