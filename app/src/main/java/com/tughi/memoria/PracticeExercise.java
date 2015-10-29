package com.tughi.memoria;

import android.os.Parcel;
import android.os.Parcelable;

public class PracticeExercise implements Parcelable {

    public final long id;
    public final String scope;
    public final String scopeLetters;
    public final String definition;
    public final int rating;

    public PracticeExercise(long id, String scope, String scopeLetters, String definition, int rating) {
        this.id = id;
        this.scope = scope;
        this.scopeLetters = scopeLetters;
        this.definition = definition;
        this.rating = rating;
    }

    private PracticeExercise(Parcel in) {
        id = in.readLong();
        scope = in.readString();
        scopeLetters = in.readString();
        definition = in.readString();
        rating = in.readInt();
    }

    public static final Creator<PracticeExercise> CREATOR = new Creator<PracticeExercise>() {
        @Override
        public PracticeExercise createFromParcel(Parcel in) {
            return new PracticeExercise(in);
        }

        @Override
        public PracticeExercise[] newArray(int size) {
            return new PracticeExercise[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(scope);
        dest.writeString(scopeLetters);
        dest.writeString(definition);
        dest.writeInt(rating);
    }

}
