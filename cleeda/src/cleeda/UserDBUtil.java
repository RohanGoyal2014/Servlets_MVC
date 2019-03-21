package cleeda;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.logging.Logger;

import javax.sql.DataSource;

public class UserDBUtil {

	private DataSource dataSource;

	public UserDBUtil(DataSource dataSource) {
		super();
		this.dataSource = dataSource;
	}
	
	private void close(Connection conn,Statement stmt,ResultSet rs) throws Exception{
		
		if(conn!=null) {
			conn.close();
		}
		if(stmt!=null) {
			stmt.close();
		}
		if(rs!=null) {
			rs.close();
		}
	}

	public boolean doesEmailExist(String email) throws Exception {
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet result = null;
		
		try {
			conn = dataSource.getConnection();
			
			String sql="select count(*) 'count' from users where email=?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, email);
			result = stmt.executeQuery();
			if(result.next()) {
				return (result.getInt("count")!=0);
			}
			return true; 
		} catch(Exception e) {
			e.printStackTrace();
			return true;
		}finally {
			close(conn,stmt,result);
		}
		
	}

	public boolean createUser(User user) throws Exception{
		
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			
			conn=dataSource.getConnection();
			
			String sql="insert into users(firstName,lastName,email,password) values(?,?,?,?)";
			stmt=conn.prepareStatement(sql);
			stmt.setString(1, user.getFirstName());
			stmt.setString(2, user.getLastName());
			stmt.setString(3, user.getEmail());
			stmt.setString(4, encrypt(user.getPassword()));
			stmt.execute();
			return true;
		
		} catch(Exception exc) {
			exc.printStackTrace();
			return false;
		} finally {
			close(conn,stmt,null);
		}
		
	}
	
	private String encrypt(String input) throws Exception {
		
		MessageDigest md = MessageDigest.getInstance("SHA-256"); 
        byte[] messageDigest = md.digest(input.getBytes()); 

        // Convert byte array into signum representation 
        BigInteger no = new BigInteger(1, messageDigest); 

        // Convert message digest into hex value 
        String hashtext = no.toString(16); 

        while (hashtext.length() < 32) { 
            hashtext = "0" + hashtext; 
        } 

        return hashtext;
		
	}

	public User performLogin(String email, String password) throws Exception{
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			
			conn = dataSource.getConnection();
			
			String sql = "select * from users where email = ? and password = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, email);
			stmt.setString(2, encrypt(password));
			resultSet = stmt.executeQuery();
			if(resultSet.next()) {
				
				String firstName = resultSet.getString("firstName");
				String lastName = resultSet.getString("lastName");
				
				return new User(firstName,lastName,email,null);
			}
			return null;
			
		} catch( Exception e) {
			return null;
		} finally {
			
			close(conn,stmt,resultSet);
			
		}
		
	}

	public void setPasswordRecoveryOn(String email, long timeLimit) throws Exception{
		
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
			conn = dataSource.getConnection();
			
			String sql="update users set recoveryOn = ?, timeLimit = ? where email=?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, 1);
			stmt.setString(2, String.valueOf(timeLimit));
			stmt.setString(3, email);
			stmt.execute();
		} finally {
			close(conn,stmt,null);
		}
		
	}

	public boolean isValidRecovery(String email) throws Exception {
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null; 
		
		try {
			
			conn = dataSource.getConnection();
			
			String sql = "select recoveryOn,timeLimit from users where email = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, email);
			rs=stmt.executeQuery();
			if(rs.next()) {
				int recoveryMode = rs.getInt("recoveryOn");
				long timeLimit = Long.parseLong(rs.getString("timeLimit"));
				if(recoveryMode == 0) {
					return false;
				} else {
					long getTime =  new Date().getTime();
					if(getTime>timeLimit) {
						System.out.println("Time mismatch"+getTime);
						return false;
					}
					return true;
				} 
			}
			return false;
			
		} catch(Exception e) {
			return false;
		} finally {
			close(conn,stmt,rs);
		}
	}

	public void updatePassword(String email, String password) throws Exception {
		
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
			conn = dataSource.getConnection();
			
			String sql = "update users set password = ?, recoveryOn = 0"
					+ " ,timeLimit = ? where email = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, encrypt(password));
			stmt.setString(2, "0");
			stmt.setString(3, email);
			stmt.execute();
		} finally {
			close(conn,stmt,null);
		}
		
	}
	
	
}
