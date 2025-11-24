package com.example.smartirrigation.presentation.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume

class LocationHelper (
    private val context: Context
) {
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun getCurrentCity(): String? {
        if (!hasLocationPermission()) return null

        val location = getCurrentLocation() ?: return null
        return getCityFromLocation(location)
    }

    private suspend fun getCurrentLocation(): Location? = suspendCancellableCoroutine { cont ->
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            cont.resume(null)
            return@suspendCancellableCoroutine
        }

        // Try to get the last known location first as it's faster
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                cont.resume(location)
            } else {
                // If last location is null, request a fresh location
                // Note: In a real app, you might want to use requestLocationUpdates for higher accuracy
                // but for city level, this is usually sufficient or we can use getCurrentLocation with high priority
                 fusedLocationClient.getCurrentLocation(
                     com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                     CancellationTokenSource().token
                 ).addOnSuccessListener { currentLocation ->
                     cont.resume(currentLocation)
                 }.addOnFailureListener {
                     cont.resume(null)
                 }
            }
        }.addOnFailureListener {
            cont.resume(null)
        }
    }

    private fun getCityFromLocation(location: Location): String? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            // getFromLocation is synchronous, so we should run this in IO dispatcher in the caller
            @Suppress("DEPRECATION") // For older API levels compatibility
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                addresses[0].locality ?: addresses[0].subAdminArea ?: addresses[0].adminArea
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
