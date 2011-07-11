package parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import tree.Node;
import tree.Tree;

public class LogicTreeParserTest {

	// test file containing symmetric logic tree defining source model
	// epistemic uncertainties
	public static final String SYMMETRIC_LT_SRC_MODEL_TEST_FILE = "logic-tree-source-model.xml";

	// test file containing logic trees defining gmpe
	// epistemic uncertainties
	public static final String LT_GMPE_TEST_FILE = "logic-tree-gmpe.xml";

	@Before
	public void setUp() {
		System.setProperty("openquake.nrml.schema", new File(
				"docs/schema/nrml.xsd").getAbsolutePath());
	}

	// check that the root node contains an empty LogicTreeNode, that is with
	// uncertainty type and model set to empty strings, but weight equal to 1.
	@Test
	public void sourceModelSymmetricLogicTreeTest1() {
		LogicTreeParser parser = new LogicTreeParser(
				SYMMETRIC_LT_SRC_MODEL_TEST_FILE);
		Map<String, Tree<LogicTreeNode>> treeMap = parser.parse();
		for (String key : treeMap.keySet()) {
			assertEquals("",
					treeMap.get(key).getRootElement().data
							.getUncertaintyModel());
			assertEquals("",
					treeMap.get(key).getRootElement().data.getUncertaintyType());
			assertEquals(1,
					treeMap.get(key).getRootElement().data
							.getUncertaintyWeight(), 0.0);
		}
	}

	// check that the root element has only two children. Check also that
	// these two children describe uncertainties in the source model. Check also
	// that each node contains the expected uncertainty model and weight.
	@Test
	public void sourceModelSymmetricLogicTreeTest2() {
		LogicTreeParser parser = new LogicTreeParser(
				SYMMETRIC_LT_SRC_MODEL_TEST_FILE);
		Map<String, Tree<LogicTreeNode>> treeMap = parser.parse();
		for (String key : treeMap.keySet()) {
			assertEquals(2, treeMap.get(key).getRootElement().getChildren()
					.size());
			for (Node<LogicTreeNode> node : treeMap.get(key).getRootElement()
					.getChildren()) {
				assertTrue(node.data.getUncertaintyType().equalsIgnoreCase(
						"sourceModel"));
				assertTrue(node.data.getUncertaintyModel().equalsIgnoreCase(
						"source_model_1.xml")
						|| node.data.getUncertaintyModel().equalsIgnoreCase(
								"source_model_2.xml"));
				if (node.data.getUncertaintyModel().equalsIgnoreCase(
						"source_model_1.xml")) {
					assertEquals(0.5, node.data.getUncertaintyWeight(), 0);
				}
				if (node.data.getUncertaintyModel().equalsIgnoreCase(
						"source_model_2.xml")) {
					assertEquals(0.5, node.data.getUncertaintyWeight(), 0);
				}

			}
		}
	}

	// check that nodes that are children of the root element, are parents of
	// three children each. Each child describe uncertainties on Gutenberg
	// Richter maximum magnitude (that is maxMagnitudeGutenbergRichterRelative).
	@Test
	public void sourceModelSymmetricLogicTreeTest3() {
		LogicTreeParser parser = new LogicTreeParser(
				SYMMETRIC_LT_SRC_MODEL_TEST_FILE);
		Map<String, Tree<LogicTreeNode>> treeMap = parser.parse();
		for (String key : treeMap.keySet()) {
			List<Node<LogicTreeNode>> nodeList = treeMap.get(key)
					.getRootElement().getChildren();
			for (Node<LogicTreeNode> node : nodeList) {
				assertEquals(3, node.getChildren().size());
				List<Node<LogicTreeNode>> children = node.getChildren();
				for (Node<LogicTreeNode> child : children) {
					assertTrue(child.data.getUncertaintyType()
							.equalsIgnoreCase(
									"maxMagnitudeGutenbergRichterRelative"));
					assertTrue(child.data.getUncertaintyModel()
							.equalsIgnoreCase("0.2")
							|| child.data.getUncertaintyModel()
									.equalsIgnoreCase("0.0")
							|| child.data.getUncertaintyModel()
									.equalsIgnoreCase("-0.2"));
					if (child.data.getUncertaintyModel()
							.equalsIgnoreCase("0.2")) {
						assertEquals(0.2, child.data.getUncertaintyWeight(), 0);
					}
					if (child.data.getUncertaintyModel()
							.equalsIgnoreCase("0.0")) {
						assertEquals(0.6, child.data.getUncertaintyWeight(), 0);
					}
					if (child.data.getUncertaintyModel().equalsIgnoreCase(
							"-0.2")) {
						assertEquals(0.2, child.data.getUncertaintyWeight(), 0);
					}
				}
			}
		}
	}

