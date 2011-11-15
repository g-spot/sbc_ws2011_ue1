package at.ac.tuwien.complang.sbc11.workers;

import java.util.logging.Logger;

import at.ac.tuwien.complang.sbc11.factory.Factory;
import at.ac.tuwien.complang.sbc11.factory.MozartFactory;
import at.ac.tuwien.complang.sbc11.parts.Mainboard;
import at.ac.tuwien.complang.sbc11.parts.Part;

public class Producer extends Worker {
	private long productionLimit;
	private long producedSoFar;
	private double errorRate;
	private Class<?> partClass;
	private Factory factory;
	private Logger logger;
	
	public Producer(long productionLimit, double errorRate, Class<?> partClass, Factory factory) {
		this.productionLimit = productionLimit;
		this.errorRate = errorRate;
		this.partClass = partClass;
		this.factory = factory;
		this.producedSoFar = 0;
		
		logger = Logger.getLogger("at.ac.tuwien.complang.sbc11.workers.Producer");
	}
	
	public void produce() {
		logger.info("Starting Production...");
		while(producedSoFar < productionLimit) {
			// random value between 1000 and 3000 milliseconds
			long duration = (long)((Math.random() * 10000)%2000 + 1000);
			try {
				logger.info("Producing part " + (producedSoFar + 1) + " of " + productionLimit + "...");
				//Thread.sleep(duration);
				Part part = (Part)partClass.newInstance();
				logger.info("Done. Production took " + duration + " milliseconds.");
				
				// TODO how to get a system wide identifier for a part?
				// rmi: server has to provide a function getNextId()
				// mozart: take id from a container?
				part.setId(factory.getNextPartId());
				
				// part is defect with probabilty errorRate
				// Math.random() returns a double between 0.0 an 1.0
				// if Math.random() returns a number smaller or equal the
				// error rate, it is defect
				if(Math.random() <= errorRate)
					part.setDefect(true);
				else
					part.setDefect(false);
				
				factory.addPart(part);
			/*} catch (InterruptedException e) {
				e.printStackTrace();*/
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			producedSoFar++;
		}
		logger.info("Production finished.");
		
		for(Part p:factory.getAvailableParts()) {
			logger.info(p.toString());
		}
	}
	
	public static void main(String args[]) {
		// produce 10 Mainboards, error rate is 20%, use the MozartFactory
		Producer producer = new Producer((long)10, 0.2, Mainboard.class, new MozartFactory());
		producer.produce();
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
