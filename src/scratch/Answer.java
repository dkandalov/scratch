package scratch;


public class Answer {
	public final String explanation;
	public final boolean isYes;
	public final boolean isNo;

	public static Answer no(String explanation) {
		return new Answer(false, explanation);
	}

	public static Answer yes() {
		return new Answer(true, null);
	}

	private Answer(boolean isYes, String explanation) {
		this.isYes = isYes;
		this.isNo = !isYes;
		this.explanation = explanation;
	}

	@Override public String toString() {
		return isYes ? "Yes" : "No(" + explanation + ")";
	}

	@SuppressWarnings("RedundantIfStatement")
	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Answer answer = (Answer) o;

		if (isYes != answer.isYes) return false;
		if (explanation != null ? !explanation.equals(answer.explanation) : answer.explanation != null)
			return false;

		return true;
	}

	@Override public int hashCode() {
		int result = explanation != null ? explanation.hashCode() : 0;
		result = 31 * result + (isYes ? 1 : 0);
		return result;
	}
}
