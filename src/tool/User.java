package tool;

/**
 * This class interacts with all parts of the application.
 * It also contains the main method for the program.
 * @author Steven Fitzpatrick
 * @dateEdited 8-31-2015
 * @version 1.2
 */
public class User {
	
	protected String user;
	protected Utilities utils;
	
	protected ExcelReader resources;
	protected VisualValues visualVal;
	protected ToolGUI gui;
	protected DataLogger log;
	private StringBuffer toPrint;
	private boolean programCompleted;
	String fiscalYear;

	
	
	/**
	 * Constructor for the User class.
	 * It will ask for the user's name: used for the logger and when importing new data
	 * Then it will initialize the program, by calling the initialize method.
	 * @dateEdited 2-14-2016
	 */
	public User(){
		utils = new Utilities(this);
		System.out.println("Input Name of User: [then press enter]");
		user = utils.next();
		System.out.println("User inputed. Commencing Program Initialization.");
		/*
		 * Check the file paths. Return them in the form of an array.
		 */
		if(utils.checkResources()){
			initialize();
		}
	}

	/**
	 * Main initialization for the program.
	 * It will start by initializing the GUI, followed by the data.xls sheet
	 * If there are any errors, the program will immediately crash.
	 * If show current data is true, then it will also initialize a new excel viewer to display
	 * the data within the GUI.
	 * @dateEdited 2-14-2016
	 */
	private void initialize(){
		toPrint = new StringBuffer();//for the output buffer.
		programCompleted = false;//completion of the 
		visualVal = new VisualValues();
		gui = new ToolGUI(this);
		try{
			resources = new ExcelReader(this);
		} catch (IllegalArgumentException e){
			System.out.println("Fatal Exception on the program caused it to crash.");
			e.getMessage();
			e.printStackTrace();
			utils.close();
		}
		log = new DataLogger(utils);
		fiscalYear = utils.getFiscalYear();
		log.newEntry(user, " logged in.");
		programCompleted = true;
		System.out.println("Program fully initialized. Please refer to the new window.");
		out("program fully initialized.");
	}
	
	/**
	 * Output text. If the GUI is initialized, then output to the GUI's console.
	 * If not, send the output to the System's output buffer.
	 * @param text output text from the program
	 * @dateEdited 2-14-2016
	 */
	public void out(String text){
		if(programCompleted){
			if(toPrint.length()>0){
				gui.consoleOut(toPrint + "\n" + text);
				toPrint = new StringBuffer();
			} else{
				gui.consoleOut(text);
			}
		} else{
			toPrint.append("\n" + text);
			System.out.println(text);
		}
	}
	
	/**
	 * Input commands engine.
	 * Used in the GUI's console
	 * @param input command.
	 */
	public void in(String input){
		out(gui.userName.getText()+ input);
		if(input.equals("clean")){
			resources.cleanData(null);
			log.newEntry(user, " cleaned data.xls");
			out("data cleaned.");
		} else if(input.contains("change userName")){
			log.newEntry(user, " changed userName to " + input.substring(16));
			user = input.substring(16);
			gui.userName.setText(user +">");
			out("changed username to " + user);		
		} else if(input.equals("close program")){
			out("Closing...");
			log.newEntry(user, " logged out.");
			utils.close();
			gui.dispose();
		} else if(input.equals("print data")){
			out("Printing to GUI console...");
			out(resources.print());
		} else if(input.equals("move console")){
			System.out.println(gui.getConsoleText());
		} else if(input.equals("print log")){
			out(log.out());
		} else if(input.contains("log note")){
			String note = input.substring(8);
			log.newEntry(user, "Note: " + note);
			out("Note: " + note + "\nSent to logger.");
		} else if(input.equals("about")){
			out(utils.about());
		} else if(input.equals("open uncategorized")){
			out("attempting to make uncategorized list...");
			resources.uncategorizedItems();
			out("uncategorized list made.");
		} else if(input.equals("print notes")){
			out(log.out(true));
		} else if(input.contains("open file ")){
			utils.open(input.substring(10));
		} else if(input.equals("test")){
			utils.test();
		} else if(input.contains("update fiscal year")){
			fiscalYear = input.substring(19);
			out("updated fiscal year to " + fiscalYear);
		} else if(input.contains("print from fiscal year")){
			out("Items from fiscal year " + input.substring(22)+"\n"+ resources.getItemsFromFY(input.substring(22)));
			out("All items printed.");
		} else if(input.equals("most frequent")){
			resources.mostFrequentItems();
			out("most frequent items list made.");
		} else if(input.equals("backup")){
			ExcelWriter.backupData(this);
			out("Data backup successful.");
		} else if(input.equals("get sheet names")){
			out(resources.getSheetNames());
		} else if(input.contains("remove")){
			resources.remove(input);
		} else if(input.equals("get fiscal year")){
			out(fiscalYear);
		} else{
			StringBuffer commands = new StringBuffer();
			commands.append("Unable to process command.\n");
			commands.append("\n");
			commands.append("\n");
			commands.append("Possible commands:\n");
			commands.append("\tabout -About this project\n");
			commands.append("\tclean -Make a new data.xls sheet\n");
			commands.append("\tchange userName -Change the current user's Name. Follow the command with the desired change.\n");
			commands.append("\tprint data -print out to the gui console all of the data.\n");
			commands.append("\tprint log -Move the contents of the logger to the console.\n");
			commands.append("\tprint notes -Print out all of the logger's notes.\n");
			commands.append("\tshow data -show the current data using excel viewer\n");
			commands.append("\tmove console -Move the contents of the console to the output buffer.\n");
			commands.append("\tlog note -Write a note for the logger.\n");
			commands.append("\tclose program -dispose all program resources.\n");
			commands.append("\topen uncategorized -get all uncategorized purchases as an excel file.\n");
			commands.append("\topen file -Follow by the name of the file that you want to open.\n");
			commands.append("\tupdate fiscal year -Follow by \"FY\" and the final two numbers of the year. Defaults to the calendar year.\n");
			commands.append("\tprint from fiscal year -Follow by \"FY\" and the final two numbers of the year.\n");
			commands.append("\tget fiscal year -Get the current fiscal year that the program is using\n");
			commands.append("\tmost frequent -Get the most frequent items\n");
			commands.append("\tbackup -Backup the current data.xls file.\n");
			commands.append("\tremove [file] [sheetNumber] -remove a sheet from the designated file.\n");
			commands.append("\tget sheet names -Show the sheet numbers for the associated sheet names\n");
			commands.append("\n\n");
			out(commands.toString());
		}
		
		gui.inputLine.setText("");
	}
	
	public static void main(String[] args){
		new User();
	}
}
