import java.lang.reflect.InvocationTargetException;

import javax.swing.JApplet;

public class ScheduleApplet extends JApplet {
	public void init() {
		try {
			javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
			    public void run() {
			        createGUI();
				} 
			    
			});
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void createGUI() {
        //Create and set up the content pane.
        SchedulePanel newContentPane = new SchedulePanel(null);
        newContentPane.setOpaque(true); 
        setContentPane(newContentPane);        
    }  
}