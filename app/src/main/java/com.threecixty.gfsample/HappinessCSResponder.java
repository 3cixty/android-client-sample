package com.ambientic.goflow.happiness;

import com.ambientic.crowdsource.core.CrowdSourcedReceiver;
import com.ambientic.crowdsource.core.CrowdSourcedReceiverCallback;
import com.ambientic.crowdsource.happiness.Happiness;
import com.ambientic.crowdsource.happiness.HappinessRequest;

public class HappinessCSResponder extends CrowdSourcedReceiver<Happiness, HappinessRequest> {

	public HappinessCSResponder(Class<Happiness> responseClazz, Class<HappinessRequest> requestClazz, CrowdSourcedReceiverCallback<Happiness, HappinessRequest> handler) {
		super(responseClazz, requestClazz, handler);
	}

}
