import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

public class ScheduleRunner extends JFrame implements PropertyChangeListener{
	
	JLabel[][] displayLabels;
	
	SimpleDateFormat dateFormat;
	JFormattedTextField dateField;
	JFormattedTextField padField;
	Date dateToSchedule;
	Date pastADay;
	
	ScheduleCalc sc;
	
	Border blackLines = BorderFactory.createLineBorder(Color.black);
	Border hiliteLines = BorderFactory.createLineBorder(new Color(224, 42, 42));
	Border labelPadding = BorderFactory.createEmptyBorder(0, 10, 0, 10);
	Border blackPaddedLines = BorderFactory.createCompoundBorder(blackLines, labelPadding);
	Border hilitePaddedLines = BorderFactory.createCompoundBorder(hiliteLines, labelPadding);
	
	// Use specific colors
	private Color colFac = new Color(216, 228, 188);
	private Color colOH = new Color(246, 196, 122);
	private Color colBr = Color.white;
	private Color colAs = new Color(197, 217, 241);
	private Color colClass = new Color(249, 246, 181);
	private Color colLabFree = new Color(255, 214, 178);
	private Color colUnsched = new Color(240, 240, 240);
	
	private Color[] colorScheme = {
			colBr, 
			colClass, colClass, colClass, colClass, colClass, colClass, colClass,
			colLabFree, colOH, colBr, colAs, colBr,
			colFac, colFac,
			colAs, colUnsched
	};
	
