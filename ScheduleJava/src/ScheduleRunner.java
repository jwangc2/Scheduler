import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

public class ScheduleRunner extends JFrame{
	
	JLabel[][] displayLabels;
	
	public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	
            	// Test
            	SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
            	try {
            		ScheduleCalc sc = new ScheduleCalc(new File("./data/setup.csv"));
					Date pastADay = formatter.parse("08/15/2013");
					Date testDate = formatter.parse("01/10/2014");
            	
	            	// Run the gui
	            	ScheduleRunner sch = new ScheduleRunner("WMS Scheduler");
	            	sch.setVisible(true);
	            	sch.updateSchedule(sc, testDate, pastADay);
            	} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        });
    }
	
	public ScheduleRunner(String label){
		super(label);
		initComponents();
	}
	
	private void initComponents() {
        //Create and set up the window.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container container = getContentPane();
        container.setLayout(new FlowLayout(FlowLayout.LEADING, 4, 4));
        setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

        //Create the menu bar.  Make it have a green background.
        JMenuBar greenMenuBar = new JMenuBar();
        greenMenuBar.setOpaque(true);
        greenMenuBar.setBackground(new Color(154, 165, 127));
        greenMenuBar.setPreferredSize(new Dimension(900, 20));
        
        
        //room / object tabs
        JTabbedPane controlPane = new JTabbedPane();
        
        // Schedule Tab with BoxLayout (top to bottom)
        JPanel schedulePanel = new JPanel();
        schedulePanel.setLayout(new BoxLayout(schedulePanel, BoxLayout.Y_AXIS));
        schedulePanel.setPreferredSize(new Dimension(900, 640));
        
        // Input Panel
        JPanel inputPanel = new JPanel();
        inputPanel.setPreferredSize(new Dimension(900, 75));
        
        // Display Panel 
        JPanel gridPanel = createScheduleGrid(ScheduleCalc.TIMINGS);
        
        // Add the components
        schedulePanel.add(inputPanel);
        schedulePanel.add(gridPanel);
        
        // Add the Tab
        controlPane.addTab("Schedule", schedulePanel);
        
        //Set the menu bar and add the label to the content pane.
        setJMenuBar(greenMenuBar);
        container.add(controlPane, BorderLayout.CENTER);

        //Display the window.
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
    }
	
	public void updateSchedule(ScheduleCalc sc, Date dateToSchedule, Date pastADay) {
		
		// Set the calendar to the Monday of the dateToSchedule
		Calendar c = GregorianCalendar.getInstance();
		c.setTime(dateToSchedule);
		int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
		c.add(Calendar.DATE, (2 - dayOfWeek));
		
		// Monday thru Friday
		for (int i = 0; i < 5; i ++) {
			
			// Get the schedule for this day
			HashMap<Integer, Integer> sched = sc.getSchedule(c.getTime(), pastADay);
			for (Map.Entry<Integer, Integer> entry : sched.entrySet()) {
				int block = entry.getKey();
				int period = entry.getValue();
				
				String displayText = "<html><div style=\'text-align: center;\'>" + sc.getClass(period);
				if (block == 6) {
					displayText += "<br>and Lunch";
				}
				displayText += "<br>" + ScheduleCalc.TIMING_TEXT[block][i];
				displayText += "</div></html>";
				
				displayLabels[block][i].setText(displayText);
			}
			
			// Move onto the next day
			c.add(Calendar.DATE, 1);
		}
	}
	
	// Personalized grid pane
	private JPanel createScheduleGrid(BlockRange[][] blockRanges) {
		
		// Use the names from the schedule
		String[][] text = new String[blockRanges.length][blockRanges[0].length];
		for (int i = 0; i < text.length ; i ++) {
			for (int n = 0; n < text[i].length; n ++) {
				text[i][n] = "(" + (n + 1) + ", " + i + ")";
			}
		}
		
		// Use specific colors
		Color colFac = new Color(216, 228, 188);
		Color colOH = new Color(246, 196, 122);
		Color colBr = Color.white;
		Color colAs = new Color(197, 217, 241);
		Color colClass = new Color(249, 246, 181);
		
		Color[][] colors = new Color[blockRanges.length][blockRanges[0].length];
		for (int row = 0; row < blockRanges.length; row ++) {
			for (int col = 0; col < blockRanges[row].length; col ++) {
				if (row == 0) {
					// Faculty as default
					colors[row][col] = colFac;
					
					// Office hours as orange
					if (col == 0 || col == 4)
						colors[row][col] = colOH;
				}
				else if (row == 1) {
					// Break as white
					colors[row][col] = colBr;	
					
					// Assembly as blue
					if (col == 3)
						colors[row][col] = colAs;
				}
				else if (row == 2) {
					// Break as white
					colors[row][col] = colBr;	
					
					// Advisement as blue
					if (col == 0 || col == 1)
						colors[row][col] = colAs;
				}
				else if (row == 3) {
					// Office hours as orange
					colors[row][col] = colOH;
					
					// Weekend as white
					if (col == 4)
						colors[row][col] = colBr;
				}
				else {
					colors[row][col] = colClass;
				}
				
			}
		}
		
		displayLabels = new JLabel[blockRanges.length][blockRanges[0].length];
		
		return createGridBagPane(blockRanges, ScheduleCalc.TIMING_TEXT, colors, ScheduleCalc.TIMING_ROWS, displayLabels);
	}
	
	// Default grid pane
	private JPanel createGridBagPane(BlockRange[][] blockRanges) {
		
		// Use the positions as text
		String[][] text = new String[blockRanges.length][blockRanges[0].length];
		for (int i = 0; i < text.length ; i ++) {
			for (int n = 0; n < text[i].length; n ++) {
				text[i][n] = "(" + (n + 1) + ", " + i + ")";
			}
		}
		
		// Use all cyan
		Color[][] colors = new Color[blockRanges.length][blockRanges[0].length];
		for (Color[] row : colors)
			Arrays.fill(row, Color.cyan);
		
		return createGridBagPane(blockRanges, text, colors, new String[maxGridBagRows(blockRanges)], new JLabel[blockRanges.length][blockRanges[0].length]);
	}
	
	private int maxGridBagRows(BlockRange[][] blockRanges) {
		int maxRow = 0;
		
		for (BlockRange[] br : blockRanges) {
			for (BlockRange b : br) {
				if (b.end > maxRow)
					maxRow = b.end;
			}
		}
		
		return maxRow;
	}
	
	// General grid pane creator
	private JPanel createGridBagPane(BlockRange[][] blockRanges, String[][] text, Color[][] colors, String[] sideText, JLabel[][] references) {
		
		Border blackLines = BorderFactory.createLineBorder(Color.black);
		
		// Panel and Layout
		JPanel gridBagPane = new JPanel();
		gridBagPane.setLayout(new GridBagLayout());
		
		// Parse
		GridBagConstraints c = new GridBagConstraints();
		int maxRow = 0;
		for (int row = 0; row < blockRanges.length; row ++) {
			for (int col = 0; col < blockRanges[row].length; col ++) {
				// Get the value
				BlockRange b = blockRanges[row][col];
				
				if (b != null) {
					// Apply settings
					c.gridwidth = 1;
					c.gridheight = 1;
					
					c.fill = GridBagConstraints.BOTH;
					c.weightx = 0.5;
					c.weighty = 1.0;
					c.gridx = col + 1;
					c.gridy = b.start;
					c.gridheight = (b.end - b.start);
					c.ipady = b.pad;
					
					// Determine the max row (so we can create individual ones for height requests)
					if (maxRow < b.end)
						maxRow = b.end;
					
					// Remainder constraints
					if (col == blockRanges[row].length - 1)
						c.gridwidth = GridBagConstraints.REMAINDER;
					
					JLabel thisLabel = new JLabel(text[row][col]);
					thisLabel.setHorizontalAlignment(SwingConstants.CENTER);
					thisLabel.setBackground(colors[row][col]);
					thisLabel.setBorder(blackLines);
					thisLabel.setOpaque(true);

					gridBagPane.add(thisLabel, c);
					references[row][col] = thisLabel;
					
				}
			}
		}
		
		Border padding = BorderFactory.createEmptyBorder(0, 10, 0, 10);
		
		// Append rows so that height requests are accepted
		for (int i = 0; i < maxRow; i ++) {
			// Create the label
			JLabel thisLabel = new JLabel(sideText[i]);
			thisLabel.setBackground(Color.white);
			thisLabel.setBorder(BorderFactory.createCompoundBorder(blackLines, padding));
			thisLabel.setOpaque(true);
			
			// Basic 1 x 1 Cells
			c.gridwidth = 1;
			c.gridheight = 1;
			
			c.fill = GridBagConstraints.BOTH;
			c.weightx = 0.1;
			c.weighty = 0.5;
			c.gridx = 0;
			c.gridy = i;
			
			//JButton button = new JButton("B" + i);
			gridBagPane.add(thisLabel, c);
		}
		
		return gridBagPane;
	}
}
