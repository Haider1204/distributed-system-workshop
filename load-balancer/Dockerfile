FROM openjdk:17

WORKDIR /usrapp/bin

ENV PORT=8090
ENV REGISTRY_URL=http://localhost:8080

COPY /target/classes /usrapp/bin/classes
COPY /target/dependency /usrapp/bin/dependency

CMD ["sh", "-c", "java -DPORT=${PORT} -DREGISTRY_URL=${REGISTRY_URL} -cp ./classes:./dependency/* co.edu.escuelaing.distributed.loadbalancer.LoadBalancerApplication"]