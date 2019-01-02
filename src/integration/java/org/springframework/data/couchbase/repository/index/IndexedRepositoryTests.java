/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.data.couchbase.repository.index;

import static org.junit.Assert.*;

import java.util.Arrays;

import com.couchbase.client.java.error.DesignDocumentDoesNotExistException;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.view.DesignDocument;
import com.couchbase.client.java.view.View;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
;
import org.springframework.data.couchbase.ContainerResourceRunner;
import org.springframework.data.couchbase.IntegrationTestApplicationConfig;
import org.springframework.data.couchbase.core.CouchbaseOperations;
import org.springframework.data.couchbase.repository.config.RepositoryOperationsMapping;
import org.springframework.data.couchbase.repository.support.CouchbaseRepositoryFactory;
import org.springframework.data.couchbase.repository.support.IndexManager;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;

/**
 * This tests automatic index creation features in the Couchbase connector.
 * Automatic index creation is performed before construction of the repository implementation.
 *
 * @author Simon Baslé
 */
@RunWith(ContainerResourceRunner.class)
@ContextConfiguration(classes = IntegrationTestApplicationConfig.class)
@TestExecutionListeners(IndexedRepositoryTestListener.class)
public class IndexedRepositoryTests {

  public static final String SECONDARY = "autogeneratedIndexIndexedUserN1qlSecondary";
  public static final String VIEW_DOC = "autogeneratedIndex";
  public static final String VIEW_NAME = "IndexedUserView";

  public static final String IGNORED_VIEW_NAME = "AnotherIndexedUserView";
  public static final String IGNORED_SECONDARY = "AnotherIndexedUserN1qlSecondary";

  @Autowired
  private RepositoryOperationsMapping operationsMapping;

  @Autowired
  private IndexManager indexManager;

  private CouchbaseOperations template;
  private RepositoryFactorySupport factory;

  private RepositoryFactorySupport ignoringIndexFactory;
  private IndexManager ignoringIndexManager = new IndexManager(false, false, false);

  @Before
  public void setup() throws Exception {
    factory = new CouchbaseRepositoryFactory(operationsMapping, indexManager);
    template = operationsMapping.getDefault();
    ignoringIndexFactory = new CouchbaseRepositoryFactory(operationsMapping, ignoringIndexManager);
  }

  @Test
  public void shouldFindN1qlPrimaryIndex() {
    IndexedUserRepository repository = factory.getRepository(IndexedUserRepository.class);

    String bucket = template.getCouchbaseBucket().name();
    N1qlQuery existQuery = N1qlQuery.simple("SELECT 1 FROM `"+ bucket +"`");
    N1qlQueryResult exist = template.queryN1QL(existQuery);

    assertTrue(exist.finalSuccess());
  }

  @Test
  public void shouldFindN1qlSecondaryIndex() {
    IndexedUserRepository repository = factory.getRepository(IndexedUserRepository.class);

    String bucket = template.getCouchbaseBucket().name();
    N1qlQuery existQuery = N1qlQuery.simple("SELECT 1 FROM `"+ bucket +"` USE INDEX (" +  SECONDARY +")");
    N1qlQueryResult exist = template.queryN1QL(existQuery);

    assertTrue(exist.finalSuccess());
  }

  @Test
  public void shouldFindViewIndex() {
    IndexedUserRepository repository = factory.getRepository(IndexedUserRepository.class);

    DesignDocument designDoc = null;
    try {
      designDoc = template.getCouchbaseBucket()
              .bucketManager()
              .getDesignDocument(VIEW_DOC);
    } catch(DesignDocumentDoesNotExistException ex) {

    }

    assertNotNull(designDoc);
    for (View view : designDoc.views()) {
      if (view.name().equals(VIEW_NAME)) return;
    }
    fail("View not found");
  }
  @Test
  public void shouldNotFindN1qlSecondaryIndexWithIgnoringIndexManager() {
    AnotherIndexedUserRepository repository = ignoringIndexFactory.getRepository(AnotherIndexedUserRepository.class);

    String bucket = template.getCouchbaseBucket().name();
    N1qlQuery existQuery = N1qlQuery.simple("SELECT 1 FROM `"+ bucket +"` USE INDEX (" +  IGNORED_SECONDARY +")");
    N1qlQueryResult exist = template.queryN1QL(existQuery);

    assertFalse(exist.finalSuccess());
  }

  @Test
  public void shouldNotFindViewIndexWithIgnoringIndexManager() {
    AnotherIndexedUserRepository repository = ignoringIndexFactory.getRepository(AnotherIndexedUserRepository.class);

    DesignDocument designDoc = null;
    try {
      designDoc = template.getCouchbaseBucket()
              .bucketManager()
              .getDesignDocument(VIEW_DOC);
    } catch(DesignDocumentDoesNotExistException ex) {
      //ignored
    }

    if (designDoc != null) {
      for (View view : designDoc.views()) {
        if (view.name().equals(IGNORED_VIEW_NAME)) fail("Found unexpected " + IGNORED_VIEW_NAME);
      }
    }
  }

  @Test
  public void shouldFindListOfIdsThroughDefaulViewIndexed() {
    IndexedFooRepository.Foo foo1 = new IndexedFooRepository.Foo("foo1", "foo", 1);
    IndexedFooRepository.Foo foo2 = new IndexedFooRepository.Foo("foo2", "bar", 2);

    IndexedFooRepository repository = factory.getRepository(IndexedFooRepository.class);

    DesignDocument designDoc = template.getCouchbaseBucket()
        .bucketManager()
        .getDesignDocument("foo");

    assertNotNull(designDoc);
    boolean foundView = false;
    for (View view : designDoc.views()) {
      if (view.name().equals("all")) {
        foundView = true;
        break;
      }
    }
    assertTrue("Expected to find view \"all\" on design document \"foo\"", foundView);

    repository.save(foo1);
    repository.save(foo2);

    int count = 0;
    for (Object o : repository.findAllById(Arrays.asList("foo1", "foo2"))) {
      count++;
    }
    assertEquals(2L, count);
    count = 0;
    for (Object o : repository.findAllById(Arrays.asList("foo1", "foo3"))) {
      count++;
    }
    assertEquals(1L, count);
  }
}
