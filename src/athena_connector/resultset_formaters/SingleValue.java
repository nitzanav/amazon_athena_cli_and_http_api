package athena_connector.resultset_formaters;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SingleValue implements IResultSetFormatter {

	@Override
	public void print(ResultSet rs) throws SQLException {
		rs.next();
		System.out.println(rs.getString(1));
	}

}