	// check that children of the root node's children, are parents of three
	// children each. Each child describes uncertainties on GutenbergRichter b
	// value (bValueGutenbergRichterRelative).
	@Test
	public void sourceModelSymmetricLogicTreeTest4() {
		LogicTreeParser parser = new LogicTreeParser(
				SYMMETRIC_LT_SRC_MODEL_TEST_FILE);
		Map<String, Tree<LogicTreeNode>> treeMap = parser.parse();
		for (String key : treeMap.keySet()) {
			List<Node<LogicTreeNode>> nodeList = treeMap.get(key)
					.getRootElement().getChildren();
			for (Node<LogicTreeNode> node : nodeList) {
				for (Node<LogicTreeNode> child : node.getChildren()) {
					for (Node<LogicTreeNode> grandChild : child.getChildren()) {
						assertTrue(grandChild.data.getUncertaintyType()
								.equalsIgnoreCase(
										"bValueGutenbergRichterRelative"));
						assertTrue(grandChild.data.getUncertaintyModel()
								.equalsIgnoreCase("0.1")
								|| grandChild.data.getUncertaintyModel()
										.equalsIgnoreCase("0.0")
								|| grandChild.data.getUncertaintyModel()
										.equalsIgnoreCase("-0.1"));
						if (grandChild.data.getUncertaintyModel()
								.equalsIgnoreCase("0.1")) {
							assertEquals(0.2,
									grandChild.data.getUncertaintyWeight(), 0);
						}
						if (grandChild.data.getUncertaintyModel()
								.equalsIgnoreCase("0.0")) {
							assertEquals(0.6,
									grandChild.data.getUncertaintyWeight(), 0);
						}
						if (grandChild.data.getUncertaintyModel()
								.equalsIgnoreCase("-0.1")) {
							assertEquals(0.2,
									grandChild.data.getUncertaintyWeight(), 0);
						}
					}
				}
			}
		}
	}

	// check number of end-branch models. In SYMMETRIC_LT_SRC_MODEL_TEST_FILE,
	// the first branching level contains two branches, the second branching
	// level contains three branches, and the third branching level also three
	// branches. So, given that the logic tree is assumed to be symmetric, the
	// total number of end-branches should be 2 * 3 * 3 = 18
	@Test
	public void sourceModelSymmetricLogicTreeTest5() {
		LogicTreeParser parser = new LogicTreeParser(
				SYMMETRIC_LT_SRC_MODEL_TEST_FILE);
		Map<String, Tree<LogicTreeNode>> treeMap = parser.parse();
		for (String key : treeMap.keySet()) {
			assertEquals(18, treeMap.get(key).getLeafNodes().size());
		}
	}

	// check that two logic trees are defined one for Active Shallow Crust and a
	// second for Subduction Interface.
	@Test
	public void gmpeLogicTreeTest1() {
		LogicTreeParser parser = new LogicTreeParser(LT_GMPE_TEST_FILE);
		Map<String, Tree<LogicTreeNode>> treeMap = parser.parse();
		for (String key : treeMap.keySet()) {
			assertTrue(key.equalsIgnoreCase("Active Shallow Crust")
					|| key.equalsIgnoreCase("Subduction Interface"));
		}
	}

	// check that the logic tree defined for active shallow crust contains only two
	// nodes (BA_2008 and CB_2008).
	@Test
	public void gmpeLogicTreeTest2() {
		LogicTreeParser parser = new LogicTreeParser(LT_GMPE_TEST_FILE);
		Map<String, Tree<LogicTreeNode>> treeMap = parser.parse();
		Tree<LogicTreeNode> tree = treeMap.get("Active Shallow Crust");
		List<Node<LogicTreeNode>> children = tree.getRootElement()
				.getChildren();
		assertEquals(2, children.size());
		for (Node<LogicTreeNode> child : children) {
			assertTrue(child.getChildren().isEmpty());
			assertTrue(child.data.getUncertaintyType().equalsIgnoreCase(
					"gmpeModel"));
			assertTrue(child.data.getUncertaintyModel().equalsIgnoreCase(
					"BA_2008_AttenRel")
					|| child.data.getUncertaintyModel().equalsIgnoreCase(
							"CB_2008_AttenRel"));
			if (child.data.getUncertaintyModel()
					.equalsIgnoreCase("BA_2008_AttenRel")) {
				assertEquals(0.5,
						child.data.getUncertaintyWeight(), 0);
			}
			if (child.data.getUncertaintyModel()
					.equalsIgnoreCase("CB_2008_AttenRel")) {
				assertEquals(0.5,
						child.data.getUncertaintyWeight(), 0);
			}
		}
	}
	
	// check that the logic tree defined for subduction interface contains one
	// node (McVerryetal_2000_AttenRel) with full weight.
	@Test
	public void gmpeLogicTreeTest3() {
		LogicTreeParser parser = new LogicTreeParser(LT_GMPE_TEST_FILE);
		Map<String, Tree<LogicTreeNode>> treeMap = parser.parse();
		Tree<LogicTreeNode> tree = treeMap.get("Subduction Interface");
		List<Node<LogicTreeNode>> children = tree.getRootElement()
				.getChildren();
		assertEquals(1, children.size());
		for (Node<LogicTreeNode> child : children) {
			assertTrue(child.getChildren().isEmpty());
			assertTrue(child.data.getUncertaintyType().equalsIgnoreCase(
					"gmpeModel"));
			assertTrue(child.data.getUncertaintyModel().equalsIgnoreCase(
					"McVerryetal_2000_AttenRel"));
			assertEquals(1.0,
					child.data.getUncertaintyWeight(), 0);
		}
	}

}
