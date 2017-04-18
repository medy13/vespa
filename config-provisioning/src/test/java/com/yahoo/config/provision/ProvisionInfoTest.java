// Copyright 2016 Yahoo Inc. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.config.provision;

import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author lulf
 * @since 5.12
 */
public class ProvisionInfoTest {

    private final HostSpec h1 = new HostSpec("host1", Optional.empty());
    private final HostSpec h2 = new HostSpec("host2", Optional.empty());
    private final HostSpec h3 = new HostSpec("host3", Optional.of(ClusterMembership.from("container/test/0", Optional.empty())));
    private final HostSpec h4 = new HostSpec("host4", Optional.of(ClusterMembership.from("container/test/1", Optional.of("docker-registry.ops.yahoo.com:4443/vespa/ci:6.42.1"))));

    @Test
    public void testProvisionInfoSerialization() throws IOException {
        Set<HostSpec> hosts = new LinkedHashSet<>();
        hosts.add(h1);
        hosts.add(h2);
        hosts.add(h3);
        hosts.add(h4);
        ProvisionInfo info = ProvisionInfo.withHosts(hosts);
        assertProvisionInfo(info);
    }

    @Test
    public void testProvisionInfoMerging() throws IOException {
        Set<HostSpec> hostsA = new LinkedHashSet<>(Collections.singleton(h1));
        Set<HostSpec> hostsB = new LinkedHashSet<>();
        hostsB.add(h2);
        hostsB.add(h3);
        hostsB.add(h4);

        ProvisionInfo infoA = ProvisionInfo.withHosts(hostsA);
        ProvisionInfo infoB = ProvisionInfo.withHosts(hostsB);
        assertProvisionInfo(infoA.merge(infoB));
        assertProvisionInfo(infoB.merge(infoA));
    }

    private void assertProvisionInfo(ProvisionInfo info) throws IOException {
        ProvisionInfo serializedInfo = ProvisionInfo.fromJson(info.toJson(), Optional.empty());
        assertEquals(info.getHosts().size(), serializedInfo.getHosts().size());
        assertTrue(serializedInfo.getHosts().contains(h1));
        assertTrue(serializedInfo.getHosts().contains(h2));
        assertTrue(serializedInfo.getHosts().contains(h3));
        assertTrue(serializedInfo.getHosts().contains(h4));
        assertTrue(!getHost(h1.hostname(), serializedInfo.getHosts()).membership().isPresent());
        assertEquals("container/test/0", getHost(h3.hostname(), serializedInfo.getHosts()).membership().get().stringValue());
        assertEquals("docker-registry.ops.yahoo.com:4443/vespa/ci:6.42.1", getHost(h4.hostname(), serializedInfo.getHosts()).membership().get().cluster().dockerImage());
    }

    private HostSpec getHost(String hostname, Set<HostSpec> hosts) {
        for (HostSpec host : hosts)
            if (host.hostname().equals(hostname))
                return host;
        throw new IllegalArgumentException("No host " + hostname + " is present");
    }

}
