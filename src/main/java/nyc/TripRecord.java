package nyc;

import greycat.Graph;
import greycat.Node;
import greycat.Type;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by assaad on 21/04/2017.
 */

public class TripRecord {
    private final static SimpleDateFormat yellowDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private final static SimpleDateFormat yellowDateFormat2 = new SimpleDateFormat("dd/MM/yyyy hh:mm");


    static {
        yellowDateFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        yellowDateFormat2.setTimeZone(TimeZone.getTimeZone("America/New_York"));

    }

    public String vendorID = "";
    public long pickup_datetime = 0, dropoff_datetime = 0;
    public int passenger_count = 0;
    public double trip_distance = -1, pickup_longitude = -400, pickup_latitude = -400, dropoff_longitude = -400, dropoff_latitude = -400, fare = 0,
            extra = 0, tax = 0, tip = 0, tolls = 0, improvement = 0, total = 0;
    public int RatecodeID = -1;
    public int pulocationid = -1;
    public int dolocationid = -1;
    public char store_and_fwd_flag = '-';
    public int payment_type = -1;

    public TripRecord(Object[] headers, String[] fields, String file, long linenumber) {
        try {


            for (int j = 0; j < Math.min(headers.length, fields.length); j++) {
                if (fields[j].equals("")) {
                    continue;
                }
                String s = (String) headers[j];
                if (s.contains("vendor")) {
                    vendorID = fields[j];
                } else if (s.contains("pickup_datetime")) {
                    if (fields[j].contains("/")) {
                        pickup_datetime = yellowDateFormat2.parse(fields[j]).getTime();
                    } else {
                        pickup_datetime = yellowDateFormat.parse(fields[j]).getTime();
                    }
                } else if (s.contains("dropoff_datetime")) {
                    if (fields[j].contains("/")) {
                        dropoff_datetime = yellowDateFormat2.parse(fields[j]).getTime();
                    } else {
                        dropoff_datetime = yellowDateFormat.parse(fields[j]).getTime();
                    }
                } else if (s.contains("passenger")) {
                    passenger_count = Integer.parseInt(fields[j]);
                } else if (s.contains("distance")) {
                    trip_distance = Double.parseDouble(fields[j]);
                } else if (s.contains("pickup_lon") || s.contains("start_lon")) {
                    pickup_longitude = Double.parseDouble(fields[j]);
                } else if (s.contains("pickup_lat") || s.contains("start_lat")) {
                    pickup_latitude = Double.parseDouble(fields[j]);
                } else if (s.contains("dropoff_long") || s.contains("end_lon")) {
                    dropoff_longitude = Double.parseDouble(fields[j]);
                } else if (s.contains("dropoff_latitude") || s.contains("end_lat")) {
                    dropoff_latitude = Double.parseDouble(fields[j]);
                } else if (s.contains("fare")) {
                    fare = Double.parseDouble(fields[j]);
                } else if (s.contains("extra")) {
                    extra = Double.parseDouble(fields[j]);
                } else if (s.contains("tax")) {
                    tax = Double.parseDouble(fields[j]);
                } else if (s.contains("tip")) {
                    tip = Double.parseDouble(fields[j]);
                } else if (s.contains("tolls")) {
                    tolls = Double.parseDouble(fields[j]);
                } else if (s.contains("improvement") || s.contains("surcharge")) {
                    improvement = Double.parseDouble(fields[j]);
                } else if (s.contains("total")) {
                    total = Double.parseDouble(fields[j]);
                } else if (s.contains("rate")) {
                    RatecodeID = Integer.parseInt(fields[j]);
                } else if (s.contains("fwd_flag") || s.contains("store") || s.contains("forward")) {
                    store_and_fwd_flag = fields[j].charAt(0);
                } else if (s.contains("payment")) {
                    payment_type = getPaymentType(fields[j]);
                } else if (s.contains("pulocationid")) {
                    pulocationid = Integer.parseInt(fields[j]);
                } else if (s.contains("dolocationid")) {
                    dolocationid = Integer.parseInt(fields[j]);
                } else {
                    throw new RuntimeException("Unknown field: " + s);
                }
            }
        } catch (Exception ex) {
            Logger.printErr(file + "," + linenumber + "," + ex.getMessage());
        }
    }

    public static int PAYMENT_CREDIT_CARD = 1;
    public static int PAYMENT_CASH = 2;
    public static int PAYMENT_NO_CHARGE = 3;
    public static int PAYMENT_DISPUTE = 4;
    public static int PAYMENT_UNKNOWN = 5;
    public static int PAYMENT_VOIDED_TRIP = 6;


    private static int getPaymentType(String field) {
        String s = field.toLowerCase().trim();
        if (s.contains("csh") || s.contains("cas") || s.equals("1")) {
            return PAYMENT_CASH;
        } else if (s.contains("crd") || s.contains("cre") || s.equals("2")) {
            return PAYMENT_CREDIT_CARD;
        } else if (s.contains("unk") || s.contains("unknown") || s.contains("na") || s.equals("3")) {
            return PAYMENT_UNKNOWN;
        } else if (s.contains("dis") || s.contains("dispute") || s.equals("4")) {
            return PAYMENT_DISPUTE;
        } else if (s.contains("no") || s.equals("5")) {
            return PAYMENT_NO_CHARGE;
        } else if (s.contains("void") || s.equals("6")) {
            return PAYMENT_VOIDED_TRIP;
        }
        throw new RuntimeException("Unknown payment type: " + field);
    }

    public Node getNode(Graph graph) {
        Node record = graph.newNode(0, pickup_datetime);
        if (dropoff_datetime != 0) {
            record.set("dropoff_datetime", Type.LONG, dropoff_datetime);
        }
        if (passenger_count != 0) {
            record.set("passenger_count", Type.INT, passenger_count);
        }

        if (trip_distance != -1) {
            record.set("trip_distance", Type.DOUBLE, trip_distance);
        }
        if (pickup_longitude != -400) {
            record.set("pickup_longitude", Type.DOUBLE, pickup_longitude);
        }
        if (pickup_latitude != -400) {
            record.set("pickup_latitude", Type.DOUBLE, pickup_latitude);
        }
        if (dropoff_longitude != -400) {
            record.set("dropoff_longitude", Type.DOUBLE, dropoff_longitude);
        }
        if (dropoff_latitude != -400) {
            record.set("dropoff_latitude", Type.DOUBLE, dropoff_latitude);
        }
        if (fare != 0) {
            record.set("fare", Type.DOUBLE, fare);
        }
        if (extra != 0) {
            record.set("extra", Type.DOUBLE, extra);
        }
        if (tax != 0) {
            record.set("tax", Type.DOUBLE, tax);
        }
        if (tip != 0) {
            record.set("tip", Type.DOUBLE, tip);
        }
        if (tolls != 0) {
            record.set("tolls", Type.DOUBLE, tolls);
        }
        if (improvement != 0) {
            record.set("improvement", Type.DOUBLE, improvement);
        }
        if (total != 0) {
            record.set("total", Type.DOUBLE, total);
        }

        if (RatecodeID != -1) {
            record.set("RatecodeID", Type.INT, RatecodeID);
        }
        if (store_and_fwd_flag != '-') {
            record.set("store_and_fwd_flag", Type.INT, (int) store_and_fwd_flag);
        }
        if (payment_type != -1) {
            record.set("payment_type", Type.INT, payment_type);
        }
        if (pulocationid != -1) {
            record.set("pulocationid", Type.INT, payment_type);
        }
        if (dolocationid != -1) {
            record.set("dolocationid", Type.INT, payment_type);
        }
        return record;
    }
}