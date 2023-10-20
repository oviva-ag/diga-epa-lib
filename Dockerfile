FROM eclipse-temurin:17.0.8_7-jre-alpine

# The STOPSIGNAL instruction sets the system call signal that will be sent to the container to exit
# SIGTERM = 15 - https://de.wikipedia.org/wiki/Signal_(Unix)
STOPSIGNAL SIGTERM

# Define the exposed port or range of ports for the service
EXPOSE 9016

# Defining Healthcheck
HEALTHCHECK --interval=15s \
            --timeout=5s \
            --start-period=15s \
            --retries=3 \
            CMD ["/usr/bin/wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:9016/actuator/health"]


# Default USERID and GROUPID
ARG USERID=10000
ARG GROUPID=10000

# Run as User (not root)
USER $USERID:$GROUPID

WORKDIR /app

COPY --chown=$USERID:$GROUPID epa-ps-sim-app/target/epa-ps-sim-app.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]

# Git Args
ARG COMMIT_HASH
ARG VERSION

###########################
# Labels
###########################
LABEL de.gematik.vendor="gematik GmbH" \
      maintainer="software-development@gematik.de" \
      de.gematik.app="epa-ps-sim" \
      de.gematik.git-repo-name="https://gitlab.prod.ccs.gematik.solutions/git/Testtools/ePA/epa-ps-sim" \
      de.gematik.commit-sha=$COMMIT_HASH \
      de.gematik.version=$VERSION
