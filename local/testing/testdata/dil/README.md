# Data Integrity Testdata

## How to run

Execute the JAR

`DataIntegrityTransformer.jar 
    [ "path/to/testdatafile" ] 
    [ "path/to/outputfile" ] 
    [ "path/to/privatekeyfile" ] 
    [ "path/to/publickeyfile" ]`

e.g.:
`java -jar DataIntegrityTransformer.jar "IRS_Testdata_v1.0.0-DataIntegrity.json" "IRS_Testdata_v1.0.0-DataIntegrity-IntegrityAspect.json" "priv-key.pem" "pub-key.pem"`


## Generate private key
`openssl genrsa -out priv-key.pem 2048`

## Generate public key from private key
`openssl rsa -in priv-key.pem -pubout -outform PEM -out pub-key.pem`


## How to build
Build the jar from IRS root

`mvn clean package -DskipTests -pl irs-testdata-upload`

Copy the jar to `local/testing/testdata/dil/`

`cp .\irs-testdata-upload\target\irs-testdata-upload-0.0.2-SNAPSHOT-jar-with-dependencies.jar .\local\testing\testdata\dil\DataIntegrityTransformer.jar`

