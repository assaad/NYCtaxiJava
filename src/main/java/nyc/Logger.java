package nyc;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * Created by assaad on 21/04/2017.
 */
public class Logger {
    private final static String path = "/Users/assaad/Desktop/result/";

    private final static DecimalFormat df = new DecimalFormat("#.###");
    private static BufferedWriter errorWriter;
    private static BufferedWriter speedWriter;
    private static long starttime;

    private static long totalerrors = 0;


    public static void start() {
        try {
            starttime = System.currentTimeMillis();
            errorWriter = new BufferedWriter(new FileWriter(path + "error.csv"));
            errorWriter.write("file,line number,exception");
            errorWriter.newLine();
            errorWriter.flush();

            speedWriter = new BufferedWriter(new FileWriter(path + "speed.csv"));
            speedWriter.write("file,partial,total lines,total time (in s),speed (kv/s), total errors");
            speedWriter.newLine();
            speedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void printErr(String s) {
        try {
            totalerrors++;
            errorWriter.write(s);
            errorWriter.newLine();
            if(totalerrors%100==0) {
                errorWriter.flush();
                System.err.println(s+", total err: "+totalerrors);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void printSpeed(long totallines, String file, boolean partial) {
        long now = System.currentTimeMillis();
        long diff = now - starttime;
        double time = diff / 1000.0;
        double speed = totallines / (1000.0 * time);
        try {
            speedWriter.write(file + "," + partial + "," + totallines + "," + time + "," + speed+","+totalerrors);
            speedWriter.newLine();
            speedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (partial) {
            System.out.println("File " + file + " partially done: " + (totallines / 1000000) + "M, time elapsed: " + time + " s, speed: " + df.format(speed) + " kv/s, total err: "+totalerrors);
        } else {
            System.out.println("File " + file + " done: " + (totallines / 1000000) + "M, time elapsed: " + time + " s, speed: " + df.format(speed) + " kv/s, total err: "+totalerrors);
            System.out.println();
        }
    }


    public static void close() {
        try {
            System.out.println("Total errors: " + totalerrors);
            errorWriter.close();
            speedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
