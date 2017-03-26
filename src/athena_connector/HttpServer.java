package athena_connector;

import static spark.Spark.post;

public class HttpServer {
	public static void main(String[] args) {
		post("/athena_connector", (request, response) -> {

			String sql = request.queryParams("sql");
			String s3_staging_dir = request.queryParams("s3_staging_dir");
			String export_to_s3_file = request.queryParams("export_to_s3_file");
			String aws_credentials_provider_arguments = request.queryParams("aws_credentials_provider_arguments");
			String resultsFormatter = request.queryParams("resultsFormatter");

			String logPath = System.getenv("logPath") != null ? System.getenv("logPath")
					: "athena_connector_jdbc.log";
			
			String logLevel = System.getenv("log_level") != null
					? System.getenv("log_level") : "WARN";
			
			String aws_credentials_provider_class = System.getenv("athena_aws_credentials_provider_class") != null
					? System.getenv("athena_aws_credentials_provider_class")
					: "com.amazonaws.auth.DefaultAWSCredentialsProviderChain";

			if (aws_credentials_provider_arguments == null)
				aws_credentials_provider_arguments = "";

			System.out.println(sql);
			System.out.println(s3_staging_dir);
			System.out.println(export_to_s3_file);
			System.out.println(aws_credentials_provider_class);
			System.out.println(aws_credentials_provider_arguments);
			System.out.println(logPath);
			System.out.println(logLevel);
			System.out.println(resultsFormatter);

			Command.execute(sql, s3_staging_dir, export_to_s3_file, aws_credentials_provider_class,
					aws_credentials_provider_arguments, logPath, logLevel, resultsFormatter);

			return "Success";
		});
	}
}
