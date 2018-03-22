package tool;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneLayout;

/**
 * This class is designed to be the main java gui for this application
 * @author Steven Fitzpatrick
 * @dateEdited 8-31-2015
 * @version 1.2
 *
 */
@SuppressWarnings("serial")
public class ToolGUI extends JFrame{
	
	private User user;
	private JTabbedPane tabs;
	private JTextArea console;
	private StringBuffer consoleText;
	private JPanel navigationPanel;
	protected JTextField inputLine;
	protected JTextField userName;

	public ToolGUI(User user){
		super("Nitrogen Footprint: Excel Tool");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.user = user;
		this.setBackground(Color.black);
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.X_AXIS));
		this.setPreferredSize(user.visualVal.fullScreen);
		JPanel mainPanel = new JPanel();
		user.visualVal.configurePanel(mainPanel, "x", VisualValues.TAB_SIZE, Color.black, null);
		tabs = new JTabbedPane();
		user.visualVal.configurePanel(tabs, "x", VisualValues.SUBTAB_SIZE, Color.GRAY, null);
		mainPanel.add(addNavigation());
		mainPanel.add(tabs);
		addConsole();
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(WindowEvent e){
				user.utils.close();
			}
		});
		this.add(mainPanel);
		this.pack();
		this.setVisible(true);
	}
	
	private JPanel addNavigation(){
		user.out("Adding Navigation Panel to Gui...");
		navigationPanel = new JPanel();
		Dimension navDimension = new Dimension(120, user.visualVal.tabSize.height);
		user.visualVal.configurePanel(navigationPanel, "y", VisualValues.CUSTOM_SIZE, Color.gray, navDimension);
		JLabel navLabel = new JLabel("Go to:");
		JButton loadDataBtn = new JButton("Load New Data");
		loadDataBtn.setName("Data");
		loadDataBtn.addActionListener(new ButtonListener(loadDataBtn));
		JButton runNewFootprint = new JButton("Run New Categorization");
		runNewFootprint.setName("fprint");
		runNewFootprint.addActionListener(new ButtonListener(runNewFootprint));
		navigationPanel.add(navLabel);
		navigationPanel.add(navLabel);
		navigationPanel.add(loadDataBtn);
		navigationPanel.add(runNewFootprint);
		user.out("Navigation Panel successfully added.");
		return navigationPanel;
	}
	
	public void addTab(JPanel panel, String name){
		user.out("Attempting to add tab " + name);
		if(name == null || panel == null){
			user.out("Unable to process request due to null values on the panel or panel title.");
			return;
		}
		tabs.addTab(name, panel);
		JButton tabBtn = new JButton(name);
		tabBtn.setName(name);
		tabBtn.addActionListener(new ButtonListener(tabBtn));
		navigationPanel.add(tabBtn);
		user.out("Tab successfully added.");
		updateGUI();
	}
	
	private void addConsole(){
		JPanel consolePanel = new JPanel();
		user.visualVal.configurePanel(consolePanel, "y", VisualValues.SUBTAB_SIZE, Color.darkGray, null);
		if(consoleText == null){
			consoleText = new StringBuffer();
		}
		console = new JTextArea(consoleText.toString());
		console.setBorder(BorderFactory.createTitledBorder("Console Window"));
		consolePanel.add(Box.createHorizontalGlue());
		
		JScrollPane consoleScroll = new JScrollPane();
		consoleScroll.setViewportView(console);
		ScrollPaneLayout layout = new ScrollPaneLayout();
		layout.setVerticalScrollBarPolicy(ScrollPaneLayout.VERTICAL_SCROLLBAR_AS_NEEDED);
		consoleScroll.setLayout(layout);
		
		JPanel inputLinePanel = new JPanel();
		user.visualVal.configurePanel(inputLinePanel, "x", VisualValues.ONE_FULL_LINE, null, null);
		userName = new JTextField();
		userName.setEditable(false);
		userName.setText(user.user + ">");
		userName.setMaximumSize(new Dimension(user.visualVal.oneFullLine.width/16,user.visualVal.oneFullLine.height));
		
		inputLine = new JTextField();
		inputLine.setMaximumSize(user.visualVal.oneFullLine);
		inputLine.setEditable(true);
		inputLine.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				user.in(inputLine.getText());
			}
		});
		inputLinePanel.add(userName);
		inputLinePanel.add(inputLine);
		consolePanel.add(consoleScroll);
		consolePanel.add(inputLinePanel);
		addTab(consolePanel, "Console");
	}
	
	protected boolean updateGUI(){
		try{
			this.paint(getGraphics());
			return true;
		} catch(Exception e){
			return false;
		}
	}
	
	protected void consoleOut(String append){
		if(consoleText == null){
			consoleText = new StringBuffer();
			consoleText.append(append);
		} else if(console == null){
			consoleText.append('\n' + append);
		} else{
			consoleText.append('\n' + append);
			console.setText(consoleText.toString());
			if(!updateGUI()){
				System.out.println(append);
			}
		}
	}
	
	protected String getConsoleText(){
		return consoleText.toString();
	}
	
	public class ButtonListener implements ActionListener{
		private JButton button;
		public ButtonListener(JButton button){
			this.button = button;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			consoleOut(button.getName() + " selected.");
			if(button.getName().equals("Data") || button.getName().equals("fprint")){
				fileButtons();
			}
			else {
				consoleOut(tabs.indexOfTab(button.getName()) + " tab selected...");
				tabs.setSelectedIndex(tabs.indexOfTab(button.getName()));
				updateGUI();
			}
		}
		
		private void fileButtons(){
			JFileChooser fc = new JFileChooser();
			if(fc.showOpenDialog(tabs) == JFileChooser.APPROVE_OPTION){
				File file = fc.getSelectedFile();
				if(button.getName().equals("Data")){
					try {
						user.resources.importNewDataExcel(file);
					} catch (IOException e1) {
						consoleOut("Failed to load new Data.xls");
					}
				} else{
					try {
						user.resources.runNewCategorization(file);
					} catch (IOException e1) {
						user.out("Failed to load new Footprint");
						e1.printStackTrace();
					} catch (IllegalArgumentException e){
						user.out("Footprint already completed (already have a sheet named 'calculated sheet')");
					}
				}
			} else{
				user.out("Stopped due to user cancelation selection...");
			}
		}
	}
}
