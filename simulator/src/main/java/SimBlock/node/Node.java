/**
 * Copyright 2019 Distributed Systems Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package SimBlock.node;

import static SimBlock.settings.SimulationConfiguration.*;
import static SimBlock.simulator.Main.*;
import static SimBlock.simulator.Network.*;
import static SimBlock.simulator.Simulator.*;
import static SimBlock.simulator.Timer.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import SimBlock.localiser.TaskExecutor;
import SimBlock.localiser.Localiser;
import SimBlock.node.routingTable.AbstractRoutingTable;
import SimBlock.task.AbstractMessageTask;
import SimBlock.task.BlockMessageTask;
import SimBlock.task.ChangeNeighborTask;
import SimBlock.task.DegreeMessageTask;
import SimBlock.task.InvMessageTask;
import SimBlock.task.LocaliserTask;
import SimBlock.task.MiningTask;
import SimBlock.task.RecDegreeMessageTask;
import SimBlock.task.RecMessageTask;
import SimBlock.task.Task;
public class Node {
	private int region;
	private int nodeID;
	private long miningRate;
	private AbstractRoutingTable routingTable;

	private Block block;
	private Set<Block> orphans = new HashSet<Block>();

	private Task executingTask = null;

	private boolean sendingBlock = false;
	private ArrayList<RecMessageTask> messageQue = new ArrayList<RecMessageTask>();
	private Set<Block> downloadingBlocks = new HashSet<Block>();

	private Localiser localiser = null;

	private long processingTime = 2;

	public Node(int nodeID,int nConnection ,int region, long power, String routingTableName){
		this.nodeID = nodeID;
		this.region = region;
		this.miningRate = power;
		try {
			this.routingTable = (AbstractRoutingTable) Class.forName(routingTableName).getConstructor(Node.class).newInstance(this);
			this.setnConnection(nConnection);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getNodeID(){ return this.nodeID; }
	public Block getBlock(){ return this.block; }
	public long getPower(){ return this.miningRate; }
	public Set<Block> getOrphans(){ return this.orphans; }
	public void setRegion(int region){ this.region = region; }
	public int getRegion(){ return this.region; }

	public boolean addNeighbor(Node node, boolean isInit){ return this.routingTable.addNeighbor(node, isInit); }
	public boolean removeNeighbor(Node node){ return this.routingTable.removeNeighbor(node); }
	public ArrayList<Node> getNeighbors(){ return this.routingTable.getNeighbors(); }
	public ArrayList<Node> getOutBoundNodes(){ return this.routingTable.getOutboundNodes(); }
	public ArrayList<Node> getInBoundNodes(){ return this.routingTable.getInboundNodes(); }
	public AbstractRoutingTable getRoutingTable(){ return this.routingTable; }
	public void setnConnection(int nConnection){ this.routingTable.setnConnection(nConnection); }
	public int getnConnection(){ return this.routingTable.getnConnection(); }


	public void joinNetwork(){
		this.routingTable.initTable();
	}

	public void startLocaliser() {
		LocaliserTask task = new LocaliserTask(this);
		TaskExecutor.putTask(task);
	}

	public void genesisBlock(){
		Block genesis = new Block(1, null, this, 0);
		this.receiveBlock(genesis);
	}

	public void addToChain(Block newBlock) {
		if(this.executingTask != null){
			removeTask(this.executingTask);
			this.executingTask = null;
		}
		this.block = newBlock;
		printAddBlock(newBlock);
		arriveBlock(newBlock, this);
	}

	private void printAddBlock(Block newBlock){
		OUT_JSON_FILE.print("{");
		OUT_JSON_FILE.print(	"\"kind\":\"add-block\",");
		OUT_JSON_FILE.print(	"\"content\":{");
		OUT_JSON_FILE.print(		"\"timestamp\":" + getCurrentTime() + ",");
		OUT_JSON_FILE.print(		"\"node-id\":" + this.getNodeID() + ",");
		OUT_JSON_FILE.print(		"\"block-id\":" + newBlock.getId());
		OUT_JSON_FILE.print(	"}");
		OUT_JSON_FILE.print("},");
		OUT_JSON_FILE.flush();
	}

	public void addOrphans(Block newBlock, Block correctBlock){
		if(newBlock != correctBlock){
			this.orphans.add(newBlock);
			this.orphans.remove(correctBlock);
			if(newBlock.getParent() != null && correctBlock.getParent() != null){
				this.addOrphans(newBlock.getParent(),correctBlock.getParent());
			}
		}
	}

	public void mining(){
		Task task = new MiningTask(this);
		this.executingTask = task;
		putTask(task);
	}

	public void sendInv(Block block){
		for(Node to : this.routingTable.getNeighbors()){
			AbstractMessageTask task = new InvMessageTask(this,to,block);
			putTask(task);
		}
	}

	public void receiveBlock(Block receivedBlock){
		Block sameHeightBlock;

		if(this.block == null){
			this.addToChain(receivedBlock);
			this.mining();
			this.sendInv(receivedBlock);

		}else if(receivedBlock.getHeight() > this.block.getHeight()){
			sameHeightBlock = receivedBlock.getBlockWithHeight(this.block.getHeight());
			if(sameHeightBlock != this.block){
				this.addOrphans(this.block, sameHeightBlock);
			}
			this.addToChain(receivedBlock);
			this.mining();
			this.sendInv(receivedBlock);

		}else if(receivedBlock.getHeight() <= this.block.getHeight()){
			sameHeightBlock = this.block.getBlockWithHeight(receivedBlock.getHeight());
			if(!this.orphans.contains(receivedBlock) && receivedBlock != sameHeightBlock){
				this.addOrphans(receivedBlock, sameHeightBlock);
				arriveBlock(receivedBlock, this);
			}
		}

	}

	public void receiveMessage(AbstractMessageTask message){
		Node from = message.getFrom();
		if(message instanceof InvMessageTask){
			Block block = ((InvMessageTask) message).getBlock();
			if(!this.orphans.contains(block) && !this.downloadingBlocks.contains(block)){
				if(this.block == null || block.getHeight() > this.block.getHeight()){
					AbstractMessageTask task = new RecMessageTask(this,from,block);
					putTask(task);
					downloadingBlocks.add(block);
				}else{

					// get orphan block
					if(block != this.block.getBlockWithHeight(block.getHeight())){
						AbstractMessageTask task = new RecMessageTask(this,from,block);
						putTask(task);
						downloadingBlocks.add(block);
					}
				}
			}
		}

		if(message instanceof RecMessageTask){
			this.messageQue.add((RecMessageTask) message);
			if(!sendingBlock){
				this.sendNextBlockMessage();
			}
		}

		if(message instanceof BlockMessageTask){
			Block block = ((BlockMessageTask) message).getBlock();
			downloadingBlocks.remove(block);
			this.receiveBlock(block);
		}

		// j, kが受け取るメッセージ
		if(message instanceof RecDegreeMessageTask) {
			int neighborsSize = this.routingTable.getNeighbors().size();
			DegreeMessageTask task = new DegreeMessageTask(this, message.getFrom(), neighborsSize);
			if(((RecDegreeMessageTask) message).estimateEnabled()) {
				RecDegreeMessageTask t = (RecDegreeMessageTask) message;
				task.setEstimateCost(Localiser.getCost(this.getRegion(), t.getKRegion()));
			}
			TaskExecutor.putTask(task);
		}

		if(message instanceof ChangeNeighborTask) {
			ChangeNeighborTask task = (ChangeNeighborTask) message;
			// addNeighborkかremoveNeighborで条件分岐
			if(addNeighbor(task.getDestination(), false)){
				//System.out.println("i: " + task.getFrom().getNodeID());
				//System.out.println("j: " + this.getNodeID());
				//System.out.println("k: " + task.getDestination().getNodeID());
				//System.out.println("c(i,j): " + getLatency(task.getFrom().region, this.getRegion()));
				//System.out.println("c(j,k): " + getLatency(this.region, task.getDestination().getRegion()));
				//System.out.println();
				task.getFrom().removeNeighbor(this);	
			}			
		}

		// j, kからの返信
		if(message instanceof DegreeMessageTask) {
			DegreeMessageTask degreeMessage = (DegreeMessageTask) message;
			if(degreeMessage.getEstimateCost() == null) {
				localiser.setdk(degreeMessage.getDegree());
			} else {
				localiser.setdj(degreeMessage.getDegree(), degreeMessage.getEstimateCost());
			}
			if(localiser.calculatable()) {
				Node j = localiser.getJ();
				Node k = localiser.getK();
				changeNeighbor(j,k);
			}
		}
	}

	private void changeNeighbor(Node to ,Node destination) {
		double random = Math.random();
		if(random >= localiser.calcP()) {
			localiser = null;
			return;
		}
		localiser = null;
		ChangeNeighborTask task = new ChangeNeighborTask(this, to, destination);
		TaskExecutor.putTask(task);
		
	}

	public void sendDegreeRequest(Node j, Node k) {
		long degree = this.routingTable.getNeighbors().size();
		localiser = new Localiser(degree, j, k);
		localiser.setcij(Localiser.getCost(this.region, j.region));
		RecDegreeMessageTask taskJ = new RecDegreeMessageTask(this, j);
		taskJ.setEstimatable(k.region);
		RecDegreeMessageTask taskK = new RecDegreeMessageTask(this, k);
		TaskExecutor.putTask(taskJ);
		TaskExecutor.putTask(taskK);
	}

	// send a block to the sender of the next queued recMessage
	public void sendNextBlockMessage(){
		if(this.messageQue.size() > 0){

			sendingBlock = true;

			Node to = this.messageQue.get(0).getFrom();
			Block block = this.messageQue.get(0).getBlock();
			this.messageQue.remove(0);
			long blockSize = BLOCKSIZE;
			long bandwidth = getBandwidth(this.getRegion(),to.getRegion());
			long delay = blockSize * 8 / (bandwidth/1000) + processingTime;
			BlockMessageTask messageTask = new BlockMessageTask(this, to, block, delay);

			putTask(messageTask);
		}else{
			sendingBlock = false;
		}
	}

}
