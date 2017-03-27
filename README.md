# amazon_athena_cli_and_http_server
Amazon Athena CLI and HTTP Server for running athena SQL, exporting athena SQL to CSV, using CLI and HTTP.

## Install
mvn clean install

## Execute AWS Athena CLI
`$ echo select 1 | java -jar target/athena-cli.jar -s s3://my_bucket/athena-results/ -f SingleValue -e s3://my_bucket/export/athena/csv/to/ath_test.csv`

This will export CSV to s3://my_bucket/export/athena/csv/to/ath_test.csv and will look for credentials using DefaultAWSCredentialsProviderChain, at Environment Variables, ~/.aws/credentials, and more.

If you want to use different profile use `-c com.amazonaws.auth.profile.ProfileCredentialsProvider -a profile-name` or read on [AWSCredentialsProvider](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/AWSCredentialsProvider.html).

## Get help on executing Amazon Athena CLI
`$ java -jar target/athena-cli.jar`
<pre>
Missing required option: s
usage: Amazon Athena CLI
 -a,--aws_credentials_provider_arguments <arg>   Arguments for the
                                                 credentials provider
                                                 constructor as
                                                 comma-separated values.
 -c,--aws_credentials_provider_class <arg>       The credentials provider
                                                 class name, which
                                                 implements the
                                                 AWSCredentialsProvider
                                                 interface. The default is
                                                 com.amazonaws.auth.Defaul
                                                 tAWSCredentialsProviderCh
                                                 ain
 -e,--export_to_s3_file <arg>                    If provided, output will
                                                 be sent to S3 in an
                                                 performent way, by moving
                                                 the result file in
                                                 s3_staging_dir to the
                                                 provided s3 file key.
                                                 When provided the file
                                                 will bewritten at first
                                                 to temp subfolder inside
                                                 s3_staging_dir and will
                                                 be moved to
                                                 s3_output_file from
                                                 there. e.g.
                                                 s3://mybucket/folder/filn
                                                 ame.csv. The credetials
                                                 provided should be
                                                 permitted to write to
                                                 that fiel location.
 -f,--results_formatter <arg>                    Prints the result to
                                                 stdout and formats it. By
                                                 default it doesn't print
                                                 the result, this option
                                                 is faster, results are
                                                 saved in any case to S3
                                                 in s3_staging_dir. Choose
                                                 SingleValue in order just
                                                 to print the fist column
                                                 of the first row. TODO:
                                                 JSON, CSV and ASCII
                                                 table.
 -h,--help                                       Shows this help message.
 -l,--log_path <arg>                             Local path of the Athena
                                                 JDBC driver logs. If no
                                                 log path is provided,
                                                 then no log files are
                                                 created. Default is
                                                 athena_connector_jdbc.log
 -s,--s3_staging_dir <arg>                       The Amazon S3 location to
                                                 which your query output
                                                 is written. The JDBC
                                                 driver then asks Athena
                                                 to read the results and
                                                 provide rows of data back
                                                 to the user..
 -v,--log_level <arg>                            Log level of the Athena
                                                 JDBC driver logs. Valid
                                                 values: INFO, DEBUG,
                                                 WARN, ERROR, ALL, OFF,
                                                 FATAL, TRACE. Default is
                                                 WARN.
                                                 </pre>
