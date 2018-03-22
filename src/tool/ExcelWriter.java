package tool;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * 
 * Class designed to facilitate the writing of data to an Excel File
 * @author Steven Fitzpatrick
 * @dateEdited 8-31-2015
 * @version 1.2
 *
 */
public class ExcelWriter {

	private User user;
	private ExcelReader reader;
	private File saveFile;
	
	private HSSFWorkbook writeBook;
	private HSSFSheet writeSheet;
	
	private HSSFWorkbook dataBook;
	private HSSFSheet dataSheet;
	
	private int inputRow;
	private int historicalCol;
	private boolean updateHistorical;
	
	//Settings
	private int setting;
	public static final int CLEAN_DATA = 0;
	public static final int CALCULATION = 1;
	public static final int FOOD_CATEGORIZATION = 2;
	public static final int REMOVE = 3;
	
	/**
	 * Make a new ExcelWriter object. This will write to the input sheet by making a new "CalculatedSheet"
	 * @param user user
	 * @param input input Excel Sheet
	 * @param saveFile location to save this sheet.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public ExcelWriter(User user, File sourceFile, int setting) throws FileNotFoundException, IOException{
		loadResources(user);
		loadDataExcel();
		this.setting = setting;
		if(setting == ExcelWriter.CLEAN_DATA){
			cleanData(sourceFile);
		} else if(setting == ExcelWriter.FOOD_CATEGORIZATION){
			categorization(sourceFile);
		} else if(setting == ExcelWriter.CALCULATION){
			calculation(sourceFile);
		} else if(setting == ExcelWriter.REMOVE){
			remove(sourceFile);
		}
		
	}
	
	/**
	 * Initialize the clean data setting.
	 * This setting involves importing data as a new excel tab.
	 * @param sourceFile used to update the metaData
	 * @throws IOException
	 */
	private void cleanData(File sourceFile) throws IOException{
		saveFile = user.utils.getFile("data");
		String tabName = makeTabName();
		writeSheet = dataBook.createSheet(tabName);
		makeHeaderRow(writeSheet);
		addMeta(tabName, sourceFile);
		inputRow = 2;
		user.out("Cleaning data...");
	}

	/**
	 * Initialize the categorization setting.
	 * This setting involves using the program to categorize data. Historical data in data.xls will also be updated.
	 * @param saveFile file to save the categorized workbook.
	 * @throws IOException
	 */
	private void categorization(File saveFile) throws IOException{
		writeBook = reader.loadBook(saveFile);
		this.saveFile = saveFile;
		writeSheet = writeBook.createSheet("Calculated Sheet");
		inputRow = 1;
		user.out("New cattegorization file made.");
	}
	
	/**
	 * Initialize the calculation setting.
	 * This setting is for outputting calculations without changing data.xls
	 * @param saveFile output file for the calculations
	 */
	private void calculation(File saveFile){
		this.saveFile = saveFile;
		writeBook = new HSSFWorkbook();
		updateHistorical = false;
		user.out("New calculation excel file made.");
	}
	
	/**
	 * Initialize the remove setting.
	 * Check if the file is the same as data. If it is, then backup data.xls
	 * @throws IOException
	 */
	private void remove(File removeFile) throws IOException {
		if(user.utils.getFile("data").equals(removeFile)){
			backupData(user);
			loadDataExcel();
			writeBook = dataBook;
			writeSheet = writeBook.getSheet("Meta");
		} else{
			writeBook = reader.loadBook(user.utils.getFile("data"));
		}	
	}
	
	private void loadResources(User user){
		this.user = user;
		reader = user.resources;
		updateHistorical = true;
	}
	
	/**
	 * Load data.xls excel sheet, for historical purposes.
	 * @throws IOException
	 */
	private void loadDataExcel() throws IOException {
		dataBook = reader.loadBook("data");
		dataSheet = dataBook.getSheetAt(dataBook.getNumberOfSheets()-1);
		if(reader.header[reader.header.length-1].equals(user.fiscalYear)){
			historicalCol = reader.header.length -1;
		} else{
			historicalCol = dataSheet.getRow(ExcelReader.HEADER_ROW).getLastCellNum();
			dataSheet.getRow(ExcelReader.HEADER_ROW).createCell(historicalCol).setCellValue(user.fiscalYear);
		}
	}
	
	public static void backupData(User user){
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		try{
			HSSFWorkbook backupFile = user.resources.loadBook(user.utils.getFile("data"));
			backupFile.write(new FileOutputStream(new File("src/resources/backups/"+user.user+ "_"+ dateFormat.format(date)+".xls")));
			user.log.newEntry(user.user, "made a backup of data.xls");
			user.out("Backup of data.xls created.");
		} catch(Exception e){
			user.utils.exceptionHandler(e, "Unable to backup data");
		}
	}
	
	public void setHistorical(boolean updateHistorical){
		this.updateHistorical=updateHistorical;
	}
	
