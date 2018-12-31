### Build
run `./Dockerfile_build.sh` to build an application.

### Run localy
run `docker run -tid -p 8080:8080  zil_be:latest`

### access application
curl -k https://localhost:8080

### under the hood of build script

Major steps for building:
- create a Docker container with gradle 4.10.2 based on openjdk:8-jdk-alpine
        create a docker_file for gradle container to be built locally. Before building the gradle container we make cleanup of any previous versions of gradle container we might have locally.

- run a `gradle build` in this docker container ( get the application's artifact )
        while running the gradle build command in a gradle container we map our current folder inside the container. After the build process if over the build artifact appears in the current directory.

- create a Docker container with JRE and copy application in it
        Use alpine jre image to run an application. This image also has SSM-env application to get the parameters from the AWS parameter store. SSM-env set put parameters stored in parameter-store in environment variables, it also
        decrypts encrypted parameters.


- push the container with the application into ECR
        use `docker push` to put a container in ECR. We use SHA of a commit as a container tag in ECR.

- optional: use ecs-deploy script to update the ecr service with a new version of ecs task that have a new container version
        To make a deployment (make the new version running) the ecs-deploy script is used. It creates a new revision of ecs task, update ecs service with the new task version. We use DAEMON task placement strategy that tells 
	AWS to run one coppy of a task in every container instance in ECR cluster (exactly one!). It should be changed to REPLICA policy if more than one copy of a container should be run to provide fault tolerance in a containers.


# Notes for build
The application is run in AWS ECS service. You should adopt the script to fit your AWS account.

#### override application properies
a file ./src/main/resources/application.properties bare externalised properties. Any of them can be set as an environment variable or via java argument.


To change a listen port:
- via env variables
	(server.port interpolates into SERVER_PORT)
	$ export SERVER.PORT=8084 && java -jar ...

- via java args
	java -Dserver.port=8043 -jar ...

If a variable has an empty value in this file - it has to be set at an application start time, otherwise, an application will fail.

#### FlyWay
The FlyWay migration scripts are stored in ./src/main/resources/db folder. 

The Mysql schema should be created before the application start. The DDL is applied by Flyway.

