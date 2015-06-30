package se_tpb_rmfCreator.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import com.codestudio.sql.PoolMan;

public class DbHandler {

	public static Connection getConnection() throws DatabaseException{
		return DbHandler.getPoolmanConnection(); 
	}
	public static Connection getPoolmanConnection() throws DatabaseException{
		
		Connection conn = null;
		
		try {
			DataSource ds = PoolMan.findDataSource("NDSDS");
			
			if (ds != null) {
			  conn = ds.getConnection();
			}else{
				throw new DatabaseException("- No DataSource -");
			}
			
			if(conn==null){
				throw new DatabaseException("No db connection");
			}
			
		} catch (SQLException sqlEx) {
			throw new DatabaseException(sqlEx);
		}

		return conn; 
	}
	
	public static void close(ResultSet rs, Statement stmt, Connection conn) throws DatabaseException{
		try {
			if(rs!=null){rs.close();}
		} catch (SQLException sqlEx) {
					throw new DatabaseException(sqlEx);
		}finally{
			try {
				if(stmt!=null){stmt.close();}
			} catch (SQLException sqlEx) {
					throw new DatabaseException(sqlEx);
			}finally{
				try {
					if(conn!=null){conn.close();}
				} catch (SQLException sqlEx) {
					throw new DatabaseException(sqlEx);
				}			
			}
		}
	}
			
}
