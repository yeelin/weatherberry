package com.example.yeelin.homework2.h312yeelin.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Created by ninjakiki on 5/13/15.
 * A wrapper around the play services dialog. The activity is required to handle the
 * REQUEST_CODE_PLAY_SERVICES_RESOLUTION in onActivityResult() if you want to be notified that
 * play services are ready for use, or failed to install.
 */
public class PlayServicesErrorDialogFragment extends DialogFragment {

    private static final String ARG_ERROR_CODE = PlayServicesErrorDialogFragment.class.getSimpleName() + ".errorCode";
    private static final String ARG_REQUEST_CODE = PlayServicesErrorDialogFragment.class.getSimpleName() + ".requestCode";

    //listener member variable
    private PlayServicesErrorDialogFragmentListener listener;

    /**
     * Interface for callbacks from this fragment.
     */
    public static interface PlayServicesErrorDialogFragmentListener {
        public void onPlayServicesErrorDialogCancelled();
    }

    /**
     * Use this to instantiate the fragment
     * @param errorCode
     * @return
     */
    public static PlayServicesErrorDialogFragment newInstance(int errorCode, int requestCode) {
        PlayServicesErrorDialogFragment fragment = new PlayServicesErrorDialogFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_ERROR_CODE, errorCode);
        args.putInt(ARG_REQUEST_CODE, requestCode);
        fragment.setArguments(args);

        return fragment;
    }

    public PlayServicesErrorDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Fragment parent = getParentFragment();
        Object objectToCast = parent != null ? parent : activity;
        try {
            listener = (PlayServicesErrorDialogFragmentListener) objectToCast;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(objectToCast.getClass().getSimpleName()
                    + " must implement PlayServicesDialogFragmentListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        int errorCode = args.getInt(ARG_ERROR_CODE);
        int requestCode = args.getInt(ARG_REQUEST_CODE);

        // All play services dialogs start activity for result from the activity, so the activity is
        // responsible for handling.
        //check for the result in onActivityResult() in the activity class
        return GooglePlayServicesUtil.getErrorDialog(errorCode, getActivity(), requestCode);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        // Listener may be null during rotation... So ignore that case.
        if (listener != null) {
            listener.onPlayServicesErrorDialogCancelled();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
