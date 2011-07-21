package ac.simons.ssl;

import java.io.IOException;

/**
 * @author michael.simons, 2011-07-22
 */
public class Agent {
	private Agent() { 	
	}
	
	public static void premain(String agentArgs)  throws IOException {
		XTrustProvider.install();
	}
}