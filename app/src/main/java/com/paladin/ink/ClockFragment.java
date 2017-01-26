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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 */
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
        patternView = (MaterialLockView) rootView.findViewById(R.id.pattern);
        patternView.setOnPatternListener(new MaterialLockView.OnPatternListener() {
            @Override
            public void onPatternDetected(List<MaterialLockView.Cell> pattern, String SimplePattern) {
                Log.d("INKLOCK", SimplePattern);
                //should find a way to store a password originally.
                //right now, checking for typical Android password.
                if (!SimplePattern.equals("24589")) {
                    patternView.clearPattern();
                } else {
                    patternView.setDisplayMode(MaterialLockView.DisplayMode.Correct);
                    getActivity().finish();
                }
                super.onPatternDetected(pattern, SimplePattern);
            }

            @Override
            public void onPatternStart() {
                super.onPatternStart();
            }
        });

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
//        inkView.setOnPathDrawnListener(new PathDrawnListener() {
//            @Override
//            public void onNewPathDrawn() {
//
//            }
//        });
//        inkView.addListener(new InkView.InkListener() {
//            @Override
//            public void onInkClear() {
//
//            }
//
//            @Override
//            public void onInkDraw() {
//                YoYo.with(Techniques.FadeOut).duration(350).playOn(colorButton);
//                YoYo.with(Techniques.FadeOut).duration(350).playOn(colorPicker);
//                YoYo.with(Techniques.FadeOut).duration(350).playOn(undoButton);
//                YoYo.with(Techniques.FadeOut).duration(350).playOn(sendButton);
//            }
//
//            @Override
//            public void onInkUp() {
//                YoYo.with(Techniques.FadeIn).duration(350).playOn(colorButton);
//                YoYo.with(Techniques.FadeIn).duration(350).playOn(colorPicker);
//                YoYo.with(Techniques.FadeIn).duration(350).playOn(undoButton);
//                YoYo.with(Techniques.FadeIn).duration(350).playOn(sendButton);
//            }
//        });

        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inkView.undoLast();
//                if (b) {
//                    undoButton.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.holo_blue_bright));
//                } else {
//                    undoButton.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
//                }
            }
        });
        drawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToDraw();
            }
        });


        final GestureDetector gdt = new GestureDetector(getActivity(), new GestureListener());
        receivedImg = (ImageView) rootView.findViewById(R.id.receivedImg);
        receivedImg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("INKLOCK", "touch");
                if(event.getAction() == MotionEvent.ACTION_UP && dismiss) {
                    receivedImg.clearAnimation();
                    dismiss = false;
                    clockLayout.setVisibility(View.VISIBLE);
                    YoYo.with(Techniques.FadeIn).duration(350).playOn(clockLayout);
                    if(photoList.size() > 0) {
                        picture = photoList.get(0);
                        Picasso.with(getContext()).load(picture.getUrl()).into(receivedImg);
                        photoList.remove(0);
                    } else {
                        receivedImg.setImageBitmap(null);
                    }
                    //delete pic after viewing
                    if(picture != null) {
                        Log.d("INKLOCK", "Deleting " + picture.getId());
                        inkApi.deletePicture(picture.getId());
                        picture = null;
                    }
                } else {
                    gdt.onTouchEvent(event);
                }
                return true;
            }
        });
//        receivedImg.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                switch(event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//
//                }
//                return false;
//            }
//        });
//        receivedImg.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(dismiss) {
//                    dismiss = false;
//                    clockLayout.setVisibility(View.VISIBLE);
//                    YoYo.with(Techniques.FadeIn).duration(350).playOn(clockLayout);
//                    if(photoList.size() > 0) {
//                        picture = photoList.get(0);
//                        Picasso.with(getContext()).load(picture.getUrl()).into(receivedImg);
//                        photoList.remove(0);
//                    } else {
//                        receivedImg.setImageBitmap(null);
//                        picture = null;
//                    }
//                    //delete pic after viewing
//                    if(picture != null) {
//                        inkApi.deletePicture(picture.getId());
//                    }
//                }
//
//            }
//        });
//        receivedImg.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                YoYo.with(Techniques.FadeOut).duration(350).withListener(new Animator.AnimatorListener() {
//                    @Override
//                    public void onAnimationStart(Animator animation) {
//
//                    }
//
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        clockLayout.setVisibility(View.GONE);
//                    }
//
//                    @Override
//                    public void onAnimationCancel(Animator animation) {
//
//                    }
//
//                    @Override
//                    public void onAnimationRepeat(Animator animation) {
//
//                    }
//                }).playOn(clockLayout);
//                dismiss = true;
//
//                return false;
//            }
//        });


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

//        listAdapter.add(user);
        listView.setAdapter(listAdapter);


        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getUsersPics();
    }

    private void sendToUser(final String uid) {
        Log.d("INKLOCK", "Sending");
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
    private void switchToLock() {
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

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_MIN_DISTANCE = 120;
        private static final int SWIPE_THRESHOLD_VELOCITY = 200;


        @Override
        public void onLongPress(MotionEvent e) {
            YoYo.with(Techniques.FadeOut).duration(350).withListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        clockLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).playOn(clockLayout);
                dismiss = true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean didFling = false;
            if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                didFling = true;

//                return false; // Right to left
            }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                didFling = true;
//                return false; // Left to right
            }

            if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                didFling = true;
//                return false; // Bottom to top
            }  else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                didFling = true;
//                return false; // Top to bottom
            }
            if(didFling) {
                //unlock action
//                switchToLock();
                Toast.makeText(getActivity(), "Updating...", Toast.LENGTH_SHORT).show();
//                getUsersPics();

            }
            return false;
        }
    }
}
