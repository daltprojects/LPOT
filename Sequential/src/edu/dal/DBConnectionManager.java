package edu.dal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import edu.models.UserItemRating;
import edu.models.UserTrustRating;

public class DBConnectionManager {

	static String dbUrl = "jdbc:mysql://127.0.0.1:3306/epinions_2";
	static String dbClass = "com.mysql.jdbc.Driver";
	static String user_item_query = "Select * FROM user_item_ratings ";
	static String user_item_query_user_id = "Select * FROM user_item_ratings where user_id=$$";
	static String user_item_query_item_id = "Select * FROM user_item_ratings where item_id=$$";
	static String user_trust_query = "Select * FROM user_trust";
	static String user_item_query_user_item_id = "Select * FROM user_item_ratings where user_id=$$ and item_id=%%";
	static String delete_user_item_query_user_item_id = "delete FROM user_item_ratings where user_id=$$ and item_id=%%";
	static String insert_user_item_query_user_item_id = "insert into user_item_ratings values ($$, %%, ##)";

	public static long hits = 0;

	public static Connection getConnection() {
		Connection con = null;
		try {

			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection(dbUrl, "root", null);

		} // end try

		catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return con;
	}

	public static List<UserItemRating> getUserItemRatings() {
		List<UserItemRating> retVal = new ArrayList<UserItemRating>();
		Connection con = null;
		try {
			hits++;
			con = getConnection();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(user_item_query);
			while (rs.next()) {
				UserItemRating userItemRating = new UserItemRating();
				userItemRating.setUserId(rs.getLong(1));
				userItemRating.setItemId(rs.getLong(2));
				userItemRating.setRating(rs.getFloat(3));
				retVal.add(userItemRating);
			} // e
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return retVal;
	}

	public static List<UserItemRating> getUserItemRatings(Long userId) {
		List<UserItemRating> retVal = new ArrayList<UserItemRating>();
		Connection con = null;
		try {
			con = getConnection();
			hits++;
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(user_item_query_user_id.replace(
					"$$", userId + ""));
			while (rs.next()) {
				UserItemRating userItemRating = new UserItemRating();
				userItemRating.setUserId(rs.getLong(1));
				userItemRating.setItemId(rs.getLong(2));
				userItemRating.setRating(rs.getFloat(3));
				retVal.add(userItemRating);
			} // e
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return retVal;
	}

	public static void deleteUserItemRating(UserItemRating userItemRating) {

		Connection con = null;
		try {
			con = getConnection();
			hits++;
			Statement stmt = con.createStatement();
			String query = delete_user_item_query_user_item_id.replace("$$",
					userItemRating.getUserId() + "");
			query = query.replace("%%", userItemRating.getItemId() + "");
			stmt.executeUpdate(query);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	public static void insertUserItemRating(UserItemRating userItemRating) {

		Connection con = null;
		try {
			con = getConnection();
			Statement stmt = con.createStatement();
			String query = insert_user_item_query_user_item_id.replace("$$",
					userItemRating.getUserId() + "");
			query = query.replace("%%", userItemRating.getItemId() + "");
			query = query.replace("##", userItemRating.getRating() + "");
			stmt.executeUpdate(query);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	public static UserItemRating getUserItemRatingByUserIdItemId(Long userId,
			Long itemId) {
		UserItemRating retVal = null;
		Connection con = null;
		try {
			con = getConnection();
			hits++;
			Statement stmt = con.createStatement();
			String query = user_item_query_user_item_id.replace("$$", userId
					+ "");
			query = query.replace("%%", itemId + "");
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next()) {
				retVal = new UserItemRating();
				retVal.setUserId(rs.getLong(1));
				retVal.setItemId(rs.getLong(2));
				retVal.setRating(rs.getFloat(3));

			} // e
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return retVal;
	}

	public static List<UserItemRating> getUserItemRatingsForItemId(Long itemId) {
		List<UserItemRating> retVal = new ArrayList<UserItemRating>();
		Connection con = null;
		try {
			con = getConnection();
			hits++;
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(user_item_query_item_id.replace(
					"$$", itemId + ""));
			while (rs.next()) {
				UserItemRating userItemRating = new UserItemRating();
				userItemRating.setUserId(rs.getLong(1));
				userItemRating.setItemId(rs.getLong(2));
				userItemRating.setRating(rs.getFloat(3));
				retVal.add(userItemRating);
			} // e
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return retVal;
	}

	public static List<UserTrustRating> getUserTrustRatings() {
		List<UserTrustRating> retVal = new ArrayList<UserTrustRating>();
		Connection con = null;
		try {
			con = getConnection();
			hits++;
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(user_trust_query);
			while (rs.next()) {
				UserTrustRating userTrustRating = new UserTrustRating();
				userTrustRating.setUserId(rs.getLong(1));
				userTrustRating.setOtherUserId(rs.getLong(2));
				userTrustRating.setTrustRating(rs.getFloat(3));
				retVal.add(userTrustRating);
			} // e
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return retVal;
	}

	public static void main(String[] args) {
		List<UserTrustRating> ratings = getUserTrustRatings();
		for (UserTrustRating rating : ratings) {
			System.out.println(rating.getUserId());
		}
	}

}
