import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

class CloneMappingError extends Exception {
    public CloneMappingError(String message) {
        super(message);
    }
}

public class CloneMapper {

        private static final int BLOCK_SIZE = 500_000;
        private static final int THREAD_COUNT = 8;
        

        public static class Clone {

            private String directory = "";
            private String filename1 = "";
            private int startLine1 = -1;
            private int endLine1 = -1;
            private String directory2 = "";
            private String filename2 = "";
            private int startLine2 = -1;
            private int endLine2 = -1;
        
            // Konstruktor
            public Clone(String directory, String filename1, int startLine1, int endLine1,String directory2, String filename2, int startLine2, int endLine2) {
                this.directory = directory;
                this.filename1 = filename1;
                this.startLine1 = startLine1;
                this.endLine1 = endLine1;
                this.directory2 = directory2;
                this.filename2 = filename2;
                this.startLine2 = startLine2;
                this.endLine2 = endLine2;
            }
        
            public String getFirstClone(){
                return directory+","+filename1;
            }
            
            public String getSecondClone(){
                return directory2+","+filename2;
            }

            public String getDir(){
                return directory;
            }

            public String getDir2(){
                return directory2;
            }
            public String getFilename1() {
                return filename1;
            }
            public int getStartLine1() {
                return startLine1;
            }
            public int getEndLine1() {
                return endLine1;
            }
            public String getFilename2() {
                return filename2;
            }
            public int getStartLine2() {
                return startLine2;
            }
            public int getEndLine2() {
                return endLine2;
            }

            // toString-Methode
            @Override
            public String toString() {
                return "Clone{" +
                        "directory='" + directory + '\'' +
                        ", filename1='" + filename1 + '\'' +
                        ", startLine1=" + startLine1 +
                        ", endLine1=" + endLine1 +
                        ", filename2='" + filename2 + '\'' +
                        ", startLine2=" + startLine2 +
                        ", endLine2=" + endLine2 +
                        '}';
            }
        }
    
    /** 
     * Reads content from a file
     * @param filePath 
     * @return List<String> containing all line of the file 
     * @throws IOException
     */
    public static List<String> getFileContent(String filePath) throws IOException {
        return Files.readAllLines(Paths.get(filePath));
    }

    // /** 
    //  * Reads content from larger files (100MB+), is used in blocks BLOCK_SIZE
    //  * @param filePath
    //  * @param blockNumber current block
    //  * @return List<String> with BLOCK_SIZE lines from the file 
    //  * @throws IOException
    //  */
    // public static List<String> getLargeFileContent(String filePath, int blockNumber ) throws IOException {
    //     BufferedReader reader = new BufferedReader(new FileReader(filePath));
    //     List<String> output = new ArrayList<>();
    //     int startLine = blockNumber * BLOCK_SIZE;
    //     int currentLine = -1;
    //     String line;
    //     // Überspringe Zeilen bis zur gewünschten Block-Startzeile
    //     while ((line = reader.readLine()) != null && currentLine < startLine) {
    //         currentLine++;
    //     }
    //     // Lese den aktuellen Block
    //     int linesRead = 0;
    //     while (line != null && linesRead < BLOCK_SIZE) {
    //         output.add(line);
    //         line = reader.readLine();
    //         linesRead++;
    //     }
    //     reader.close();
    //     return output;
    // }

    /** 
     * Puts inputs and mappings in the same format for easier compare
     * @param inputs content from the mapper file
     * @param mappings content from the clones found by BCE
     * @return List<String> with in findClones() found clones
     */
    public static List<String> mapOutput(List<String> inputs, List<String> mappings) {
        List<Clone> bce_clones = new ArrayList<>();
        HashMap<String, List<String>> mapper_input = new HashMap<>();
        for(String i: mappings){
            String[] temp = i.split(",");
            if(temp.length == 8){
                bce_clones.add(new Clone( temp[0],temp[1],Integer.parseInt(temp[2]),Integer.parseInt(temp[3]),temp[4],temp[5],Integer.parseInt(temp[6]),Integer.parseInt(temp[7])));
            }
        }
        for(String i: inputs){
            String[] temp = i.split(",");
            if(temp.length == 7){
                String first = temp[0] + "," + temp[6]+ ".jimple";
                String second = temp[1] + "," + temp[2]+ "," + temp[3] + "," + temp[4] + "," + temp[5];
                if(mapper_input.containsKey(first)){
                    mapper_input.get(first).add(second);
                }else{ 
                    List<String> temp2 = new ArrayList<>();
                    temp2.add(second);
                    mapper_input.put(first, temp2); 
                }
            }

        }
        return findClones(bce_clones, mapper_input);
    }

