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

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nineoldandroids.animation.Animator;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
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

    private OnClockFragmentInteractionListener mListener;

    public ClockFragment() {
        // Required empty public constructor
    }

    private float mDownX;
    private float mDownY;

    InkView inkView;
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
    TextView textDate;

    ListView listView;
    UserAdapter listAdapter;
    String userId;
    Api inkApi;
    String[] pendingPics;

    boolean dismiss = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        SharedPreferences preferences = getActivity().getSharedPreferences("inklocksharedprefs", Context.MODE_PRIVATE);
        userId = preferences.getString("auth_key", null);
        assert userId != null;
        Log.d("INKLOCK", "Logged in as: " + userId);
        inkApi = new Api(getContext(), userId);
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_clock, container, false);

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
                inkView.setColor(i);
            }
        });



        inkView = (InkView) rootView.findViewById(R.id.ink);
        inkView.setColor(colorPicker.getColor());
        inkView.setMinStrokeWidth(1.5f);
        inkView.setMaxStrokeWidth(6f);

        inkView.addListener(new InkView.InkListener() {
            @Override
            public void onInkClear() {

            }

            @Override
            public void onInkDraw() {
                YoYo.with(Techniques.FadeOut).duration(350).playOn(colorButton);
                YoYo.with(Techniques.FadeOut).duration(350).playOn(colorPicker);
                YoYo.with(Techniques.FadeOut).duration(350).playOn(undoButton);
                YoYo.with(Techniques.FadeOut).duration(350).playOn(sendButton);
            }

            @Override
            public void onInkUp() {
                YoYo.with(Techniques.FadeIn).duration(350).playOn(colorButton);
                YoYo.with(Techniques.FadeIn).duration(350).playOn(colorPicker);
                YoYo.with(Techniques.FadeIn).duration(350).playOn(undoButton);
                YoYo.with(Techniques.FadeIn).duration(350).playOn(sendButton);
            }
        });

        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean b = inkView.undo();
                if (b) {
                    undoButton.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.holo_blue_bright));
                } else {
                    undoButton.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
                }
            }
        });
        drawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToDraw();
            }
        });


        final GestureDetector gdt = new GestureDetector(new GestureListener());
        receivedImg = (ImageView) rootView.findViewById(R.id.receivedImg);
        receivedImg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
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
                        picture = null;
                    }
                    //delete pic after viewing
                    if(picture != null) {
                        inkApi.deletePicture(picture.getId());
                    }
                    Toast.makeText(getActivity(), "UP AFTER VIEW", Toast.LENGTH_SHORT).show();
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
        User user = new User("58706219c798bb01c62d9c10", "jaytj95");
        listAdapter.add(user);
        listView.setAdapter(listAdapter);

        getUsersPics();

        return rootView;
    }

    private void sendToUser(final String uid) {
        Log.d("INKLOCK", "Sending");
        Bitmap bitmap = inkView.getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
        byte[] data = baos.toByteArray();
        String encoded = Base64.encodeToString(data, Base64.DEFAULT);


//        inkApi.sendPicture(userId, uid, encoded, new );

    }

    private void getUsersPics() {
        inkApi.getPendingPics(new Api.OnPendingPicsLoaded() {
            @Override
            public void onPendingPicsLoaded(ArrayList<Picture> pictures) {
                photoList = pictures;
                if(!photoList.isEmpty()) {
                    Log.d("INKLOCK", photoList.get(0).getId());
                    Picasso.with(getActivity()).load(photoList.get(0).getUrl()).into(receivedImg);
                }
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
    }



    private void switchToSelect() {
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
        YoYo.with(Techniques.ZoomOut).duration(ANIM_SPEED).withListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                lockView.setVisibility(View.GONE);
                drawingView.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.ZoomIn).duration(ANIM_SPEED).playOn(drawingView);
                mListener.onSwitchToDraw();
                getUsersPics();
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

    private class UserAdapter extends ArrayAdapter<User> {
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rootView = convertView;

            User user = getItem(position);

            TwoLineListItem twoLineListItem;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                twoLineListItem = (TwoLineListItem) inflater.inflate(
                        android.R.layout.simple_list_item_2, null);
            } else {
                twoLineListItem = (TwoLineListItem) convertView;
            }

            TextView text1 = twoLineListItem.getText1();
            TextView text2 = twoLineListItem.getText2();
            text1.setTextColor(ContextCompat.getColor(getContext(),android.R.color.white));
            text2.setTextColor(ContextCompat.getColor(getContext(),android.R.color.white));

            text1.setText(user.getName());
            text2.setText(user.getId());

            return twoLineListItem;

//            return rootView;
        }

        public UserAdapter(Context context, int resource) {
            super(context, resource);
        }
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
                Toast.makeText(getActivity(), "LONG PRESS", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(), "FLING", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
    }
}
