# Setup ethereum chain and start nodes
FROM ubuntu:22.04
LABEL description="Container to setup and startup a private ethereum chain"

WORKDIR /usr/src/etherium-private-chain 

# Install necessary software, sudo might not be required
RUN sudo apt-get update
RUN sudo apt-get install npm
RUN sudo add-apt-repository -y ppa:ethereum/ethereum
RUN sudo apt-get update
RUN sudo apt-get install ethereum
RUN npm install --save-dev keythereum 
RUN npm install --save-dev @openzeppelin/test-helpers 
RUN npm install --save-dev hardhat 
RUN npm install --save-dev @openzeppelin/contracts 
RUN npm install --save-dev config

ENV export NODE1_PORT=2001
ENV export NODE1_PORT_UDP_TCP=30304
ENV export NODE1_PORT_RPC=8552

EXPOSE 2001

ENV export NODE2_PORT=2002
ENV export NODE2_PORT_UDP_TCP=30305
ENV export NODE2_PORT_RPC=8553

ENV export NODE3_PORT=2003
ENV export NODE3_PORT_UDP_TCP=30306
ENV export NODE3_PORT_RPC=8554