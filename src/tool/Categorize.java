package tool;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;

/**
 * Runs the Nitrogen-Footprint Program.
 * Main Method for all of these calculations. Called from the GUI After user decides which file to use.
 * @dateEdited 9-29-2015
 * @author Steven Fitzpatrick
 * @dateEdited 8-31-2015
 * @version 1.2
 */
public class Categorize {
	
	protected static void runNewCategorization(File file, ExcelReader reader) throws IOException{
		/*
		 * Get the first row to categorize.
		 */
		HSSFSheet dataSheet =reader.loadBook(file).getSheetAt(0); //Get the first worksheet
		ExcelWriter writer = new ExcelWriter(reader.user, file, ExcelWriter.FOOD_CATEGORIZATION);
		
		int currentRow = dataSheet.getFirstRowNum();//Current Row Number that is being read
		
		long time = System.currentTimeMillis();

		while(currentRow <= dataSheet.getLastRowNum()){
			int updatedRows = categorizeItem(currentRow, dataSheet, reader, writer);
			currentRow = updatedRows +1;
		}
		/*
		 * write the file, open the calculated sheet, and output the status to the user.
		 */		
		reader.user.log.newEntry(reader.user.user, " ran new Calculation on " + file.getName() + ".");
	    
		writer.save();
	    time = (System.currentTimeMillis()- time)/1000;
	    reader.user.out("Transaction took " + time +" seconds.");
	}

	/**
	 * Categorize a Single Food Item.
	 * A Food Item may have multiple line entries associated with it, but we will just total the one entry.
	 * @param currentRow current excel row number
	 * @param sheet dataSheet
	 * @param reader Excel Reader (for output)
	 * @return the updated currentRow after this item is complete.
	 */
	private static int categorizeItem(int currentRow, HSSFSheet sheet, ExcelReader reader, ExcelWriter writer){
		HSSFRow row = sheet.getRow(currentRow);//Get the current row to be read
		/*
		 * Get the Current Item Name and the Receive Unit
		 */
		String itemName = getCell(row.getCell(ExcelReader.ITEM_NAME));
		String rcvUnit = getCell(row.getCell(ExcelReader.RCV_UNIT));

		/*
		 * Make a list of vendors and quantities for each vendor 
		 */
		HashMap<String, CostsAndQuantities> vendors = new HashMap<String,CostsAndQuantities>();
		
		/*
		 * Continue this next loop totaling for the current item until the last item (by name or by a change in receive unit)
		 */
		while (currentRow < sheet.getLastRowNum() && getCell(sheet.getRow(currentRow + 1).getCell(0)).equals(itemName) && getCell(sheet.getRow(currentRow + 1).getCell(1)).equals(rcvUnit)){
			row = sheet.getRow(currentRow);//Get the current row
			totalItem(vendors,row);
			currentRow++;//the current row changes
		}
		/*
		 * Do the last item.
		 */
		try{
			row = sheet.getRow(currentRow);
			totalItem(vendors,row);
		} catch (IllegalStateException e){
			reader.user.out("Error understanding value on row " + currentRow);
		}
		
		/*
		 * Dump all of the line entries for this item into the calculated excel sheet.
		 */
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
		String vendor = getCell(row.getCell(ExcelReader.VENDOR));//Get the vendor of this line item
		double qty = row.getCell(ExcelReader.QUANTITY).getNumericCellValue();//Get the Receive Quantity for this line item
		double price = row.getCell(ExcelReader.PRICE).getNumericCellValue(); //Get the Unit Price
		if(vendors.containsKey(vendor)){ //If the vendor is already in the list
			CostsAndQuantities costs = vendors.get(vendor);//Put this new quantity into the hashmap
			costs.addCost(price, qty);
		} else{
			vendors.put(vendor, new CostsAndQuantities(price, qty));//Otherwise add this new vendor and quantity to the list
		}
	}

	private static String getCell(HSSFCell cell){
		return cell.getStringCellValue().trim().toLowerCase();
	}
}
