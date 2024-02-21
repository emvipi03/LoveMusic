package com.medium.lovemusic.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.medium.lovemusic.R;
import com.medium.lovemusic.constant.Constant;
import com.medium.lovemusic.constant.GlobalFuntion;
import com.medium.lovemusic.databinding.FragmentPlaySongBinding;
import com.medium.lovemusic.model.Song;
import com.medium.lovemusic.service.MusicService;
import com.medium.lovemusic.utils.AppUtil;
import com.medium.lovemusic.utils.GlideUtils;

import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("NonConstantResourceId")
public class PlaySongFragment extends Fragment implements View.OnClickListener {

    private FragmentPlaySongBinding mFragmentPlaySongBinding;
    private Timer mTimer;
    private int mAction;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mAction = intent.getIntExtra(Constant.MUSIC_ACTION, 0);
            handleMusicAction();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mFragmentPlaySongBinding = FragmentPlaySongBinding.inflate(inflater, container, false);

        if (getActivity() != null) {
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver,
                    new IntentFilter(Constant.CHANGE_LISTENER));
        }
        initControl();
        showInforSong();
        mAction = MusicService.mAction;
        handleMusicAction();

        return mFragmentPlaySongBinding.getRoot();
    }

    private void initControl() {
        mTimer = new Timer();

        mFragmentPlaySongBinding.imgPrevious.setOnClickListener(this);
        mFragmentPlaySongBinding.imgPlay.setOnClickListener(this);
        mFragmentPlaySongBinding.imgNext.setOnClickListener(this);
        mFragmentPlaySongBinding.imgRepeat.setOnClickListener(this);

        mFragmentPlaySongBinding.seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                MusicService.mPlayer.seekTo(seekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }
        });
    }

    private void showInforSong() {
        if (MusicService.mListSongPlaying == null || MusicService.mListSongPlaying.isEmpty()) {
            return;
        }
        Song currentSong = MusicService.mListSongPlaying.get(MusicService.mSongPosition);
        mFragmentPlaySongBinding.tvSongName.setText(currentSong.getTitle());
        mFragmentPlaySongBinding.tvArtist.setText(currentSong.getArtist());
        GlideUtils.loadUrl(currentSong.getImage(), mFragmentPlaySongBinding.imgSong);
    }

    private void handleMusicAction() {
        if (Constant.CANNEL_NOTIFICATION == mAction) {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
            return;
        }
        switch (mAction) {
            case Constant.PREVIOUS:
            case Constant.NEXT:
                stopAnimationPlayMusic();
                showInforSong();
                break;

            case Constant.PLAY:
                showInforSong();
                if (MusicService.isPlaying) {
                    startAnimationPlayMusic();
                }
                showSeekBar();
                showStatusButtonPlay();
                break;

            case Constant.PAUSE:
                stopAnimationPlayMusic();
                showSeekBar();
                showStatusButtonPlay();
                break;

            case Constant.RESUME:
                startAnimationPlayMusic();
                showSeekBar();
                showStatusButtonPlay();
                break;

            case R.id.img_repeat: // Add this case
                clickOnRepeatButton();
                break;

            default:
                break;
        }
    }

    private void startAnimationPlayMusic() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mFragmentPlaySongBinding.imgSong.animate().rotationBy(360).withEndAction(this).setDuration(15000)
                        .setInterpolator(new LinearInterpolator()).start();
            }
        };
        mFragmentPlaySongBinding.imgSong.animate().rotationBy(360).withEndAction(runnable).setDuration(15000)
                .setInterpolator(new LinearInterpolator()).start();
    }

    private void stopAnimationPlayMusic() {
        mFragmentPlaySongBinding.imgSong.animate().cancel();
    }

    public void showSeekBar() {
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(() -> {
                    if (MusicService.mPlayer == null) {
                        return;
                    }
                    mFragmentPlaySongBinding.tvTimeCurrent.setText(AppUtil.getTime(MusicService.mPlayer.getCurrentPosition()));
                    mFragmentPlaySongBinding.tvTimeMax.setText(AppUtil.getTime(MusicService.mLengthSong));
                    mFragmentPlaySongBinding.seekbar.setMax(MusicService.mLengthSong);
                    mFragmentPlaySongBinding.seekbar.setProgress(MusicService.mPlayer.getCurrentPosition());
                });
            }
        }, 0, 1000);
    }

    private void showStatusButtonPlay() {
        if (MusicService.isPlaying) {
            mFragmentPlaySongBinding.imgPlay.setImageResource(R.drawable.ic_pause_black);
        } else {
            mFragmentPlaySongBinding.imgPlay.setImageResource(R.drawable.ic_play_black);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (getActivity() != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_previous:
                clickOnPrevButton();
                break;

            case R.id.img_play:
                clickOnPlayButton();
                break;

            case R.id.img_next:
                clickOnNextButton();
                break;

            case R.id.img_repeat:
                clickOnRepeatButton();
                break;

            default:
                break;
        }
    }

    private void clickOnPrevButton() {
        GlobalFuntion.startMusicService(getActivity(), Constant.PREVIOUS, MusicService.mSongPosition);
    }

    private void clickOnNextButton() {
        GlobalFuntion.startMusicService(getActivity(), Constant.NEXT, MusicService.mSongPosition);
    }

    public void clickOnRepeatButton() {
        GlobalFuntion.startMusicService(getActivity(), Constant.REPEAT, MusicService.mSongPosition);
        MusicService.toggleRepeatMode();

        // Update the UI based on the repeat mode
        updateRepeatButtonUI();
    }

    private void updateRepeatButtonUI() {
        int repeatMode = MusicService.getRepeatMode();
        switch (repeatMode) {
            case Constant.REPEAT_NONE:
                mFragmentPlaySongBinding.imgRepeat.setImageResource(R.drawable.ic_repeatoff);
                break;
            case Constant.REPEAT_ALL:
                mFragmentPlaySongBinding.imgRepeat.setImageResource(R.drawable.ic_repeaton);
                break;
            case Constant.REPEAT_ONE:
                mFragmentPlaySongBinding.imgRepeat.setImageResource(R.drawable.ic_repeatone);
                break;
            // Add more cases if needed
        }
    }

    private void clickOnPlayButton() {
        if (MusicService.isPlaying) {
            GlobalFuntion.startMusicService(getActivity(), Constant.PAUSE, MusicService.mSongPosition);
        } else {
            GlobalFuntion.startMusicService(getActivity(), Constant.RESUME, MusicService.mSongPosition);
        }
    }


}
