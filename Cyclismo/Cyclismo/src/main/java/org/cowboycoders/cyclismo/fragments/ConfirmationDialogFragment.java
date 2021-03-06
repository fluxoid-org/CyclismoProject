package org.cowboycoders.cyclismo.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import org.cowboycoders.cyclismo.util.DialogUtils;


public class ConfirmationDialogFragment extends DialogFragment {

  public static final String TAG = ConfirmationDialogFragment.class.getSimpleName();
  private static final String CALLBACK_KEY = "callbackKey";

  public interface DialogCallback extends Parcelable {
    void onConfirm(Context context);
    CharSequence getConfirmationMessage(Context context);
    void onFinish(Context context);
  }

  private FragmentActivity activity;
  private DialogCallback callback;
  private CharSequence confirmationMessage;

  @Override
  public void onAttach(Activity anActivity) {
    super.onAttach(anActivity);
    callback = getArguments().getParcelable(CALLBACK_KEY);
    confirmationMessage = callback.getConfirmationMessage(anActivity);
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    activity = getActivity();
    return DialogUtils.createConfirmationDialog(activity,
        confirmationMessage, new DialogInterface.OnClickListener() {

            @Override
          public void onClick(DialogInterface dialog, int which) {
            callback.onConfirm(activity);
            
            // Close the activity since its content can change after delete
            activity.finish();
            
            callback.onFinish(activity);

          }
        });
  }

  public static DialogFragment newInstance(DialogCallback callback) {
    if (callback == null) {
      throw new IllegalArgumentException("callback cannot be null");
    }
    Bundle bundle = new Bundle();
    bundle.putParcelable(CALLBACK_KEY, callback);
    ConfirmationDialogFragment confirmationDialogFragment = new ConfirmationDialogFragment();
    confirmationDialogFragment.setArguments(bundle);
    return confirmationDialogFragment;
  }

}
