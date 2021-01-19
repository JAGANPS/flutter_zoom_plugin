package com.rubrixanalytics.flutter_zoom_plugin;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.app.AlertDialog;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import android.content.DialogInterface;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.platform.PlatformView;
import us.zoom.sdk.JoinMeetingOptions;
import us.zoom.sdk.InMeetingService;
import us.zoom.sdk.InMeetingUserInfo;
import us.zoom.sdk.MeetingService;
import us.zoom.sdk.JoinMeetingParams;
import us.zoom.sdk.StartMeetingParamsWithoutLogin;
import us.zoom.sdk.StartMeetingOptions;
import us.zoom.sdk.MeetingService;
import us.zoom.sdk.MeetingStatus;
import us.zoom.sdk.ZoomError;
import us.zoom.sdk.ZoomSDK;
import us.zoom.sdk.ZoomSDKAuthenticationListener;
import us.zoom.sdk.ZoomSDKInitParams;
import us.zoom.sdk.ZoomSDKInitializeListener;
import us.zoom.sdk.InMeetingEventHandler;

public class ZoomView  implements PlatformView,
        MethodChannel.MethodCallHandler,
        ZoomSDKAuthenticationListener {
    private final TextView textView;
 private MeetingService mMeetingService;

    private InMeetingService mInMeetingService;
    private final MethodChannel methodChannel;
    private final Context context;
    private final EventChannel meetingStatusChannel;

    ZoomView(Context context, BinaryMessenger messenger, int id) {
        textView = new TextView(context);
        this.context = context;

        methodChannel = new MethodChannel(messenger, "com.rubrixanalytics/flutter_zoom_plugin");
        methodChannel.setMethodCallHandler(this);

        meetingStatusChannel = new EventChannel(messenger, "com.rubrixanalytics/zoom_event_stream");
    }

    @Override
    public View getView() {
        return textView;
    }


    @Override
    public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
        switch (methodCall.method) {
            case "init":
                init(methodCall, result);
                break;
            case "join":
                joinMeeting(methodCall, result);
                break;

            case "start":
                startMeeting(methodCall, result);
                break;
            case "meeting_status":
                meetingStatus(result);
                break;  
            default:
                result.notImplemented();
        }

    }

    private void init(final MethodCall methodCall, final MethodChannel.Result result) {

        Map<String, String> options = methodCall.arguments();

        ZoomSDK zoomSDK = ZoomSDK.getInstance();

        if(zoomSDK.isInitialized()) {
            List<Integer> response = Arrays.asList(0, 0);
            result.success(response);
            return;
        }

        ZoomSDKInitParams initParams = new ZoomSDKInitParams();
        initParams.jwtToken = options.get("sdkToken");
        initParams.appKey = options.get("appKey");
        initParams.appSecret = options.get("appSecret");
        initParams.domain = options.get("domain");
        
        zoomSDK.initialize(
                context,
                new ZoomSDKInitializeListener() {

                    @Override
                    public void onZoomAuthIdentityExpired() {

                    }
                    

                    @Override
                    public void onZoomSDKInitializeResult(int errorCode, int internalErrorCode) {
                        List<Integer> response = Arrays.asList(errorCode, internalErrorCode);

                        if (errorCode != ZoomError.ZOOM_ERROR_SUCCESS) {
                            System.out.println("Failed to initialize Zoom SDK");
                            result.success(response);
                            return;
                        }

                        ZoomSDK zoomSDK = ZoomSDK.getInstance();
                        MeetingService meetingService = zoomSDK.getMeetingService();
                        meetingStatusChannel.setStreamHandler(new StatusStreamHandler(meetingService));
                        result.success(response);
                    }
                },
                initParams);
    }

    private void joinMeeting(MethodCall methodCall, MethodChannel.Result result) {

        Map<String, String> options = methodCall.arguments();

        ZoomSDK zoomSDK = ZoomSDK.getInstance();

        if(!zoomSDK.isInitialized()) {
            System.out.println("Not initialized!!!!!!");
            result.success(false);
            return;
        }

        final MeetingService meetingService = zoomSDK.getInstance().getMeetingService();

        JoinMeetingOptions opts = new JoinMeetingOptions();
        opts.no_invite = parseBoolean(options, "disableInvite", false);
        opts.no_share = parseBoolean(options, "disableShare", false);
        opts.no_driving_mode = parseBoolean(options, "disableDrive", false);
        opts.no_dial_in_via_phone = parseBoolean(options, "disableDialIn", false);
        opts.no_disconnect_audio = parseBoolean(options, "noDisconnectAudio", false);
        opts.no_audio = parseBoolean(options, "noAudio", false);
        opts.no_webinar_register_dialog = parseBoolean(options, "no_webinar_register_dialog", false);
        
      //  opts.onJoinWebinarNeedUserNameAndEmail = parseBoolean(options, "onJoinWebinarNeedUserNameAndEmail", true);
///tested here without forum please proceed it 
//removed true for webinar
//addedd from changsestdhyfuyfyfufhohig
//added a new option here
        JoinMeetingParams params = new JoinMeetingParams();

       
        params.meetingNo = options.get("meetingId");
        params.password = options.get("meetingPassword");
         params.displayName = options.get("userId");
         
   
       //removed email address


       // params.no_webinar_register_dialog=options.get("no_webinar_register_dialog");
        meetingService.joinMeetingWithParams(context, params, opts);
        result.success(true);
    }
    ///here we go with error
