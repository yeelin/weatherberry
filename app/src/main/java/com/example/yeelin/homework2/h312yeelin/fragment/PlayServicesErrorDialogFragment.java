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
 * PLAY_SERVICES_DIALOG_RESULT in onActivityResult() if you want to be notified that
 * play services are ready for use, or failed to install.
 */
public class PlayServicesErrorDialogFragment extends DialogFragment {

    public static final int PLAY_SERVICES_DIALOG_RESULT = 100;

    private static final String ARG_ERROR_CODE = PlayServicesErrorDialogFragment.class.getSimpleName() + ".errorCode";

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
    public static PlayServicesErrorDialogFragment newInstance(int errorCode) {
        PlayServicesErrorDialogFragment fragment = new PlayServicesErrorDialogFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_ERROR_CODE, errorCode);
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

        // All play services dialogs start activity for result from the activity, so the activity is
        // responsible for handling.
        return GooglePlayServicesUtil.getErrorDialog(errorCode, getActivity(), PLAY_SERVICES_DIALOG_RESULT);
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
