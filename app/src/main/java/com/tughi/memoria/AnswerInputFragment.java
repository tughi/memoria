package com.tughi.memoria;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class AnswerInputFragment extends PracticeFragment {

    private EditText answerEditText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.answer_input_fragment, container, false);

        TextView ratingTextView = (TextView) view.findViewById(R.id.rating);
        ratingTextView.setText(Exercises.getRatingText(getExerciseRating()));

        TextView questionTextView = (TextView) view.findViewById(R.id.question);
        questionTextView.setText(getExerciseDefinition());

        answerEditText = (EditText) view.findViewById(R.id.answer);
        answerEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onSubmit();
                }

                return false;
            }
        });

        return view;
    }

    private void onSubmit() {
        boolean correct = submitAnswer(answerEditText.getText().toString().trim());

        answerEditText.setEnabled(false);
        answerEditText.setBackgroundColor(getResources().getColor(correct ? R.color.correct : R.color.wrong));

        PracticeActivity activity = (PracticeActivity) getActivity();
        activity.continuePractice(correct ? PRACTICE_NORMAL : PRACTICE_DELAYED);
    }

}
