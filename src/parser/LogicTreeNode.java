package parser;

public class LogicTreeNode {

	private String uncertaintyType;
	private String uncertaintyModel;
	private Double uncertaintyWeight;

	public LogicTreeNode() {
		this.uncertaintyType = "";
		this.uncertaintyModel = "";
		this.uncertaintyWeight = 1.0;
	}

	public LogicTreeNode(String uncertaintyType, String uncertaintyModel,
			double uncertaintyWeight) {
		this.uncertaintyType = uncertaintyType;
		this.uncertaintyModel = uncertaintyModel;
		this.uncertaintyWeight = uncertaintyWeight;
	}

	public String getUncertaintyModel() {
		return uncertaintyModel;
	}

	public double getUncertaintyWeight() {
		return uncertaintyWeight;
	}

	public String getUncertaintyType() {
		return uncertaintyType;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LogicTreeNode)) {
			return false;
		}

		LogicTreeNode other = (LogicTreeNode) obj;

		return uncertaintyType.equals(other.uncertaintyType)
				&& uncertaintyModel.equals(other.uncertaintyModel)
				&& uncertaintyWeight.equals(other.uncertaintyWeight);
	}

	@Override
	public int hashCode() {
		int hash = 1;
		hash = hash * 31 + uncertaintyWeight.hashCode();
		hash = hash * 31
				+ (uncertaintyModel == null ? 0 : uncertaintyModel.hashCode());
		hash = hash * 31
				+ (uncertaintyType == null ? 0 : uncertaintyType.hashCode());
		return hash;
	}

}
