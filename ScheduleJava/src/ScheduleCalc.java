import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
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
		
		{new BlockRange(1, 2, 30), new BlockRange(0, 2), new BlockRange(0, 3), new BlockRange(0, 2), new BlockRange(1, 2, 30)},
		{new BlockRange(4, 5), new BlockRange(4, 5), new BlockRange(7, 8), new BlockRange(4, 8), new BlockRange(4, 5)},
		{new BlockRange(14, 17), new BlockRange(14, 17), new BlockRange(16, 17), new BlockRange(16, 17), new BlockRange(14, 15)},
		{new BlockRange(20, 21, 30), new BlockRange(20, 21, 30), new BlockRange(20, 21, 30), new BlockRange(20, 21, 30), new BlockRange(19, 21)},
		
		{new BlockRange(2, 4), new BlockRange(2, 4), new BlockRange(3, 7), new BlockRange(2, 4), new BlockRange(2, 4)},
		{new BlockRange(5, 10), new BlockRange(5, 10), new BlockRange(8, 11), new BlockRange(8, 11), new BlockRange(5, 10)},
		{new BlockRange(10, 14), new BlockRange(10, 14), new BlockRange(11, 16), new BlockRange(11, 16), new BlockRange(10, 14)},
		{new BlockRange(17, 20), new BlockRange(17, 20), new BlockRange(17, 20), new BlockRange(17, 20), new BlockRange(15, 19)}
		
		//{new BlockRange(0, 1), null, null, null, new BlockRange(0, 1)}
	};
	
	public final static String[][] TIMING_TEXT = {
		{"(8:00-8:30)", "(7:45-8:30)", "(7:45-9:00)", "(7:45-8:30)", "(8:00-8:30)"},
		{"(9:40-9:55)", "(9:40-9:55)", "(10:10-10:25)", "(9:40-10:25)", "(9:40-9:55)"},
		{"(1:10-1:50)", "(1:10-1:50)", "(1:40-1:50)", "(1:40-1:50)", "(1:10-1:20)"},
		{"(3:00-3:30)", "(3:00-3:30)", "(3:00-3:30)", "(3:00-3:30)", "(0:00)"},
		
		{"(8:30-9:40)", "(8:30-9:40)", "(9:00-10:10)", "(8:30-9:40)", "(8:30-9:40)"},
		{"(9:55-11:05)", "(9:55-11:05)", "(10:25-11:35)", "(10:25-11:35)", "(9:55-11:05)"},
		{"(11:05-1:10)", "(11:05-1:10)", "(11:35-1:40)", "(11:35-1:40)", "(11:05-1:10)"},
		{"(1:50-3:00)", "(1:50-3:00)", "(1:50-3:00)", "(1:50-3:00)", "(1:20-2:30)"}
	};
	
	public final static String[] TIMING_ROWS = {
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
	
	public ScheduleCalc() {
		initSchedule();
		
		labFrees = new boolean[7][7];
		holidays = new ArrayList<Date>();
	}
	
	public ScheduleCalc(File setup) {
		// Ensure that the members are initiated
		this();
		
		// Try to load and parse
		try {
			// Default date format
			SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");;
			
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
					else if (currentLine > 9) {                        									// Holidays
						Date thisDate = formatter.parse(nextValue);
						holidays.add(thisDate);
						break;
					}
				}
				
				csv.close();
			}
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
		
		Date dateToUse = dateToSchedule;
		if (dateToSchedule.before(pastADay))
			dateToUse = pastADay;
		
		// Get the rotation day
		int rotDay = (weekdaysFactor(pastADay, dateToUse) % 7) + 1;
		
		// Get the day of the week
		Calendar c = GregorianCalendar.getInstance();
		c.setTime(dateToUse);
		int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
		
		
		if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {			// Check for weekend
			for (int i = 0; i < 8; i ++)
				schedule.put(i, ScheduleCalc.WEEKEND);
		}
		
		else if (holidays.contains(dateToUse)) {                        			// Check for holidays
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
				System.out.println("Period: " + thisPeriod + " | Rot: " + rotDay);
				if (labFrees[thisPeriod - 1][rotDay - 1]) {
					thisPeriod = ScheduleCalc.LAB_FREE;
				}
				schedule.put(i + 4, thisPeriod);
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
	
	// Getters
	public ArrayList<Date> getHolidays() {
		return holidays;
	}
	
	public String getClass(int period) {
		return classes[period];
	}
}
