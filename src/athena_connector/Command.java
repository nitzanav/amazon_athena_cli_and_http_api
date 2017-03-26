package athena_connector;

import java.io.FileNotFoundException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.athena.jdbc.shaded.com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class Command {

	public static void execute(String sql, String s3_staging_dir, String export_to_s3_file_value,
			String aws_credentials_provider_class, String aws_credentials_provider_arguments, String logPath,
			String logLevel, String resultsFormatter) throws FileNotFoundException {

		if (!StringUtils.isBlank(export_to_s3_file_value)) {
			String tempS3DirName = UUID.randomUUID().toString();
			s3_staging_dir = s3_staging_dir + tempS3DirName + "/";
		}

		AthenaConnector.execute(s3_staging_dir, aws_credentials_provider_class, aws_credentials_provider_arguments,
				logPath, logLevel, resultsFormatter, sql);

		if (!StringUtils.isBlank(export_to_s3_file_value)) {
			AmazonS3 s3client = AmazonS3ClientBuilder.standard()
					.withCredentials(new ProfileCredentialsProvider(aws_credentials_provider_arguments)).build();

			String s3StagingBucketName = getBucketName(s3_staging_dir);
			String s3StagingFileKey = s3_staging_dir.substring(s3StagingBucketName.length() + 6);
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

	private static String getAthenaResultFileS3Path(AmazonS3 s3client, String bucketName, String athenaResultDirPrefix)
			throws FileNotFoundException {
		final ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucketName)
				.withPrefix(athenaResultDirPrefix).withMaxKeys(2);
		ListObjectsV2Result result;
		do {
			result = s3client.listObjectsV2(req);

			for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
				if (!objectSummary.getKey().endsWith(".metadata"))
					return objectSummary.getKey();
			}
			req.setContinuationToken(result.getNextContinuationToken());
		} while (result.isTruncated() == true);

		throw new FileNotFoundException("Athena result file.");
	}

}
