FROM openjdk:8-jre-slim

ENV APP_HOME=/opt/waves
WORKDIR $APP_HOME

ADD waves.conf .
RUN apt-get update && apt-get install -y curl jq wget \
    && JAR_REMOTE_PATH=$(curl "https://api.github.com/repos/wavesplatform/Waves/releases" \
        | jq -r '[.[] | select(.prerelease == true)] | first.assets[] | select(.name|endswith(".jar")) | .browser_download_url') \
    && echo $JAR_REMOTE_PATH \
    && wget $JAR_REMOTE_PATH -O $APP_HOME/waves.jar -q --show-progress
EXPOSE 6860 6869
ENTRYPOINT ["java", "-jar", "/opt/waves/waves.jar", "/opt/waves/waves.conf"]

# provide volume for local.conf