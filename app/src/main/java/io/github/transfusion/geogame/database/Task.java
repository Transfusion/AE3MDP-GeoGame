package io.github.transfusion.geogame.database;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Bryan Kok on 17-5-5.
 */
public class Task implements Parcelable {
    public double latitude;
    public double longitude;
    public long ts;
    public long id;
    public boolean completed;

    public Task(long id, double latitude, double longitude, long ts, int completed){
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.ts = ts;
        this.completed = (completed != 0);
    }

    @Override
    public boolean equals(Object _other){
        if (this == _other){
            return true;
        }
        if (!(_other instanceof Task)){ return false; }

        Task other = (Task) _other;
        return (other.completed == this.completed) && (other.longitude == this.longitude) &&
                (other.latitude == this.latitude) && (other.ts == this.ts) && (other.id == this.id);
    }

    @Override
    public int hashCode(){
        int result = 1000;
        result += new Double(latitude).hashCode();
        result += new Double(longitude).hashCode();
        result += new Long(ts).hashCode();
        result += new Boolean(completed).hashCode();
        return result;
    }


    protected Task(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
        ts = in.readLong();
        id = in.readLong();
        completed = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeLong(ts);
        dest.writeLong(id);
        dest.writeByte((byte) (completed ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Task> CREATOR = new Parcelable.Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };
}
