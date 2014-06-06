
public class Account {
	
	// Unique Account ID
	public int id;
	// Current balance of the account
	public double currentBalance;
	// Number of transactions the account has undergone
	public int numTransactions;
		
	/**
	 * Instantiates a new Account with the provided ID and sets the currentBalance to 
	 * the initialBalance. Sets transactions to 0.
	 * @param id
	 * @param initialBalance
	 */
	public Account(int id, double initialBalance){
		this.id = id;
		currentBalance = initialBalance;
		numTransactions = 0;
	}
	
	/**
	 * @return the current Account balance
	 */
	public synchronized double getBalance() {
		return currentBalance;
	}
	
	/**
	 * Sets the Account balance to the value of newBalance.
	 * @param newBalance
	 */
	public synchronized void setBalance(double newBalance){
		currentBalance = newBalance;
	}
		
	/**
	 * Adds one to the number of transactions that have occurred. 
	 */
	public synchronized void incrementTransactions(){
		numTransactions++;
	}
		
	/**
	 * Decreases the current balance of the current Account by amount and increments
	 * the balance of the passed in account to its original value + the amount value.
	 * Increments transactions for both. Blind of to if the given amount will make the
	 * current balance negative (i.e. there are insufficient funds in the Account).
	 * @param to
	 * @param amount
	 */
	public synchronized void transfer(Account to, double amount) {
			currentBalance -= amount;
			incrementTransactions();
			to.setBalance(to.getBalance() + amount);
			to.incrementTransactions();
	}
		
	/**
	 * Prints out the relevant Account information
	 * e.g. acct:1 balance:175 transactions:3
	 */
	@Override
	public String toString() {
		return("acct:" + id + " balance:" + currentBalance + " transactions:" + numTransactions);
	}
	
}
