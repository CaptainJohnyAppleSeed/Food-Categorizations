package tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * This class is designed to interact with Excel Files.
 * Does the body of all calculations for this program.
 * This class is organized into blocks:
 * 			1) Food Categorizations BLOCK 	-categorize a new fiscal year's data from the previous year's data
 * 			2) DATA.xls BLOCK				-Import new data to the data.xls file; import existing data.xls data
 * 			3) Helper Methods BLOCK			-Clean all of the data into a new data.xls tab; import row from data.xls
 * @author Steven Fitzpatrick
 * @dateEdited 8-31-2015
 * @version 1.2
 */

public class ExcelReader {
	protected static String HISTORICAL_TRUE = "t";
	protected TreeMap<String, FoodItem> currentData; //Full List of Food Items by Name
	protected User user; //Used for output
	
	protected static int ITEM_NAME = 0;
	protected static int RCV_UNIT = 1;
	protected static int VENDOR = 2;
	
	protected static int QUANTITY = 3;
	protected static int PRICE = 4;
	
	protected static int READ_FAO = 3;
	protected static int READ_WEIGHT_PER_UNIT = 7;
	protected static int READ_WEIGHT_UNIT = 8;
	
	protected static int WRITE_FAO = 4;
	protected static int WRITE_WEIGHT_PER_UNIT = 8;
	protected static int WRITE_WEIGHT_UNIT = 9;
	protected static int COST = 10;
	
	protected static boolean HISTORICAL_DATA = false;
	protected static int HEADER_ROW = 1;
	protected String[] header;
	
	/**
	 * Initialize this Excel Reader.
	 * @dateEdited 9-29-2015
	 * @author fitzpats
	 * @param user object used for text output.
	 * @throws IllegalArgumentException for any issues loading files. Used to kill the program.
	 */
	public ExcelReader(User user) throws IllegalArgumentException{
		user.out("Attempting to load critical resources...");
		currentData = new TreeMap<String,FoodItem>(); //Initialize the list
		this.user = user; //Store the path for output.
		try {
			importData(); //Imports data from data.xls and stores it in currentData HashMap
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to read 'ImportData()"); //Will Kill the program...
		}
		user.out("Successfully acquired and loaded Data resources.");
	}
	
	//Food Categorizations BLOCK--------------------------------------------------------------------------------
	/**
	 * Runs the Nitrogen-Footprint Program.
	 * Main Method for all of these calculations. Called from the GUI After user decides which file to use.
	 * @dateEdited 9-29-2015
	 * @author fitzpats
	 * @param file that contains the Food Purchase Information
	 * @throws IOException for problems interacting with the designated file.
	 */
	protected void runNewCategorization(File file) throws IOException{
		Categorize.runNewCategorization(file, this);
	}
	
	//DATA.xls BLOCK--------------------------------------------------------------------------------------------
	/**
	 * Method to read a new excel to append the data.xls. This method will add each Food Entry based on the import and export methods.
	 * FORMATING:	A			B			C		D		E		F		G		H		I				J	
	 * 				ItemName	RcvType		Vendor	QTY		FAO1	FAO2	FAO3	FAO4	WeightPerUnit	Unit Weight(Kg,lb,...)
	 * All String except for  Column I
	 * @dateEdited 10-12-2015
	 * @author fitzpats
	 * @param file to append to data.xls
	 * @throws IOException
	 */
	protected void importNewDataExcel(File sourceFile) throws IOException{
		HSSFWorkbook databook = this.loadBook(sourceFile);
		HSSFSheet dataSheet = databook.getSheetAt(0);
		ArrayList<FoodItem> newEntries = new ArrayList<FoodItem>();
		for(int currentRow = dataSheet.getFirstRowNum(); currentRow <= dataSheet.getLastRowNum(); currentRow++){ 
			try{
				FoodItem newEntry = importRow(dataSheet.getRow(currentRow));
				if(!currentData.containsKey(newEntry.identifier())){
					newEntries.add(newEntry);
					currentData.put(newEntry.identifier(), newEntry);
				} else{
					FoodItem previousItem = currentData.get(newEntry.identifier());
					if(!previousItem.equals(newEntry)){
						newEntry.addHistoricalData(previousItem.getHistorical(), previousItem.getRowNum());
						currentData.put(newEntry.identifier(),newEntry);
					}
				}
			} catch(Exception e){
				user.out("ERROR: " + e + " on " + currentRow);
			}
		}
		user.out("Total new Entries: " + newEntries.size());
		user.log.newEntry(user.user, " Imported new Data using file " + sourceFile.getName());
		cleanData(sourceFile);
		user.out("Operation Completed.");
	}
	
