package com.ambientic.goflow.happiness;

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ambientic.crowdsource.core.CrowdSourcedCommonPullTask;
import com.ambientic.crowdsource.core.CrowdSourcedPullCallback;
import com.ambientic.crowdsource.core.CrowdSourcedPushCallback;
import com.ambientic.crowdsource.core.CrowdSourcedReceiver;
import com.ambientic.crowdsource.core.CrowdSourcedReceiverCallback;
import com.ambientic.crowdsource.core.CrowdSourcedResponsePushTask;
import com.ambientic.crowdsource.core.CrowdSourcedServerCallback;
import com.ambientic.crowdsource.core.CrowdSourcedServerPullTask;
import com.ambientic.crowdsource.core.CrowdsourceManager;
import com.ambientic.crowdsource.happiness.Happiness;
import com.ambientic.crowdsource.happiness.HappinessLevel;
import com.ambientic.crowdsource.happiness.HappinessRequest;
import com.ambientic.crowdsource.happiness.HappinessVote;
import com.ambientic.crowdsource.happiness.Station;
import com.clickntap.expo2015.crowdness.HappinessPushLayout;
import com.clickntap.expobarcellona.R;
import com.clickntap.gtap.App;


/**
 * 
 * @author Pierre-Guillaume Raverdy, Ambientic
 *
 */
public class HappinessCSManager implements CrowdSourcedServerCallback<Happiness> {
	/** data from server should be this fresh : 15 minutes **/
	static final long FRESHNESS = 15 * 60 * 1000;

	private Activity activity;

	private HappinessVote happinessInfo;
	private Station station;

	// for listenning to requests
	HappinessCSResponder happinessListener = null;
	// for sending/receiving responses
	HappinessCSInfoRequesterCallback requesterHandler = null;
	HappinessCSInfoRequester requester = null;

	//
	//
	//

	public HappinessCSManager(Activity act, Station station) {
		this.activity = act;
		this.station = station;
		Log.d("GOFLOW", "HappinessCSManager is called with station = " + station.getCanonicalName());
	}
	
	public void initCrowdsourcing() {
		// first start to listen for request from other peers
		startListeningRequests();

		// then start to listen for responses
		// and pulls information from the server
		// the callback for the server pull will automatically send a request to the peers 
		// and the responses will be received
		pullHappinessDataFromServer();
	}

	public void terminate() {
		stoptListeningRequests();
		stopListeningResponses();
	}

	//
	//
	// ##################################### PUBLIC METHODS
	//
	//

	/**
	 * 
	 */
	public synchronized void startListeningRequests() {

		if (happinessListener == null) {
			// first create the callback for receiving requests - e
			HappinessCSInfoResponderCallback responderHandler = new HappinessCSInfoResponderCallback(activity);
			// then create the class that launch the process task
			HappinessCSInfoResponderCallbackPerformer responderPerformer = new HappinessCSInfoResponderCallbackPerformer();
			// then the receiver
			happinessListener = new HappinessCSResponder(Happiness.class, HappinessRequest.class, responderHandler, responderPerformer);

			// then start to listen for requests
			happinessListener.startReceivingNotification(station.getId());
		}
	}

	/**
	 * 
	 */
	public synchronized void stoptListeningRequests() {
		if (happinessListener != null) {
			happinessListener.stopReceivingNotification();
			happinessListener = null;
		}
	}

	/**
	 * Sends again the request to the peers. 
	 * The lsitenner and requester must be initialized beforehand (i.e., initCrowdsourcing() must be called first)
	 */
	public synchronized void sendRequest() {
		if (requester != null) {
			requester.sendRequest();
		}
	}

	public synchronized void startListeningResponses() {
		if (requester == null) {
			requesterHandler = new HappinessCSInfoRequesterCallback();
			HappinessRequest happiRequest = new HappinessRequest();
			happiRequest.setStation(station);
			happiRequest.setTimstamp(System.currentTimeMillis());
			requester = new HappinessCSInfoRequester(Happiness.class, happiRequest, requesterHandler);
			requester.startListening();
		}
	}

