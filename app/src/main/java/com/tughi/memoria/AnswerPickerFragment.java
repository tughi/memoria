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

    private boolean invert;
    private boolean loaded;

    private AnswerButton answer1Button;
    private AnswerButton answer2Button;
    private AnswerButton answer3Button;
    private AnswerButton answer4Button;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        invert = !getArguments().getBoolean(ARG_INVERT, false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.answer_picker_fragment, container, false);

        PracticeExercise exercise = getExercise();

        TextView questionTextView = (TextView) view.findViewById(R.id.question);
        questionTextView.setText(invert ? exercise.definition : exercise.scope);

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

        getLoaderManager().initLoader(100, null, this);
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

            PracticeExercise exercise = getExercise();

            AnswerButton answerButton = answerButtons.remove(random.nextInt(answerButtons.size()));
            answerButton.setBackgroundResource(R.drawable.correct_picker_button);
            answerButton.setText(invert ? exercise.scope : exercise.definition);
            answerButton.setTag(R.id.answer_button_solution, true);
            answerButton.setTag(R.id.answer_button_exercise, exercise);

            int count = cursor.getCount();
            while (!answerButtons.isEmpty()) {
                int wrongPosition = random.nextInt(count);
                if (cursor.moveToPosition(wrongPosition) && cursor.getLong(EXERCISE_ID) != getArguments().getLong(Exercises.COLUMN_ID)) {
                    PracticeExercise answer = new PracticeExercise(
                            cursor.getLong(EXERCISE_ID),
                            cursor.getString(EXERCISE_SCOPE),
                            cursor.getString(EXERCISE_SCOPE_LETTERS),
                            cursor.getString(EXERCISE_DEFINITION),
                            cursor.getInt(EXERCISE_RATING)
                    );

                    answerButton = answerButtons.remove(random.nextInt(answerButtons.size()));
                    boolean solution = exercise.definition.equals(answer.definition);
                    answerButton.setBackgroundResource(solution ? R.drawable.correct_picker_button : R.drawable.wrong_picker_button);
                    answerButton.setText(invert ? answer.scope : answer.definition);
                    answerButton.setTag(R.id.answer_button_solution, solution);
                    answerButton.setTag(R.id.answer_button_exercise, invert ? answer : exercise);
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
        Boolean solution = (Boolean) view.getTag(R.id.answer_button_solution);
        PracticeExercise answer = (PracticeExercise) view.getTag(R.id.answer_button_exercise);

        submitAnswer(solution == Boolean.TRUE ? answer : null);

        if (!solution) {
            if (answer1Button.getTag(R.id.answer_button_solution) == Boolean.TRUE) {
                answer1Button.setChecked(true);
            }
            if (answer2Button.getTag(R.id.answer_button_solution) == Boolean.TRUE) {
                answer2Button.setChecked(true);
            }
            if (answer3Button.getTag(R.id.answer_button_solution) == Boolean.TRUE) {
                answer3Button.setChecked(true);
            }
            if (answer4Button.getTag(R.id.answer_button_solution) == Boolean.TRUE) {
                answer4Button.setChecked(true);
            }

            // TODO: reduce the rating for the wrong answer too
        }

        answer1Button.setEnabled(false);
        answer2Button.setEnabled(false);
        answer3Button.setEnabled(false);
        answer4Button.setEnabled(false);
    }

}
