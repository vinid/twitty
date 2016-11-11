package entity;

import java.util.ArrayList;

public class UserModel extends UserFields {
	private double score;
	public ArrayList<String> fragments = new ArrayList<>();
	
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	
	public ArrayList<String> getFragments() {
		return fragments;
	}
	public void setFragments(ArrayList<String> fragments) {
		this.fragments = fragments;
	}
	
	
}
