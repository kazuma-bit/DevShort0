package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("DevToggle", appName)
  }

  @Test
  fun `check dev settings manager basic query`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val isEnabled = DevSettingsManager.isDevSettingsEnabled(context)
    // Default in test environment is false/0
    assertEquals(false, isEnabled)
  }
}
