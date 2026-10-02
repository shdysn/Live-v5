package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pk.livecaster.app.core.common.Resource
import pk.livecaster.app.core.util.Formatters

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun formatDuration_formatsMinutesAndSeconds() {
    val duration = Formatters.formatDuration(75) // 1m 15s
    assertEquals("01:15", duration)
  }

  @Test
  fun formatDuration_formatsHoursMinutesSeconds() {
    val duration = Formatters.formatDuration(3665) // 1h 1m 5s
    assertEquals("01:01:05", duration)
  }

  @Test
  fun formatBitrate_formatsKbpsAndMbps() {
    assertEquals("3.5 Mbps", Formatters.formatBitrate(3500))
    assertEquals("800 kbps", Formatters.formatBitrate(800))
  }

  @Test
  fun formatViewers_formatsThousandsAndMillions() {
    assertEquals("1.5K", Formatters.formatViewers(1500))
    assertEquals("2.4M", Formatters.formatViewers(2400000))
    assertEquals("350", Formatters.formatViewers(350))
  }

  @Test
  fun resource_handlesSuccessAndError() {
    val successRes = Resource.Success("Live data")
    assertTrue(successRes.isSuccess)
    assertEquals("Live data", successRes.getOrNull())

    val errorRes = Resource.Error("Connection timed out")
    assertTrue(errorRes.isError)
    assertNull(errorRes.getOrNull())
  }

  @Test
  fun connectAccounts_defaultStateInitializesCleanly() {
    val state = pk.livecaster.app.accounts.presentation.ConnectAccountsUiState()
    org.junit.Assert.assertFalse(state.isFacebookConnected)
    org.junit.Assert.assertFalse(state.isYouTubeConnected)
    assertTrue(state.facebookPermissions.contains("View managed Pages"))
    assertTrue(state.facebookPermissions.contains("Create live broadcasts"))
    assertTrue(state.facebookPermissions.contains("Read Page engagement"))
    assertTrue(state.youTubePermissions.contains("View YouTube Channel"))
    assertTrue(state.youTubePermissions.contains("Create and manage live broadcasts"))
    assertTrue(state.youTubePermissions.contains("View Live status"))
  }
}