	/**
	 * Imports existing data.xls file to the list. File path based on dataFilePath.
	 * @dateEdited 10-12-2015
	 * @author fitzpats
	 * @throws IOException
	 */
	protected void importData() throws IOException{
		user.out("Attempting to load data.xls");
		HSSFWorkbook dataBook = loadBook("data");
		HSSFSheet dataSheet = dataBook.getSheetAt(dataBook.getNumberOfSheets()-1);
		user.out("Importing " + dataSheet.getSheetName());
		header = importHeaderRow(dataSheet);
		for(int currentRow = dataSheet.getFirstRowNum()+1; currentRow <= dataSheet.getLastRowNum(); currentRow++){ 
			try{
				FoodItem newItem = importRow(dataSheet.getRow(currentRow));
				currentData.put(newItem.identifier(), newItem);
			} catch(Exception e){
				user.utils.exceptionHandler(e, "Unable to import row " + currentRow);
			}
		}
		user.out("Successfully imported all data.");
	}
	
	//Helper Methods BLOCK---------------------------------------------------------------------------------------

	/**
	 * Helper method to make a new sheet in data.xls from the current list of information.
	 * @dateEdited 10-12-2015
	 * @author fitzpats
	 * @param newEntries
	 * @throws IOException
	 */
	protected void cleanData(File sourceFile) {
		try{
			ExcelWriter writer = new ExcelWriter(user, sourceFile, ExcelWriter.CLEAN_DATA);
			Set<String> keys = currentData.keySet();
			for(String current : keys){
				FoodItem currentItem = currentData.get(current);
				writer.writeToExcel(currentItem);
			}
			writer.save();
		} catch(Exception e){
			user.out("Unable to clean the data."); 
		}
	}
	
	/**
	 * Import the current row of data and return a FoodItem object
	 * @dateEdited 4-20-2016
	 * @author fitzpats
	 * @param row current Excel Row object that needs to be processed
	 * @return FoodItem representation of the row
	 */
	protected FoodItem importRow(HSSFRow row){
		String name = row.getCell(ExcelReader.ITEM_NAME).getStringCellValue().trim().toLowerCase(Locale.ENGLISH);
		String receiveUnit = row.getCell(ExcelReader.RCV_UNIT).getStringCellValue();
		String vendor = row.getCell(ExcelReader.VENDOR).getStringCellValue();
		double weightPerUnit=-5.0;
		if(row.getCell(ExcelReader.READ_WEIGHT_PER_UNIT) != null){
			weightPerUnit = row.getCell(ExcelReader.READ_WEIGHT_PER_UNIT).getNumericCellValue(); //weight per receive unit
		}
		String weightUnit = "";
		if(row.getCell(ExcelReader.READ_WEIGHT_UNIT) != null){
			weightUnit = row.getCell(ExcelReader.READ_WEIGHT_UNIT).getStringCellValue(); //weight per receive unit
		} 
		int fao = ExcelReader.READ_FAO;
		String faoName = row.getCell(fao).getStringCellValue();
		String[] faos = new String[4];
		while(fao<ExcelReader.READ_WEIGHT_PER_UNIT && !faoName.equals("")){
			faos[fao-ExcelReader.READ_FAO] =faoName;
			fao++;
			if(row.getCell(fao) != null){
				try{
					faoName = row.getCell(fao).getStringCellValue();
				} catch (Exception e){
					throw new IllegalArgumentException("Unable to process the FAO Cattegories of " + name);
				}
			} else {
				faoName = "";
			}
		}
		FoodItem item = new FoodItem(name, receiveUnit, vendor, faos, weightPerUnit, weightUnit);
		readHistoricalInfo(item,row);
		return item;
	}
	
	
	/**
	 * Add historical Information to the given Food Item, if any is available.
	 * @param item Food Item to have historical data added
	 * @param row current Excel Row that is being processed
	 */
	private void readHistoricalInfo(FoodItem item, HSSFRow row) {
		if(ExcelReader.HISTORICAL_DATA){
			HashMap<String,Boolean> historical = new HashMap<String,Boolean>();
			for(int i = ExcelReader.READ_WEIGHT_UNIT+1; i<header.length;i++){
				if(row.getCell(i) != null){
					historical.put(header[i], row.getCell(i).getStringCellValue().toLowerCase().trim().equals(ExcelReader.HISTORICAL_TRUE));
				}
			}
			item.addHistoricalData(historical, row.getRowNum());
		}
	}

	/**
	 * Get all of the uncategorized food items
	 */
	protected void uncategorizedItems(){
		File file = new File("uncategorized.xls");
		try{
			ExcelWriter writer = new ExcelWriter(user,file,ExcelWriter.CALCULATION);
			writer.newSheet("Uncategorized purchases");
			Set<String> keys = currentData.keySet();
			for(String current : keys){
				FoodItem currentItem = currentData.get(current);
				if(currentItem.uncategorized()){
					writer.writeToExcel(currentItem);
				}
			}
			writer.save();
		} catch(Exception e){
			user.utils.exceptionHandler(e, "could not make new uncategorized purchase list.");
		}
	}
	
