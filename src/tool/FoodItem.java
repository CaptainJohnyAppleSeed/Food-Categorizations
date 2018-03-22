package tool;

import java.util.HashMap;
import java.util.Set;

/**
 * This class is designed to keep track of each food item.
 * @author Steven Fitzpatrick
 * @dateEdited 8-31-2015
 * @version 1.2
 */
public class FoodItem {
	private String name;
	private String vendor;
	private String receiveUnit;
	
	private String[] faoCategories;
	private double weightPerUnit; //weight per receive unit
	private String weightUnit; //lbs, grams,...
	private int numCategories;
	private HashMap<String,Boolean> historicalData;
	private int rowNum;
	
	private double cost;
	private double quantity;
	
	/**
	 * Constructor for a template for Categorized Items
	 * @param itemName product name
	 * @param receiveUnit receive unit
	 * @param vendor vendor
	 * @param faoCategories list of food categories
	 * @param weightPerUnit weight per unit
	 * @param weightUnit the units for weightPerUnit
	 */
	public FoodItem(String itemName, String receiveUnit, String vendor, String[] faoCategories, double weightPerUnit, String weightUnit){
		this.name = itemName;
		this.vendor = vendor;
		this.faoCategories = faoCategories;
		this.weightPerUnit = weightPerUnit;
		this.weightUnit = weightUnit;
		this.receiveUnit = receiveUnit;
		numCategories = 0;
		if(faoCategories[0] != null){
			for(int i =0; i<4;i++){
				String fao = faoCategories[i];
				if(fao == null){
					faoCategories[i]="";
				}else{
					if(!fao.equals("")){
						numCategories++;
					}
				}
			}
		}
	}
	
	/**
	 * Constructor for a template for Categorized Items
	 * @param itemName product name
	 * @param receiveUnit receive unit
	 * @param vendor vendor
	 * @param faoCategories list of food categories
	 * @param weightPerUnit weight per unit
	 * @param weightUnit the units for weightPerUnit
	 */
	public FoodItem(String itemName, String receiveUnit, String vendor, double cost, double quantity){
		this.name = itemName;
		this.vendor = vendor;
		this.receiveUnit = receiveUnit;
		this.quantity=quantity;
		this.cost=cost;
	}
	
	public void addHistoricalData(HashMap<String,Boolean> historicalData, int rowNum){
		this.historicalData=historicalData;
		this.rowNum = rowNum;
	}
	
	public int getRowNum(){
		return this.rowNum;
	}
	
	public String identifier(){
		String str = name + " (" + vendor + ") " + receiveUnit;
		return str.toLowerCase().trim();
	}
	
	@Override
	public String toString(){
		return name + ": " + vendor + " (" + numCategories + ")";
	}
	
	/**
	 * Check that the following are the same:
	 * 		Item Name
	 * 		Vendor
	 * 		ReceiveUnit
	 * 
	 * @return true if the object is the same food item 
	 */
	public boolean equals(Object obj){
		if(obj instanceof FoodItem){
			FoodItem item = (FoodItem) obj;
			/*
			 * Check if this the basic info of a product: name, vendor, and the receive unit
			 */
			if(item.getItemName().equals(this.getItemName())&&item.getVendor().equals(this.getVendor())&&item.getReceiveUnit().equals(this.getReceiveUnit())){
				/*
				 * Check the categorization particulars
				 */
				if((item.numCategories() == this.numCategories())&&item.getWeightUnit().equals(this.getWeightUnit())&&item.getWeightPerItem()==this.getWeightPerItem()){
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Check that the following are the same:
	 * 		Number of Food Categories
	 * 		The Unit of mass for weightPerUnit
	 * 		WeightPerUnit
	 * 
	 * @return true if the object is the same food item 
	 */
	public boolean sameItemCategorization(FoodItem item){
		if(item.equals(this)){
			/*
			 * Check the categorization particulars
			 */
			if((item.numCategories() == this.numCategories())&&item.getWeightUnit().equals(this.getWeightUnit())){
				return true;
			}
		}
		return false;
	}
	
	public int getFrequencyOfItem(){
		if(historicalData != null){
			int frequency = 0;
			Set<String> keys = historicalData.keySet();
			for(String key: keys){
				if(historicalData.get(key)){
					frequency++;
				}
			}
			return frequency;
		} else{
			throw new IllegalArgumentException("Historical Data not initialized for item: " + this.getItemName() + ".");
		}
	}
	
	public boolean itemPartOfFY(String fiscalYear){
		if(historicalData != null){
			if(historicalData.get(fiscalYear) == null){
				return false;
			} else{
				return historicalData.get(fiscalYear);
			}
		} else{
			throw new IllegalArgumentException("Historical Data not initialized for item: " + this.getItemName() + ".");
		}
	}
	
	public HashMap<String,Boolean> getHistorical(){
		return this.historicalData;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean uncategorized(){
		return getWeightPerItem()==0 
				&& !getReceiveUnit().toLowerCase().equals("dollars")//Ensure that it is Not dollars
				&& !getFAOCategories()[0].equals("non-food related");//Ensure that it is not a non-food product
	}
	/*
	 * General Identifiers For an Item...
	 */
	/**
	 * Get the food Item Name. (i.e., chix brst)
	 * @return itemName
	 */
	public String getItemName(){
		return name;
	}
	
	/**
	 * Get the Vendor (Company) that supplies this item (i.e., Feesers)
	 * @return vendor
	 */
	public String getVendor(){
		return vendor;
	}
	
	/**
	 * Get the Receive Unit for this item (i.e., Case, Basket, CS,...)
	 * @return rcvUnit
	 */
	public String getReceiveUnit(){
		return receiveUnit;
	}
	
	//Identifiers for CATEGORIZED (Template)
	/**
	 * Get the list of FAO Food Categories associated with this item.
	 * @return arr of FAO Categories
	 */
	public String[] getFAOCategories(){
		return faoCategories;
	}
	
	public String getWeightUnit(){
		return weightUnit;
	}
	
	public int numCategories(){
		return numCategories;
	}
	
	
	public double getWeightPerItem(){
		return weightPerUnit;
	}
	
	//Identifiers for costs and associated Quantities
	
	public double getCost(){
		return this.cost;
	}
	
	public double getQuantity(){
		return this.quantity;
	}
}
