package com.example.literise.utils;

import android.location.Location;

/**
 * Checks whether the user is within the allowed school area before
 * starting the pre-assessment or post-assessment.
 *
 * Allowed schools:
 *  1. Holy Spirit Elementary School   — 14.678315,  121.072018
 *  2. Dona Juana Elementary School    — 14.6892901, 121.0817437
 */
public class LocationHelper {

    // Holy Spirit Elementary School
    private static final double SCHOOL_1_LAT = 14.678315;
    private static final double SCHOOL_1_LNG = 121.072018;

    // Dona Juana Elementary School
    private static final double SCHOOL_2_LAT = 14.6892901;
    private static final double SCHOOL_2_LNG = 121.0817437;

    /** Geofence radius in metres — user must be within this distance of a school. */
    public static final float RADIUS_METERS = 200f;

    /**
     * Returns {@code true} when the given coordinates are within {@link #RADIUS_METERS}
     * of either allowed school.
     */
    public static boolean isWithinAllowedArea(double userLat, double userLng) {
        float[] result = new float[1];

        Location.distanceBetween(userLat, userLng, SCHOOL_1_LAT, SCHOOL_1_LNG, result);
        if (result[0] <= RADIUS_METERS) return true;

        Location.distanceBetween(userLat, userLng, SCHOOL_2_LAT, SCHOOL_2_LNG, result);
        return result[0] <= RADIUS_METERS;
    }
}