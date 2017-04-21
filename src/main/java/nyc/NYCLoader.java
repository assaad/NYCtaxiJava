package nyc;

import greycat.*;
import greycat.importer.ImporterPlugin;
import greycat.leveldb.LevelDBStorage;
import greycat.ml.MLPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

import static greycat.Tasks.newTask;
import static greycat.importer.ImporterActions.readFiles;
import static greycat.importer.ImporterActions.readLines;

/**
 * Created by assaad on 20/04/2017.
 */
public class NYCLoader {
    private static String path = "/Volumes/SSD/data/";
    //    private static String path = "/Volumes/SSD/testdata/";
//    private static String leveldb = "/Volumes/NTFS/database/nyc/leveldb/";
    private static String leveldb = "/Users/assaad/Desktop/leveldb/";


    public static void processTime(TaskContext ctx) {
        long ts = (long) ctx.variable("starttime").get(0);
        long now = System.currentTimeMillis();
        long diff = now - ts;
        double time = diff / 1000.0;
        long count = (long) ctx.variable("counter").get(0);
        double speed = count / time;
        ctx.setVariable("processtime", time);
        ctx.setVariable("speed", speed);
        ctx.continueTask();

    }


    public static void main(String[] args) {

        deleteTestBase();


        final SimpleDateFormat weatherDateFormat = new SimpleDateFormat("yyyyMMdd");
        weatherDateFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        final int WEATHER_DATE_POS = 2;

        final SimpleDateFormat yellowDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        yellowDateFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));

        final SimpleDateFormat yellowDateFormat2 = new SimpleDateFormat("dd/MM/yyyy hh:mm");
        yellowDateFormat2.setTimeZone(TimeZone.getTimeZone("America/New_York"));


        final Graph graph = GraphBuilder
                .newBuilder()
                .withMemorySize(4000000)
                .withPlugin(new MLPlugin())
                .withPlugin(new ImporterPlugin())
                .withStorage(new LevelDBStorage(leveldb))
                .build();

        graph.connect(new Callback<Boolean>() {
            public void on(Boolean result) {
                final Task readFiles = newTask()
                        .then(readFiles("{{path}}"))
                        .setAsVar("files")
                        .thenDo(new ActionFunction() {
                            @Override
                            public void eval(TaskContext ctx) {
                                TaskResult filenames = ctx.result();

                                for (int i = 0; i < filenames.size(); i++) {
                                    String fn = (String) filenames.get(i);
                                    if (fn.contains("fhv_tripdata")) {
                                        ctx.addToVariable("fhv", fn);
                                    } else if (fn.contains("green_tripdata")) {
                                        ctx.addToVariable("green", fn);
                                    } else if (fn.contains("yellow_tripdata")) {
                                        ctx.addToVariable("yellow", fn);
                                    } else if (fn.contains("uber-raw")) {
                                        ctx.addToVariable("uber", fn);
                                    } else {
                                        ctx.addToVariable("params", fn);
                                    }
                                }
                                ctx.continueTask();
                            }
                        })
                        .readVar("params")
                        .forEach(newTask()
                                .setAsVar("file")
                                .then(readLines("{{result}}"))
                                .declareVar("headers")
                                .declareVar("mainNode")
                                .forEach(newTask()
                                        .thenDo(new ActionFunction() {
                                                    public void eval(TaskContext ctx) {
                                                        long counter = (long) ctx.variable("counter").get(0);
                                                        counter++;
                                                        ctx.setVariable("counter", counter);
                                                        String line = (String) ctx.result().get(0);
                                                        String[] fields = line.split(",");
                                                        String file = (String) ctx.variable("file").get(0);
                                                        int i = (int) ctx.variable("i").get(0);

                                                        if (file.contains("weather")) {
                                                            if (i == 0) {
                                                                for (int j = 0; j < fields.length; j++) {
                                                                    fields[j] = fields[j].toLowerCase().trim();
                                                                }
                                                                ctx.setVariable("headers", fields);
                                                                Node weather = graph.newNode(0, Constants.BEGINNING_OF_TIME);
                                                                ctx.setVariable("mainNode", weather);
                                                                ctx.continueTask();
                                                            } else {
                                                                Node weather = (Node) ctx.variable("mainNode").get(0);
                                                                Object[] headers = ctx.variable("headers").asArray();
                                                                try {
                                                                    Date date = weatherDateFormat.parse((String) fields[WEATHER_DATE_POS]);
                                                                    weather.travelInTime(date.getTime(), new Callback<Node>() {
                                                                        @Override
                                                                        public void on(Node result) {
                                                                            for (int i = 0; i < headers.length; i++) {
                                                                                if (i < WEATHER_DATE_POS) {
                                                                                    result.set((String) headers[i], Type.STRING, fields[i]);
                                                                                } else if (i == WEATHER_DATE_POS) {
                                                                                    continue;
                                                                                } else {
                                                                                    result.set((String) headers[i], Type.DOUBLE, Double.valueOf(fields[i]));
                                                                                }
                                                                            }
                                                                            result.free();
                                                                            ctx.continueTask();
                                                                        }
                                                                    });
                                                                } catch (ParseException e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                        } else if (file.contains("fhv_bases")) {
                                                            if (i == 0) {
                                                                for (int j = 0; j < fields.length; j++) {
                                                                    fields[j] = fields[j].toLowerCase().trim();
                                                                }
                                                                ctx.setVariable("headers", fields);
                                                                ctx.continueTask();
                                                            } else {
                                                                Object[] headers = ctx.variable("headers").asArray();
                                                                Node baseNode = graph.newNode(0, Constants.BEGINNING_OF_TIME);
                                                                baseNode.setTimeSensitivity(-1, 0);
                                                                for (int j = 0; j < Math.min(headers.length, fields.length); j++) {
                                                                    baseNode.set((String) headers[j], Type.STRING, fields[j]);
                                                                }
                                                                graph.index(0, Constants.BEGINNING_OF_TIME, "fhv-bases", new Callback<NodeIndex>() {
                                                                    @Override
                                                                    public void on(NodeIndex result) {
                                                                        result.addToIndex(baseNode, (String) headers[0]);
                                                                        baseNode.free();
                                                                        ctx.continueTask();
                                                                    }
                                                                });
                                                            }
                                                        } else if (file.contains("taxi")) {
                                                            if (i == 0) {
                                                                for (int j = 0; j < fields.length; j++) {
                                                                    fields[j] = fields[j].toLowerCase().trim();
                                                                }
                                                                ctx.setVariable("headers", fields);
                                                                ctx.continueTask();
                                                            } else {
                                                                Object[] headers = ctx.variable("headers").asArray();
                                                                Node baseNode = graph.newNode(0, Constants.BEGINNING_OF_TIME);
                                                                baseNode.setTimeSensitivity(-1, 0);

                                                                for (int j = 0; j < Math.min(headers.length, fields.length); j++) {
                                                                    baseNode.set((String) headers[j], Type.STRING, fields[j]);
                                                                }
                                                                graph.index(0, Constants.BEGINNING_OF_TIME, "taxi-zone-lookup", new Callback<NodeIndex>() {
                                                                    @Override
                                                                    public void on(NodeIndex result) {
                                                                        result.addToIndex(baseNode, (String) headers[0]);
                                                                        baseNode.free();
                                                                        ctx.continueTask();
                                                                    }
                                                                });
                                                            }
                                                        } else {
                                                            System.out.println("unknown file: " + file);
                                                            ctx.continueTask();
                                                        }
                                                    }
                                                }
                                        )
                                )
                                .thenDo(new ActionFunction() {
                                    @Override
                                    public void eval(TaskContext ctx) {
                                        processTime(ctx);
                                    }
                                })
                                .log("File {{file}} done. Parsed lines: {{counter}}, time elapsed: {{processtime}} s, speed: {{speed}} v/s")
                                .save()
                        )

                        .readVar("yellow")
                        .forEach(newTask()
                                .setAsVar("file")
                                .declareVar("headers")
                                .declareVar("vendor")
                                .declareVar("record")
                                .declareVar("tripstart")
                                .declareVar("tripend")
                                .then(readLines("{{result}}"))
                                .forEach(newTask()
                                        .thenDo(new ActionFunction() {
                                                    public void eval(TaskContext ctx) {
                                                        long counter = (long) ctx.variable("counter").get(0);
                                                        counter++;
                                                        ctx.setVariable("counter", counter);
                                                        String line = (String) ctx.result().get(0);
                                                        String[] fields = line.split(",");
                                                        int i = (int) ctx.variable("i").get(0);

                                                        if (i == 0) {
                                                            for (int j = 0; j < fields.length; j++) {
                                                                fields[j] = fields[j].toLowerCase().trim();
                                                            }
                                                            ctx.setVariable("headers", fields);
                                                            ctx.setVariable("vendor", null);
                                                            ctx.setVariable("record", null);
                                                            ctx.setVariable("tripstart", null);
                                                            ctx.setVariable("tripend", null);
                                                            ctx.continueTask();
                                                        } else {
                                                            for (int j = 0; j < fields.length; j++) {
                                                                fields[j] = fields[j].trim();
                                                            }
                                                            Object[] headers = ctx.variable("headers").asArray();

                                                            TripRecord tripRecord =new TripRecord(headers,fields);
                                                            Node record = tripRecord.getNode(graph);

                                                            ctx.setVariable("vendor", tripRecord.vendorID);
                                                            ctx.setVariable("record", record);
                                                            ctx.setVariable("tripstart", tripRecord.pickup_datetime);
                                                            ctx.setVariable("tripend", tripRecord.dropoff_datetime);
                                                            record.free();
                                                            ctx.continueTask();
                                                        }
                                                    }
                                                }
                                        )
                                        .ifThen(new ConditionalFunction() {
                                            @Override
                                            public boolean eval(TaskContext ctx) {
                                                long count = (long) ctx.variable("counter").get(0);
                                                return count % 500000 == 0;
                                            }
                                        }, newTask()
                                                .thenDo(new ActionFunction() {
                                                    @Override
                                                    public void eval(TaskContext ctx) {
                                                        processTime(ctx);
                                                    }
                                                })
                                                .log("File {{file}} partially done. Parsed lines: {{counter}}, time elapsed: {{processtime}} s, speed: {{speed}} v/s")
                                                .save())

                                )
                                .thenDo(new ActionFunction() {
                                    @Override
                                    public void eval(TaskContext ctx) {
                                        processTime(ctx);
                                    }
                                })
                                .log("File {{file}} done. Parsed lines: {{counter}}, time elapsed: {{processtime}} s, speed: {{speed}} v/s")
                                .save()

                        );


                TaskContext context = readFiles.prepare(graph, null, new Callback<TaskResult>() {
                    public void on(TaskResult result) {
                        if (result.exception() != null) {
                            result.exception().printStackTrace();
                        }
                    }
                });
                context.setVariable("starttime", System.currentTimeMillis());
                context.setVariable("path", path);
                context.setVariable("counter", 0l);
                context.setVariable("speed", 0);
                readFiles.executeUsing(context);

            }
        });
    }




    public static void deleteTestBase() {
        try {
            Files.walk(Paths.get(leveldb), FileVisitOption.FOLLOW_LINKS)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    //.peek(System.out::println)
                    .forEach(File::delete);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

