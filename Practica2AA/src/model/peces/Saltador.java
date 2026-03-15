package model.peces;

public class Saltador extends Peca {

    public Saltador() {
        nombre = this.getClass().getName();
        imagen = "/imagenes/caballo.png";
        //imagen = "/imagenes/saltador.png";

        movx = new int[4];
        movy = new int[4];

        int pos = 0;

        movx[pos] = 0;
        movy[pos++] = 3;

        movx[pos] = 0;
        movy[pos++] = -3;

        movx[pos] = 3;
        movy[pos++] = 0;

        movx[pos] = -3;
        movy[pos++] = 0;
    }
}
