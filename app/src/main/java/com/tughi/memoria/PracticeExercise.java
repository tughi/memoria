package com.tughi.memoria;

import android.os.Parcel;
import android.os.Parcelable;

public class PracticeExercise implements Parcelable {

    public final long id;
    public final String scope;
    public final String scopeLetters;
    public final String definition;
    public final double easinessFactor;
    public final int practiceCount;
    public final long practiceInterval;

    public PracticeExercise(long id, String scope, String scopeLetters, String definition, double easinessFactor, int practiceCount, long practiceInterval) {
        this.id = id;
        this.scope = scope;
        this.scopeLetters = scopeLetters;
        this.definition = definition;
        this.easinessFactor = easinessFactor;
        this.practiceCount = practiceCount;
        this.practiceInterval = practiceInterval;
    }

    private PracticeExercise(Parcel in) {
        id = in.readLong();
        scope = in.readString();
        scopeLetters = in.readString();
        definition = in.readString();
        easinessFactor = in.readDouble();
        practiceCount = in.readInt();
        practiceInterval = in.readLong();
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
        dest.writeDouble(easinessFactor);
        dest.writeInt(practiceCount);
        dest.writeLong(practiceInterval);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof PracticeExercise)) return false;

        PracticeExercise exercise = (PracticeExercise) other;

        return id == exercise.id &&
                easinessFactor == exercise.easinessFactor &&
                practiceCount == exercise.practiceCount &&
                practiceInterval == exercise.practiceInterval &&
                scope.equals(exercise.scope) &&
                scopeLetters.equals(exercise.scopeLetters) &&
                definition.equals(exercise.definition);
    }

}
