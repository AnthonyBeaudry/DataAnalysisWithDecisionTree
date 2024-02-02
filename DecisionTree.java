import java.io.Serializable;
import java.util.ArrayList;
import java.text.*;
import java.lang.Math;

public class DecisionTree implements Serializable {

	DTNode rootDTNode;
	int minSizeDatalist; //minimum number of datapoints that should be present in the dataset so as to initiate a split
	
	// Mention the serialVersionUID explicitly in order to avoid getting errors while deserializing.
	public static final long serialVersionUID = 343L;
	
	public DecisionTree(ArrayList<Datum> datalist , int min) {
		minSizeDatalist = min;
		rootDTNode = (new DTNode()).fillDTNode(datalist);
	}

	class DTNode implements Serializable{
		//Mention the serialVersionUID explicitly in order to avoid getting errors while deserializing.
		public static final long serialVersionUID = 438L;
		boolean leaf;
		int label = -1;      // only defined if node is a leaf
		int attribute; // only defined if node is not a leaf
		double threshold;  // only defined if node is not a leaf

		DTNode left, right; //the left and right child of a particular node. (null if leaf)

		DTNode() {
			leaf = true;
			threshold = Double.MAX_VALUE;
		}

		
		// this method takes in a datalist (ArrayList of type datum). It returns the calling DTNode object 
		// as the root of a decision tree trained using the datapoints present in the datalist variable and minSizeDatalist.
		// Also, the left and right child of the node correspond to "less than" and "greater than or equal to" threshold
		DTNode fillDTNode(ArrayList<Datum> datalist) {
			DTNode newNode = new DTNode();
			if(datalist.size() < minSizeDatalist) { // Basic Step to end recursive Calls
				newNode.label = findMajority(datalist);
				newNode.left = null;
				newNode.right = null;
				return newNode;
			}
			// Checks if all Nodes are same label to decide to return or not (other base case)
			boolean allSameLabel = true;
			int testLabel = datalist.get(0).y;
			for(Datum d: datalist) {
				if(d.y != testLabel) {
					allSameLabel = false;
					break;
				}
			}
			if(allSameLabel) {
				newNode.label = testLabel;
				newNode.left = null;
				newNode.right = null;
				return newNode;
			}
			
			// Core of computing the decision tree based on the entropy, and values of the dataset
			double datalistEntropy = calcEntropy(datalist);
			double bestEntropy = Double.MAX_VALUE;
			int bestAttribute = -1;
			double bestThreshold = -1;
			int totalAttributes = datalist.get(0).x.length;
			
			for(int i = 0; i < totalAttributes; i++) {
				for(int j = 0; j < datalist.size(); j++) { 
					ArrayList<Datum> leftDump = new ArrayList<Datum>();
					ArrayList<Datum> rightDump = new ArrayList<Datum>();
					double dumpThreshold = datalist.get(j).x[i];
					for(int k = 0; k < datalist.size(); k++) {
						if(datalist.get(k).x[i] < dumpThreshold)
							leftDump.add(datalist.get(k));
						else
							rightDump.add(datalist.get(k));
					}
					double currentEntropy = (leftDump.size()*calcEntropy(leftDump))/datalist.size()
							+ (rightDump.size()*calcEntropy(rightDump))/datalist.size();
					if(currentEntropy < bestEntropy) {
						bestEntropy = currentEntropy;
						bestAttribute = i;
						bestThreshold = dumpThreshold;
					}
				}
			}
			
			// Minimum differential set point to continue with decision tree filling
			if((datalistEntropy - bestEntropy) < 0.00001) {
				System.out.println(datalistEntropy + " " + bestEntropy);
				newNode.label = findMajority(datalist);
				newNode.left = null;
				newNode.right = null;
				return newNode;
			}
			
			// Recursive calls to build decision tree
			ArrayList<Datum> left = new ArrayList<Datum>();
			ArrayList<Datum> right = new ArrayList<Datum>();
			for(Datum d: datalist) {
				if(d.x[bestAttribute] < bestThreshold)
					left.add(d);
				else
					right.add(d);
			}
			newNode.attribute = bestAttribute;
			newNode.threshold = bestThreshold;
			newNode.leaf = false;
			newNode.left = fillDTNode(left);
			newNode.right = fillDTNode(right);
			
			return newNode;
		}



