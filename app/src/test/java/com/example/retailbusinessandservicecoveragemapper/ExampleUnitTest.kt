package com.example.retailbusinessandservicecoveragemapper

import org.junit.Test
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

class ExampleUnitTest {
    @Test
    fun nasugbuCenterIsInsideCoverage() {
        assertTrue(NasugbuBoundary.contains(NASUGBU_CENTER))
    }

    @Test
    fun locationOutsideNasugbuIsRejected() {
        assertFalse(NasugbuBoundary.contains(com.google.android.gms.maps.model.LatLng(14.073, 120.90)))
    }

    @Test
    fun boundaryLocationIsAccepted() {
        assertTrue(NasugbuBoundary.contains(NasugbuBoundary.points.first()))
    }
}