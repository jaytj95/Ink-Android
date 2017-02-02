package com.paladin.ink;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DigitalClock;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TwoLineListItem;

import com.amnix.materiallockview.MaterialLockView;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.nineoldandroids.animation.Animator;
import com.rm.freedrawview.FreeDrawView;
import com.rm.freedrawview.PathDrawnListener;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import mehdi.sakout.fancybuttons.FancyButton;
import uz.shift.colorpicker.LineColorPicker;
import uz.shift.colorpicker.OnColorChangedListener;


public class ClockFragment extends Fragment {
    private static final int ANIM_SPEED = 350;

    Picture picture;
    ArrayList<Picture> photoList;
    enum Mode {DRAW, CLOCK, LOCK, SEND};
    private Mode mode;

    private OnClockFragmentInteractionListener mListener;

    public ClockFragment() {
        // Required empty public constructor
    }

    private float mDownX;
    private float mDownY;

//    InkView inkView;
    FreeDrawView inkView;
    RelativeLayout drawingView;
    RelativeLayout lockView;
    RelativeLayout selectUserView;
    RelativeLayout clockLayout;

    ImageView receivedImg;

    FancyButton colorButton;
    FancyButton undoButton;
    FancyButton drawButton;
    FancyButton sendButton;
    LineColorPicker colorPicker;

    TextClock clock;
    TextView textDate, statusText;

    ListView listView;
    UserAdapter listAdapter;
    String userId;
    Api inkApi;

    MaterialLockView patternView;

    boolean dismiss = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mode = Mode.CLOCK;
        SharedPreferences preferences = getActivity().getSharedPreferences("inklocksharedprefs", Context.MODE_PRIVATE);
        userId = preferences.getString("auth_key", null);
        assert userId != null;
        Log.d("INKLOCK", "Logged in as: " + userId);

        inkApi = new Api(getContext(), userId);
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_clock, container, false);

        statusText = (TextView) rootView.findViewById(R.id.status_text);
        statusText.setText(preferences.getString("username", "NOPE"));

        clock = (TextClock) rootView.findViewById(R.id.digitalClock);
        textDate = (TextView) rootView.findViewById(R.id.textDate);
        textDate.setText(Util.getDateString());

        photoList = new ArrayList<>();

        drawingView = (RelativeLayout) rootView.findViewById(R.id.drawingView);
        lockView = (RelativeLayout) rootView.findViewById(R.id.lock_layout);
        selectUserView = (RelativeLayout) rootView.findViewById(R.id.select_user_send);
        clockLayout = (RelativeLayout) rootView.findViewById(R.id.clockLayout);

        colorButton = (FancyButton) rootView.findViewById(R.id.colorButton);
        undoButton = (FancyButton) rootView.findViewById(R.id.undoButton);
        drawButton = (FancyButton) rootView.findViewById(R.id.drawButton);
        sendButton = (FancyButton) rootView.findViewById(R.id.sendButton);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToSelect();

            }
        });

        colorPicker = (LineColorPicker) rootView.findViewById(R.id.color_picker);
        colorPicker.setOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int i) {
                colorButton.setBackgroundColor(i);
                inkView.setPaintColor(i);
            }
        });



        inkView = (FreeDrawView) rootView.findViewById(R.id.ink);
        inkView.setPaintColor(colorPicker.getColor());
        inkView.setPaintWidthDp(2.5f);
        inkView.setBackground(null);
        inkView.setOnPathDrawnListener(new PathDrawnListener() {
            @Override
            public void onNewPathDrawn() {
                YoYo.with(Techniques.FadeIn).duration(350).playOn(colorButton);
                YoYo.with(Techniques.FadeIn).duration(350).playOn(colorPicker);
                YoYo.with(Techniques.FadeIn).duration(350).playOn(undoButton);
                YoYo.with(Techniques.FadeIn).duration(350).playOn(sendButton);
            }

            @Override
            public void onPathStart() {
                YoYo.with(Techniques.FadeOut).duration(350).playOn(colorButton);
                YoYo.with(Techniques.FadeOut).duration(350).playOn(colorPicker);
                YoYo.with(Techniques.FadeOut).duration(350).playOn(undoButton);
                YoYo.with(Techniques.FadeOut).duration(350).playOn(sendButton);
            }
        });

        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inkView.undoLast();
            }
        });
        drawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToDraw();
            }
        });

        receivedImg = (ImageView) rootView.findViewById(R.id.receivedImg);

        listView = (ListView) rootView.findViewById(R.id.listview);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User user = listAdapter.getItem(position);
                sendToUser(user.getId());
                Intent i = new Intent();

            }
        });
        listAdapter = new UserAdapter(getContext(), android.R.layout.simple_list_item_2);
