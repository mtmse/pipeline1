package se_tpb_rmfCreator.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class BookDAO {
	
	public static Map<String,String> retrieveBookProperties(String tpbNr) throws DatabaseException{
		Map<String,String> props = new LinkedHashMap<String, String>();
	
		Connection conn = DbHandler.getConnection();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		final String name_COLUMN = "name";
		final String value_COLUMN = "value";
		
		try {
			String query ="SELECT "+name_COLUMN+","+value_COLUMN +
						  " FROM book, bookproperty " +
						  " WHERE book.tpbNr = ? " +
						  " AND book.id=bookproperty.owner_id"; 
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1,tpbNr);
			
			rs = pstmt.executeQuery();
			while(rs.next()){
				props.put(rs.getString(name_COLUMN),rs.getString(value_COLUMN));
			}
		}catch (SQLException e) {
			DatabaseException dstEx = new DatabaseException("BookDAO#retrieveBookProperties: "+tpbNr+", "+e.getMessage(),e);
			throw dstEx;
		}finally{
			DbHandler.close(rs,pstmt,conn);
		}
			
		return props;
	}

}
