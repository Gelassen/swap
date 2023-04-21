# !/bin/bash

export pwd="Sector@1"

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

export NETWORK_ID=50101
export BOOTNODE=enode://7520982eef13d72705933d02021c42250800aa8cb1741345d372a34eb25c58ec75c232f2fceabb0f847675abf008ecad01597e1a6a3f7ff5cde5bc8c0edd153c@127.0.0.1:0?discport=30302
export ACCOUNT_1=0x1892C37627bF84077C195AcAf6a0bA0Bc74765Bb
export ACCOUNT_2=0x148D856D936332F7a979C3Ab4A4c13938E3CeBE8
export ACCOUNT_3=0xd177B912F9c5F07e9E9997d246EF2c9Ed9E991eb

echo "Run existing geth nodes. Please make sure they has been created & configured first!"

if ! command -v geth &> /dev/null
then 
    echo "geth command could not be found"
    exit
else 
    echo "geth has been found. continue shell script"
fi

# geth --datadir ./node1/data --port 2001 &

# geth --datadir ./node2/data --port 2002 &

# geth --datadir ./node3/data --port 2003 &

# nodes should be run over http to allow curl interactiion for add peer automation

# can not connect to http node from android genymotion emulator despite on there is connection between host machine and swap server in VM, possible cause --rpc  flag which  enables rpc connection to http node

# --allow-insecure-unlock is still necessary here because under http access is secure breach - the rest connections is ok

# echo "
# 
# 	Please confirm boot.key has been generated and env variable $BOOTNODE is updated with the relevant enode
# 
# "

# it was originally implemented for Clique consensus mode, but Clique has deadlock at its core; there was an attempt to 
# apply to ethash mode, but I haven't solved issue to automatically unlock accounts for the bootnode - leave for a future or reference

# bootnode -nodekey boot.key -addr :30302

geth --allow-insecure-unlock --http --http.addr "0.0.0.0" --port $NODE1_PORT_UDP_TCP --http.corsdomain '*' --authrpc.port $NODE1_PORT_RPC --http.port $NODE1_PORT --http.api admin,personal,eth,net,web3  --datadir node1 --bootnodes $BOOTNODE --networkid $NETWORK_ID --unlock $ACCOUNT_1 --password node1/pwd.txt --miner.gasprice 1 --miner.gaslimit '0x1C9C380' --verbosity 3 &

geth --allow-insecure-unlock --http --port $NODE2_PORT_UDP_TCP --http.corsdomain '*' --authrpc.port $NODE2_PORT_RPC --http.port $NODE2_PORT --http.api admin,personal,eth,net,web3  --datadir node2 --bootnodes $BOOTNODE --networkid $NETWORK_ID --unlock $ACCOUNT_2 --password node2/pwd.txt --miner.gasprice 1 --miner.gaslimit '0x1C9C380' --verbosity 3 &

geth --allow-insecure-unlock -http --port $NODE3_PORT_UDP_TCP --http.corsdomain '*' --authrpc.port $NODE3_PORT_RPC --http.port $NODE3_PORT --http.api admin,personal,eth,net,web3  --datadir node3 --bootnodes $BOOTNODE --networkid $NETWORK_ID --unlock $ACCOUNT_3 --password node3/pwd.txt --miner.gasprice 1 --miner.gaslimit '0x1C9C380' --verbosity 3 &

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

# echo "Node1 enode ${node1Enode}"

# echo "Our main enode: ${node1Enode.enode}"

# echo "Show all three processes are run in the background"

# jobs

# echo "Get nodes addresses and add them as peers to the main node"

# geth attach ipc:node1/data/geth.ipc


