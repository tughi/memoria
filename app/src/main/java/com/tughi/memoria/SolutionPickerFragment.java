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

public class SolutionPickerFragment extends PracticeFragment implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    private static final String[] ITEMS_PROJECTION = {
            Items.Columns.ID,
            Items.Columns.PROBLEM,
            Items.Columns.SOLUTION,
    };
    private static final int ITEM_ID = 0;
    private static final int ITEM_PROBLEM = 1;
    private static final int ITEM_SOLUTION = 2;

    private boolean loaded;

    private PickerButton solution1Button;
    private PickerButton solution2Button;
    private PickerButton solution3Button;
    private PickerButton solution4Button;

    private PickerButton correctSolutionButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.solution_picker_fragment, container, false);
        TextView problemTextView = (TextView) view.findViewById(R.id.problem);
        problemTextView.setText(getArguments().getString(Items.Columns.PROBLEM));

        solution1Button = (PickerButton) view.findViewById(R.id.solution_1);
        solution1Button.setOnClickListener(this);
        solution2Button = (PickerButton) view.findViewById(R.id.solution_2);
        solution2Button.setOnClickListener(this);
        solution3Button = (PickerButton) view.findViewById(R.id.solution_3);
        solution3Button.setOnClickListener(this);
        solution4Button = (PickerButton) view.findViewById(R.id.solution_4);
        solution4Button.setOnClickListener(this);

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
            pickerButtons.add(solution1Button);
            pickerButtons.add(solution2Button);
            pickerButtons.add(solution3Button);
            pickerButtons.add(solution4Button);

            correctSolutionButton = pickerButtons.remove(random.nextInt(pickerButtons.size()));
            correctSolutionButton.setBackgroundResource(R.drawable.correct_picker_button);
            correctSolutionButton.setText(getItemSolution());
            correctSolutionButton.setTag(getItemProblem());

            int count = cursor.getCount();
            while (!pickerButtons.isEmpty()) {
                int wrongPosition = random.nextInt(count);
                if (cursor.moveToPosition(wrongPosition) && cursor.getLong(ITEM_ID) != getArguments().getLong(Items.Columns.ID)) {
                    PickerButton solutionButton = pickerButtons.remove(random.nextInt(pickerButtons.size()));
                    solutionButton.setBackgroundResource(R.drawable.wrong_picker_button);
                    solutionButton.setText(cursor.getString(ITEM_SOLUTION));
                    solutionButton.setTag(cursor.getString(ITEM_PROBLEM));
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
            correctSolutionButton.setChecked(true);
        }

        solution1Button.setEnabled(false);
        solution2Button.setEnabled(false);
        solution3Button.setEnabled(false);
        solution4Button.setEnabled(false);

        ((MainActivity) getActivity()).continuePractice(correct ? NEXT_PROBLEM_NORMAL : NEXT_PROBLEM_DELAYED);
    }

}
