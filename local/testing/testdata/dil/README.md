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

## Testdata
The provided test data file [IRS_Testdata_v1.0.0-DataIntegrity-IntegrityAspect-with-invalid-vehicles.json](IRS_Testdata_v1.0.0-DataIntegrity-IntegrityAspect-with-invalid-vehicles.json)
includes 4 vehicles. 
- Vehicle A
  - `urn:uuid:a1fa0f85-697d-4c9d-982f-2501af8e8636`
  - Complete and valid integrity aspects
- Vehicle B
  - `urn:uuid:05abf6ff-8c78-4b72-948b-40e08e9b83f3`
  - 1 Part `urn:uuid:dc60fc50-c875-4ce6-a1b9-d59c4c1e0b17` has payload which changed after the hash was created
- Vehicle C
  - `urn:uuid:17e11d67-0315-4504-82cd-8e70a8c33a6a`
  - Signatures for Part `urn:uuid:8c437b9d-f1b8-4397-b030-c3637eaf9b53` were created with a different private key
- Vehicle D
  - `urn:uuid:5672e8ff-8a73-425e-b2a5-5561b5b21d7a`
  - IntegrityAspect for part `urn:uuid:ef3865b8-6811-4659-a1b5-e186f8e42258` is missing