	public void writeHeader(){
		this.makeHeaderRow(writeSheet);
	}
	
	public void removeSheet(int sheetNumber){
		if(sheetNumber>0 && sheetNumber< writeBook.getNumberOfSheets()){
			user.out("Attempting to remove sheet " + writeBook.getSheetName(sheetNumber));
			writeBook.removeSheetAt(sheetNumber);
			removeFromMeta(sheetNumber);
			user.out("Successfully removed sheet.");
		} else{
			user.out("invalid sheet number.");
		}
	}
	
	private void removeFromMeta(int sheetNumber) {
		if(writeSheet != null){
			/*
			 * Find the first row for this.
			 */
			int curRow = 12;
			double finalRow = writeSheet.getRow(0).getCell(1).getNumericCellValue()-7;
			curRow+=4*(sheetNumber-1);
			int lastRowNum=writeSheet.getLastRowNum();
		    if(curRow <lastRowNum-4){
		        writeSheet.shiftRows(curRow+4,lastRowNum, -4);
		    }
			while(curRow<=finalRow){
				double curTab = writeSheet.getRow(curRow).getCell(1).getNumericCellValue();
				writeSheet.getRow(curRow).getCell(1).setCellValue((curTab-1));
				curRow= curRow+4;
			}
			 writeSheet.getRow(0).getCell(1).setCellValue(finalRow-4);
		}
	}

	public void newSheet(String sheetName){
		writeSheet = writeBook.createSheet(sheetName);
		inputRow = 2;
	}
	
	/**
	 * Write the specified food item into the excel workbook.
	 * @param item food item to 
	 * @throws NullPointerException
	 */
	public void writeToExcel(FoodItem item){
		try{
			if(setting==ExcelWriter.FOOD_CATEGORIZATION){
				writeToCalculated(item);
			} else{
				writeToClean(item);
			}
		} catch(Exception e){
			user.out("ERROR: failed to write item " + item.getItemName() + " on row " + (inputRow -1) +" Exception: " + e);
		}
	}
	
	/**
	 * Helper method to write to the data.xls for cleanData() purposes
	 * @param item current FoodItem
	 */
	private void writeToClean(FoodItem item) {
		HSSFRow row = writeItemInfo(item);
		if(item.getWeightPerItem() != -5.0){
			row.createCell(ExcelReader.READ_WEIGHT_PER_UNIT).setCellValue(item.getWeightPerItem());
		}
		row.createCell(ExcelReader.READ_WEIGHT_UNIT).setCellValue(item.getWeightUnit());
		for(int z = 0; z< item.numCategories(); z++){
			row.createCell(ExcelReader.READ_FAO+z).setCellValue(item.getFAOCategories()[z]);
		}
		writeHistoricalInfo(item,row);
		
	}

	/**
	 * This method is used to write a line item from the N-Footprint into Excel
	 * @dateEdited 10-12-2015
	 * @author fitzpats
	 * @param item food item to be categorized
	 * @param row current row
	 * @param data whether or not this is a data cleaning
	 * @param excelReader for resources
	 */
	private void writeToCalculated(FoodItem item){
		HSSFRow row = writeItemInfo(item);
		FoodItem currentItem = reader.getItem(item.identifier());
		row.createCell(ExcelReader.QUANTITY).setCellValue(item.getQuantity());//Set the Cell Quantity
		row.createCell(ExcelReader.COST).setCellValue(item.getCost());//Set the Cell Costs
		if(currentItem != null){
			if(currentItem.getWeightPerItem() != -5.0){
				row.createCell(ExcelReader.WRITE_WEIGHT_PER_UNIT).setCellValue(currentItem.getWeightPerItem());
			}	
			row.createCell(ExcelReader.WRITE_WEIGHT_UNIT).setCellValue(currentItem.getWeightUnit());
			for(int z = 0; z< currentItem.numCategories(); z++){
				row.createCell(ExcelReader.WRITE_FAO+z).setCellValue(currentItem.getFAOCategories()[z]);
			}
			updateHistorical(currentItem);
		}
	} 
	
	private void updateHistorical(FoodItem currentItem) {
		if(ExcelReader.HISTORICAL_DATA && this.updateHistorical){
			this.dataSheet.getRow(currentItem.getRowNum()).createCell(this.historicalCol).setCellValue(ExcelReader.HISTORICAL_TRUE);
		}
		
	}

	/**
	 * Write the header information (necessary for both forms of write) and return the created row.
	 * @param item Food Item object
	 * @return the current Row
	 */
	private HSSFRow writeItemInfo(FoodItem item) {
		HSSFRow row = writeSheet.createRow(inputRow);
		this.inputRow++;
		row.createCell(ExcelReader.ITEM_NAME).setCellValue(item.getItemName()); //Add the Item Name
		row.createCell(ExcelReader.RCV_UNIT).setCellValue(item.getReceiveUnit());
		row.createCell(ExcelReader.VENDOR).setCellValue(item.getVendor());//Add The Vendor
		return row;
	}
	
