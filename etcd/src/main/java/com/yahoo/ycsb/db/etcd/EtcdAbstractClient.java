/**
 * Copyright (c) 2013-2018 YCSB contributors. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License. See accompanying LICENSE file.
 * <p>
 */

package com.yahoo.ycsb.db.etcd;

import com.yahoo.ycsb.ByteIterator;
import com.yahoo.ycsb.DB;
import com.yahoo.ycsb.DBException;
import com.yahoo.ycsb.Status;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Etcd abstract client.
 * <p>
 * See {@code ignite/README.md} for details.
 */
public abstract class EtcdAbstractClient extends DB {
  /** */
  protected static Logger log = LogManager.getLogger(EtcdAbstractClient.class);

  protected static final String DEFAULT_CACHE_NAME = "usertable";
  protected static final String HOSTS_PROPERTY = "hosts";
  protected static final String PORTS_PROPERTY = "ports";
  protected static final String CLIENT_NODE_NAME = "YCSB client node";
  protected static final String PORTS_DEFAULTS = "47500..47509";

  /**
   * Count the number of times initialized to teardown on the last
   * {@link #cleanup()}.
   */
  protected static final AtomicInteger INIT_COUNT = new AtomicInteger(0);
  /** Debug flag. */
  protected static boolean debug = false;

  /**
   * Initialize any state for this DB. Called once per DB instance; there is one
   * DB instance per client thread.
   */
  @Override
  public void init() throws DBException {

    // Keep track of number of calls to init (for later cleanup)
    INIT_COUNT.incrementAndGet();

    // Synchronized so that we only have a single
    // cluster/session instance for all the threads.
    synchronized (INIT_COUNT) {

      try {
        /* main code */
      } catch (Exception e) {
        throw new DBException(e);
      }
    } // synchronized
  }

  /**
   * Cleanup any state for this DB. Called once per DB instance; there is one DB
   * instance per client thread.
   */
  @Override
  public void cleanup() throws DBException {
    synchronized (INIT_COUNT) {
      final int curInitCount = INIT_COUNT.decrementAndGet();

      if (curInitCount <= 0) {
      }

      if (curInitCount < 0) {
        // This should never happen.
        throw new DBException(
            String.format("initCount is negative: %d", curInitCount));
      }
    }
  }

  @Override
  public Status scan(String table, String startkey, int recordcount,
                     Set<String> fields, Vector<HashMap<String, ByteIterator>> result) {
    return Status.NOT_IMPLEMENTED;
  }
}
