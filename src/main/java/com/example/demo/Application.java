package com.example.demo;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class Application {
	
	// https://learnforeverlearn.com/cooccurrence/
	// http://www.stat.wisc.edu/~st571-1/06-tables-4.pdf
	// https://mapr.com/products/mapr-sandbox-hadoop/tutorials/recommender-tutorial/
	
	private static int NUM_USERS = 4;
	private static int NUM_ITEMS = 8;
	
	public void run(String... args) {
				
		// Generate simulated user/item interactions.
		List<Interaction> interactionsRaw = getInteractions();
		
		// Generate the A matrix (user/item interaction).
		int[][] aMatrix = generateAmatrix(interactionsRaw);
		
		// Take transpose of A.
		int[][] aTransposeMatrix = generateAtransposeMatrix(aMatrix);
				
		// Create an example current interaction matrix.
		int currentItemInteraction[] = createCurrentInteractionMatrix();
		
		// Find users who do H.
		usersWhoDoH(aMatrix, currentItemInteraction);
		
		// User centric recommendations.
		userCentricRecommendations(aMatrix, aTransposeMatrix, currentItemInteraction);
		
		// Generate the cooccurance matrix
		int[][] cooccurance = generateCooccurance(aMatrix, aTransposeMatrix);
		
		testItemRecommendation(cooccurance, currentItemInteraction);
		
		List<ScoredPair> surprisingRelationsips = generateSurprisingRelationships(cooccurance);
		surprisingRelationshipRecommendation(surprisingRelationsips, currentItemInteraction);
		
		System.out.println("----------------------------------");
		
		double[][] t1 = new double[][] { {13, 1000}, {1000, 100000}};
		double[][] t2 = new double[][] { {1, 0}, {0, 2}};
		double[][] t3 = new double[][] { {1, 0}, {0, 10000}};
		double[][] t4 = new double[][] { {10, 0}, {0, 100000}};
		
		System.out.println(getGscore(t1));
		System.out.println(getGscore(t2));
		System.out.println(getGscore(t3));
		System.out.println(getGscore(t4));
	}
	
	private int[] createCurrentInteractionMatrix() {
		// Create an example current interaction matrix.
		int currentItemInteraction[] = new int[NUM_ITEMS];
		currentItemInteraction[4] = 1;			// just one interaction with item 4;
		System.out.println("Current Interaction Matrix");
		printMatrix(currentItemInteraction);
		return currentItemInteraction;
	}
	
	private void usersWhoDoH(int[][] aMatrix, int[] currentItemInteraction) {
		int[] users = matrixMultiply(aMatrix, currentItemInteraction);
		
		System.out.println("Users Who Do H");
		printMatrix(users);

	}
	
	private void userCentricRecommendations(
			int[][] aMatrix, 
			int[][] aTransposeMatrix, 
			int[] currentItemInteraction) {
		
		int[] usersWhoDoH = matrixMultiply(aMatrix, currentItemInteraction);
		int[] userCentricRecommendations = matrixMultiply(aTransposeMatrix, usersWhoDoH);
		
		System.out.println("User centric recommendations");
		printMatrix(userCentricRecommendations);

	}
	
	private int[][] generateAmatrix(List<Interaction> interactionsRaw) {
		// Build interaction history matrix.
		int interactionHistory[][] = new int[NUM_USERS][NUM_ITEMS];
		for (Interaction i : interactionsRaw) {
			interactionHistory[i.getUser()][i.getInteraction()]++;
		}
		
		System.out.println("Matrix A (Interaction Matrix)");
		printMatrix(interactionHistory);
		
		return interactionHistory;
	}
	
	private int[][] generateAtransposeMatrix(int[][] interactionHistory) {
		// Create the transpose of the matrix.
		int[][] aTranspose = transposeMatrix(interactionHistory);
		
		System.out.println("Matrix A transposed");
		printMatrix(aTranspose);

		return aTranspose;
	}
	
	private double getGscore(double contingencyMatrix[][]) {
		ContigencyTableTotals contigencyTableTotals = generateContingencyTableTotals(contingencyMatrix);
		ExpectationTable expectationTable = generateExpectationTable(contigencyTableTotals);
		double gTest = calculateGtest(contingencyMatrix, expectationTable);
		return gTest;
	}
	
	private void surprisingRelationshipRecommendation(List<ScoredPair> surprisingRelationsips, int[] currentItemInteraction) {
		
		System.out.println("Surprising recommendations: ");
		
		// See if any of the current items being interacted with have a surprising relationship.
		for (int index = 0; index < currentItemInteraction.length; index++) {
			if (currentItemInteraction[index] == 0) continue;
			
			// Look through the surprising item relationships. Normally this would be done with tags on a search engine.
			for (ScoredPair sp : surprisingRelationsips) {
				if (sp.getI() == index) {
					System.out.println(sp.getJ());
				} else if (sp.getJ() == index) {
					System.out.println(sp.getI());
				}
			}
		}
	}
	
	private List<Interaction> getInteractions() {
		
		List<Interaction> interactionsRaw = new LinkedList<>();
		
		// Define interactions.
		interactionsRaw.add(new Interaction(0,7));
		interactionsRaw.add(new Interaction(0,3));
		interactionsRaw.add(new Interaction(3,3));
		interactionsRaw.add(new Interaction(0,1));
		interactionsRaw.add(new Interaction(0,4));
		interactionsRaw.add(new Interaction(3,2));
		interactionsRaw.add(new Interaction(2,3));
		interactionsRaw.add(new Interaction(3,0));
		interactionsRaw.add(new Interaction(1,2));
		interactionsRaw.add(new Interaction(2,2));
		interactionsRaw.add(new Interaction(1,5));
		interactionsRaw.add(new Interaction(2,4));
		interactionsRaw.add(new Interaction(1,7));
		interactionsRaw.add(new Interaction(2,1));
		interactionsRaw.add(new Interaction(3,6));
		
//		// Assume item 0 is popular and interacted with by all users.
//		for (int i = 0; i < NUM_USERS; i++) {
//			interactionsRaw.add(new Interaction(i,0));
//		}
	
//		// Assume there is a prolific buyer that buys one of each.
//		for (int i = 0; i < NUM_ITEMS; i++) {
//			interactionsRaw.add(new Interaction(0, i));
//		}
		
		System.out.println("Raw Interactions");
		printRawInteractions(interactionsRaw);

		return interactionsRaw;
	}
	
	private int[][] generateCooccurance(int[][] interactionHistory, int[][] aTranspose) {
				
		// Calculate co-occurance.
		int cooccurance[][] = matrixMultiply(aTranspose, interactionHistory);
		System.out.println("Co-occurance Matrix");
		printMatrix(cooccurance);
		
		// Zero out the diagonal so the item does not recommend itself.
		for (int i = 0; i < cooccurance.length; i++) {
			cooccurance[i][i] = 0;
		}
		
		return cooccurance;
	}
	

	
	private void testItemRecommendation(int cooccurance[][], int currentItemInteraction[]) {
		
		// Create recommendation weights.
		int[] recommendations = matrixMultiply(cooccurance, currentItemInteraction);
		System.out.println("Recommendation Weights - Co-occurance");
		printMatrix(recommendations);
		
		// Recommend using cosine distance.
		double[] cosineDistance = recommendUsingCosineMetric(cooccurance, currentItemInteraction);
		System.out.println("Recommendation Weights - Cosine");
		printMatrix(cosineDistance);

	}
	
	private void printRawInteractions(List<Interaction> interactionsRaw) {
		for (Interaction in : interactionsRaw) {
			System.out.println("user: " + in.getUser() + ", interaction: " + in.getInteraction());
		}
		System.out.println();
	}
	
	
	private int[][] transposeMatrix(int m[][]) {
		int x = m.length;
		int y = m[0].length;
		
		int t[][] = new int[y][x];
		
		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {
				t[j][i] = m[i][j];
			}
		}
		return t;
	}
	
	private void printMatrix(int m[][]) {
		System.out.print("        ");
		for (int i = 0; i < m[0].length; i++) {
			System.out.format("%3d",i);
			System.out.print("    ");
		}
		System.out.println();
		
		System.out.print("        ");
		for (int i = 0; i < m[0].length; i++) {
			System.out.print("---");
			System.out.print("    ");
		}
		System.out.println();
		
		
		for (int i = 0; i <  m.length; i++) {
			System.out.format("%3d --- ", i);
			int[] row = m[i];
			for (int j = 0; j < row.length; j++) {
				System.out.format("%3d", m[i][j]);
				System.out.print("    ");
			}
			System.out.println();
		}	
		System.out.println();
	}
	
	private void printMatrix(double m[][]) {
		System.out.print("             ");
		for (int i = 0; i < m[0].length; i++) {
			System.out.format("%3d",i);
			System.out.print("         ");
		}
		System.out.println();
		
		System.out.print("             ");
		for (int i = 0; i < m[0].length; i++) {
			System.out.print("---");
			System.out.print("         ");
		}
		System.out.println();
		
		
		for (int i = 0; i <  m.length; i++) {
			System.out.format("%3d --- ", i);
			double[] row = m[i];
			for (int j = 0; j < row.length; j++) {
				System.out.format("%6f", m[i][j]);
				System.out.print("    ");
			}
			System.out.println();
		}	
		System.out.println();
	}
	
	private void printMatrix(int m[]) {
		for (int i = 0; i < m.length; i++) {
			System.out.println(i + " - " + m[i]);
		}
		System.out.println();
	}
	
	private void printMatrix(double m[]) {
		for (int i = 0; i < m.length; i++) {
			System.out.println(i + " - " + m[i]);
		}
		System.out.println();
	}
	
	private int[][] matrixMultiply(int m[][], int n[][]) {
		
		int[][] t = new int[m.length][n[0].length];
		
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < n[0].length; j++) {
				int z = 0;
				for (int k = 0; k < m[0].length; k++) {
					z += m[i][k] * n[k][j];
				}
				t[i][j] = z;
			}
		}
		return t;
	}
	
	private int[] matrixMultiply(int m[][], int n[]) {
		
		int[] t = new int[m.length];
		
		for (int i = 0; i < m.length; i++) {
			int z = 0;
			for (int j = 0; j < n.length; j++) {
				z += m[i][j] * n[j];
			}
			t[i] = z;
		}
		
		return t;
	}
	
	private double[] recommendUsingCosineMetric(int m[][], int n[]) {
		double[] t = new double[m.length];

		for (int i = 0; i < t.length; i++) {
			t[i] = cosineMetric(m[i], n);
		}
		return t;
	}
	
	private double cosineMetric(int m[], int n[]) {
		double dotProd = 0;
		double mSum = 0;
		double nSum = 0;
		
		for (int i = 0; i < m.length; i++) {
			dotProd += m[i] * n[i];
			mSum += m[i] * m[i];
			nSum += n[i] * n[i];
		}
		
		return dotProd / (Math.sqrt(mSum) * Math.sqrt(nSum));
	}
	
	
	private double[][] computeGscoreMatrix(int c[][]) {
		
		double[][] gScores = new double[c.length][c.length];
		
		List<ScoredPair> scoredPairs = new LinkedList<>();
						
		// Generate the G-test score for half of the matrix.
		for (int i = 0; i < c.length; i++) {
			for (int j = i + 1; j < c.length; j++) {
				if (c[i][j] == 0) continue;
					
				double[][] contingencyMatrix =  generateContingencyTable(c, i, j);
				ContigencyTableTotals contigencyTableTotals = generateContingencyTableTotals(contingencyMatrix);
				ExpectationTable expectationTable = generateExpectationTable(contigencyTableTotals);
				double gTest = calculateGtest(contingencyMatrix, expectationTable);
				
				scoredPairs.add(new ScoredPair(i, j, gTest));
				
				gScores[i][j] = gTest;
				gScores[j][i] = gTest;
			}
		}	
		
		return gScores;
	}
	
	private List<ScoredPair> getScoredPairs(double[][] gscores) {
		List<ScoredPair> scoredPairs = new LinkedList<>();

		for (int i = 0; i < gscores.length; i++) {
			for (int j = i + 1; j < gscores.length; j++) {
				
				if (gscores[i][j] == 0) continue;									
				scoredPairs.add(new ScoredPair(i, j, gscores[i][j]));
			}
		}

		return scoredPairs;
	}

	private List<ScoredPair> generateSurprisingRelationships(int c[][]) {
		
		// Generate the gscore matrix. Take the unique pairs. This could be
		// done better in one step, but I want to re-use the routines later.
		double[][] gScores = computeGscoreMatrix(c);
		List<ScoredPair> scoredPairs = getScoredPairs(gScores);
		
		System.out.println("G-test distribution");
//		for (ScoredPair sp : scoredPairs) System.out.println(sp.getI() + " " + sp.getJ() + " " + sp.getScore());
		for (ScoredPair sp : scoredPairs) System.out.println(sp.getScore() + ",1");
		System.out.println();
		
		System.out.println("G scores for cooccurance matrix");
		printMatrix(gScores);
		
//		// Use K-means to cluster and separate anomalies.
//		double[] centroids = generateCentroids(scoredPairs);
//		System.out.println("Centroids:");
//		for (double centroid : centroids) System.out.println(centroid);
//		System.out.println();	
//		
//		// Find surprising relationships by finding pairs that are not in the bottom centroid.
//		List<ScoredPair> surprisingRelationships = findSurprisingRelationships(centroids, scoredPairs);
//		System.out.println("Surprising relationships:");
//		for (ScoredPair sp : surprisingRelationships) System.out.println("(" + sp.getI() + "," + sp.getJ() + ")  -  " + sp.getScore());
//		System.out.println();

		
		// Use a fixed threshold of 0.9 as the threshold.
		List<ScoredPair> surprisingRelationships = new LinkedList<>();
		for (ScoredPair sp : scoredPairs) {
			if (sp.getScore() > 0.90) {
				surprisingRelationships.add(sp);
			}
		}
		
		System.out.println("Surprising relationships:");
		for (ScoredPair sp : surprisingRelationships) System.out.println("(" + sp.getI() + "," + sp.getJ() + ")  -  " + sp.getScore());
		System.out.println();

		return surprisingRelationships;
	}
	
	
	private double[] generateCentroids(List<ScoredPair> scoredPair) {
		
		double[] centroids = new double[2];
		double[] distances = new double[centroids.length];
		int[] centroidMap = new int[scoredPair.size()];
		
		// Set the centroids to the min and max data values.
		double minVal = scoredPair.get(0).getScore();
		double maxVal = minVal;
		
		for (int i = 1; i < scoredPair.size(); i++) {
			double eg = scoredPair.get(i).getScore();
			minVal = Math.min(minVal, eg);
			maxVal = Math.max(maxVal, eg);
		}

		// Set initial centroids.
		centroids[0] = minVal;
		centroids[1] = maxVal;

		
		// Do 3 iterations of K-means.
		for (int z = 0; z < 3; z++) {
			// Map each cm to the closest centroid.
			for (int i = 0; i < scoredPair.size(); i++) {
	
				// Find distances to all centroids.
				double entropyGain = scoredPair.get(i).getScore();
				for (int j = 0; j < centroids.length; j++) {
					distances[j] = Math.abs(centroids[j] - entropyGain);
				}
				
				// From the distance list, find the closest centroid.
				int closestCentroid = 0;
				double minDist = distances[0];
				for (int j = 1; j < distances.length; j++) {
					double tempDist = distances[j];
					if (tempDist < minDist) {
						minDist = tempDist;
						closestCentroid = j;
					}
				}
				
				// For this cm, save the index to the closest centroid.
				centroidMap[i] = closestCentroid;
			}
			
			// Now that we know all of the closest centroids, recompute them.
			int[] pointCount = new int[centroids.length];
			double[] sum = new double[centroids.length];
			
			// Accumulate the points for each centroid.
			for (int j = 0; j < scoredPair.size(); j++) {
				int index = centroidMap[j];
				pointCount[index]++;
				sum[index] += scoredPair.get(j).getScore();
			}
	
			// Normalize each centroid.
			for (int i = 0; i < centroids.length; i++) {
				centroids[i] = sum[i] / pointCount[i];
			}
		
		}
		
		return centroids;
	}
	

	private List<ScoredPair> findSurprisingRelationships(double[] centroids,  List<ScoredPair> scoredPairs) {
		
		List<ScoredPair> surprise = new LinkedList<>();
		
		for (ScoredPair cm : scoredPairs) {
			

			// Find closest centroid.
			
			double d = Math.abs(centroids[0] - cm.getScore());
			int index = 0;
			for (int i = 1; i < centroids.length; i++) {
				double tempDist = Math.abs(centroids[i] - cm.getScore());
				if (tempDist < d) {
					d = tempDist;
					index = i;
				}
			}
			
			// If not bottom centroid, then select as surprising.
			if (index != 0) {
				surprise.add(cm);
			}
			
		}
		return surprise;
	}	
	
	
	private double[][] generateContingencyTable(int c[][], int i, int j) {		
		
		int _AandB = c[i][j];

		int _AandNotB = 0;
		int _NotAandB = 0;
		int _NotAandNotB = 0;
		
		for (int m = 0; m < c.length; m++) {
			if (m == j) continue;
			_AandNotB += c[i][m];
		}
		
		for (int m = 0; m < c.length; m++) {
			if (m == i) continue;
			_NotAandB += c[m][j];
		}
		
		for (int m = 0; m < c.length; m++) {
			for (int n = m + 1; n < c.length; n++) {
				if (m == i || m == j || n == i || n == j) continue;
				_NotAandNotB += c[m][n];
			}
		}

		
		double[][] cm = new double[2][2];
		cm[0][0] = _AandB;
		cm[0][1] = _AandNotB;
		cm[1][0] = _NotAandB;
		cm[1][1] = _NotAandNotB;
		
		return cm;
	}
	
	
	private ContigencyTableTotals generateContingencyTableTotals(double c[][]) {
		double rowTotals[] = new double[2];
		double colTotals[] = new double[2];
		double totalCount;
		
		totalCount = 0;

		// compute row/col totals.
		for (int i = 0; i < 2; i++) {
			double sumr = 0;
			double sumc = 0;
			for (int j = 0; j < 2; j++) {
				sumc += c[j][i];
				sumr += c[i][j];
				
				totalCount += c[i][j];
			}
			rowTotals[i] = sumr;
			colTotals[i] = sumc;
		}
		
		return new ContigencyTableTotals(c, rowTotals, colTotals, totalCount);
	}
	
	
	private ExpectationTable generateExpectationTable(ContigencyTableTotals ct) {
		double e[][] = new double[2][2];
		
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {

				e[i][j] = ct.getRowTotals()[i] * ct.getColTotals()[j] / ct.getTotalCount();
				
			}
		}
		
		return new ExpectationTable(e);
	}
	
	private double calculateGtest(double c[][], ExpectationTable et) {
		
		double s = 0;
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				if (c[i][j] == 0.0) continue;
				s += c[i][j] * Math.log(c[i][j] / et.getE()[i][j]);
			}
		}
		
		return Math.sqrt(2.0 * s);
	}

}