		// This is a helper method. Given a datalist, this method returns the label that has the most
		// occurrences. In case of a tie it returns the label with the smallest value (numerically) involved in the tie.
		int findMajority(ArrayList<Datum> datalist) {
			
			int [] votes = new int[2];

			//loop through the data and count the occurrences of datapoints of each label
			for (Datum data : datalist)
			{
				votes[data.y]+=1;
			}
			
			if (votes[0] >= votes[1])
				return 0;
			else
				return 1;
		}




		// This method takes in a datapoint (excluding the label) in the form of an array of type double (Datum.x) and
		// returns its corresponding label, as determined by the decision tree
		int classifyAtNode(double[] xQuery) {
			if(this.leaf) return label;
			
			if(xQuery[attribute] >= threshold)
				return this.right.classifyAtNode(xQuery);
			else
				return this.left.classifyAtNode(xQuery);
		}


		//given another DTNode object, this method checks if the tree rooted at the calling DTNode is equal to the tree rooted
		//at DTNode object passed as the parameter
		public boolean equals(Object dt2)
		{
			if(dt2 == null || dt2.getClass() != getClass()) return false;
			
			if(this.leaf && ((DTNode)(dt2)).leaf)
				return this.label == ((DTNode)(dt2)).label;
			
			else if(this.left != null && ((DTNode)(dt2)).left != null) {
				
				if(this.right != null && ((DTNode)(dt2)).right != null)
					return this.threshold == ((DTNode)(dt2)).threshold && this.attribute == ((DTNode)(dt2)).attribute
					&& this.left.equals(((DTNode)(dt2)).left) && this.right.equals(((DTNode)(dt2)).right);
				
				else if(this.right == null && ((DTNode)(dt2)).right == null)
					return this.threshold == ((DTNode)(dt2)).threshold && this.attribute == ((DTNode)(dt2)).attribute
					&& this.left.equals(((DTNode)(dt2)).left);
				
				else
					return false;
			}
			else if(this.right != null && ((DTNode)(dt2)).right != null) {
				
				if(this.left == null && ((DTNode)(dt2)).left == null)
					return this.threshold == ((DTNode)(dt2)).threshold && this.attribute == ((DTNode)(dt2)).attribute
					&& this.right.equals(((DTNode)(dt2)).right);
				
				else
					return false;
			}
			else
				return false;
		}
	}



	//Given a dataset, this returns the entropy of the dataset
	double calcEntropy(ArrayList<Datum> datalist) {
		double entropy = 0;
		double px = 0;
		float [] counter= new float[2];
		if (datalist.size()==0)
			return 0;
		double num0 = 0.00000001,num1 = 0.000000001;

		//calculates the number of points belonging to each of the labels
		for (Datum d : datalist)
		{
			counter[d.y]+=1;
		}
		//calculates the entropy using the formula specified in the document
		for (int i = 0 ; i< counter.length ; i++)
		{
			if (counter[i]>0)
			{
				px = counter[i]/datalist.size();
				entropy -= (px*Math.log(px)/Math.log(2));
			}
		}

		return entropy;
	}


	// given a datapoint (without the label) calls the DTNode.classifyAtNode() on the rootnode of the calling DecisionTree object
	int classify(double[] xQuery ) {
		return this.rootDTNode.classifyAtNode( xQuery );
	}

	// Checks the performance of a DecisionTree on a dataset
	String checkPerformance( ArrayList<Datum> datalist) {
		DecimalFormat df = new DecimalFormat("0.000");
		float total = datalist.size();
		float count = 0;

		for (int s = 0 ; s < datalist.size() ; s++) {
			double[] x = datalist.get(s).x;
			int result = datalist.get(s).y;
			if (classify(x) != result) {
				count = count + 1;
			}
		}

		return df.format((count/total));
	}


	//Given two DecisionTree objects, this method checks if both the trees are equal by
	//calling onto the DTNode.equals() method
	public static boolean equals(DecisionTree dt1,  DecisionTree dt2)
	{
		boolean flag = true;
		flag = dt1.rootDTNode.equals(dt2.rootDTNode);
		return flag;
	}

}
