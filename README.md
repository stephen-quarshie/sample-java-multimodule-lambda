# Sample Java Multi-Module Lambda Project

---
This is a sample multi-module Java maven project for developing REST APIs with AWS Lambda. The intend is to provide you with a local development environemnt for developing a serverless application.\
This project touches on the following AWS services and development tools.
- Lambda functions
- APIGateway
- DynamoDB
- AWS CLI
- SAM CLI

NOTE: You will not require an AWS account to run the application. The instructions for running the application have currently only been tested on a Mac.\

## Developer Setup
Installations are available for Linux, Mac & Windows. Install the appropriate software for your operating system.

1. Install [Amazom Java 11 Corretto](https://docs.aws.amazon.com/corretto/latest/corretto-11-ug/what-is-corretto-11.html)
    - NOTE: If you are using Linux or a Mac, you can use [SDKMAN](https://sdkman.io/install) to install Java 11 Corretto.
    - At the time of writing the latest version of Java runtime for Lambda functions was Java 11. Consult AWS [documentation](https://docs.aws.amazon.com/lambda/latest/dg/lambda-runtimes.html) for latest runtimes. 
2. Download [Maven](https://maven.apache.org/download.cgi) and [install](https://maven.apache.org/install.html)
3. Install [Intellij Community Edition IDE](https://www.jetbrains.com/idea/download)
    - NOTE: You can use any other Java IDE
4. Install [Docker Desktop](https://www.docker.com/products/docker-desktop)
5. Install [AWS CLI 2](https://docs.aws.amazon.com/cli/latest/userguide/install-cliv2.html)
6. Install [AWS SAM CLI](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-install.html)
    - Note: You do not need an AWS account to use the AWS SAM CLI locally for development and can therefore skip to the following steps;
        - "Create AWS account"
        - "Create an IAM user with administrator permissions"
        - "Install Docker" (which was already installed in 4)
7. Install [DynamoDB NoSQL workbench](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/workbench.settingup.html)
8. Install [Postman](https://www.postman.com/downloads/)

## Running The Application 
Clone the application

#### Start Local DynamoDb instance
In the terminal window of Intellij or the command line, navigate to the project root.\
Create a docker network. The Lambda functions will use this network to communicate with the DynamoDB instance.
```bash
$ docker network create -d bridge lambda-local
```
Run docker container for dynamodb on the network that we just created above.\
NOTE:
- filepath-to-create-dynamodb-table: a folder where you want to dynamodb data persisted.\  
Note: This path must be within one of the default or set directories that can be mounted into  a docker container.\
See your docker dashboard > settings > Resources
```bash
$ docker run -d --rm \
    -v [filepath-to-create-dynamodb-table]:/home/dynamodblocal/data \
    --name local-dynamodb \
    --network lambda-local \
    -p 8000:8000 amazon/dynamodb-local \
    -Djava.library.path=./DynamoDBLocal_lib -jar DynamoDBLocal.jar -sharedDb -dbPath ./data
```

#### Create DynamoDB tables
Create "Students" table
```bash
$ aws dynamodb create-table \
      --table-name Students \
      --attribute-definitions \
          AttributeName=id,AttributeType=S \
      --key-schema \
          AttributeName=id,KeyType=HASH \
  --provisioned-throughput \
          ReadCapacityUnits=5,WriteCapacityUnits=5 \
  --endpoint-url http://localhost:8000
```
Confirm that tables exists.
```bash
$ aws dynamodb list-tables  --endpoint-url http://localhost:8000
```

#### Connect to DynamoDB using DynamoDB NoSQL Workbench (Optional)
This step is not required, but will provide you with a GUI to view and manipulate you dynamo data.\
Alternatively you can access and manipulate your data from the command line; see [DynamoDB CLI](https://awscli.amazonaws.com/v2/documentation/api/latest/reference/dynamodb/index.html)
1. Open DynamoDB NoSQL Workbench
2. On the left side bar click on "Operation Builder"
3. Click "Add Connection"
4. Select "DynamoDB local"
5. Enter a connection name and click connect
6. Click on the connection created in the Active Connections list

#### Build the application
In the terminal window of Intellij or the command line, navigate to the project root.\
NOTE: You only need to run this command everytime you make changes to the java code
```bash
$ mvn clean install
```

#### Start SAM (Serverless Application Model)
In the terminal window of Intellij or the command line, navigate to the project root.
NOTE: This command will have to be re-run every time you make changes to the template.yaml file. \
The template.yaml file is what SAM uses to configure your api and what lambda functions to calls when a request to make to an endpoint
```bash
$ sam local start-api --docker-network lambda-local
``` 
#### Test the api endpoints
At this point your API should be ready for you to test locally. 
Open Postman and import the [collection](sample-java-multimodule-lambda.postman_collection.json).
