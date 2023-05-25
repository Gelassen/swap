# !/bin/bash

export pwd="<your password>"

export NODE1_PORT=2001
export NODE1_PORT_UDP_TCP=30304
export NODE1_PORT_RPC=8552

export NODE2_PORT=2002
export NODE2_PORT_UDP_TCP=30305
export NODE2_PORT_RPC=8553

export NODE3_PORT=2003
export NODE3_PORT_UDP_TCP=30306
export NODE3_PORT_RPC=8554

export NODE_IP=127.0.0.1

echo "Run existing geth nodes. Please make sure they has been create & configured first!"

if ! command -v geth &> /dev/null
then 
    echo "geth command could not be found"
    exit
else 
    echo "geth has been found. continue shell script"
fi

# nodes should be run over http to allow curl interactiion for add peer automation

geth --allow-insecure-unlock --http --http.addr "0.0.0.0" --port $NODE1_PORT_UDP_TCP --http.corsdomain '*' --authrpc.port $NODE1_PORT_RPC --http.port $NODE1_PORT --http.api admin,personal,eth,net,web3  --datadir ./node1/data --miner.gasprice 1 --miner.gaslimit '0x1C9C380' --verbosity 3 &

geth --http --port $NODE2_PORT_UDP_TCP --http.corsdomain '*' --authrpc.port $NODE2_PORT_RPC --http.port $NODE2_PORT --http.api admin,personal,eth,net,web3  --datadir ./node2/data --miner.gasprice 1 --miner.gaslimit '0x1C9C380' --verbosity 3 &

geth --http --port $NODE3_PORT_UDP_TCP --http.corsdomain '*' --authrpc.port $NODE3_PORT_RPC --http.port $NODE3_PORT --http.api admin,personal,eth,net,web3  --datadir ./node3/data --miner.gasprice 1 --miner.gaslimit '0x1C9C380' --verbosity 3 &

echo "Install jq"

# sudo apt-get install jq -y $p

sudo apt-get install jq -y "${pwd}"

echo "Get enode info and add peers to each node"

node1_enode_result=$(curl -X GET http://$NODE_IP:$NODE1_PORT -H "Content-Type: application/json" --data '{"jsonrpc":"2.0", "id": 1,  "method":"admin_nodeInfo"}' | jq -r '.result.enode')

IFS="@" read -r node1_enode_id node1_end_point <<< "$node1_enode_result"

echo "$node1_enode_id"

node2_enode_result=$(curl -X GET http://$NODE_IP:$NODE2_PORT -H "Content-Type: application/json" --data '{"jsonrpc":"2.0", "id": 1,  "method":"admin_nodeInfo"}' | jq -r '.result.enode')

IFS="@" read -r node2_enode_id node2_end_point <<< "$node2_enode_result"

echo "$node2_enode_id"

node3_enode_result=$(curl -X GET http://$NODE_IP:$NODE3_PORT -H "Content-Type: application/json" --data '{"jsonrpc":"2.0", "id": 1,  "method":"admin_nodeInfo"}' | jq -r '.result.enode')

IFS="@" read -r node3_enode_id node3_end_point <<< "$node3_enode_result"

echo "$node3_enode_id"

node1_endpoint="${node1_enode_id}@${NODE_IP}:${NODE1_PORT}"

echo "${node1_endpoint}"

node2_endpoint="${node2_enode_id}@${NODE_IP}:${NODE2_PORT}"

echo "${node2_endpoint}"

node3_endpoint="${node3_enode_id}@${NODE_IP}:${NODE3_PORT}"

echo "${node3_endpoint}"

# the automation of the peers adding is disabled due an issue with getting response from server and lack of actual peers despite on positive server response when command is executed ver console

# curl -X POST http://$NODE_IP:$NODE2_PORT -H "Content-Type:application/json" --data "{"jsonrpc": "2.0", "method":"admin_addPeer", "id":1, "params":["$node1_endpoint"]}"