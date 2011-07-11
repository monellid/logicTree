package processor;

import java.util.List;


import parser.LogicTreeNode;

public class LogicTreePath {
	
	private List<LogicTreeNode> path;
	private double pathWeight;
	
	public LogicTreePath(List<LogicTreeNode> path, double pathWeight){
		this.path = path;
		this.pathWeight = pathWeight;
	}

	public List<LogicTreeNode> getPath() {
		return path;
	}

	public double getPathWeight() {
		return pathWeight;
	}
	
	public void addNode(LogicTreeNode node){
		this.path.add(node);
		this.pathWeight = this.pathWeight * node.getUncertaintyWeight();
	}
	
    @Override
    public boolean equals(Object obj){
        if (!(obj instanceof LogicTreePath))
        {
            return false;
        }
        
        LogicTreePath other  = (LogicTreePath)obj;

        return path.equals(other.path)
        		&& pathWeight == other.pathWeight;
    }
    
    @Override
    public int hashCode() { 
        int hash = 1;
        hash = hash * 31 + new Double(pathWeight).hashCode();
        hash = hash * 31 
                    + (path == null ? 0 : path.hashCode());
        return hash;
      }
    
    @Override
    public String toString(){
    	String p = "";
    	for(int i=0;i<path.size();i++){
    		p = p + path.get(i).getUncertaintyModel()+" "+path.get(i).getUncertaintyType()+" "+path.get(i).getUncertaintyWeight()+"/";
    	}
    	return p;
    }

}
