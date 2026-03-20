package model.peces;

/**
 * Classe que gestiona la Peça 'Torre' amb els seus moviments. La Torre es mou
 * en línia recta qualsevol nombre de caselles dins del taulell, tant en
 * direcció:
 *
 * - vertical (amunt i avall) - horitzontal (esquerra i dreta)
 *
 * A diferència d'altres peces, els seus moviments DEPENEN de la dimensió del
 * taulell. Per aquest motiu, necessita conèixer la mida del taulell per generar
 * tots els moviments possibles.
 *
 * @author Equip
 */
public class Torre extends Peca {

    /**
     * Mètode constructor 1. Només s'utilitza per instanciació inicial.
     */
    public Torre() {
        afectadimension = true; // indicam que depèn del taulell.
        nom = this.getClass().getName();
        imatge = "torre.png";
        movx = new int[0];
        movy = new int[0];
    }

    /**
     * Mètode constructor 2.
     *
     * @param d
     */
    public Torre(int d) {
        afectadimension = true; // es mou en direcció del taulell.
        nom = this.getClass().getName();
        imatge = "torre.png";

        // Per cada direcció hi ha (d-1) moviments possibles:
        // amunt, avall, esquerra i dreta: 4 direccions
        movx = new int[(d - 1) * 4];
        movy = new int[(d - 1) * 4];
        int pos = 0;
        for (int i = -(d - 1); i < d; i++) {
            if (i != 0) {
                movx[pos] = 0; // vertical
                movy[pos++] = i; //vertical
                movx[pos] = i; // horitzontal
                movy[pos++] = 0; //horitzontal            
            }
        }
    }
}
