package entity;

public class Search {
	private String interest;
	private boolean interestDic;
	private String number;
	private String gender;
	private boolean genderDic;
	private String age;
	private boolean ageDic;
	private boolean geoDic;
	private String latitude;
	private String longitude;
	private String radius;
	private boolean boost;
	
	public String getInterest() {
		return interest;
	}
	public void setInterest(String interest) {
		this.interest = interest;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getAge() {
		return age;
	}
	public void setAge(String age) {
		this.age = age;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	public String getRadius() {
		return radius;
	}
	public void setRadius(String radius) {
		this.radius = radius;
	}
	public boolean isBoost() {
		return boost;
	}
	public void setBoost(boolean boost) {
		this.boost = boost;
	}
	public boolean isInterestDic() {
		return interestDic;
	}
	public void setInterestDic(boolean interestDic) {
		this.interestDic = interestDic;
	}
	public boolean isGenderDic() {
		return genderDic;
	}
	public void setGenderDic(boolean genderDic) {
		this.genderDic = genderDic;
	}
	public boolean isAgeDic() {
		return ageDic;
	}
	public void setAgeDic(boolean ageDic) {
		this.ageDic = ageDic;
	}
	public boolean isGeoDic() {
		return geoDic;
	}
	public void setGeoDic(boolean geoDic) {
		this.geoDic = geoDic;
	}
	
}