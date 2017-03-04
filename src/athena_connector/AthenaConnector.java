package athena_connector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import athena_connector.resultset_formaters.IResultSetFormatter;

public class AthenaConnector {

	static final String athenaUrl = "jdbc:awsathena://athena.us-east-1.amazonaws.com:443";

	public static void execute(String s3_staging_dir, String aws_credentials_provider_class,
			String aws_credentials_provider_arguments, String log_path, String log_level, String resultSetFormatter, String sql) {

		Connection conn = null;
		Statement statement = null;
		ResultSet rs = null;

		try {

			Class.forName("com.amazonaws.athena.jdbc.AthenaDriver");

			Properties info = new Properties();
			info.put("s3_staging_dir", s3_staging_dir);
			info.put("log_path", log_path);
			info.put("log_level", log_level);
			info.put("aws_credentials_provider_class", aws_credentials_provider_class);
			info.put("aws_credentials_provider_arguments", aws_credentials_provider_arguments);

			conn = DriverManager.getConnection(athenaUrl, info);
			statement = conn.createStatement();
			boolean hasResultSet = statement.execute(sql);
			if(hasResultSet && resultSetFormatter != null) {
				IResultSetFormatter formater = athena_connector.resultset_formaters.Factory.create(resultSetFormatter);
				rs = statement.getResultSet();
				formater.print(rs);
				rs.close();
			}
			statement.close();
			conn.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			try {
				if (statement != null)
					statement.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			try {
				if (conn != null)
					conn.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

}
