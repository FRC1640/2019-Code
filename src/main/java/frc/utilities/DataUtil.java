package frc.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.io.PrintWriter;

public class DataUtil {

    private PrintWriter p;
    ArrayDeque<CvtData> queue = new ArrayDeque<>();

    public DataUtil(File file) throws FileNotFoundException {

        PrintWriter p = new PrintWriter(file);
        p.println("timestamp, wheel counts, wheel rate, servo angle, neo speed, set speed");

        new Thread(() -> {

            while (true) {

                int size;
                synchronized (queue) {

                    size = queue.size();
                    size = Math.min(size, 15);

                }






                for (int i = 0; i < size; i++) {

                    // maybe not double
                    CvtData val;

                    synchronized (queue) {

                        val = queue.poll();

                    }

                    p.format("%d, %f, %f, %f, %f, %f, \n", val.timeStamp, val.wheelCounts, val.wheelRate, val.servoAngle, val.neoSpeed, val.setSpeed);

                }

                p.flush();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        }).start();

    }

    public void addInput (CvtData data) {

        synchronized (queue) {

            queue.add(data);

        }

    }

}