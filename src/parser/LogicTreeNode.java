package parser;

public class LogicTreeNode {

	private String branchID;
	private String uncertaintyType;
	private String uncertaintyModel;
	private Double uncertaintyWeight;
	private String applyToSources;
	private String applyToSourceType;
	private String applyToTectonicRegionType;

	public LogicTreeNode() {
		this.branchID = "";
		this.uncertaintyType = "";
		this.uncertaintyModel = "";
		this.uncertaintyWeight = 1.0;
		this.applyToSources = "";
		this.applyToSourceType = "";
		this.applyToTectonicRegionType = "";
	}

	public LogicTreeNode(String branchID, String uncertaintyType,
			String uncertaintyModel, double uncertaintyWeight,
			String applyToSources, String applyToSourceType,
			String applyToTectonicRegionType) {
		this.branchID = branchID;
		this.uncertaintyType = uncertaintyType;
		this.uncertaintyModel = uncertaintyModel;
		this.uncertaintyWeight = uncertaintyWeight;
		this.applyToSources = applyToSources;
		this.applyToSourceType = applyToSourceType;
		this.applyToTectonicRegionType = applyToTectonicRegionType;
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

	public String getApplyToSources() {
		return applyToSources;
	}

	public String getApplyToSourceType() {
		return applyToSourceType;
	}

	public String getApplyToTectonicRegionType() {
		return applyToTectonicRegionType;
	}

	public String getBranchID() {
		return branchID;
	}

	@Override
	public String toString() {
		return "branchID: " + branchID + ", uncertainity type: "
				+ uncertaintyType + ", uncertainty model: " + uncertaintyModel
				+ ", uncertainty weight: " + uncertaintyWeight
				+ ", applyToSources: " + applyToSources
				+ ", applyToSourceType: " + applyToSourceType;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LogicTreeNode)) {
			return false;
		}

		LogicTreeNode other = (LogicTreeNode) obj;

		return branchID.equals(other.branchID)
				&& uncertaintyType.equals(other.uncertaintyType)
				&& uncertaintyModel.equals(other.uncertaintyModel)
				&& uncertaintyWeight.equals(other.uncertaintyWeight)
				&& applyToSources.equals(other.applyToSources)
				&& applyToSourceType.equals(other.applyToSourceType)
				&& applyToTectonicRegionType
						.equals(other.applyToTectonicRegionType);
	}

	@Override
	public int hashCode() {
		int hash = 1;
		hash = hash * 31 + uncertaintyWeight.hashCode();
		hash = hash * 31
				+ (uncertaintyModel == null ? 0 : uncertaintyModel.hashCode());
		hash = hash * 31
				+ (uncertaintyType == null ? 0 : uncertaintyType.hashCode());
		hash = hash * 31
				+ (applyToSources == null ? 0 : applyToSources.hashCode());
		hash = hash
				* 31
				+ (applyToSourceType == null ? 0 : applyToSourceType.hashCode());
		hash = hash
				* 31
				+ (applyToTectonicRegionType == null ? 0
						: applyToTectonicRegionType.hashCode());
		hash = hash * 31 + (branchID == null ? 0 : branchID.hashCode());
		return hash;
	}

}
