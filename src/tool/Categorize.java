package tool;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;

/**
 * Main Method for all of these calculations. 
 * Called from the GUI After user decides which file to use.
 */
public class Categorize {
	
	protected static void runNewCategorization(File file, ExcelReader reader) throws IOException{
		/*
		 * Get the first row to categorize.
		 */
		HSSFSheet dataSheet =reader.loadBook(file).getSheetAt(0);
		ExcelWriter writer = new ExcelWriter(reader.user, file, ExcelWriter.FOOD_CATEGORIZATION);
		
		int currentRow = dataSheet.getFirstRowNum();
		
		long time = System.currentTimeMillis();

		while(currentRow <= dataSheet.getLastRowNum()){
			int updatedRows = categorizeItem(currentRow, dataSheet, reader, writer);
			currentRow = updatedRows +1;
		}
		
		reader.user.log.newEntry(reader.user.user, " ran new Calculation on " + file.getName() + ".");
	    
		writer.save();
	    time = (System.currentTimeMillis()- time)/1000;
	    reader.user.out("Transaction took " + time +" seconds.");
	}

	/**
	 * Categorize a Single Food Item.
	 */
	private static int categorizeItem(int currentRow, HSSFSheet sheet, ExcelReader reader, ExcelWriter writer){
		HSSFRow row = sheet.getRow(currentRow);
		
		String itemName = getCell(row.getCell(ExcelReader.ITEM_NAME));
		String rcvUnit = getCell(row.getCell(ExcelReader.RCV_UNIT));

		HashMap<String, CostsAndQuantities> vendors = new HashMap<String,CostsAndQuantities>();
		
		while (currentRow < sheet.getLastRowNum() && getCell(sheet.getRow(currentRow + 1).getCell(0)).equals(itemName) && getCell(sheet.getRow(currentRow + 1).getCell(1)).equals(rcvUnit)){
			row = sheet.getRow(currentRow);//Get the current row
			totalItem(vendors,row);
			currentRow++;//the current row changes
		}
		
		try{
			row = sheet.getRow(currentRow);
			totalItem(vendors,row);
		} catch (IllegalStateException e){
			reader.user.out("Error understanding value on row " + currentRow);
		}
		
		if(vendors.size()>0){
			Set<String> allVendors = vendors.keySet();
			for(String vendor : allVendors){
				FoodItem item = new FoodItem(itemName, rcvUnit, vendor, vendors.get(vendor).getCost(), vendors.get(vendor).getQty());
				writer.writeToExcel(item);
			}
		} else{
			try{
				CostsAndQuantities costAndQty = new CostsAndQuantities(row.getCell(ExcelReader.PRICE).getNumericCellValue(),row.getCell(ExcelReader.QUANTITY).getNumericCellValue());
				FoodItem item = new FoodItem(itemName, rcvUnit, getCell(row.getCell(ExcelReader.VENDOR)), costAndQty.getCost(), costAndQty.getQty());
				writer.writeToExcel(item);
			} catch(IllegalStateException e){
				reader.user.out("Error understanding value on row " + currentRow);
			}
		}
		return currentRow;
	}

	private static void totalItem(HashMap<String, CostsAndQuantities> vendors, HSSFRow row) {
		String vendor = getCell(row.getCell(ExcelReader.VENDOR));
		double qty = row.getCell(ExcelReader.QUANTITY).getNumericCellValue();
		double price = row.getCell(ExcelReader.PRICE).getNumericCellValue();
		if(vendors.containsKey(vendor)){
			CostsAndQuantities costs = vendors.get(vendor);
			costs.addCost(price, qty);
		} else{
			vendors.put(vendor, new CostsAndQuantities(price, qty));
		}
	}

	private static String getCell(HSSFCell cell){
		return cell.getStringCellValue().trim().toLowerCase();
	}
}
