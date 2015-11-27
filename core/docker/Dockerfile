FROM dom-docker.cloud.dev.phx3.gdg:5000/java-base


RUN curl -O -s http://dom-jenkins.cloud.dev.phx3.gdg/job/Cassandra.Loader/lastSuccessfulBuild/artifact/target/cassandra-loader-exe.jar && \
    mkdir -p /data/tools && \
    mv cassandra-loader-exe.jar /data/tools/cassandra-loader.jar

ADD data /data

# set the service to run
ENTRYPOINT ["/data/bin/service"]