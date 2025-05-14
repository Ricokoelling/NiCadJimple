import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class StreamedCSVProcessor {

    private static final int THREAD_COUNT = 8;
    private static final int BLOCK_SIZE = 500_000;

    public static void main(String[] args) throws Exception {
        Path inputPath1 = Paths.get("datei1.csv");
        Path inputPath2 = Paths.get("datei2.csv");
        Path outputPath = Paths.get("output.csv");

        // Datei 1 einlesen
        List<String> dataFile1 = Files.readAllLines(inputPath1);

        // Output Writer mit Synchronisierung
        BufferedWriter writer = Files.newBufferedWriter(outputPath);
        Object writeLock = new Object();

        // BlockingQueue zur Kommunikation
        BlockingQueue<List<String>> workQueue = new ArrayBlockingQueue<>(THREAD_COUNT);

        // Fortschrittszähler
        AtomicInteger blockCounter = new AtomicInteger(0);
        AtomicInteger totalLineCounter = new AtomicInteger(0);

        // Worker-Threads starten
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    while (true) {
                        List<String> block = workQueue.take();
                        if (block.isEmpty()) break; // Stoppsignal

                        List<String> result = processBlock(block, dataFile1);

                        synchronized (writeLock) {
                            for (String line : result) {
                                writer.write(line);
                                writer.newLine();
                            }
                        }

                        int blockNum = blockCounter.incrementAndGet();
                        int totalLines = totalLineCounter.addAndGet(block.size());
                        System.out.printf("✔ Block %d verarbeitet (%d Gesamtzeilen)%n", blockNum, totalLines);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        // Reader für Datei 2
        try (BufferedReader reader = Files.newBufferedReader(inputPath2)) {
            // Header einmalig schreiben
            String header = reader.readLine();
            if (header == null) {
                System.out.println("Datei2 ist leer.");
                return;
            }
            synchronized (writeLock) {
                writer.write(header);
                writer.newLine();
            }

            // Datei streamend blockweise einlesen
            List<String> buffer = new ArrayList<>(BLOCK_SIZE);
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.add(line);
                if (buffer.size() == BLOCK_SIZE) {
                    workQueue.put(new ArrayList<>(buffer));
                    buffer.clear();
                }
            }

            // letzten Block senden, falls nicht leer
            if (!buffer.isEmpty()) {
                workQueue.put(new ArrayList<>(buffer));
            }

            // Threads beenden (leerer Block = Stoppsignal)
            for (int i = 0; i < THREAD_COUNT; i++) {
                workQueue.put(Collections.emptyList());
            }

            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.HOURS);
        }

        writer.close();
        System.out.println("🎉 Verarbeitung vollständig abgeschlossen.");
    }

    private static List<String> processBlock(List<String> block, List<String> dataFile1) {
        List<String> result = new ArrayList<>(block.size());

        for (String line : block) {
            // Beispiel: füge Länge der ersten Zeile von Datei1 an
            result.add(line + "," + dataFile1.get(0).length());
        }

        return result;
    }
}
