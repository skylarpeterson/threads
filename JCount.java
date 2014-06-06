import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;


public class JCount extends JPanel{
	
	private class Counter extends Thread {
		
		private JLabel label;
		private int countMax;
		private int count;
		
		public Counter(int countMax, JLabel label){
			this.countMax = countMax;
			this.label = label;
		}
		
		@Override
		public void run(){
			for(count = 0; count < countMax; count++){
				if(count % 10000 == 0 || count == countMax - 1) {
					try {
						Thread.sleep(100);
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								label.setText(Integer.toString(count));
							}
						});
					} catch (InterruptedException e) {
						break;
					}	
				}
			}
		}
	}
	
	public JPanel panel;
	private JTextField countTo;
	private JLabel countLabel;
	private JButton startButton;
	private JButton stopButton;
	private Counter counter;
	
	public JCount(){
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		countTo = new JTextField();
		countLabel = new JLabel("0");
		startButton = new JButton("Start");
		startButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(counter != null) {
					counter.interrupt();
					countLabel.setText("0");
				}
				int inputInt;
				String input = countTo.getText();
				if(input.isEmpty()) inputInt = 100000000;
				else inputInt = Integer.parseInt(input);
				counter = new Counter(inputInt, countLabel);
				counter.start();
			}
		});
		stopButton = new JButton("Stop");
		stopButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(counter != null) counter.interrupt();
			}
		});
		panel.add(countTo);
		panel.add(countLabel);
		panel.add(startButton);
		panel.add(stopButton);
	}
	
	public void setLabelText(String text){
		countLabel.setText(text);
	}
	
	private static void createAndShowGUI() {
		JFrame frame = new JFrame();
		JComponent content = (JComponent) frame.getContentPane();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		
		JCount first = new JCount();
		JCount second = new JCount();
		JCount third = new JCount();
		JCount fourth = new JCount();
		
		content.add(first.panel);
		content.add(Box.createRigidArea(new Dimension(0, 40)));
		content.add(second.panel);
		content.add(Box.createRigidArea(new Dimension(0, 40)));
		content.add(third.panel);
		content.add(Box.createRigidArea(new Dimension(0, 40)));
		content.add(fourth.panel);
		
		frame.setLocationByPlatform(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				createAndShowGUI();
			}
		});
	}

}
