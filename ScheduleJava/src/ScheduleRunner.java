import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.FlowLayout;

import javax.swing.JFrame;

public class ScheduleRunner extends JFrame {
	public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
    			// Run the gui
            	ScheduleRunner sr = new ScheduleRunner();
            	sr.createGUI();
        	} 
            
        });
    }
	
	private void createGUI() {
		//Create and set up the window.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container container = getContentPane();
        container.setLayout(new FlowLayout(FlowLayout.LEADING, 4, 4));
        setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        
        SchedulePanel sch = new SchedulePanel(this);
        
        //Set the menu bar and add the label to the content pane.
        container.add(sch, BorderLayout.CENTER);

        //Display the window.
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        
        setVisible(true);
	}
}
