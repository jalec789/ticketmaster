/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

//To hash passwords
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class Ticketmaster{

	public static byte[] getSHA(String input) throws NoSuchAlgorithmException
	{
		// Static getInstance method is called with hashing SHA
		MessageDigest md = MessageDigest.getInstance("SHA-256");

		// digest() method called
		// to calculate message digest of an input
		// and return array of byte
		return md.digest(input.getBytes(StandardCharsets.UTF_8));
	}

	public static String toHexString(byte[] hash)
	{
		// Convert byte array into signum representation
		BigInteger number = new BigInteger(1, hash);

		// Convert message digest into hex value
		StringBuilder hexString = new StringBuilder(number.toString(16));

		// Pad with leading zeros
		while (hexString.length() < 32)
		{
			hexString.insert(0, '0');
		}

		return hexString.toString();
	}

	public static String hashPassword(String password) {
		String hash = "SHA256";
		try {
			hash = toHexString(getSHA(password));
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Exception thrown for incorrect algorithm: " + e);
		}
		return hash;
	}

	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public Ticketmaster(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + Ticketmaster.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		Ticketmaster esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new Ticketmaster (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add User");
				System.out.println("2. Add Booking");
				System.out.println("3. Add Movie Showing for an Existing Theater");
				System.out.println("4. Cancel Pending Bookings");
				System.out.println("5. Change Seats Reserved for a Booking");
				System.out.println("6. Remove a Payment");
				System.out.println("7. Clear Cancelled Bookings");
				System.out.println("8. Remove Shows on a Given Date");
				System.out.println("9. List all Theaters in a Cinema Playing a Given Show");
				System.out.println("10. List all Shows that Start at a Given Time and Date");
				System.out.println("11. List Movie Titles Containing \"love\" Released After 2010");
				System.out.println("12. List the First Name, Last Name, and Email of Users with a Pending Booking");
				System.out.println("13. List the Title, Duration, Date, and Time of Shows Playing a Given Movie at a Given Cinema During a Date Range");
				System.out.println("14. List the Movie Title, Show Date & Start Time, Theater Name, and Cinema Seat Number for all Bookings of a Given User");
				System.out.println("15. EXIT");
				
				/*
				 * FOLLOW THE SPECIFICATION IN THE PROJECT DESCRIPTION
				 */
				switch (readChoice()){
					case 1: AddUser(esql); break;
					case 2: AddBooking(esql); break;
					case 3: AddMovieShowingToTheater(esql); break;
					case 4: CancelPendingBookings(esql); break;
					case 5: ChangeSeatsForBooking(esql); break;
					case 6: RemovePayment(esql); break;
					case 7: ClearCancelledBookings(esql); break;
					case 8: RemoveShowsOnDate(esql); break;
					case 9: ListTheatersPlayingShow(esql); break;
					case 10: ListShowsStartingOnTimeAndDate(esql); break;
					case 11: ListMovieTitlesContainingLoveReleasedAfter2010(esql); break;
					case 12: ListUsersWithPendingBooking(esql); break;
					case 13: ListMovieAndShowInfoAtCinemaInDateRange(esql); break;
					case 14: ListBookingInfoForUser(esql); break;
					case 15: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice

	public static String getString(String prompt) {
		String input;
		do {
			System.out.print(prompt);
			try {
				input = in.readLine();
				//if statement here if needed...
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}
		} while (true);
		return input;
	}

	//idk what kind of ranges we should have???
	public static int getInt(String prompt){
		int input;
		do {
			System.out.print(prompt);
			try {
				input = Integer.parseInt(in.readLine());
				//if statement here if needed...
				break;
			}
			catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}
		} while (true);
		return input;
	}
	
	public static void AddUser(Ticketmaster esql){//1
		String firstname;
		firstname = getString("Input firstname: ");

		String lastname;
		lastname = getString("Input lastname: ");

		String email;
		email = getString("Input email");

		int phone;
		phone = getInt("input phone number: ");

		String password;
		password = getString("Input password: ");
		password = hashPassword(password);
		//System.out.println("Hash is " + password);		//DEBUG

		String query = String.format("INSERT INTO Users (fname, lname, email, phone, pwd) VALUES ('%s', '%s', '%s', %d, '%s');", firstname, lastname, email, phone, password);
		//System.out.println("query string: " + query);		//DEBUG
		try {
			esql.executeUpdate(query);
		} catch (Exception e) {
			//some error message idk
			System.out.println("Did not update DB");
		}
	}
	
	public static void AddBooking(Ticketmaster esql){//2
		int bookingId;
		//Should this follow the sequence using function getCurrSeqVal()???
		bookingId = getInt("Input booking ID: ");
		//or we could check to see if the id already exists???

		String status;
		status = getString("Input status: ");
		
		String dateTime;
		//maybe separate these two?
		dateTime = getString("Input booking date and time (M/D/YYYY hh:mm): ");

		int numSeats;
		numSeats = getInt("Input number of seats booked: ");

		int showId;
		showId = getInt("Input show ID: ");

		String email;
		//if an email is non-existant the database will not update
		email = getString("Input email: ");

		String query = String.format("INSERT INTO Bookings (bid, status, bdatetime, seats, sid, email) VALUES (%d, '%s', '%s', %d, %d, '%s');", bookingId, status, dateTime, numSeats, showId, email);
		//System.out.println("query string: " + query);		//DEBUG
		try {
			esql.executeUpdate(query);
		} catch (Exception a) {
			System.out.println("Did not update DB");
		}
	}

	public static void AddMovieShowingToTheater(Ticketmaster esql){//3
		//since movie PK=mvid and show FK=mvid we want to
		//add movie first then add the show
		
		int movieId;
		movieId = getInt("Input the movie ID: ");

		//check if movie ID exists
		String queryCheck = String.format("SELECT * FROM Movies WHERE mvid=%d;", movieId);

		//check if movie ID exists
		try{
			int i =	esql.executeQuery(queryCheck); //if >= 1 then we should
			if(i == 0) {
				throw new Exception("Movie not found");
			}
			System.out.println("Movie ID exists!!!");
		} catch (Exception e) {
			System.out.println("Movie ID does not exist so a new movie will be added");

			String title;
			title = getString("Input title of movie: ");

			String releaseDate;
			releaseDate = getString("Input the release date (M/D/YYYY): ");

			String country;
			country = getString("Input release country: ");

			String description;
			description = getString("Input description of the movie: ");

			int duration;
			duration = getInt("Input duration of movie (in seconds): ");

			String language;
			language = getString("Input language of movie: ");

			String genre;
			genre = getString("Input genre of movie: ");

			String movieQuery;
			movieQuery = String.format("INSERT INTO Movies (mvid, title, rdate, country, description, duration, lang, genre) VALUES (%d, '%s', '%s', '%s', '%s', %d, '%s', '%s');", movieId, title, releaseDate, country, description, duration, language, genre);

			try {
				esql.executeUpdate(movieQuery);
			} catch (Exception a) {
				System.out.println("Did not update DB");		//idk why it wouldnt add the movie if the pk was prespecified
			}
		}

		//now we can add the show to theater
		int showId;
		showId = getInt("Input show ID: ");
		//should we make sure the id is non existant???

		String showDate;
		showDate = getString("Input the show date: ");

		String startTime;
		startTime = getString("Input the start time: ");

		String endTime;
		endTime = getString("Input the end time: ");

		String query;
		query = String.format("INSERT INTO Shows (sid, mvid, sdate, sttime, edtime) VALUES (%d, %d, '%s', '%s', '%s');", showId, movieId, showDate, startTime, endTime);

		try {
			esql.executeUpdate(query);
		} catch (Exception e) {
			System.out.println("Did not update DB");
		}
	}

	// STILL NEEDS ATTENTION ---------------------------------------------------------
	public static void CancelPendingBookings(Ticketmaster esql){//4

		//Maybe we should unlink the seats. like if status is now cancelled maybe we should get those bid and update the ShowSeats table???

		String query;
		query = String.format("UPDATE Bookings SET status = 'cancelled' WHERE status = 'pending';");
		try {
			esql.executeUpdate(query);
		} catch (Exception e) {
			System.out.println("Did not update DB");
		}
	}


	// STILL NEEDS ATTENTION ---------------------------------------------------------
	public static void ChangeSeatsForBooking(Ticketmaster esql) throws Exception{//5

		//we need to select seats where
		//to do this we need to
		//retrive booking id executeQueryAndReturnResult(), i think they seem to be empty in the given data set
		//then we change the cinema seat id or show seat ID???
		//BUT also check if the sum of seats are the same price... idk how to retrive a single cell???
		//query: UPDATE csid FROM ShowSeats WHERE bid='bid';

		//The problem with this is that the data that holds ShowSeats has an empty bid column

//		String query;
//		query = String.format("");
//		try {
//			esql.executeUpdate(query);
//		} catch (Exception e) {
//			System.out.println("Did not update DB");
//		}
	}

	// STILL NEEDS ATTENTION ---------------------------------------------------------
	public static void RemovePayment(Ticketmaster esql){//6
		//Maybe we should unlink the seats. like if status is now cancelled maybe we should get those bid and update the ShowSeats table???

		int bookingId;
		bookingId = getInt("Input booking ID to be cancelled: ");

		String query;
		query = String.format("UPDATE Bookings SET status = 'cancelled' WHERE bid = %d;", bookingId);
		try {
			esql.executeUpdate(query);
		} catch (Exception e) {
			System.out.println("Did not update DB");
		}
	}
	
	public static void ClearCancelledBookings(Ticketmaster esql){//7
		String query;
		query = String.format("DELETE FROM Bookings WHERE status = 'cancelled';");
		try {
			esql.executeUpdate(query);
		} catch (Exception e) {
			System.out.println("Did not update DB");
		}
	}

	// STILL NEEDS ATTENTION ---------------------------------------------------------
	public static void RemoveShowsOnDate(Ticketmaster esql){//8
		//Maybe we should unlink the seats. like if status is now cancelled maybe we should get those bid and update the ShowSeats table???

		String date;
		date = getString("Input date to remove all shows: ");


		//select * from bookings where sid in (select sid from shows where sdate = '2/2/2019');
		//^this doesnt work since sid is not unique :(
		//this works though:
		//select * from bookings where bdatetime > '2019-02-02 00:00:00-08' AND bdatetime < '2019-02-02 23:59:59-08';
		//this format also works (somehow it translates in sql)
		//select * from bookings where bdatetime > '2/2/2019 00:00:00-08' AND bdatetime < '2/2/2019 23:59:59-08';
		String query;
		query = String.format("UPDATE Bookings SET status = 'cancelled' WHERE bdatetime > '%s 00:00:00' AND bdatetime < '%s 23:59:59';", date);
		try {
			esql.executeUpdate(query);
		} catch (Exception e) {
			System.out.println("Did not update DB");
		}
	}

	//given a show sid???	--test successful
	public static void ListTheatersPlayingShow(Ticketmaster esql){//9
		//
		//executeQueryAndPrintResult()
		//since show to cinema theater is many to many with show we have to wrap around using show seating to connect show id with theater id
		int showId;
		showId = getInt("Input the show ID: ");

		//SELECT * FROM Theaters WHERE tid IN (SELECT tid FROM CinemaSeats WHERE csid IN (SELECT csid FROM ShowSeats WHERE sid = 1));

		String query;
		query = String.format("SELECT * FROM Theaters WHERE tid IN (SELECT tid FROM CinemaSeats WHERE csid IN (SELECT csid FROM ShowSeats WHERE sid = %d));", showId);
		try {
			esql.executeQueryAndPrintResult(query);
		} catch (Exception e) {
			System.out.println("Did not update DB");
		}
	}

	//it just says list all shows not movie titles		---test successful
	public static void ListShowsStartingOnTimeAndDate(Ticketmaster esql){//10
		//
		String date;
		//will also accept specific second lol (YYYY-MM-DD HH:MM:SS)
		date = getString("Input date (YYYY-MM-DD): ");

		String time;
		time = getString("Input a time in 24hr format (HH:MM): ");

		String query;
		query = String.format("SELECT * FROM Shows WHERE sdate = '%s' AND sttime = '%s';", date, time);
		try {
			esql.executeQueryAndPrintResult(query);
		} catch (Exception e) {
			System.out.println("Did not update DB");
		}
	}

	public static void ListMovieTitlesContainingLoveReleasedAfter2010(Ticketmaster esql){//11
		//

		//select * from movies where (title like '%Love%') AND rdate > '2010-12-31'; non-inclusive 2010
		String query;
		query = String.format("SELECT title FROM Movies WHERE (title LIKE '%Love%') AND rdate > '2010-12-31';");
		try {
			esql.executeQueryAndPrintResult(query);
		} catch (Exception e) {
			System.out.println("Did not update DB");
		}
	}

	public static void ListUsersWithPendingBooking(Ticketmaster esql){//12
		//

		//SELECT fname, lname, email FROM Users WHERE email IN (SELECT email FROM Bookings WHERE status = 'Paid');
		String query;
		query = String.format("SELECT fname, lname, email FROM Users WHERE email IN (SELECT email FROM Bookings WHERE status = 'Paid');");
		try {
			esql.executeQueryAndPrintResult(query);
		} catch (Exception e) {
			System.out.println("Did not update DB");
		}
	}

	//not tested
	public static void ListMovieAndShowInfoAtCinemaInDateRange(Ticketmaster esql){//13
		
		String date1; // lower bound
		String date2; // upper bound
		date1 = getString("Input date (YYYY-MM-DD): ");
		date2 = getString("Input date (YYYY-MM-DD): ");
		
		String query;
		
		// do we need to select from movie as well?
		query = String.format("SELECT * FROM Shows WHERE sdate > '%s' AND sdate < '%s';", date1, date2);
		try {
			esql.executeQueryAndPrintResult(query);
		} catch (Exception e) {
			System.out.println("Did not update DB");
		}
	}

	public static void ListBookingInfoForUser(Ticketmaster esql){//14
		// vars
		String firstname;
		String lastname;
		
		//get names
		firstname = getString("Input first name: ");
		lastname = getString("Input last name: ");
		
		String query;
		
		// display all specicfied users info
		query = String.format("SELECT * FROM Users WHERE fname = '%s' AND lname = '%s';", firstname, lastname);
		try {
			esql.executeQueryAndPrintResult(query);
		} catch (Exception e) {
			System.out.println("Did not update DB");
		}
		
	}
	
}
