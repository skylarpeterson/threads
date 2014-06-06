import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.text.SimpleDateFormat;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Semaphore;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.net.*;


public class WebFrame extends JFrame{
	
	private class WebWorker extends Thread{
		private String url;
		private int row;
		
		public WebWorker(String url, int row){
			this.url = url;
			this.row = row;
		}
		
		private void download(){
	 		InputStream input = null;
			StringBuilder contents = null;
			try {
				long start = System.currentTimeMillis();
				URL url_obj = new URL(url);
				URLConnection connection = url_obj.openConnection();
			
				// Set connect() to throw an IOException
				// if connection does not succeed in this many msecs.
				connection.setConnectTimeout(5000);
				
				connection.connect();
				input = connection.getInputStream();

				BufferedReader reader  = new BufferedReader(new InputStreamReader(input));
			
				char[] array = new char[1000];
				int len;
				contents = new StringBuilder(1000);
				while ((len = reader.read(array, 0, array.length)) > 0) {
					if(Thread.interrupted()) {
						updateTable(row, "Interrupted");
						return;
					}
					contents.append(array, 0, len);
					Thread.sleep(100);
				}
				long end = System.currentTimeMillis();
				SimpleDateFormat date = new SimpleDateFormat("HH:mm:ss");
				Date now = new Date();
				
				String string = date.format(now) + "\t" + Long.toString((end - start)) + "ms\t" + contents.length() + " bytes";
				updateTable(row, string);
				
				// Successful download if we get here
				
			}
			// Otherwise control jumps to a catch...
			catch(MalformedURLException exception) {
				updateTable(row, "err");
			}
			catch(InterruptedException exception) {
				updateTable(row, "Interrupted");
				this.interrupt();
			}
			catch(IOException ignored) {}
			// "finally" clause, to close the input stream
			// in any case
			finally {
				try{
					if (input != null) input.close();
				}
				catch(IOException ignored) {}
			}
		}
		
		@Override
		public void run(){
			addRunningThread();
			download();
			if(Thread.interrupted()) {
				updateTable(row, "Interrupted");
				return;
			}
			endRunningThread();
			threads.release();
		}
	}
	
	private class Launcher extends Thread{
		
		private int threadLimit;
		private ArrayList<WebWorker> workers;
		
		public Launcher(int threadLimit){
			this.threadLimit = threadLimit;
		}
		
		@Override
		public void run(){
			long startTime = System.currentTimeMillis();
			addRunningThread();
			workers = new ArrayList<WebWorker>();
			updateButtons(false, false, true, numURLs);
			threads = new Semaphore(threadLimit);
			for(int i = 0; i < numURLs; i++){
				try {
					threads.acquire();
					String url = (String) model.getValueAt(i, 0);
					WebWorker worker = new WebWorker(url, i);
					workers.add(worker);
					worker.start();
				} catch (InterruptedException e) {
					updateTable(i, "Interrupted");
					break;
				}
			}
			endRunningThread();
			long endTime = System.currentTimeMillis();
			runTime = endTime - startTime;
			updateButtons(true, true, false, 0);
		}
	}

	private Semaphore threads;
	private int runningThreads;
	private int completedThreads;
	private long runTime;
	private Launcher launcher;
	
	private JPanel panel;
	private DefaultTableModel model;
	private int numURLs;
	private JTable table;
	private JButton singleThreadButton;
	private JButton concurrentThreadButton;
	private JTextField numThreads;
	private JLabel runningLabel;
	private JLabel completedLabel;
	private JLabel elapsedLabel;
	private JProgressBar progressBar;
	private JButton stopButton;
	
	public void updateButtons(boolean single, boolean concurrent, boolean stop, int max){
		singleThreadButton.setEnabled(single);
		concurrentThreadButton.setEnabled(concurrent);
		stopButton.setEnabled(stop);
		progressBar.setMaximum(max);
	}
	
	public void updateLabels(){
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				runningLabel.setText("Running: " + Integer.toString(runningThreads));
				completedLabel.setText("Completed: " + completedThreads);
				elapsedLabel.setText("Elapsed: " + runTime);
				progressBar.setValue(completedThreads);
			}
		});
	}
	
	public void updateTable(final int row, final String update){
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				model.setValueAt(update, row, 1);
			}
		});
	}
	
	public void clearTable(){
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				for(int i = 0; i < numURLs; i++){
					model.setValueAt("", i, 1);
				}
			}
		});
	}
	
	public synchronized void addRunningThread(){
		runningThreads++;
		updateLabels();
	}
	
	public synchronized void endRunningThread(){
		completedThreads++;
		runningThreads--;
		updateLabels();
	}
	
	public WebFrame(String filename) {
		super("Web Worker");
		runningThreads = 0;
		completedThreads = 0;
		
		JComponent content = (JComponent) getContentPane();
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		model = new DefaultTableModel(new String[] { "url", "status"}, 0);
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line = br.readLine();
			int i = 0;
			while(line != null){
				model.addRow(new String[] {line, ""});
				i++;
				line = br.readLine();
			}
			numURLs = i;
			br.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		table = new JTable(model);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		
		JScrollPane scrollpane = new JScrollPane(table);
		scrollpane.setPreferredSize(new Dimension(600, 300));
		panel.add(scrollpane);
		
		singleThreadButton = new JButton("Single Thread Fetch");
		singleThreadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				clearTable();
				completedThreads = 0;
				launcher = new Launcher(1);
				launcher.start();
			}
		});
		panel.add(singleThreadButton);
		concurrentThreadButton = new JButton("Concurrent Fetch");
		concurrentThreadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				clearTable();
				completedThreads = 0;
				int threadLimit;
				if(numThreads.getText().isEmpty()) threadLimit = 1;
				else threadLimit = Integer.parseInt(numThreads.getText());
				launcher = new Launcher(threadLimit);
				launcher.start();
			}
		});
		panel.add(concurrentThreadButton);
		
		numThreads = new JTextField();
		numThreads.setMaximumSize(new Dimension(80, 20));
		panel.add(numThreads);
		
		runningLabel = new JLabel("Running: 0");
		completedLabel = new JLabel("Completed: 0");
		elapsedLabel = new JLabel("Elapsed: ");
		panel.add(runningLabel);
		panel.add(completedLabel);
		panel.add(elapsedLabel);
		
		progressBar = new JProgressBar();
		progressBar.setMaximum(0);
		panel.add(progressBar);
		
		stopButton = new JButton("Stop");
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				launcher.interrupt();
				ArrayList<WebWorker> workers = launcher.workers;
				for(int i = 0; i < workers.size(); i++){
					workers.get(i).interrupt();
				}
			}
		});
		stopButton.setEnabled(false);
		panel.add(stopButton);
		
		content.add(panel);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		WebFrame frame = new WebFrame("links.txt");
		frame.setLocationByPlatform(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

}
