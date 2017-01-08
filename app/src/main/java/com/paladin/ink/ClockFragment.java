package com.paladin.ink;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
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

    private void sendToUser(final String uid) {
        Log.d("INKLOCK", "Sending");
    }
    boolean dismiss = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        SharedPreferences preferences = getActivity().getSharedPreferences("inklocksharedprefs", Context.MODE_PRIVATE);
        userId = preferences.getString("auth_key", null);
        assert userId != null;
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

        receivedImg = (ImageView) rootView.findViewById(R.id.receivedImg);
        receivedImg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                }
                return false;
            }
        });
        receivedImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dismiss) {
                    dismiss = false;
                    clockLayout.setVisibility(View.VISIBLE);
                    YoYo.with(Techniques.FadeIn).duration(350).playOn(clockLayout);
                    photoList.remove(0);
                    if(photoList.size() > 0) {
                        picture = photoList.get(0);
                        Picasso.with(getContext()).load(picture.getUrl()).into(receivedImg);
                    } else {
                        receivedImg.setImageBitmap(null);
                    }
                }

            }
        });
        receivedImg.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
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

                return false;
            }
        });


        listView = (ListView) rootView.findViewById(R.id.listview);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User user = listAdapter.getItem(position);
                sendToUser(user.getId());

            }
        });
        listAdapter = new UserAdapter(getContext(), android.R.layout.simple_list_item_2);
        listView.setAdapter(listAdapter);

        getUsersPics();

        return rootView;
    }

    private void getUsersPics() {
        inkApi.getPendingPics(new Api.OnPendingPicsLoaded() {
            @Override
            public void onPendingPicsLoaded(ArrayList<Picture> pictures) {
                photoList = pictures;
                Picasso.with(getActivity()).load(photoList.get(0).getUrl()).into(receivedImg);
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
        listAdapter.clear();
        listAdapter.notifyDataSetChanged();
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
}
