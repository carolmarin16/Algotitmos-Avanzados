package control;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;

/**
 * Classe per homogeneizar l'amissió d'errors.
 *
 * @author Equip
 */
public class MeuErrors {

    static public void informaError(Exception ex) {
        informaError("S'ha produït un error.", ex);
    }

    static public void informaError(String context, Exception ex) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        String slogs = writer.toString();
        System.err.println(context + "\n" + slogs);
        try {
            FileWriter fr = new FileWriter("logs.txt", true);
            BufferedWriter br = new BufferedWriter(fr);
            br.write("********************** Error date: " + (new Date()).toString());
            br.newLine();
            br.write(context);
            br.newLine();
            br.write(slogs);
            br.write("********************** End error report");
            br.newLine();
            br.newLine();
            br.close();
            fr.close();
        } catch (Exception e) {
            System.err.println("Error a la manipulació de l'arxiu de logs.");
            e.printStackTrace();
        }
    }
}
