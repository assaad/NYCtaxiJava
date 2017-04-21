package nyc;

import java.io.*;

/**
 * Created by assaad on 20/04/2017.
 */
public class Subset {

    private static String path = "/Volumes/SSD/data/";
    private static String pathTest = "/Volumes/SSD/testdata/";
    private static int TOCOPY=10000;

    public static void main(String[] args) {

        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();

        long totallines = 0;
        long starttime = System.currentTimeMillis();
        String line;

        System.out.println(listOfFiles.length);
        for (File listOfFile : listOfFiles) {
            int copylines=0;
            try (BufferedReader br = new BufferedReader(new FileReader(listOfFile))) {
                BufferedWriter bw =new BufferedWriter(new FileWriter(pathTest+listOfFile.getName()));
                while ((line = br.readLine()) != null) {
                    bw.write(line);
                    bw.newLine();
                    totallines++;
                    copylines++;
                    if(copylines==TOCOPY){
                        break;
                    }
                }
                bw.flush();
                bw.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            long now = System.currentTimeMillis();
            long diff = now - starttime;
            double time = diff / 1000.0;
            System.out.println("File " + listOfFile.getName() + " done: " + totallines + " time elapsed: " + time + " s");
        }
    }
}
