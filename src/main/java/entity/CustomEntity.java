package entity;

public class CustomEntity implements Comparable<CustomEntity>{
	double confidenceScore;
	String matchedText;
	
	public CustomEntity(double confidenceScore, String matchedText) {
		super();
		this.confidenceScore = confidenceScore;
		this.matchedText = matchedText;
	}
	public double getConfidenceScore() {
		return confidenceScore;
	}
	public void setConfidenceScore(double confidenceScore) {
		this.confidenceScore = confidenceScore;
	}
	public String getMatchedText() {
		return matchedText;
	}
	public void setMatchedText(String matchedText) {
		this.matchedText = matchedText;
	}
	public int compareTo(CustomEntity compareEntity) {
		if (compareEntity.getConfidenceScore() - this.getConfidenceScore() == 0)
			return 0;
		return (compareEntity.getConfidenceScore() - this.getConfidenceScore() < 0 ? -1 : 1); //decrescent
	}
	public String toString(){
		return "Text: " + this.matchedText + " Confidence: " + this.confidenceScore;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((matchedText == null) ? 0 : matchedText.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CustomEntity other = (CustomEntity) obj;
		if (matchedText == null) {
			if (other.matchedText != null)
				return false;
		} else if (!matchedText.equals(other.matchedText))
			return false;
		return true;
	}
}
