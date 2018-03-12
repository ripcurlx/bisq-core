package bisq.core.network.p2p.seed;

import bisq.core.app.BisqEnvironment;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;

import java.util.Collections;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;



import io.bisq.network.p2p.NodeAddress;

public class SeedNodeAddressLookupTest {
    @Before
    public void setUp() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    public void testResolveNodeAddressesWhenLocalAddressSpecified() {
        SeedNodeAddressLookup lookup = new SeedNodeAddressLookup(
                mock(BisqEnvironment.class), false, 1, "192.168.0.1:1234",
                "192.168.0.1:1234, 192.168.0.2:9897");

        Set<NodeAddress> actual = lookup.resolveNodeAddresses();
        Set<NodeAddress> expected = Collections.singleton(new NodeAddress("192.168.0.2:9897"));
        assertEquals(expected, actual);
    }

    @Test
    public void testResolveNodeAddressesWhenSeedNodesAreNull() {
        SeedNodeAddressLookup lookup = new SeedNodeAddressLookup(
                mock(BisqEnvironment.class), false, 1, "192.168.0.1:1234", null);

        Set<NodeAddress> actual = lookup.resolveNodeAddresses();
        assertFalse(actual.isEmpty());
    }
}