FROM openjdk:8-jre-alpine
#Alpine docker image doesn't have bash installed by default. 
#You will need to add following commands to get bash:
RUN apk add --no-cache bash
# Adding curl
RUN apk --no-cache add curl
ENV APPROOT="/app" \
    JAEGER_SERVICE_NAME=customer \
    JAEGER_ENDPOINT=http://jaeger-collector.istio-system.svc:14268/api/traces \
    JAEGER_PROPAGATION=b3 \
    JAEGER_SAMPLER_TYPE=const \
    JAEGER_SAMPLER_PARAM=1    
WORKDIR $APPROOT
EXPOSE 8080 8778 9779
COPY target/customer-1.0.jar $APPROOT
ENTRYPOINT ["java"]
CMD ["-jar", "-Xms256m", "-Xmx256m", "customer-1.0.jar"]