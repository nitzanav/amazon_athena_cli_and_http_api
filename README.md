# amazon_athena_cli_and_http_server
Amazon Athena CLI and HTTP Server for running athena SQL, exporting athena SQL to CSV, using CLI and HTTP.

## Execute AWS Athena CLI to epost as CSV
`$ echo select 1 | java -jar target/athena-cli.jar -s s3://my_bucket/athena-results/ -f SingleValue -e s3://my_bucket/export/athena/csv/to/ath_test.csv`

This will export CSV to s3://my_bucket/export/athena/csv/to/ath_test.csv and will look for credentials using DefaultAWSCredentialsProviderChain, at Environment Variables, ~/.aws/credentials, and more.

If you want to use different profile use `-c com.amazonaws.auth.profile.ProfileCredentialsProvider -a profile-name` or read on [AWSCredentialsProvider](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/AWSCredentialsProvider.html).

## to export as JSON
contribute and implement at https://github.com/nitzanav/amazon_athena_cli_and_http_server/tree/master/src/athena_connector/resultset_formaters

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

## How to start a web server
You can launch a microservice (secured only to you internal VPC becuase there is no athentication!) instead of using CLI.
```
$ java -jar target/athena-http-server.jar
[Thread-0] INFO org.eclipse.jetty.util.log - Logging initialized @193ms
[Thread-0] INFO spark.embeddedserver.jetty.EmbeddedJettyServer - == Spark has ignited ...
[Thread-0] INFO spark.embeddedserver.jetty.EmbeddedJettyServer - >> Listening on 0.0.0.0:4567
[Thread-0] INFO org.eclipse.jetty.server.Server - jetty-9.3.z-SNAPSHOT
[Thread-0] INFO org.eclipse.jetty.server.ServerConnector - Started ServerConnector@5fde0aa0{HTTP/1.1,[http/1.1]}{0.0.0.0:4567}
[Thread-0] INFO org.eclipse.jetty.server.Server - Started @341ms
```

Now do something like:
```
curl --request POST http://localhost:4567/athena_connector -H 'Content-Type: application/json' -F "sql=SELECT+1&s3_staging_dir=s3://my_bucket/athena-results/&export_to_s3_file=s3://my_bucket/export/athena/csv/to/ath_test.csv"
```

For more parameters see https://github.com/nitzanav/amazon_athena_cli_and_http_server/blob/master/src/athena_connector/HttpServer.java

## How to Build Sources
`mvn clean install`

you wil need to google up how to install java and maven if you don't have it.