//        if(friendsCached) {
//            addFriendsFromCache();
//        } else {
//            //ink api
//        }
        inkApi.getFriends(new Api.OnFriendsLoaded() {
            @Override
            public void onFriendsLoaded(ArrayList<User> friends) {
                listAdapter.addAll(friends);
            }
        });

        listView.setAdapter(listAdapter);


        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getUsersPics();
    }

    public void setLayoutOpacity(float offset) {
        clockLayout.setAlpha(offset);
    }

    private void sendToUser(final String uid) {
        inkView.getDrawScreenshot(new FreeDrawView.DrawCreatorListener() {
            @Override
            public void onDrawCreated(Bitmap bitmap) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 10, baos);
                byte[] data = baos.toByteArray();
                String encoded = Base64.encodeToString(data, Base64.DEFAULT);

                inkApi.sendPicture(userId, uid, encoded, new Api.OnPictureSent() {
                    @Override
                    public void onPictureSent(boolean success) {
                        if(success) {
                            switchToLock();
                            inkView.undoAll();
                        }
                    }
                });
            }

            @Override
            public void onDrawCreationError() {

            }
        });

    }


    private void getUsersPics() {
        inkApi.getPendingPics(new Api.OnPendingPicsLoaded() {
            @Override
            public void onPendingPicsLoaded(ArrayList<Picture> pictures) {
                photoList = pictures;
                if(!photoList.isEmpty()) {
                    picture = photoList.get(0);
                    Log.d("INKLOCK", picture.getId());
                    Picasso.with(getActivity()).load(picture.getUrl()).into(receivedImg);
                }
                statusText.setText(statusText.getText()+ " " + photoList.size());
            }
        });
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnClockFragmentInteractionListener) {
            mListener = (OnClockFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnClockFragmentInteractionListener {
        // TODO: Update argument type and name
        void onSwitchToDraw();
        void onSwitchToLock();
        void onSwitchToSelect();
        void onSwitchToClock();
    }

    private void switchToClock() {

    }

    public Mode getStatus() {
        return mode;
    }

    private void switchToSelect() {
        mode = Mode.SEND;
//        listAdapter.clear();
//        listAdapter.notifyDataSetChanged();
        YoYo.with(Techniques.ZoomOut).duration(ANIM_SPEED).withListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                drawingView.setVisibility(View.GONE);
                selectUserView.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.ZoomIn).duration(ANIM_SPEED).playOn(selectUserView);
                mListener.onSwitchToSelect();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).playOn(drawingView);
    }


    private void switchToDraw() {
        mode = Mode.DRAW;
        YoYo.with(Techniques.FadeOut).duration(ANIM_SPEED).withListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                lockView.setVisibility(View.GONE);
                drawingView.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeIn).duration(ANIM_SPEED).playOn(drawingView);
                mListener.onSwitchToDraw();
//                getUsersPics();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).playOn(lockView);
    }

    public void switchToLock() {
        inkView.undoAll();
        mode = Mode.LOCK;
        YoYo.with(Techniques.ZoomOut).duration(ANIM_SPEED).withListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                drawingView.setVisibility(View.GONE);
                selectUserView.setVisibility(View.GONE);
                lockView.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.ZoomIn).duration(ANIM_SPEED).playOn(lockView);
                mListener.onSwitchToLock();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).playOn(drawingView);

    }

    public void switchToView() {

    }
}
