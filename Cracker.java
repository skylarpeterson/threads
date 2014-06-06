import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class Cracker {
	
	/*
	 * This inner class is meant to handle the string-searching work. It takes the
	 * particular chunk of the CHARS array from startIndex to endIndex (the index
	 * after the last accessible index to this thread).  It uses a recursive method
	 * to check all possible string combinations of length up to passwordLength starting
	 * with the characters in CHARS between startIndex and endIndex.
	 */
	private class Worker extends Thread {
		private int startIndex;
		private int endIndex;
		private byte[] hex;
		
		/*
		 * Standard constructor. Pass in params to set instance variable values.
		 */
		public Worker(int startIndex, int endIndex, byte[] hex){
			this.startIndex = startIndex;
			this.endIndex = endIndex;
			this.hex = hex;
		}
		
		/*
		 * Recursive method for searching all possible string combinations.  Takes
		 * the passed in string and appends every character of CHARS to string's
		 * end, one at a time, and makes a recursive string with the new string.
		 * Base case is that the built-up string are of length passwordLength. If
		 * there is a match, immediately prints out string to the console.
		 */
		public void findStrings(MessageDigest md, StringBuilder string){
			if(Arrays.equals(md.digest(string.toString().getBytes()), hex)) System.out.println(string.toString());
			if(string.length() == passwordLength) return;
			
			for(int i = 0; i < CHARS.length; i++){
				StringBuilder newString = new StringBuilder(string.toString());
				newString.append(CHARS[i]);
				findStrings(md, newString);
			}
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Thread#run()
		 * Makes calls to recursive function for all characters in CHARS between
		 * startIndex and endIndex. When done, counts down the countdown latch.
		 */
		@Override
		public void run(){
			try {
				MessageDigest md = MessageDigest.getInstance("SHA");
				for(int i = startIndex; i < endIndex; i++){
					StringBuilder string = new StringBuilder();
					string.append(CHARS[i]);
					findStrings(md, string);
				}
				latch.countDown();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}
	}
	
	// Array of chars used to produce strings
	public static final char[] CHARS = "abcdefghijklmnopqrstuvwxyz0123456789.,-!".toCharArray();	
	// Maximum length a generated password can be
	private int passwordLength;
	// Number of threads to run when cracking
	private int numThreads;
	// CountDownLatch to notify main thread when all threads are done.
	private CountDownLatch latch;
	
	/*
	 Given a byte[] array, produces a hex String,
	 such as "234a6f". with 2 chars for each byte in the array.
	 (provided code)
	*/
	public static String hexToString(byte[] bytes) {
		StringBuffer buff = new StringBuffer();
		for (int i=0; i<bytes.length; i++) {
			int val = bytes[i];
			val = val & 0xff;  // remove higher bits, sign
			if (val<16) buff.append('0'); // leading 0
			buff.append(Integer.toString(val, 16));
		}
		return buff.toString();
	}
	
	/*
	 Given a string of hex byte values such as "24a26f", creates
	 a byte[] array of those values, one byte value -128..127
	 for each 2 chars.
	 (provided code)
	*/
	public static byte[] hexToArray(String hex) {
		byte[] result = new byte[hex.length()/2];
		for (int i=0; i<hex.length(); i+=2) {
			result[i/2] = (byte) Integer.parseInt(hex.substring(i, i+2), 16);
		}
		return result;
	}
	
	/**
	 * Standard constructor for Cracker. Sets the values for the instance variables
	 * passwordLength and numThreads to the appropriate passed in values.
	 * @param passwordLength
	 * @param numThreads
	 */
	public Cracker(int passwordLength, int numThreads){
		this.passwordLength = passwordLength;
		this.numThreads = numThreads;
	}
	
	/**
	 * Given a String password, this static function prints a hex encoding for said 
	 * password according to the rules of the SHA algorithm to the console.
	 * @param password
	 */
	public static void generate(String password){
		try {
			MessageDigest md = MessageDigest.getInstance("SHA");
			System.out.println(hexToString(md.digest(password.getBytes())));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Uses multiple threads to print out to the console all of the possible 
	 * password strings that could be reverse engineered from the passed in 
	 * hex string.  Makes use of multiple threads to handle checking of all 
	 * possible character combinations that could result from the input. Prints
	 * "All done!" upon completion.
	 * @param hex
	 */
	public void crack(String hex){
		try {
			byte[] hexArr = hexToArray(hex); 
			latch = new CountDownLatch(numThreads);
			int indexIncrement = CHARS.length/numThreads;
			for(int i = 0; i < numThreads; i++){
				Worker newWorker = new Worker(i*indexIncrement, (i+1)*indexIncrement, hexArr);
				newWorker.start();
			}
			latch.await();
			System.out.println("All done!");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Does one of two things depending on the parameters stored in args[].  If
	 * the user has only passed in a password to generate a hex code for, a call
	 * to generate is made.  If the user has included the other two arguments for 
	 * passwordLength and numThreads, a Cracker is instantiated and the hex code is
	 * cracked.
	 * @param args
	 */
	public static void main(String args[]){
		if(args.length == 1){
			Cracker.generate(args[0]);
		} else if(args.length > 1) {
			Cracker cracker = new Cracker(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
			cracker.crack(args[0]);
		}
	}

}
