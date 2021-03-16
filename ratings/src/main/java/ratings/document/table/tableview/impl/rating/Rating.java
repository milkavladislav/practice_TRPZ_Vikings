package ratings.document.table.tableview.impl.rating;

import java.io.Serializable;

public class Rating implements Serializable {

	private static final long serialVersionUID = 5929103364572670728L;

	/**
	 * Номер
	 */
	private int number;

	/**
	 * ФИО
	 */
	private String name;

	/**
	 * Група
	 */
	private String group;

	/**
	 * Средний бал
	 */
	private float averageScore;

	/**
	 * Спортивна діяльність, %
	 */
	private String sportActivityPercent;

	/**
	 * Творча діяльність, %
	 */
	private String creativeActivityPercent;

	/**
	 * Громадянська діяльність, %
	 */
	private String civilActivityPercent;

	/**
	 * Наукова діяльність, %
	 */
	private String scientificActivityPercent;

	/**
	 * Всього, додано балів -> процент
	 */
	private String percent;

	/**
	 * Всього, додано балів -> бал
	 */
	private String score;

	/**
	 * Консолідований бал
	 */
	private float consolidatedScore;

	/**
	 * Соц. стипендия
	 */
	private boolean socialScholarship;

	public void setField(String field, String newValue) {
		switch (field) {
			case "name":
				name = newValue;
				break;
			case "group":
				group = newValue;
				break;
			case "sportActivityPercent":
				sportActivityPercent = newValue;
				break;
			case "creativeActivityPercent":
				creativeActivityPercent = newValue;
				break;
			case "civilActivityPercent":
				civilActivityPercent = newValue;
				break;
			case "scientificActivityPercent":
				scientificActivityPercent = newValue;
				break;
			default:
				throw new IllegalArgumentException("Field " + field + "[" + newValue + "] is unknown!");
		}
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public float getAverageScore() {
		return averageScore;
	}

	public void setAverageScore(float averageScore) {
		this.averageScore = averageScore;
	}

	public String getSportActivityPercent() {
		return sportActivityPercent;
	}

	public void setSportActivityPercent(String sportActivityPercent) {
		this.sportActivityPercent = sportActivityPercent;
	}

	public String getCreativeActivityPercent() {
		return creativeActivityPercent;
	}

	public void setCreativeActivityPercent(String creativeActivityPercent) {
		this.creativeActivityPercent = creativeActivityPercent;
	}

	public String getCivilActivityPercent() {
		return civilActivityPercent;
	}

	public void setCivilActivityPercent(String civilActivityPercent) {
		this.civilActivityPercent = civilActivityPercent;
	}

	public String getScientificActivityPercent() {
		return scientificActivityPercent;
	}

	public void setScientificActivityPercent(String scientificActivityPercent) {
		this.scientificActivityPercent = scientificActivityPercent;
	}

	public String getPercent() {
		return percent;
	}

	public void setPercent(String percent) {
		this.percent = percent;
	}

	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}

	public float getConsolidatedScore() {
		return consolidatedScore;
	}

	public void setConsolidatedScore(float consolidatedScore) {
		this.consolidatedScore = consolidatedScore;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isSocialScholarship() {
		return socialScholarship;
	}

	public void setSocialScholarship(boolean socialScholarship) {
		this.socialScholarship = socialScholarship;
	}
}
