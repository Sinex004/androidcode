package com.knn.entfun.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.tts.TextToSpeech;

import androidx.annotation.NonNull;

import com.knn.entfun.helper.CircleImageView;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;

import com.knn.entfun.Constant;
import com.knn.entfun.R;
import com.knn.entfun.helper.AppController;
import com.knn.entfun.helper.CircleTimer;
import com.knn.entfun.helper.Session;
import com.knn.entfun.helper.Utils;
import com.knn.entfun.helper.TouchImageView;
import com.knn.entfun.model.Question;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class GamePlayActivity extends AppCompatActivity implements View.OnClickListener {
    public static TextView btnOpt1, btnOpt2, btnOpt3, btnOpt4, btnOpt5, txtQuestion, txtQuestion1,
            p2ans_a, p2ans_b, p2ans_c, p2ans_d, p2ans_e, tvPlayer1Name, tvPlayer2Name, btnQuitGame,
            txtQuestionIndex, option_a, option_b, option_c, option_d, option_e, txtTrueQuestion, txtFalseQuestion;
    public static ArrayList<String> options;
    public static Boolean virtual_play = false;
    static String roomKey = "";
    private final Handler mHandler = new Handler();
    public Question question;

    public Toolbar toolbar;
    public RelativeLayout playLayout;
    public Animation RightSwipe_A, RightSwipe_B, RightSwipe_C, RightSwipe_D, RightSwipe_E, Fade_in;
    public DatabaseReference databaseReference;

    CircleImageView imgPlayer1, imgPlayer2;
    DatabaseReference myGameRef;
    ImageView imgZoom, imgMic;
    boolean isPlayStarted = false, player1GameStatus, player2GameStatus;

    RelativeLayout layout_A, layout_B, layout_C, layout_D, layout_E;
    ArrayList<Question> questionList;
    TouchImageView imgQuestion;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    ProgressBar imgProgress, rightProgress, wrongProgress;

    private Context mContext;
    private Animation animation;
    public CircleTimer circleTimer;

    public long leftTime;
    public MyCountDownTimer timer;
    public AlertDialog quitAlertDialog;
    public TextSwitcher right_p1, right_p2, right_p01, right_p02;
    public Animation in, out;
    public TextToSpeech textToSpeech;
    public ProgressBar progressBar;
    public RelativeLayout mainLayout;
    public int questionIndex = 0, btnPosition = 0, correctQuestion = 0, inCorrectQuestion = 0,
            questionIndex_vplayer = 0, correctQuestion_vplayer = 0, inCorrectQuestion_vplayer = 0,
            click = 0, textSize, preScore = 0;
    public String userId1, userId2, Player1Name, Player2Name, Player1UserID, Player2UserID, gameId, winner, winnerMessage,
            player1Key = "", player2Key = "", battlePlayer, optionClicked = "false",
            tts = "tts", profilePlayer1, profilePlayer2, winDialogTitle, pauseCheck = "regular";

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game_play);
        mContext = GamePlayActivity.this;
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        gameId = getIntent().getStringExtra("gameid");
        battlePlayer = getIntent().getStringExtra("battlePlayer");
        roomKey = gameId;
        myGameRef = FirebaseDatabase.getInstance().getReference().child(Constant.DB_GAME_ROOM);
        init();
        final int[] CLICKABLE = new int[]{R.id.a_layout, R.id.b_layout, R.id.c_layout, R.id.d_layout, R.id.e_layout};

        for (int i : CLICKABLE) {
            findViewById(i).setOnClickListener(this);
        }
        InitializeTTF();
        progressBar = findViewById(R.id.progressBar);
        txtQuestionIndex = findViewById(R.id.tvIndex);
        right_p1 = findViewById(R.id.right_p1);
        right_p2 = findViewById(R.id.right_p2);
        right_p01 = findViewById(R.id.right_p01);
        right_p02 = findViewById(R.id.right_p02);

        p2ans_a = findViewById(R.id.p2ans_a);
        p2ans_b = findViewById(R.id.p2ans_b);
        p2ans_c = findViewById(R.id.p2ans_c);
        p2ans_d = findViewById(R.id.p2ans_d);
        p2ans_e = findViewById(R.id.p2ans_e);


        imgProgress = findViewById(R.id.imgProgress);
        rightProgress = findViewById(R.id.rightProgress);
        wrongProgress = findViewById(R.id.wrongProgress);
        imgQuestion = findViewById(R.id.imgQuestion);

        btnOpt1 = findViewById(R.id.btnOpt1);
        btnOpt2 = findViewById(R.id.btnOpt2);
        btnOpt3 = findViewById(R.id.btnOpt3);
        btnOpt4 = findViewById(R.id.btnOpt4);
        btnOpt5 = findViewById(R.id.btnOpt5);

        option_a = findViewById(R.id.option_a);
        option_b = findViewById(R.id.option_b);
        option_c = findViewById(R.id.option_c);
        option_d = findViewById(R.id.option_d);
        option_e = findViewById(R.id.option_e);


        imgZoom = findViewById(R.id.imgZoom);
        imgMic = findViewById(R.id.imgMic);

        mainLayout = findViewById(R.id.main_layout);
        tvPlayer1Name = findViewById(R.id.tv_player1_name);
        tvPlayer2Name = findViewById(R.id.tv_player2_name);
        imgPlayer1 = findViewById(R.id.iv_player1_pic);
        imgPlayer2 = findViewById(R.id.iv_player2_pic);

        btnQuitGame = findViewById(R.id.btn_quit);
        imgPlayer1.setDefaultImageResId(R.drawable.ic_account);
        imgPlayer2.setDefaultImageResId(R.drawable.ic_android);
        textSize = Integer.valueOf(Session.getSavedTextSize(GamePlayActivity.this));
        Session.removeSharedPreferencesData(GamePlayActivity.this);

        RightSwipe_A = AnimationUtils.loadAnimation(mContext, R.anim.anim_right_a);
        RightSwipe_B = AnimationUtils.loadAnimation(mContext, R.anim.anim_right_b);
        RightSwipe_C = AnimationUtils.loadAnimation(mContext, R.anim.anim_right_c);
        RightSwipe_D = AnimationUtils.loadAnimation(mContext, R.anim.anim_right_d);
        RightSwipe_E = AnimationUtils.loadAnimation(mContext, R.anim.anim_right_e);
        Fade_in = AnimationUtils.loadAnimation(mContext, R.anim.fade_out);


        playLayout = findViewById(R.id.innerLayout);
        playLayout.setVisibility(View.GONE);

        txtTrueQuestion = findViewById(R.id.txtTrueQuestion);
        txtTrueQuestion.setText("0");
        txtFalseQuestion = findViewById(R.id.txtFalseQuestion);
        txtFalseQuestion.setText("0");


        txtQuestion = findViewById(R.id.txtQuestion);
        txtQuestion1 = findViewById(R.id.txtQuestion1);
        layout_A = findViewById(R.id.a_layout);
        layout_B = findViewById(R.id.b_layout);
        layout_C = findViewById(R.id.c_layout);
        layout_D = findViewById(R.id.d_layout);
        layout_E = findViewById(R.id.e_layout);


        ChangeTextSize(textSize);
        circleTimer = findViewById(R.id.circleTimer);
        circleTimer.setMaxProgress(Constant.CIRCULAR_MAX_PROGRESS);
        circleTimer.setCurrentProgress(Constant.CIRCULAR_MAX_PROGRESS);


        animation = AnimationUtils.loadAnimation(GamePlayActivity.this, R.anim.right_ans_anim); // Change alpha from fully visible
        animation.setDuration(500); // duration - half a second
        animation.setInterpolator(new LinearInterpolator()); // do not alter
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation
        animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the


        rightProgress.setMax(Constant.MAX_QUESTION_PER_BATTLE);
        wrongProgress.setMax(Constant.MAX_QUESTION_PER_BATTLE);


        if (Utils.isNetworkAvailable(GamePlayActivity.this)) {

            if (battlePlayer.equals(getString(R.string.robot))) {
                questionList = GetOpponentActivity.questionArrayList;
            } else {
                questionList = GetOpponentActivity.battleQuestionList;
            }
            playLayout.setVisibility(View.VISIBLE);

        } else {
            View parentLayout = findViewById(android.R.id.content);
            Snackbar snackbar = Snackbar
                    .make(parentLayout, getString(R.string.msg_no_internet), Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.retry), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    });
            snackbar.show();

        }
        btnQuitGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();

            }
        });


        in = AnimationUtils.loadAnimation(this, R.anim.slide_up1);
        out = AnimationUtils.loadAnimation(this, R.anim.slide_up);


        right_p1.setFactory(mFactory);
        right_p01.setFactory(mFactory);
        right_p2.setFactory(mFactory);
        right_p02.setFactory(mFactory);

        right_p1.setCurrentText(String.valueOf(correctQuestion));
        right_p01.setCurrentText(String.valueOf(inCorrectQuestion));
        right_p2.setCurrentText(String.valueOf(correctQuestion));
        right_p02.setCurrentText(String.valueOf(inCorrectQuestion));

        right_p1.setInAnimation(in);
        right_p1.setOutAnimation(out);

        right_p01.setOutAnimation(out);
        right_p2.setInAnimation(in);
        right_p2.setOutAnimation(out);

        right_p02.setOutAnimation(out);
        imgMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textToSpeech.speak(questionList.get(questionIndex).getQuestion(), TextToSpeech.QUEUE_FLUSH, null);
                tts = "ttsCall";
            }
        });

    }

    public void InitializeTTF() {
        textToSpeech = new TextToSpeech(GamePlayActivity.this, new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Constant.ttsLanguage);
                    textToSpeech.setSpeechRate(1f);
                    textToSpeech.setPitch(1.1f);

                }
            }
        });
    }

    private final Runnable mUpdateUITimerTask = new Runnable() {
        public void run() {
            progressBar.setVisibility(View.GONE);
            mainLayout.setVisibility(View.VISIBLE);
            nextQuizQuestion();
        }
    };

    private void init() {
        Player1UserID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        Player2UserID = getIntent().getStringExtra("opponentId");
        userId1 = getIntent().getStringExtra("user_id1");
        userId2 = getIntent().getStringExtra("user_id2");

        databaseReference = FirebaseDatabase.getInstance().getReference().child(Constant.DB_USER);
        databaseReference.child(Player1UserID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Player1Name = dataSnapshot.child(Constant.USER_NAME).getValue().toString();
                            player1Key = Player1UserID;
                            tvPlayer1Name.setText(Player1Name);
                            profilePlayer1 = dataSnapshot.child(Constant.PROFILE_PIC).getValue().toString();
                            imgPlayer1.setImageUrl(profilePlayer1, imageLoader);

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
        databaseReference.child(Player2UserID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Player2Name = dataSnapshot.child(Constant.USER_NAME).getValue().toString();
                            profilePlayer2 = dataSnapshot.child(Constant.PROFILE_PIC).getValue().toString();
                            tvPlayer2Name.setText(Player2Name);
                            player2Key = Player2UserID;
                            imgPlayer2.setImageUrl(profilePlayer2, imageLoader);
                            virtual_play = false;

                        } else {

                            virtual_play = true;
                            Player2Name = getString(R.string.robot);
                            tvPlayer2Name.setText(Player2Name);
                            player2Key = Player2UserID;
                            imgPlayer2.setDefaultImageResId(R.drawable.ic_android);

                        }

                        p2ans_a.setText(Player2Name);
                        p2ans_b.setText(Player2Name);
                        p2ans_c.setText(Player2Name);
                        p2ans_d.setText(Player2Name);
                        p2ans_e.setText(Player2Name);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        myGameRef.child(gameId).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {

                        player1Key = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                        player2Key = getIntent().getStringExtra("opponentId");

                        if (dataSnapshot.child(player1Key).getValue() != null)
                            player1GameStatus = (boolean) dataSnapshot.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("status").getValue();
                        if (dataSnapshot.child(player2Key).getValue() != null)
                            player2GameStatus = (boolean) dataSnapshot.child(getIntent().getStringExtra("opponentId")).child("status").getValue();
                        if (player1GameStatus && player2GameStatus) {
                            isPlayStarted = true;
                        }
                        if (isPlayStarted) {
                            if (!player1GameStatus || !player2GameStatus) {
                                showOtherUserQuitDialog();
                            } else {


                                int p1_que = dataSnapshot.child(player1Key).child(Constant.QUE_NO).getValue(Integer.class);
                                int p2_que = dataSnapshot.child(player2Key).child(Constant.QUE_NO).getValue(Integer.class);

                                if (p1_que == p2_que) {

                                    final int r_2 = dataSnapshot.child(player2Key).child(Constant.RIGHT).getValue(Integer.class);
                                    final int r_1 = dataSnapshot.child(player1Key).child(Constant.RIGHT).getValue(Integer.class);

                                    // String p1_sel = dataSnapshot.child(player1Key).child("sel_ans").getValue(String.class);
                                    String p2_sel = dataSnapshot.child(player2Key).child(Constant.SEL_ANS).getValue(String.class);


                                    if (btnOpt1.getText().toString().equalsIgnoreCase(p2_sel)) {
                                        p2ans_a.setVisibility(View.VISIBLE);

                                        assert p2_sel != null;
                                        if (p2_sel.equalsIgnoreCase(question.getTrueAns().trim())) {
                                            p2ans_a.setTextColor(getResources().getColor(R.color.wrong_dark));
                                        } else {
                                            p2ans_a.setTextColor(getResources().getColor(R.color.right_dark));
                                        }

                                    } else if (btnOpt2.getText().toString().equalsIgnoreCase(p2_sel)) {
                                        p2ans_b.setVisibility(View.VISIBLE);
                                        assert p2_sel != null;
                                        if (p2_sel.equalsIgnoreCase(question.getTrueAns().trim())) {
                                            p2ans_b.setTextColor(getResources().getColor(R.color.wrong_dark));
                                        } else {
                                            p2ans_b.setTextColor(getResources().getColor(R.color.right_dark));
                                        }

                                    } else if (btnOpt3.getText().toString().equalsIgnoreCase(p2_sel)) {
                                        p2ans_c.setVisibility(View.VISIBLE);
                                        assert p2_sel != null;
                                        if (p2_sel.equalsIgnoreCase(question.getTrueAns().trim())) {
                                            p2ans_c.setTextColor(getResources().getColor(R.color.wrong_dark));
                                        } else {
                                            p2ans_c.setTextColor(getResources().getColor(R.color.right_dark));
                                        }

                                    } else if (btnOpt4.getText().toString().equalsIgnoreCase(p2_sel)) {
                                        p2ans_d.setVisibility(View.VISIBLE);
                                        assert p2_sel != null;
                                        if (p2_sel.equalsIgnoreCase(question.getTrueAns().trim())) {
                                            p2ans_d.setTextColor(getResources().getColor(R.color.wrong_dark));
                                        } else {
                                            p2ans_d.setTextColor(getResources().getColor(R.color.right_dark));
                                        }

                                    } else if (btnOpt5.getText().toString().equalsIgnoreCase(p2_sel)) {
                                        p2ans_e.setVisibility(View.VISIBLE);
                                        assert p2_sel != null;
                                        if (p2_sel.equalsIgnoreCase(question.getTrueAns().trim())) {
                                            p2ans_e.setTextColor(getResources().getColor(R.color.wrong_dark));
                                        } else {
                                            p2ans_e.setTextColor(getResources().getColor(R.color.right_dark));
                                        }

                                    }
                                    if (r_2 == 10) {
                                        right_p02.setText("");
                                    }

                                    if (preScore != r_2) {
                                        right_p2.setText(String.valueOf(r_2));
                                        preScore = r_2;
                                    }
                                    ///right_p2.setText(String.valueOf(r_2));

                                    // right_p02.setText(String.valueOf(dataSnapshot.child(player2Key).child("wrong").getValue()));


                                    if (p1_que == Constant.MAX_QUESTION_PER_BATTLE) {

                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {

                                                if (r_1 > r_2) {
                                                    winnerMessage = Player1Name + getString(R.string.msg_win_battle);
                                                    winner = "you";
                                                    winDialogTitle = getString(R.string.congrats);

                                                    showWinnerDialog();
                                                    if (!battlePlayer.equals("robot"))
                                                        SetBattleStatistics("0", userId1);

                                                } else if (r_2 > r_1) {
                                                    winnerMessage = Player2Name + getString(R.string.msg_opponent_win_battle);
                                                    winner = Player2Name;
                                                    winDialogTitle = getString(R.string.next_time);

                                                    showWinnerDialog();
                                                    if (!battlePlayer.equals("robot"))
                                                        SetBattleStatistics("0", userId2);

                                                } else {
                                                    showResetGameAlert();
                                                    if (!battlePlayer.equals("robot"))
                                                        SetBattleStatistics("1", "");

                                                }
                                                //  btnQuitGame.setText("GO BACK");
                                            }
                                        }, 2000);

                                    } else
                                        mHandler.postDelayed(mUpdateUITimerTask, 1000);

                                }

                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    public void SetBattleStatistics(final String isDraw, final String winnerId) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.QUIZ_URL,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(Constant.accessKey, Constant.accessKeyValue);
                params.put(Constant.SET_BATTLE_STATISTICS, "1");
                params.put(Constant.USER_ID1, userId1);
                params.put(Constant.USER_ID2, userId2);
                params.put(Constant.WINNER_ID, winnerId);
                params.put(Constant.IS_DRAWN, isDraw);
                System.out.println("===params " + params);
                return params;
            }
        };
        AppController.getInstance().getRequestQueue().getCache().clear();
        AppController.getInstance().addToRequestQueue(stringRequest);
    }

    @Override
    public void onClick(View v) {
        if (questionIndex < questionList.size()) {
            question = questionList.get(questionIndex);
            layout_A.setClickable(false);
            layout_B.setClickable(false);
            layout_C.setClickable(false);
            layout_D.setClickable(false);
            layout_E.setClickable(false);

            option_a.setVisibility(View.VISIBLE);
            option_b.setVisibility(View.VISIBLE);
            option_c.setVisibility(View.VISIBLE);
            option_d.setVisibility(View.VISIBLE);
            option_e.setVisibility(View.VISIBLE);


            switch (v.getId()) {
                case R.id.a_layout:
                    AddReview(question, btnOpt1, layout_A);
                    break;

                case R.id.b_layout:
                    AddReview(question, btnOpt2, layout_B);

                    break;
                case R.id.c_layout:
                    AddReview(question, btnOpt3, layout_C);

                    break;
                case R.id.d_layout:
                    AddReview(question, btnOpt4, layout_D);

                    break;
                case R.id.e_layout:
                    AddReview(question, btnOpt5, layout_E);

                    break;
            }

            if (virtual_play) {
                PerformVirtualClick();
            }

            optionClicked = "true";

        }
    }

    public void AddReview(Question question, TextView tvBtnOpt, RelativeLayout layout) {
        layout_A.setClickable(false);
        layout_B.setClickable(false);
        layout_C.setClickable(false);
        layout_D.setClickable(false);
        layout_E.setClickable(false);

        if (tvBtnOpt.getText().toString().equalsIgnoreCase(question.getTrueAns())) {
            layout.setBackgroundResource(R.drawable.right_gradient);
            correctQuestion = correctQuestion + 1;
            rightProgress.setProgress(correctQuestion);
            txtTrueQuestion.setText(String.valueOf(correctQuestion));
            addScore(tvBtnOpt.getText().toString().trim());

        } else {
            layout.setBackgroundResource(R.drawable.wrong_gradient);
            inCorrectQuestion = inCorrectQuestion + 1;
            wrongProgress.setProgress(inCorrectQuestion);
            txtFalseQuestion.setText(String.valueOf(inCorrectQuestion));
            WrongQuestion(tvBtnOpt.getText().toString().trim());
        }

        question.setSelectedAns(tvBtnOpt.getText().toString());
        RightAnswerBackgroundSet();
        question.setAttended(true);
        questionIndex++;

    }

    public void RightAnswerBackgroundSet() {
        if (btnOpt1.getText().toString().equalsIgnoreCase(question.getTrueAns())) {
            layout_A.setBackgroundResource(R.drawable.right_gradient);
            layout_A.startAnimation(animation);

        } else if (btnOpt2.getText().toString().equalsIgnoreCase(question.getTrueAns())) {
            layout_B.setBackgroundResource(R.drawable.right_gradient);
            layout_B.startAnimation(animation);

        } else if (btnOpt3.getText().toString().equalsIgnoreCase(question.getTrueAns())) {
            layout_C.setBackgroundResource(R.drawable.right_gradient);
            layout_C.startAnimation(animation);

        } else if (btnOpt4.getText().toString().equalsIgnoreCase(question.getTrueAns())) {
            layout_D.setBackgroundResource(R.drawable.right_gradient);
            layout_D.startAnimation(animation);
        } else if (btnOpt5.getText().toString().equalsIgnoreCase(question.getTrueAns())) {
            layout_E.setBackgroundResource(R.drawable.right_gradient);
            layout_E.startAnimation(animation);
        }
    }

    private void showOtherUserQuitDialog() {
        if (timer != null) {
            timer.cancel();
        }
        DatabaseReference databaseReference = myGameRef.child(gameId);
        if (databaseReference != null) {
            databaseReference.removeValue();
        }
        try {

            final AlertDialog.Builder dialog = new AlertDialog.Builder(GamePlayActivity.this);
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View dialogView = inflater.inflate(R.layout.dialog_reset_game, null);
            dialog.setView(dialogView);
            dialog.setCancelable(false);
            quitAlertDialog = dialog.create();

            TextView tvMessage = dialogView.findViewById(R.id.tv_message);
            TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
            tvTitle.setText(Player1Name);
            TextView btnok = dialogView.findViewById(R.id.btn_ok);

            tvMessage.setText("You Win!! \n" + Player2Name + getString(R.string.leave_battle_txt));
            btnok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                    DistroyKey(roomKey);
                    quitAlertDialog.dismiss();
                    if (GetOpponentActivity.battleQuestionList != null)
                        GetOpponentActivity.battleQuestionList.clear();
                }
            });


            Objects.requireNonNull(quitAlertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

            quitAlertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    System.out.println("connected");
                } else {
                    System.out.println("not connected");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Listener was cancelled");
            }
        });
    }

    private void showQuitGameAlertDialog() {
        try {
            if (timer != null) {
                timer.cancel();
            }
            final AlertDialog.Builder dialog1 = new AlertDialog.Builder(GamePlayActivity.this);
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View dialogView = inflater.inflate(R.layout.dialog_leave_battle, null);
            dialog1.setView(dialogView);
            dialog1.setCancelable(true);

            final AlertDialog alertDialog = dialog1.create();
            TextView tvMessage = dialogView.findViewById(R.id.tv_message);
            TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
            tvTitle.setText(Player1Name);
            TextView btnok = dialogView.findViewById(R.id.btn_ok);
            TextView btnNo = dialogView.findViewById(R.id.btnNo);
            tvMessage.setText(getString(R.string.msg_alert_leave));
            btnok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    myGameRef.child(gameId).child(FirebaseAuth.getInstance().getUid()).child(Constant.STATUS).setValue(false);
                    finish();
                    DistroyKey(roomKey);
                    if (GetOpponentActivity.battleQuestionList != null)
                        GetOpponentActivity.battleQuestionList.clear();
                }
            });
            btnNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    timer = new MyCountDownTimer(leftTime, 1000);
                    timer.start();
                    alertDialog.dismiss();
                }
            });
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            alertDialog.setCancelable(false);
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void DistroyKey(final String roomKey) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.QUIZ_URL,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            JSONObject jsonObject = new JSONObject(response);
                            String error = jsonObject.getString(Constant.ERROR);

                            if (error.equalsIgnoreCase("false")) {
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }) {


            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(Constant.accessKey, Constant.accessKeyValue);
                params.put(Constant.getRandomQuestion, "1");
                params.put(Constant.GAME_ROOM_KEY, roomKey);
                params.put(Constant.DESTROY_GAME_KEY, "1");

                return params;
            }
        };
        AppController.getInstance().getRequestQueue().getCache().clear();
        AppController.getInstance().addToRequestQueue(stringRequest);
    }

    private void showWinnerDialog() {
        try {


            if (timer != null) {
                timer.cancel();
            }
            DatabaseReference databaseReference = myGameRef.child(gameId);
            if (databaseReference != null) {
                databaseReference.removeValue();
            }
            final AlertDialog.Builder dialog1 = new AlertDialog.Builder(GamePlayActivity.this);
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View dialogView = inflater.inflate(R.layout.winner_dialog, null);
            dialog1.setView(dialogView);
            dialog1.setCancelable(false);
            DistroyKey(roomKey);
            final AlertDialog alertDialog = dialog1.create();
            TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
            TextView tvMessage = dialogView.findViewById(R.id.tv_message);
            Button btnok = dialogView.findViewById(R.id.btn_ok);
            Button btnReBattle = dialogView.findViewById(R.id.btnReBattle);
            NetworkImageView winnerImg = dialogView.findViewById(R.id.winnerImg);
            if (winner.equals("you")) {
                tvTitle.setText(getString(R.string.congrats));
                tvMessage.setText(winnerMessage);
                winnerImg.setImageUrl(profilePlayer1, imageLoader);
            } else {
                tvTitle.setText(getString(R.string.next_time));
                tvMessage.setText(winnerMessage);
                winnerImg.setImageUrl(profilePlayer2, imageLoader);
            }

            btnok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                    alertDialog.dismiss();

                }
            });

            btnReBattle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intentReBattle = new Intent(GamePlayActivity.this, GetOpponentActivity.class);
                    startActivity(intentReBattle);
                    finish();
                    alertDialog.dismiss();
                }
            });
            alertDialog.show();
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void PerformVirtualClick() {
        String option;
        if (Session.getBoolean(Session.E_MODE, getApplicationContext())) {
            option = randomAlphaNumericWith_E();
        } else {
            option = randomAlphaNumeric();
        }
        switch (option) {
            case "A":

                if (btnOpt1.getText().toString().trim().equalsIgnoreCase(question.getTrueAns().trim()))
                    RightVirtualAnswer(btnOpt1.getText().toString().trim());
                else if (!btnOpt1.getText().toString().trim().equalsIgnoreCase(question.getTrueAns().trim()))
                    WrongVirtualAnswer(btnOpt1.getText().toString().trim());

                break;
            case "B":

                if (btnOpt2.getText().toString().trim().equalsIgnoreCase(question.getTrueAns().trim()))
                    RightVirtualAnswer(btnOpt2.getText().toString().trim());
                else if (!btnOpt2.getText().toString().trim().equalsIgnoreCase(question.getTrueAns().trim()))
                    WrongVirtualAnswer(btnOpt2.getText().toString().trim());

                break;
            case "C":

                if (btnOpt3.getText().toString().trim().equalsIgnoreCase(question.getTrueAns().trim()))
                    RightVirtualAnswer(btnOpt3.getText().toString().trim());
                else if (!btnOpt3.getText().toString().trim().equalsIgnoreCase(question.getTrueAns().trim()))
                    WrongVirtualAnswer(btnOpt3.getText().toString().trim());

                break;
            case "D":

                if (btnOpt4.getText().toString().trim().equalsIgnoreCase(question.getTrueAns().trim()))
                    RightVirtualAnswer(btnOpt4.getText().toString().trim());
                else if (!btnOpt4.getText().toString().trim().equalsIgnoreCase(question.getTrueAns().trim()))
                    WrongVirtualAnswer(btnOpt4.getText().toString().trim());

                break;
            case "E":

                if (btnOpt5.getText().toString().trim().equalsIgnoreCase(question.getTrueAns().trim()))
                    RightVirtualAnswer(btnOpt5.getText().toString().trim());
                else if (!btnOpt5.getText().toString().trim().equalsIgnoreCase(question.getTrueAns().trim()))
                    WrongVirtualAnswer(btnOpt5.getText().toString().trim());

                break;
        }
    }


    public void RightVirtualAnswer(final String sel_ans) {
        questionIndex_vplayer++;
        correctQuestion_vplayer++;
        Map<String, Object> taskMap = new HashMap<String, Object>();
        taskMap.put(Constant.RIGHT, correctQuestion_vplayer);
        taskMap.put(Constant.QUE_NO, questionIndex_vplayer);
        taskMap.put(Constant.SEL_ANS, sel_ans);
        myGameRef.child(gameId).child(Player2UserID).updateChildren(taskMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Write was successful!
                        System.out.println("success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Write failed
                        Map<String, Object> taskMap = new HashMap<String, Object>();
                        taskMap.put(Constant.RIGHT, correctQuestion_vplayer);
                        taskMap.put(Constant.QUE_NO, questionIndex_vplayer);
                        taskMap.put(Constant.SEL_ANS, sel_ans);
                        myGameRef.child(gameId).child(Player2UserID).updateChildren(taskMap);
                    }
                });


    }


    public void WrongVirtualAnswer(final String sel_ans) {

        inCorrectQuestion_vplayer++;
        questionIndex_vplayer++;
        Map<String, Object> taskMap = new HashMap<String, Object>();
        taskMap.put(Constant.WRONG, inCorrectQuestion_vplayer);
        taskMap.put(Constant.QUE_NO, questionIndex_vplayer);
        taskMap.put(Constant.SEL_ANS, sel_ans);

        myGameRef.child(gameId).child(Player2UserID).updateChildren(taskMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Write was successful!
                        System.out.println("success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Write failed
                        Map<String, Object> taskMap = new HashMap<String, Object>();
                        taskMap.put(Constant.WRONG, inCorrectQuestion_vplayer);
                        taskMap.put(Constant.QUE_NO, questionIndex_vplayer);
                        taskMap.put(Constant.SEL_ANS, sel_ans);
                        myGameRef.child(gameId).child(Player2UserID).updateChildren(taskMap);
                    }
                });


    }

    private void showResetGameAlert() {
        DatabaseReference databaseReference = myGameRef.child(gameId);
        if (databaseReference != null) {
            databaseReference.removeValue();
        }
        try {
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_reset_game);
            dialog.setCancelable(false);
            TextView tvMessage = dialog.findViewById(R.id.tv_message);
            TextView btnok = dialog.findViewById(R.id.btn_ok);
            tvMessage.setText(getString(R.string.msg_draw_game));
            btnok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                    dialog.dismiss();
                }
            });
            dialog.show();
            Window window = dialog.getWindow();
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void ChangeTextSize(int size) {

        if (btnOpt1 != null)
            btnOpt1.setTextSize(size);
        if (btnOpt2 != null)
            btnOpt2.setTextSize(size);
        if (btnOpt3 != null)
            btnOpt3.setTextSize(size);
        if (btnOpt4 != null)
            btnOpt4.setTextSize(size);
        if (btnOpt5 != null)
            btnOpt5.setTextSize(size);
        if (txtQuestion != null)
            txtQuestion.setTextSize(size);
        if (txtQuestion1 != null)
            txtQuestion1.setTextSize(size);
    }

    public String randomAlphaNumeric() {

        String option = "ABCD";
        int character = (int) (Math.random() * 4);
        return String.valueOf(option.charAt(character));
    }

    public String randomAlphaNumericWith_E() {

        String option = "ABCDE";
        int character = (int) (Math.random() * 5);
        return String.valueOf(option.charAt(character));
    }

    private ViewSwitcher.ViewFactory mFactory = new ViewSwitcher.ViewFactory() {

        @Override
        public View makeView() {

            // Create a new TextView
            TextView t = new TextView(GamePlayActivity.this);
            t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            t.setTextAppearance(GamePlayActivity.this, android.R.style.TextAppearance_Large);
            t.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
            return t;
        }
    };


    public class MyCountDownTimer extends CountDownTimer {

        private MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            leftTime = millisUntilFinished;

            int progress = (int) (millisUntilFinished / 1000);

            if (circleTimer == null) {
                circleTimer = (CircleTimer) findViewById(R.id.circleTimer);
            } else {
                circleTimer.setCurrentProgress(progress);
            }
            //when left last 5 second we show progress color red
            if (millisUntilFinished <= 6000) {
                circleTimer.SetTimerAttributes(Color.RED, Color.RED);
            } else {
                circleTimer.SetTimerAttributes(Color.parseColor(Constant.PROGRESS_COLOR), Color.parseColor(Constant.PROGRESS_COLOR));
            }
        }

        @Override
        public void onFinish() {
            if (questionIndex >= Constant.MAX_QUESTION_PER_BATTLE) {

            } else {

                //WrongQuestion();
                if (optionClicked.equals("false")) {
                    layout_A.setClickable(false);
                    layout_B.setClickable(false);
                    layout_C.setClickable(false);
                    layout_D.setClickable(false);
                    layout_E.setClickable(false);
                    if (virtual_play) {
                        WrongVirtualAnswer("wrong");
                    }
                    WrongQuestion("wrong");


                    questionIndex++;
                    //mHandler.postDelayed(mUpdateUITimerTask, 1000);

                }
            }

        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {

        super.onStop();
        UpdateOnlineStatus();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (tts.equals("tts")) {
            if (pauseCheck.equals("regular"))
                UpdateOnlineStatus();
        }
    }

    @Override
    protected void onDestroy() {

        if (timer != null) {
            timer.cancel();
        }
        if (textToSpeech != null) {
            textToSpeech.shutdown();
        }
        if (quitAlertDialog != null) {
            if (quitAlertDialog.isShowing()) {
                quitAlertDialog.dismiss();
            }
        }
        UpdateOnlineStatus();
        super.onDestroy();

    }

    public void UpdateOnlineStatus() {
        final DatabaseReference databaseReference = myGameRef.child(gameId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    if (data.exists()) {
                        //do ur stuff
                        databaseReference.child(FirebaseAuth.getInstance().getUid()).child(Constant.STATUS).setValue(false);
                        finish();
                    } else {
                        //do something if not exists
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });

    }

    private void addScore(final String sel_ans) {


        rightSound();
        if (correctQuestion == 10) {
            right_p01.setText("");

        }
        right_p1.setText(String.valueOf(correctQuestion));
        Map<String, Object> taskMap = new HashMap<String, Object>();
        taskMap.put(Constant.RIGHT, correctQuestion);
        taskMap.put(Constant.QUE_NO, (questionIndex + 1));
        taskMap.put(Constant.SEL_ANS, sel_ans);

        myGameRef.child(gameId).child(Player1UserID).updateChildren(taskMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Write was successful!
                        System.out.println("success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Write failed
                        Map<String, Object> taskMap = new HashMap<String, Object>();
                        taskMap.put(Constant.RIGHT, correctQuestion);
                        taskMap.put(Constant.QUE_NO, (questionIndex + 1));
                        taskMap.put(Constant.SEL_ANS, sel_ans);
                        myGameRef.child(gameId).child(Player1UserID).updateChildren(taskMap);
                    }
                });


    }

    private void WrongQuestion(String sel_ans) {
        setAgain();
        playWrongSound();

        Map<String, Object> taskMap = new HashMap<String, Object>();
        taskMap.put(Constant.WRONG, inCorrectQuestion);
        taskMap.put(Constant.QUE_NO, (questionIndex + 1));
        taskMap.put(Constant.SEL_ANS, sel_ans);

        myGameRef.child(gameId).child(Player1UserID).updateChildren(taskMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Write was successful!
                        System.out.println("success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Write failed
                        Map<String, Object> taskMap = new HashMap<String, Object>();
                        taskMap.put(Constant.WRONG, inCorrectQuestion);
                        taskMap.put(Constant.QUE_NO, (questionIndex + 1));

                        myGameRef.child(gameId).child(Player1UserID).updateChildren(taskMap);
                    }
                });

    }

    /*
     * Save score in Preferences
     */
    //play sound when answer is correct
    public void rightSound() {
        if (Session.getSoundEnableDisable(mContext)) {
            Utils.setrightAnssound(mContext);
        }
        if (Session.getVibration(mContext)) {
            Utils.vibrate(mContext, Utils.VIBRATION_DURATION);
        }
    }

    //play sound when answer is incorrect
    private void playWrongSound() {
        if (Session.getSoundEnableDisable(mContext)) {
            Utils.setwronAnssound(mContext);
        }
        if (Session.getVibration(mContext)) {
            Utils.vibrate(mContext, Utils.VIBRATION_DURATION);
        }
    }

    //set progress again after next question
    private void setAgain() {

        p2ans_a.setVisibility(View.GONE);
        p2ans_b.setVisibility(View.GONE);
        p2ans_c.setVisibility(View.GONE);
        p2ans_d.setVisibility(View.GONE);
        p2ans_e.setVisibility(View.GONE);


    }

    private void nextQuizQuestion() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
        optionClicked = "false";
        tts = "tts";

        setAgain();

        if (timer != null) {
            timer.cancel();
        }
        timer = new MyCountDownTimer(Constant.TIME_PER_QUESTION, Constant.COUNT_DOWN_TIMER);
        if (timer != null) {
            timer.cancel();
            timer.start();
        } else {
            timer.start();
        }
        layout_A.setBackgroundResource(R.drawable.answer_bg);
        layout_B.setBackgroundResource(R.drawable.answer_bg);
        layout_C.setBackgroundResource(R.drawable.answer_bg);
        layout_D.setBackgroundResource(R.drawable.answer_bg);
        layout_E.setBackgroundResource(R.drawable.answer_bg);
        layout_A.clearAnimation();
        layout_B.clearAnimation();
        layout_C.clearAnimation();
        layout_D.clearAnimation();
        layout_E.clearAnimation();


        layout_A.setClickable(true);
        layout_B.setClickable(true);
        layout_C.setClickable(true);
        layout_D.setClickable(true);
        layout_E.setClickable(true);
        btnOpt1.startAnimation(RightSwipe_A);
        btnOpt2.startAnimation(RightSwipe_B);
        btnOpt3.startAnimation(RightSwipe_C);
        btnOpt4.startAnimation(RightSwipe_D);
        btnOpt5.startAnimation(RightSwipe_E);
        txtQuestion1.startAnimation(Fade_in);
        if (questionIndex < questionList.size()) {
            question = questionList.get(questionIndex);
            int temp = questionIndex;
            imgQuestion.resetZoom();
            txtQuestionIndex.setText(++temp + "");
            if (!question.getImage().isEmpty()) {
                imgZoom.setVisibility(View.VISIBLE);
                txtQuestion1.setVisibility(View.VISIBLE);
                txtQuestion.setVisibility(View.GONE);

                imgQuestion.setImageUrl(question.getImage(), imageLoader);
                imgQuestion.setVisibility(View.VISIBLE);
                imgProgress.setVisibility(View.GONE);
                imgZoom.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        click++;
                        if (click == 1)
                            imgQuestion.setZoom(1.25f);
                        else if (click == 2)
                            imgQuestion.setZoom(1.50f);
                        else if (click == 3)
                            imgQuestion.setZoom(1.75f);
                        else if (click == 4) {
                            imgQuestion.setZoom(2.00f);
                            click = 0;
                        }
                    }
                });
            } else {
                imgZoom.setVisibility(View.GONE);
                imgQuestion.setVisibility(View.GONE);
                txtQuestion1.setVisibility(View.GONE);
                txtQuestion.setVisibility(View.VISIBLE);
            }


            System.out.println("img Question " + question.getImage());
            txtQuestion.setText(Html.fromHtml(question.getQuestion()));
            txtQuestion1.setText(Html.fromHtml(question.getQuestion()));
            options = new ArrayList<String>();
            options.addAll(question.getOptions());
            Collections.shuffle(options);
            if (Session.getBoolean(Session.E_MODE, getApplicationContext())) {
                if (options.size() == 4)
                    layout_E.setVisibility(View.GONE);
                else
                    layout_E.setVisibility(View.VISIBLE);
            }

            btnOpt1.setText(Html.fromHtml(options.get(0).trim()));
            btnOpt2.setText(Html.fromHtml(options.get(1).trim()));
            btnOpt3.setText(Html.fromHtml(options.get(2).trim()));
            btnOpt4.setText(Html.fromHtml(options.get(3).trim()));
            if (Session.getBoolean(Session.E_MODE, getApplicationContext()))
                if (options.size() == 5)
                    btnOpt5.setText(Html.fromHtml(options.get(4).trim()));

        }


    }

    public void CheckSound() {
        if (Session.getSoundEnableDisable(mContext)) {
            Utils.backSoundonclick(mContext);
        }
        if (Session.getVibration(mContext)) {
            Utils.vibrate(mContext, Utils.VIBRATION_DURATION);
        }
    }

    @Override
    public void onBackPressed() {
        showQuitGameAlertDialog();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.bookmark).setVisible(false);
        menu.findItem(R.id.report).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.setting:

                pauseCheck = "setting";
                Intent intent = new Intent(GamePlayActivity.this, SettingActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.open_next, R.anim.close_next);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
