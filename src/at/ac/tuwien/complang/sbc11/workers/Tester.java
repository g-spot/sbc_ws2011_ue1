package at.ac.tuwien.complang.sbc11.workers;

public class Tester extends Worker {
	/**
	 * 
	 */
	private static final long serialVersionUID = -89701659089204368L;

	public enum TestType { COMPLETENESS, CORRECTNESS };
	public enum TestState { NOT_TESTED, FAILED, PASSED };
	
	private TestType testType;

	public TestType getTestType() {
		return testType;
	}

	public void setTestType(TestType testType) {
		this.testType = testType;
	}
}
