FROM openjdk:8-jre-alpine

ENV APP_HOME=/opt/waves
WORKDIR $APP_HOME

ADD waves.conf .
RUN apk update && apk add curl jq wget \
    && JAR_REMOTE_PATH=$(curl "https://api.github.com/repos/wavesplatform/Waves/releases" \
        | jq -r '[.[] | select(.prerelease == true)] | first.assets[] | select(.name|endswith(".jar")) | .browser_download_url') \
    && echo "Downloading '$JAR_REMOTE_PATH'. It may take a few minutes..."\
    && wget $JAR_REMOTE_PATH -O $APP_HOME/waves.jar -q
EXPOSE 6860 6869
ENTRYPOINT ["java", "-jar", "waves.jar", "waves.conf"]

# provide volume for local.conf