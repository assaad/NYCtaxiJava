package nyc;

import java.io.*;

/**
 * Created by assaad on 20/04/2017.
 */
public class Headers {

    private static String path = "/Volumes/SSD/data/";
    private static String pathTest = "/Volumes/SSD/headers/h.csv";
    private static int TOCOPY = 1;

    public static void main(String[] args) {

        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();

        long totallines = 0;
        long starttime = System.currentTimeMillis();
        String line;

        System.out.println(listOfFiles.length);

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(pathTest));

            for (File listOfFile : listOfFiles) {
                int copylines = 0;
                BufferedReader br = new BufferedReader(new FileReader(listOfFile));
                bw.write(listOfFile.getName()+",");
                while ((line = br.readLine()) != null) {
                    bw.write(line);
                    bw.newLine();
                    totallines++;
                    copylines++;
                    if (copylines == TOCOPY) {
                        break;
                    }
                }
                bw.flush();
                long now = System.currentTimeMillis();
                long diff = now - starttime;
                double time = diff / 1000.0;
                System.out.println("File " + listOfFile.getName() + " done: " + totallines + " time elapsed: " + time + " s");
            }
            bw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
