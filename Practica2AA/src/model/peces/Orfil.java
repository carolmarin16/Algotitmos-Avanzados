package model.peces;

public class Orfil extends Peca {

    public Orfil() {

        nombre = this.getClass().getName();
        //imagen = "/imagenes/orfil.png";
        imagen = "/imagenes/caballo.png";

        movx = new int[4];
        movy = new int[4];

        int pos = 0;

        movx[pos] = 1;
        movy[pos++] = 1;

        movx[pos] = 1;
        movy[pos++] = -1;

        movx[pos] = -1;
        movy[pos++] = 1;

        movx[pos] = -1;
        movy[pos++] = -1;
    }
}
