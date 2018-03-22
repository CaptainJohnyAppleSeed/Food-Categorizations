package tool;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;

import javax.swing.BoxLayout;
import javax.swing.JComponent;

/**
 * Gives all of the Visual Values necessary for the GUI
 * @author Steven Fitzpatrick
 * @dateEdited 8-31-2015
 * @version 1.2
 *
 */
public class VisualValues {
	
	public Dimension fullScreen;
	public Dimension tabSize;
	public Dimension oneFullLine;
	public Dimension tabSizeHalfHeight;
	public Dimension subTab;
	public Dimension subTabHalfHeight; 
	
	//SIZES
	public static final int FULL_SCREEN = 0;
	public static final int TAB_SIZE = 1;
	public static final int TAB_SIZE_HALF_HEIGHT = 2;
	public static final int SUBTAB_SIZE = 1;
	public static final int SUBTAB_SIZE_HALF_HEIGHT = 2;
	public static final int ONE_FULL_LINE = 3;
	public static final int CUSTOM_SIZE = 4;

	public VisualValues(){
		Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
		int screenHeight = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height;
		int screenWidth = screenDimension.width;
		initializeDimensions(screenHeight, screenWidth);
	}

	private void initializeDimensions(int screenHeight, int screenWidth) {
		fullScreen = new Dimension(screenWidth, screenHeight);
		tabSize = new Dimension(screenWidth - 100, screenHeight - 100);
		tabSizeHalfHeight = new Dimension(tabSize.width, screenHeight/2);
		subTab = new Dimension(tabSize.width - 100, tabSize.height -50);
		subTabHalfHeight = new Dimension(subTab.width, subTab.height/2);
		oneFullLine = new Dimension(tabSize.width, 30);
	}
	
	/**
	 * Method to configure panels for the GUI
	 * @dateEdited 8-31-2015
	 * @param component input component such as a JPanel
	 * @param orientation "x" or "y"
	 * @param size from the constants
	 * @param color of the panel
	 * @param dimension used for custom size
	 */
	public void configurePanel(JComponent component, String orientation, int size, Color color, Dimension dimension){
		if(orientation.equals("x") || orientation.equals("X")){
			component.setLayout(new BoxLayout(component, BoxLayout.X_AXIS));
		} else if(orientation.equals("y") || orientation.equals("Y")){
			component.setLayout(new BoxLayout(component, BoxLayout.Y_AXIS));
		}
		
		if(size == VisualValues.FULL_SCREEN){
			component.setMinimumSize(fullScreen);
			component.setMaximumSize(fullScreen);
		} else if(size == VisualValues.TAB_SIZE){
			component.setMinimumSize(tabSize);
			component.setMaximumSize(tabSize);
		} else if(size == VisualValues.TAB_SIZE_HALF_HEIGHT){
				component.setMinimumSize(tabSizeHalfHeight);
				component.setMaximumSize(tabSizeHalfHeight);
		} else if(size == VisualValues.SUBTAB_SIZE){
			component.setMinimumSize(subTab);
			component.setMaximumSize(subTab);
		} else if(size == VisualValues.SUBTAB_SIZE_HALF_HEIGHT){
				component.setMinimumSize(subTabHalfHeight);
				component.setMaximumSize(subTabHalfHeight);
		} else if(size == VisualValues.ONE_FULL_LINE){
			component.setMinimumSize(oneFullLine);
			component.setMaximumSize(oneFullLine);
		} else if(size == VisualValues.CUSTOM_SIZE && dimension != null){
			component.setMinimumSize(dimension);
			component.setMaximumSize(dimension);
		} 
		
		if(color != null){
			component.setBackground(color);
		}
	}
}
