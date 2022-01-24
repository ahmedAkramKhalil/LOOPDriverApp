package com.general.files;

import android.content.Context;
import android.os.Handler;

import com.taxifgo.driver.MainActivity;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNReconnectionPolicy;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.utils.Logger;
import com.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Admin on 05-10-2016.
 */
public class ConfigPubNub extends SubscribeCallback {

    private static ConfigPubNub instance = null;

    public boolean isSubsToCabReq = false;
    Context mContext;
    PubNub pubnub;
    GeneralFunctions generalFunc;

    ArrayList<String[]> listOfPublishMsg = new ArrayList<>();
    boolean isCurrentMsgPublished = true;


    public static ConfigPubNub getInstance() {
        if (instance == null) {
            instance = new ConfigPubNub(MyApp.getInstance().getCurrentAct());
        }
        return instance;
    }

    public static ConfigPubNub getInstance(boolean isForceReset) {

        if (instance != null) {
            instance.releaseInstances();
        }

        instance = new ConfigPubNub(MyApp.getInstance().getCurrentAct());

        return instance;
    }

    public static ConfigPubNub retrieveInstance() {
        return instance;
    }

    public ConfigPubNub(Context mContext) {
        this.mContext = mContext;
    }

    public void buildPubSub() {
        releaseInstances();

        if (mContext == null) {
            return;
        }

        generalFunc = MyApp.getInstance().getAppLevelGeneralFunc();

        if (generalFunc.retrieveValue(Utils.PUBSUB_TECHNIQUE).equalsIgnoreCase("SocketCluster")) {
            ConfigSCConnection.getInstance().buildConnection();
            return;
        } else if (generalFunc.retrieveValue(Utils.PUBSUB_TECHNIQUE).equalsIgnoreCase("PubNub")) {
            PNConfiguration pnConfiguration = new PNConfiguration();
            pnConfiguration.setUuid((generalFunc.retrieveValue(Utils.DEVICE_SESSION_ID_KEY).equals("") ? generalFunc.getMemberId() : generalFunc.retrieveValue(Utils.DEVICE_SESSION_ID_KEY)));

            pnConfiguration.setSubscribeKey(generalFunc.retrieveValue(Utils.PUBNUB_SUB_KEY));
            pnConfiguration.setPublishKey(generalFunc.retrieveValue(Utils.PUBNUB_PUB_KEY));
            pnConfiguration.setSecretKey(generalFunc.retrieveValue(Utils.PUBNUB_SEC_KEY));
            pnConfiguration.setReconnectionPolicy(PNReconnectionPolicy.LINEAR);
//        pnConfiguration.setLogVerbosity(PNLogVerbosity.BODY);

            pubnub = new PubNub(pnConfiguration);
            addListener();
            subscribeToPrivateChannel();
            reConnectPubNub(10000);
            reConnectPubNub(20000);
            reConnectPubNub(30000);
        } else if (generalFunc.retrieveValue(Utils.PUBSUB_TECHNIQUE).equalsIgnoreCase("Yalgaar")) {
            //yalgaar
            ConfigYalgaarConnection.getInstance().buildConnection();
            return;

        }
    }


    public void reConnectPubNub(int duration) {
        new Handler().postDelayed(() -> connectToPubNub(), duration);
    }

    public void connectToPubNub(int interval) {
        new Handler().postDelayed(() -> {
            if (pubnub != null) {
                pubnub.reconnect();
            }
        }, interval);
    }

    public void connectToPubNub() {
        new Handler().postDelayed(() -> {
            if (pubnub != null) {
                pubnub.reconnect();
            }
        }, 10000);
    }

    public void connectToPubNub(final PubNub pubNub) {

        new Handler().postDelayed(() -> {
            if (pubNub != null) {
                pubNub.reconnect();
            }
        }, 10000);
    }

    public void subscribeToPrivateChannel() {
        if (pubnub != null) {
            pubnub.subscribe()
                    .channels(Arrays.asList("DRIVER_" + generalFunc.getMemberId())) // subscribe to channels
                    .execute();
        }
        if (ConfigSCConnection.retrieveInstance() != null) {
            ConfigSCConnection.getInstance().subscribeToChannels("DRIVER_" + generalFunc.getMemberId());
        }
        if (ConfigYalgaarConnection.retrieveInstance() != null) {
            ConfigYalgaarConnection.getInstance().subscribeToChannels("DRIVER_" + generalFunc.getMemberId());
        }
    }

    public void unSubscribeToPrivateChannel() {
        if (pubnub != null) {
            pubnub.unsubscribe()
                    .channels(Arrays.asList("DRIVER_" + generalFunc.getMemberId())) // subscribe to channels
                    .execute();
        }
        if (ConfigSCConnection.retrieveInstance() != null) {
            ConfigSCConnection.getInstance().unSubscribeFromChannels("DRIVER_" + generalFunc.getMemberId());
        }
        if (ConfigYalgaarConnection.retrieveInstance() != null) {
            ConfigYalgaarConnection.getInstance().unSubscribeFromChannels("DRIVER_" + generalFunc.getMemberId());
        }
    }

    public void releasePubSubInstance() {
        releaseInstances();
    }

    private void releaseInstances() {

        try {
            if (pubnub != null) {
                pubnub.removeListener(this);
                pubnub.forceDestroy();
            }

            if (ConfigSCConnection.retrieveInstance() != null) {
                ConfigSCConnection.getInstance().forceDestroy();
            }

            if (ConfigYalgaarConnection.retrieveInstance() != null) {
                ConfigYalgaarConnection.getInstance().forceDestroy();
            }

            Utils.runGC();
        } catch (Exception e) {

        }
    }


