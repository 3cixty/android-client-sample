package com.threecixty.gfsample.happiness;

import android.app.Activity;
import android.widget.Toast;

import com.ambientic.crowdsource.core.CrowdSourcedPushCallback;
import com.ambientic.crowdsource.core.CrowdSourcedReceiverCallback;
import com.ambientic.crowdsource.happiness.Happiness;
import com.ambientic.crowdsource.happiness.HappinessRequest;
import com.ambientic.crowdsource.happiness.Station;

public class HappinessCSInfoResponderCallback implements CrowdSourcedReceiverCallback<Happiness, HappinessRequest> {

	// XXX PGR
	Happiness localHqppiness = null;
	Station localStation = null;
	Activity activity;

	public void setValues(Happiness localHqppiness, Station localStation, Activity activity) {
		this.localHqppiness = localHqppiness;
		this.localStation = localStation;
		this.activity = activity;
	}

	@Override
	public void receiveRequest(final HappinessRequest request, final boolean self) {
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(activity, "Got request " + request.getHotspotId() + " isSelf=" + self, Toast.LENGTH_LONG).show();
				}
			});
		}
	}

	// PushTask callback when sending response
	//
	public class HappinessCSPushCallback implements CrowdSourcedPushCallback<Happiness> {

		@Override
		public void failedPush(long requestId) {
			System.out.println("HappinessCSPushCallback::Unable to send response");
		}

		@Override
		public void successPush(long requestId) {
			System.out.println("HappinessCSPushCallback::Response sent");
		}

		@Override
		public void delayedPush(long requestId) {
			System.out.println("HappinessCSPushCallback::Response delayed");

		}

	}
}
