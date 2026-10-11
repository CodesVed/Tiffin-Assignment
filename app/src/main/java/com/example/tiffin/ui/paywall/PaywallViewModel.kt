package com.example.tiffin.ui.paywall

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.tiffin.AnalyticsEvents
import com.example.tiffin.AppAnalytics
import com.example.tiffin.data.UserPrefs
import com.example.tiffin.data.chargeTimeMillis
import com.example.tiffin.data.formatDate
import com.example.tiffin.data.formatDateTime
import com.example.tiffin.ui.PaywallRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class PaywallNotice { None, TermsUpdated, SaveFailed }

sealed interface PaywallUiState {
    /** Before paying: [chargeDate] is the date the ₹249 charge would fall on. */
    data class Offer(val chargeDate: String, val notice: PaywallNotice) : PaywallUiState

    /** After paying: everything is derived from the persisted purchase timestamp. */
    data class Confirmed(val purchasedAt: String, val chargeAt: String) : PaywallUiState
}

class PaywallViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {

    private val userPrefs = UserPrefs.get(application)
    private val analytics = AppAnalytics.instance

    /** What opened this paywall: PaywallSource.LAUNCH or PaywallSource.SUBSCRIBE_TAP. */
    private val source: String = savedStateHandle.toRoute<PaywallRoute>().source

    // The moment the offer was shown. Kept in SavedStateHandle so rotation and
    // process death don't change the date the user is looking at.
    private val offerAnchorMillis: StateFlow<Long> =
        savedStateHandle.getStateFlow(KEY_OFFER_ANCHOR, System.currentTimeMillis())

    private val notice = MutableStateFlow(PaywallNotice.None)

    val uiState: StateFlow<PaywallUiState> =
        combine(offerAnchorMillis, userPrefs.purchasedAtMillis, notice, ::buildState)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = buildState(
                    offerAnchorMillis.value,
                    userPrefs.purchasedAtMillis.value,
                    PaywallNotice.None,
                ),
            )

    init {
        // Log the view once per paywall visit. Rotation keeps this ViewModel alive; the flag in
        // SavedStateHandle survives process death, so a restored paywall is not counted twice.
        if (!userPrefs.isPaid && savedStateHandle.get<Boolean>(KEY_VIEW_LOGGED) != true) {
            analytics.logEvent(AnalyticsEvents.PAYWALL_VIEWED, mapOf("source" to source))
            savedStateHandle[KEY_VIEW_LOGGED] = true
        }
    }

    fun onPayClicked() {
        val wasPaid = userPrefs.isPaid
        val now = System.currentTimeMillis()

        // Is the date on screen still the date we would charge on right now?
        // It may not be if the paywall stayed open past midnight or the time zone changed.
        val dateOnScreen = (uiState.value as? PaywallUiState.Offer)?.chargeDate
        val dateIfPaidNow = formatDate(chargeTimeMillis(now))
        if (dateOnScreen != null && dateOnScreen != dateIfPaidNow) {
            savedStateHandle[KEY_OFFER_ANCHOR] = now
            notice.value = PaywallNotice.TermsUpdated
            return // the user reviews the new date and taps again
        }

        val saved = userPrefs.markPurchased(now)
        if (saved && !wasPaid) {
            analytics.logEvent(AnalyticsEvents.SUBSCRIPTION_PURCHASED, mapOf("source" to source))
        }
        notice.value = if (saved) PaywallNotice.None else PaywallNotice.SaveFailed
    }

    private fun buildState(
        anchorMillis: Long,
        purchasedAtMillis: Long,
        notice: PaywallNotice,
    ): PaywallUiState =
        if (purchasedAtMillis > 0L) {
            PaywallUiState.Confirmed(
                purchasedAt = formatDateTime(purchasedAtMillis),
                chargeAt = formatDateTime(chargeTimeMillis(purchasedAtMillis)),
            )
        } else {
            PaywallUiState.Offer(
                chargeDate = formatDate(chargeTimeMillis(anchorMillis)),
                notice = notice,
            )
        }

    private companion object {
        const val KEY_OFFER_ANCHOR = "offer_anchor_millis"
        const val KEY_VIEW_LOGGED = "paywall_view_logged"
    }
}