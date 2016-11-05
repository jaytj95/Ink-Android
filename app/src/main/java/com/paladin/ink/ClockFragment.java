package com.paladin.ink;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.nineoldandroids.animation.Animator;

import uz.shift.colorpicker.LineColorPicker;
import uz.shift.colorpicker.OnColorChangedListener;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 */
public class ClockFragment extends Fragment {
    private static final int ANIM_SPEED = 350;

    private OnClockFragmentInteractionListener mListener;

    public ClockFragment() {
        // Required empty public constructor
    }

    InkView inkView;
    RelativeLayout drawingView;
    RelativeLayout lockView;

    Button colorButton;
    Button undoButton;
    Button drawButton;
    LineColorPicker colorPicker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_clock, container, false);

        drawingView = (RelativeLayout) rootView.findViewById(R.id.drawingView);
        lockView = (RelativeLayout) rootView.findViewById(R.id.lock_layout);

        colorButton = (Button) rootView.findViewById(R.id.colorButton);
        undoButton = (Button) rootView.findViewById(R.id.undoButton);
        drawButton = (Button) rootView.findViewById(R.id.drawButton);

        colorPicker = (LineColorPicker) rootView.findViewById(R.id.color_picker);
        colorPicker.setOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int i) {
                colorButton.setBackgroundColor(i);
                inkView.setColor(i);
            }
        });
//        colorPicker.setOnColorChangeListener(new VerticalSlideColorPicker.OnColorChangeListener() {
//            @Override
//            public void onColorChange(int i) {
//                colorButton.setBackgroundColor(i);
//                inkView.setColor(i);
//            }
//        });

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
            }

            @Override
            public void onInkUp() {
                YoYo.with(Techniques.FadeIn).duration(350).playOn(colorButton);
                YoYo.with(Techniques.FadeIn).duration(350).playOn(colorPicker);
                YoYo.with(Techniques.FadeIn).duration(350).playOn(undoButton);
            }
        });

        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean b = inkView.undo();
            }
        });
        drawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToDraw();
            }
        });

        return rootView;
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
}
