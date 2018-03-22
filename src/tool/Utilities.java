package tool;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * The MAIN for the program is in this class.
 * Starts the program.
 * @author Steven Fitzpatrick
 * @dateEdited 11-9-2017
 * @version 2.0
 */
public class Utilities {
	private User user;
	private Scanner reader;
	
	private HashMap<String,String> filePaths;
	
	
	public Utilities(User user){
		this.user=user;
		reader = new Scanner(System.in);
		filePaths = new HashMap<String,String>();
		filePaths.put("log","src/resources/log.txt");
		filePaths.put("data","src/resources/data.xls");
		filePaths.put("info","src/resources/info.txt");
		filePaths.put("settings","src/resources/settings.txt");
		filePaths.put("backups","src/resources/backups/");

	}
	
	/**
	 * Next Line on the Scanner
	 * @return String of the next Line
	 */
	protected String next(){
		return reader.nextLine();
	}
	
	/**
	 * Open a file using the OS
	 * This file could be a stored file from the resources (or settings).
	 * @param fileName the short name of the file
	 */
	protected void open(String fileName){
		try {
			Desktop.getDesktop().open(getFile(fileName));
			user.out("Opened file: " + fileName);
			user.log.newEntry(user.user, "Opened file "+ fileName);
			close();
			user.gui.dispose();
		} catch (IOException e) {
			user.log.newEntry(user.user, "ERROR: Opening file " + fileName);
			user.out("Failed to open file.");
		}
	}
	
	/**
	 * Check the file resources (ie. data.xls and log.txt)
	 * @dateEdited 2-23-2016
	 * @return true if files are in order
	 */
	public boolean checkResources(){
		if(getFile("log") != null && getFile("data") != null && getFile("info") !=null){
			System.out.println("All files accepted.");
			try {
				settings();
			} catch (Exception e) {
				System.out.println("Unable to update settings.");
			}
			return true;
		} else{
			System.out.println("Unable to collect all resources.\nClosing program.");
			return false;
		}
	}
	
	/**
	 * Get A file. If a file is unaccessible. Access it through JFileChooser.
	 * @param filePaths list of file paths
	 * @param fileNumber File Number
	 * @return the pertinent file
	 */
	protected File getFile(String fileName){
		File currentFile = new File(filePaths.get(fileName));
		return checkFile(currentFile, fileName);
		
	}
	
	private File checkFile(File file, String fileName){
		if(!file.exists()){
			fileError(fileName);
			JFileChooser fc = new JFileChooser();
			if(fc.showOpenDialog(new JFrame()) == JFileChooser.APPROVE_OPTION){
				filePaths.put(fileName, fc.getSelectedFile().getPath());
				return fc.getSelectedFile();
			} else{
				return null;
			}
		}
		return file;
	}

	/**
	 * Helper method to send out error messages to System.out for 
	 * File related errors
	 * @param fileNumber file number that produced the error.
	 */
	private void fileError(String fileName) {
		System.err.println("An error has occurred in loading a critical resource: " + fileName +" Please use the File Chooser to find this file.");
	}

	/**
	 * Helper to close out of the program.
	 * Called at the termination of a session.
	 * Makes a new log entry about logging off, and closes out of any ExcelReader resources.
	 */
	public void close() {
		reader.close();
		if(user.gui != null){
			System.out.println(user.gui.getConsoleText());
		}
		user.log.newEntry(user.user, " logged out.");
	}

	/**
	 * Helper method to give all of the information about the program that is stored in info.txt
	 * @return contents of info.txt
	 * @throws IOException 
	 */
	public String about() {
		try{
			StringBuffer about = infoPage();
			String categorizationInfo = numberOfCategorizations();
			about.append('\n' + categorizationInfo);
			return about.toString();
		} catch(Exception e){
			exceptionHandler(e, "Unable to open about page.");
			return "";
		}
	}
	
	private String numberOfCategorizations() throws FileNotFoundException, IOException {
			HSSFWorkbook data = new HSSFWorkbook(new FileInputStream(getFile("data")));
			int numSheets = data.getNumberOfSheets()-1;
			int numItems = user.resources.currentData.size();
		return "Total Number of data.xls pages (versions): " + numSheets + '\n' +"Current number of categorized items: " + numItems;
	}

	private StringBuffer infoPage() throws FileNotFoundException{
		StringBuffer str = new StringBuffer();
		File aboutPage = getFile("info");
		Scanner scr = new Scanner(new FileInputStream(aboutPage));
		while(scr.hasNext()){
			str.append(scr.nextLine() + '\n');
		}
		scr.close();
		return str;
	}
	
	private void settings() throws FileNotFoundException{
		File settings = getFile("settings");
		Scanner scr = new Scanner(new FileInputStream(settings));
		if(scr.nextLine().equals("Settings")){
			scr.nextLine();
			ExcelReader.HISTORICAL_DATA = getLastChar(scr.nextLine()) =='t';
			ExcelReader.HISTORICAL_TRUE = scr.nextLine().split(": ")[1];
			ExcelReader.ITEM_NAME = getNum(scr.nextLine());
			ExcelReader.RCV_UNIT = getNum(scr.nextLine());
			ExcelReader.VENDOR = getNum(scr.nextLine());
			ExcelReader.QUANTITY = getNum(scr.nextLine());
			ExcelReader.PRICE = getNum(scr.nextLine());
			ExcelReader.READ_FAO = getNum(scr.nextLine());
			ExcelReader.READ_WEIGHT_PER_UNIT = getNum(scr.nextLine());
			ExcelReader.READ_WEIGHT_UNIT = getNum(scr.nextLine());
			ExcelReader.WRITE_FAO = getNum(scr.nextLine());
			ExcelReader.WRITE_WEIGHT_PER_UNIT = getNum(scr.nextLine());
			ExcelReader.WRITE_WEIGHT_UNIT = getNum(scr.nextLine());
			ExcelReader.COST = getNum(scr.nextLine());
			if(scr.hasNext()){
				if(scr.nextLine().equals("files:")){
					while(scr.hasNext()){
						addFileFromSettings(scr.nextLine());
					}
				}
			}
			
			System.out.println("All settings updated.");
		} else{
			System.out.println("Wrong settings file.");
		}
		scr.close();
	}

	private void addFileFromSettings(String input) {
		String[] fileInfo = input.split(": ");
		File file = new File(fileInfo[1]);
		if(checkFile(file,fileInfo[0])!=null){
			filePaths.put(fileInfo[0],fileInfo[1]);
		}
	}

	private char getLastChar(String nextLine) {
		return nextLine.charAt(nextLine.length()-1);
	}
	
	private int getNum(String nextLine){
		return Integer.parseInt(nextLine.split(": ")[1]);
	}

	public void test() {
		user.out("No current tests in progress.");
	}

	public String getFiscalYear() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy");
		Date date = new Date();
		int year = Integer.parseInt(dateFormat.format(date).substring(2))-1;
		String fiscalYear = "FY"+ year;
		return fiscalYear;
	}
	
	public void exceptionHandler(Exception e,String message){
		user.log.newEntry(user.user, "Error: " + e + " " + message);
		user.out(message);
	}
}
