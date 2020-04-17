package com.cluster.footprints.utils;

public class UserLocation {

    private String mProvider;
    private long mTime = 0;
    private long mElapsedRealtimeNanos = 0;
    // Estimate of the relative precision of the alignment of this SystemClock
    // timestamp, with the reported measurements in nanoseconds (68% confidence).
    private double mElapsedRealtimeUncertaintyNanos = 0.0f;
    private double mLatitude = 0.0;
    private double mLongitude = 0.0;
    private double mAltitude = 0.0f;
    private float mSpeed = 0.0f;
    private float mBearing = 0.0f;
    private float mHorizontalAccuracyMeters = 0.0f;
    private float mVerticalAccuracyMeters = 0.0f;
    private float mSpeedAccuracyMetersPerSecond = 0.0f;
    private float mBearingAccuracyDegrees = 0.0f;

    public UserLocation(){

    }

    public String getmProvider() {
        return mProvider;
    }

    public void setmProvider(String mProvider) {
        this.mProvider = mProvider;
    }

    public long getmTime() {
        return mTime;
    }

    public void setmTime(long mTime) {
        this.mTime = mTime;
    }

    public long getmElapsedRealtimeNanos() {
        return mElapsedRealtimeNanos;
    }

    public void setmElapsedRealtimeNanos(long mElapsedRealtimeNanos) {
        this.mElapsedRealtimeNanos = mElapsedRealtimeNanos;
    }

    public double getmElapsedRealtimeUncertaintyNanos() {
        return mElapsedRealtimeUncertaintyNanos;
    }

    public void setmElapsedRealtimeUncertaintyNanos(double mElapsedRealtimeUncertaintyNanos) {
        this.mElapsedRealtimeUncertaintyNanos = mElapsedRealtimeUncertaintyNanos;
    }

    public double getmLatitude() {
        return mLatitude;
    }

    public void setmLatitude(double mLatitude) {
        this.mLatitude = mLatitude;
    }

    public double getmLongitude() {
        return mLongitude;
    }

    public void setmLongitude(double mLongitude) {
        this.mLongitude = mLongitude;
    }

    public double getmAltitude() {
        return mAltitude;
    }

    public void setmAltitude(double mAltitude) {
        this.mAltitude = mAltitude;
    }

    public float getmSpeed() {
        return mSpeed;
    }

    public void setmSpeed(float mSpeed) {
        this.mSpeed = mSpeed;
    }

    public float getmBearing() {
        return mBearing;
    }

    public void setmBearing(float mBearing) {
        this.mBearing = mBearing;
    }

    public float getmHorizontalAccuracyMeters() {
        return mHorizontalAccuracyMeters;
    }

    public void setmHorizontalAccuracyMeters(float mHorizontalAccuracyMeters) {
        this.mHorizontalAccuracyMeters = mHorizontalAccuracyMeters;
    }

    public float getmVerticalAccuracyMeters() {
        return mVerticalAccuracyMeters;
    }

    public void setmVerticalAccuracyMeters(float mVerticalAccuracyMeters) {
        this.mVerticalAccuracyMeters = mVerticalAccuracyMeters;
    }

    public float getmSpeedAccuracyMetersPerSecond() {
        return mSpeedAccuracyMetersPerSecond;
    }

    public void setmSpeedAccuracyMetersPerSecond(float mSpeedAccuracyMetersPerSecond) {
        this.mSpeedAccuracyMetersPerSecond = mSpeedAccuracyMetersPerSecond;
    }

    public float getmBearingAccuracyDegrees() {
        return mBearingAccuracyDegrees;
    }

    public void setmBearingAccuracyDegrees(float mBearingAccuracyDegrees) {
        this.mBearingAccuracyDegrees = mBearingAccuracyDegrees;
    }
}
