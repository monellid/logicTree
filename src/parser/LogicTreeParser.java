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
 * Parse logicTreeSet element (that is a set of logic trees). Returns a
 * {@link Map} of {@link String}s and {@link Tree}s containing nodes of type
 * {@link LogicTreeNode}.The root of each tree is defined as an 'empty' logic
 * tree node (that is with uncertainty type and uncertainty model set to empty
 * strings but with weight equal to 1).
 * 
 * @author damianomonelli
 * 
 */
public class LogicTreeParser {

	private final BufferedReader bufferedReader;

	private final Map<String, Tree<LogicTreeNode>> logicTreeHashMap;

	private static final String BRANCHING_LEVEL = "branchingLevel";
	private static final String TECTONIC_REGION = "tectonicRegion";
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
		logicTreeHashMap = new HashMap<String, Tree<LogicTreeNode>>();
	}

	/**
	 * Reads file and returns logic tree data. The method loops over the
	 * possible logic trees defined in the file. For each logic tree definition,
	 * it creates a corresponding {@link Tree} object and stores it in a map
	 * with a key that is the logic tree number or the tectonic region type (if
	 * defined in the file).
	 */
	public Map<String, Tree<LogicTreeNode>> parse() {
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

		/**
		 * Makes a loop over the possible logic trees defined in the file. In
		 * case of the GMPE logic tree file, multiple logic trees are defined,
		 * one for each tectonic region implied by the source model. For the
		 * source model logic tree file, currently only one logic tree is
		 * defined. For each logic tree definition, creates a logic tree object.
		 * Depending on the uncertainty type, additional attributed of the
		 * branch class must be edited. In case of source model uncertainties, a
		 * source model file must be specified. In case of parameter
		 * uncertainties, a logic tree rule must be defined.
		 */
		int indexLogicTree = 1;
		Iterator i = root.elements().iterator();
		while (i.hasNext()) {
			Element logicTreeSetElem = (Element) i.next();

			Map<String, Tree<LogicTreeNode>> logicTrees = parseLogicTreeSet(
					logicTreeSetElem, indexLogicTree);

			for (String key : logicTrees.keySet()) {
				logicTreeHashMap.put(key, logicTrees.get(key));
			}

			indexLogicTree++;
		}
		return logicTreeHashMap;
	}

	/**
	 * Parse child elements of a &lt;logicTreeSet&gt; element.
	 * 
	 * @param logicTreeSet
	 * @param indexLogicTree
	 * @return Map of Trees, keyed by tectonicRegion. If no tectonicRegion is
	 *         defined for a logicTree, keys will be "1" through "N", where N is
	 *         the total number of logicTree elements in the logicTreeSet.
	 */
	private Map<String, Tree<LogicTreeNode>> parseLogicTreeSet(
			Element logicTreeSet, int indexLogicTree) {

		String key = Integer.toString(indexLogicTree);
		Map<String, Tree<LogicTreeNode>> logicTrees = new HashMap<String, Tree<LogicTreeNode>>();
		Iterator i = logicTreeSet.elementIterator();
		while (i.hasNext()) {
			Element elem = (Element) i.next();

			// define tree structure
			Tree<LogicTreeNode> logicTree = new Tree<LogicTreeNode>();

			// set root element as an empty branch
			Node<LogicTreeNode> logicTreeRoot = new Node(new LogicTreeNode());
			logicTree.setRootElement(logicTreeRoot);

			// skip config for now
			// TODO(LB): we might care about the <config> elem later
			// at the time this was written, the example files did not
			// include any config items
			if (elem.getName().equals("config")) {
				continue;
			}

			String tectonicRegion = parseLogicTree(elem, logicTree);
			if (tectonicRegion != null) {
				key = tectonicRegion;
			}
			logicTrees.put(key, logicTree);
		}
		return logicTrees;
	}

	/**
	 * Parse attributes and children of a &lt;logicTree&gt; element.
	 * 
	 * @param logicTreeElem
	 * @param logicTree
	 * @return tectonicRegion of the logic tree (or null if none is defined)
	 */
	private String parseLogicTree(Element logicTreeElem,
			Tree<LogicTreeNode> logicTree) {

		String tectonicRegion = logicTreeElem.attributeValue(TECTONIC_REGION);

		Iterator i = logicTreeElem.elementIterator();
		while (i.hasNext()) {
			Element branchSet = (Element) i.next();

			parseLogicTreeBranchSet(branchSet, logicTree);

		}
		return tectonicRegion;
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
