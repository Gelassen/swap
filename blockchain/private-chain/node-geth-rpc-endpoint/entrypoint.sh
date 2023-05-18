#!/bin/sh
set -e

NODE_ACCOUNT=$1
NETWORK_ID=$2
BOOTNODE=$3
NETWORK=$4

geth --unlock ${NODE_ACCOUNT} \
     --password /tmp/pwd.txt \
     --keystore /tmp/keystore \
     --bootnodes=${BOOTNODE} \
     --allow-insecure-unlock \
     --http \
     --http.addr="0.0.0.0" \
     --http.api="eth,web3,net,admin,personal" \
     --http.corsdomain="*" \
     --networkid=${NETWORK_ID} \
     --datadir /tmp \
     --netrestrict=${NETWORK} \
     --verbosity=3