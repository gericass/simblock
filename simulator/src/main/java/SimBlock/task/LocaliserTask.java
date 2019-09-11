package SimBlock.task;

import SimBlock.node.Node;
import java.util.Random;

public class LocaliserTask implements Task {
	private Node i;

	private long interval;
	
	public LocaliserTask(Node i) {
		this.i = i;
        // TODO インターバル調整
		this.interval = 100l;
	}
	
	@Override
	public long getInterval() {
		return this.interval;
	}

	@Override
	public void run() {
        Random rand = new Random();
        int outBoundIndex = rand.nextInt(i.getOutBoundNodes().size());
        Node j = i.getOutBoundNodes().get(outBoundIndex);
        int neighborIndex = rand.nextInt(i.getNeighbors().size());
        Node k = i.getNeighbors().get(neighborIndex);
        while(j.equals(k)) {
            neighborIndex = rand.nextInt(i.getNeighbors().size());
            k = i.getNeighbors().get(neighborIndex);
        }
		this.i.sendDegreeRequest(j, k);
	}
}
