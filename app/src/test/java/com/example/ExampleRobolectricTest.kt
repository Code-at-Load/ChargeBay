package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.ClockFaceStyle
import com.example.model.LeftWidgetType
import com.example.model.RightWidgetType
import com.example.model.SampleTracks
import com.example.state.NightModeOption
import com.example.state.StandByViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches StandBy`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("StandBy", appName)
    }

    @Test
    fun `viewModel initial state and widget cycling`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = StandByViewModel(app)

        val initialState = viewModel.uiState.value
        assertEquals(LeftWidgetType.BIG_DATE, initialState.leftWidgetType)
        assertEquals(RightWidgetType.CALENDAR, initialState.rightWidgetType)
        assertEquals(ClockFaceStyle.BUBBLY_BLUE, initialState.clockFace)

        viewModel.cycleLeftWidget()
        assertEquals(LeftWidgetType.ANALOG_CLOCK, viewModel.uiState.value.leftWidgetType)

        viewModel.cycleRightWidget()
        assertEquals(RightWidgetType.WEATHER, viewModel.uiState.value.rightWidgetType)
    }

    @Test
    fun `viewModel night mode triggers crimson red below lux threshold`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = StandByViewModel(app)

        viewModel.setNightModeOption(NightModeOption.AUTO)
        viewModel.updateAmbientLux(50f)
        assertFalse(viewModel.uiState.value.isNightModeActive)

        viewModel.updateAmbientLux(4f)
        assertTrue(viewModel.uiState.value.isNightModeActive)

        viewModel.setNightModeOption(NightModeOption.ALWAYS_ON)
        assertTrue(viewModel.uiState.value.isNightModeActive)

        viewModel.setNightModeOption(NightModeOption.ALWAYS_OFF)
        assertFalse(viewModel.uiState.value.isNightModeActive)
    }

    @Test
    fun `viewModel music controls cycle tracks and activate playback`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = StandByViewModel(app)

        assertEquals(SampleTracks[0].id, viewModel.uiState.value.currentTrack.id)
        viewModel.nextTrack()
        assertEquals(SampleTracks[1].id, viewModel.uiState.value.currentTrack.id)
        assertTrue(viewModel.uiState.value.isMediaPlaying)

        viewModel.previousTrack()
        assertEquals(SampleTracks[0].id, viewModel.uiState.value.currentTrack.id)
    }

    @Test
    fun `bedside checklist item addition and deletion`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = StandByViewModel(app)

        val initialCount = viewModel.uiState.value.reminders.size
        viewModel.addReminder("Read book 20 mins")
        assertEquals(initialCount + 1, viewModel.uiState.value.reminders.size)

        val addedItem = viewModel.uiState.value.reminders.last()
        assertEquals("Read book 20 mins", addedItem.title)

        viewModel.deleteReminder(addedItem.id)
        assertEquals(initialCount, viewModel.uiState.value.reminders.size)
    }

    @Test
    fun `auto exit preference toggle works as expected`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = StandByViewModel(app)

        viewModel.setAutoExitOnUnplugOrPortrait(true)
        assertTrue(viewModel.uiState.value.autoExitOnUnplugOrPortrait)

        viewModel.setAutoExitOnUnplugOrPortrait(false)
        assertFalse(viewModel.uiState.value.autoExitOnUnplugOrPortrait)
    }

    @Test
    fun `only real devices shown without fake accessories and stocks removed`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = StandByViewModel(app)

        val devices = viewModel.uiState.value.connectedDevices
        assertEquals(1, devices.size)
        assertEquals(com.example.model.BatteryDeviceType.PHONE, devices[0].deviceType)

        val rightStack = viewModel.uiState.value.rightWidgetStack
        assertFalse(rightStack.any { it.name == "STOCKS_TICKER" })
    }

    @Test
    fun `clock face styles cycle forwards and backwards with vertical slide state`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = StandByViewModel(app)

        assertEquals(ClockFaceStyle.BUBBLY_BLUE, viewModel.uiState.value.clockFace)

        viewModel.cycleClockFace()
        assertEquals(ClockFaceStyle.BUBBLY_CANDY, viewModel.uiState.value.clockFace)

        viewModel.cycleClockFace()
        assertEquals(ClockFaceStyle.WORLD_MAP, viewModel.uiState.value.clockFace)

        viewModel.previousClockFace()
        assertEquals(ClockFaceStyle.BUBBLY_CANDY, viewModel.uiState.value.clockFace)

        viewModel.selectClockFace(ClockFaceStyle.FULL_CALENDAR)
        assertEquals(ClockFaceStyle.FULL_CALENDAR, viewModel.uiState.value.clockFace)
    }
}
