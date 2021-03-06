/*
 * Copyright (c) 2012 - 2015, Internet Corporation for Assigned Names and
 * Numbers (ICANN) and China Internet Network Information Center (CNNIC)
 * 
 * All rights reserved.
 *  
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  
 * * Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 * * Neither the name of the ICANN, CNNIC nor the names of its contributors may
 *  be used to endorse or promote products derived from this software without
 *  specific prior written permission.
 *  
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL ICANN OR CNNIC BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */
package org.restfulwhois.rdap.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dbunit.DatabaseUnitException;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.restfulwhois.rdap.BaseTest;
import org.restfulwhois.rdap.common.dao.QueryDao;
import org.restfulwhois.rdap.common.model.Domain;
import org.restfulwhois.rdap.common.model.DsData;
import org.restfulwhois.rdap.common.model.Event;
import org.restfulwhois.rdap.common.model.KeyData;
import org.restfulwhois.rdap.common.model.Link;
import org.restfulwhois.rdap.common.model.PublicId;
import org.restfulwhois.rdap.common.model.Remark;
import org.restfulwhois.rdap.common.model.SecureDns;
import org.restfulwhois.rdap.common.model.Variant;
import org.restfulwhois.rdap.common.model.Variants;
import org.restfulwhois.rdap.common.util.DomainUtil;
import org.restfulwhois.rdap.core.domain.queryparam.DomainQueryParam;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.springtestdbunit.annotation.DatabaseTearDown;

/**
 * Test for domain DAO.
 * 
 * @author jiashuo
 * 
 */
@SuppressWarnings("rawtypes")
public class DomainQueryDaoTest extends BaseTest {
    /**
     * domainQueryDao.
     */
    @Autowired
    private QueryDao<Domain> domainQueryDao;

    /**
     * test query exist domain.
     */
    @Test
    @DatabaseTearDown("teardown.xml")
    public void testQueryExistDomain() {
        String domainName = "cnnic.cn";
        String punyDomainName = DomainUtil.geneDomainPunyName(domainName);
        Domain domain =
                domainQueryDao.query(DomainQueryParam.generateQueryParam(
                        domainName, punyDomainName));
        assertNotNull(domain);
        assertEquals("1", domain.getHandle());
        assertEquals(domainName, domain.getLdhName());
        assertEquals(domainName, domain.getUnicodeName());
        assertEquals("port43", domain.getPort43());
        assertEquals("zh", domain.getLang());
        // status
        List<String> statusList = domain.getStatus();
        assertThat(statusList,
                CoreMatchers.hasItems("validated", "update prohibited"));
        // events
        List<Event> events = domain.getEvents();
        assertNotNull(events);
        assertEquals(events.size(), 1);
        Event event = events.get(0);
        assertNotNull(event);
        assertEquals(event.getEventAction(), "action1");
        assertEquals(event.getEventActor(), "jiashuo");
        // DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        assertEquals(event.getEventDate(), "2014-01-01T00:01:01Z");
        // links
        List<Link> domainLinks = domain.getLinks();
        assertNotNull(domainLinks);
        assertEquals(2, domainLinks.size());
        Link domainLink = domainLinks.get(0);
        assertNotNull(domainLink);
        assertEquals("http://domainlink", domainLink.getValue());
        assertEquals("http://domainlink", domainLink.getHref());
        // variants
        List<Variants> variantsList = domain.getVariants();
        assertNotNull(variantsList);
        assertEquals(1, variantsList.size());
        Variants variants = variantsList.get(0);
        assertNotNull(variants);
        List<Variant> variantList = variants.getVariantNames();
        assertNotNull(variantList);
        assertEquals(1, variantList.size());
        Variant variant = variantList.get(0);
        assertNotNull(variant);
        assertEquals("variant1", variant.getLdhName());
        assertEquals("unicodeName1", variant.getUnicodeName());
        // publicId
        List<PublicId> publicIds = domain.getPublicIds();
        assertNotNull(publicIds);
        assertEquals(1, publicIds.size());
        PublicId publicId = publicIds.get(0);
        assertEquals("identifier", publicId.getIdentifier());
        assertEquals("type", publicId.getType());
        // remarks
        List<Remark> remarks = domain.getRemarks();
        assertNotNull(remarks);
        assertTrue(remarks.size() > 0);
        Remark remark = remarks.get(0);
        assertNotNull(remark);
        assertEquals("Terms of Use", remark.getTitle());
        assertThat(remark.getDescription(),
                CoreMatchers.hasItems("description1", "description2"));
        List<Link> links = remark.getLinks();
        assertNotNull(links);
        assertEquals(1, links.size());
        Link link = links.get(0);
        assertNotNull(link);
        assertEquals("http://example.com/context_uri", link.getValue());
        // secureDns
        SecureDns secureDns = domain.getSecureDns();
        assertNotNull(secureDns);
        assertNotNull(secureDns);
        assertEquals(1, secureDns.getMaxSigLife().intValue());
        assertEquals(true, secureDns.isDelegationSigned());
        assertEquals(true, secureDns.isZoneSigned());
        // dsData
        List<DsData> dsDataList = secureDns.getDsData();
        assertNotNull(dsDataList);
        assertEquals(1, dsDataList.size());
        DsData dsData = dsDataList.get(0);
        assertNotNull(dsData);
        assertEquals(1, dsData.getAlgorithm().intValue());
        assertEquals(
                "D4B7D520E7BB5F0F67674A0CCEB1E3E0614B93C4F9E99B8383F6A1E4469DA50A",
                dsData.getDigest());
        assertEquals(1, dsData.getDigestType().intValue());
        assertEquals(1, dsData.getKeyTag().intValue());
        // keyData
        List<KeyData> keyDataList = secureDns.getKeyData();
        assertNotNull(keyDataList);
        assertEquals(1, keyDataList.size());
        KeyData keyData = keyDataList.get(0);
        assertNotNull(keyData);
        assertEquals(1, keyData.getAlgorithm().intValue());
        assertEquals(
                "D4B7D520E7BB5F0F67674A0CCEB1E3E0614B93C4F9E99B8383F6A1E4469DA50A",
                keyData.getPublicKey());
        assertEquals(1, keyData.getProtocol().intValue());
        assertEquals(1, keyData.getFlags().intValue());
        // custom properties
        Map<String, String> customProperties = domain.getCustomProperties();
        assertNotNull(customProperties);
        assertEquals(2, customProperties.size());
        Map<String, String> expectedCustomProperties =
                new LinkedHashMap<String, String>();
        expectedCustomProperties.put("customKey1", "customValue1");
        expectedCustomProperties.put("customKey2", "customValue2");
        assertThat(customProperties.entrySet(),
                CoreMatchers.equalTo(expectedCustomProperties.entrySet()));
    }

