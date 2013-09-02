import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class ScheduleRunner extends JFrame{
	
	public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
            	try {
            		ScheduleCalc sc = new ScheduleCalc(new File("./data/setup.csv"));
					Date pastADay = formatter.parse("08/15/2013");
					Date testDate = formatter.parse("01/10/2014");
					HashMap<Integer, Integer> mySchedule = sc.getSchedule(testDate, pastADay);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	
            	
            	ScheduleRunner sch = new ScheduleRunner("WMS Scheduler");
            	sch.setVisible(true);
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
        greenMenuBar.setPreferredSize(new Dimension(800, 20));
        
        
        //room / object tabs
        JTabbedPane controlPane = new JTabbedPane();
        
        JPanel schedulePanel = new JPanel();
        schedulePanel.setPreferredSize(new Dimension(800, 600));
        
        controlPane.addTab("Schedule", schedulePanel);
        
        //Set the menu bar and add the label to the content pane.
        setJMenuBar(greenMenuBar);
        container.add(controlPane, BorderLayout.CENTER);

        //Display the window.
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
    }
}
