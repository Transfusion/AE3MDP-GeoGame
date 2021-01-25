package io.github.transfusion.geogame.database;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Bryan Kok on 17-5-11.
 */

public class TrailPoint implements Parcelable{
    public double latitude;
    public double longitude;
    public long ts;
    public long id;

    public TrailPoint(long id, double latitude, double longitude, long ts){
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.ts = ts;
    }

    protected TrailPoint(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
        ts = in.readLong();
        id = in.readLong();
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
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<TrailPoint> CREATOR = new Parcelable.Creator<TrailPoint>() {
        @Override
        public TrailPoint createFromParcel(Parcel in) {
            return new TrailPoint(in);
        }

        @Override
        public TrailPoint[] newArray(int size) {
            return new TrailPoint[size];
        }
    };
}
