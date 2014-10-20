package com.threecixty.gfsample.happiness;


public class HappinessCSResponder extends CrowdSourcedReceiver<Happiness, HappinessRequest> {

	public HappinessCSResponder(Class<Happiness> responseClazz, Class<HappinessRequest> requestClazz, CrowdSourcedReceiverCallback<Happiness, HappinessRequest> handler) {
		super(responseClazz, requestClazz, handler);
	}

}
