package SimBlock.task;

import SimBlock.node.Node;

public class ChangeNeighborTask extends AbstractMessageTask {

    Node destination;

	public ChangeNeighborTask(Node from, Node to, Node destination) {
        super(from, to);
        this.destination = destination;
    }

    public Node getDestination() {
        return destination;
    }
}