package tool;

/**
 * This class is constructed to aid in the totaling of the costs and quantities
 * Of a food item from the runNewFootprint method.
 * @author Steven Fitzpatrick
 * @dateEdited 8-31-2015
 * @version 1.2
 *
 */
public class CostsAndQuantities {
	private double cost;//running total of the cost for a food item.
	private double qty;//the running total of the quantities for a food item.
	
	public CostsAndQuantities(double price, double qty){
		cost = qty*price;
		this.qty = qty;
	}
	
	/**
	 * Another line item with this product from this vendor is found
	 * So, update the total charge for this item and the Quantity
	 * @param price cost of this item
	 * @param qty additional quantity for this item
	 */
	public void addCost(double price, double qty){
		cost += qty*price;
		this.qty +=qty;
	}
	
	/**
	 * Return the cost of this item. 
	 * @return cost of product
	 */
	public double getCost(){
		return cost;
	}
	
	/**
	 * Return the total quantity of items.
	 * @return quantity of items
	 */
	public double getQty(){
		return qty;
	}
}
