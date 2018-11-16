#!/bin/bash 

if [ "$#" -ne 1 ]; then
    echo " Accepting exsaclty 1 parameter - aws config profile name! Exiting... "
    exit 1
fi


tag=`date +%s`
cont_name=javabuild


echo -e "\t \t \t \t \t \t \t \t \t \t Deleting all versions of build container"

docker ps -a | grep $cont_name | awk {'print $1'} |  while read CONT_ID; do docker rm $CONT_ID --force; done 
docker images -a |grep $cont_name | awk {'print $3'}| uniq | while read IMAGE_ID; do docker rmi $IMAGE_ID --force; done

echo -e "\t \t \t \t \t \t \t \t \t \tClean workspace"

sudo rm -rfv ./build .gradle

cat > Dockerfile_build << EOF
FROM openjdk:8-jdk-alpine
CMD ["gradle"]

ENV GRADLE_HOME /opt/gradle
ENV GRADLE_VERSION 4.10.2

RUN apk add --no-cache bash bash-completion curl
ARG GRADLE_DOWNLOAD_SHA256=b49c6da1b2cb67a0caf6c7480630b51c70a11ca2016ff2f555eaeda863143a29
RUN set -o errexit -o nounset \
	&& wget -O gradle.zip "https://services.gradle.org/distributions/gradle-\${GRADLE_VERSION}-bin.zip" \
	&& echo "\${GRADLE_DOWNLOAD_SHA256} *gradle.zip" | sha256sum -c - \
	&& unzip gradle.zip \
	&& rm gradle.zip \
	&& mkdir /opt \
	&& mv "gradle-\${GRADLE_VERSION}" "\${GRADLE_HOME}/" \
	&& ln -s "\${GRADLE_HOME}/bin/gradle" /usr/bin/gradle 
RUN mkdir /home/gradle
WORKDIR /home/gradle
EOF

echo -e "\t \t \t \t \t \t \t \t \t \t Building a gradle container"
docker build  -f Dockerfile_build -t $cont_name:$tag .

rm -rfv Dockerfile_build

echo -e "\t \t \t \t \t \t \t \t \t \t Building a project"
docker run -v "$(pwd)":/home/gradle  -ti $cont_name:$tag gradle clean build -x test

echo -e "\t \t \t \t \t \t \t \t \t \t Find the artifacts in ./build/libs/ "



cat > Dockerfile << EOF
FROM java:8u111-jre-alpine
LABEL name="zil_be"

Run set -x \ && apk add --no-cache  bash bash-completion curl openjdk8

RUN curl -L https://github.com/ags/ssm-env/releases/download/v0.0.0/ssm-env > /usr/local/bin/ssm-env && \
      cd /usr/local/bin && \
      echo 384ac29bec6ff6251893f1dba46d00b6 ssm-env && md5sum ssm-env | md5sum -c && \
      chmod +x ssm-env

ENV LANG C.UTF-8
RUN mkdir -p /opt/app/jar/
COPY ./build/libs/*.jar /opt/app/jar/zil.jar
WORKDIR /opt/app/jar/
ENTRYPOINT ["/usr/local/bin/ssm-env", "-with-decryption"]
CMD [ "/bin/bash", "-c", "java \$JAVA_OPTIONS -jar zil.jar "]

EOF

echo -e "\t \t \t \t \t \t \t \t \t \t Building container "
sha=`git log -n 1 --pretty=format:"%H"`
echo $sha
docker build -t 140914792638.dkr.ecr.eu-central-1.amazonaws.com/zil-explorer-be_dev:$sha .

rm Dockerfile 

$(aws ecr get-login --no-include-email --region eu-central-1 --profile $1)

echo -e "\t \t \t \t \t \t \t \t \t \t Pushing container to ECR 140914792638.dkr.ecr.eu-central-1.amazonaws.com/zil-explorer-be_dev:$sha"

docker push 140914792638.dkr.ecr.eu-central-1.amazonaws.com/zil-explorer-be_dev:$sha

echo -e "\t \t \t \t \t \t \t \t \t \t Cleanin docker image"

docker rmi 140914792638.dkr.ecr.eu-central-1.amazonaws.com/zil-explorer-be_dev:$sha 

echo -e "\t \t \t \t \t \t \t \t \t \t Deploying new container "

./ecs-deploy -c ZIL_dev -n zil_be_dev -i 140914792638.dkr.ecr.eu-central-1.amazonaws.com/zil-explorer-be_dev:$sha --max-definitions 4 -r eu-central-1 --profile $1


