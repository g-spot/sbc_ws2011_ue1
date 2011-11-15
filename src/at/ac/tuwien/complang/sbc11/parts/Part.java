package at.ac.tuwien.complang.sbc11.parts;

import at.ac.tuwien.complang.sbc11.workers.Producer;

public class Part {
	private long id;
	private Producer producer;
	private boolean isDefect;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public Producer getProducer() {
		return producer;
	}
	public void setProducer(Producer producer) {
		this.producer = producer;
	}
	public boolean isDefect() {
		return isDefect;
	}
	public void setDefect(boolean isDefect) {
		this.isDefect = isDefect;
	}
	
	@Override
	public String toString() {
		return "Part TYPE=" + this.getClass().getSimpleName() + ", ID=" + id + ", DEFECT=" + isDefect;
	}
}
