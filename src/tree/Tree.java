package tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a Tree of Objects of generic type T. The Tree is represented as a
 * single rootElement which points to a List<Node<T>> of children. There is no
 * restriction on the number of children that a particular node may have. This
 * Tree provides a method to serialize the Tree into a List by doing a pre-order
 * traversal. It has several methods to allow easy updation of Nodes in the
 * Tree. Code taken from:
 * http://sujitpal.blogspot.com/2006/05/java-data-structure-generic-tree.html
 */
public class Tree<T> {

	private Node<T> rootElement;

	/**
	 * Default ctor.
	 */
	public Tree() {
		super();
	}

	/**
	 * Return the root Node of the tree.
	 * 
	 * @return the root element.
	 */
	public Node<T> getRootElement() {
		return this.rootElement;
	}

	/**
	 * Set the root Element for the tree.
	 * 
	 * @param rootElement
	 *            the root element to set.
	 */
	public void setRootElement(Node<T> rootElement) {
		this.rootElement = rootElement;
	}

	/**
	 * Returns the Tree<T> as a List of Node<T> objects. The elements of the
	 * List are generated from a pre-order traversal of the tree.
	 * 
	 * @return a List<Node<T>>.
	 */
	public List<Node<T>> toList() {
		List<Node<T>> list = new ArrayList<Node<T>>();
		walk(rootElement, list);
		return list;
	}

	/**
	 * Returns a String representation of the Tree. The elements are generated
	 * from a pre-order traversal of the Tree.
	 * 
	 * @return the String representation of the Tree.
	 */
	public String toString() {
		return toList().toString();
	}

	/**
	 * Walks the Tree in pre-order style. This is a recursive method, and is
	 * called from the toList() method with the root element as the first
	 * argument. It appends to the second argument, which is passed by reference
	 * * as it recurses down the tree.
	 * 
	 * @param element
	 *            the starting element.
	 * @param list
	 *            the output of the walk.
	 */
	private void walk(Node<T> element, List<Node<T>> list) {
		list.add(element);
		for (Node<T> data : element.getChildren()) {
			walk(data, list);
		}
	}

	/**
	 * Added by DM. Return list of nodes without children, that is leaf-nodes.
	 */
	public List<Node<T>> getLeafNodes() {
		List<Node<T>> leafNodeList = new ArrayList<Node<T>>();
		for (Node<T> node : toList()) {
			if (node.getChildren().isEmpty()) {
				leafNodeList.add(node);
			}
		}
		return leafNodeList;
	}
//
//	/**
//	 * Added by DM. Return all possible tree paths.
//	 */
//	public List<List<Node<T>>> getTreePaths() {
//		List<List<Node<T>>> treePaths = new ArrayList<List<Node<T>>>();
//		Node<T>[] path = (Node<T>[]) new Object[1000];	
//		computeTreePaths(rootElement, path, 0, treePaths);
//		return treePaths;
//	}
//
//	private void computeTreePaths(Node<T> node, Node<T>[] path, int pathLen, List<List<Node<T>>> treePaths) {
//		if (node == null)
//			return;
//
//		path[pathLen] = node;
//		pathLen = pathLen + 1;
//
//		if (node.getChildren().isEmpty()) {
//			List<Node<T>> treePath = new ArrayList<Node<T>>();
//			for (int i = 0; i < pathLen; i++) {
//				treePath.add(path[i]);
//			}
//			treePaths.add(treePath);
//		} else {
//			for (Node<T> n : node.getChildren()) {
//				computeTreePaths(n, path, pathLen,treePaths);
//			}
//		}
//	}

	//
	// /**
	// * Added by D.M. Print tree paths.
	// */
	// public void printPaths(){
	// String[] path = new String[1000];
	// printPaths(rootElement, path,0);
	// }
	//
	// /**
	// * Added by D.M.
	// */
	// private void printPaths(Node<T> node, String[] path, int pathLen){
	// if(node==null) return;
	//
	// path[pathLen] = node.data.toString();
	// pathLen = pathLen + 1;
	//
	// if(node.getChildren().isEmpty()){
	// for(int i=0;i<pathLen;i++){
	// System.out.print(path[i].toString()+" ");
	// }
	// System.out.println();
	// }
	// else{
	// for(Node<T> n : node.getChildren()){
	// printPaths(n,path,pathLen);
	// }
	// }
	// }

}
