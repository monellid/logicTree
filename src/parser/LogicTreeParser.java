package parser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import tree.Node;
import tree.Tree;

/**
 * Parse logicTree element. Returns a {@link Tree} containing nodes of type
 * {@link LogicTreeNode}.The root of each tree is defined as an 'empty' logic
 * tree node (that is with uncertainty type and uncertainty model set to empty
 * strings but with weight equal to 1).
 * 
 * @author damianomonelli
 * 
 */
public class LogicTreeParser {

	private final BufferedReader bufferedReader;

	private final Tree<LogicTreeNode> logicTree;

	private static final String BRANCHING_LEVEL = "branchingLevel";
	private static final String UNCERTAINTY_TYPE = "uncertaintyType";
	private static final String UNCERTAINTY_MODEL = "uncertaintyModel";
	private static final String UNCERTAINTY_WEIGHT = "uncertaintyWeight";

	public LogicTreeParser(String path) {
		File xml = new File(path);
		FileInputStream fileInputStream;
		try {
			fileInputStream = new FileInputStream(xml.getPath());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		BufferedInputStream bufferedInputStream = new BufferedInputStream(
				fileInputStream);
		this.bufferedReader = new BufferedReader(new InputStreamReader(
				bufferedInputStream));
		logicTree = new Tree<LogicTreeNode>();
		// set root element as an empty branch
		logicTree.setRootElement(new Node(new LogicTreeNode()));
	}

	/**
	 * Reads file and returns logic tree data.
	 */
	public Tree<LogicTreeNode> parse() {
		if (System.getProperty("openquake.nrml.schema") == null)
			throw new RuntimeException(
					"Set openquake.nrml.schema property to the NRML schema path");

		SAXReader reader = new SAXReader(true);
		Document doc = null;
		try {
			reader.setFeature(
					"http://apache.org/xml/features/validation/schema", true);
			reader.setProperty(
					"http://java.sun.com/xml/jaxp/properties/schemaLanguage",
					"http://www.w3.org/2001/XMLSchema");
			reader.setProperty(
					"http://java.sun.com/xml/jaxp/properties/schemaSource",
					"file://" + System.getProperty("openquake.nrml.schema"));
			doc = reader.read(this.bufferedReader);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		Element root = doc.getRootElement(); // <nrml> element

		Iterator i = root.elements().iterator();
		while (i.hasNext()) {
			Element elem = (Element) i.next();
			
			// skip config for now
			if (elem.getName().equals("config")) {
				continue;
			}
			
			parseLogicTree(elem,logicTree);
		}
		return logicTree;
	}

	/**
	 * Parse attributes and children of a &lt;logicTree&gt; element.
	 * 
	 * @param logicTreeElem
	 * @param logicTree
	 */
	private void parseLogicTree(Element logicTreeElem,
			Tree<LogicTreeNode> logicTree) {

		Iterator i = logicTreeElem.elementIterator();
		while (i.hasNext()) {
			Element branchSet = (Element) i.next();

			parseLogicTreeBranchSet(branchSet, logicTree);

		}
	}

	/**
	 * Parse attributes and children of a &lt;logicTreeBranchSet&gt; element.
	 * 
	 * @param branchSet
	 * @param logicTree
	 */
	private void parseLogicTreeBranchSet(Element branchSet,
			Tree<LogicTreeNode> logicTree) {

		int indexBranchingLevel = Integer.parseInt(branchSet
				.attributeValue(BRANCHING_LEVEL));

		String uncertaintyType = branchSet.attributeValue(UNCERTAINTY_TYPE);

		// the current schema assumes symmetric logic tree
		// so each node in this branch set applies
		// to all current end nodes
		List<Node<LogicTreeNode>> nList = logicTree.getLeafNodes();
		for (Node<LogicTreeNode> n : nList) {
			// loop over branches
			Iterator i = branchSet.elementIterator();
			while (i.hasNext()) {
				Element logicTreeBranch = (Element) i.next();

				String uncertaintyModel = (String) logicTreeBranch.element(
						UNCERTAINTY_MODEL).getData();

				Double uncertaintyWeight = Double
						.valueOf((String) logicTreeBranch.element(
								UNCERTAINTY_WEIGHT).getData());

				// create logic tree node
				LogicTreeNode logicTreeNode = new LogicTreeNode(
						uncertaintyType, uncertaintyModel, uncertaintyWeight);
				// create corresponding node
				Node<LogicTreeNode> node = new Node<LogicTreeNode>(
						logicTreeNode);

				// add node as a child
				n.addChild(node);
			}
		}

	}
}