	public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
    			// Run the gui
            	ScheduleRunner sch = new ScheduleRunner("WMS Scheduler");
            	sch.setVisible(true);
	            
        	} 
            
        });
    }
	
	public ScheduleRunner(String label){
		super(label);
		
		// Attempt to load the setup file. Otherwise use the broken default one.
		try {
			sc = new ScheduleCalc(new File("./schedule_data/setup.csv"));
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(this, "You have no setup file!\nYou might be missing unscheduled days!");
			sc = new ScheduleCalc();
		} catch (ParseException e) {
			JOptionPane.showMessageDialog(this, "Corrupted setup file. Get a new one.");
			sc = new ScheduleCalc();
		}
		
		// Create our components and add them to this frame
		initComponents();
		
		// Get a schedule on the board
		updateSchedule(sc, dateToSchedule, pastADay);
	}
	
	private void initComponents() {
        //Create and set up the window.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container container = getContentPane();
        container.setLayout(new FlowLayout(FlowLayout.LEADING, 4, 4));
        setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        
        //room / object tabs
        JTabbedPane controlPane = new JTabbedPane();
        
        // Schedule Tab with BoxLayout (top to bottom)
        JPanel schedulePanel = new JPanel();
        schedulePanel.setLayout(new BoxLayout(schedulePanel, BoxLayout.Y_AXIS));
        schedulePanel.setPreferredSize(new Dimension(900, 640));
        
        // Input Panel
        JPanel inputPanel = createInputPane(900);
        
        // Display Panel 
        JPanel gridPanel = createScheduleGrid(ScheduleCalc.TIMINGS);
        gridPanel.setPreferredSize(new Dimension(900, 500));
        
        // Add the components
        schedulePanel.add(inputPanel);
        schedulePanel.add(gridPanel);
        
        // Add the Tab
        controlPane.addTab("Schedule", schedulePanel);
        
        //Set the menu bar and add the label to the content pane.
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
		    
		    Date thisDate = ScheduleCalc.getZeroedDate(c.getTime());
			
			// Get the schedule for this day
			HashMap<Integer, Integer> sched = sc.getSchedule(thisDate, pastADay);
			for (Map.Entry<Integer, Integer> entry : sched.entrySet()) {
				int block = entry.getKey();
				int period = entry.getValue();
				
				// Change the label text
				String displayText = "<html><div style=\'text-align: center;\'>" + sc.getClass(period);
				if (period != ScheduleCalc.WEEKEND && period != ScheduleCalc.UNSCHEDULED) {
					if (block == 6) {
						displayText += "<br>and Lunch";
					}
					if (period != ScheduleCalc.BREAK && period != ScheduleCalc.ADV) {
						displayText += "<br>" + ScheduleCalc.TIMING_TEXT[block][i];
					}
				}
				displayText += "</div></html>";
				
				displayLabels[block][i].setText(displayText);
				
				// Change the label color
				Color labelColor = colorScheme[period];
				
				if (!thisDate.before(dateToSchedule) && !thisDate.after(dateToSchedule)) {
					float[] hsv = new float[3];
					Color.RGBtoHSB(labelColor.getRed(), labelColor.getGreen(), labelColor.getBlue(), hsv);
					if (hsv[1] > 0.01)
						hsv[1] += 0.3;
					labelColor = Color.getHSBColor(hsv[0], hsv[1], hsv[2]);
				}
				
				displayLabels[block][i].setBackground(labelColor);
			}
			
			// Date
			displayLabels[9][i].setText(dateFormat.format(c.getTime()));
			
			// Letter Day
			int rotDay = (sc.weekdaysFactor(pastADay, c.getTime()) % 7) + 1;
			String letDay = "-";
			if (!c.getTime().before(pastADay) && !sc.isHoliday(c.getTime()))
				letDay = "" + (char)((int)('A') + (rotDay - 1));
			
			displayLabels[10][i].setText(letDay);
			
			// Move onto the next day
			c.add(Calendar.DATE, 1);
		}
	}
	
	private JPanel createInputPane(int width) {
		// Base Pane
		JPanel inputPane = new JPanel();
		inputPane.setLayout(new GridBagLayout());
		inputPane.setPreferredSize(new Dimension(width, 80));
		
		// Parse
		GridBagConstraints c = new GridBagConstraints();
		dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		
		c.insets = new Insets(1, 2, 1, 2);
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.0;
		c.weighty = 0.0;
		
		// x = 0
		c.gridx = 0;
		c.gridy = 0;
		inputPane.add(new JLabel("Past A Day:"), c);
		
		c.gridx = 0;
		c.gridy = 1;
		inputPane.add(new JLabel("Date to Schedule:"), c);
		
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		JButton prevWeekButton = new JButton("Prev. Week");
		prevWeekButton.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e) {
            	Calendar cal = GregorianCalendar.getInstance();
            	cal.setTime(dateToSchedule);
            	cal.add(Calendar.DATE, -7);
            	dateToSchedule = cal.getTime();
                dateField.setValue(dateToSchedule);
                updateSchedule(sc, dateToSchedule, pastADay);
            }

        });  
		inputPane.add(prevWeekButton, c);
	
		
		c.gridheight = 1;
		// x = 1
		// PAD Field
		padField = new JFormattedTextField(dateFormat);
		pastADay = sc.getDefaultPad();
		padField.setValue(pastADay);
	
		padField.setColumns(10);
		padField.addPropertyChangeListener("value", this);
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 2;
		inputPane.add(padField, c);
		
		JButton prevDayButton = new JButton("Prev. Day");
		prevDayButton.addActionListener(new ActionListener() {
			 
            public void actionPerformed(ActionEvent e) {
            	Calendar cal = GregorianCalendar.getInstance();
            	cal.setTime(dateToSchedule);
            	cal.add(Calendar.DATE, -1);
            	dateToSchedule = cal.getTime();
                dateField.setValue(dateToSchedule);
                updateSchedule(sc, dateToSchedule, pastADay);
            }

        });  
		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 1;
		inputPane.add(prevDayButton, c);
		
		// Date Field
		dateField = new JFormattedTextField(dateFormat);
	
		dateToSchedule = ScheduleCalc.getZeroedDate(new Date());
		dateField.setValue(dateToSchedule);
		dateField.setColumns(10);
		dateField.addPropertyChangeListener("value", this);
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 2;
		inputPane.add(dateField, c);
		
	
		
		c.gridheight = 1;
		// x = 2
		JButton nextDayButton = new JButton("Next Day");
		nextDayButton.addActionListener(new ActionListener() {
			 
            public void actionPerformed(ActionEvent e) {
            	Calendar cal = GregorianCalendar.getInstance();
            	cal.setTime(dateToSchedule);
            	cal.add(Calendar.DATE, 1);
            	dateToSchedule = cal.getTime();
                dateField.setValue(dateToSchedule);
                updateSchedule(sc, dateToSchedule, pastADay);
            }

        });  
		c.gridx = 2;
		c.gridy = 2;
		c.gridwidth = 1;
		inputPane.add(nextDayButton, c);
		
		// x = 3
		c.gridx = 3;
		c.gridy = 0;
		c.gridwidth = 1;
		JButton defaultButton = new JButton("Default");
		defaultButton.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e) {
            	try {
					pastADay = dateFormat.parse("08/15/2013");
					
					padField.setValue(pastADay);
	                updateSchedule(sc, dateToSchedule, pastADay);
				} catch (ParseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }

        });  
		inputPane.add(defaultButton, c);
		
		c.gridx = 3;
		c.gridy = 1;
		c.gridwidth = 1;
		JButton currentButton = new JButton("Today");
		currentButton.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e) {
				dateToSchedule = ScheduleCalc.getZeroedDate(new Date());
				dateField.setValue(dateToSchedule);
                updateSchedule(sc, dateToSchedule, pastADay);
            }

        });  
		inputPane.add(currentButton, c);
		
		c.gridx = 3;
		c.gridy = 2;
		c.gridwidth = 1;
		JButton nextWeekButton = new JButton("Next Week");
		nextWeekButton.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e) {
            	Calendar cal = GregorianCalendar.getInstance();
            	cal.setTime(dateToSchedule);
            	cal.add(Calendar.DATE, 7);
            	dateToSchedule = cal.getTime();
                dateField.setValue(dateToSchedule);
                updateSchedule(sc, dateToSchedule, pastADay);
            }

        });  
		inputPane.add(nextWeekButton, c);
		
		
		inputPane.setBorder(BorderFactory.createLineBorder(Color.black));

		return inputPane;
	}
	
	public void propertyChange(PropertyChangeEvent e) {
        Object source = e.getSource();
        if (source == dateField) {
            dateToSchedule = ScheduleCalc.getZeroedDate((Date)(dateField.getValue()));
        } else if (source == padField) {
        	pastADay = ScheduleCalc.getZeroedDate((Date)(padField.getValue()));
        }

        updateSchedule(sc, dateToSchedule, pastADay);
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
		
		Color[][] colors = new Color[blockRanges.length][blockRanges[0].length];
		for (int row = 0; row < blockRanges.length; row ++) {
			for (int col = 0; col < blockRanges[row].length; col ++) {
				colors[row][col] = Color.white;
				if (row == 11)
					colors[row][col] = colUnsched;
			}
		}
		
		displayLabels = new JLabel[blockRanges.length][blockRanges[0].length];
		
		return createGridBagPane(blockRanges, ScheduleCalc.TIMING_TEXT, colors, ScheduleCalc.TIMING_ROWS, displayLabels);
	}

	// General grid pane creator
	private JPanel createGridBagPane(BlockRange[][] blockRanges, String[][] text, Color[][] colors, String[] sideText, JLabel[][] references) {
		
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
		
		
		
		// Append rows so that height requests are accepted
		for (int i = 0; i < maxRow; i ++) {
			// Create the label
			JLabel thisLabel = new JLabel(sideText[i]);
			thisLabel.setBackground(Color.white);
			thisLabel.setBorder(BorderFactory.createCompoundBorder(blackLines, labelPadding));
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
