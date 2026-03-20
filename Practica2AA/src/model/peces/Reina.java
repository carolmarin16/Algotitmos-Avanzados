package model.peces;

/**
 * Classe que gestiona la Peça 'Reina' amb els seus moviments. La Reina és la
 * peça més potent del joc, ja que pot moure's qualsevol nombre de caselles en:
 *
 * - direcció vertical - direcció horitzontal - direcció diagonal
 *
 * Com que el nombre de moviments possibles depèn de la dimensió del taulell,
 * necessita conèixer la mida del tauler per generar tots els seus moviments.
 *
 * @author Equip
 */
public class Reina extends Peca {

    /**
     * Mètode constructor 1. S'utilitza per a la creació inicial de la peça
     * abans de conèixer la dimensió del taulell.
     */
    public Reina() {
        afectadimension = true; //se mueve en dimensión tablero
        imatge = "reina.png";
        movx = new int[0];
        movy = new int[0];
        nom = this.getClass().getName();
    }

    /**
     * Mètode constructor 2.
     *
     * @param d
     */
    public Reina(int d) {
        afectadimension = true; // Els moviments de la reina depenen del taulell.
        nom = this.getClass().getName();
        imatge = "reina.png";
        movx = new int[(d - 1) * 4 * 2];
        movy = new int[(d - 1) * 4 * 2];
        int pos = 0;
        for (int i = -(d - 1); i < d; i++) {
            if (i != 0) {
                movx[pos] = 0; // vertical
                movy[pos++] = i; //vertical
                movx[pos] = i; // horitzontal
                movy[pos++] = 0; //horitzontal
                movx[pos] = i; //   oblicu 1
                movy[pos++] = i; //    oblicu 1
                movx[pos] = -i; //   oblicu 2
                movy[pos++] = i; //    oblicu 2               
            }
        }
    }
}
