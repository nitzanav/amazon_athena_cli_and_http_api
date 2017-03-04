package athena_connector.resultset_formaters;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface IResultSetFormatter {
	void print(ResultSet rs) throws SQLException;
}
