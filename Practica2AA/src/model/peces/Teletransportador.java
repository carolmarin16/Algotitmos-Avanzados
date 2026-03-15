package model.peces;

public class Teletransportador extends Peca {

    public Teletransportador() {

        nombre = this.getClass().getName();
        //imagen = "/imagenes/teletransportador.png";
        imagen = "/imagenes/caballo.png";

        movx = new int[8];
        movy = new int[8];

        int pos = 0;

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
    }
}
