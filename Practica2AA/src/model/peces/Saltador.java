package model.peces;

/**
 * Classe que gestiona la Peça 'Saltador' amb els seus moviments. El Saltador és
 * una peça especial que combina dos tipus de moviment:
 *
 * 1) Moviments de Cavall: - Es desplaça en forma de L (2 caselles en una
 * direcció i 1 en la perpendicular). - Té un total de 8 moviments possibles com
 * el cavall dels escacs.
 *
 * 2) Salts rectes llargs: - Pot saltar exactament 3 caselles en línia recta
 * (amunt, avall, esquerra o dreta). - Afegeix 4 moviments addicionals.
 *
 * En total, el Saltador disposa de 12 moviments possibles.
 *
 * Aquesta peça NO depèn de la dimensió del taulell, ja que els seus moviments
 * són fixes.
 *
 * @author Equip
 */
public class Saltador extends Peca {

    /**
     * Mètode constructor.
     */
    public Saltador() {
        nom = this.getClass().getName();
        imatge = "saltador.png";

        // 8 moviments de cavall + 4 salts rectes = 12 moviments totals
        movx = new int[12];
        movy = new int[12];

        int pos = 0;

        // Moviments tipus CAVALL
        // (±2, ±1) i (±1, ±2)
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

        // Salts rectes de 3 caselles
        // Amunt
        movx[pos] = 0;
        movy[pos++] = 3;
        // Avall
        movx[pos] = 0;
        movy[pos++] = -3;
        // Dreta
        movx[pos] = 3;
        movy[pos++] = 0;
        // Esquerra
        movx[pos] = -3;
        movy[pos++] = 0;
    }
}
