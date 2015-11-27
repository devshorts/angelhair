FROM --REPO--/java-base:--TAG-ENV--

RUN curl -O -s http://dom-jenkins.cloud.dev.phx3.gdg/job/Cassandra.Loader/lastSuccessfulBuild/artifact/target/cassandra-loader-exe.jar && \
    mkdir -p /data/tools && \
    mv cassandra-loader-exe.jar /data/tools/cassandra-loader.jar

# set the service to run
ENV SERVICE_RUN /data/bin/service
