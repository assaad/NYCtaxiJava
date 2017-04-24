package nyc;

import java.io.*;

/**
 * Created by assaad on 20/04/2017.
 */
public class FixDataSet {
    private static String path = "/Volumes/SSD/fix/";
    private static String pathcorr = "/Volumes/SSD/fixcorr/";

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
                BufferedWriter bw = new BufferedWriter(new FileWriter(pathcorr + listOfFile.getName()));
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
                        bw.write(line);
                        bw.newLine();
                    } else {
                        fields = line.split(",");
                        for (int j = 0; j < fields.length; j++) {
                            fields[j] = fields[j].trim();
                        }
                        if (fields.length > headers.length) {
                            String[] ff = new String[headers.length];
                            for (int i = 0; i < 8; i++) {
                                ff[i] = fields[i];
                                bw.write(ff[i] + ",");
                            }
                            for (int i = 9; i < fields.length; i++) {
                                ff[i - 1] = fields[i];
                                bw.write(ff[i - 1] + ",");
                            }
                            fields = ff;
                            bw.newLine();
                        } else {
                            bw.write(line);
                            bw.newLine();
                        }
                        TripRecord r = new TripRecord(headers, fields, listOfFile.getName(), lineinfile);
                        if (totallines % 1000000 == 0) {
                            Logger.printSpeed(totallines, listOfFile.getName(), true);
                        }
                    }
                }
                bw.flush();
                bw.close();
                Logger.printSpeed(totallines, listOfFile.getName(), false);
            }
            Logger.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
