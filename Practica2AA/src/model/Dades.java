package model;

import model.peces.Peca;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Dades {

    private int dimension;
    private List<Peca> peces = new ArrayList<>();
    private List<String> clasesPeces = new ArrayList<>();
    private int[][] tablero;
    private int velocidad;

    // ──────────────────────────────────────── Constructores
    public Dades() {
        velocidad = 50;   // Normal (sincronizado con el slider inicial)
        List<String> def = new ArrayList<>();
        def.add("datos.piezas.Caballo");
        def.add("datos.piezas.Reina");
        inicializar(8, def);
    }

    public Dades(int n, List<String> clases) {
        velocidad = 50;
        inicializar(n, clases);
    }

    // ──────────────────────────────────────── Inicialización
    private void inicializar(int d, List<String> clases) {
        dimension = d;
        peces.clear();
        clasesPeces.clear();
        for (String c : clases) {
            instanciarYAgregar(c);
        }
        tablero = new int[dimension][dimension];
    }

    private void instanciarYAgregar(String className) {
        try {
            Class c = Class.forName(className);
            Peca p = (Peca) c.newInstance();
            if (p.afectaDimension()) {
                p = (Peca) c.getConstructor(int.class).newInstance(dimension);
            }
            peces.add(p);
            clasesPeces.add(className);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ──────────────────────────────────────── Gestión de Peces
    public void setPeces(List<String> clases) {
        peces.clear();
        clasesPeces.clear();
        for (String c : clases) {
            instanciarYAgregar(c);
        }
    }

    public void setPecaEn(int index, String className) {
        if (index < 0 || index >= peces.size()) {
            return;
        }
        try {
            Class c = Class.forName(className);
            Peca p = (Peca) c.newInstance();
            if (p.afectaDimension()) {
                p = (Peca) c.getConstructor(int.class).newInstance(dimension);
            }
            peces.set(index, p);
            clasesPeces.set(index, className);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void agregarPeca(String className) {
        instanciarYAgregar(className);
    }

    public void quitarUltimaPeca() {
        if (peces.size() > 2) {
            peces.remove(peces.size() - 1);
            clasesPeces.remove(clasesPeces.size() - 1);
        }
    }

    public int getNumPeces() {
        return peces.size();
    }

    public List<String> getClasesPeces() {
        return new ArrayList<>(clasesPeces);
    }

    // ──────────────────────────────────────── Tablero
    public void regenerar(int d) {
        dimension = d;
        List<String> viejas = new ArrayList<>(clasesPeces);
        peces.clear();
        clasesPeces.clear();
        for (String c : viejas) {
            instanciarYAgregar(c);
        }
        tablero = new int[dimension][dimension];
    }

    public int getDimension() {
        return dimension;
    }

    public boolean noPisadaYEnTablero(int fila, int columna) {
        if (fila < 0 || fila >= dimension || columna < 0 || columna >= dimension) {
            return false;
        }
        return tablero[fila][columna] == 0;
    }

    public boolean hayPeca(int fila, int columna) {
        return tablero[fila][columna] != 0;
    }

    public int quePeca(int fila, int columna) {
        return tablero[fila][columna];
    }

    public void ponPeca(int fila, int columna, int num) {
        tablero[fila][columna] = num;
    }

    public void quitaPeca(int fila, int columna) {
        tablero[fila][columna] = 0;
    }

    public int getVal(int i, int j) {
        return tablero[i][j];
    }

    // ──────────────────────────────────────── Acceso a movimientos (por índice de Peca)
    public int getNumMovs(int pieceIdx) {
        return peces.get(pieceIdx).getNumMovs();
    }

    public int getMovX(int pieceIdx, int moveIdx) {
        return peces.get(pieceIdx).getMovX(moveIdx);
    }

    public int getMovY(int pieceIdx, int moveIdx) {
        return peces.get(pieceIdx).getMovY(moveIdx);
    }

    // ──────────────────────────────────────── Imágenes (por índice de Peca)
    public String getImagen(int pieceIdx) {
        return peces.get(pieceIdx).getImagen();
    }

    // ──────────────────────────────────────── Velocidad
    public int getVelocidad() {
        return velocidad;
    }

    public void setVelocidad(int v) {
        velocidad = v;
    }

    // ──────────────────────────────────────── Compatibilidad con GUI antigua
    /**
     * Devuelve la primera clase seleccionada
     */
    public String getPecesel() {
        return clasesPeces.isEmpty() ? "" : clasesPeces.get(0);
    }

    /**
     * Backward compat: nombre de la primera Peca
     */
    public String getPeca() {
        return peces.isEmpty() ? "" : peces.get(0).getNombre();
    }

    /**
     * Backward compat: primera instancia de Peca (para descubrir el paquete)
     */
    public Peca getClasePeca() {
        return peces.isEmpty() ? null : peces.get(0);
    }

    public void imprimir() {
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                System.out.println(tablero[i][j]);
            }
        }
    }

    // ──────────────────────────────────────── Descubrimiento de Peces disponibles
    /**
     * Devuelve los nombres simples de todas las clases en datos.Peces (sin
     * Peca.class)
     */
    public String[] getNombresPecesDisponibles() {
        String paquete = "datos.Peces";
        URL path = getClass().getResource("/" + paquete.replaceAll("\\.", "/"));
        File dir = new File(path.getPath());
        String[] archivos = dir.list();
        List<String> nombres = new ArrayList<>();
        for (String f : archivos) {
            if (f.endsWith(".class") && !f.equals("Peca.class")) {
                nombres.add(f.replace(".class", ""));
            }
        }
        return nombres.toArray(new String[0]);
    }

    /**
     * Devuelve todos los Peca instanciados disponibles
     */
    public Peca[] getPecesDisponibles() {
        String paquete = "datos.Peces";
        String[] nombres = getNombresPecesDisponibles();
        List<Peca> lista = new ArrayList<>();
        for (String nombre : nombres) {
            try {
                Class c = Class.forName(paquete + "." + nombre);
                Peca p = (Peca) c.newInstance();
                lista.add(p);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return lista.toArray(new Peca[0]);
    }
}
