package at.ac.tuwien.complang.sbc11.mozart;

import java.io.Serializable;
import java.util.List;

import org.mozartspaces.core.Request;
import org.mozartspaces.core.RequestCallbackHandler;

import at.ac.tuwien.complang.sbc11.parts.Part;
import at.ac.tuwien.complang.sbc11.workers.AsyncAssembler;

public class TakePartsRequestCallbackHandler implements RequestCallbackHandler {
	
	private AsyncAssembler asyncAssembler;
	
	public TakePartsRequestCallbackHandler(AsyncAssembler asyncAssembler) {
		this.asyncAssembler = asyncAssembler;
	}

	@Override
	public void requestFailed(Request<?> request, Throwable error) {
		//System.out.println("---------------------");
		// TODO remove test output
		//System.out.println("request failed");
		//System.out.println("REQUEST: " + request);
		//System.out.println("error: " + error);
		//System.out.println("finished.");
		
		if(error.getClass().equals(org.mozartspaces.capi3.CountNotMetException.class)) {
			// TODO - ok because in know that only the graphicboard request can
			// throw a count not met exception
			// but how can i differ between a "real" error and an expected error
			// in general?
			asyncAssembler.setResponseReceivedForGraphicBoard(true);
		}
		else // there's really an error
		{
			asyncAssembler.setAsyncException(error);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void requestProcessed(Request<?> request, Serializable result) {
		System.out.println("======================");
		// TODO Auto-generated method stub
		System.out.println("request processed");
		System.out.println("REQUEST: " + request);
		System.out.println("Result.getClass(): " + result.getClass());
		System.out.println("Result: " + result);
		System.out.println("finished.");
		//assembler.setParts(result);
		
		asyncAssembler.setParts((List<Part>)result);
	}

}
