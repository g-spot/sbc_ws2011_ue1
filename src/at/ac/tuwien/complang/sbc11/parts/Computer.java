package at.ac.tuwien.complang.sbc11.parts;

import java.util.List;

import at.ac.tuwien.complang.sbc11.workers.Worker;

public class Computer {
	// general information
	private boolean isDefect;
	private boolean isComplete;
	
	// parts
	private CPU cpu = null;
	private Mainboard mainboard = null;
	private List<RAM> ramModules = null;
	private GraphicBoard graphicBoard = null;
	
	// who did the work
	private List<Worker> workers;
}