    /** 
     * Works through BCE found clones (BLOCK_SIZE) and filters out the ones which are also found in the mapperfile
     * @param bce_clones BLOCK_SIZE of BCE found clones
     * @param mapper_input full mapper 
     * @return List<String> of all clones
     */
    public static List<String> findClones(List<Clone> bce_clones, HashMap<String, List<String>> mapper_input ){
        List<String> output = new ArrayList<>();
        for(Clone bce: bce_clones){
            String clone1 = "";
            String clone2 = "";
            String[] value1 = new String[1];
            String[] value2 = new String[1];
            if(mapper_input.containsKey(bce.getFirstClone()) && mapper_input.containsKey(bce.getSecondClone())){
                for(String i: mapper_input.get(bce.getFirstClone())){
                    value1 = i.split(",");
                    clone1 = cloneTolerance(bce.getStartLine1(),bce.getEndLine1(), value1);
                    if(clone1 != null){
                        break;
                    }
                }
                for(String k: mapper_input.get(bce.getSecondClone())){
                    value2 = k.split(",");
                    clone2 = cloneTolerance(bce.getStartLine2(),bce.getEndLine2(), value2);
                    if(clone2 != null){
                        break;
                    }
                }
                if(clone1 != null && clone2 != null){
                    output.add(bce.getDir() + "," + value1[0] + ","+ clone1 + "," + bce.getDir2() + "," + value2[0] + "," + clone2);
                }
            }
        }
        return output;

    }

    /**
     * Checks if a clones Start and Endline are within boundaries
     * @param start mapper startline
     * @param end mapper endline
     * @param value BCE start- and endline
     * @return either the BCE start- and endline or null 
     */
    public static String cloneTolerance(int start, int end, String[] value){
        int jimpleStart = Integer.parseInt(value[3]);
        int jimpleEnd = Integer.parseInt(value[4]);
        if((((start - 2) <= jimpleStart) && ((start + 2)>= jimpleStart)) && (((end - 2 ) <= jimpleEnd) && ((end +2 ) >= jimpleEnd) ) ){
            return value[1]+","+value[2];
        }else{
            return null;
        }

    }

    // public static void writeOutputToCsv(List<String> outputs, BufferedWriter writer) throws IOException {
    //     for (String output : outputs) {
    //         writer.write(output);
    //         writer.newLine();
    //     }
    //     writer.flush();
    // }

    /**
     * Reads fileinput and starts threads for simultaneously work 
     * @param mapper_filepath 
     * @param bce_filepath
     * @param output_filepath
     * @throws Exception
     */
    public static void ReadAndWork_BCE(Path mapper_filepath, Path bce_filepath, Path output_filepath) throws Exception{

        List<String> dataFile1 = Files.readAllLines(mapper_filepath);
        BufferedWriter writer = Files.newBufferedWriter(output_filepath);
        Object writeLock = new Object();
        BlockingQueue<List<String>> workQueue = new ArrayBlockingQueue<>(THREAD_COUNT);

        // progress counter
        AtomicInteger blockCounter = new AtomicInteger(0);
        AtomicInteger totalLineCounter = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    while (true) {
                        List<String> block = workQueue.take();
                        if (block.isEmpty()) break;

                        List<String> result = mapOutput(dataFile1, block);

                        synchronized (writeLock) {
                            for (String line : result) {
                                writer.write(line);
                                writer.newLine();
                            }
                        }

                        int blockNum = blockCounter.incrementAndGet();
                        int totalLines = totalLineCounter.addAndGet(block.size());
                        System.out.printf(" Block %d verarbeitet (%d Gesamtzeilen)%n", blockNum, totalLines);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        try (BufferedReader reader = Files.newBufferedReader(bce_filepath)) {
            List<String> buffer = new ArrayList<>(BLOCK_SIZE);
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.add(line);
                if (buffer.size() == BLOCK_SIZE) {
                    workQueue.put(new ArrayList<>(buffer));
                    buffer.clear();
                }
            }
            if (!buffer.isEmpty()) {
                workQueue.put(new ArrayList<>(buffer));
            }
            for (int i = 0; i < THREAD_COUNT; i++) {
                workQueue.put(Collections.emptyList());
            }

            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.HOURS);
        }

        writer.close();
        System.out.println("Verarbeitung vollständig abgeschlossen.");
    }

    public static void main(String[] args) throws Exception{
        if (args.length < 3) {
            System.out.println("Usage: java CloneMapper <input_file> <mapping_file> <output_file>");
            return;
        }
        try{
            Path mapper_filepath = Paths.get(args[0]);
            Path bce_filePath = Paths.get(args[1]);
            Path output_filePath = Paths.get(args[2]);

            ReadAndWork_BCE(mapper_filepath,bce_filePath , output_filePath);
        }catch (Exception e){
            e.printStackTrace();
            System.exit(0);
        } 
    }   
}



