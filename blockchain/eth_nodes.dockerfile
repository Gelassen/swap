
# ARG init_nodes

# RUN [ "${init_nodes}" == "True" ||  "${init_nodes}" == "False" ] 
 
# RUN test -n "$init_nodes" || (echo "init_nodes is not set" && false)

# Setup ethereum chain and start nodes
FROM ubuntu:22.04

LABEL description="Container to setup and startup a private ethereum chain"

RUN echo "Step 1"

#/usr/src/etherium-private-chain
WORKDIR /opt/eth 

RUN echo "Step 2"

COPY ./blockchain/package.json /opt/eth

COPY ./blockchain/start_nodes.sh /opt/eth

RUN pwd

COPY ./infrastructure/chain /opt/eth

RUN echo "Step 3"

# Install necessary software, sudo might not be required
RUN apt-get update && \
    add-apt-repository -y ppa:ethereum/ethereum && \
    apt-get update && \
    apt-get install ethereum && \
    npm install

RUN mkdir node1 node2 node3 

# copy pre-configured accounts and initialize all nodes with chain config
# RUN if [ "$init_nodes" = "true" ]; then \
#         echo "init nodes" \
#         cp /opt/eth/accounts/keystore/UTC--2023-04-21T13-54-40.845472438Z--367103555b34eb9a46d92833e7293d540bfd7143 /opt/eth/node1/keystore && \
#         cp /opt/eth/accounts/keystore/UTC--2023-04-21T13-54-53.297432467Z--1a75262751ac4e6290ec8287d1de823f33036498 /opt/eth/node2/keystore && \
#         cp /opt/eth/accounts/keystore/UTC--2023-04-21T13-55-17.400128588Z--d9df253d1f92dcba495c8b0a9616e9d817bb12b7 /opt/eth/node3/keystore && \
#         geth --datadir ./node1 init swap.ethash.genesis.json && \
#         geth --datadir ./node2 init swap.ethash.genesis.json && \
#         geth --datadir ./node3 init swap.ethash.genesis.json; \
#     else \
#         echo "run nodes" \
#         start_nodes.sh
#     fi
RUN cp /opt/eth/accounts/keystore/UTC--2023-04-21T13-54-40.845472438Z--367103555b34eb9a46d92833e7293d540bfd7143 /opt/eth/node1/keystore && \
    cp /opt/eth/accounts/keystore/UTC--2023-04-21T13-54-53.297432467Z--1a75262751ac4e6290ec8287d1de823f33036498 /opt/eth/node2/keystore && \
    cp /opt/eth/accounts/keystore/UTC--2023-04-21T13-55-17.400128588Z--d9df253d1f92dcba495c8b0a9616e9d817bb12b7 /opt/eth/node3/keystore && \
    geth --datadir ./node1 init swap.ethash.genesis.json && \
    geth --datadir ./node2 init swap.ethash.genesis.json && \
    geth --datadir ./node3 init swap.ethash.genesis.json && \  
    start_nodes.sh   

# ENV export NODE1_PORT=2001
# ENV export NODE1_PORT_UDP_TCP=30304
# ENV export NODE1_PORT_RPC=8552

# EXPOSE 2001

# ENV export NODE2_PORT=2002
# ENV export NODE2_PORT_UDP_TCP=30305
# ENV export NODE2_PORT_RPC=8553

# ENV export NODE3_PORT=2003
# ENV export NODE3_PORT_UDP_TCP=30306
# ENV export NODE3_PORT_RPC=8554