FROM mysql:8.0.33 

COPY ./entrypoint.sh /tmp

RUN chmod +x ./tmp/entrypoint.sh

ENTRYPOINT ./entrypoint.sh