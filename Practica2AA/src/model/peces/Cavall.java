package model.peces;

/**
 * Classe que gestiona la Peça 'Cavall' amb els seus moviments. El Cavall es mou
 * en forma de "L", és a dir: - dues caselles en una direcció (vertical o
 * horitzontal) - una casella en direcció perpendicular
 *
 * Aquest moviment genera un total de 8 moviments possibles, independents de la
 * dimensió del taulell.
 *
 * A diferència d'altres peces com la Torre o la Reina, els moviments del Cavall
 * són fixes i no depenen de la mida del tauler.
 *
 * @author Equip
 */
public class Cavall extends Peca {

    /**
     * Mètode constructor.
     */
    public Cavall() {
        nom = this.getClass().getName();
        imatge = "cavall.png";

        // El cavall té 8 moviments possibles.
        movx = new int[8];
        movy = new int[8];

        int pos = 0;

        // Moviments en forma de L (±1, ±2) i (±2, ±1)
        movx[pos] = 1;
        movy[pos++] = 2;
        movx[pos] = 2;
        movy[pos++] = 1;
        movx[pos] = 1;
        movy[pos++] = -2;
        movx[pos] = 2;
        movy[pos++] = -1;
        movx[pos] = -1;
        movy[pos++] = 2;
        movx[pos] = -2;
        movy[pos++] = 1;
        movx[pos] = -1;
        movy[pos++] = -2;
        movx[pos] = -2;
        movy[pos++] = -1;
    }
}
