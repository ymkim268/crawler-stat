import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CSV {
	
	FileWriter fileWriter;
	int rowCount;
	
	public CSV(String fileName) throws IOException {
		File file = new File(fileName);
		fileWriter = new FileWriter(file.getAbsoluteFile(), true);
		rowCount = 0;
	}
	
	public void appendRow(String str) throws IOException {
		synchronized(this) {
			fileWriter.append(str);
			fileWriter.append(",");
		}	
	}
	
	public void endRow() throws IOException {
		synchronized(this) {
			fileWriter.append('\n');
			rowCount++;
			fileWriter.flush();
		}
	}
	
	public int close() throws IOException {
		fileWriter.close();
		return rowCount;
	}
	
}
