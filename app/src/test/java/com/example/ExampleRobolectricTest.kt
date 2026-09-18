package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CustomerDebt
import com.example.data.model.OperatorType
import com.example.data.repository.OperatorSettings
import com.example.data.repository.SettingsRepository
import com.example.data.security.DeviceActivationManager
import com.example.data.security.SecurityRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
        assertEquals("محول الرصيد السوري", appName)
    }

    @Test
    fun `operator auto detection from syrian prefixes`() {
        assertEquals(OperatorType.SYRIATEL, OperatorType.detectFromPhone("0933123456"))
        assertEquals(OperatorType.SYRIATEL, OperatorType.detectFromPhone("0988000000"))
        assertEquals(OperatorType.SYRIATEL, OperatorType.detectFromPhone("0991999999"))
        assertEquals(OperatorType.MTN, OperatorType.detectFromPhone("0944123456"))
        assertEquals(OperatorType.MTN, OperatorType.detectFromPhone("0955000000"))
        assertEquals(OperatorType.MTN, OperatorType.detectFromPhone("0966999999"))
        assertEquals(OperatorType.CUSTOM, OperatorType.detectFromPhone("0977000000", "097"))
    }

    @Test
    fun `ussd code generation test`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val settingsRepo = SettingsRepository(context)
        val code = settingsRepo.buildUssdCode(OperatorType.SYRIATEL, "0933123456", 5000)
        assertEquals("*150*1*0933123456*5000*1234#", code)
    }

    @Test
    fun `customer debt calculation test`() {
        val debt = CustomerDebt(
            customerName = "سامر الحمصي",
            customerPhone = "0933111222",
            totalDebt = 15000,
            totalCredit = 5000
        )
        assertEquals(10000L, debt.netBalance)
    }

    @Test
    fun `device activation and licensing validation test`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val deviceId = DeviceActivationManager.getDeviceId(context)
        assertTrue(deviceId.startsWith("SYR-"))

        val customerName = "تسجيلات السلام"
        val generatedKey = DeviceActivationManager.generateLicenseKey(deviceId, customerName)
        assertTrue(generatedKey.startsWith("ACT-"))

        // Correct key and matching device must be valid
        assertTrue(DeviceActivationManager.verifyLicenseKey(deviceId, customerName, generatedKey))

        // Same key must FAIL on a different device
        assertFalse(DeviceActivationManager.verifyLicenseKey("SYR-DIFF-9999", customerName, generatedKey))

        // Same key must FAIL if customer name changed
        assertFalse(DeviceActivationManager.verifyLicenseKey(deviceId, "متجر آخر", generatedKey))

        // Invalid key must FAIL
        assertFalse(DeviceActivationManager.verifyLicenseKey(deviceId, customerName, "ACT-0000-0000"))
    }

    @Test
    fun `security repository activation and investor login lifecycle`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = SecurityRepository(context)

        // Initial state
        val deviceId = repo.currentDeviceId
        val clientName = "مركز البركة للاتصالات"
        val clientKey = repo.generateKeyForClient(deviceId, clientName)

        // Activate
        val activationSuccess = repo.activate(clientName, clientKey)
        assertTrue(activationSuccess)
        assertTrue(repo.securityState.value.isActivated)
        assertEquals(clientName, repo.securityState.value.licensedCustomerName)

        // Investor PIN check (default 123456)
        assertTrue(repo.verifyInvestorPin("123456"))
        assertFalse(repo.verifyInvestorPin("000000"))

        // Lock & Unlock
        repo.lockApp()
        assertTrue(repo.securityState.value.isAppLocked)
        repo.unlockApp()
        assertFalse(repo.securityState.value.isAppLocked)

        // Investor Login
        assertTrue(repo.verifyInvestorLogin("المستثمر", "123456"))
        assertFalse(repo.verifyInvestorLogin("المستثمر", "wrong_pin"))

        // Deactivate with wrong PIN should fail
        assertFalse(repo.deactivate("wrong_pin"))
        assertTrue(repo.securityState.value.isActivated)

        // Deactivate with correct PIN should succeed
        assertTrue(repo.deactivate("123456"))
        assertFalse(repo.securityState.value.isActivated)
    }
}
