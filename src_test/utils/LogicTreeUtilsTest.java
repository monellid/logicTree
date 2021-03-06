package utils;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import parser.LogicTreeNode;
import parser.LogicTreeParser;
import tree.Tree;
import utils.LogicTreePath;
import utils.LogicTreeUtils;

public class LogicTreeUtilsTest {

	// test file containing symmetric logic tree defining source model
	// epistemic uncertainties
	public static final String SYMMETRIC_LT_SRC_MODEL_TEST_FILE = "symmetric-logic-tree-source-model.xml";

	// test file containing symmetric logic tree defining source model
	// epistemic uncertainties with invalid weights
	public static final String INVALID_SYMMETRIC_LT_SRC_MODEL_TEST_FILE = "invalid-weights-logic-tree-source-model.xml";

	// test file containing non symmetric logic tree defining source model
	// epistemic uncertainties
	public static final String NON_SYMMETRIC_LT_SRC_MODEL_TEST_FILE = "non-symmetric-logic-tree-source-model.xml";

	// test file containing logic tree defining gmpe epistemic uncertainties
	public static final String GMPE_LT_TEST_FILE = "logic-tree-gmpe.xml";

	private LogicTreeUtils treeProcessor;

	@Before
	public void setUp() {
		System.setProperty("openquake.nrml.schema", new File(
				"docs/schema/nrml.xsd").getAbsolutePath());
		treeProcessor = new LogicTreeUtils();
	}

	// check that SYMMETRIC_LT_SRC_MODEL_TEST_FILE has valid weights.
	@Test
	public void logicTreeHasValidWeightTest() {

		LogicTreeParser parser = new LogicTreeParser(
				SYMMETRIC_LT_SRC_MODEL_TEST_FILE);
		Tree<LogicTreeNode> tree = parser.parse();

		assertTrue(treeProcessor.hasValidWeights(tree));
	}

	// check that INVALID_SYMMETRIC_LT_SRC_MODEL_TEST_FILE has invalid weights.
	@Test
	public void logicTreeHasInvalidWeightsTest() {

		LogicTreeParser parser = new LogicTreeParser(
				INVALID_SYMMETRIC_LT_SRC_MODEL_TEST_FILE);
		Tree<LogicTreeNode> tree = parser.parse();

		assertFalse(treeProcessor.hasValidWeights(tree));
	}

	// check that logic tree paths are computed correctly. That is
	// compare computed logic tree paths with logic tree paths computed by hand.
	@Test
	public void checkLogicTreePathComputation() {

		LogicTreeParser parser = new LogicTreeParser(
				SYMMETRIC_LT_SRC_MODEL_TEST_FILE);
		Tree<LogicTreeNode> tree = parser.parse();

		Set<LogicTreePath> computedPaths = new HashSet<LogicTreePath>(
				treeProcessor.computeAllLogicTreePaths(tree));
		assertTrue(computedPaths.equals(getExpectedPathsSymmetricLogicTree()));
	}

	// check that logic tree paths are computed correctly (for non-symmetric
	// logic tree). That is compare computed logic tree paths with logic tree
	// paths computed by hand.
	@Test
	public void checkLogicTreePathComputationNonSymmetricLogicTree() {

		LogicTreeParser parser = new LogicTreeParser(
				NON_SYMMETRIC_LT_SRC_MODEL_TEST_FILE);
		Tree<LogicTreeNode> tree = parser.parse();

		Set<LogicTreePath> computedPaths = new HashSet<LogicTreePath>(
				treeProcessor.computeAllLogicTreePaths(tree));
		assertTrue(computedPaths
				.equals(getExpectedPathsForNonSymmetricLogicTree()));
	}

	// check logic tree path computation for GMPE logic tree. That is compare
	// computed logic tree paths with logic tree paths computed by hand.
	@Test
	public void checkLogicTreePathComputationGMPELogicTree() {
		LogicTreeParser parser = new LogicTreeParser(GMPE_LT_TEST_FILE);
		Tree<LogicTreeNode> tree = parser.parse();

		Set<LogicTreePath> computedPaths = new HashSet<LogicTreePath>(
				treeProcessor.computeAllLogicTreePaths(tree));
		assertTrue(computedPaths.equals(getExpetedPathsGMPELogicTree()));
	}

