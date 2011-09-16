package parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;


import tree.Node;
import tree.Tree;
import utils.LogicTreeUtils;

public class LogicTreeParserTest {

	// test file containing symmetric logic tree defining source model
	// epistemic uncertainties
	public static final String SYMMETRIC_LT_SRC_MODEL_TEST_FILE = "symmetric-logic-tree-source-model.xml";

	// test file containing logic trees defining gmpe
	// epistemic uncertainties
	public static final String LT_GMPE_TEST_FILE = "logic-tree-gmpe.xml";

	// test file containing non-symmetric logic tree defining source model
	// epistemic uncertainties
	public static final String NON_SYMMETRIC_LT_SRC_MODEL_TEST_FILE = "non-symmetric-logic-tree-source-model.xml";

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
		Tree<LogicTreeNode> tree = parser.parse();
		assertEquals("", tree.getRootElement().data.getUncertaintyModel());
		assertEquals("", tree.getRootElement().data.getUncertaintyType());
		assertEquals(1, tree.getRootElement().data.getUncertaintyWeight(), 0.0);
	}

	// check that the root element has only two children. Check also that
	// these two children describe uncertainties in the source model. Check also
	// that each node contains the expected uncertainty model and weight.
	@Test
	public void sourceModelSymmetricLogicTreeTest2() {
		LogicTreeParser parser = new LogicTreeParser(
				SYMMETRIC_LT_SRC_MODEL_TEST_FILE);
		Tree<LogicTreeNode> tree = parser.parse();
		assertEquals(2, tree.getRootElement().getChildren().size());
		for (Node<LogicTreeNode> node : tree.getRootElement().getChildren()) {
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

	// check that nodes that are children of the root element, are parents of
	// three children each. Each child describe uncertainties on Gutenberg
	// Richter maximum magnitude (that is maxMagnitudeGutenbergRichterRelative).
	@Test
	public void sourceModelSymmetricLogicTreeTest3() {
		LogicTreeParser parser = new LogicTreeParser(
				SYMMETRIC_LT_SRC_MODEL_TEST_FILE);
		Tree<LogicTreeNode> tree = parser.parse();
		List<Node<LogicTreeNode>> nodeList = tree.getRootElement()
				.getChildren();
		for (Node<LogicTreeNode> node : nodeList) {
			assertEquals(3, node.getChildren().size());
			List<Node<LogicTreeNode>> children = node.getChildren();
			for (Node<LogicTreeNode> child : children) {
				assertTrue(child.data.getUncertaintyType().equalsIgnoreCase(
						"maxMagGRRelative"));
				assertTrue(child.data.getUncertaintyModel().equalsIgnoreCase(
						"0.2")
						|| child.data.getUncertaintyModel().equalsIgnoreCase(
								"0.0")
						|| child.data.getUncertaintyModel().equalsIgnoreCase(
								"-0.2"));
				if (child.data.getUncertaintyModel().equalsIgnoreCase("0.2")) {
					assertEquals(0.2, child.data.getUncertaintyWeight(), 0);
				}
				if (child.data.getUncertaintyModel().equalsIgnoreCase("0.0")) {
					assertEquals(0.6, child.data.getUncertaintyWeight(), 0);
				}
				if (child.data.getUncertaintyModel().equalsIgnoreCase("-0.2")) {
					assertEquals(0.2, child.data.getUncertaintyWeight(), 0);
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
		Tree<LogicTreeNode> tree = parser.parse();
		List<Node<LogicTreeNode>> nodeList = tree.getRootElement()
				.getChildren();
		for (Node<LogicTreeNode> node : nodeList) {
			for (Node<LogicTreeNode> child : node.getChildren()) {
				for (Node<LogicTreeNode> grandChild : child.getChildren()) {
					assertTrue(grandChild.data.getUncertaintyType()
							.equalsIgnoreCase("bGRRelative"));
					assertTrue(grandChild.data.getUncertaintyModel()
							.equalsIgnoreCase("0.1")
							|| grandChild.data.getUncertaintyModel()
									.equalsIgnoreCase("0.0")
							|| grandChild.data.getUncertaintyModel()
									.equalsIgnoreCase("-0.1"));
					if (grandChild.data.getUncertaintyModel().equalsIgnoreCase(
							"0.1")) {
						assertEquals(0.2,
								grandChild.data.getUncertaintyWeight(), 0);
					}
					if (grandChild.data.getUncertaintyModel().equalsIgnoreCase(
							"0.0")) {
						assertEquals(0.6,
								grandChild.data.getUncertaintyWeight(), 0);
					}
					if (grandChild.data.getUncertaintyModel().equalsIgnoreCase(
							"-0.1")) {
						assertEquals(0.2,
								grandChild.data.getUncertaintyWeight(), 0);
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
		Tree<LogicTreeNode> tree = parser.parse();
		assertEquals(18, tree.getLeafNodes().size());
	}

	// check that in the first branching level, two gmpes are defined for active
	// shallow crust (BA_2008 and CB_2008).
	@Test
	public void gmpeLogicTreeTest1() {
		LogicTreeParser parser = new LogicTreeParser(LT_GMPE_TEST_FILE);
		Tree<LogicTreeNode> tree = parser.parse();
		List<Node<LogicTreeNode>> children = tree.getRootElement()
				.getChildren();
		assertEquals(2, children.size());
		for (Node<LogicTreeNode> child : children) {
			assertTrue(child.data.getUncertaintyType().equalsIgnoreCase(
					"gmpeModel"));
			assertTrue(child.data.getUncertaintyModel().equalsIgnoreCase(
					"BA_2008_AttenRel")
					|| child.data.getUncertaintyModel().equalsIgnoreCase(
							"CB_2008_AttenRel"));
			if (child.data.getUncertaintyModel().equalsIgnoreCase(
					"BA_2008_AttenRel")) {
				assertEquals(0.5, child.data.getUncertaintyWeight(), 0);
			}
			if (child.data.getUncertaintyModel().equalsIgnoreCase(
					"CB_2008_AttenRel")) {
				assertEquals(0.5, child.data.getUncertaintyWeight(), 0);
			}
		}
	}

	// check that in the 2nd branching level one gmpe is defined
	// (McVerryetal_2000_AttenRel) with full weight.
	@Test
	public void gmpeLogicTreeTest3() {
		LogicTreeParser parser = new LogicTreeParser(LT_GMPE_TEST_FILE);
		Tree<LogicTreeNode> tree = parser.parse();
		List<Node<LogicTreeNode>> children = tree.getRootElement()
				.getChildren();
		for (Node<LogicTreeNode> child : children) {
			List<Node<LogicTreeNode>> grandChildren = child.getChildren();
			for (Node<LogicTreeNode> grandChild : grandChildren) {
				assertTrue(grandChild.getChildren().isEmpty());
				assertTrue(grandChild.data.getUncertaintyType()
						.equalsIgnoreCase("gmpeModel"));
				assertTrue(grandChild.data.getUncertaintyModel()
						.equalsIgnoreCase("McVerryetal_2000_AttenRel"));
				assertEquals(1.0, grandChild.data.getUncertaintyWeight(), 0);
			}
		}
	}

	// check content of the non-symmetric logic tree
	@Test
	public void sourceModelNonSymmetricLogicTreeTest1() {

		// expected nodes in the first branching level
		LogicTreeNode node1 = new LogicTreeNode("_11", "sourceModel",
				"source_model_1.xml", 0.2, "", "", "");
		LogicTreeNode node2 = new LogicTreeNode("_12", "sourceModel",
				"source_model_2.xml", 0.6, "", "", "");
		LogicTreeNode node3 = new LogicTreeNode("_13", "sourceModel",
				"source_model_3.xml", 0.2, "", "", "");
		Set<LogicTreeNode> expectedNodes1 = new HashSet<LogicTreeNode>();
		expectedNodes1.add(node1);
		expectedNodes1.add(node2);
		expectedNodes1.add(node3);

		// expected nodes in the second branching level (linked to branches with
		// ID = _11 _12)
		LogicTreeNode node4 = new LogicTreeNode("_11_12_21", "abGRAbsolute",
				"3.0 1.0", 0.2, "", "area", "");
		LogicTreeNode node5 = new LogicTreeNode("_11_12_22", "abGRAbsolute",
				"3.2 1.2", 0.6, "", "area", "");
		LogicTreeNode node6 = new LogicTreeNode("_11_12_23", "abGRAbsolute",
				"2.8 0.8", 0.2, "", "area", "");
		Set<LogicTreeNode> expectedNodes2 = new HashSet<LogicTreeNode>();
		expectedNodes2.add(node4);
		expectedNodes2.add(node5);
		expectedNodes2.add(node6);

		// expected nodes in the second branching level (linked to branch with
		// ID = _13)
		LogicTreeNode node7 = new LogicTreeNode("_13_21", "abGRAbsolute",
				"2.0 1.0", 0.2, "_2 _3", "", "");
		LogicTreeNode node8 = new LogicTreeNode("_13_22", "abGRAbsolute",
				"1.8 0.8", 0.8, "_2 _3", "", "");
		Set<LogicTreeNode> expectedNodes3 = new HashSet<LogicTreeNode>();
		expectedNodes3.add(node7);
		expectedNodes3.add(node8);

		// expected nodes in the third branching level (linked to all branches
		// in the previous branching level)
		LogicTreeNode node9 = new LogicTreeNode("_31", "maxMagGRAbsolute",
				"7.5", 0.2, "", "", "Active Shallow Crust");
		LogicTreeNode node10 = new LogicTreeNode("_32", "maxMagGRAbsolute",
				"7.2", 0.8, "", "", "Active Shallow Crust");
		Set<LogicTreeNode> expectedNodes4 = new HashSet<LogicTreeNode>();
		expectedNodes4.add(node9);
		expectedNodes4.add(node10);

		LogicTreeParser parser = new LogicTreeParser(
				NON_SYMMETRIC_LT_SRC_MODEL_TEST_FILE);
		Tree<LogicTreeNode> tree = parser.parse();

		// check nodes in the first branching level
		List<Node<LogicTreeNode>> children = tree.getRootElement()
				.getChildren();
		Set<LogicTreeNode> computedNodes = new HashSet<LogicTreeNode>();
		for (Node<LogicTreeNode> n : children) {
			computedNodes.add(n.getData());
		}
		assertTrue(expectedNodes1.equals(computedNodes));

		// check nodes in the second branching level
		for (Node<LogicTreeNode> child : children) {
			List<Node<LogicTreeNode>> grandChildren = child.getChildren();
			computedNodes = new HashSet<LogicTreeNode>();
			for (Node<LogicTreeNode> n : grandChildren) {
				computedNodes.add(n.getData());
			}
			if (child.getData().equals(node1) || child.getData().equals(node2)) {
				assertTrue(expectedNodes2.equals(computedNodes));
			}
			if (child.getData().equals(node3)) {
				assertTrue(expectedNodes3.equals(computedNodes));
			}
		}

		// check nodes in the third branching level
		for (Node<LogicTreeNode> child : children) {
			List<Node<LogicTreeNode>> grandChildren = child.getChildren();
			for (Node<LogicTreeNode> grandChild : grandChildren) {
				List<Node<LogicTreeNode>> grandGrandChildren = grandChild
						.getChildren();
				computedNodes = new HashSet<LogicTreeNode>();
				for (Node<LogicTreeNode> grandGrandChild : grandGrandChildren) {
					computedNodes.add(grandGrandChild.getData());
				}
				assertTrue(expectedNodes4.equals(computedNodes));
			}
		}

	}
}
