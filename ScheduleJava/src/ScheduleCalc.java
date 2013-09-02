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
	
	// static values
	public static int WEEKEND = 0;
	public static int LAB_FREE = 8;
	public static int OFFICE_HOURS = 9;
	public static int BREAK = 10;
	public static int ASSEMBLY = 11;
	public static int ADV = 12;
	public static int FAC_PLC = 13;
	public static int FAC_MTGS = 14;
	public static int ARTS_ADV = 15;
	public static int UNSCHEDULED = 16;
	
	// members
	String[] classes;
	int[][] interjections;
	boolean[][] labFrees;
	public ArrayList<Date> holidays;
	
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
		
		// Interjections by blocks
		int[][] layout = {
				{ScheduleCalc.OFFICE_HOURS, ScheduleCalc.FAC_PLC, ScheduleCalc.FAC_MTGS, ScheduleCalc.FAC_PLC, ScheduleCalc.OFFICE_HOURS},
				{ScheduleCalc.BREAK, ScheduleCalc.BREAK, ScheduleCalc.BREAK, ScheduleCalc.ASSEMBLY, ScheduleCalc.ADV},
				{ScheduleCalc.ARTS_ADV, ScheduleCalc.ARTS_ADV, ScheduleCalc.BREAK, ScheduleCalc.BREAK, ScheduleCalc.BREAK},
				{ScheduleCalc.OFFICE_HOURS, ScheduleCalc.OFFICE_HOURS, ScheduleCalc.OFFICE_HOURS, ScheduleCalc.OFFICE_HOURS, ScheduleCalc.WEEKEND}
		};
		
		interjections = layout;
		
	}
	
	// Returns the schedule of the [dateToSchedule] from a [pastADay] in the form of a Map<Block, Period>
	public HashMap<Integer, Integer> getSchedule(Date dateToSchedule, Date pastADay) {
		HashMap<Integer, Integer> schedule = new HashMap<Integer, Integer>();
		
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
			
			// Loop through each class block
			for (int i = 0; i < 4; i ++) {
				// Get this period based on the first block
				int thisPeriod = ((firstPeriod + i - 1) % 7) + 1;
				
				// Check if this is a lab free
				if (labFrees[thisPeriod - 1][rotDay - 1]) {
					thisPeriod = ScheduleCalc.LAB_FREE;
				}
				schedule.put(i, thisPeriod);
			}
			
			// Loop through each interjection block
			for (int i = 4; i < 8; i ++) {
				schedule.put(i, interjections[i - 4][dayOfWeek - 2]);
			}
		}
		
		for (Map.Entry<Integer, Integer> entry : schedule.entrySet()) {
			System.out.println("Block [" + entry.getKey() + "]: Period (" + entry.getValue() + "), " + classes[entry.getValue()]);
		}
		
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
}
