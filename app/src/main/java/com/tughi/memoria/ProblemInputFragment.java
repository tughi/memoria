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

public class ProblemInputFragment extends PracticeFragment {

    private EditText problemEditText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.problem_input_fragment, container, false);

        TextView solutionTextView = (TextView) view.findViewById(R.id.solution);
        solutionTextView.setText(getItemSolution());

        problemEditText = (EditText) view.findViewById(R.id.problem);
        problemEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
        boolean correct = submitAnswer(problemEditText.getText().toString().trim());

        problemEditText.setEnabled(false);
        problemEditText.setBackgroundColor(getResources().getColor(correct ? R.color.correct : R.color.wrong));

        MainActivity activity = (MainActivity) getActivity();
        activity.continuePractice(correct ? NEXT_PROBLEM_NORMAL : NEXT_PROBLEM_DELAYED);
    }

}
