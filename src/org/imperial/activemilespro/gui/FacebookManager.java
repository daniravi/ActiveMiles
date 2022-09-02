package org.imperial.activemilespro.gui;

import java.util.ArrayList;
import java.util.Arrays;

import org.imperial.activemilespro.R;
import org.imperial.activemilespro.interface_utility.IntDrowableContainer;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.ShareApi;
import com.facebook.share.Sharer;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

public class FacebookManager extends SlidingFragmentActivity implements IntDrowableContainer {
    private static final String TAG = "FacebookManager";
    private static final String[] READ_PERMISSION = {"user_friends", "user_photos", "user_posts"};
    private static final String[] PUBL_PERMISSION = {"publish_actions"};
    private ImageView profilePictureView;
    private Profile profile;
    private TextView greeting;
    TextView facebookButon;
    Bitmap MyPhoto;
    private DownloadImageFromHttp myDownloadImageFromHttp;
    CallbackManager callbackManager;
    private ProfileTracker profileTracker;
    private ProgressDialog progress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(this.getApplicationContext());

        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                updateUI();

            }

            @Override
            public void onCancel() {
                showAlert();
                updateUI();
            }

            @Override
            public void onError(FacebookException exception) {
                if (exception instanceof FacebookAuthorizationException) {
                    showAlert();

                }
                updateUI();
            }

            private void showAlert() {
                new AlertDialog.Builder(FacebookManager.this).setTitle(R.string.cancelled).setMessage(R.string.permission_not_granted).setPositiveButton(R.string.ok, null).show();
            }
        });

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                profile = currentProfile;
                updateUI();
                onSessionStateChange();
                facebookStatusIsChanged("Login");

            }
        };
        progress = new ProgressDialog(this);

    }

    void openProgressBar() {

        Handler h = new Handler();
        h.post(new Runnable() {
            @Override
            public void run() {
                progress.setMessage("Upload data:");
                progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progress.setIndeterminate(true);
                progress.show();
            }
        });

        final int totalProgressTime = 100;


        Thread progresBarTrhead = new Thread() {

            @Override
            public void run() {
                int jumpTime = 0;
                while (jumpTime < totalProgressTime) {
                    try {
                        sleep(200);
                        jumpTime += 5;
                        progress.setProgress(jumpTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        };
        progresBarTrhead.start();


    }


    void closeProgressBar() {

        Handler h = new Handler();
        h.post(new Runnable() {
            @Override
            public void run() {
                progress.hide();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeProgressBar();
        progress.dismiss();
        if (myDownloadImageFromHttp != null)
            myDownloadImageFromHttp.cancel(true);
        profileTracker.stopTracking();
    }

    private void updateUI() {
        boolean enableButtons = AccessToken.getCurrentAccessToken() != null;
        profilePictureView.setBackgroundResource(R.drawable.empty_profile);
        if (enableButtons && profile != null) {
            if (myDownloadImageFromHttp != null)
                myDownloadImageFromHttp.cancel(true);
            myDownloadImageFromHttp = new DownloadImageFromHttp(this);
            myDownloadImageFromHttp.execute("https://graph.facebook.com/" + profile.getId() + "/picture?type=large", 0, 0);
            greeting.setText(getString(R.string.hello_user, profile.getFirstName()));

        } else {
            greeting.setText(null);
        }
    }

    boolean hasPermission() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null && accessToken.getPermissions().contains("publish_actions");
    }

    void obtainPermission() {
        LoginManager.getInstance().logInWithPublishPermissions(this, Arrays.asList(PUBL_PERMISSION));
    }

    private void onSessionStateChange() {

        boolean enableButtons = AccessToken.getCurrentAccessToken() != null;
        facebookButon = (TextView) this.findViewById(R.id.facebookButton);
        LinearLayout login_button_layout = (LinearLayout) this.findViewById(R.id.login_button_layout);
        if (enableButtons && profile != null) {
            if (facebookButon != null)
                facebookButon.setVisibility(View.VISIBLE);
            if (login_button_layout != null)
                login_button_layout.setVisibility(View.GONE);

        } else {
            if (facebookButon != null)
                facebookButon.setVisibility(View.GONE);
            if (login_button_layout != null)
                login_button_layout.setVisibility(View.VISIBLE);
        }

    }

    protected void initializeFacebook() {
        profile = Profile.getCurrentProfile();
        onSessionStateChange();
        profilePictureView = (ImageView) findViewById(R.id.profilePicture);
        greeting = (TextView) findViewById(R.id.greeting);

        LoginButton authButton = (LoginButton) findViewById(R.id.login_button);
        authButton.setReadPermissions(Arrays.asList(READ_PERMISSION));
        if (!hasPermission())
            obtainPermission();
    }

    private void facebookStatusIsChanged(String state) {
        Intent intent = new Intent("FacebookStatusChange");
        intent.putExtra("message", state);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void pictureIsDownloaded(String name) {
        Intent intent = new Intent("FacebookPictureDownload");
        intent.putExtra("Name", name);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void saveImage(int position, int activity, Bitmap MyPhoto) {
        this.MyPhoto = MyPhoto;
        if (profile != null) {
            pictureIsDownloaded(profile.getName());
            profilePictureView.setBackground(new BitmapDrawable(getResources(), MyPhoto));
        }
    }

    void postPhoto(Bitmap snapshotHolder, FacebookCallback<Sharer.Result> sharePhotoCallback) {
        SharePhoto sharePhoto = new SharePhoto.Builder().setBitmap(snapshotHolder).setUserGenerated(true).build();
        ArrayList<SharePhoto> photos = new ArrayList<>();
        photos.add(sharePhoto);
        SharePhotoContent sharePhotoContent = new SharePhotoContent.Builder().setPhotos(photos).build();
        ShareApi.share(sharePhotoContent, sharePhotoCallback);
    }

    final FacebookCallback<Sharer.Result> defaultCallBack = new FacebookCallback<Sharer.Result>() {
        @Override
        public void onCancel() {
            Log.d(TAG, "Canceled");
            closeProgressBar();
        }

        @Override
        public void onError(FacebookException error) {
            Log.d(TAG, String.format("Error: %s", error.toString()));
            String title = getString(R.string.error);
            String alertMessage = error.getMessage();
            showResult(title, alertMessage);
            closeProgressBar();
        }

        @Override
        public void onSuccess(Sharer.Result result) {
            Log.d(TAG, "Success!");
            Toast.makeText(getApplicationContext(), "Image Shared on Facebook!", Toast.LENGTH_LONG).show();
            closeProgressBar();
        }

        private void showResult(String title, String alertMessage) {
            new AlertDialog.Builder(FacebookManager.this).setTitle(title).setMessage(alertMessage).setPositiveButton(R.string.ok, null)
                    .show();
        }
    };
}
