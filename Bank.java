import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;


public class Bank {

	/*
	 * A simple class that houses a transaction.  Little more than an object-oriented
	 * design that houses variables.  idFrom and idTo are the account ids associated
	 * with the transfer, and amount is the amount to be transferred.
	 */
	private class Transaction {
		
		public int idFrom;
		public int idTo;
		public double amount;
		
		/*
		 * Sets the values of the instance variables.
		 */
		public Transaction(int idFrom, int idTo, double amount){
			this.idFrom = idFrom;
			this.idTo = idTo;
			this.amount = amount;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 * Prints out to the console the relevant transaction information.
		 * e.g. From:1 To:2 Amount:100
		 * (For debugging purposes)
		 */
		@Override
		public String toString(){
			return ("From:" + idFrom + " To:" + idTo + " Amount:" + amount);
		}
	}
	
	/*
	 * This class handles the multi-threaded work of dequeueing transfers and 
	 * executing them.  A BlockingQueue is used to communicate between Worker 
	 * threads and the main thread.  Loops infinitely, pulling Transfer from
	 * the queue and using the Account method transfer() to transfer funds 
	 * between accounts, until it reaches a sentinel transfer that indicates
	 * no more transfers need be processed.  
	 */
	private class Worker extends Thread{
		@Override
		public void run(){
			try {
				while(true) {
					Transaction newTransaction = transactions.take();
					if(newTransaction.idFrom < 0) break;
					Account from = accounts.get(newTransaction.idFrom);
					Account to = accounts.get(newTransaction.idTo);
					from.transfer(to, newTransaction.amount);
				}
				latch.countDown();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	// Maximum amount of transfers in queue at a time
	private static int MAX_IN_QUEUE = 100;
	// Initial balance of all 20 Accounts
	private static int INITIAL_BALANCE = 1000;
	// List of 20 accounts
	private ArrayList<Account> accounts;
	// Queue of transfers
	private ArrayBlockingQueue<Transaction> transactions;
	// Countdown latch used to notify main thread when Workers are done
	static CountDownLatch latch;
	
	/**
	 * Initializes the 20 bank Accounts and adds them to the List.  Initializes 
	 * empty transfer queue with a size of MAX_IN_QUEUE.
	 */
	public Bank() {	
		accounts = new ArrayList<Account>();
		for (int i = 0; i < 20; i++){
			Account newAccount = new Account(i, INITIAL_BALANCE);
			accounts.add(newAccount);
		}
		transactions = new ArrayBlockingQueue<Transaction>(MAX_IN_QUEUE);
	}
	
	/**
	 * @return the list of 20 accounts
	 */
	public ArrayList<Account> getAccounts(){
		return accounts;
	}
	
	/**
	 * Uses a buffered reader to read transfers from the passed in file line by line.
	 * Each transfer is queued and then handled by one of the Workers (of which numWorkers
	 * are instantiated). Uses the countdown latch to receive notification when the Worker
	 * threads are done. 
	 * @param filename
	 * @param numWorkers
	 */
	public void makeTransfers(String filename, int numWorkers){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			latch = new CountDownLatch(numWorkers);
			for(int i = 0; i < numWorkers; i++){
				Worker newWorker = new Worker();
				newWorker.start();
			}
			
			String nextLine = reader.readLine();
			while(nextLine != null){
				String[] tokens = nextLine.split(" ");
				Transaction newTransaction = new Transaction(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]), Double.parseDouble(tokens[2]));
				transactions.put(newTransaction);
				nextLine = reader.readLine();
			}
			reader.close();
			Transaction newTransaction = new Transaction(-1, -1, 0);
			for(int j = 0; j < numWorkers; j++) transactions.put(newTransaction);
			latch.await();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Uses the passed in text file of transfers and number of threads to
	 * execute the transfers and print the account information to the console
	 * when all transfers are completed.
	 * @param args
	 */
	public static void main(String[] args) {
		Bank newBank = new Bank();
		newBank.makeTransfers(args[0], Integer.parseInt(args[1]));
		ArrayList<Account> accounts = newBank.getAccounts();
		for(int i = 0; i < accounts.size(); i++){
			System.out.println(accounts.get(i).toString());
		}
	}

}
