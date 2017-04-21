package nyc;

import com.sun.org.apache.regexp.internal.RE;

import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by assaad on 21/04/2017.
 */
public class NYCLoaderJava {
    private static String path = "/Volumes/SSD/data/";
    //    private static String path = "/Volumes/SSD/testdata/";


    private final static DecimalFormat df = new DecimalFormat("#.###");


    private static void printspeed(long starttime, long totallines, String file, boolean partial) {
        long now = System.currentTimeMillis();
        long diff = now - starttime;
        double time = diff / 1000.0;
        double speed = totallines /(1000.0* time);
        if (partial) {
            System.out.println("File " + file + " partially done: " + (totallines/1000000) + "M, time elapsed: " + time + " s, speed: " + df.format(speed) + " kv/s");
        } else {
            System.out.println("File " + file + " done: " + (totallines/1000000) + "M, time elapsed: " + time + " s, speed: " + df.format(speed) + " kv/s");
            System.out.println();
        }

    }

    public static void main(String[] args) {
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();

        long totallines = 0;
        long lineinfile = 0;
        long starttime = System.currentTimeMillis();
        String line;

        try {
            for (File listOfFile : listOfFiles) {
                if (!listOfFile.getName().contains("yellow_tripdata")) {
                    continue;
                }

                BufferedReader br = new BufferedReader(new FileReader(listOfFile));
                lineinfile = 0;
                String[] headers = null;
                String[] fields = null;

                while ((line = br.readLine()) != null) {
                    if (lineinfile == 0) {
                        headers = line.split(",");
                        for (int j = 0; j < headers.length; j++) {
                            headers[j] = headers[j].toLowerCase().trim();
                        }
                    } else {
                        fields = line.split(",");
                        for (int j = 0; j < fields.length; j++) {
                            fields[j] = fields[j].trim();
                        }
                        TripRecord r = new TripRecord(headers, fields);
                    }

                    lineinfile++;
                    totallines++;
                    if (totallines % 1000000 == 0) {
                        printspeed(starttime, totallines, listOfFile.getName(), true);
                    }
                }
                printspeed(starttime, totallines, listOfFile.getName(), false);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