	public synchronized void stopListeningResponses() {
		if (requester != null) {
			requester.stopListening();
			requester = null;
			requesterHandler = null;
		}
	}

	/**
	 * Initiates action to show five levels of happiness.
	 */
	public synchronized void provideFeedback() {
		Log.d("GOFLOW", "Init HappinessPushController");
		LinearLayout happinessIcon = (LinearLayout) activity.findViewById(R.id.push_crowding);
		ProgressBar loadingIcon = (ProgressBar) activity.findViewById(R.id.crowd_loading);
		loadingIcon.setVisibility(View.INVISIBLE);
		happinessIcon.setVisibility(View.VISIBLE);
		happinessIcon.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Log.d("GOFLOW", "into Onclick");
				showPushHappinessDialog();
			}
		});
	}

	//
	//
	// ##################################### PRIVATE METHODS
	//
	//

	/**
	 * Pull happiness from server.
	 */
	private void pullHappinessDataFromServer() {
		Log.d("GOFLOW", "HappinessPullController -PullHappinesData is called with station = " + station.getName() + " and id " + station.getId());
		System.out.println("PullHappinesData is called with station = " + station.getName() + " and id " + station.getId());
		CrowdsourceManager.getSingleton().getMonitoredDataAsync(station.getId(), -1, Happiness.class, this);
	}

	//
	//
	// ##################################### UI UPDATE METHODS
	//
	//

	/**
	 * Show or hide views.
	 */
	private synchronized void showOrHideView() {
		final RelativeLayout fumetto;
		Animation animation;
		ImageView iv;
		TextView tv;

		if (happinessInfo == null || activity == null)
			return;

		Log.d("GOFLOW", "HappinessPullController- Entered Show Or Hide View");

		Date start = new Date(happinessInfo.getTimeStamp());
		Date end = new Date(happinessInfo.getTimeStamp() + HappinessRequest.INTERVAL);

		// TextView levelTv = (TextView) activity.findViewById(R.id.crowd_text);
		// what about findview above ? not used/needed ?
		iv = (ImageView) activity.findViewById(R.id.crowed_icon);
		tv = (TextView) activity.findViewById(R.id.crowd_text_level);
		if (iv != null) {
			if (happinessInfo != null) {
				fumetto = (RelativeLayout) activity.findViewById(R.id.croudsourcingView);
				animation = AnimationUtils.loadAnimation(activity, R.anim.get_crowding);
				Runnable prova = new Runnable() {
					@Override
					public void run() {
						// AD OGNI ESECUZIONE DEL RUN ANIMIAMO ....
						// fumetto.startAnimation(animation);
						fumetto.setMinimumWidth(650);
					}
				};

				// DIAMO IL VIA AL PRIMO CICLO DI ANIMAZIONI
				prova.run();

				iv.setVisibility(View.VISIBLE);
				tv.setVisibility(View.VISIBLE);
				// levelTv.setText("" + happinessInfo.getCrowdSourcedValue().getLevel()
				// + "/5");
				// levelTv.setVisibility(View.VISIBLE);
				if (happinessInfo.getMostVotedLevel() == HappinessLevel.HAPPY.getLevel()) {
					iv.setImageResource(R.drawable.happy_icon_v3);
					fumetto.setMinimumWidth(650);
					tv.setText("The station is empty!");

					Log.d("GOFLOW", "HappinessPullController - A HAPPY ICON RECEIVED");
				} else if (happinessInfo.getMostVotedLevel() == HappinessLevel.GLAD.getLevel()) {
					iv.setImageResource(R.drawable.glad_icon_v3);
					fumetto.setMinimumWidth(650);
					tv.setText("The station is quite empty!");

					Log.d("GOFLOW", "HappinessPullController- A GLAD ICON RECEIVED");
				} else if (happinessInfo.getMostVotedLevel() == HappinessLevel.OK.getLevel()) {
					iv.setImageResource(R.drawable.ok_icon_v3);
					tv.setText("The crowding level is normal");
					fumetto.setMinimumWidth(650);

					Log.d("GOFLOW", "HappinessPullController- A OK ICON RECEIVED");
				} else if (happinessInfo.getMostVotedLevel() == HappinessLevel.SAD.getLevel()) {
					iv.setImageResource(R.drawable.sad_icon_v3);
					fumetto.setMinimumWidth(650);
					tv.setText("The station is quite crowded");

					Log.d("GOFLOW", "HappinessPullController- A SAD ICON RECEIVED");
				} else if (happinessInfo.getMostVotedLevel() == HappinessLevel.CRIED.getLevel()) {
					iv.setImageResource(R.drawable.cry_icon_v3);
					fumetto.setMinimumWidth(650);
					tv.setText("The station is very crowded!");
					Log.d("GOFLOW", "HappinessPullController- A CRY ICON RECEIVED");
				} else {
					iv.setVisibility(View.GONE);
					tv.setVisibility(View.GONE);
					// levelTv.setVisibility(View.GONE);
				}
				iv.invalidate();
				tv.invalidate();
			} else {
				iv.setVisibility(View.GONE);
				tv.setVisibility(View.GONE);
				// levelTv.setVisibility(View.GONE);
			}
			TextView totalTv = (TextView) activity.findViewById(R.id.crowd_text);
			// TODO PGR : for now, just use happiness, not happinessvote, discuss with
			// TI
			totalTv.setText(happinessInfo.getMostVotedNumber() + " of " + happinessInfo.getNbVotes() + " votes");
			totalTv.invalidate();
		}
	}

	/**
	 * 
	 */
	private void showPushHappinessDialog() {
		Log.d("GOFLOW", "ENTERED PUSH HAPPINES DIALOG IN CONTROLLER");
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);

		final HappinessPushLayout localLayout = new HappinessPushLayout(activity, station);
		builder.setView(localLayout);

		// Push happiness at a station to server
		builder.setPositiveButton("Report Crowding", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				Happiness selected = localLayout.getSelectedHappiness();
				if (selected != null) {
					selected.setTimestamp(System.currentTimeMillis());

					HappinessResponsePushCallback respCallback = new HappinessResponsePushCallback();

					CrowdSourcedResponsePushTask<Happiness> pushTask = new CrowdSourcedResponsePushTask<Happiness>(station.getId(), Happiness.class, selected, respCallback);

					pushTask.execute();

					dialog.dismiss();
				}
			}
		});

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	//
	//
	// ##################################### INNER CLASSES
	//
	//

	// for processing requests from other peers
	//
	protected class HappinessCSInfoRequesterCallback implements CrowdSourcedPullCallback<Happiness> {

		@Override
		public void failedPull() {
			System.out.println("Device error, failed to send request");
			final Activity currentActivity = App.getCurrentActivity();
			currentActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(currentActivity, "Server error - Failed to request crowding", Toast.LENGTH_SHORT).show();
				}
			});
		}

		@Override
		public void updatePull(List<Happiness> crowdSourceds, boolean isSelf) {
			if (isSelf) {
				System.out.println("Got my feedback, ignore");
			} else {
				System.out.println("Got new feedback from people " + crowdSourceds.size());
				// received response, display immediatly
				if (crowdSourceds != null && crowdSourceds.size() > 0) {

					// ATTENTION : update first before launching ui thread
					if (happinessInfo == null) {
						happinessInfo = new HappinessVote(null);
					}
					happinessInfo.addVotes(crowdSourceds);

					final Activity currentActivity = App.getCurrentActivity();
					currentActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(currentActivity, "Got new feedback from people", Toast.LENGTH_SHORT).show();
							// need to be done in ui thread
							showOrHideView();
						}
					});
				}
			}
		}
	}

	// for sending requests to other
	//
	protected class HappinessCSInfoRequester extends CrowdSourcedCommonPullTask<Happiness> {

		public HappinessCSInfoRequester(Class<Happiness> clazz, HappinessRequest csRequest, CrowdSourcedPullCallback<Happiness> callback) {
			super(clazz, csRequest, callback);
		}
	}

	// for processing request to the server
	//
	protected class HappinessCSInfoServerRequester extends CrowdSourcedServerPullTask<Happiness> {

		public HappinessCSInfoServerRequester(Class<Happiness> clazz, String hotspotId) {
			super(clazz, hotspotId);
		}

		public HappinessCSInfoServerRequester(Class<Happiness> clazz, String hotspotId, int freshnessSec) {
			super(clazz, hotspotId, timestamp);
		}
	}

	// PushTask callback when sending response - cannot be implemented by main
	// class since need callback classes for pushing request as well as response
	//
	protected class HappinessResponsePushCallback implements CrowdSourcedPushCallback<Happiness> {

		@Override
		public void failedPush(Happiness data) {
			System.out.println("HappinessCSPushCallback failed");
			final Activity currentActivity = App.getCurrentActivity();
			currentActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(currentActivity, "Server error - Failed to report crowding", Toast.LENGTH_SHORT).show();
				}
			});
		}

		@Override
		public void successPush() {
			// TODO Auto-generated method stub
			System.out.println("successfully push happiness -------------");
			final Activity currentActivity = App.getCurrentActivity();
			currentActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(currentActivity, "Crowding reported successfully", Toast.LENGTH_SHORT).show();
				}
			});
			// if close, then activity is not usable anymore and the toast above will
			// not work...
			// activity.finish();
		}
	}

	// PushTask callback when sending request - cannot be implemented by main
	// class since need callback classes for pushing request as well as response
	//
	protected class HappinessRequestPushCallback implements CrowdSourcedPushCallback<Happiness> {

		@Override
		public void failedPush(Happiness data) {
			System.out.println("HappinessCSPushCallback failed");
			final Activity currentActivity = App.getCurrentActivity();
			currentActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(currentActivity, "Server error - Failed to ask crowding", Toast.LENGTH_SHORT).show();
				}
			});
		}

		@Override
		public void successPush() {
			System.out.println("successfully push happiness request-------------");
			final Activity currentActivity = App.getCurrentActivity();
			currentActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(currentActivity, "Request sent successfully", Toast.LENGTH_SHORT).show();
				}
			});
			// if close, then activity is not usable anymore and the toast above will
			// not work...
			// activity.finish();
		}

	}
	
	public class HappinessCSResponder extends CrowdSourcedReceiver<Happiness, HappinessRequest> {

		public HappinessCSResponder(Class<Happiness> responseClazz, Class<HappinessRequest> requestClazz, CrowdSourcedReceiverCallback<Happiness, HappinessRequest> handler,
				CrowdSourcedReceiver.OnDemandHandlerPerformer<Happiness, HappinessRequest> handlerPerformer) {
			super(responseClazz, requestClazz, handler, handlerPerformer);
		}

	}
	
	public class HappinessCSInfoResponderCallbackPerformer implements CrowdSourcedReceiver.OnDemandHandlerPerformer<Happiness, HappinessRequest> {

		@Override
		public void receiveRequest(HappinessRequest request, CrowdSourcedReceiverCallback<Happiness, HappinessRequest> handler, boolean isSelf) {
			// simple java, not android, no need to do async
			handler.receiveRequest(request, isSelf);
		}
	}
	
	public class HappinessCSInfoResponderCallback implements CrowdSourcedReceiverCallback<Happiness, HappinessRequest> {


		private static final boolean DOSENDAUTO = false;
		private static final boolean DOTOAST = true;

		Context appCtx;
		
		/**
		 * 
		 * @param appCtx application's context in order to displqay toast
		 */
		public HappinessCSInfoResponderCallback(Context appCtx) {
			this.appCtx = appCtx;
		}
		
		Happiness localHqppiness = null;
		Station localStation = null;

		@Override
		public void receiveRequest(final HappinessRequest request, final boolean isSelf) {
			System.out.println("RECEIVED REQUEST FOR HAPPPINESS isSefl=" + isSelf);
			
			// automatic response ???
			if (!isSelf && DOSENDAUTO) {
				System.out.println("SENDING RESPONSE FOR HAPPPINESS");
				HappinessCSPushCallback callback = new HappinessCSPushCallback();
				localHqppiness.setTimestamp(System.currentTimeMillis());
				CrowdSourcedResponsePushTask<Happiness> pushTask = new CrowdSourcedResponsePushTask<Happiness>(localStation.getId(),Happiness.class, localHqppiness, callback);
				pushTask.execute();			
			}

			// displaying toast to user
			if (!isSelf && DOTOAST) {
				final Activity currentActivity = App.getCurrentActivity();
				currentActivity.runOnUiThread(new Runnable() {
					   @Override
					   public void run() {
							Toast.makeText(currentActivity, "You received a request for happiness information at station " + request.getStation().getName(), Toast.LENGTH_LONG).show();
					   }
					  });
			}
		}

		// PushTask callback when sending response
		//
		public class HappinessCSPushCallback implements CrowdSourcedPushCallback<Happiness> {

			@Override
			public void failedPush(Happiness data) {
				System.out.println("HappinessCSPushCallback failed");
				final Activity currentActivity = App.getCurrentActivity();
				currentActivity.runOnUiThread(new Runnable() {
					   @Override
					   public void run() {
							Toast.makeText(currentActivity, "Unable to send feedback, try again later", Toast.LENGTH_LONG).show();
					   }
					  });
			}

			@Override
			public void successPush() {
				System.out.println("HappinessCSPushCallback success");
				final Activity currentActivity = App.getCurrentActivity();
				currentActivity.runOnUiThread(new Runnable() {
					   @Override
					   public void run() {
							Toast.makeText(currentActivity, "Feedback sent, thanks", Toast.LENGTH_LONG).show();
					   }
					  });
			}

		}
	}


	//
	//
	// ##################################### INTERFACES METHODS
	//
	//

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ambientic.crowdsource.core.CrowdSourcedServerCallback#failedServer()
	 */
	@Override
	public void failedServer() {
		final Activity currentActivity = App.getCurrentActivity();
		currentActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(currentActivity, "Crowd sourcing server error - unable to connect", Toast.LENGTH_SHORT).show();
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
	public void successServer(Object[] paramArrayOfObject) {
		Happiness[] results = (Happiness[]) paramArrayOfObject;

		if (results == null) {
			System.out.println("Error : unable to get CS info from server");
		}

		// if no valid CS info from server, pull from other people currently at
		// location
		if (results != null) {
			System.out.println("GOFLOW got nb results from server = " + results.length);
			// TODO PGR should compute average ??
			for (Happiness happy : results) {
				Log.d("GOFLOW", " - " + happy.getTimestamp() + " : " + happy.getCrowdSourcedValue().getLevel());
			}
			// TODO PGR for now take the first one
			happinessInfo = new HappinessVote(results);
			// ATT : not sure if done in ui thread ??
			showOrHideView();
		} else {
		}

		// for now, always perform request

		// create the happiness request to be sent
		HappinessRequest happiRequest = new HappinessRequest();
		happiRequest.setStation(station);
		happiRequest.setTimstamp(System.currentTimeMillis());

		// then create the CS requester, that will send the request and wait for
		// the response
		requesterHandler = new HappinessCSInfoRequesterCallback();
		requester = new HappinessCSInfoRequester(Happiness.class, happiRequest, requesterHandler);
		requester.startListening();
		requester.sendRequest();
	}

}
