# FROM hseeberger/scala-sbt:8u181_2.12.8_1.2.8 as builder

# MAINTAINER Maxim Smolyakov <smolyakovmaxim@gmail.com> (@msmolyakov)

# RUN cd /opt \
# && curl -fsLO https://github.com/wavesplatform/Waves/archive/master.zip \
# && unzip -aoq master.zip

# VOLUME /root/.ivy2

# RUN cd /opt/Waves-master \
# && SBT_OPTS="-XX:MaxJavaStackTraceDepth=5000 -Xmx2536M -XX:+CMSClassUnloadingEnabled -Xss2M" \
# && sbt node/assembly \
# && cp $(ls "/opt/Waves-master/node/target/waves-all*.jar") /opt/Waves-master/waves.jar

# COPY --from=builder /opt/Waves-master/node/target/waves*.jar ./waves.jar

FROM openjdk:10-jre-alpine
ENV APP_HOME=/opt/waves
WORKDIR $APP_HOME
VOLUME $APP_HOME
ADD waves.conf .
# TODO remove ADD jar
ADD waves.jar .
# TODO remove wget
# TODO RUN apk add curl jq wget \
#     && JAR_REMOTE_PATH=$(curl "https://api.github.com/repos/wavesplatform/Waves/releases" \
#         | jq -r '[.[] | select(.prerelease == true)] | first.assets[] | select(.name|endswith(".jar")) | .browser_download_url') \
#     && echo $JAR_REMOTE_PATH \
#     && curl $JAR_REMOTE_PATH --create-dirs -o $APP_HOME/waves.jar \
# TODO    && wget $JAR_REMOTE_PATH -O $APP_HOME/waves.jar -q --show-progress
# TODO    && chmod +x $APP_HOME/waves.jar
#    && sed -i 's/directory = .+/directory = /g' waves.conf
ENTRYPOINT ["java", "-jar", "/opt/waves/waves.jar", "/opt/waves/waves.conf"]

# ENV APP_HOME /opt/waves
# ENV JAR_REMOTE_PATH ${curl https://api.github.com/repos/wavesplatform/Waves/releases
#     | jq '[.[] | select(.prerelease == true)] | first.assets[] | select(.name|endswith(".jar")) | .browser_download_url'}
# RUN mkdir -p $APP_HOME \
# && curl -o $APP_HOME/waves.jar $JAR_REMOTE_PATH
# provide volume for local.conf
# EXPOSE ports

# env.version: [mainnet, testnet, master, $commit, version]