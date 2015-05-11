package com.tughi.memoria;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class AnswerPickerFragment extends PracticeFragment implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    public static final String ARG_INVERT = "invert";

    private static final String[] EXERCISES_PROJECTION = {
            Exercises.COLUMN_ID,
            Exercises.COLUMN_SCOPE,
            Exercises.COLUMN_DEFINITION,
    };
    private static final int EXERCISE_ID = 0;
    private static final int EXERCISE_SCOPE = 1;
    private static final int EXERCISE_DEFINITION = 2;

    private boolean invert;
    private boolean loaded;

    private AnswerButton answer1Button;
    private AnswerButton answer2Button;
    private AnswerButton answer3Button;
    private AnswerButton answer4Button;

    private AnswerButton correctAnswerButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        invert = !getArguments().getBoolean(ARG_INVERT, false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.answer_picker_fragment, container, false);

        TextView ratingTextView = (TextView) view.findViewById(R.id.rating);
        ratingTextView.setText(getRatingText());

        TextView questionTextView = (TextView) view.findViewById(R.id.question);
        questionTextView.setText(getArguments().getString(invert ? Exercises.COLUMN_DEFINITION : Exercises.COLUMN_SCOPE));

        answer1Button = (AnswerButton) view.findViewById(R.id.answer_1);
        answer1Button.setOnClickListener(this);
        answer2Button = (AnswerButton) view.findViewById(R.id.answer_2);
        answer2Button.setOnClickListener(this);
        answer3Button = (AnswerButton) view.findViewById(R.id.answer_3);
        answer3Button.setOnClickListener(this);
        answer4Button = (AnswerButton) view.findViewById(R.id.answer_4);
        answer4Button.setOnClickListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), Exercises.CONTENT_URI, EXERCISES_PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (!loaded) {
            Random random = new Random();

            List<AnswerButton> answerButtons = new LinkedList<>();
            answerButtons.add(answer1Button);
            answerButtons.add(answer2Button);
            answerButtons.add(answer3Button);
            answerButtons.add(answer4Button);

            correctAnswerButton = answerButtons.remove(random.nextInt(answerButtons.size()));
            correctAnswerButton.setBackgroundResource(R.drawable.correct_picker_button);
            correctAnswerButton.setText(invert ? getExerciseScope() : getExerciseDefinition());
            correctAnswerButton.setTag(getExerciseScope());

            int count = cursor.getCount();
            while (!answerButtons.isEmpty()) {
                int wrongPosition = random.nextInt(count);
                if (cursor.moveToPosition(wrongPosition) && cursor.getLong(EXERCISE_ID) != getArguments().getLong(Exercises.COLUMN_ID)) {
                    AnswerButton answerButton = answerButtons.remove(random.nextInt(answerButtons.size()));
                    answerButton.setBackgroundResource(R.drawable.wrong_picker_button);
                    answerButton.setText(cursor.getString(invert ? EXERCISE_SCOPE : EXERCISE_DEFINITION));
                    answerButton.setTag(cursor.getString(EXERCISE_SCOPE));
                }
            }

            loaded = true;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // nothing to do
    }

    @Override
    public void onClick(View view) {
        final boolean correct = submitAnswer((String) view.getTag());
        if (!correct) {
            correctAnswerButton.setChecked(true);
        }

        answer1Button.setEnabled(false);
        answer2Button.setEnabled(false);
        answer3Button.setEnabled(false);
        answer4Button.setEnabled(false);

        ((MainActivity) getActivity()).continuePractice(correct ? PRACTICE_NORMAL : PRACTICE_DELAYED);
    }

}
