package processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import parser.LogicTreeNode;
import tree.Node;
import tree.Tree;

public class LogicTreeProcessor {

	public LogicTreeProcessor() {

	}

	/**
	 * Checks that children of a node have weights that sum to 1.
	 */
	public Boolean hasValidWeights(Tree<LogicTreeNode> tree) {
		Boolean isValid = true;
		List<Node<LogicTreeNode>> nodeList = tree.toList();
		for (Node<LogicTreeNode> node : nodeList) {
			if (!node.getChildren().isEmpty()) {
				double weigth = 0.0;
				for (Node<LogicTreeNode> child : node.getChildren()) {
					weigth = weigth + child.data.getUncertaintyWeight();
				}
				if (weigth != 1.0) {
					isValid = false;
					break;
				}
			}
		}
		return isValid;
	}

	/**
	 * Computes all logic tree paths from the root node. Algorithm taken and
	 * adapted from http://cslibrary.stanford.edu/110/BinaryTrees.html#java
	 * (printPaths() solution)
	 */
	public List<LogicTreePath> computeAllLogicTreePaths(Tree<LogicTreeNode> tree) {
		List<LogicTreePath> logicTreePaths = new ArrayList<LogicTreePath>();
		List<LogicTreeNode> path = new ArrayList<LogicTreeNode>();
		int pathLen = 0;
		computeAllLogicTreePaths(tree.getRootElement(), path, pathLen,
				logicTreePaths);
		return logicTreePaths;
	}

	/**
	 * Compute all logic tree paths in a recursive way.
	 */
	private void computeAllLogicTreePaths(Node<LogicTreeNode> node,
			List<LogicTreeNode> path, int pathLen,
			List<LogicTreePath> logicTreePaths) {
		if (node == null)
			return;

		if (path.size() <= pathLen) {
			path.add(pathLen, node.data);
		} else {
			path.set(pathLen, node.data);
		}
		pathLen = pathLen + 1;

		if (node.getChildren().isEmpty()) {
			// create LogicTreePath and add to logicTreePaths
			List<LogicTreeNode> nodeList = new ArrayList<LogicTreeNode>();
			double pathWeight = 1;
			for (int i = 0; i < pathLen; i++) {
				nodeList.add(path.get(i));
				pathWeight = pathWeight * path.get(i).getUncertaintyWeight();
			}
			LogicTreePath p = new LogicTreePath(nodeList, pathWeight);
			logicTreePaths.add(p);
		} else {
			for (Node<LogicTreeNode> n : node.getChildren()) {
				computeAllLogicTreePaths(n, path, pathLen, logicTreePaths);
			}
		}

	}

	/**
	 * Randomly sample n logic tree paths.
	 */
	public List<LogicTreePath> sampleLogicTreePaths(
			Tree<LogicTreeNode> logicTree, Random rn, int n) {

		List<LogicTreePath> logicTreePaths = new ArrayList<LogicTreePath>();

		for (int i = 0; i < n; i++) {
			List<LogicTreeNode> nodeList = new ArrayList<LogicTreeNode>();
			// add root node
			nodeList.add(0, logicTree.getRootElement().data);
			double pathWeight = 1;
			LogicTreePath treePath = new LogicTreePath(nodeList, pathWeight);
			sampleLogicTreePath(logicTree.getRootElement(), rn, treePath);
			logicTreePaths.add(treePath);
		}
		return logicTreePaths;
	}

	/**
	 * Sample logic tree path recursively.
	 */
	private void sampleLogicTreePath(Node<LogicTreeNode> node, Random rn,
			LogicTreePath path) {

		List<Node<LogicTreeNode>> children = node.getChildren();

		if (children.isEmpty()) {
			return;
		} else {
			// randomly select a child
			Node<LogicTreeNode> sampledNode = sampleChild(rn, children);
			path.addNode(sampledNode.getData());
			sampleLogicTreePath(sampledNode, rn, path);
		}
	}

	/**
	 * Sample child from a list of children. The sampling is done using the
	 * inverse transform method (See for instance:
	 * "Computational Statistics Handbook with Matlab", Wendy L. Martinez, Angel
	 * R. Martinez, CHAPMAN&ALL 2002).
	 */
	private Node<LogicTreeNode> sampleChild(Random rn,
			List<Node<LogicTreeNode>> children) {
		// define probability mass function
		double[] prob = new double[children.size()];
		Node<LogicTreeNode>[] x = new Node[children.size()];
		for (int i = 0; i < children.size(); i++) {
			x[i] = children.get(i);
			prob[i] = children.get(i).getData().getUncertaintyWeight();
		}
		// do the random samplings
		Node<LogicTreeNode> sampledNode = null;
		double u = rn.nextDouble();
		double p = 0;
		for (int j = 0; j < x.length; j++) {
			p = p + prob[j];
			if (u <= p) {
				sampledNode = x[j];
				break;
			}
		}
		return sampledNode;
	}

	/**
	 * Join logic trees.
	 */
	public Tree<LogicTreeNode> joinTrees(List<Tree<LogicTreeNode>> trees) {
		Tree<LogicTreeNode> tree = new Tree<LogicTreeNode>();
		// set as root node, the root of the first logic tree
		tree.setRootElement(trees.get(0).getRootElement());
		// loop over the remaining logic trees, and add each tree to the leaf
		// nodes
		for (int i = 1; i < trees.size(); i++) {
			List<Node<LogicTreeNode>> leafNodes = tree.getLeafNodes();
			for (Node<LogicTreeNode> node : leafNodes) {
				node.addChild(trees.get(i).getRootElement());
			}
		}
		return tree;
	}

}