	protected void mostFrequentItems(){
		File file = new File("mostFrequentItems.xls");
		ExcelWriter writer;
		try {
			writer = new ExcelWriter(user,file,ExcelWriter.CALCULATION);
			int totalNumYears = (header.length)-ExcelReader.READ_WEIGHT_PER_UNIT;
			ArrayList<ArrayList<FoodItem>> freqList = new ArrayList<ArrayList<FoodItem>>();
			for(int i = 0; i<totalNumYears;i++){
				freqList.add(new ArrayList<FoodItem>());
			}
			user.out("All Items sorted by frequency...");
			Set<String> keys = currentData.keySet();
			for(String current : keys){
				FoodItem currentItem = currentData.get(current);
				freqList.get(currentItem.getHistorical().size()).add(currentItem);
			}
			for(int i = freqList.size()-1;i>0; i--){
				if(freqList.get(i).size()>0){
					writer.newSheet("Frequency "+ i + "years");
					writer.writeHeader();
					ArrayList<FoodItem> curList = freqList.get(i);
					for(int j = 0;j<curList.size(); j++){
						writer.writeToExcel(curList.get(j));
					}
				}
			}
			writer.save();
		} catch (Exception e){
			user.utils.exceptionHandler(e, "Unable to make most frequent items list.");
		}
		
	}
	
	private String[] importHeaderRow(HSSFSheet dataSheet) {
		HSSFRow headerRow = dataSheet.getRow(ExcelReader.HEADER_ROW);
		if(headerRow != null){
			String[] header = new String[headerRow.getLastCellNum()];
			for(int i = 0; i<headerRow.getLastCellNum();i++){
				if(headerRow.getCell(i) != null){
					header[i] = headerRow.getCell(i).getStringCellValue();
				} else{
					header[i] = "";
				}
			}
			return header;
		}
		return null;
	}

	/**
	 * Helper method to load the excel sheet from a file
	 * @dateEdited 10-12-2015
	 * @author fitzpats
	 * @param file excel file to be opened for reading purposes
	 * @return a HSSF WorkSheet
	 * @throws IOException
	 */
	protected HSSFWorkbook loadBook(Object inputFile) throws IOException{
		FileInputStream dataExcel;
		if(inputFile instanceof File){
			dataExcel = new FileInputStream((File)inputFile);
		} else if (inputFile instanceof String){
			String fileName = (String) inputFile;
			if(fileName.equals("data")){
				dataExcel = new FileInputStream(user.utils.getFile(fileName));
			} else{
				dataExcel = new FileInputStream((String) inputFile);
			}
		} else{
			throw new IllegalArgumentException("Input given can not be loaded as a file (Not a String or File)");
		}
		return new HSSFWorkbook(dataExcel);
	}
	
	protected String print(){
		StringBuffer output = new StringBuffer();
		Set<String> keys = currentData.keySet();
		for(String current : keys){
			output.append(currentData.get(current).toString() + '\n');
		}
		return output.toString();
	}
	
	protected FoodItem getItem(String identifier){
		return currentData.get(identifier);
	}

	public String getItemsFromFY(String fiscalYear){
		StringBuffer str = new StringBuffer();
		Set<String> keys = currentData.keySet();
		String fy = fiscalYear.trim().toUpperCase();
		for(String key:keys){
			if(currentData.get(key).itemPartOfFY(fy)){
				str.append("Item name: " + currentData.get(key).getItemName() +" Historical Frequency: " + currentData.get(key).getFrequencyOfItem()+"\n");
			}
		}
		return str.toString();
	}

	public void remove(String input) {
		String[] elements = input.split(" ");
		File file = user.utils.getFile(elements[1]);
		ExcelWriter writer;
		try {
			writer = new ExcelWriter(user, file, ExcelWriter.REMOVE);
			for(int i = 2; i<elements.length;i++){
				int removeTab = Integer.parseInt(elements[i]);
				writer.removeSheet(removeTab);
			}
			writer.save();
			user.out("Remove completed.");
		} catch (Exception e) {
			user.out("Unable to remove tab due to error.");
		}
	}

	public String getSheetNames() {
		HSSFWorkbook data;
		StringBuffer str = new StringBuffer();
		try {
			data = loadBook(user.utils.getFile("data"));
			for(int i =1; i<data.getNumberOfSheets();i++){
				str.append(i+": "+data.getSheetName(i)+"\n");
			}
			return str.toString();
		} catch (IOException e) {
			user.utils.exceptionHandler(e, "Unable to get sheet names.");
			return "";
		}
	}
}
