package at.ac.tuwien.complang.sbc11.parts;

import java.io.Serializable;

import org.mozartspaces.capi3.Index;
import org.mozartspaces.capi3.Queryable;

import at.ac.tuwien.complang.sbc11.workers.Producer;

@Queryable
public class Part implements Serializable {

	private static final long serialVersionUID = 7132593689392643018L;
	
	@Index
	protected long id;
	protected Producer producer;
	protected boolean isDefect;
	
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
		String result = this.getClass().getSimpleName() + "[" + id + "], DEFECT=" + isDefect;
		if(producer != null)
			result +=", PRODUCER[" + producer.getId() + "]";
		return result;
	}
}