	// check that sampled paths are contained in the expected set of logic tree
	// paths.
	@Test
	public void checkLogicTreePathsSampling1() {

		Set<LogicTreePath> expectedPaths = getExpectedPathsSymmetricLogicTree();

		LogicTreeParser parser = new LogicTreeParser(
				SYMMETRIC_LT_SRC_MODEL_TEST_FILE);
		Tree<LogicTreeNode> tree = parser.parse();

		long seed = 123456789;
		Random rn = new Random(seed);
		int n = 100;

		List<LogicTreePath> sampledPaths = treeProcessor.sampleLogicTreePaths(
				tree, rn, n);
		for (LogicTreePath path : sampledPaths) {
			assertTrue(expectedPaths.contains(path));
		}

	}

	// check logic tree path sampling algorithm. That is count number of times
	// a path is sampled and compute the 'observed' probability (number of
	// times observed / total number of path sampled) and compare with expected
	// probability (that is the path weight computed by hand).
	@Test
	public void checkLogicTreePathSampling2() {

		LogicTreeParser parser = new LogicTreeParser(
				SYMMETRIC_LT_SRC_MODEL_TEST_FILE);
		Tree<LogicTreeNode> tree = parser.parse();

		// sample logic tree
		long seed = 123456789;
		Random rn = new Random(seed);
		int n = 5000;
		List<LogicTreePath> sampledPaths = treeProcessor.sampleLogicTreePaths(
				tree, rn, n);

		// loop over expected paths
		Set<LogicTreePath> expectedPaths = getExpectedPathsSymmetricLogicTree();
		for (LogicTreePath path : expectedPaths) {
			double pathCount = 0.0;
			// counts how many paths have been observed
			for (LogicTreePath sampledPath : sampledPaths) {
				if (sampledPath.equals(path)) {
					pathCount = pathCount + 1.0;
				}
			}
			// check observed against expected probability
			assertEquals(path.getPathWeight(), pathCount / n, 0.01);
		}
	}

	// check joinTree method by comparing expected with computed paths.
	// Takes source model and GMPE logic trees and joins them. Then check the
	// computed paths.
	@Test
	public void checkJoinTreesMethod() {
		
		// create source model and gmpe logic trees and then combine them.
		LogicTreeParser parser = new LogicTreeParser(
				SYMMETRIC_LT_SRC_MODEL_TEST_FILE);
		Tree<LogicTreeNode> sourceModelTree = parser.parse();

		parser = new LogicTreeParser(GMPE_LT_TEST_FILE);
		Tree<LogicTreeNode> gmpeTree = parser.parse();
		
		List<Tree<LogicTreeNode>> trees = new ArrayList<Tree<LogicTreeNode>>();
		trees.add(sourceModelTree);
		trees.add(gmpeTree);
		
		Tree<LogicTreeNode> joinedTree = treeProcessor.joinTrees(trees);
		
		Set<LogicTreePath> computedPaths = new HashSet<LogicTreePath>(
				treeProcessor.computeAllLogicTreePaths(joinedTree));
		for(LogicTreePath path : computedPaths){
			System.out.println(path.toString());
		}
		assertTrue(computedPaths.equals(getJoinedTreesExpectedPaths()));

	}

