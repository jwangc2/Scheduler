import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Scanner;


public class ScheduleCalc {
	
	// final static values
	public final static int WEEKEND = 0;
	public final static int LAB_FREE = 8;
	public final static int OFFICE_HOURS = 9;
	public final static int BREAK = 10;
	public final static int ASSEMBLY = 11;
	public final static int ADV = 12;
	public final static int FAC_PLC = 13;
	public final static int FAC_MTGS = 14;
	public final static int ARTS_ADV = 15;
	public final static int UNSCHEDULED = 16;
	
	public final static int[][] INTERJECTIONS = {
		{ScheduleCalc.OFFICE_HOURS, ScheduleCalc.FAC_PLC, ScheduleCalc.FAC_MTGS, ScheduleCalc.FAC_PLC, ScheduleCalc.OFFICE_HOURS},
		{ScheduleCalc.BREAK, ScheduleCalc.BREAK, ScheduleCalc.BREAK, ScheduleCalc.ASSEMBLY, ScheduleCalc.ADV},
		{ScheduleCalc.ARTS_ADV, ScheduleCalc.ARTS_ADV, ScheduleCalc.BREAK, ScheduleCalc.BREAK, ScheduleCalc.BREAK},
		{ScheduleCalc.OFFICE_HOURS, ScheduleCalc.OFFICE_HOURS, ScheduleCalc.OFFICE_HOURS, ScheduleCalc.OFFICE_HOURS, ScheduleCalc.WEEKEND}
	};
	
	public final static BlockRange[][] TIMINGS = {
		
		{new BlockRange(4, 5, 30), new BlockRange(3, 5), new BlockRange(3, 6), new BlockRange(3, 5), new BlockRange(4, 5, 30)},
		{new BlockRange(7, 8), new BlockRange(7, 8), new BlockRange(10, 11), new BlockRange(7, 11), new BlockRange(7, 8)},
		{new BlockRange(17, 20), new BlockRange(17, 20), new BlockRange(19, 20), new BlockRange(19, 20), new BlockRange(17, 18)},
		{new BlockRange(23, 24, 30), new BlockRange(23, 24, 30), new BlockRange(23, 24, 30), new BlockRange(23, 24, 30), new BlockRange(22, 24)},
		
		{new BlockRange(5, 7), new BlockRange(5, 7), new BlockRange(6, 10), new BlockRange(5, 7), new BlockRange(5, 7)},
		{new BlockRange(8, 13), new BlockRange(8, 13), new BlockRange(11, 14), new BlockRange(11, 14), new BlockRange(8, 13)},
		{new BlockRange(13, 17), new BlockRange(13, 17), new BlockRange(14, 19), new BlockRange(14, 19), new BlockRange(13, 17)},
		{new BlockRange(20, 23), new BlockRange(20, 23), new BlockRange(20, 23), new BlockRange(20, 23), new BlockRange(18, 22)},
		
		{new BlockRange(0, 1), new BlockRange(0, 1), new BlockRange(0, 1), new BlockRange(0, 1), new BlockRange(0, 1)},
		{new BlockRange(1, 2), new BlockRange(1, 2), new BlockRange(1, 2), new BlockRange(1, 2), new BlockRange(1, 2)},
		{new BlockRange(2, 3), new BlockRange(2, 3), new BlockRange(2, 3), new BlockRange(2, 3), new BlockRange(2, 3)},
		
		{new BlockRange(3, 4), null, null, null, new BlockRange(3, 4)}

	};
	
