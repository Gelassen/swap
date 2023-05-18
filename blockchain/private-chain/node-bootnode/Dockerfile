FROM ethereum/client-go:v1.10.1

ARG NODE_ACCOUNT
ARG NODE_ACCOUNT_PWD
ARG NETWORK_ID
ARG BOOTNODE
ARG NETWORK

ENV NODE_ACCOUNT=${NODE_ACCOUNT}
ENV NETWORK_ID=${NETWORK_ID}
ENV BOOTNODE_NODEKEYHEX=${BOOTNODE_NODEKEYHEX}
ENV NETWORK=${NETWORK}

COPY ./swap.ethash.genesis.json /tmp

COPY ./boot.key /tmp

RUN mkdir -p /tmp/keystore

COPY ./keystore/${NODE_ACCOUNT_PWD_FILENAME} /tmp/keystore

RUN geth --datadir /tmp init /tmp/swap.ethash.genesis.json \
    && rm -f ~/.ethereum/geth/nodekey 

RUN echo ${NODE_ACCOUNT_PWD} >> /tmp/pwd.txt

COPY ./entrypoint.sh /tmp

RUN chmod +x ./tmp/entrypoint.sh

# This way entrypoint usage would ignore OS signals sent the container. That might cause some issues
# Is it root cause of issue to unable 'geth attach <ip address>'?
ENTRYPOINT ./tmp/entrypoint.sh ${NODE_ACCOUNT} ${NETWORK_ID} ${BOOTNODE_NODEKEYHEX} ${NETWORK}