FROM openjdk:8-jre-alpine
#Alpine docker image doesn't have bash installed by default. 
#You will need to add following commands to get bash:
RUN apk add --no-cache bash
# Adding curl
RUN apk --no-cache add curl
ENV APPROOT="/app"   
WORKDIR $APPROOT
EXPOSE 8080
COPY target/customer-1.0.jar $APPROOT
ENTRYPOINT ["java"]
CMD ["-jar", "-Xms256m", "-Xmx256m", "customer-1.0.jar"]