	public final static String[][] TIMING_TEXT = {
		
		{"(8:00-8:30)", "(7:45-8:30)", "(7:45-9:00)", "(7:45-8:30)", "(8:00-8:30)"},
		{"(9:40-9:55)", "(9:40-9:55)", "(10:10-10:25)", "(9:40-10:25)", "(9:40-9:55)"},
		{"(1:10-1:50)", "(1:10-1:50)", "(1:40-1:50)", "(1:40-1:50)", "(1:10-1:20)"},
		{"(3:00-3:30)", "(3:00-3:30)", "(3:00-3:30)", "(3:00-3:30)", "(0:00)"},
		
		{"(8:30-9:40)", "(8:30-9:40)", "(9:00-10:10)", "(8:30-9:40)", "(8:30-9:40)"},
		{"(9:55-11:05)", "(9:55-11:05)", "(10:25-11:35)", "(10:25-11:35)", "(9:55-11:05)"},
		{"(11:05-1:10)", "(11:05-1:10)", "(11:35-1:40)", "(11:35-1:40)", "(11:05-1:10)"},
		{"(1:50-3:00)", "(1:50-3:00)", "(1:50-3:00)", "(1:50-3:00)", "(1:20-2:30)"},
		
		{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"},
		{"Date", "Date", "Date", "Date", "Date"},
		{"A", "B", "C", "D", "E"},
		
		{" ", " ", " ", " ", " "}
	};
	
	public final static String[] TIMING_ROWS = {
		"Day", "Date", "Rot. Day",
		"7:45","8:00","8:30","9:00",
		"9:40","9:55","10:00","10:10",
		"10:25","11:00","11:05","11:35",
		"12:00","1:00","1:10","1:20","1:40",
		"1:50","2:00","2:30","3:00- 3:30"
	};

	// members
	private String[] classes;
	private boolean[][] labFrees;
	private ArrayList<Date> holidays;
	private Date defaultPad;
	private SimpleDateFormat formatter;
	
	public ScheduleCalc() {
		initSchedule();
		
		labFrees = new boolean[7][7];
		holidays = new ArrayList<Date>();
		defaultPad = new Date();
		formatter = new SimpleDateFormat("MM/dd/yyyy");
	}
	
	public ScheduleCalc(File setup) {
		// Ensure that the members are initiated
		this();
		
		// Try to load and parse
		try {
			
			Scanner sc = new Scanner(setup);
			int currentLine = 0;
			
			// Get the next line
			while(sc.hasNextLine()) {
				currentLine ++;
				
				// Parse this line
				Scanner csv = new Scanner(sc.nextLine());
				csv.useDelimiter(",");
				
				// Get the next csv in this line
				int currentIndex = 0;
				while (csv.hasNext()) {
					currentIndex ++;
					
					String nextValue = csv.next();
					if (currentLine == 1) {                                                             // Class Names
						classes[currentIndex] = nextValue;
						System.out.println("Period [" + currentIndex +"]: " + nextValue);
					}
					else if (currentLine > 1 && currentLine < 9) {										// Lab Frees
						// Take the first character
						char c = nextValue.toUpperCase().charAt(0);
						int val = ((int)c - (int)('A'));
						val = Math.max(Math.min(val, 6), 0);
						
						labFrees[currentLine - 2][val] = true;
						System.out.println("Lab Free on [" + (currentLine - 2) + "][" + val + "]");
					}
					else if (currentLine == 9) {                            							// Set Date format
						formatter = new SimpleDateFormat(nextValue);
						break;
					}
					else if (currentLine == 10) {
						defaultPad = formatter.parse(nextValue);
						break;
					}
					else if (currentLine > 11) {                        									// Holidays
						Date thisDate = formatter.parse(nextValue);
						if (thisDate != null)
							holidays.add(thisDate);
						break;
					}
				}
				
				csv.close();
			}
			
			sc.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// Initialize the pre-determined values (possibly varies from year to year)
	private void initSchedule() {
		// Classes
		classes = new String[17];
		
		classes[ScheduleCalc.WEEKEND] = "Weekend";
		classes[ScheduleCalc.LAB_FREE] = "Lab Free";
		classes[ScheduleCalc.OFFICE_HOURS] = "Office Hours";
		classes[ScheduleCalc.BREAK] = "Break";
		classes[ScheduleCalc.ASSEMBLY] = "Assembly";
		classes[ScheduleCalc.ADV]= "ADV";
		classes[ScheduleCalc.FAC_PLC] = "FAC PLC";
		classes[ScheduleCalc.FAC_MTGS] = "FAC MTGS";
		classes[ScheduleCalc.ARTS_ADV] = "ARTS/ADV";
		classes[ScheduleCalc.UNSCHEDULED] = "Unscheduled";
		
		// Default class names
		for (int i = 1; i <= 7; i ++) {
			classes[i] = "Period " + i;
		}
	}
	
	// Returns the schedule of the [dateToSchedule] from a [pastADay] in the form of a Map<Block, Period>
	public HashMap<Integer, Integer> getSchedule(Date dateToSchedule, Date pastADay) {
		HashMap<Integer, Integer> schedule = new HashMap<Integer, Integer>();
		
		if (dateToSchedule.before(pastADay)) {
			for (int i = 0; i < 8; i ++) {
				schedule.put(i, ScheduleCalc.UNSCHEDULED);
			}
		}
		
		else {
			
		
			
			// Get the rotation day
			int rotDay = (weekdaysFactor(pastADay, dateToSchedule) % 7) + 1;
			
			// Get the day of the week
			Calendar c = GregorianCalendar.getInstance();
			c.setTime(dateToSchedule);
			int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
			
			
			if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {			// Check for weekend
				for (int i = 0; i < 8; i ++)
					schedule.put(i, ScheduleCalc.WEEKEND);
			}
			
			else if (holidays.contains(dateToSchedule)) {                        			// Check for holidays
				for (int i = 0; i < 8; i ++)
					schedule.put(i,  ScheduleCalc.UNSCHEDULED);
			}
			else {                                                               			// This was proper input
				
			
				// Determine what period the first block of this day should be
				int firstPeriod = ((4 * (rotDay - 1)) % 7) + 1;
				
				
				// Loop through each interjection block
				for (int i = 0; i < 4; i ++) {
					schedule.put(i, ScheduleCalc.INTERJECTIONS[i][dayOfWeek - 2]);
				}
	
				// Loop through each class block
				for (int i = 0; i < 4; i ++) {
					// Get this period based on the first block
					int thisPeriod = ((firstPeriod + i - 1) % 7) + 1;
					
					// Check if this is a lab free
					if (labFrees[thisPeriod - 1][rotDay - 1]) {
						thisPeriod = ScheduleCalc.LAB_FREE;
					}
					schedule.put(i + 4, thisPeriod);
				}
				
				
			}
		}
		
//		for (Map.Entry<Integer, Integer> entry : schedule.entrySet()) {
//			System.out.println("Block [" + entry.getKey() + "]: Period (" + entry.getValue() + "), " + classes[entry.getValue()]);
//		}
		
		return schedule;
	}
	
	// Get the number of days from [end] since [start] (excluding weekends and holidays)
	public int weekdaysFactor(Date start, Date end){
		int days = weekdays(start, end);
		
		Calendar c = GregorianCalendar.getInstance();
		for (Date d : holidays) {
			// Get the day of the week
			c.setTime(d);
			int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
			
			// If this holiday isn't a weekend
			if (dayOfWeek != Calendar.SUNDAY && dayOfWeek != Calendar.SATURDAY) {
				
				// And it applies to this query
				if ( (d.after(start) && d.before(end) ) || d.equals(start) || d.equals(end) ){
					days --;
				}
			}
		}
		
		return days;
	}
	
	// Get the number of days from [end] since [start] (excluding weekends)
	public int weekdays(Date start, Date end){
	    //Ignore argument check

	    Calendar c1 = GregorianCalendar.getInstance();
	    c1.setTime(start);
	    int w1 = c1.get(Calendar.DAY_OF_WEEK);
	    c1.add(Calendar.DAY_OF_WEEK, -w1 + 1);

	    Calendar c2 = GregorianCalendar.getInstance();
	    c2.setTime(end);
	    int w2 = c2.get(Calendar.DAY_OF_WEEK);
	    c2.add(Calendar.DAY_OF_WEEK, -w2 + 1);

	    //end Saturday to start Saturday 
	    long days = (c2.getTimeInMillis()-c1.getTimeInMillis())/(1000*60*60*24);
	    long daysWithoutSunday = days-(days*2/7);

	    if (w1 == Calendar.SUNDAY) {
	        w1 = Calendar.MONDAY;
	    }
	    if (w2 == Calendar.SUNDAY) {
	        w2 = Calendar.MONDAY;
	    }
	    return (int) (daysWithoutSunday-w1+w2);
	}
	
	public void export(String path) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(path));
			
			System.out.println("classes");
			// Write yo classes
			for (int i = 1; i <= 7; i ++) {
				String toWrite = classes[i];
				if (i < 7)
					toWrite += ",";
				bw.write(toWrite);
			}
			bw.newLine();
			
			// Write yo lab frees
			for (int i = 0; i < labFrees.length; i ++) {
				String toWrite = "";
				for (int n = 0; n < labFrees[i].length; n ++) {
					if (labFrees[i][n]) {
						toWrite += (char)((int)('A') + n) + ',';
					}
				}
				
				if (toWrite != "")
					toWrite = toWrite.substring(0, toWrite.length() - 1);
				
				bw.write(toWrite);
				bw.newLine();
			}
			
			// Our date pattern
			bw.write(formatter.toPattern());
			bw.newLine();
			
			// Our default past a day
			bw.write(formatter.format(defaultPad));
			bw.newLine();
			bw.newLine();
			
			// Holidays
			for (int i = 0; i < holidays.size(); i ++) {
				bw.write(formatter.format(holidays.get(i)));
				if (i < holidays.size() - 1)
					bw.newLine();
			}
			
			// Update
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// Getters
	public ArrayList<Date> getHolidays() {
		return holidays;
	}
	
	public boolean isHoliday(Date date) {
		return (holidays.contains(date));
	}
	
	public String getClass(int period) {
		return classes[period];
	}
	
	public Date getDefaultPad() {
		return defaultPad;
	}
}