//////hererererererere


    private void startMeeting(MethodCall methodCall, MethodChannel.Result result) {

        Map<String, String> options = methodCall.arguments();

        ZoomSDK zoomSDK = ZoomSDK.getInstance();

        if(!zoomSDK.isInitialized()) {
            System.out.println("Not initialized!!!!!!");
            result.success(false);
            return;
        }

        final MeetingService meetingService = zoomSDK.getMeetingService();

        StartMeetingOptions opts = new StartMeetingOptions();
        opts.no_invite = parseBoolean(options, "disableInvite", false);
        opts.no_share = parseBoolean(options, "disableShare", false);
        opts.no_driving_mode = parseBoolean(options, "disableDrive", false);
        opts.no_dial_in_via_phone = parseBoolean(options, "disableDialIn", false);
        opts.no_disconnect_audio = parseBoolean(options, "noDisconnectAudio", false);
        opts.no_audio = parseBoolean(options, "noAudio", false);
    
        StartMeetingParamsWithoutLogin params = new StartMeetingParamsWithoutLogin();
		params.userId = options.get("userId");
        params.displayName=options.get("displayName");
        params.meetingNo = options.get("meetingId");
		params.userType = MeetingService.USER_TYPE_API_USER;
		params.zoomToken = options.get("zoomToken");
		params.zoomAccessToken = options.get("zoomAccessToken");
        

///hgdfjchdtfcxtydfxty
//removed email address
		//cghghjhfjhfhgdgggsgffgsgsfgsfafsgsfsgfssgstrstsgsgsgf
        meetingService.startMeetingWithParams(context, params, opts);

        result.success(true);
    }

    private boolean parseBoolean(Map<String, String> options, String property, boolean defaultValue) {
        return options.get(property) == null ? defaultValue : Boolean.parseBoolean(options.get(property));
    }


    private void meetingStatus(MethodChannel.Result result) {

        ZoomSDK zoomSDK = ZoomSDK.getInstance();

        if(!zoomSDK.isInitialized()) {
            System.out.println("Not initialized!!!!!!");
            result.success(Arrays.asList("MEETING_STATUS_UNKNOWN", "SDK not initialized"));
            return;
        }

        MeetingService meetingService = zoomSDK.getMeetingService();

        if(meetingService == null) {
            result.success(Arrays.asList("MEETING_STATUS_UNKNOWN", "No status available"));
            return;
        }

        MeetingStatus status = meetingService.getMeetingStatus();
        result.success(status != null ? Arrays.asList(status.name(), "") :  Arrays.asList("MEETING_STATUS_UNKNOWN", "No status available"));
    }
///removed no webinar option

private void showWebinarNeedRegisterDialog(final InMeetingEventHandler inMeetingEventHandler) {
    
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle("Need register to join this webinar meeting ")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mInMeetingService.leaveCurrentMeeting(true);
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(null!=inMeetingEventHandler)
                        {
                            long time=System.currentTimeMillis();
                            inMeetingEventHandler.setRegisterWebinarInfo("test", time+"@example.com", false);
                        }
                    }
                }).create();
        dialog.hide();
    }
     
///////hererexchgchjchxy jh

/////removed some part of the code testing purpose here its occured again
    public void onJoinWebinarNeedUserNameAndEmail(InMeetingEventHandler inMeetingEventHandler) {
        long time=System.currentTimeMillis();
        showWebinarNeedRegisterDialog(inMeetingEventHandler);
        inMeetingEventHandler.setRegisterWebinarInfo("test", time+"@example.com", false);
    }
  
    @Override
    public void dispose() {}

    @Override
    public void onZoomAuthIdentityExpired() {

    }


    @Override
    public void onZoomSDKLoginResult(long result) {

    }

    @Override
    public void onZoomSDKLogoutResult(long result) {

    }

    @Override
    public void onZoomIdentityExpired() {

    }



}
