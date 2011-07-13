package parser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import tree.Node;
import tree.Tree;

/**
 * Parse logicTree element.
 * 
 * A LogicTree is defined as a sequence of LogicTreeBranchingLevel elements. The
 * position in the sequence specifies in which level of the tree the branching
 * level is located. That is, the first LogicTreeBranchingLevel element in the
 * sequence represents the first branching level in the tree, the second element
 * the second branching level in the tree, and so on.
 * 
 * A LogicTreeBranchingLevel is defined as a sequence of LogicTreeBranchSet
 * elements. Each LogicTreeBranchSet defines a particular epistemic uncertainty
 * inside a branching level.
 * 
 * A LogicTreeBranchSet is defined as a sequence of LogicTreeBranch elements. A
 * branch set has two required attributes (ID and uncertaintyType (defining the
 * type of epistemic uncertainty the branch set is defining)). Optional
 * attributes are:
 * 
 * - applyToBranches: to be used to specify to which LogicTreeBranch elements
 * (one or more), in the previous branching level, the branch set is linked to.
 * The default is the keyword ALL, which means that a branch set is by default
 * linked to all branches in the previous branching level.
 * 
 * - applyToSources: it can be used in the Source Model Logic Tree definition,
 * it allows to specify to which source in a source model the uncertainty
 * applies to.
 * 
 * - applyToSourceType: it can be used in the Source Model Logic Tree
 * definition, it allows to specify to which source type (area, point, simple
 * fault, complex fault) the uncertainty applies to.
 * 
 * - applyToTectonicRegionType: it can be used in both the Source Model and GMPE
 * Logic Tree definition, it allows to specify to which tectonic region type
 * (Active Shallow Crust, Stable Shallow Crust, etc.) the uncertainty applies
 * to.
 * 
 * A logicTreeBranchSet element contains a sequence of logicTreeBranch elements.
 * Each logicTreeBranch contains as required attribute:
 * 
 * - branchID (xs:ID) [that is the uncertainty ID; for instance, it is used to
 * construct non-symmetric logic tree, when the branchID is defined in the
 * applyToBranches attribute]
 * 
 * Two elements are defined in a logicTreeBranch:
 * 
 * - uncertaintyModel (xs:string) [this is a generic string identifying a model
 * of uncertainty. The interpretation of the string is done in view of the
 * uncertaintyType attribute in the branchSet element. For instance, if
 * uncertaintyType="sourceModel", then the string is supposed to contain the
 * path to an xml file containing a source model; if
 * uncertaintyType="gmpeModel", then the string is supposed to contain the name
 * of a GMPE. However this interpretation is not done at the parser level; the
 * parsers limits itself to store the uncertaintyModel as a string simply.]
 * 
 * - uncertaintyWeight (nrml:NonNegativeDoubleType) [this is the weight
 * (probability) associated to the uncertainty model.]
 * 
 * Returns a {@link Tree} containing nodes of type {@link LogicTreeNode}.The
 * root of each tree is defined as an 'empty' logic tree node (that is with
 * uncertainty type, uncertainty model, and applyTo flags set to empty strings
 * but with weight equal to 1).
 * 
 * @author damianomonelli
 * 
 */
public class LogicTreeParser {

	private final BufferedReader bufferedReader;

	private final Tree<LogicTreeNode> logicTree;

	private static final String APPLY_TO_BRANCHES = "applyToBranches";
	private static final String APPLY_TO_SOURCES = "applyToSources";
	private static final String APPLY_TO_SOURCE_TYPE = "applyToSourceType";
	private static final String APPLY_TO_TECTONIC_REGION_TYPE = "applyToTectonicRegionType";
	private static final String BRANCH_ID = "branchID";
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

			parseLogicTree(elem, logicTree);
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
			Element branchingLevel = (Element) i.next();

			parseLogicTreeBranchingLevel(branchingLevel, logicTree);

		}
	}

	/**
	 * Parse attributes and children of logicTreeBranchingLevel element.
	 */
	private void parseLogicTreeBranchingLevel(Element branchingLevel,
			Tree<LogicTreeNode> logicTree) {
		Iterator i = branchingLevel.elementIterator();
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

		String uncertaintyType = branchSet.attributeValue(UNCERTAINTY_TYPE);
		String applyToBranches = "";
		String applyToSources = "";
		String applyToSourceType = "";
		String applyToTectonicRegionType = "";
		if (branchSet.attributeValue(APPLY_TO_BRANCHES) != null) {
			applyToBranches = branchSet.attributeValue(APPLY_TO_BRANCHES);
		}
		if (branchSet.attributeValue(APPLY_TO_SOURCES) != null) {
			applyToSources = branchSet.attributeValue(APPLY_TO_SOURCES);
		}
		if (branchSet.attributeValue(APPLY_TO_SOURCE_TYPE) != null) {
			applyToSourceType = branchSet.attributeValue(APPLY_TO_SOURCE_TYPE);
		}
		if (branchSet.attributeValue(APPLY_TO_TECTONIC_REGION_TYPE) != null) {
			applyToTectonicRegionType = branchSet
					.attributeValue(APPLY_TO_TECTONIC_REGION_TYPE);
		}

		List<String> branchIDs = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(applyToBranches);
		while (st.hasMoreTokens()) {
			branchIDs.add(st.nextToken());
		}

		// get all the current leaf nodes and loop over them.
		// Add a node to a leaf node only if the applyToBranches attribute is
		// empty, or if the leaf node's branchID is among the IDs listed in
		// applyToBranches
		// flag.
		List<Node<LogicTreeNode>> nList = logicTree.getLeafNodes();
		for (Node<LogicTreeNode> n : nList) {
			// loop over branches
			Iterator i = branchSet.elementIterator();
			while (i.hasNext()) {
				Element logicTreeBranch = (Element) i.next();

				String branchID = logicTreeBranch.attributeValue(BRANCH_ID);

				String uncertaintyModel = (String) logicTreeBranch.element(
						UNCERTAINTY_MODEL).getData();

				Double uncertaintyWeight = Double
						.valueOf((String) logicTreeBranch.element(
								UNCERTAINTY_WEIGHT).getData());

				// create logic tree node
				LogicTreeNode logicTreeNode = new LogicTreeNode(branchID,
						uncertaintyType, uncertaintyModel, uncertaintyWeight,
						applyToSources, applyToSourceType,
						applyToTectonicRegionType);
				// create corresponding node
				Node<LogicTreeNode> node = new Node<LogicTreeNode>(
						logicTreeNode);

				// add node as a child
				if (branchIDs.contains("ALL")
						|| branchIDs.contains(n.getData().getBranchID())) {
					n.addChild(node);
				}
			}
		}

	}
}
