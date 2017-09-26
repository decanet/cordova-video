package cordova.decanet.video;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.ffmpeg.android.ShellUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.ffmpeg.android.FfmpegController;
import org.ffmpeg.android.Clip;
import org.ffmpeg.android.ShellUtils.ShellCallback;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;




public class DecanetVideo extends CordovaPlugin {
    private static final String TAG = "DECANET_VIDEO";
    private static final String ACTION_START_PREVIEW = "startpreview";
    private static final String ACTION_START_RECORDING = "startrecording";
    private static final String ACTION_STOP_RECORDING = "stoprecording";
    private static final String ACTION_STOP_PREVIEW = "stop";
    private static final String FILE_EXTENSION = ".mp4";
    private static final int START_REQUEST_CODE = 0;

    private String FILE_PATH = "";
    private VideoOverlay videoOverlay;
    private CallbackContext callbackContext;
    private JSONArray requestArgs;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        //FILE_PATH = Environment.getExternalStorageDirectory().toString() + "/";
        //FILE_PATH = cordova.getActivity().getCacheDir().toString() + "/";
		//FILE_PATH = cordova.getActivity().getExternalCacheDir().toString() + "/";
        FILE_PATH = cordova.getActivity().getFilesDir().toString() + "/";
		//FILE_PATH = cordova.getActivity().getExternalFilesDir().toString() + "/";
        //FILE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString() + "/";
    }


    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        this.requestArgs = args;

        try {
            Log.d(TAG, "ACTION: " + action);

            if (ACTION_START_RECORDING.equalsIgnoreCase(action)) {

                List<String> permissions = new ArrayList<String>();
                if (!cordova.hasPermission(android.Manifest.permission.CAMERA)) {
                    permissions.add(android.Manifest.permission.CAMERA);
                }
                if (permissions.size() > 0) {
                    cordova.requestPermissions(this, START_REQUEST_CODE, permissions.toArray(new String[0]));
                    return true;
                }

                StartRecording(this.requestArgs);
                return true;
            }
			
			 if (ACTION_START_PREVIEW.equalsIgnoreCase(action)) {

                List<String> permissions = new ArrayList<String>();
                if (!cordova.hasPermission(android.Manifest.permission.CAMERA)) {
                    permissions.add(android.Manifest.permission.CAMERA);
                }
                if (permissions.size() > 0) {
                    cordova.requestPermissions(this, START_REQUEST_CODE, permissions.toArray(new String[0]));
                    return true;
                }

                StartPreview(this.requestArgs);
                return true;
            }

			if (ACTION_STOP_RECORDING.equalsIgnoreCase(action)) {
                StopRecording();
                return true;
            }
			
            if (ACTION_STOP_PREVIEW.equalsIgnoreCase(action)) {
                Stop();
                return true;
            } else if (action.equals("execFFMPEG")) {
				try {
					this.execFFMPEG(args);
				} catch (IOException e) {
					callbackContext.error(e.toString());
				}
				return true;
			}

            callbackContext.error(TAG + ": INVALID ACTION");
            return false;
        } catch (Exception e) {
            Log.e(TAG, "ERROR: " + e.getMessage(), e);
            callbackContext.error(TAG + ": " + e.getMessage());
        }

        return true;
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        for (int r : grantResults) {
            if (r == PackageManager.PERMISSION_DENIED) {
                callbackContext.error("Camera Permission Denied");
                return;
            }
        }

        if (requestCode == START_REQUEST_CODE) {
            StartRecording(this.requestArgs);
        }
    }

    private void StartRecording(JSONArray args) throws JSONException {
        // params filename
        final String filename = args.getString(0);	

        if (videoOverlay == null) {
            videoOverlay = new VideoOverlay(cordova.getActivity()); //, getFilePath());
        }
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    videoOverlay.StartRecording(getFilePath(filename));
                    callbackContext.success();
                } catch (Exception e) {
                    e.printStackTrace();
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }
	
	private void StartPreview(JSONArray args) throws JSONException {
        // params camera, quality
        final String cameraFace = args.getString(0);

        if (videoOverlay == null) {
            videoOverlay = new VideoOverlay(cordova.getActivity()); //, getFilePath());

            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cordova.getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    try {
                        // Get screen dimensions
                        DisplayMetrics displaymetrics = new DisplayMetrics();
                        cordova.getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

                        // NOTE: GT-I9300 testing required wrapping view in relative layout for setAlpha to work.
                        RelativeLayout containerView = new RelativeLayout(cordova.getActivity());
                        containerView.addView(videoOverlay, new ViewGroup.LayoutParams(displaymetrics.widthPixels, displaymetrics.heightPixels));

                        cordova.getActivity().addContentView(containerView, new ViewGroup.LayoutParams(displaymetrics.widthPixels, displaymetrics.heightPixels));

                        webView.getView().setBackgroundColor(0x00000000);
                        ((ViewGroup)webView.getView()).bringToFront();
                    } catch (Exception e) {
                        Log.e(TAG, "Error during preview create", e);
                        callbackContext.error(TAG + ": " + e.getMessage());
                    }
                }
            });
        }

        videoOverlay.setCameraFacing(cameraFace);

        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    videoOverlay.StartPreview();
                    callbackContext.success();
                } catch (Exception e) {
                    e.printStackTrace();
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

    private void Stop() throws JSONException {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (videoOverlay != null) {
                    try {
                        videoOverlay.Stop();
                        callbackContext.success();
                    } catch (IOException e) {
                        e.printStackTrace();
                        callbackContext.error(e.getMessage());
                    }
                }
            }
        });
    }

	private void StopRecording() throws JSONException {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (videoOverlay != null) {
                    try {
                        String filepath = videoOverlay.StopRecording();
                        callbackContext.success(filepath);
                    } catch (IOException e) {
                        e.printStackTrace();
                        callbackContext.error(e.getMessage());
                    }
                }
            }
        });
    }
	
    private String getFilePath(String filename) {
        // Add number suffix if file exists
        int i = 1;
        String fileName = filename;
        while (new File(FILE_PATH + fileName + FILE_EXTENSION).exists()) {
            fileName = filename + '_' + i;
            i++;
        }
        return FILE_PATH + fileName + FILE_EXTENSION;
    }

    //Plugin Method Overrides
    @Override
    public void onPause(boolean multitasking) {
        if (videoOverlay != null) {
            try {
                this.Stop();
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage(), e);
                callbackContext.error(e.getMessage());
            }
        }
        super.onPause(multitasking);
    }

    @Override
    public void onDestroy() {
        try {
            this.Stop();
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        super.onDestroy();
    }
	
	/**
     * execFFMPEG
     *
     * Executes an ffmpeg command
     *
     * ARGUMENTS
     * =========
     *
     * cmd - ffmpeg command as a string array
     *
     * RESPONSE
     * ========
     *
     * VOID
     *
     * @param JSONArray args
     * @return void
     */
    private void execFFMPEG(JSONArray args) throws JSONException, IOException {
        Log.d(TAG, "execFFMPEG firing");

        // parse arguments
        JSONObject options = args.optJSONObject(0);

        Log.d(TAG, "options: " + options.toString());

        final JSONArray cmds = options.getJSONArray("cmd");

        // start task
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    File tempFile = File.createTempFile("ffmpeg", null, appContext.getCacheDir());
                    FfmpegController ffmpegController = new FfmpegController(cordova.getActivity().getApplicationContext(), tempFile);

                    ArrayList<String> al = new ArrayList<String>();
                    al.add(ffmpegController.getBinaryPath());

                    int cmdArrLength = cmds.length();
                    for (int i = 0; i < cmdArrLength; i++) {
                        al.add(cmds.optString(i));
                    }

                    ffmpegController.execFFMPEG(al, new ShellUtils.ShellCallback() {
                        @Override
                        public void shellOut(String shellLine) {
                            Log.d(TAG, "shellOut: " + shellLine);
                            try {
                                JSONObject jsonObj = new JSONObject();
                                jsonObj.put("progress", shellLine.toString());
                                PluginResult progressResult = new PluginResult(PluginResult.Status.OK, jsonObj);
                                progressResult.setKeepCallback(true);
                                callbackContext.sendPluginResult(progressResult);
                            } catch (JSONException e) {
                                Log.d(TAG, "PluginResult error: " + e);
                            }
                        }
                        @Override
                        public void processComplete(int exitValue) {}
                    });
                    Log.d(TAG, "ffmpeg finished");

                    callbackContext.success();
                } catch (Throwable e) {
                    Log.d(TAG, "ffmpeg exception ", e);
                    callbackContext.error(e.toString());
                }
            }
        });
    }

}
