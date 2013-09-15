import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
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
	
	JFormattedTextField[] classFields;
	String[] classNames;
	
	JCheckBox[][] labCheckBoxes;
	
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
	
	private final String fileDir = "./schedule_data/";
	private final String setupFname = "setup.csv";
	private final String holidayFname = "holidays.csv";
	
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
		
		// Create a clean ScheduleCalc and define the default directory
		sc = new ScheduleCalc();
		
		// Load the setup file
		try {
			sc.loadSetup(new File(fileDir + setupFname));
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(this, "Cannot find the [" + setupFname + "] file.");
		} catch (ParseException e) {
			JOptionPane.showMessageDialog(this, "Corrupted [" + setupFname + "] file.");
		}
		
		// Load the holidays file
		try {
			sc.loadHolidays(new File(fileDir + holidayFname));
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(this, "Cannot find the [" + holidayFname + "] file.");
		} catch (ParseException e) {
			JOptionPane.showMessageDialog(this, "Corrupted [" + holidayFname + "] file.");
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
        int tabWidth = 900;
        int tabHeight = 640;
        
        dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        
        // Setup Tab with BoxLayout (left to right)
        JPanel setupPanel = new JPanel();
        setupPanel.setLayout(new GridBagLayout());
        
        {
	        GridBagConstraints c = new GridBagConstraints();
	        
	        // Add the components
	        // Class Name Input
	        c.gridx = 0;
	        c.gridy = 0;
	        c.gridwidth = 2;
	        c.gridheight = 1;
	        c.weighty = 0.0;
	        c.anchor = GridBagConstraints.PAGE_START;
	        JPanel classInputPane = initClassInputPane(new Dimension(tabWidth / 2, 0), 24, 12, 4, 20);
	        setupPanel.add(classInputPane, c);
	        
	        // -PAD Input-
	        
	        // >Add the JLabel
	        c.gridx = 0;
	        c.gridy = 1;
	        c.gridwidth = 1;
	        c.gridheight = 1;
	        c.weightx = 0.0;
	        c.weighty = 0.0;
	        c.anchor = GridBagConstraints.LINE_START;
	        JLabel testLabel = new JLabel("Past 'A' Day: ");
	        setupPanel.add(testLabel, c);
	       
	        // >Add the Text Field
	        c.gridx = 1;
	        c.gridy = 1;
	        c.gridwidth = 1;
	        c.gridheight = 1;
	        c.weightx = 0.0;
	        c.weighty = 0.0;
	        c.anchor = GridBagConstraints.LINE_END;
	        padField = new JFormattedTextField(dateFormat);
	        pastADay = sc.getDefaultPad();
			padField.setValue(pastADay);
			padField.setColumns(10);
			setupPanel.add(padField, c);
			
			// -Apply Button-
			c.gridx = 0;
			c.gridy = 2;
			c.gridwidth = 2;
			c.gridheight = 1;
			c.weightx = 0.0;
			c.weighty = 0.0;
			c.anchor = GridBagConstraints.PAGE_END;
			c.fill = GridBagConstraints.HORIZONTAL;
			JButton applyButton = new JButton("Apply Settings");
			applyButton.addActionListener(new ActionListener() {
	 
	            public void actionPerformed(ActionEvent e) {
	            	// Set PAD
	            	pastADay = (Date)(padField.getValue());
	            	sc.setDefaultPad(pastADay);
	            	
	            	// Set class names
	            	for (int i = 0; i < 7; i ++) {
	            		sc.setClass(i + 1, classFields[i].getText());
	            	}
	            	
	            	// Set lab frees
	            	for (int row = 0; row < labCheckBoxes.length; row ++) {
	            		for (int col = 0; col < labCheckBoxes[row].length; col ++) {
	            			sc.setLabFree(row + 1, col + 1, labCheckBoxes[row][col].isSelected());
	            		}
	            	}
	            	
	            	// Update
	                updateSchedule(sc, dateToSchedule, pastADay);
	                
	                // Export
	                exportSettings();
	            }
	
	        });  
			setupPanel.add(applyButton, c);
	        
	        // -Lab-Free Input-
	        c.gridx = 2;
	        c.gridy = 0;
	        c.gridwidth = 1;
	        c.gridheight = 3;
	        c.weightx = 0.0;
	        c.weighty = 0.0;
	        c.anchor = GridBagConstraints.FIRST_LINE_END;
	        c.fill = GridBagConstraints.NONE;
	        JPanel labInputPane = initLabInputPane(new Dimension((int)(tabWidth / 2), 0), 33, 10);
	        setupPanel.add(labInputPane, c);
	        
	        // -Bottom Spacer-
	        c.gridx = 0;
	        c.gridy = 3;
	        c.gridwidth = 3;
	        c.gridheight = 1;
	        c.weightx = 1.0;
	        c.weighty = 1.0;
	        c.anchor = GridBagConstraints.PAGE_START;
	        c.fill = GridBagConstraints.BOTH;
	        JPanel spacerPane = new JPanel();
	        setupPanel.add(spacerPane, c);
	
	        int padding = 75;
	        setupPanel.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
        }
        
        // Schedule Tab with BoxLayout (top to bottom)
        JPanel schedulePanel = new JPanel();
        schedulePanel.setLayout(new BoxLayout(schedulePanel, BoxLayout.Y_AXIS));
        schedulePanel.setPreferredSize(new Dimension(tabWidth, tabHeight));
        
        {
	        // Input Panel
	        JPanel inputPanel = createInputPane(tabWidth);
	        
	        // Display Panel 
	        JPanel gridPanel = createScheduleGrid(ScheduleCalc.TIMINGS);
	        gridPanel.setPreferredSize(new Dimension(tabWidth, (int)(0.9 * tabHeight)));
	        
	        // Add the components
	        schedulePanel.add(inputPanel);
	        schedulePanel.add(gridPanel);
        }
        
        // Add the Tabs
        controlPane.addTab("Setup", setupPanel);
        controlPane.addTab("Schedule", schedulePanel);
        
        //Set the menu bar and add the label to the content pane.
        container.add(controlPane, BorderLayout.CENTER);

        //Display the window.
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
    }
	
	private JPanel initClassInputPane(Dimension area, int fieldHeight, int hgap, int vgap, int padding) {
		
		// Base Panel that we will return
		JPanel classInputPane = new JPanel();
		classInputPane.setLayout(new GridLayout(7, 1, 0, vgap));
		
		// Initialize the ScheduleRunner's field references
		classFields = new JFormattedTextField[7];
		classNames = new String[7];
		
		// One pair for each period
		for (int i = 0; i < 7; i ++) {
			// Base Panel for this pair
			JPanel overBox = new JPanel();
			overBox.setLayout(new BoxLayout(overBox, BoxLayout.X_AXIS));
			
			// JLabel with text
			JLabel periodLabel = new JLabel("Period " + (i + 1) + ":");
			overBox.add(periodLabel);
			
			overBox.add(Box.createRigidArea(new Dimension(hgap, 2)));
			
			// JFormattedTextField with left-over width
			classFields[i] = new JFormattedTextField();
			classFields[i].setText(sc.getClass(i + 1));
			classFields[i].setColumns(15);
			classFields[i].addPropertyChangeListener("value", this);
			overBox.add(classFields[i]);
			
			// Add this pair to the input pane
			classInputPane.add(overBox);
		}
		
		// Padding, border, alignment
		Border padBorder = BorderFactory.createEmptyBorder(0, 0, padding, 0);
		//padBorder = BorderFactory.createCompoundBorder(blackLines, padBorder);
		classInputPane.setBorder(padBorder);
		
		return classInputPane;
	}
	
	private JPanel initLabInputPane(Dimension area, int fieldHeight, int padding) {
		
		// Base Panel that we will return
		JPanel labInputPane = new JPanel();
		labInputPane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		// Set proper dimensioning
		Dimension preferredArea;
		int preferredWidth = (int)(area.getWidth());
		int preferredHeight = (int)(area.getHeight());
		
		if (fieldHeight > 0) {
			preferredHeight = (fieldHeight * 7);
		}
		
		preferredWidth += padding * 2;
		preferredHeight += padding * 2;
		
		preferredArea = new Dimension(preferredWidth, preferredHeight);
		labInputPane.setPreferredSize(preferredArea);
		
		// Initialize the field references
		labCheckBoxes = new JCheckBox[7][7];
		
		// Add our content
		// i < (1 + 7) means i < (top row + 7 periods)
		int maxRow = 1 + 7;
		int maxCol = 1 + 7;
		for (int row = 0; row < maxRow; row ++) {
			// n < (1 + 7) means n < (left column + 7 letter days)
			for (int col = 0; col < maxCol; col ++) {
				// Set the position of this cell
				c.gridx = col;
				c.gridy = row;
				
				// Default constraints
				c.gridwidth = 1;
				c.gridheight = 1;
				c.fill = GridBagConstraints.BOTH;
				c.weightx = 0.5;
				c.weighty = 0.5;
				
				// Custom constraints
				if (col == 0)
					c.weightx = 0.2;
				
				// End rows or columns
				if (row == maxRow - 1) 
					c.gridheight = GridBagConstraints.REMAINDER;
				
				if (col == maxCol - 1)
					c.gridwidth = GridBagConstraints.REMAINDER;
				
				// Create the JComponent that will represent this cell
				JComponent cell;
				
				// Create a label or a jpanel
				if (col == 0) {
					// Periods Label (Left Column)
					String cellText = "-";
					if (row > 0)
						cellText = " P." + row + " ";
					cell = new JLabel(cellText, SwingConstants.CENTER);
				} 
				else if (row == 0) {
					// Letter Day Label (Top Row)
					char letDay = (char)((int)('A') + (col - 1));
					cell = new JLabel("" + letDay, SwingConstants.CENTER);
				} 
				else {
					cell = new JPanel();
					
					// Checkbox (Everything else)
					JCheckBox thisCheckBox = new JCheckBox();
					thisCheckBox.setSelected(sc.isLabFree(row, col));
					
					cell.add(thisCheckBox, BorderLayout.CENTER);
					labCheckBoxes[row - 1][col - 1] = thisCheckBox;
				}
				
				// Bordering
				cell.setBorder(blackLines);
				
				// Add the cell panel to the input pane
				labInputPane.add(cell, c);
			}
		}
		
		// Border
		Border padBorder = BorderFactory.createEmptyBorder(0, padding, 0, 0);
		//padBorder = BorderFactory.createCompoundBorder(blackLines, padBorder);
		labInputPane.setBorder(padBorder);
		
		return labInputPane;
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
		
		c.insets = new Insets(1, 2, 1, 2);
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.0;
		c.weighty = 0.0;
		
		// x = 0
		c.gridx = 0;
		c.gridy = 0;
		inputPane.add(new JLabel("Date to Schedule:"), c);
		
		c.gridx = 0;
		c.gridy = 1;
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
		c.gridy = 1;
		c.gridwidth = 1;
		inputPane.add(prevDayButton, c);
		
		// Date Field
		dateField = new JFormattedTextField(dateFormat);
	
		dateToSchedule = ScheduleCalc.getZeroedDate(new Date());
		dateField.setValue(dateToSchedule);
		dateField.setColumns(10);
		dateField.addPropertyChangeListener("value", this);
		c.gridx = 1;
		c.gridy = 0;
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
		c.gridy = 1;
		c.gridwidth = 1;
		inputPane.add(nextDayButton, c);
		
		// x = 3
		c.gridx = 3;
		c.gridy = 0;
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
		c.gridy = 1;
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
	
	private void exportSettings() {
		try {
			sc.export(fileDir, setupFname, holidayFname);
			JOptionPane.showMessageDialog(this, "Your settings have been saved.");
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Failed to export.");
		}
	}
}
