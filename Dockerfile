FROM clojure:openjdk-11-tools-deps-slim-buster

ENV AERO_WORLD_HOME=/opt/aero-world

RUN mkdir $AERO_WORLD_HOME

WORKDIR $AERO_WORLD_HOME

COPY . .

RUN chmod +x ./build/build.sh

RUN chmod +x ./build/aw.sh

RUN ./build/build.sh

EXPOSE 3000

CMD ["./build/aw.sh"]