package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
    assertEquals("Focus", appName)
  }

  @Test
  fun `database prepopulation and schema sanity check`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = AppDatabase.getInstance(context)
    assertNotNull(database)
    
    val listDao = database.listDao()
    val lists = listDao.getAllLists().first()
    
    assertEquals(4, lists.size)
    assertEquals("Personal", lists[0].name)
    assertEquals("🏡", lists[0].emoji)
    assertEquals("#0284C7", lists[0].colorHex)
    assertEquals(true, lists[0].isDefault)
  }
}

