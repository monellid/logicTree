import java.util.List;

import org.opensha.sha.earthquake.rupForecastImpl.GEM1.SourceData.GEMSourceData;

import processor.LogicTreePath;

/**
 * Class representing source model data to be used as input for PSHA.
 * @author damianomonelli
 *
 */
public class SourceModelData {
	
	private List<GEMSourceData> srcList;
	private double modelWeight;
	
	public SourceModelData(LogicTreePath path){
		srcList = getSourceModelFromPath(path);
		modelWeight = path.getPathWeight();
	}
	
	private List<GEMSourceData> getSourceModelFromPath(LogicTreePath path){
		List<GEMSourceData> srcList = null;
		return srcList;
	}

}
