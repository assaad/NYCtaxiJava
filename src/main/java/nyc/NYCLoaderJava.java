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

    public static void main(String[] args) {
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();

        long totallines = 0;
        long lineinfile = 0;
        String line;
        Logger.start();

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
                    lineinfile++;
                    totallines++;

                    if (lineinfile == 1) {
                        headers = line.split(",");
                        for (int j = 0; j < headers.length; j++) {
                            headers[j] = headers[j].toLowerCase().trim();
                        }
                    } else {
                        fields = line.split(",");
                        for (int j = 0; j < fields.length; j++) {
                            fields[j] = fields[j].trim();
                        }
                        TripRecord r = new TripRecord(headers, fields, listOfFile.getName(), lineinfile);
                    }

                    if (totallines % 1000000 == 0) {
                        Logger.printSpeed(totallines, listOfFile.getName(), true);
                    }
                }
                Logger.printSpeed(totallines, listOfFile.getName(), false);
            }
            Logger.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