	private Set<LogicTreePath> getJoinedTreesExpectedPaths() {
		Set<LogicTreePath> expectedPaths = new HashSet<LogicTreePath>();
		// expected source model and gmpe logic tree paths
		Set<LogicTreePath> sourceModelExpectedPaths = getExpectedPathsSymmetricLogicTree();
		Set<LogicTreePath> gmpeExpectedPaths = getExpetedPathsGMPELogicTree();
		// join expected paths
		Iterator<LogicTreePath> sourceModelPathsIter = sourceModelExpectedPaths
				.iterator();
		while (sourceModelPathsIter.hasNext()) {
			LogicTreePath sourceModelPath = sourceModelPathsIter.next();
			Iterator<LogicTreePath> gmpePathsIter = gmpeExpectedPaths
					.iterator();
			while (gmpePathsIter.hasNext()) {
				LogicTreePath gmpePath = gmpePathsIter.next();
				List<LogicTreeNode> nodeList = new ArrayList<LogicTreeNode>();
				nodeList.addAll(sourceModelPath.getPath());
				nodeList.addAll(gmpePath.getPath());
				LogicTreePath path = new LogicTreePath(nodeList,
						sourceModelPath.getPathWeight()
								* gmpePath.getPathWeight());
				expectedPaths.add(path);
			}
		}
		return expectedPaths;
	}

	// logic tree paths in GMPE_LT_TEST_FILE
	private Set<LogicTreePath> getExpetedPathsGMPELogicTree() {

		// these are the expected nodes
		LogicTreeNode n0 = new LogicTreeNode();
		LogicTreeNode n11 = new LogicTreeNode("_11", "gmpeModel",
				"BA_2008_AttenRel", 0.5, "", "", "Active Shallow Crust");
		LogicTreeNode n12 = new LogicTreeNode("_12", "gmpeModel",
				"CB_2008_AttenRel", 0.5, "", "", "Active Shallow Crust");
		LogicTreeNode n21 = new LogicTreeNode("_21", "gmpeModel",
				"McVerryetal_2000_AttenRel", 1.0, "", "",
				"Subduction Interface");

		// these are the expected tree paths. There are a total of 1(root)*2*1 =
		// 2 paths
		Set<LogicTreePath> expectedPaths = new HashSet<LogicTreePath>();

		// path 1
		List<LogicTreeNode> p1 = new ArrayList<LogicTreeNode>();
		p1.add(0, n0);
		p1.add(1, n11);
		p1.add(2, n21);
		double pathWeight = 0.5 * 1.0;
		LogicTreePath path1 = new LogicTreePath(p1, pathWeight);
		// path 2
		List<LogicTreeNode> p2 = new ArrayList<LogicTreeNode>();
		p2.add(0, n0);
		p2.add(1, n12);
		p2.add(2, n21);
		pathWeight = 0.5 * 1.0;
		LogicTreePath path2 = new LogicTreePath(p2, pathWeight);

		expectedPaths.add(path1);
		expectedPaths.add(path2);
		return expectedPaths;
	}

