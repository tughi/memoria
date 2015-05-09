package com.tughi.memoria;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

public class SolutionPickerFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    private static final String[] ITEMS_PROJECTION = {
            Items.Columns.ID,
            Items.Columns.PROBLEM,
            Items.Columns.SOLUTION,
    };
    private static final int ITEM_ID = 0;
    private static final int ITEM_PROBLEM = 1;
    private static final int ITEM_SOLUTION = 2;

    private boolean loaded;

    private SolutionButton solution1Button;
    private SolutionButton solution2Button;
    private SolutionButton solution3Button;
    private SolutionButton solution4Button;

    private SolutionButton correctSolutionButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.solutions_practice_fragment, container, false);
        TextView problemTextView = (TextView) view.findViewById(R.id.problem);
        problemTextView.setText(getArguments().getString(Items.Columns.PROBLEM));

        solution1Button = (SolutionButton) view.findViewById(R.id.solution_1);
        solution1Button.setOnClickListener(this);
        solution2Button = (SolutionButton) view.findViewById(R.id.solution_2);
        solution2Button.setOnClickListener(this);
        solution3Button = (SolutionButton) view.findViewById(R.id.solution_3);
        solution3Button.setOnClickListener(this);
        solution4Button = (SolutionButton) view.findViewById(R.id.solution_4);
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
            int count = cursor.getCount();
            if (count == 0) {
                // TODO: inform about empty database
                return;
            }

            Random random = new Random();

            List<SolutionButton> solutionButtons = new LinkedList<>();
            solutionButtons.add(solution1Button);
            solutionButtons.add(solution2Button);
            solutionButtons.add(solution3Button);
            solutionButtons.add(solution4Button);

            correctSolutionButton = solutionButtons.remove(random.nextInt(solutionButtons.size()));
            correctSolutionButton.setBackgroundResource(R.drawable.solution_correct);
            correctSolutionButton.setText(getArguments().getString(Items.Columns.SOLUTION));

            while (!solutionButtons.isEmpty()) {
                int wrongPosition = random.nextInt(count);
                if (cursor.moveToPosition(wrongPosition) && cursor.getLong(ITEM_ID) != getArguments().getLong(Items.Columns.ID)) {
                    SolutionButton solutionButton = solutionButtons.remove(random.nextInt(solutionButtons.size()));
                    solutionButton.setBackgroundResource(R.drawable.solution_wrong);
                    solutionButton.setText(cursor.getString(ITEM_SOLUTION));
                }
            }

            loaded = true;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onClick(View view) {
        final boolean correct = view == correctSolutionButton;
        if (!correct) {
            correctSolutionButton.setChecked(true);
        }

        new AsyncTask<Object, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Object... params) {
                Context context = (Context) params[0];
                Bundle arguments = (Bundle) params[1];

                long id = arguments.getLong(Items.Columns.ID);
                int rating = arguments.getInt(Items.Columns.RATING);

                ContentValues values = new ContentValues();
                values.put(Items.Columns.RATING, correct ? Math.max(rating + 1, 5) : rating / 2);
                values.put(Items.Columns.TESTED, System.currentTimeMillis());

                return context.getContentResolver().update(ContentUris.withAppendedId(Items.CONTENT_URI, id), values, null, null) > 0;
            }
        }.execute(getActivity().getApplicationContext(), getArguments());

        solution1Button.setEnabled(false);
        solution2Button.setEnabled(false);
        solution3Button.setEnabled(false);
        solution4Button.setEnabled(false);

        ((MainActivity) getActivity()).continuePractice(correct ? 500 : 1500);
    }

}
