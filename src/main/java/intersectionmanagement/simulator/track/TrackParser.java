package intersectionmanagement.simulator.track;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import intersectionmanagement.simulator.Utility;
import intersectionmanagement.simulator.control.CarController;

import org.json.JSONObject;
import org.json.JSONArray;

public class TrackParser {
    public static Node calculateBezierCurve(int degree, int precision,
        float[] x, float[] y, boolean active,
        CarController controller) {

        // Precision should specify number of points, since point has to be added at the end, use -1
        float increment = 1.0f/(precision-1);

        Node root = new Node(x[0], y[0], active, controller);
        Node currentNode = root;

        for (float t = increment; t < 1; t += increment) {
            Node nextNode = calculateNode(degree, t, x, y, active, controller);
            currentNode.addNextNode(nextNode);
            currentNode = nextNode;
        }

        currentNode.addNextNode(new Node(x[degree-1], y[degree-1], active));
        return root;
    }

    private static Node calculateNode(int degree, float t,
        float[] x, float[] y, boolean active,
        CarController controller) {
        float nextNodeX;
        float nextNodeY;

        switch (degree) {
            case 2:
                nextNodeX = (1-t)*x[0]+t*x[1];
                nextNodeY = (1-t)*y[0]+t*y[1];
                break;

            case 3:
                nextNodeX = (float) (Math.pow(1-t, 2)*x[0] + 2*(1-t)*t*x[1] + Math.pow(t, 2)*x[2]);
                nextNodeY = (float) (Math.pow(1-t, 2)*y[0] + 2*(1-t)*t*y[1] + Math.pow(t, 2)*y[2]);
                break;

            case 4:
                nextNodeX = (float) (
                                Math.pow(1-t, 3)*x[0] +
                                3*Math.pow(1-t, 2)*t*x[1] +
                                3*(1-t)*Math.pow(t, 2)*x[2] +
                                Math.pow(t, 3)*x[3]);
                nextNodeY = (float) (
                        Math.pow(1-t, 3)*y[0] +
                                3*Math.pow(1-t, 2)*t*y[1] +
                                3*(1-t)*Math.pow(t, 2)*y[2] +
                                Math.pow(t, 3)*y[3]);
                return new Node(nextNodeX, nextNodeY, active);

            default:
                throw new InvalidBezierCurveDegreeException(degree);
        }

        return new Node(nextNodeX, nextNodeY, active, controller);
    }

    // Finds curves that have a start point equal to the end point of another curve and links them together
    public static List<Node> linkCurves(List<Node> roots) {
        // If a root node is linked backwards, then it is no longer a root node, store these here
        List<Node> linkedNodes = new LinkedList<>();
        for (Node node : roots) {
            linkedNodes.addAll(recursiveLinkEnd(roots, node));
        }

        roots.removeAll(linkedNodes);
        return roots;
    }

    /*
    Recursively finds penultimate nodes and checks if their next nodes (which will be end nodes) are the same as any
    root nodes
    If so, then the penultimate node is linked to the root of the other curve and that curve's root node will be
    removed as a root node
     */
    private static List<Node> recursiveLinkEnd(List<Node> roots, Node node) {
        LinkedList<Node> removedEndNodes = new LinkedList<>();
        LinkedList<Node> addedEndNodes = new LinkedList<>();
        LinkedList<Node> removedRoots = new LinkedList<>();

        for (Node nextNode : node.getNextNodes()) {
            if (nextNode.getNextNodes().size() == 0) {
                for (Node rootNode : roots) {
                    if (rootNode.getX() == nextNode.getX() && rootNode.getY() == nextNode.getY()) {
                        addedEndNodes.add(rootNode);
                        removedEndNodes.add(nextNode);
                        removedRoots.add(rootNode);
                    }
                }
            } else {
                removedRoots.addAll(recursiveLinkEnd(roots, nextNode));
            }
        }
        for (Node removedEndNode : removedEndNodes) {
            node.removeNextNode(removedEndNode);
        }
        for (Node addedEndNode : addedEndNodes) {
            node.addNextNode(addedEndNode);
        }
        return removedRoots;
    }

}
