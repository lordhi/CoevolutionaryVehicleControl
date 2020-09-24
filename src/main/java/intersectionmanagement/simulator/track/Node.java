package intersectionmanagement.simulator.track;

import java.util.List;
import java.util.ArrayList;
import intersectionmanagement.simulator.control.CarController;


// Basic building block of a track, it's a glorified linked list
public class Node {
    public CarController controller;

    private List<Node> nextNodes;
    private float x;
    private float y;
    private boolean active;

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public boolean isActive() {
        return active;
    }

    Node(float x, float y, boolean active) {
        this.x = x;
        this.y = y;
        this.active = active;
        nextNodes = new ArrayList<>();
    }

    Node(float x, float y, boolean active, CarController controller) {
        this.x = x;
        this.y = y;
        this.active = active;
        this.controller = controller;
        nextNodes = new ArrayList<>();
    }

    public void addNextNode(Node node) {
        nextNodes.add(node);
    }

    public List<Node> getNextNodes() {
        return nextNodes;
    }

    public void removeNextNode(Node node) {
        nextNodes.remove(node);
    }
}