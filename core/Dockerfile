FROM --REPO--/java-base:--TAG-ENV--

# add the rpm
RUN mkdir /rpms

ADD --RPM_SRC-- /rpms/

# install the rpm
RUN yum -y install /rpms/--RPM_NAME--

# set the boxes environment
ENV ENV_CONF --MACHINE-ENV--

# set the service to run
ENV SERVICE_RUN /data/bin/service
