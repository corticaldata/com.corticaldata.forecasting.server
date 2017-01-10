FROM pacohernandezg/vertx

ADD misc /usr/app/misc/
ADD bin /usr/app/bin/
ADD lib /usr/app/lib/
ADD web /usr/app/web/

COPY config-docker.json /usr/app/config.json

WORKDIR /usr/app

EXPOSE 9999

ENV ENABLE_VERTX_SYNC_AGENT true

CMD vertx run -cp bin:lib/graphql-java-2.2.0.jar:lib/htm.java-0.6.11-jmh.jar -conf config.json main.Main
