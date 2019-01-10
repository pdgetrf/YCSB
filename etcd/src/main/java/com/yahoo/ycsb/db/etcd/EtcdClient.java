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

import com.coreos.jetcd.Client;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.kv.GetResponse;
import com.coreos.jetcd.options.GetOption;
import com.yahoo.ycsb.ByteIterator;
import com.yahoo.ycsb.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Set;

import static com.google.common.base.Charsets.UTF_8;


/**
 * Etcd client.
 * <p>
 * See {@code etcd/README.md} for details.
 */
public class EtcdClient extends EtcdAbstractClient {
  /** */
  private static Logger log = LogManager.getLogger(EtcdClient.class);

  /**
   * Read a record from the database. Each field/value pair from the result will
   * be stored in a HashMap.
   *
   * @param table  The name of the table
   * @param key    The record key of the record to read.
   * @param fields The list of fields to read, or null for all of them
   * @param result A HashMap of field/value pairs for the result
   * @return Zero on success, a non-zero error code on error
   */
  @Override
  public Status read(String table, String key, Set<String> fields,
                     Map<String, ByteIterator> result) {

    String endpoints = "http://127.0.0.1:2379";


    log.info("pengdu reading");
    try {
      Client client = Client.builder().endpoints(endpoints.split(",")).build();

      GetResponse getResponse = client.getKVClient().get(
          ByteSequence.fromString("fat"),
          GetOption.newBuilder().withRevision(0).build()
      ).get();


      log.info(key);
      log.info(getResponse.getKvs().get(0).getValue().toString(UTF_8));

      return Status.OK;

    } catch (Exception e) {
      log.error(String.format("Error reading key: %s", key), e);

      return Status.ERROR;
    }
  }

  /**
   * Update a record in the database. Any field/value pairs in the specified
   * values HashMap will be written into the record with the specified record
   * key, overwriting any existing values with the same field name.
   *
   * @param table  The name of the table
   * @param key    The record key of the record to write.
   * @param values A HashMap of field/value pairs to update in the record
   * @return Zero on success, a non-zero error code on error
   */
  @Override
  public Status update(String table, String key,
                       Map<String, ByteIterator> values) {
    try {
      /*
      main code
       */
      return Status.OK;
    } catch (Exception e) {
      log.error(String.format("Error updating key: %s", key), e);

      return Status.ERROR;
    }
  }

  /**
   * Insert a record in the database. Any field/value pairs in the specified
   * values HashMap will be written into the record with the specified record
   * key.
   *
   * @param table  The name of the table
   * @param key    The record key of the record to insert.
   * @param values A HashMap of field/value pairs to insert in the record
   * @return Zero on success, a non-zero error code on error
   */
  @Override
  public Status insert(String table, String key,
                       Map<String, ByteIterator> values) {


    String endpoints = "http://127.0.0.1:2379";

    try {

      Client client = Client.builder().endpoints(endpoints.split(",")).build();
      client.getKVClient().put(
          ByteSequence.fromString("hello"),
          ByteSequence.fromString("world")
      ).get();

      return Status.OK;
    } catch (Exception e) {
      log.error(String.format("Error inserting key: %s", key), e);

      return Status.ERROR;
    }
  }

  /**
   * Delete a record from the database.
   *
   * @param table The name of the table
   * @param key   The record key of the record to delete.
   * @return Zero on success, a non-zero error code on error
   */
  @Override
  public Status delete(String table, String key) {
    try {
      /*
      main code
       */
      return Status.OK;
    } catch (Exception e) {
      log.error(String.format("Error deleting key: %s ", key), e);
    }

    return Status.ERROR;
  }
}
