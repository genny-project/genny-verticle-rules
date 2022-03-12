package life.genny.test.qwandautils.capabilties;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import life.genny.qwanda.datatype.CapabilityMode;

import static life.genny.utils.CapabilityUtilsRefactored.*;
import static life.genny.qwanda.datatype.CapabilityMode.*;

public class CapabilityUtilsTest {
    
    @Test
    public void cleanCapabilityCodeTest() {
        String badCode1 = "prm_APPLE";
        String goodCode1 = "PRM_APPLE";

        String badCode2 = "OWN_APPLE";
        String goodCode2 = "PRM_OWN_APPLE";

        assertEquals(goodCode1, cleanCapabilityCode(badCode1));
        assertEquals(goodCode2, cleanCapabilityCode(badCode2));
    }

    @Test
    public void getCapModeArrayTest() {
        String capModeString1 = "[\"VIEW\",\"ADD\"]";
        CapabilityMode[] goodArray1 = {VIEW, ADD};
        assertArrayEquals(goodArray1, getCapModesFromString(capModeString1));
    }

    @Test
    public void getCapModeStringTest() {
        CapabilityMode[] capModeArray = {VIEW, ADD};
        String goodCapModeString1 = "[\"VIEW\",\"ADD\"]";
        assertEquals(goodCapModeString1, getModeString(capModeArray));
    }
}
