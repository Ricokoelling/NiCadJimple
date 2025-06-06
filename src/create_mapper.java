import java.util.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;


public class create_mapper {

	public static class credentials{
		private String Path = "";
		private String preFileName = "";
		private String className = "";

		public credentials(String Path, String preFileName,String className ){
			this.Path = Path;
			this.preFileName = preFileName;
			this.className = className;
		}
		@Override
		public boolean equals(Object obj){
			if(this == obj) return true;
			if (obj == null || getClass() != obj.getClass()) return false;
			credentials cred = (credentials) obj;
			return Objects.equals(cred.Path, Path) && Objects.equals(cred.className, className) && Objects.equals(cred.preFileName, preFileName);
		}

	 	// hashCode() überschreiben
	 	@Override
	 	public int hashCode() {
			return Objects.hash(Path, className, preFileName);
	 	}

		@Override
		public String toString(){
			return "Path: " + this.Path + ", preFileName: " + this.preFileName + ", classname: " + this.className;
		}
	}	

	public static class identifier{
		String functionName = "";
		int startLine = -1;
		int endLine = -1;

		public identifier(String functionName, int startLine, int endLine){
			this.functionName = functionName;
			this.startLine = startLine;
			this.endLine = endLine;
		}
		public String getFunctionName(){
			return this.functionName;
		}
		public Integer getStartLine(){
			return this.startLine;
		}

		public Integer getEndLine(){
			return this.endLine;
		}

		@Override
		public String toString(){
			return "funcName: " + this.functionName + ", startLine: " + this.startLine + ", endLine: " + this.endLine;
		}
	}


	public static void main(String[] args) {
		final String TARGET_FOLDER= args[0];
		final String JAVA_MAPPER = args[1];
		final String OUTPUT_FILE = args[2];

		HashMap<credentials, List<identifier>> files = new HashMap<credentials, List<identifier>>();
		files = handleInputData(JAVA_MAPPER);
		File jimpleFiles = new File(TARGET_FOLDER);
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILE, false))){
			loopFiles(jimpleFiles, files, writer);
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loops through files and folders for files found by ByteCodeInfos
	 * @param dir
	 * @param javaFiles
	 * @param writer
	 * 
	 */
	public static void loopFiles(File dir, HashMap<credentials, List<identifier>> javaFiles, BufferedWriter writer) {
			if (dir.isDirectory()) {
				File[] files = dir.listFiles();
				if (files != null) {
						for (File file : files) {
								if (file.isDirectory()) {
									loopFiles(file, javaFiles, writer);
								} else {
										handeFiles(file, javaFiles, writer);
								}
						}
				}
			}
	}

	/**
	 * Loops through ByteCodeInfos output and maps them to credentials and identifier 
	 * @param filePath
	 * @return Hashmap containing all infos about a function in the original files
	 */
	public static HashMap<credentials, List<identifier>> handleInputData(String filePath){
		HashMap<credentials, List<identifier>> files = new HashMap<credentials, List<identifier>>();
		try(BufferedReader reader = new BufferedReader(new FileReader(filePath));){

			String line;
			while((line = reader.readLine()) != null){
				String[] parts = line.split("!");
				credentials cred = new credentials(parts[0], parts[1], parts[2]);
				identifier id = new identifier(parts[3], Integer.parseInt(parts[4]), Integer.parseInt(parts[5]));
				//System.out.println(id.toString());
				if(files.containsKey(cred)){
					files.get(cred).add(id);
				}else{
					files.put(cred, new ArrayList<>(Arrays.asList(id)));
				}
			}
		}catch (IOException e) {
            e.printStackTrace();
        }

		return files;
	}

	/**
	 * Works through jimple files to find start- and endline of all suitable functions
	 * suitable functions are all functions which are over 15 total lines 
	 * @param file jimple file
	 * @param files Hashmap containing all infos about a function in the original files 
	 * @param writer
	 */
	public static void handeFiles(File file, HashMap<credentials, List<identifier>> files, BufferedWriter writer){
		try (BufferedReader reader = new BufferedReader(new FileReader(file))){
			String line, preFileName = "", className="";
			String[] splittedPath =  file.getAbsolutePath().split("/");
			String filePath = splittedPath[splittedPath.length-3]+"/"+splittedPath[splittedPath.length-2];
			int index = file.getName().indexOf('_');
			if(index != -1){
				preFileName = file.getName().substring(0,index);
				className = file.getName().substring(index+1);
				className = className.replace(".jimple", "");
			}else{
				System.exit(0);
				System.err.println("NO UNDERSCORE");
			}
			credentials cred = new credentials(filePath, preFileName, className);

			int count = 0;
			boolean counts = false;
			int startLine = -1;
			int endLine = -1;
			int currentLine = 0;
			int MAXLINES =(int) Files.lines(Path.of(file.getAbsolutePath())).count();  

			if(files.containsKey(cred)){
				List<identifier> id = files.get(cred);
				for(identifier k: id){
				}

				while((line = reader.readLine()) != null){
					if(removeSpaces(line).equals(removeSpaces(id.get(count).getFunctionName()))){
						if((id.get(count).getEndLine() - id.get(count).getStartLine())+1 < 15){
							if(counts){
								endLine = currentLine - 1;
								counts = false;
								String writeString = splittedPath[splittedPath.length-2] + "," 
																		+preFileName+".java" + "," 
																		+id.get(count-1).getStartLine()+","
																		+id.get(count-1).getEndLine()+","
																		+(startLine+1)+","
																		+endLine+","
																		+file.getName().replace(".jimple", "");
								writer.write(writeString);
								writer.newLine();
								writer.flush();
							}
						}else{
							if(counts){
								endLine = currentLine - 1;
								String writeString = splittedPath[splittedPath.length-2] + "," 
																		+preFileName+".java" + "," 
																		+id.get(count-1).getStartLine()+","
																		+id.get(count-1).getEndLine()+","
																		+(startLine+1)+","
																		+endLine+","
																		+file.getName().replace(".jimple", "");
								writer.write(writeString);
								writer.newLine();
								writer.flush();
								startLine = currentLine;
							}else{
								counts = true;
								startLine = currentLine;
							}

						}
						count++;
						if(count >= id.size()){
							break;
						}
					}
					currentLine++;
				}
				if(counts){
					endLine = MAXLINES - 1;
					String writeString = splittedPath[splittedPath.length-2] + "," 
															+preFileName+".java" + "," 
															+id.get(count-1).getStartLine()+","
															+id.get(count-1).getEndLine()+","
															+startLine+","
															+endLine+","
															+file.getName().replace(".jimple", "");
					writer.write(writeString);
					writer.newLine();
					writer.flush();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 *  Removes all space and tabs in contained in the jimple function name  
	 * @param str
	 * @return string without tabs and spaces
	 */
	public static String removeSpaces(String str) {
		return str.replaceAll("\\s+", "");  
	}

}