	/**
	 * Return the new tab name for data.xls while cleaning the data.
	 * @return string representation of the new tab's name
	 */
	private String makeTabName(){
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		return "DataSheet " + user.user.substring(0, 4) + " "+ dateFormat.format(date);
	}
	
	private void makeHeaderRow(HSSFSheet sheet) {
		if(reader.header == null){
			HSSFRow row = sheet.createRow(ExcelReader.HEADER_ROW);
			row.createCell(ExcelReader.ITEM_NAME).setCellValue("Item Name");
			row.createCell(ExcelReader.RCV_UNIT).setCellValue("Received Unit");
			row.createCell(ExcelReader.VENDOR).setCellValue("Vendor");
			for(int i=0; i<4;i++){
				row.createCell(ExcelReader.READ_FAO+i).setCellValue("FAO " + i);
			}
			row.createCell(ExcelReader.READ_WEIGHT_PER_UNIT).setCellValue("Weight Per Unit");
			row.createCell(ExcelReader.READ_WEIGHT_UNIT).setCellValue("Unit Weight");
		} else{
			HSSFRow row = sheet.createRow(ExcelReader.HEADER_ROW);
			for(int i =0; i<reader.header.length;i++){
				row.createCell(i).setCellValue(reader.header[i]);
			}
		}
	}
	/**
	 * Helper method to write out all of the historical information while cleaning data.xls
	 * @param item currentItem
	 * @param row currentRow
	 */
	private void writeHistoricalInfo(FoodItem item, HSSFRow row){
		HashMap<String,Boolean> historical = item.getHistorical();
		if(historical != null){
			Set<String> keys = historical.keySet();
			for(String key:keys){
				if(historical.get(key)!= null){
					if(historical.get(key)){
						for(int i = ExcelReader.READ_WEIGHT_UNIT; i<reader.header.length;i++){
							if(reader.header[i].equals(key)){
								row.createCell(i).setCellValue(ExcelReader.HISTORICAL_TRUE);
		}	}	}	}	}	}
	}
	
	/**
	 * Add Meta Data to the data.xls file.
	 * Helper method for the importNewDataExcel method
	 * @param file source file for the data
	 * @param tabName tab which was created.
	 * @throws IOException
	 */
	protected void addMeta(String tabName, File sourceFile) throws IOException {
		HSSFSheet meta = dataBook.getSheetAt(0);
		HSSFCell lineNumCell =  meta.getRow(0).getCell(1);
		int currentRow = 0;
		if(lineNumCell.getCellType() == HSSFCell.CELL_TYPE_STRING){
			currentRow = Integer.parseInt(lineNumCell.getStringCellValue());
		} else{
			currentRow = Integer.parseInt(String.valueOf(meta.getRow(0).getCell(1).getNumericCellValue()).split("\\.")[0]);
		}
		HSSFRow row1 = meta.createRow(currentRow);
		row1.createCell(1).setCellValue(meta.getWorkbook().getNumberOfSheets()-1);
		row1.createCell(2).setCellValue("@Source File Added");
		if(sourceFile !=null){
			row1.createCell(3).setCellValue(sourceFile.getName());
		}else{
			row1.createCell(3).setCellValue("No file: Data Cleaning.");
		}
		HSSFRow row2 = meta.createRow(currentRow +1);
		row2.createCell(1).setCellValue("-");
		row2.createCell(2).setCellValue("@Source File Author");
		row2.createCell(3).setCellValue(user.user);
		HSSFRow row3 = meta.createRow(currentRow +2);
		row3.createCell(1).setCellValue("-");
		row3.createCell(2).setCellValue("@Date Created");
		DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
		Date date = new Date();
		row3.createCell(3).setCellValue(dateFormat.format(date));
		HSSFRow row4 = meta.createRow(currentRow +3);
		row4.createCell(1).setCellValue("-");
		row4.createCell(2).setCellValue("@Tab Name");
		row4.createCell(3).setCellValue(tabName);
		lineNumCell.setCellValue(currentRow + 4);
	}
	
	/**
	 * Save the excel file and open it.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void save() throws FileNotFoundException, IOException {
		saveData();
		if(this.setting !=ExcelWriter.CLEAN_DATA && this.setting != ExcelWriter.REMOVE){
			writeBook.write(new FileOutputStream(saveFile));
			user.out("Saved work and opening...");
			Desktop.getDesktop().open(saveFile);
			user.out("Operation completed.");
		}
	}

	private void saveData() throws FileNotFoundException, IOException {
		if(setting != ExcelWriter.CALCULATION){
			File file = user.utils.getFile("data");
			dataBook.write(new FileOutputStream(file));
			user.out("Saved changes to data.xls");
		}
	}

}