    public void addListener() {

        if (pubnub != null) {
            pubnub.removeListener(this);
            pubnub.addListener(this);

            pubnub.reconnect();
        }

        connectToPubNub();

    }

    private void dispatchMsg(String jsonMsg, String receivedBy) {
        try {
            (new FireTripStatusMsg(MyApp.getInstance().getCurrentAct(), receivedBy)).fireTripMsg(jsonMsg);
        } catch (Exception e) {

        }
    }

    public void subscribeToChannels(ArrayList<String> channels) {
        Logger.e("SubscribdChannels", ":::" + channels.toString());
        if (pubnub != null) {
            pubnub.subscribe()
                    .channels(channels) // subscribe to channels
                    .execute();
        }
        if (ConfigSCConnection.retrieveInstance() != null) {
            for (int i = 0; i < channels.size(); i++) {
                ConfigSCConnection.getInstance().subscribeToChannels(channels.get(i));
            }
        }

        if (ConfigYalgaarConnection.retrieveInstance() != null) {
            for (int i = 0; i < channels.size(); i++) {
                ConfigYalgaarConnection.getInstance().subscribeToChannels(channels.get(i));
            }
        }
    }

    public void unSubscribeToChannels(ArrayList<String> channels) {
        Logger.e("UnSubscribdChannels", ":::" + channels.toString());
        if (pubnub != null) {
            pubnub.unsubscribe()
                    .channels(channels)
                    .execute();
        }
        if (ConfigSCConnection.retrieveInstance() != null) {
            for (int i = 0; i < channels.size(); i++) {
                ConfigSCConnection.getInstance().unSubscribeFromChannels(channels.get(i));
            }
        }


        if (ConfigYalgaarConnection.retrieveInstance() != null) {
            for (int i = 0; i < channels.size(); i++) {
                ConfigYalgaarConnection.getInstance().unSubscribeFromChannels(channels.get(i));
            }
        }
    }

    public void subscribeToCabRequestChannel() {
        isSubsToCabReq = true;

        if (pubnub != null) {
            pubnub.subscribe()
                    .channels(Arrays.asList("CAB_REQUEST_DRIVER_" + generalFunc.getMemberId())) // subscribe to channels
                    .execute();
        }

        if (ConfigSCConnection.retrieveInstance() != null) {
            ConfigSCConnection.getInstance().subscribeToChannels("CAB_REQUEST_DRIVER_" + generalFunc.getMemberId());
        }

        if (ConfigYalgaarConnection.retrieveInstance() != null) {
            ConfigYalgaarConnection.getInstance().subscribeToChannels("CAB_REQUEST_DRIVER_" + generalFunc.getMemberId());
        }
    }


    public void unSubscribeToCabRequestChannel() {
        isSubsToCabReq = false;

        if (pubnub != null) {
            pubnub.unsubscribe()
                    .channels(Arrays.asList("CAB_REQUEST_DRIVER_" + generalFunc.getMemberId())) // subscribe to channels
                    .execute();
        }
        if (ConfigSCConnection.retrieveInstance() != null) {
            ConfigSCConnection.getInstance().unSubscribeFromChannels("CAB_REQUEST_DRIVER_" + generalFunc.getMemberId());
        }

        if (ConfigYalgaarConnection.retrieveInstance() != null) {
            ConfigYalgaarConnection.getInstance().unSubscribeFromChannels("CAB_REQUEST_DRIVER_" + generalFunc.getMemberId());
        }
    }


    public void publishMsg(String channel, String message) {
        if (message == null) {
            return;
        }

        if (pubnub != null) {
            if (!isCurrentMsgPublished) {
                String[] arr = {channel, message};
                listOfPublishMsg.add(arr);
                return;
            }

            continuePublish(channel, message);
        }

        if (ConfigSCConnection.retrieveInstance() != null) {
            ConfigSCConnection.getInstance().publishMsg(channel, message);
        }

        if (ConfigYalgaarConnection.retrieveInstance() != null) {
            ConfigYalgaarConnection.getInstance().publishMsg(channel, message);
        }

    }

    private void continuePublish(String channel, String message) {
        isCurrentMsgPublished = false;

        if (pubnub != null) {
            pubnub.publish()
                    .message(message)
                    .channel(channel)
                    .async(new PNCallback<PNPublishResult>() {
                        @Override
                        public void onResponse(PNPublishResult result, PNStatus status) {
                            isCurrentMsgPublished = true;

                            if (listOfPublishMsg.size() > 0) {
                                String[] arr = listOfPublishMsg.get(0);
                                listOfPublishMsg.remove(0);
                                continuePublish(arr[0], arr[1]);
                            }
                        }
                    });
        }

    }

    @Override
    public void status(PubNub pubnub, PNStatus status) {
        if (pubnub == null || status == null || status.getCategory() == null) {
            connectToPubNub();
            return;
        }

        if (mContext instanceof MainActivity) {
            ((MainActivity) mContext).pubNubStatus(status.getCategory());
        }
        switch (status.getCategory()) {
            case PNMalformedResponseCategory:
            case PNUnexpectedDisconnectCategory:
            case PNTimeoutCategory:
            case PNNetworkIssuesCategory:
            case PNDisconnectedCategory:
                connectToPubNub(pubnub);
                break;
            case PNConnectedCategory:
                // Connect event. You can do stuff like publish, and know you'll get it.
                // Or just use the connected event to confirm you are subscribed for
                // UI / internal notifications, etc
                break;

            default:
                break;

        }
    }

    @Override
    public void message(PubNub pubnub, PNMessageResult message) {
        Logger.e("PubNubMsg", "::" + message.getMessage().toString());
        dispatchMsg(message.getMessage().toString(), "PubSub");
    }

    @Override
    public void presence(PubNub pubnub, PNPresenceEventResult presence) {

    }
}