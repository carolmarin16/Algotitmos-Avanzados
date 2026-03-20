package model.peces;

/**
 * Classe que gestiona la Peça 'Teletransportador' amb els seus moviments. El
 * Teletransportador és una peça especial capaç de desplaçar-se instantàniament
 * a diverses posicions properes del taulell sense seguir un moviment continu.
 *
 * Característiques del moviment:
 *
 * - Pot moure's 1, 2 o 3 caselles de distància. - Els moviments poden ser: •
 * horitzontals • verticals • diagonals - Sempre es desplaça directament a la
 * casella destí, simulant un "teletransport".
 *
 * Això significa que combina moviments equivalents a: - rei ampliat (distància
 * 1), - salts mitjans (distància 2), - salts llargs (distància 3).
 *
 * En total disposa de 32 moviments possibles.
 *
 * Aquesta peça NO depèn de la dimensió del taulell, ja que els seus moviments
 * estan predefinits.
 *
 * @author Equip
 */
public class Teletransportador extends Peca {

    /**
     * Mètode constructor.
     */
    public Teletransportador() {
        nom = this.getClass().getName();
        imatge = "teletransportador.png";

        // 32 moviments totals
        movx = new int[32];
        movy = new int[32];

        int pos = 0;

        // Distància 1 (moviment tipus REI)
        movx[pos] = 1;
        movy[pos++] = 0;
        movx[pos] = -1;
        movy[pos++] = 0;
        movx[pos] = 0;
        movy[pos++] = 1;
        movx[pos] = 0;
        movy[pos++] = -1;

        // Diagonals
        movx[pos] = 1;
        movy[pos++] = 1;
        movx[pos] = 1;
        movy[pos++] = -1;
        movx[pos] = -1;
        movy[pos++] = 1;
        movx[pos] = -1;
        movy[pos++] = -1;

        // Distància 2
        movx[pos] = 2;
        movy[pos++] = 0;
        movx[pos] = -2;
        movy[pos++] = 0;
        movx[pos] = 0;
        movy[pos++] = 2;
        movx[pos] = 0;
        movy[pos++] = -2;
        movx[pos] = 2;
        movy[pos++] = 2;
        movx[pos] = 2;
        movy[pos++] = -2;
        movx[pos] = -2;
        movy[pos++] = 2;
        movx[pos] = -2;
        movy[pos++] = -2;

        // Distància 3
        movx[pos] = 3;
        movy[pos++] = 0;
        movx[pos] = -3;
        movy[pos++] = 0;
        movx[pos] = 0;
        movy[pos++] = 3;
        movx[pos] = 0;
        movy[pos++] = -3;
        movx[pos] = 3;
        movy[pos++] = 3;
        movx[pos] = 3;
        movy[pos++] = -3;
        movx[pos] = -3;
        movy[pos++] = 3;
        movx[pos] = -3;
        movy[pos++] = -3;
    }
}
