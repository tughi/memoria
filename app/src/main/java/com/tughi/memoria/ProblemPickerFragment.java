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

public class ProblemPickerFragment extends PracticeFragment implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    private static final String[] ITEMS_PROJECTION = {
            Items.Columns.ID,
            Items.Columns.PROBLEM,
    };
    private static final int ITEM_ID = 0;
    private static final int ITEM_PROBLEM = 1;

    private boolean loaded;

    private PickerButton problem1Button;
    private PickerButton problem2Button;
    private PickerButton problem3Button;
    private PickerButton problem4Button;

    private PickerButton correctProblemButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.problem_picker_fragment, container, false);
        TextView solutionTextView = (TextView) view.findViewById(R.id.solution);
        solutionTextView.setText(getArguments().getString(Items.Columns.SOLUTION));

        problem1Button = (PickerButton) view.findViewById(R.id.problem_1);
        problem1Button.setOnClickListener(this);
        problem2Button = (PickerButton) view.findViewById(R.id.problem_2);
        problem2Button.setOnClickListener(this);
        problem3Button = (PickerButton) view.findViewById(R.id.problem_3);
        problem3Button.setOnClickListener(this);
        problem4Button = (PickerButton) view.findViewById(R.id.problem_4);
        problem4Button.setOnClickListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), Items.CONTENT_URI, ITEMS_PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (!loaded) {
            Random random = new Random();

            List<PickerButton> pickerButtons = new LinkedList<>();
            pickerButtons.add(problem1Button);
            pickerButtons.add(problem2Button);
            pickerButtons.add(problem3Button);
            pickerButtons.add(problem4Button);

            correctProblemButton = pickerButtons.remove(random.nextInt(pickerButtons.size()));
            correctProblemButton.setBackgroundResource(R.drawable.correct_picker_button);
            correctProblemButton.setText(getItemProblem());
            correctProblemButton.setTag(getItemProblem());

            int count = cursor.getCount();
            while (!pickerButtons.isEmpty()) {
                int wrongPosition = random.nextInt(count);
                if (cursor.moveToPosition(wrongPosition) && cursor.getLong(ITEM_ID) != getArguments().getLong(Items.Columns.ID)) {
                    PickerButton problemButton = pickerButtons.remove(random.nextInt(pickerButtons.size()));
                    problemButton.setBackgroundResource(R.drawable.wrong_picker_button);
                    problemButton.setText(cursor.getString(ITEM_PROBLEM));
                    problemButton.setTag(cursor.getString(ITEM_PROBLEM));
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
            correctProblemButton.setChecked(true);
        }

        problem1Button.setEnabled(false);
        problem2Button.setEnabled(false);
        problem3Button.setEnabled(false);
        problem4Button.setEnabled(false);

        ((MainActivity) getActivity()).continuePractice(correct ? NEXT_PROBLEM_NORMAL : NEXT_PROBLEM_DELAYED);
    }

}