	// logic tree paths in SYMMETRIC_LT_SRC_MODEL_TEST_FILE
	private Set<LogicTreePath> getExpectedPathsSymmetricLogicTree() {

		// these are the expected nodes
		LogicTreeNode n0 = new LogicTreeNode();
		LogicTreeNode n11 = new LogicTreeNode("_11", "sourceModel",
				"source_model_1.xml", 0.5, "", "", "");
		LogicTreeNode n12 = new LogicTreeNode("_12", "sourceModel",
				"source_model_2.xml", 0.5, "", "", "");
		LogicTreeNode n21 = new LogicTreeNode("_21",
				"maxMagGRRelative", "0.2", 0.2, "", "", "");
		LogicTreeNode n22 = new LogicTreeNode("_22",
				"maxMagGRRelative", "0.0", 0.6, "", "", "");
		LogicTreeNode n23 = new LogicTreeNode("_23",
				"maxMagGRRelative", "-0.2", 0.2, "", "", "");
		LogicTreeNode n31 = new LogicTreeNode("_31", "bGRRelative",
				"0.1", 0.2, "", "", "");
		LogicTreeNode n32 = new LogicTreeNode("_32", "bGRRelative",
				"0.0", 0.6, "", "", "");
		LogicTreeNode n33 = new LogicTreeNode("_33", "bGRRelative",
				"-0.1", 0.2, "", "", "");

		// these are the expected tree paths. There are a total of 1(root)*2*3*3
		// = 18 paths.
		Set<LogicTreePath> expectedPaths = new HashSet<LogicTreePath>();

		// path 1
		List<LogicTreeNode> p1 = new ArrayList<LogicTreeNode>();
		p1.add(0, n0);
		p1.add(1, n11);
		p1.add(2, n21);
		p1.add(3, n31);
		double pathWeight = 0.5 * 0.2 * 0.2;
		LogicTreePath path1 = new LogicTreePath(p1, pathWeight);
		// path 2
		List<LogicTreeNode> p2 = new ArrayList<LogicTreeNode>();
		p2.add(0, n0);
		p2.add(1, n11);
		p2.add(2, n21);
		p2.add(3, n32);
		pathWeight = 0.5 * 0.2 * 0.6;
		LogicTreePath path2 = new LogicTreePath(p2, pathWeight);
		// path 3
		List<LogicTreeNode> p3 = new ArrayList<LogicTreeNode>();
		p3.add(0, n0);
		p3.add(1, n11);
		p3.add(2, n21);
		p3.add(3, n33);
		pathWeight = 0.5 * 0.2 * 0.2;
		LogicTreePath path3 = new LogicTreePath(p3, pathWeight);

		// path 4
		List<LogicTreeNode> p4 = new ArrayList<LogicTreeNode>();
		p4.add(0, n0);
		p4.add(1, n11);
		p4.add(2, n22);
		p4.add(3, n31);
		pathWeight = 0.5 * 0.6 * 0.2;
		LogicTreePath path4 = new LogicTreePath(p4, pathWeight);
		// path 5
		List<LogicTreeNode> p5 = new ArrayList<LogicTreeNode>();
		p5.add(0, n0);
		p5.add(1, n11);
		p5.add(2, n22);
		p5.add(3, n32);
		pathWeight = 0.5 * 0.6 * 0.6;
		LogicTreePath path5 = new LogicTreePath(p5, pathWeight);
		// path 6
		List<LogicTreeNode> p6 = new ArrayList<LogicTreeNode>();
		p6.add(0, n0);
		p6.add(1, n11);
		p6.add(2, n22);
		p6.add(3, n33);
		pathWeight = 0.5 * 0.6 * 0.2;
		LogicTreePath path6 = new LogicTreePath(p6, pathWeight);

		// path 7
		List<LogicTreeNode> p7 = new ArrayList<LogicTreeNode>();
		p7.add(0, n0);
		p7.add(1, n11);
		p7.add(2, n23);
		p7.add(3, n31);
		pathWeight = 0.5 * 0.2 * 0.2;
		LogicTreePath path7 = new LogicTreePath(p7, pathWeight);
		// path 8
		List<LogicTreeNode> p8 = new ArrayList<LogicTreeNode>();
		p8.add(0, n0);
		p8.add(1, n11);
		p8.add(2, n23);
		p8.add(3, n32);
		pathWeight = 0.5 * 0.2 * 0.6;
		LogicTreePath path8 = new LogicTreePath(p8, pathWeight);
		// path 9
		List<LogicTreeNode> p9 = new ArrayList<LogicTreeNode>();
		p9.add(0, n0);
		p9.add(1, n11);
		p9.add(2, n23);
		p9.add(3, n33);
		pathWeight = 0.5 * 0.2 * 0.2;
		LogicTreePath path9 = new LogicTreePath(p9, pathWeight);

		// path 10
		List<LogicTreeNode> p10 = new ArrayList<LogicTreeNode>();
		p10.add(0, n0);
		p10.add(1, n12);
		p10.add(2, n21);
		p10.add(3, n31);
		pathWeight = 0.5 * 0.2 * 0.2;
		LogicTreePath path10 = new LogicTreePath(p10, pathWeight);
		// path 11
		List<LogicTreeNode> p11 = new ArrayList<LogicTreeNode>();
		p11.add(0, n0);
		p11.add(1, n12);
		p11.add(2, n21);
		p11.add(3, n32);
		pathWeight = 0.5 * 0.2 * 0.6;
		LogicTreePath path11 = new LogicTreePath(p11, pathWeight);
		// path 12
		List<LogicTreeNode> p12 = new ArrayList<LogicTreeNode>();
		p12.add(0, n0);
		p12.add(1, n12);
		p12.add(2, n21);
		p12.add(3, n33);
		pathWeight = 0.5 * 0.2 * 0.2;
		LogicTreePath path12 = new LogicTreePath(p12, pathWeight);

		// path 13
		List<LogicTreeNode> p13 = new ArrayList<LogicTreeNode>();
		p13.add(0, n0);
		p13.add(1, n12);
		p13.add(2, n22);
		p13.add(3, n31);
		pathWeight = 0.5 * 0.6 * 0.2;
		LogicTreePath path13 = new LogicTreePath(p13, pathWeight);
		// path 14
		List<LogicTreeNode> p14 = new ArrayList<LogicTreeNode>();
		p14.add(0, n0);
		p14.add(1, n12);
		p14.add(2, n22);
		p14.add(3, n32);
		pathWeight = 0.5 * 0.6 * 0.6;
		LogicTreePath path14 = new LogicTreePath(p14, pathWeight);
		// path 15
		List<LogicTreeNode> p15 = new ArrayList<LogicTreeNode>();
		p15.add(0, n0);
		p15.add(1, n12);
		p15.add(2, n22);
		p15.add(3, n33);
		pathWeight = 0.5 * 0.6 * 0.2;
		LogicTreePath path15 = new LogicTreePath(p15, pathWeight);

		// path 16
		List<LogicTreeNode> p16 = new ArrayList<LogicTreeNode>();
		p16.add(0, n0);
		p16.add(1, n12);
		p16.add(2, n23);
		p16.add(3, n31);
		pathWeight = 0.5 * 0.2 * 0.2;
		LogicTreePath path16 = new LogicTreePath(p16, pathWeight);
		// path 17
		List<LogicTreeNode> p17 = new ArrayList<LogicTreeNode>();
		p17.add(0, n0);
		p17.add(1, n12);
		p17.add(2, n23);
		p17.add(3, n32);
		pathWeight = 0.5 * 0.2 * 0.6;
		LogicTreePath path17 = new LogicTreePath(p17, pathWeight);
		// path 18
		List<LogicTreeNode> p18 = new ArrayList<LogicTreeNode>();
		p18.add(0, n0);
		p18.add(1, n12);
		p18.add(2, n23);
		p18.add(3, n33);
		pathWeight = 0.5 * 0.2 * 0.2;
		LogicTreePath path18 = new LogicTreePath(p18, pathWeight);

		expectedPaths.add(path1);
		expectedPaths.add(path2);
		expectedPaths.add(path3);
		expectedPaths.add(path4);
		expectedPaths.add(path5);
		expectedPaths.add(path6);
		expectedPaths.add(path7);
		expectedPaths.add(path8);
		expectedPaths.add(path9);
		expectedPaths.add(path10);
		expectedPaths.add(path11);
		expectedPaths.add(path12);
		expectedPaths.add(path13);
		expectedPaths.add(path14);
		expectedPaths.add(path15);
		expectedPaths.add(path16);
		expectedPaths.add(path17);
		expectedPaths.add(path18);
		return expectedPaths;
	}

