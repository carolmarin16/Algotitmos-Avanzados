package model.peces;

/**
 * Classe abstracta que representa una peça del joc.
 *
 * Defineix el comportament comú de totes les peces que es poden col·locar al
 * taulell. Cada peça disposa d’un conjunt de moviments possibles definits
 * mitjançant desplaçaments en els eixos X i Y.
 *
 * @author Equip
 */
public abstract class Peca {

    /**
     * Desplaçaments horitzontals possibles de la peça.
     */
    protected int movx[];

    /**
     * Desplaçaments verticals possibles de la peça.
     */
    protected int movy[];

    /**
     * Nom de la peça.
     */
    protected String nom;

    /**
     * Ruta o nom del fitxer d’imatge associat a la peça.
     */
    protected String imatge;

    /**
     * Indica si la peça depèn de la dimensió del taulell per inicialitzar els
     * seus moviments.
     */
    protected boolean afectadimension = false;

    /**
     *
     * Indica si la peça necessita conèixer la dimensió del taulell.
     *
     * @return
     */
    public boolean afectaDimensio() {
        return afectadimension;
    }

    /**
     * Retorna el nom de la peça.
     *
     * @return
     */
    public String getNombre() {
        return nom;
    }

    /**
     * Retorna la imatge associada a la peça.
     *
     * @return
     */
    public String getImagen() {
        return imatge;
    }

    /**
     * Retorna el nombre total de moviments possibles.
     *
     * @return
     */
    public int getNumMovs() {
        return movx.length;
    }

    /**
     * Retorna el desplaçament horitzontal d’un moviment.
     *
     * @param i
     * @return
     */
    public int getMovX(int i) {
        return movx[i];
    }

    /**
     * Retorna el desplaçament vertical d’un moviment.
     *
     * @param i
     * @return
     */
    public int getMovY(int i) {
        return movy[i];
    }
}