    /**
     * test query exist domain.
     */
    @Test
    @DatabaseTearDown("teardown.xml")
    public void testQueryExistUnicodeDomain() {
        String unicodeName = "清华大学.中国";
        String punyDomainName = DomainUtil.geneDomainPunyName(unicodeName);
        Domain domain =
                domainQueryDao.query(DomainQueryParam.generateQueryParam(
                        unicodeName, punyDomainName));
        assertNotNull(domain);
        assertEquals("2", domain.getHandle());
        assertEquals(punyDomainName, domain.getLdhName());
        assertEquals(unicodeName, domain.getUnicodeName());
        assertEquals("port43", domain.getPort43());
        assertEquals("zh", domain.getLang());
        // status
        List<String> statusList = domain.getStatus();
        assertThat(statusList, CoreMatchers.hasItems("validated"));
    }
    
    @Test
    @DatabaseTearDown("teardown.xml")
    public void test_QueryDomainStatus_status_is_null(){
    	String domainName = "1.0.0.in-addr.arpa";
        String punyDomainName = DomainUtil.geneDomainPunyName(domainName);
        Domain domain =
                domainQueryDao.query(DomainQueryParam.generateQueryParam(
                        domainName, punyDomainName));
    	assertEquals(null, domain.getStatus());
    }

    /**
     * test query ont exist domain.
     */
    @Test
    @DatabaseTearDown("teardown.xml")
    public void testQueryNotExistDomain() {
        String domainName = "cnnic";
        String punyDomainName = DomainUtil.geneDomainPunyName(domainName);
        Domain domain =
                domainQueryDao.query(DomainQueryParam.generateQueryParam(
                        domainName, punyDomainName));
        assertNull(domain);
    }

    /**
     * test query exist domain.
     * 
     * @throws Exception
     * @throws SQLException
     * @throws DatabaseUnitException
     */
    @Test
    @DatabaseTearDown("teardown.xml")
    // @DatabaseSetup("domain.xml")
            public
            void testQueryArpa() throws DatabaseUnitException, SQLException,
                    Exception {
        String domainName = "1.0.0.in-addr.arpa";
        Domain domain =
                domainQueryDao.query(DomainQueryParam.generateQueryParam(
                        domainName, domainName));

        assertNotNull(domain);
    }

    /**
     * test query exist domain.
     */
    @Test
    @DatabaseTearDown("teardown.xml")
    public void testQueryArpaIpv6_1() {
        String domainName =
                "2.1.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.ip6.arpa";

        Domain domain =
                domainQueryDao.query(DomainQueryParam.generateQueryParam(
                        domainName, domainName));
        assertNull(domain);
    }

    /**
     * test query exist domain.
     */
    @Test
    // @DatabaseTearDown("teardown.xml")
            public
            void testQueryArpaIpv6_2() {
        String domainName =
                "1.1.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.ip6.arpa";

        Domain domain =
                domainQueryDao.query(DomainQueryParam.generateQueryParam(
                        domainName, domainName));
        assertNotNull(domain);
    }

    @Test
    @DatabaseTearDown("teardown.xml")
    public void testQueryArpaIpv6_3() {
        String domainName = "f.f.f.ip6.arpa";
        Domain domain =
                domainQueryDao.query(DomainQueryParam.generateQueryParam(
                        domainName, domainName));
        assertNotNull(domain);
    }

    @Test
    // @DatabaseTearDown("teardown.xml")
            public
            void testQueryArpaIpv6_4() {
        String domainName =
                "f.f.f.f.f.f.f.f.f.f.f.f.f.f.f.f.f.f.f.f.f.f.f.f.f.f.f.f.f.f.f.f.ip6.arpa";
        Domain domain =
                domainQueryDao.query(DomainQueryParam.generateQueryParam(
                        domainName, domainName));
        assertNotNull(domain);
    }

    /**
     * test query not exist domain.
     */
    @Test
    @DatabaseTearDown("teardown.xml")
    public void testQueryNotExistArpa() {
        String domainName = "10.in-addr.arpa";

        Domain domain =
                domainQueryDao.query(DomainQueryParam.generateQueryParam(
                        domainName, domainName));
        assertNull(domain);
    }

    @Override
    public void before() throws Exception {
        databaseSetupWithBinaryColumns("domain.xml");
    }

}
