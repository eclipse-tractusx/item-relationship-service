# Data Integrity Testdata

## How to run

Execute the JAR

`DataIntegrityTransformer.jar 
    [ "path/to/testdatafile" ] 
    [ "path/to/outputfile" ] 
    [ "path/to/privatekeyfile" ]`

e.g.:
`java -jar DataIntegrityTransformer.jar "IRS_Testdata_v1.0.0-DataIntegrity.json" "IRS_Testdata_v1.0.0-DataIntegrity-IntegrityAspect.json" "pkcs8.key"`


## Generate key
`openssl genrsa -out keypair.pem 2048`

## Generate public certificate from key
`openssl rsa -in keypair.pem -pubout -out publickey.crt`

## How to build
Build the jar from IRS root

`mvn clean package -DskipTests -pl testdata-upload`

Copy the jar to `local/testing/testdata/dil/`

`cp .\testdata-upload\target\testdata-upload-0.0.2-SNAPSHOT-jar-with-dependencies.jar .\local\testing\testdata\dil\DataIntegrityTransformer.jar`

