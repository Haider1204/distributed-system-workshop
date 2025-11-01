FROM openjdk:17

WORKDIR /usrapp/bin

ENV PORT=8081
ENV NODE_NAME=backend-default
ENV REGISTRY_URL=http://localhost:8080
ENV SERVICE_HOST=localhost

COPY /target/classes /usrapp/bin/classes
COPY /target/dependency /usrapp/bin/dependency

CMD ["sh", "-c", "java -DPORT=${PORT} -DNODE_NAME=${NODE_NAME} -DREGISTRY_URL=${REGISTRY_URL} -DSERVICE_HOST=${SERVICE_HOST} -cp ./classes:./dependency/* co.edu.escuelaing.distributed.backend.BackendApplication"]