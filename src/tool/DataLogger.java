package tool;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
/**
 * Class to log entries for future reference.
 * Based on a text file.
 * Utilized by other methods for notable occurrences in the program.
 * @author Steven Fitzpatrick
 * @dateEdited 8-31-2015
 * @version 1.2
 *
 */

public class DataLogger {
	Utilities utils;
	
	/**
	 * Create a new data logger by finding the resource file.
	 * @param path relative file path to the logger.
	 */
	public DataLogger(Utilities utils){
		this.utils = utils;
	}
	
	protected boolean newEntry(String user, String action){
		try {
			PrintWriter write = new PrintWriter(new FileOutputStream(utils.getFile("log"), true));
			write.append(time() + " " + user + ":" + action + "\n");
			write.flush();
			write.close();
		} catch (Exception e) {
			System.err.println("Logger failed.");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	protected String out(){
		return this.out(false);
	}
	protected String out(boolean onlyNotes){
		
		String output = "";
		if(onlyNotes){
			output += "Printing notes:\n";
		}
		Scanner reader;
		try {
			reader = new Scanner(utils.getFile("log"));
			while(reader.hasNext()){
				String currentOut = reader.nextLine() + "\n";
				if(onlyNotes){
					if(!currentOut.contains("Note:")){
						currentOut = "";
					}
				}
				output+=currentOut;
			}
			reader.close();
		} catch (Exception e) {
			System.err.println("Logger failed to output.");
			output = "Logger Failure.";
			e.printStackTrace();
		}
		return output;
	}
	
	private String time(){
		DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
		
	}
}
