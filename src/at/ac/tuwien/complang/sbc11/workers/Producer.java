package at.ac.tuwien.complang.sbc11.workers;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import at.ac.tuwien.complang.sbc11.parts.Mainboard;
import at.ac.tuwien.complang.sbc11.parts.Part;

public class Producer extends Worker {
	private long productionLimit;
	private long producedSoFar;
	private double errorRate;
	private Class<?> partClass;
	
	// for testing purpose only:
	private List<Part> partsProduced;
	private Logger logger;
	
	public Producer(long productionLimit, double errorRate, Class<?> partClass) {
		this.productionLimit = productionLimit;
		this.errorRate = errorRate;
		this.partClass = partClass;
		this.producedSoFar = 0;
		
		partsProduced = new ArrayList<Part>();
		logger = Logger.getLogger("at.ac.tuwien.complang.sbc11.workers.Producer");
	}
	
	public void produce() {
		logger.info("Starting Production...");
		while(producedSoFar < productionLimit) {
			// random value between 1000 and 3000 milliseconds
			long duration = (long)((Math.random() * 10000)%2000 + 1000);
			try {
				logger.info("Producing part " + (producedSoFar + 1) + " of " + productionLimit + "...");
				Thread.sleep(duration);
				Part part = (Part)partClass.newInstance();
				logger.info("Done. Production took " + duration + " milliseconds.");
				
				// TODO how to get a system wide identifier for a part?
				// rmi: server has to provide a function getNextId()
				// mozart: take id from a container?
				//part.setId(id)
				
				// part is defect with probabilty errorRate
				// Math.random() returns a double between 0.0 an 1.0
				// if Math.random() returns a number smaller or equal the
				// error rate, it is defect
				if(Math.random() <= errorRate)
					part.setDefect(true);
				else
					part.setDefect(false);
				
				partsProduced.add(part);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			producedSoFar++;
		}
		logger.info("Production finished.");
		printProducedParts();
	}
	
	private void printProducedParts() {
		for(Part p:partsProduced) {
			System.out.println(p);
		}
	}
	
	public static void main(String args[]) {
		new Producer((long)10, 0.2, Mainboard.class).produce();
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
