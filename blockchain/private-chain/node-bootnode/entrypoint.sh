#!/bin/sh
set -e

NODE_ACCOUNT=$1
NETWORK_ID=$2
BOOTNODE=$3
NETWORK=$4

geth --unlock ${NODE_ACCOUNT} \
     --password /tmp/pwd.txt \
     --keystore /tmp/keystore \
     --nodekeyhex="c26d705bd5933bc2be72813f1b74f140aa6b3726d2a71a43b163a5c084e9dcb4" \
     --nodiscover \
     --ipcdisable \
     --networkid=${NETWORK_ID} \
     --netrestrict=${NETWORK} \
     --datadir /tmp