package com.tughi.memoria;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class AnswerInputFragment extends PracticeFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String EXERCISES_SELECTION = Exercises.COLUMN_DEFINITION + " = ?";

    private EditText answerEditText;

    private List<PracticeExercise> solutions = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        solutions.add(getExercise());

        getLoaderManager().initLoader(10, null, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.answer_input_fragment, container, false);

        PracticeExercise exercise = getExercise();

        TextView questionTextView = (TextView) view.findViewById(R.id.question);
        questionTextView.setText(Html.fromHtml(exercise.definition));

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
        answerEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
                // ignored
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                // ignored
            }

            @Override
            public void afterTextChanged(Editable text) {
                PracticeExercise solution = getSolution(text.toString());
                if (solution != null) {
                    onSubmit(solution);
                }
            }
        });

        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] selectionArgs = {getExercise().definition};
        return new CursorLoader(getActivity(), Exercises.CONTENT_URI, EXERCISES_PROJECTION, EXERCISES_SELECTION, selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            solutions.clear();

            do {
                PracticeExercise solution = new PracticeExercise(
                        cursor.getLong(EXERCISE_ID),
                        cursor.getString(EXERCISE_SCOPE),
                        cursor.getString(EXERCISE_SCOPE_LETTERS),
                        cursor.getString(EXERCISE_DEFINITION),
                        cursor.getDouble(EXERCISE_EASINESS_FACTOR),
                        cursor.getInt(EXERCISE_PRACTICE_COUNT),
                        cursor.getLong(EXERCISE_PRACTICE_INTERVAL)
                );

                solutions.add(solution);
            } while (cursor.moveToNext());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void onSubmit() {
        PracticeExercise solution = getSolution(answerEditText.getText().toString());

        onSubmit(solution);
    }

    @SuppressWarnings("deprecation")
    private void onSubmit(PracticeExercise solution) {
        submitAnswer(solution, solution != null ? 5 : 0);

        answerEditText.setEnabled(false);
        answerEditText.setBackgroundColor(getResources().getColor(solution != null ? R.color.correct : R.color.wrong));
    }

    private PracticeExercise getSolution(String answerScope) {
        StringBuilder answer = new StringBuilder(answerScope.length());
        for (char c : answerScope.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                answer.append(c);
            }
        }

        PracticeExercise exercise = getExercise();
        if (exercise.scopeLetters.equals(answer.toString())) {
            return exercise;
        }

        for (PracticeExercise solution : solutions) {
            if (solution.scopeLetters.equals(answer.toString())) {
                return solution;
            }
        }

        return null;
    }

}
