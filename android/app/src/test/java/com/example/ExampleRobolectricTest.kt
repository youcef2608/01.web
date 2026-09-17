package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.HelpCall
import com.example.data.model.HelpCategory
import com.example.data.model.UrgencyLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("لمّة", appName)
  }

  @Test
  fun `verify haversine distance calculation`() {
    val call = HelpCall(
      id = "test_call",
      title = "فحص المسافة",
      latitude = 36.7538,
      longitude = 3.0588,
      category = HelpCategory.DELIVERY,
      urgency = UrgencyLevel.URGENT
    )
    val distance = call.distanceTo(36.7538, 3.0588)
    assertEquals(0.0, distance, 0.01)

    val distanceApart = call.distanceTo(36.7638, 3.0588)
    assertTrue("Distance should be around 1 km", distanceApart > 0.9 && distanceApart < 1.3)
  }
}
