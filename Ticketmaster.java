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

	//Says we dont need it but here is kinda how you use it i think
	//Select * from information_schema.sequences;
	//select * from pg_sequences;
	//CREATE SEQUENCE 'serial' START 101;
	//select currval('serial');
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
	
	//needs testing
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

		String query = String.format("INSERT INTO Users (fname, lname, email, phone, pwd) VALUES ('%s', '%s', '%s', %d, '%s');", firstname, lastname, email, phone, password);
		try {
			esql.executeUpdate(query);
		} catch (Exception e) {
			//some error message 
			System.out.println("Did not update DB");
		}
	}

	//needs testing
	public static void AddBooking(Ticketmaster esql){//2
		int bookingId;
		bookingId = getInt("Input booking ID: ");

		String status;
		status = getString("Input status: ");
		
		String dateTime;
		dateTime = getString("Input booking date and time (M/D/YYYY hh:mm): ");

		int numSeats;
		numSeats = getInt("Input number of seats booked: ");

		int showId;
		showId = getInt("Input show ID: ");

		String email;
		//if an email is non-existant the database will not update
		email = getString("Input email: ");

		String query = String.format("INSERT INTO Bookings (bid, status, bdatetime, seats, sid, email) VALUES (%d, '%s', '%s', %d, %d, '%s');", bookingId, status, dateTime, numSeats, showId, email);
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

	public static void CancelPendingBookings(Ticketmaster esql){//4

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
		String bookingId;
		String showSeatIDOriginal;
		String showSeatIDAvailable;

		bookingId = getString("Enter your booking ID: ");
		showSeatIDOriginal = getString("Enter the show seat ID that you would like to change: ");
		showSeatIDAvailable = getString("Enter the show seat ID that you would like to change to(make sure it is the same price): ");

		//first check if the seat is avaible
		String query1;
		String query2;
		String query3;
		query1 = String.format("UPDATE ShowSeats SET bid = %s WHERE ssid = %s AND bid IS NULL;", bookingId, showSeatIDAvailable);// if not found the execute should return 0
		query2 = String.format("SELECT price FROM ShowSeats WHERE bid = %s AND ssid = %s;", bookingId, showSeatIDOriginal);		//this will be used to compare price
		String seatPrice;
		try {
			int check = esql.executeQuery(query1); //if there are 0 rows updated then throw exception
			if(check == 0){
				throw new Exception("Seat is not availble or not found");
			}
			else {
				seatPrice = (esql.executeQueryAndReturnResult(query2).get(0)).get(0);
				query3 = String.format("UPDATE ShowSeats SET bid = NULL WHERE ssid = %s AND bid = %s AND price = %s;", showSeatIDOriginal, bookingId, seatPrice);	//then remove the original seat
				esql.executeQuery(query3);
			}
		} catch (Exception e) {
			System.out.println("Did not update DB");
		}
	}

	//needs testing
	public static void RemovePayment(Ticketmaster esql){//6

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
		query = String.format("DELETE FROM Payments WHERE bid IN (select bid from bookings where status = 'cancelled');");
		String query1;
		query1 = String.format("DELETE FROM Bookings WHERE status = 'cancelled';");
		try {
			esql.executeUpdate(query);
			esql.executeUpdate(query1);
		} catch (Exception e) {
			System.out.println("Did not update DB");
		}
	}

	public static void RemoveShowsOnDate(Ticketmaster esql){//8

		String date;
		date = getString("Input date to remove all shows: ");

		String cinemaName;
		cinemaName = getString("Enter the cinema name closing: ");
		
		//select * from bookings where bdatetime > '2019-02-02 00:00:00-08' AND bdatetime < '2019-02-02 23:59:59-08';
		//this format also works (somehow it translates in sql)
		//select * from bookings where bdatetime > '2/2/2019 00:00:00-08' AND bdatetime < '2/2/2019 23:59:59-08';

		String query;
		query = String.format("UPDATE Bookings SET status = 'cancelled' where sid IN (select sid from shows where sdate='%s' AND sid IN (select sid from plays where tid IN (select tid from theaters where cid IN (select cid from cinemas where cname = '%s'))));", date, cinemaName);
		try {
			esql.executeUpdate(query);
		} catch (Exception e) {
			System.out.println("Did not update DB");
		}
	}

	// test successful
	public static void ListTheatersPlayingShow(Ticketmaster esql){//9
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

	//test successful
	public static void ListShowsStartingOnTimeAndDate(Ticketmaster esql){//10
		String date;
		//will also accept specific second  (YYYY-MM-DD HH:MM:SS)
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

	//test successful
	public static void ListMovieTitlesContainingLoveReleasedAfter2010(Ticketmaster esql){//11

		//select * from movies where (title like '%Love%') AND rdate > '2010-12-31'; non-inclusive 2010
		String query;
		query = "SELECT title FROM Movies WHERE (title LIKE '%Love%') AND rdate > '2010-12-31';";
		try {
			esql.executeQueryAndPrintResult(query);
		} catch (Exception e) {
			System.out.println("Did not update DB");
		}
	}

	//test successful
	public static void ListUsersWithPendingBooking(Ticketmaster esql){//12

		//SELECT fname, lname, email FROM Users WHERE email IN (SELECT email FROM Bookings WHERE status = 'pending');
		String query;
		query = String.format("SELECT fname, lname, email FROM Users WHERE email IN (SELECT email FROM Bookings WHERE status = 'pending');");
		try {
			esql.executeQueryAndPrintResult(query);
		} catch (Exception e) {
			System.out.println("Did not update DB");
		}
	}

	//test successful
	public static void ListMovieAndShowInfoAtCinemaInDateRange(Ticketmaster esql){//13
		
		String date1; // lower bound
		String date2; // upper bound
		String movieName;
		String cinemaName;
		
		date1 = getString("Input start date (YYYY-MM-DD): ");
		date2 = getString("Input end date (YYYY-MM-DD): ");
		movieName = getString("Input movie name: ");
		cinemaName = getString("Input cinema name: ");
		
		String query;
		//SELECT cname, title, duration, sdate, sttime FROM Theaters INNER JOIN Plays ON Plays.tid = Theaters.tid INNER JOIN Shows ON Shows.sid = Plays.sid INNER JOIN Movies ON Movies.mvid = Shows.mvid INNER JOIN  Cinemas ON Cinemas.cid = Theaters.cid WHERE title = 'Aquaman' AND cname = 'AMC' AND sdate >= '2019-02-01' AND sdate <= '2019-02-07';
		query = String.format("SELECT cname, title, duration, sdate, sttime FROM Theaters INNER JOIN Plays ON Plays.tid = Theaters.tid INNER JOIN Shows ON Shows.sid = Plays.sid INNER JOIN Movies ON Movies.mvid = Shows.mvid INNER JOIN  Cinemas ON Cinemas.cid = Theaters.cid WHERE title = '%s' AND cname = '%s' AND sdate >= '%s' AND sdate <= '%s';", movieName, cinemaName, date1, date2);
		try {
			esql.executeQueryAndPrintResult(query);
		} catch (Exception e) {
			System.out.println("Did not update DB");
		}
	}

	//test successful
	public static void ListBookingInfoForUser(Ticketmaster esql){//14
		// vars
		String emailaddress;
		
		//get names
		emailaddress = getString("Input user's email address: ");
		
		String query;
		
		// display all specicfied users info
		query = String.format("SELECT title AS \"Movie Title\", sdate AS \"Show Date\", sttime AS \"Start Time\", tname AS \"Theater Name\", csid \"Cinema Seat Number\" FROM Bookings INNER JOIN Shows ON Bookings.sid=Shows.sid INNER JOIN Movies ON Shows.mvid=Movies.mvid INNER JOIN ShowSeats ON Bookings.sid=ShowSeats.sid INNER JOIN Plays ON Bookings.sid=Plays.sid INNER JOIN Theaters ON Plays.tid=Theaters.tid WHERE bookings.email = '%s';", emailaddress);
		try {
			esql.executeQueryAndPrintResult(query);
		} catch (Exception e) {
			System.out.println("Did not update DB");
		}
		
	}
	
}
