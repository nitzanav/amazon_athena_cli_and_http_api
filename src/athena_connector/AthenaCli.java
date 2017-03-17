package athena_connector;

import java.io.FileNotFoundException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import com.amazonaws.athena.jdbc.shaded.com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class AthenaCli {

	public static void main(String[] args) throws FileNotFoundException {

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
			String export_to_s3_file_value = cmdLine.getOptionValue("export_to_s3_file");
			if (!StringUtils.isBlank(export_to_s3_file_value)) {
				String tempS3DirName = UUID.randomUUID().toString();
				s3_staging_dir_value = s3_staging_dir_value + tempS3DirName + "/";
			}
			

			String aws_credentials_provider_class_value = cmdLine.getOptionValue("aws_credentials_provider_class",
					"com.amazonaws.auth.DefaultAWSCredentialsProviderChain");
			String aws_credentials_provider_arguments_value = cmdLine.getOptionValue("aws_credentials_provider_arguments", "");
			
			AthenaConnector.execute(s3_staging_dir_value,
					aws_credentials_provider_class_value,
					aws_credentials_provider_arguments_value,
					cmdLine.getOptionValue("log_path", "athena_connector_jdbc.log"),
					cmdLine.getOptionValue("log_level", "WARN"), cmdLine.getOptionValue("results_formatter"), sql);

			if (!StringUtils.isBlank(export_to_s3_file_value)) {
				AmazonS3 s3client = AmazonS3ClientBuilder.standard().withCredentials(new ProfileCredentialsProvider(aws_credentials_provider_arguments_value)).build();

				
				String s3StagingBucketName = getBucketName(s3_staging_dir_value);
				String s3StagingFileKey = s3_staging_dir_value.substring(s3StagingBucketName.length() + 6);
				System.out.println(s3StagingBucketName);
				System.out.println(s3StagingFileKey);
				String athenaResultS3Key = getAthenaResultFileS3Path(s3client, s3StagingBucketName, s3StagingFileKey); 

				String exportBucketName = getBucketName(export_to_s3_file_value);
				String exportFileKey = export_to_s3_file_value.substring(exportBucketName.length() + 6);
				System.out.println(s3StagingBucketName);
				System.out.println(athenaResultS3Key);
				System.out.println(exportBucketName);
				System.out.println(exportFileKey);
				s3client.copyObject(s3StagingBucketName, athenaResultS3Key, exportBucketName, exportFileKey);
			}

		} catch (ParseException exp) {
			// oops, something went wrong
			System.err.println(exp.getMessage());
			new HelpFormatter().printHelp("Amazon Athena CLI", options);
		}

	}

	private static String getBucketName(String export_to_s3_file_value) {
		Pattern pattern = Pattern.compile("s3://(.+?)/");
		Matcher matcher = pattern.matcher(export_to_s3_file_value);

		if (matcher.find()) {
			return matcher.group(1);
		} else {
		    throw new IllegalArgumentException("doesn't follow the pattern of s3://mybucket/foo/bar.");
		}
	}

	private static String getAthenaResultFileS3Path(AmazonS3 s3client, String bucketName, String athenaResultDirPrefix) throws FileNotFoundException {
		final ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucketName).withPrefix(athenaResultDirPrefix).withMaxKeys(2);
		ListObjectsV2Result result;
		do {               
		   result = s3client.listObjectsV2(req);
		   
		   for (S3ObjectSummary objectSummary : 
		       result.getObjectSummaries()) {
			   if (objectSummary.getKey().endsWith(".csv"))
				   return objectSummary.getKey();
		   }
		   req.setContinuationToken(result.getNextContinuationToken());
		} while(result.isTruncated() == true );
		
		throw new FileNotFoundException("Athena result file.");
	}
	
}
