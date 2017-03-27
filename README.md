# amazon_athena_cli_and_http_server
Amazon Athena CLI and HTTP Server for running athena SQL, exporting athena SQL to CSV, using CLI and HTTP.

## Install
mvn clean install

## Execute AWS Athena CLI
echo select 1 | java -jar athena-cli.jar -s s3://my_bucket/athena-results/ -f SingleValue -e s3://my_bucket/export/athena/csv/to/ath_test.csv

This will export CSV to s3://my_bucket/export/athena/csv/to/ath_test.csv and will look for credentials using DefaultAWSCredentialsProviderChain, at Environment Variables, ~/.aws/credentials, and more.

If you want to use different profile use `-c com.amazonaws.auth.profile.ProfileCredentialsProvider -a profile-name` or read on [AWSCredentialsProvider](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/AWSCredentialsProvider.html).
