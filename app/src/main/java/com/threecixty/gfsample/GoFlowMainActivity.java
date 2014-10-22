/**
 * 
 */
package com.threecixty.gfsample;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.ambientic.crowdsource.core.CrowdSourcedCommonPullTask;
import com.ambientic.crowdsource.core.CrowdSourcedPullCallback;
import com.ambientic.crowdsource.core.CrowdSourcedPushCallback;
import com.ambientic.crowdsource.core.CrowdSourcedPushTask;
import com.ambientic.crowdsource.core.CrowdSourcedServerCallback;
import com.ambientic.crowdsource.core.CrowdsourceManagerFactory;
import com.ambientic.crowdsource.core.CrowdsourcingCallback;
import com.ambientic.crowdsource.core.Incentives;
import com.ambientic.crowdsource.happiness.Happiness;
import com.ambientic.crowdsource.happiness.HappinessLevel;
import com.ambientic.crowdsource.happiness.HappinessRequest;
import com.ambientic.crowdsource.happiness.Station;
import com.ambientic.incentive.core.IncentiveCallback;
import com.ambientic.incentive.core.IncentiveItem;
import com.threecixty.gfsample.happiness.HappinessCSInfoResponderCallback;
import com.threecixty.gfsample.happiness.HappinessCSResponder;
import com.threecixty.oauthsample.GoflowUtils;
import com.threecixty.oauthsample.R;

import java.io.IOException;
import java.util.List;


/**
 * This activity manages a simple lsit of buttons that exercise the GoFlo API.
 * 
 * - Subscribe buttons turn on/off the subscribtion to crowdsourcing requests
 * for a specific station
 * 
 * - Ask buttons send crowdsourcing requests at a specified station
 * 
 * - Provide buttons send crowdsourced information for a specific station to any
 * peer that request such information
 * 
 * - Query servicer buttons send a query to the crowdsoourcing server for the
 * latest information received.
 * 
 * @author pgr
 *
 */
public class GoFlowMainActivity extends Activity implements CrowdSourcedPullCallback<Happiness>, CrowdSourcedServerCallback<Happiness> {

	CrowdsourcingMonitor monitor = null;

	Button buttonRcv1;
	Button buttonRcv2;
	Button buttonSnd1;
	Button buttonSnd2;
	Button buttonSrv1;
	Button buttonSrv2;
	Button buttonAsk1;
	Button buttonAsk2;

	TextView myText;

	// Station station1 = new Station("station1", "stationDescr 1", 0.0, 0.0);
	Station station1 = new Station("recs:winery:-1", "Duomo", 0.0, 0.0);
	Station station2 = new Station("station2", "stationDescr 2", 0.0, 0.0);

	HappinessLevel hlevel = new HappinessLevel();

	com.threecixty.gfsample.happiness.HappinessCSResponder responder1 = null;
	com.threecixty.gfsample.happiness.HappinessCSResponder responder2 = null;

	HappinessCSInfoRequester requester1 = null;
	HappinessCSInfoRequester requester2 = null;

	static Activity currentActivity = null;

