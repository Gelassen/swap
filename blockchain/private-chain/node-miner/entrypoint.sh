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
     --mine \
     --miner.threads=1 \
     --miner.etherbase=${NODE_ACCOUNT} \
     --miner.gasprice 1 \
     --verbosity=5 \
     --networkid=${NETWORK_ID} \
     --datadir \tmp \
     --netrestrict=${NETWORK}