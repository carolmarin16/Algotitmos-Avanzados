package model.peces;

/**
 * Classe que gestiona la Peça 'Orfil' amb els seus moviments. L'Orfil és una
 * peça que es pot moure únicament en direccions diagonals del tauler.
 *
 * Els seus moviments no depenen del taulell.
 *
 * @author Equip
 */
public class Orfil extends Peca {

    /**
     * Mètode constructor.
     */
    public Orfil() {

        nom = this.getClass().getName();
        imatge = "orfil.png";

        movx = new int[4];
        movy = new int[4];

        int pos = 0;

        // Diagonal inferior dreta (+1, +1)
        movx[pos] = 1;
        movy[pos++] = 1;

        // Diagonal superior dreta (+1, -1)
        movx[pos] = 1;
        movy[pos++] = -1;

        // Diagonal inferior esquerra (-1, +1)
        movx[pos] = -1;
        movy[pos++] = 1;

        // Diagonal superior esquerra (-1, -1)
        movx[pos] = -1;
        movy[pos++] = -1;
    }
}
