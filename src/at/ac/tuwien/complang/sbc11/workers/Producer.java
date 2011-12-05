package at.ac.tuwien.complang.sbc11.workers;

import java.io.Serializable;
import java.util.logging.Logger;

import at.ac.tuwien.complang.sbc11.factory.SharedWorkspace;
import at.ac.tuwien.complang.sbc11.factory.exception.SharedWorkspaceException;
import at.ac.tuwien.complang.sbc11.parts.Part;

public class Producer extends Worker implements Runnable, Serializable {
	private static final long serialVersionUID = -6198419403429038567L;
	
	private long productionLimit;
	private long producedSoFar;
	private double errorRate;
	private Class<?> partClass;
	transient private Logger logger;
	
	public Producer(long productionLimit, double errorRate, Class<?> partClass, SharedWorkspace sharedWorkspace) {
		this.productionLimit = productionLimit;
		this.errorRate = errorRate;
		this.partClass = partClass;
		this.sharedWorkspace = sharedWorkspace;
		this.producedSoFar = 0;
		
		logger = Logger.getLogger("at.ac.tuwien.complang.sbc11.workers.Producer");
	}
	
	@Override
	public void run() {
		this.produce();
	}
	
	public void produce() {
		logger.info("Starting Production...");
		while(producedSoFar < productionLimit) {
			// random value between 1000 and 3000 milliseconds
			long duration = (long)((Math.random() * 10000)%2000 + 1000);
			try {
				logger.info("Producing part " + (producedSoFar + 1) + " of " + productionLimit + "...");
				Thread.sleep(duration);
				//Thread.sleep(0);
				Part part = (Part)partClass.newInstance();
				logger.info("Done. Production took " + duration + " milliseconds.");

				part.setId(sharedWorkspace.getNextPartId());
				part.setProducer(this);
				
				// part is defect with probabilty errorRate
				// Math.random() returns a double between 0.0 an 1.0
				// if Math.random() returns a number smaller or equal the
				// error rate, it is defect
				if(Math.random() <= errorRate)
					part.setDefect(true);
				else
					part.setDefect(false);
				
				sharedWorkspace.addPart(part);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (SharedWorkspaceException e) {
				e.printStackTrace();
			}
			producedSoFar++;
		}
		logger.info("Production finished.");
	}
	
	// getters and setters
	public long getProductionLimit() {
		return productionLimit;
	}
	public void setProductionLimit(long productionLimit) {
		this.productionLimit = productionLimit;
	}
	public double getErrorRate() {
		return errorRate;
	}
	public void setErrorRate(double errorRate) {
		this.errorRate = errorRate;
	}
}
