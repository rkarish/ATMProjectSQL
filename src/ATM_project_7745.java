import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import org.sqlite.SQLiteException;

public class ATM_project_7745 {

	public static void main(String[] args) throws SQLException, ParseException, SQLiteException {
		String uri = "jdbc:sqlite:db/ATM_Management.db";

		Connection conn = DriverManager.getConnection(uri);
		conn.setAutoCommit(false);
		Statement stmt = conn.createStatement();

		String selectAccounts = "SELECT bank_id, SUM(balance) AS bank_balance FROM Account GROUP BY bank_id;";
		Map<Integer, Integer> accounts = new HashMap<Integer, Integer>();

		ResultSet rs = stmt.executeQuery(selectAccounts);

		while (rs.next()) {
			accounts.put(rs.getInt("bank_id"), rs.getInt("bank_balance"));
			//System.out.println(rs.getInt("bank_id") + " " + rs.getInt("bank_balance"));
		}

		String addColumnToBank = "ALTER TABLE Bank ADD total INTEGER;";

		try {
			stmt.executeUpdate(addColumnToBank);
		} catch (SQLiteException e) {
			System.out.println("Column already exists");
		}
		
		Iterator<?> it = accounts.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			String insertTotal = "UPDATE Bank SET total = " + pair.getValue().toString() + " WHERE bank_id = "
					+ pair.getKey().toString() + ";";
			//System.out.println(insertTotal);
			stmt.executeUpdate(insertTotal);
		}
		
		String createView = "CREATE VIEW bank_totals AS SELECT bank_id, SUM(balance / 100.00) AS total_balance FROM Account " +
				"GROUP BY bank_id;";
		
		try {
			stmt.executeUpdate(createView);
		} catch (SQLiteException e) {
			System.out.println("View already exists");
		}
		
		conn.commit();

		Scanner input = new Scanner(System.in);
		String insertATMSQL = "INSERT INTO ATM(atm_id, bank_id, atm_location," +
				" location_name, balance, num_of_tran)" +
				" VALUES (?, ?, ?, ?, ?, 0);";
		PreparedStatement insertATMStmt = conn.prepareStatement(insertATMSQL);
		
		String insertMemberSQL = "INSERT INTO Member(mem_id, acct_id, mem_fname," +
				" mem_lname, ssn, phone, email, address, birthdate)" +
				" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
		PreparedStatement insertMemberStmt = conn.prepareStatement(insertMemberSQL);
		
		String insertAccountSQL = "INSERT INTO Account(acct_id, bank_id, acct_type," +
				" balance, is_active)" +
				" VALUES (?, ?, ?, ?, 'true');";
		PreparedStatement insertAccountStmt = conn.prepareStatement(insertAccountSQL);
		
