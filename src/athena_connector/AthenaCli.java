package athena_connector;

import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

// echo show tables in live_sync | java -classpath ./:./aws-java-sdk-1.11.98.jar:./AthenaJDBC41-1.0.0.jar Main s3://snippet-files/athena-results/ warehouse-app

public class AthenaCli {

	public static void main(String[] args) {

		// create the parser
		CommandLineParser parser = new DefaultParser();

		Option help = Option.builder("h").longOpt("help").required(false).desc("Shows this help message.").build();

		Option export_to_s3_file = Option.builder("e").longOpt("export_to_s3_file").required(false).hasArg()
				.desc("If provided, output will be sent to S3 in an performent way, by moving the result file in s3_staging_dir to the provided s3 file key. When provided the file will bewritten at first to temp subfolder inside s3_staging_dir and will be moved to s3_output_file from there. e.g. s3://mybucket/folder/filname.csv. The credetials provided should be permitted to write to that fiel location.")
				.build();

		Option results_formatter = Option.builder("f").longOpt("results_formatter").required(false).hasArg()
				.desc("Prints the result to stdout and formats it. By default it doesn't print the result, this option is faster, results are saved in any case to S3 in s3_staging_dir. Choose SingleValue in order just to print the fist column of the first row. TODO: JSON, CSV and ASCII table.")
				.build();

		Option s3_staging_dir = Option.builder("s").longOpt("s3_staging_dir").required(true).hasArg()
				.desc("The Amazon S3 location to which your query output is written. The JDBC driver then asks Athena to read the results and provide rows of data back to the user..")
				.build();

		Option aws_credentials_provider_class = Option.builder("c").longOpt("aws_credentials_provider_class").hasArg()
				.required(false)
				.desc("The credentials provider class name, which implements the AWSCredentialsProvider interface. The default is com.amazonaws.auth.DefaultAWSCredentialsProviderChain")
				.build();

		Option aws_credentials_provider_arguments = Option.builder("a").longOpt("aws_credentials_provider_arguments")
				.hasArg().required(false)
				.desc("Arguments for the credentials provider constructor as comma-separated values.").build();

		Option log_path = Option.builder("l").longOpt("log_path").hasArg().required(false)
				.desc("Local path of the Athena JDBC driver logs. If no log path is provided, then no log files are created. Default is athena_connector_jdbc.log")
				.build();

		Option log_level = Option.builder("v").longOpt("log_level").hasArg().required(false)
				.desc("Log level of the Athena JDBC driver logs. Valid values: INFO, DEBUG, WARN, ERROR, ALL, OFF, FATAL, TRACE. Default is WARN.")
				.build();

		Options options = new Options();

		options.addOption(help);
		options.addOption(results_formatter);
		options.addOption(export_to_s3_file);
		options.addOption(s3_staging_dir);
		options.addOption(aws_credentials_provider_class);
		options.addOption(aws_credentials_provider_arguments);
		options.addOption(log_path);
		options.addOption(log_level);

		try {
			// parse the command line arguments
			CommandLine cmdLine = parser.parse(options, args);

			if (cmdLine.hasOption("help")) {
				new HelpFormatter().printHelp("Amazon Athena CLI", options);
				return;
			}

			String sql = StdInReader.read();

			String s3_staging_dir_value = cmdLine.getOptionValue("s3_staging_dir");
			if (!StringUtils.isBlank(export_to_s3_file.getValue())) {
				String tempS3DirName = RandomStringUtils.randomAlphanumeric(36);
				s3_staging_dir_value = Paths.get(s3_staging_dir_value, tempS3DirName).toString();
			}

			AthenaConnector.execute(s3_staging_dir_value,
					cmdLine.getOptionValue("aws_credentials_provider_class",
							"com.amazonaws.auth.DefaultAWSCredentialsProviderChain"),
					cmdLine.getOptionValue("aws_credentials_provider_arguments", ""),
					cmdLine.getOptionValue("log_path", "athena_connector_jdbc.log"),
					cmdLine.getOptionValue("log_level", "WARN"), cmdLine.getOptionValue("results_formatter"), sql);

			if (!StringUtils.isBlank(export_to_s3_file.getValue())) {
			}

		} catch (ParseException exp) {
			// oops, something went wrong
			System.err.println(exp.getMessage());
			new HelpFormatter().printHelp("Amazon Athena CLI", options);
		}

	}
}