	// logic tree paths in NON_SYMMETRIC_LT_SRC_MODEL_TEST_FILE
	private Set<LogicTreePath> getExpectedPathsForNonSymmetricLogicTree() {

		// these are the expected nodes
		LogicTreeNode n0 = new LogicTreeNode();
		LogicTreeNode n11 = new LogicTreeNode("_11", "sourceModel",
				"source_model_1.xml", 0.2, "", "", "");
		LogicTreeNode n12 = new LogicTreeNode("_12", "sourceModel",
				"source_model_2.xml", 0.6, "", "", "");
		LogicTreeNode n13 = new LogicTreeNode("_13", "sourceModel",
				"source_model_3.xml", 0.2, "", "", "");

		LogicTreeNode n111221 = new LogicTreeNode("_11_12_21", "abGRAbsolute",
				"3.0 1.0", 0.2, "", "area", "");
		LogicTreeNode n111222 = new LogicTreeNode("_11_12_22", "abGRAbsolute",
				"3.2 1.2", 0.6, "", "area", "");
		LogicTreeNode n111223 = new LogicTreeNode("_11_12_23", "abGRAbsolute",
				"2.8 0.8", 0.2, "", "area", "");

		LogicTreeNode n1321 = new LogicTreeNode("_13_21", "abGRAbsolute",
				"2.0 1.0", 0.2, "_2 _3", "", "");
		LogicTreeNode n1322 = new LogicTreeNode("_13_22", "abGRAbsolute",
				"1.8 0.8", 0.8, "_2 _3", "", "");

		LogicTreeNode n31 = new LogicTreeNode("_31", "maxMagGRAbsolute", "7.5",
				0.2, "", "", "Active Shallow Crust");
		LogicTreeNode n32 = new LogicTreeNode("_32", "maxMagGRAbsolute", "7.2",
				0.8, "", "", "Active Shallow Crust");

		// these are the expected tree paths. There are a total of 2*3*2 + 1*2*2
		// = 16 tree paths.
		Set<LogicTreePath> expectedPaths = new HashSet<LogicTreePath>();

		// path 1
		List<LogicTreeNode> p1 = new ArrayList<LogicTreeNode>();
		p1.add(0, n0);
		p1.add(1, n11);
		p1.add(2, n111221);
		p1.add(3, n31);
		double pathWeight = 0.2 * 0.2 * 0.2;
		LogicTreePath path1 = new LogicTreePath(p1, pathWeight);
		// path 2
		List<LogicTreeNode> p2 = new ArrayList<LogicTreeNode>();
		p2.add(0, n0);
		p2.add(1, n11);
		p2.add(2, n111221);
		p2.add(3, n32);
		pathWeight = 0.2 * 0.2 * 0.8;
		LogicTreePath path2 = new LogicTreePath(p2, pathWeight);

		// path 3
		List<LogicTreeNode> p3 = new ArrayList<LogicTreeNode>();
		p3.add(0, n0);
		p3.add(1, n11);
		p3.add(2, n111222);
		p3.add(3, n31);
		pathWeight = 0.2 * 0.6 * 0.2;
		LogicTreePath path3 = new LogicTreePath(p3, pathWeight);
		// path 4
		List<LogicTreeNode> p4 = new ArrayList<LogicTreeNode>();
		p4.add(0, n0);
		p4.add(1, n11);
		p4.add(2, n111222);
		p4.add(3, n32);
		pathWeight = 0.2 * 0.6 * 0.8;
		LogicTreePath path4 = new LogicTreePath(p4, pathWeight);

		// path 5
		List<LogicTreeNode> p5 = new ArrayList<LogicTreeNode>();
		p5.add(0, n0);
		p5.add(1, n11);
		p5.add(2, n111223);
		p5.add(3, n31);
		pathWeight = 0.2 * 0.2 * 0.2;
		LogicTreePath path5 = new LogicTreePath(p5, pathWeight);
		// path 6
		List<LogicTreeNode> p6 = new ArrayList<LogicTreeNode>();
		p6.add(0, n0);
		p6.add(1, n11);
		p6.add(2, n111223);
		p6.add(3, n32);
		pathWeight = 0.2 * 0.2 * 0.8;
		LogicTreePath path6 = new LogicTreePath(p6, pathWeight);

		// path 7
		List<LogicTreeNode> p7 = new ArrayList<LogicTreeNode>();
		p7.add(0, n0);
		p7.add(1, n12);
		p7.add(2, n111221);
		p7.add(3, n31);
		pathWeight = 0.6 * 0.2 * 0.2;
		LogicTreePath path7 = new LogicTreePath(p7, pathWeight);
		// path 8
		List<LogicTreeNode> p8 = new ArrayList<LogicTreeNode>();
		p8.add(0, n0);
		p8.add(1, n12);
		p8.add(2, n111221);
		p8.add(3, n32);
		pathWeight = 0.6 * 0.2 * 0.8;
		LogicTreePath path8 = new LogicTreePath(p8, pathWeight);

		// path 9
		List<LogicTreeNode> p9 = new ArrayList<LogicTreeNode>();
		p9.add(0, n0);
		p9.add(1, n12);
		p9.add(2, n111222);
		p9.add(3, n31);
		pathWeight = 0.6 * 0.6 * 0.2;
		LogicTreePath path9 = new LogicTreePath(p9, pathWeight);
		// path 10
		List<LogicTreeNode> p10 = new ArrayList<LogicTreeNode>();
		p10.add(0, n0);
		p10.add(1, n12);
		p10.add(2, n111222);
		p10.add(3, n32);
		pathWeight = 0.6 * 0.6 * 0.8;
		LogicTreePath path10 = new LogicTreePath(p10, pathWeight);

		// path 11
		List<LogicTreeNode> p11 = new ArrayList<LogicTreeNode>();
		p11.add(0, n0);
		p11.add(1, n12);
		p11.add(2, n111223);
		p11.add(3, n31);
		pathWeight = 0.6 * 0.2 * 0.2;
		LogicTreePath path11 = new LogicTreePath(p11, pathWeight);
		// path 12
		List<LogicTreeNode> p12 = new ArrayList<LogicTreeNode>();
		p12.add(0, n0);
		p12.add(1, n12);
		p12.add(2, n111223);
		p12.add(3, n32);
		pathWeight = 0.6 * 0.2 * 0.8;
		LogicTreePath path12 = new LogicTreePath(p12, pathWeight);

		// path 13
		List<LogicTreeNode> p13 = new ArrayList<LogicTreeNode>();
		p13.add(0, n0);
		p13.add(1, n13);
		p13.add(2, n1321);
		p13.add(3, n31);
		pathWeight = 0.2 * 0.2 * 0.2;
		LogicTreePath path13 = new LogicTreePath(p13, pathWeight);
		// path 14
		List<LogicTreeNode> p14 = new ArrayList<LogicTreeNode>();
		p14.add(0, n0);
		p14.add(1, n13);
		p14.add(2, n1321);
		p14.add(3, n32);
		pathWeight = 0.2 * 0.2 * 0.8;
		LogicTreePath path14 = new LogicTreePath(p14, pathWeight);

		// path 15
		List<LogicTreeNode> p15 = new ArrayList<LogicTreeNode>();
		p15.add(0, n0);
		p15.add(1, n13);
		p15.add(2, n1322);
		p15.add(3, n31);
		pathWeight = 0.2 * 0.8 * 0.2;
		LogicTreePath path15 = new LogicTreePath(p15, pathWeight);
		// path 16
		List<LogicTreeNode> p16 = new ArrayList<LogicTreeNode>();
		p16.add(0, n0);
		p16.add(1, n13);
		p16.add(2, n1322);
		p16.add(3, n32);
		pathWeight = 0.2 * 0.8 * 0.8;
		LogicTreePath path16 = new LogicTreePath(p16, pathWeight);

		expectedPaths.add(path1);
		expectedPaths.add(path2);
		expectedPaths.add(path3);
		expectedPaths.add(path4);
		expectedPaths.add(path5);
		expectedPaths.add(path6);
		expectedPaths.add(path7);
		expectedPaths.add(path8);
		expectedPaths.add(path9);
		expectedPaths.add(path10);
		expectedPaths.add(path11);
		expectedPaths.add(path12);
		expectedPaths.add(path13);
		expectedPaths.add(path14);
		expectedPaths.add(path15);
		expectedPaths.add(path16);
		return expectedPaths;
	}
}
