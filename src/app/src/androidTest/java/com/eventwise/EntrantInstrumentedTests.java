package com.eventwise;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Assert;
import org.junit.Test;

public class EntrantInstrumentedTests {
    @Test
    public void verifyDeviceIdisConstant() {
        Entrant entrant1 = new Entrant("Spongebob", "sponge@krustykrab.ca", "1234567890", true, ApplicationProvider.getApplicationContext());
        Entrant entrant2 = new Entrant("Patrick", "patrick@krustykrab.ca", "0987654321", true, ApplicationProvider.getApplicationContext());
        Assert.assertEquals(entrant1.getDeviceId(), entrant2.getDeviceId());
    }
}
