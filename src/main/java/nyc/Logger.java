package nyc;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by assaad on 21/04/2017.
 */
public class Logger {
    private final static String path = "/Users/assaad/Desktop/result/log.csv";
    private static BufferedWriter bw;

    public static void start(){
        try {
            bw = new BufferedWriter(new FileWriter(path));
            bw.write("file,line number,exception");
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void println(String s){
        try {
            bw.write(s);
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.err.println(s);
    }


    public static void close(){
        try {
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
