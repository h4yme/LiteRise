package com.example.literise.adapters;



import android.view.LayoutInflater;

import android.view.View;

import android.view.ViewGroup;

import android.view.animation.Animation;

import android.view.animation.AnimationUtils;

import android.widget.ImageView;

import androidx.annotation.NonNull;

import androidx.recyclerview.widget.RecyclerView;



import com.example.literise.R;



public class IntroSlideAdapter extends RecyclerView.Adapter<IntroSlideAdapter.IntroViewHolder> {



    private final int[] layouts = {

            R.layout.intro_slide_1,

            R.layout.intro_slide_2,

            R.layout.intro_slide_3,

            R.layout.intro_slide_4,

            R.layout.intro_slide_5

    };



    @NonNull

    @Override

    public IntroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())

                .inflate(layouts[viewType], parent, false);

        return new IntroViewHolder(view);

    }



    @Override

    public void onBindViewHolder(@NonNull IntroViewHolder holder, int position) {

        // Add bounce animation to mascot

        ImageView mascot = holder.itemView.findViewById(R.id.ivMascot);

        if (mascot != null) {

            Animation bounceAnim = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.bounce);

            Animation floatAnim = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.float_up_down);

            mascot.startAnimation(bounceAnim);



            // Start floating animation after bounce completes

            bounceAnim.setAnimationListener(new Animation.AnimationListener() {

                @Override

                public void onAnimationStart(Animation animation) {}



                @Override

                public void onAnimationEnd(Animation animation) {

                    mascot.startAnimation(floatAnim);

                }



                @Override

                public void onAnimationRepeat(Animation animation) {}

            });

        }

    }



    @Override

    public int getItemCount() {

        return layouts.length;

    }



    @Override

    public int getItemViewType(int position) {

        return position;

    }



    static class IntroViewHolder extends RecyclerView.ViewHolder {

        IntroViewHolder(@NonNull View itemView) {

            super(itemView);

        }

    }

}