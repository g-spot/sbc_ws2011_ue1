package at.ac.tuwien.complang.sbc11.workers;

public class Tester extends Worker {
	public enum TestType { COMPLETENESS, CORRECTNESS };
	
	private TestType testType;

	public TestType getTestType() {
		return testType;
	}

	public void setTestType(TestType testType) {
		this.testType = testType;
	}
}