		String insertATMTransSQL = "INSERT INTO ATM_transaction(tran_id, atm_id, mem_id, tran_amount, tran_time) " +
				" VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP);";
		PreparedStatement insertATMTransStmt = conn.prepareStatement(insertATMTransSQL);
		
		
		Boolean quit = false;
		int choice = 0;
		int count = 0;
		while (!quit) {
			System.out.println("Enter [1] ATM insert\nEnter [2] Member insert\nEnter [3] ATM transaction\nEnter [4] to exit");
			choice = input.nextInt();			
			if (choice == 1) {
				count = 0;
				while (true) {
					System.out.println("Enter atm_id:");
					insertATMStmt.setInt(1, input.nextInt());
					System.out.println("Enter bank_id:");
					insertATMStmt.setInt(2, input.nextInt());
					System.out.println("Enter atm_location:");
					insertATMStmt.setInt(3, input.nextInt());
					System.out.println("Enter location_name");
					input.nextLine();
					insertATMStmt.setString(4, input.nextLine());
					System.out.println("Enter balance");
					insertATMStmt.setInt(5, input.nextInt());
					insertATMStmt.addBatch();
					count++;
					System.out.println("Add more ATMs? (Y/N)");
					if (input.next().toLowerCase().startsWith("n")) {
						break;
					}
				}
				if (!conn.isClosed()) {
					System.out.println("There are " + count + " rolls waiting to insert. Do you wish to continue? (Y/N)");
					String crInput = input.next().toLowerCase();
				
					if (crInput.startsWith("y")) {
						insertATMStmt.executeBatch();
						conn.commit();
					} else {
						conn.rollback();
					}
				}
			} else if (choice == 2) {
				count = 0;
				while (true) {
					System.out.println("Enter mem_id:");
					insertMemberStmt.setInt(1, input.nextInt());
					System.out.println("Enter acct_id:");
					int acct_id = input.nextInt();
					insertMemberStmt.setInt(2, acct_id);
					System.out.println("Enter mem_fname:");
					input.nextLine();
					insertMemberStmt.setString(3, input.nextLine());
					System.out.println("Enter mem_lname:");
					insertMemberStmt.setString(4, input.nextLine());
					System.out.println("Enter ssn:");
					insertMemberStmt.setInt(5, input.nextInt());
					System.out.println("Enter phone:");
					insertMemberStmt.setInt(6, input.nextInt());
					System.out.println("Enter email:");
					input.nextLine();
					insertMemberStmt.setString(7, input.nextLine());
					System.out.println("Enter address:");
					insertMemberStmt.setString(8, input.nextLine());
					System.out.println("Enter birthdate (yyyy-mm-dd):");
					insertMemberStmt.setString(9, input.nextLine());
					
					insertAccountStmt.setInt(1, acct_id);
					System.out.println("Enter bank_id:");
					insertAccountStmt.setInt(2, input.nextInt());
					input.nextLine();
					System.out.println("Enter acct_type:");
					insertAccountStmt.setString(3, input.nextLine());
					System.out.println("Enter balance:");
					insertAccountStmt.setInt(4, input.nextInt());
					
					insertAccountStmt.addBatch();
					insertMemberStmt.addBatch();
					
					count++;
					System.out.println("Add more Members? (Y/N)");
					if (input.next().toLowerCase().startsWith("n")) {
						break;
					}
				}
				if (!conn.isClosed()) {
					System.out.println("There are " + count + " rolls waiting to insert. Do you wish to continue? (Y/N)");
					String crInput = input.next().toLowerCase();
				
					if (crInput.startsWith("y")) {
						insertAccountStmt.executeBatch();
						insertMemberStmt.executeBatch();
						conn.commit();
					} else {
						conn.rollback();
					}
				}
			} else if (choice == 3) {
				
				while (true) {
					System.out.println("Enter member id:");
					int mem_id = input.nextInt();
					
					String getBalance = "SELECT Account.acct_id, balance FROM Account INNER JOIN Member ON account.acct_id = member.acct_id " +
							"WHERE mem_id = " + mem_id + ";";
					
					ResultSet result = stmt.executeQuery(getBalance);
					int account_balance = result.getInt(2);
					int acct_id = result.getInt(1);
					System.out.println("Balance: $" + result.getInt(2) / 100.00);
					
					System.out.println("Enter amount to withdraw ($xxxx.xx):");
					double withdraw = input.nextDouble() * 100;
					
					if (result.getInt(1) >= (int)withdraw) {
						String getTransID = "SELECT max(tran_id) FROM ATM_transaction;";
						ResultSet trans_id_result = stmt.executeQuery(getTransID);
						int trans_id = trans_id_result.getInt(1) + 1;
						
						System.out.println("Enter atm_id:");
						int atm_id = input.nextInt();
						
						String atm_balance_sql = "SELECT balance, bank_id FROM ATM WHERE atm_id = " + atm_id + ";";
						
						ResultSet atm_balance_result = stmt.executeQuery(atm_balance_sql);
						int atm_balance = atm_balance_result.getInt(1);
						int bank_id = atm_balance_result.getInt(2);
						
						String bank_balance_sql = "SELECT total FROM Bank WHERE bank_id = " + bank_id + ";";
						ResultSet bank_balance_result = stmt.executeQuery(bank_balance_sql);
						int bank_balance = bank_balance_result.getInt(1);
						
						if (atm_balance >= (int)withdraw) {
							insertATMTransStmt.setInt(1, trans_id);
							insertATMTransStmt.setInt(2, atm_id);
							insertATMTransStmt.setInt(3, mem_id);
							insertATMTransStmt.setInt(4, (int)withdraw);
							
							String updateBank = "UPDATE bank SET total = " + (bank_balance - (int)withdraw) + 
									" WHERE bank_id = " + bank_id + ";";
							String updateAccount = "UPDATE account SET balance = " + (account_balance - (int)withdraw) + 
									" WHERE acct_id = " + acct_id + ";";
							String updateATM = "UPDATE ATM SET balance = " + (atm_balance - (int)withdraw) + 
									" WHERE atm_id = " + atm_id + ";";
							
							System.out.println("Confirm transaction (Y/N)");
							if (input.next().toLowerCase().startsWith("y")) {
								stmt.executeUpdate(updateBank);
								stmt.executeUpdate(updateAccount);
								stmt.executeUpdate(updateATM);
								insertATMTransStmt.executeUpdate();
								conn.commit();
								result = stmt.executeQuery(getBalance);
								System.out.println("New balance: $" + result.getInt(2) / 100.00);
							} else {
								System.out.println("Transaction cancelled!");
							}
							break;
						} else {
							System.out.println("Insufficient funds in ATM");
							break;
						}
						
					} else {
						System.out.println("Insufficient funds or bank does not have ATM");
						break;
					}
				}
			} else if (choice == 4) {
				quit = true;
			}
		}

		conn.close();
		input.close();
	}

}