	List<Happiness> responses;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_go_flow_main);

		myText = (TextView) findViewById(R.id.textView1);

		//
		//
		//
		buttonRcv1 = (Button) findViewById(R.id.button_rcv_st1);
		buttonRcv1.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				// new ProviderTask("station1", 1).execute();
				if (responder1 == null) {
					Happiness localHqppiness = new Happiness();
					localHqppiness.setHotSpot(station1);
					localHqppiness.setCrowdSourcedValue(hlevel);

					// create the happiness request to be sent
					HappinessRequest happiRequest = new HappinessRequest();
					happiRequest.setStation(station1);
					happiRequest.setTimstamp(System.currentTimeMillis());


					HappinessCSInfoResponderCallback responderHandler = new HappinessCSInfoResponderCallback();
					responderHandler.setValues(localHqppiness, station1, GoFlowMainActivity.this);

					responder1 = new HappinessCSResponder(Happiness.class, HappinessRequest.class, responderHandler);
					responder1.startReceivingNotification(station1.getId());
					buttonRcv1.setText("Unsubscribe St1");
				} else {
					responder1.stopReceivingNotification();
					responder1 = null;
					buttonRcv1.setText("Subscribe St1");
				}
			}
		});
		//
		//
		buttonRcv2 = (Button) findViewById(R.id.button_rcv_st2);
		buttonRcv2.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (responder2 == null) {
					Happiness localHqppiness = new Happiness();
					localHqppiness.setHotSpot(station2);
					localHqppiness.setCrowdSourcedValue(hlevel);

					// create the happiness request to be sent
					HappinessRequest happiRequest = new HappinessRequest();
					happiRequest.setStation(station2);
					happiRequest.setTimstamp(System.currentTimeMillis());

					com.threecixty.gfsample.happiness.HappinessCSInfoResponderCallback responderHandler = new com.threecixty.gfsample.happiness.HappinessCSInfoResponderCallback();
					responderHandler.setValues(localHqppiness, station2, GoFlowMainActivity.this);

					responder2 = new com.threecixty.gfsample.happiness.HappinessCSResponder(Happiness.class, HappinessRequest.class, responderHandler);
					responder2.startReceivingNotification(station2.getId());
					buttonRcv2.setText("Unsubscribe St2");
				} else {
					responder2.stopReceivingNotification();
					responder2 = null;
					buttonRcv2.setText("Subscribe St2");
				}
			}
		});
		//
		//
		buttonAsk1 = (Button) findViewById(R.id.button_ask_st1);
		buttonAsk1.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (requester1 == null) {
					// create the request
					HappinessRequest happiRequest = new HappinessRequest();
					happiRequest.setStation(station1);
					happiRequest.setTimstamp(System.currentTimeMillis());
					// then create the CS requester, that will send the request and wait
					// for the response
					requester1 = new HappinessCSInfoRequester(Happiness.class, happiRequest, GoFlowMainActivity.this);
					requester1.execute();
					buttonAsk1.setText("Stop ask 1");
				} else {
					requester1.stopListening();
					requester1 = null;
					buttonAsk1.setText("Start ask 1");
				}
			}
		});
		buttonAsk2 = (Button) findViewById(R.id.button_ask_st2);
		buttonAsk2.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (requester2 == null) {
					// create the request
					HappinessRequest happiRequest = new HappinessRequest();
					happiRequest.setStation(station2);
					happiRequest.setTimstamp(System.currentTimeMillis());
					// then create the CS requester, that will send the request and wait
					// for the response
					requester2 = new HappinessCSInfoRequester(Happiness.class, happiRequest, GoFlowMainActivity.this);
					requester2.execute();
					buttonAsk2.setText("Stop ask 2");
				} else {
					requester2.stopListening();
					requester2 = null;
					buttonAsk2.setText("Start ask 2");
				}
			}
		});
		//
		//
		buttonSnd1 = (Button) findViewById(R.id.button_snd_st1);
		buttonSnd1.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				HappinessCSPushCallback callback = new HappinessCSPushCallback();

				Happiness localHqppiness = new Happiness();
				localHqppiness.setHotSpot(station1);
				localHqppiness.setCrowdSourcedValue(hlevel);

				CrowdSourcedPushTask<Happiness> pushTask = new CrowdSourcedPushTask<Happiness>(station1.getId(), Happiness.class, callback);
				long reqId = pushTask.sendData(localHqppiness);
				System.out.println("send data with reqid " + reqId);
			}
		});
		buttonSnd2 = (Button) findViewById(R.id.button_snd_st2);
		buttonSnd2.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				HappinessCSPushCallback callback = new HappinessCSPushCallback();

				Happiness localHqppiness = new Happiness();
				localHqppiness.setHotSpot(station2);
				localHqppiness.setCrowdSourcedValue(hlevel);

				CrowdSourcedPushTask<Happiness> pushTask = new CrowdSourcedPushTask<Happiness>(station2.getId(), Happiness.class, callback);
				long reqId = pushTask.sendData(localHqppiness);
				System.out.println("send data with reqid " + reqId);
			}
		});
		//
		//
		buttonSrv1 = (Button) findViewById(R.id.button_srv_st1);
		buttonSrv1.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				myText.setText("Query server st 1");
				CrowdsourceManagerFactory.getSingleton().getMonitoredDataAsync(station1.getId(), 15 * 60 * 1000, Happiness.class, GoFlowMainActivity.this);
			}
		});
		buttonSrv2 = (Button) findViewById(R.id.button_srv_st2);
		buttonSrv2.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				myText.setText("Query server st 2");
				CrowdsourceManagerFactory.getSingleton().getMonitoredDataAsync(station2.getId(), 15 * 60 * 1000, Happiness.class, GoFlowMainActivity.this);
			}
		});

		currentActivity = this;

		//
		//
		//

        Intent startIntent = getIntent();
        String accessToken = startIntent.getStringExtra("token");
        System.out.println("access token ="+ startIntent);

        monitor = new CrowdsourcingMonitor();

        hlevel.setLevel(2);
        hlevel.setDescription("xxx");

        try {
            Object[] args = new Object[3];
            args[0] = this.getApplicationContext();
            args[1] = monitor;
            args[2] = Incentives.FUN;
            CrowdsourceManagerFactory.getSingleton().initCrowdsourcing(args);

            new InitOperation().execute(accessToken);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}


    private class InitOperation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String accessToken = "81849f3a-9f1f-4aec-84bd-a405e7b3d2f1";
            //String accessToken = "ya29.pQDEF6fbQMeOkiwJ-GumbrW9dWn6wKV7Wo-u7HgJGNEFAGN8H1YJsof754TGuzofh4_avm1udaYjTQ";

            GoflowUtils.GoflowAccount gfaccount = GoflowUtils.getGoflowAccount(accessToken);
            String appId = gfaccount.getUsername();
            String user = gfaccount.getUsername();
            String pwd = gfaccount.getPassword();

            System.out.println("info from goflow account : appId="+ appId+ " user="+user+ " pwd="+pwd);


                // following 3Cixty registration, you can download your GoFlow credentials
                // (userid and pwd) to be used
                // when initializing the crowdsourcing middleware.

            try {
                CrowdsourceManagerFactory.getSingleton().setUserCredentials(appId, user, pwd);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onBackPressed()
	 */
	public void onBackPressed() {
		CrowdsourceManagerFactory.getSingleton().terminateCrowdsourcing();
		super.onBackPressed();
		getIntent().setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		finish();
	}

	//
	// REQUEST CALLBACKS FOR SERVER REQUEST
	//

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ambientic.crowdsource.core.CrowdSourcedServerCallback#failedServer()
	 */
	@Override
	public void failedServer() {
		runOnUiThread(new Runnable() {
            @Override
            public void run() {
                myText.setText("No res from server");
            }
        });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ambientic.crowdsource.core.CrowdSourcedServerCallback#successServer
	 * (java.lang.Object[])
	 */
	@Override
	public void successServer(final Object[] items) {
		System.out.println("Response from server");
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (items != null && items.length > 0) {
					Happiness item = (Happiness) items[0];
					myText.setText("Value from server " + item.getCrowdSourcedValue().getLevel());
				}
			}
		});
	}

	//
	// REQUESTER CALLBACK FRO HAPPINESS RESPONSE
	//

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ambientic.crowdsource.core.CrowdSourcedPullCallback#failed()
	 */
	@Override
	public void failedPull() {
		runOnUiThread(new Runnable() {
            @Override
            public void run() {
                myText.setText("failed");
            }
        });

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ambientic.crowdsource.core.CrowdSourcedPullCallback#update(java.util
	 * .List)
	 */
	@Override
	public void updatePull(List<Happiness> crowdSourceds, final boolean self) {
		responses = crowdSourceds;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(GoFlowMainActivity.this.getApplicationContext(), "Got response self=" + self, Toast.LENGTH_LONG).show();
				myText.setText("got response");
				for (Happiness tmpHappi : responses) {
					myText.setText(tmpHappi.getTimestamp() + " : " + tmpHappi.getHotSpot().getId() + " " + tmpHappi.getCrowdSourcedValue().getLevel());
				}
			}
		});
	}

	//
	// INNER CLASSES
	//

	public class CrowdsourcingMonitor implements CrowdsourcingCallback {

		@Override
		public void crowdsourcingEnabled() {
			System.out.println("crowdsourcingEnabled");
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					myText.setText("crowdsourcingEnabled");
				}
			});
			// must be done on ui thread
			//
		}

		@Override
		public void crowdsourcingDisabled() {
			System.out.println("crowdsourcingDisabled");
			// must be done on ui thread
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					myText.setText("crowdsourcingDisabled");
				}
			});
		}

		@Override
		public void crowdsourcingNetworkPending() {
			System.out.println("crowdsourcingNetworkPending");
		}

		@Override
		public IncentiveCallback getIncentiveCallback() {
			return new MyAppIncentiveCallback();
		}

	}

	public class MyAppIncentiveCallback implements IncentiveCallback {

		@Override
		public void handleNewIncentive(final IncentiveItem item) {
			try {
				currentActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						myText.setText(item.getIncentiveText());
						Toast.makeText(currentActivity.getApplicationContext(), item.getIncentiveText(), Toast.LENGTH_LONG).show();
					}
				});
			} catch (Exception e) {
				// do nothing
			}
		}

	}

	public class HappinessCSInfoRequester extends CrowdSourcedCommonPullTask<Happiness> {

		public HappinessCSInfoRequester(Class<Happiness> clazz, HappinessRequest csRequest, CrowdSourcedPullCallback<Happiness> callback) {
			super(clazz, csRequest, callback);
		}

	}

	// PushTask callback when sending response
	//
	public class HappinessCSPushCallback implements CrowdSourcedPushCallback<Happiness> {

		@Override
		public void failedPush(long requestId) {
			System.out.println("HappinessCSPushCallback response failed");
		}

		@Override
		public void successPush(long requestId) {
			System.out.println("HappinessCSPushCallback response success");
		}

		@Override
		public void delayedPush(long requestId) {
			System.out.println("HappinessCSPushCallback response delayed");
		}

	}



}