package com.dikiy.workshop.code.sdks.facebook.samples;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.dikiy.workshop.R;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.widget.ProfilePictureView;

public class ProfileFragment extends Fragment {

    public static final String TAG = "ProfileFragment";

    private TextView userNameView;
    private ProfilePictureView profilePictureView;
    private OnOptionsItemSelectedListener onOptionsItemSelectedListener;

    private Profile pendingUpdateForUser;
    private CallbackManager callbackManager;
    private ProfileTracker profileTracker;
    private AccessTokenTracker accessTokenTracker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        callbackManager = CallbackManager.Factory.create();
        profileTracker =
                new ProfileTracker() {
                    @Override
                    protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                        setProfile(currentProfile);
                    }
                };

        accessTokenTracker =
                new AccessTokenTracker() {
                    @Override
                    protected void onCurrentAccessTokenChanged(
                            AccessToken oldAccessToken, AccessToken currentAccessToken) {
                        // On AccessToken changes fetch the new profile which fires the event on
                        // the ProfileTracker if the profile is different
                        Profile.fetchProfileForCurrentAccessToken();
                    }
                };

        // Ensure that our profile is up to date
        Profile.fetchProfileForCurrentAccessToken();
        setProfile(Profile.getCurrentProfile());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_switch_user, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        boolean handled = false;
        OnOptionsItemSelectedListener listener = onOptionsItemSelectedListener;

        if (listener != null)
            handled = listener.onOptionsItemSelected(item);

        if (!handled)
            handled = super.onOptionsItemSelected(item);

        return handled;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, parent, false);

        userNameView = view.findViewById(R.id.profileUserName);
        profilePictureView = view.findViewById(R.id.profilePic);

        if (pendingUpdateForUser != null) {
            setProfile(pendingUpdateForUser);
            pendingUpdateForUser = null;
        }

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        profileTracker.stopTracking();
        accessTokenTracker.stopTracking();
    }

    void setOnOptionsItemSelectedListener(OnOptionsItemSelectedListener listener) {
        this.onOptionsItemSelectedListener = listener;
    }

    private void setProfile(Profile profile) {
        if (userNameView == null || profilePictureView == null || !isAdded()) {
            // Fragment not yet added to the view. So let's store which user was intended
            // for display.
            pendingUpdateForUser = profile;
            return;
        }

        if (profile == null) {
            profilePictureView.setProfileId(null);
            userNameView.setText(R.string.facebook_greeting_no_user);
        } else {
            profilePictureView.setProfileId(profile.getId());
            userNameView.setText(String.format(getString(R.string.facebook_greeting_format), profile.getName()));
        }
    }

    public interface OnOptionsItemSelectedListener {
        boolean onOptionsItemSelected(MenuItem item);
    }
}
